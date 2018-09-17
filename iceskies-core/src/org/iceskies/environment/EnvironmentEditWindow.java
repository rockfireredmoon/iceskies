package org.iceskies.environment;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.icelib.Icelib;
import org.icescene.HUDMessageAppState;
import org.icescene.HUDMessageAppState.Channel;
import org.icescene.IcesceneApp;
import org.icescene.environment.EnvironmentPhase;
import org.iceskies.environment.AbstractEnvironmentConfiguration.Format;
import org.iceui.XFileSelector;

import com.jme3.input.event.MouseButtonEvent;

import icetone.controls.buttons.PushButton;
import icetone.controls.lists.ComboBox;
import icetone.core.BaseElement;
import icetone.core.BaseScreen;
import icetone.core.Element;
import icetone.core.ToolKit;
import icetone.core.layout.ScreenLayoutConstraints;
import icetone.extras.windows.ButtonWindow;

public class EnvironmentEditWindow extends ButtonWindow<Element> {
	final static Logger LOG = Logger.getLogger(EnvironmentEditWindow.class.getName());

	private PushButton btnCancel;
	private EnvironmentEditPanel editPanel;
	private PushButton export;
	private ComboBox<Format> format;
	private PushButton copy;
	private HUDMessageAppState messages;
	private EnvironmentGroupConfiguration originalData;

	public EnvironmentEditWindow(String title, BaseScreen screen) {
		super(screen, true);
		setDestroyOnHide(false);
		setWindowTitle(title);
		setMovable(false);
		setButtonOkText("Save");
		screen.showElement(this, ScreenLayoutConstraints.center);
		messages = screen.getApplication().getStateManager().getState(HUDMessageAppState.class);
	}

	@Override
	public void onButtonOkPressed(MouseButtonEvent evt, boolean toggled) {
		this.originalData.copyFrom(editPanel.getEnvironmentData());
		Format selFormat = Format.JAVASCRIPT;
		File customEnvScript = ((IcesceneApp) ToolKit.get().getApplication()).getAssets().getExternalAssetFile(String
				.format("%s/Environment_%s.%s", "Environment", editPanel.getEnvironmentKey(), selFormat.toExtension()));
		try {
			saveToFile(selFormat, customEnvScript);
		} catch (Exception e) {
			messages.message(Channel.ERROR,
					String.format("Failed to save environment %s", editPanel.getEnvironmentData().getKey()), e);
			LOG.log(Level.SEVERE, "Failed to save environment.", e);
		}
	}

	@Override
	protected void createButtons(BaseElement buttons) {

		format = new ComboBox<Format>(screen);
		format.onChange(evt -> setAvailable());
		buttons.addElement(format);
		rebuildFormats();

		export = new PushButton(screen) {
			{
				setStyleClass("fancy");
			}
		};
		export.onMouseReleased(evt -> export());
		export.setText("Export");
		buttons.addElement(export);

		copy = new PushButton(screen) {
			{
				setStyleClass("fancy");
			}
		};
		copy.onMouseReleased(evt -> copy());
		copy.setText("Copy");
		buttons.addElement(copy);

		super.createButtons(buttons);
		btnCancel = new PushButton(screen, "Cancel") {
			{
				setStyleClass("cancel");
			}
		};
		btnCancel.onMouseReleased(evt -> hide());
		btnCancel.setText("Cancel");
		buttons.addElement(btnCancel);
		form.addFormElement(btnCancel);
	}

	public void setEnvironment(EnvironmentGroupConfiguration data) {
		this.originalData = data;
		data = (EnvironmentGroupConfiguration) data.clone();
		editPanel.setEnvironment(data);
	}

	public void export() {
		try {
			Format fmt = getSelectedFormat();
			File outFile = getDefaultOutputFile(fmt);
			File envDir = getEnvironmentsFolder();
			XFileSelector sel = XFileSelector.create(envDir.getPath());
			sel.setFileSelectionMode(XFileSelector.FILES_ONLY);
			sel.setSelectedFile(outFile);
			if (sel.showDialog(null, "Choose Output File") == XFileSelector.APPROVE_OPTION) {
				outFile = sel.getSelectedFile();
				saveToFile(fmt, outFile);
			}
		} catch (Exception e) {
			messages.message(Channel.ERROR,
					String.format("Faile to save environment %s", editPanel.getEnvironmentData().getKey()), e);
			LOG.log(Level.SEVERE, "Failed to save environment.", e);
		}
	}

	public void copy() {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			editPanel.getEnvironmentData().write(baos, getSelectedFormat());
			ToolKit.get().setClipboardText(new String(baos.toByteArray(), "UTF-8"));
			messages.message(Channel.INFORMATION,
					String.format("Environment %s copied to clipboard", editPanel.getEnvironmentData().getKey()));
		} catch (Exception e) {
			messages.message(Channel.ERROR,
					String.format("Faile to copy environment %s", editPanel.getEnvironmentData().getKey()), e);
			LOG.log(Level.SEVERE, "Failed to copy environment.", e);
		}
	}

	protected void saveToFile(Format fmt, File outFile) throws FileNotFoundException, IOException {
		FileOutputStream fos = new FileOutputStream(outFile);
		try {
			PrintWriter pw = new PrintWriter(fos);
			if (fmt.equals(Format.JAVASCRIPT)) {
				pw.println("__Environments =  __Environments;");
				pw.println("Scripts.require(\"Environment_Default.js\");");
				pw.println("with (JavaImporter(com.jme3.math, org.icelib.beans)) {");
			}
			editPanel.getEnvironmentData().write(fos, fmt);
			Map<EnvironmentPhase, String> ph = editPanel.getEnvironmentData().getPhases();
			for (Map.Entry<EnvironmentPhase, String> p : ph.entrySet()) {
				// TODO should only be writing this is it is not in any other
				// environment configuration. This is
				// going to be hard, as environment configurations don't really
				// know what script they are in
				AbstractEnvironmentConfiguration cfg = EnvironmentManager
						.get(ToolKit.get().getApplication().getAssetManager())
						.getEnvironmentConfiguration(p.getValue());
				if (cfg != null) {
					cfg.write(fos, fmt);
				}
			}
			if (fmt.equals(Format.JAVASCRIPT)) {
				pw.println("}");
			}
			pw.flush();
		} finally {
			fos.close();
		}
		messages.message(Channel.INFORMATION, String.format("Environment %s saved to %s",
				editPanel.getEnvironmentData().getKey(), outFile.getPath()));
	}

	@Override
	protected Element createContent() {
		editPanel = new EnvironmentEditPanel(screen) {
			@Override
			protected void onEditConfiguration() {
				hide();
			}

		};
		return editPanel;
	}

	protected File getDefaultOutputFile(Format fmt) {
		File envDir = getEnvironmentsFolder();
		File outFile = new File(envDir,
				"Environment_" + editPanel.getEnvironmentData().getKey() + "." + fmt.toExtension());
		return outFile;
	}

	protected File getEnvironmentsFolder() {
		File envDir = new File(((IcesceneApp) ToolKit.get().getApplication()).getAssets().getExternalAssetsFolder(),
				"Environment");
		if (!envDir.exists() && !envDir.mkdirs())
			LOG.warning(String.format("Failed to create directory %s", envDir));
		return envDir;
	}

	protected Format getSelectedFormat() {
		return (Format) format.getSelectedListItem().getValue();
	}

	private void setAvailable() {
		Format f = format == null ? null : (format.getSelectedListItem() == null ? null : getSelectedFormat());
		if (btnOk != null) {
			btnOk.setEnabled(f != null);
			copy.setEnabled(f != null);
			export.setEnabled(f != null);
		}
	}

	private void rebuildFormats() {
		if (format != null) {
			format.removeAllListItems();
			if (editPanel.getEnvironmentData() != null) {
				for (Format f : editPanel.getEnvironmentData().getOutputFormats()) {
					format.addListItem(Icelib.toEnglish(f), f);
				}
			}
		}
	}
}

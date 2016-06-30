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
import org.icescene.IcesceneApp;
import org.icescene.environment.EnvironmentPhase;
import org.iceskies.environment.AbstractEnvironmentConfiguration.Format;
import org.iceui.XFileSelector;
import org.iceui.controls.CancelButton;
import org.iceui.controls.FancyButton;
import org.iceui.controls.FancyButtonWindow;
import org.iceui.controls.FancyWindow;
import org.iceui.controls.UIUtil;

import com.jme3.input.event.MouseButtonEvent;
import com.jme3.math.Vector2f;

import icetone.controls.lists.ComboBox;
import icetone.core.Element;
import icetone.core.ElementManager;

public class EnvironmentEditWindow extends FancyButtonWindow<Element> {
	final static Logger LOG = Logger.getLogger(EnvironmentEditWindow.class.getName());

	private CancelButton btnCancel;
	private EnvironmentEditPanel editPanel;
	private FancyButton export;
	private ComboBox<Format> format;
	private FancyButton copy;
	private HUDMessageAppState messages;
	private EnvironmentGroupConfiguration originalData;

	public EnvironmentEditWindow(String title, ElementManager screen) {
		super(screen, new Vector2f(15, 15), FancyWindow.Size.SMALL, true);
		setDestroyOnHide(false);
		setWindowTitle(title);
		setButtonOkText("Save");
		sizeToContent();
		setIsResizable(false);
		setIsMovable(true);
		UIUtil.center(screen, this);
		screen.addElement(this);

		messages = screen.getApplication().getStateManager().getState(HUDMessageAppState.class);
	}

	@Override
	public void onButtonOkPressed(MouseButtonEvent evt, boolean toggled) {
		this.originalData.copyFrom(editPanel.getEnvironmentData());
		Format selFormat = Format.JAVASCRIPT;
		File customEnvScript = ((IcesceneApp) app).getAssets().getExternalAssetFile(
				String.format("%s/Environment_%s.%s", "Environment", editPanel.getEnvironmentKey(), selFormat.toExtension()));
		try {
			saveToFile(selFormat, customEnvScript);
			hideWithEffect();
		} catch (Exception e) {
			messages.message(Level.SEVERE, String.format("Failed to save environment %s", editPanel.getEnvironmentData().getKey()),
					e);
			LOG.log(Level.SEVERE, "Failed to save environment.", e);
		}
	}

	@Override
	protected void createButtons(Element buttons) {

		format = new ComboBox<Format>(screen) {
			@Override
			public void onChange(int selectedIndex, Format value) {
				setAvailable();
			}
		};
		buttons.addChild(format);
		rebuildFormats();

		export = new FancyButton(screen) {
			@Override
			public void onButtonMouseLeftUp(MouseButtonEvent evt, boolean toggled) {
				export();
			}
		};
		export.setText("Export");
		buttons.addChild(export);

		copy = new FancyButton(screen) {
			@Override
			public void onButtonMouseLeftUp(MouseButtonEvent evt, boolean toggled) {
				copy();
			}
		};
		copy.setText("Copy");
		buttons.addChild(copy);

		super.createButtons(buttons);
		btnCancel = new CancelButton(screen, getUID() + ":btnCancel") {
			@Override
			public void onButtonMouseLeftUp(MouseButtonEvent evt, boolean toggled) {
				hideWindow();
			}
		};
		btnCancel.setText("Cancel");
		buttons.addChild(btnCancel);
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
			messages.message(Level.SEVERE, String.format("Faile to save environment %s", editPanel.getEnvironmentData().getKey()),
					e);
			LOG.log(Level.SEVERE, "Failed to save environment.", e);
		}
	}

	public void copy() {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			editPanel.getEnvironmentData().write(baos, getSelectedFormat());
			screen.setClipboardText(new String(baos.toByteArray(), "UTF-8"));
			messages.message(Level.INFO,
					String.format("Environment %s copied to clipboard", editPanel.getEnvironmentData().getKey()));
		} catch (Exception e) {
			messages.message(Level.SEVERE, String.format("Faile to copy environment %s", editPanel.getEnvironmentData().getKey()),
					e);
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
				AbstractEnvironmentConfiguration cfg = EnvironmentManager.get(app.getAssetManager())
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
		messages.message(Level.INFO,
				String.format("Environment %s saved to %s", editPanel.getEnvironmentData().getKey(), outFile.getPath()));
	}

	@Override
	protected Element createContent() {
		editPanel = new EnvironmentEditPanel(screen) {
			@Override
			protected void onEditConfiguration() {
				hideWindow();
			}

		};
		return editPanel;
	}

	protected File getDefaultOutputFile(Format fmt) {
		File envDir = getEnvironmentsFolder();
		File outFile = new File(envDir, "Environment_" + editPanel.getEnvironmentData().getKey() + "." + fmt.toExtension());
		return outFile;
	}

	protected File getEnvironmentsFolder() {
		File envDir = new File(((IcesceneApp) app).getAssets().getExternalAssetsFolder(), "Environment");
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
			btnOk.setIsEnabled(f != null);
			copy.setIsEnabled(f != null);
			export.setIsEnabled(f != null);
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

package org.iceskies.environment;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import org.icelib.Icelib;
import org.icescene.environment.EnvironmentLight;
import org.iceskies.environment.AbstractEnvironmentConfiguration.Format;
import org.iceui.XFileSelector;

import com.jme3.font.BitmapFont.Align;
import com.jme3.font.BitmapFont.VAlign;
import com.jme3.scene.Node;

import icetone.controls.buttons.PushButton;
import icetone.controls.lists.ComboBox;
import icetone.core.BaseElement;
import icetone.core.Size;
import icetone.core.ToolKit;
import icetone.core.layout.Border;
import icetone.core.layout.BorderLayout;
import icetone.core.layout.mig.MigLayout;
import icetone.core.undo.UndoManager;
import icetone.extras.windows.PersistentWindow;
import icetone.extras.windows.SaveType;
import icetone.fontawesome.FontAwesome;

public class EnvironmentEditorAppState extends EnvironmentAppState {

	private final static Logger LOG = Logger.getLogger(EnvironmentEditorAppState.class.getName());

	private PersistentWindow environmentEditWindow;
	private AbstractEnvironmentConfigurationEditorPanel<AbstractEnvironmentConfiguration> environmentEditorPanel;
	private UndoManager undoManager;
	private ComboBox<Format> format;
	private PushButton saveEnv;
	private PushButton export;
	private PushButton copy;

	public EnvironmentEditorAppState(UndoManager undoManager, Preferences prefs, EnvironmentLight environmentLight,
			Node gameNode) {
		super(prefs, environmentLight, gameNode);
		this.undoManager = undoManager;
	}

	@Override
	protected void postInitialize() {
		// Resource scanning
		screen = app.getScreen();

		checkIfOpenWindow();
		super.postInitialize();
	}

	@Override
	protected void onCleanup() {
		super.onCleanup();
		if (environmentEditWindow != null) {
			environmentEditWindow.destroy();
		}
	}

	// @Override
	// protected void doEnvironmentChange(String name, EnvironmentPhase type) {
	// }

	@Override
	public void setEnvironment(AbstractEnvironmentConfiguration environmentConfiguration) {
		// When editing, we work on copies of the environment
		AbstractEnvironmentConfiguration copy = (AbstractEnvironmentConfiguration) environmentConfiguration.clone();
		super.setEnvironment(copy);
		checkIfOpenWindow();
		if (environmentEditorPanel != null) {
			environmentEditorPanel.setEnvironment(this.environmentConfiguration);
		}
		if (environmentEditWindow != null) {
			setEnvironmentWindowTitle();
		}

		rebuildFormats();
		setAvailable();
	}

	private void rebuildFormats() {
		if (format != null) {
			format.removeAllListItems();
			if (environmentConfiguration != null) {
				for (Format f : environmentConfiguration.getOutputFormats()) {
					format.addListItem(Icelib.toEnglish(f), f);
				}
			}
		}
	}

	private void checkIfOpenWindow() {
		LOG.info("Checking if environment editor should be opened.");
		if (screen != null && environmentEditWindow == null && environmentConfiguration != null) {
			environmentEditWindow();
			environmentEditorPanel.rebuild();
		}
	}

	@SuppressWarnings("unchecked")
	private void environmentEditWindow() {

		LOG.info("Creating environment editor.");
		environmentEditWindow = new PersistentWindow(screen, "EnvironmentEdit", VAlign.Top, Align.Right,
				new Size(410, 480), true, SaveType.POSITION_AND_SIZE, prefs) {
			@Override
			protected void onCloseWindow() {
				super.onCloseWindow();
				app.getStateManager().getState(EditableEnvironmentSwitcherAppState.class).setEdit(false);
			}
		};
		setEnvironmentWindowTitle();
		environmentEditWindow.setMinimizable(true);

		BaseElement contentArea = environmentEditWindow.getContentArea();
		contentArea.setLayoutManager(new BorderLayout(4, 4));
		contentArea.addElement(
				environmentEditorPanel = (AbstractEnvironmentConfigurationEditorPanel<AbstractEnvironmentConfiguration>) environmentConfiguration
						.createEditor(undoManager, screen, prefs),
				"span 4, growx");
		contentArea.addElement(createButtons(), Border.SOUTH);
		// environmentEditWindow.setIsResizable(false);
		screen.addElement(environmentEditWindow);
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
			error(String.format("Faile to save environment %s", environmentConfiguration.getKey()), e);
			LOG.log(Level.SEVERE, "Failed to save environment.", e);
		}
	}

	public void copy() {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			environmentConfiguration.write(baos, getSelectedFormat());
			ToolKit.get().setClipboardText(new String(baos.toByteArray(), "UTF-8"));
			info(String.format("Environment %s copied to clipboard", environmentConfiguration.getKey()));
		} catch (Exception e) {
			error(String.format("Faile to copy environment %s", environmentConfiguration.getKey()), e);
			LOG.log(Level.SEVERE, "Failed to copy environment.", e);
		}
	}

	public void save() {
		try {
			Format fmt = getSelectedFormat();
			File outFile = getDefaultOutputFile(fmt);
			saveToFile(fmt, outFile);
		} catch (Exception e) {
			error(String.format("Faile to save environment %s", environmentConfiguration.getKey()), e);
			LOG.log(Level.SEVERE, "Failed to save environment.", e);
		}
	}

	protected Format getSelectedFormat() {
		return (Format) format.getSelectedListItem().getValue();
	}

	protected File getDefaultOutputFile(Format fmt) {
		File envDir = getEnvironmentsFolder();
		File outFile = new File(envDir, "Environment_" + environmentConfiguration.getKey() + "." + fmt.toExtension());
		return outFile;
	}

	protected File getEnvironmentsFolder() {
		File envDir = new File(app.getAssets().getExternalAssetsFolder(), "Environment");
		if (!envDir.exists() && !envDir.mkdirs())
			LOG.warning(String.format("Failed to create directory %s", envDir));
		return envDir;
	}

	protected BaseElement createButtons() {
		BaseElement bottom = new BaseElement(screen);
		bottom.setLayoutManager(new MigLayout(screen, "", "[]push[][][]", "[]"));

		format = new ComboBox<Format>(screen);
		format.onChange(evt -> setAvailable());
		bottom.addElement(format);
		rebuildFormats();

		saveEnv = new PushButton(screen) {
			{
				setStyleClass("fancy");
			}
		};
		FontAwesome.SAVE.button(24, saveEnv);
		saveEnv.onMouseReleased(evt -> save());
		saveEnv.setText("Save");
		bottom.addElement(saveEnv);

		export = new PushButton(screen) {
			{
				setStyleClass("fancy");
			}
		};
		export.onMouseReleased(evt -> export());
		export.setText("Export");
		FontAwesome.HDD_O.button(24, export);
		bottom.addElement(export);

		copy = new PushButton(screen) {
			{
				setStyleClass("fancy");
			}
		};
		copy.onMouseReleased(evt -> copy());
		copy.setText("Copy");
		FontAwesome.COPY.button(24, copy);
		bottom.addElement(copy);

		setAvailable();

		return bottom;
	}

	protected void saveToFile(Format fmt, File outFile) throws FileNotFoundException, IOException {
		FileOutputStream fos = new FileOutputStream(outFile);
		try {
			environmentConfiguration.write(fos, fmt);
		} finally {
			fos.close();
		}
		info(String.format("Environment %s saved to %s", environmentConfiguration.getKey(), outFile.getPath()));
	}

	private void setEnvironmentWindowTitle() {
		environmentEditWindow.setWindowTitle(String.format("Environment - %s", environmentConfiguration.getKey()));
	}

	private void setAvailable() {
		Format f = format == null ? null : (format.getSelectedListItem() == null ? null : getSelectedFormat());
		if (saveEnv != null) {
			saveEnv.setEnabled(f != null);
			copy.setEnabled(f != null);
			export.setEnabled(f != null);
		}
	}

}

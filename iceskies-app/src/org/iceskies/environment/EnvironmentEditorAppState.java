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
import org.icelib.UndoManager;
import org.icescene.environment.EnvironmentLight;
import org.iceskies.app.SkiesConfig;
import org.iceskies.environment.AbstractEnvironmentConfiguration.Format;
import org.iceui.HPosition;
import org.iceui.UIConstants;
import org.iceui.VPosition;
import org.iceui.XFileSelector;
import org.iceui.controls.FancyButton;
import org.iceui.controls.FancyPersistentWindow;
import org.iceui.controls.FancyWindow;
import org.iceui.controls.SaveType;
import org.iceui.effects.EffectHelper;

import com.jme3.input.event.MouseButtonEvent;
import com.jme3.math.Vector2f;
import com.jme3.scene.Node;

import icetone.controls.lists.ComboBox;
import icetone.core.Element;
import icetone.core.layout.BorderLayout;
import icetone.core.layout.mig.MigLayout;
import icetone.effects.Effect;

public class EnvironmentEditorAppState extends EnvironmentAppState {

	private final static Logger LOG = Logger.getLogger(EnvironmentEditorAppState.class.getName());

	private FancyPersistentWindow environmentEditWindow;
	private AbstractEnvironmentConfigurationEditorPanel<AbstractEnvironmentConfiguration> environmentEditorPanel;
	private UndoManager undoManager;
	private ComboBox<Format> format;
	private FancyButton saveEnv;
	private FancyButton export;
	private FancyButton copy;

	public EnvironmentEditorAppState(UndoManager undoManager, Preferences prefs, EnvironmentLight environmentLight, Node gameNode) {
		super(prefs, environmentLight, gameNode);
		addPrefKeyPattern(SkiesConfig.ENVIRONMENT_EDITOR + ".*");
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
		if (environmentEditWindow != null && environmentEditWindow.getIsVisible()) {
			new EffectHelper()
					.effect(environmentEditWindow, Effect.EffectType.FadeOut, Effect.EffectEvent.Hide, UIConstants.UI_EFFECT_TIME)
					.setDestroyOnHide(true);
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
		environmentEditWindow = new FancyPersistentWindow(screen, "EnvironmentEdit",
				screen.getStyle("Common").getInt("defaultWindowOffset"), VPosition.TOP, HPosition.RIGHT, new Vector2f(410, 480),
				FancyWindow.Size.SMALL, true, SaveType.POSITION_AND_SIZE, prefs) {
			@Override
			protected void onCloseWindow() {
				super.onCloseWindow();
				app.getStateManager().getState(EditableEnvironmentSwitcherAppState.class).setEdit(false);
			}
		};
		setEnvironmentWindowTitle();
		environmentEditWindow.setMinimizable(true);

		Element contentArea = environmentEditWindow.getContentArea();
		contentArea.setLayoutManager(new BorderLayout(4, 4));
		contentArea.addChild(
				environmentEditorPanel = (AbstractEnvironmentConfigurationEditorPanel<AbstractEnvironmentConfiguration>) environmentConfiguration
						.createEditor(undoManager, screen, prefs),
				"span 4, growx");
		contentArea.addChild(createButtons(), BorderLayout.Border.SOUTH);
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
			screen.setClipboardText(new String(baos.toByteArray(), "UTF-8"));
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

	protected Element createButtons() {
		Element bottom = new Element(screen);
		bottom.setLayoutManager(new MigLayout(screen, "", "[]push[][][]", "[]"));

		format = new ComboBox<Format>(screen) {
			@Override
			public void onChange(int selectedIndex, Format value) {
				setAvailable();
			}
		};
		bottom.addChild(format);
		rebuildFormats();

		saveEnv = new FancyButton(screen) {
			@Override
			public void onButtonMouseLeftUp(MouseButtonEvent evt, boolean toggled) {
				save();
			}
		};
		saveEnv.setText("Save");
		bottom.addChild(saveEnv);

		export = new FancyButton(screen) {
			@Override
			public void onButtonMouseLeftUp(MouseButtonEvent evt, boolean toggled) {
				export();
			}
		};
		export.setText("Export");
		bottom.addChild(export);

		copy = new FancyButton(screen) {
			@Override
			public void onButtonMouseLeftUp(MouseButtonEvent evt, boolean toggled) {
				copy();
			}
		};
		copy.setText("Copy");
		bottom.addChild(copy);

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
			saveEnv.setIsEnabled(f != null);
			copy.setIsEnabled(f != null);
			export.setIsEnabled(f != null);
		}
	}

}

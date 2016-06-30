package org.iceskies.app;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import org.apache.commons.io.FileUtils;
import org.icelib.Icelib;
import org.icelib.XDesktop;
import org.icescene.IcemoonAppState;
import org.icescene.IcesceneApp;
import org.icescene.ServiceRef;
import org.icescene.environment.EnvironmentPhase;
import org.icescene.help.HelpAppState;
import org.icescene.options.OptionsAppState;
import org.iceskies.environment.AbstractEnvironmentConfiguration;
import org.iceskies.environment.EditableEnvironmentSwitcherAppState;
import org.iceskies.environment.EnvironmentEditWindow;
import org.iceskies.environment.EnvironmentGroupConfiguration;
import org.iceskies.environment.EnvironmentManager;
import org.iceskies.environment.EnvironmentSwitcherAppState;
import org.iceskies.environment.EnvironmentSwitcherAppState.EnvPriority;
import org.iceskies.environment.Environments;
import org.iceskies.environment.LocalEnvironments;
import org.iceskies.environment.enhanced.EnhancedEnvironmentConfiguration;
import org.iceskies.environment.legacy.LegacyEnvironmentConfiguration;
import org.iceui.controls.FancyButton;
import org.iceui.controls.FancyInputBox;
import org.iceui.controls.FancyWindow;
import org.iceui.controls.UIUtil;
import org.iceui.controls.XSeparator;
import org.iceui.controls.ZMenu;

import com.jme3.input.event.MouseButtonEvent;
import com.jme3.math.Vector2f;

import icetone.core.Container;
import icetone.core.Element;
import icetone.core.Element.ZPriority;
import icetone.core.layout.mig.MigLayout;

public class MenuAppState extends IcemoonAppState<IcemoonAppState<?>> {

	public enum MenuActions {

		OPEN_ENVIRONMENTS_FOLDER
	}

	private static final Logger LOG = Logger.getLogger(MenuAppState.class.getName());
	private Container layer;
	private FancyButton options;
	private FancyButton exit;
	private File cloningTerrainDirFile;
	private FancyButton help;
	private FancyButton environment;
	private EnvironmentManager manager;

	@ServiceRef
	protected static Environments environments;

	public MenuAppState(Preferences prefs) {
		super(prefs);
	}

	@Override
	protected void postInitialize() {
		manager = EnvironmentManager.get(assetManager);

		layer = new Container(screen);
		layer.setLayoutManager(new MigLayout(screen, "fill", "push[][][][][]push", "[]push"));

		// Terrain
		environment = new FancyButton(screen) {
			@Override
			public void onButtonMouseLeftUp(MouseButtonEvent evt, boolean toggled) {
				ZMenu menu = createEnvironmentMenu();
				menu.showMenu(null, evt.getX(), evt.getY());
			}
		};
		environment.setText("Environment");
		layer.addChild(environment);

		// Options
		options = new FancyButton(screen) {
			@Override
			public void onButtonMouseLeftUp(MouseButtonEvent evt, boolean toggled) {
				toggleOptions();
			}
		};
		options.setText("Options");
		layer.addChild(options);

		// Help
		help = new FancyButton(screen) {
			@Override
			public void onButtonMouseLeftUp(MouseButtonEvent evt, boolean toggled) {
				help();
			}
		};
		help.setText("Help");
		layer.addChild(help);

		// Exit
		exit = new FancyButton(screen) {
			@Override
			public void onButtonMouseLeftUp(MouseButtonEvent evt, boolean toggled) {
				exitApp();
			}
		};
		exit.setText("Exit");
		layer.addChild(exit);

		//
		app.getLayers(ZPriority.MENU).addChild(layer);
	}

	@Override
	protected void onCleanup() {
		app.getLayers(ZPriority.MENU).removeChild(layer);
	}

	private void help() {
		HelpAppState has = app.getStateManager().getState(HelpAppState.class);
		if (has == null) {
			app.getStateManager().attach(new HelpAppState(prefs));
		} else {
			app.getStateManager().detach(has);
		}
	}

	private void toggleOptions() {
		final OptionsAppState state = stateManager.getState(OptionsAppState.class);
		if (state == null) {
			stateManager.attach(new OptionsAppState(prefs));
		} else {
			stateManager.detach(state);
		}
	}

	private File getEnvironmentsFolder() {
		File envDir = new File(app.getAssets().getExternalAssetsFolder(), "Environment");
		if (!envDir.exists() && !envDir.mkdirs())
			LOG.warning(String.format("Failed to create directory %s", envDir));
		return envDir;
	}

	private ZMenu createEnvironmentMenu() {

		final SkiesSwitcherAppState env = stateManager.getState(SkiesSwitcherAppState.class);
		ZMenu menu = new ZMenu(screen) {
			@Override
			public void onItemSelected(ZMenuItem item) {
				super.onItemSelected(item);
				if (MenuActions.OPEN_ENVIRONMENTS_FOLDER.equals(item.getValue())) {
					final File terrainFolder = getEnvironmentsFolder();
					try {
						XDesktop.getDesktop().open(terrainFolder);
					} catch (IOException ex) {
						LOG.log(Level.SEVERE, String.format("Failed to open terrain folder %s", terrainFolder), ex);
						error(String.format("Failed to open terrain folder %s", terrainFolder), ex);
					}
				} else if (Boolean.FALSE.equals(item.getValue())) {
					env.setEnvironment(EnvPriority.VIEWING, null);
				} else if (Boolean.TRUE.equals(item.getValue())) {
					editEnvironment();
				} else if (String.class == item.getValue()) {
					newEnvironment();
				}
			}

		};
		for (MenuActions n : MenuActions.values()) {
			menu.addMenuItem(Icelib.toEnglish(n), n);
		}
		// Environments
		ZMenu environmentsMenu = new ZMenu(screen) {
			@Override
			public void onItemSelected(ZMenuItem item) {
				EnvironmentSwitcherAppState sas = app.getStateManager().getState(EnvironmentSwitcherAppState.class);
				sas.setEnvironment(EnvPriority.VIEWING, ((String) item.getValue()));
			}
		};
		menu.addMenuItem("Open", environmentsMenu, null);
		for (String k : manager.getEnvironments()) {
			environmentsMenu.addMenuItem(k, k);
		}

		menu.addMenuItem("New Environment", null, String.class);
		if (env.getEnvironment() != null) {
			menu.addMenuItem("Edit", Boolean.TRUE);
			menu.addMenuItem("Close", Boolean.FALSE);
		}

		// Set current time of day
		if (env.getEnvironment() != null) {
			ZMenu timeOfDay = new ZMenu(screen) {
				@Override
				public void onItemSelected(ZMenuItem item) {
					EnvironmentSwitcherAppState sas = app.getStateManager().getState(EnvironmentSwitcherAppState.class);
					sas.setPhase((EnvironmentPhase) item.getValue());
				}
			};
			timeOfDay.addMenuItem(Icelib.toEnglish(EnvironmentPhase.SUNRISE), EnvironmentPhase.SUNRISE);
			timeOfDay.addMenuItem(Icelib.toEnglish(EnvironmentPhase.DAY), EnvironmentPhase.DAY);
			timeOfDay.addMenuItem(Icelib.toEnglish(EnvironmentPhase.SUNSET), EnvironmentPhase.SUNSET);
			timeOfDay.addMenuItem(Icelib.toEnglish(EnvironmentPhase.NIGHT), EnvironmentPhase.NIGHT);
			menu.addMenuItem("Set Time Of Day", timeOfDay, null);
			menu.addMenuItem(null, new XSeparator(screen, Element.Orientation.HORIZONTAL), null).setSelectable(false);
		}
		// Environment configurations (that make up 'Environments')
		ZMenu configurationsMenu = new ZMenu(screen) {
			@Override
			public void onItemSelected(ZMenuItem item) {
				if (Boolean.FALSE.equals(item.getValue())) {
					if(env instanceof EditableEnvironmentSwitcherAppState && ((EditableEnvironmentSwitcherAppState)env).isEdit()) {
						env.setEdit(false);
					}
					env.setEnvironment(EnvPriority.VIEWING, null);
				}
				else if (Boolean.TRUE.equals(item.getValue())) {
					editEnvironmentConfiguration();
				}
			}
		};

		// New environment configuration
		ZMenu newConfig = new ZMenu(screen) {
			@SuppressWarnings("unchecked")
			@Override
			public void onItemSelected(ZMenuItem item) {
				newEnvironmentConfiguration((Class<? extends AbstractEnvironmentConfiguration>) item.getValue());
			}
		};
		newConfig.addMenuItem("Enhanced", EnhancedEnvironmentConfiguration.class);
		newConfig.addMenuItem("Legacy", LegacyEnvironmentConfiguration.class);
		configurationsMenu.addMenuItem("New", newConfig, null).setSelectable(false);

		// Environments
		ZMenu openConfigurationMenu = new ZMenu(screen) {
			@Override
			public void onItemSelected(ZMenuItem item) {
				SkiesSwitcherAppState sas = app.getStateManager().getState(SkiesSwitcherAppState.class);
				if(env instanceof EditableEnvironmentSwitcherAppState && ((EditableEnvironmentSwitcherAppState)env).isEdit())
					sas.setEnvironment(EnvPriority.EDITING, ((String) item.getValue()));
				else
					sas.setEnvironment(EnvPriority.VIEWING, ((String) item.getValue()));
			}
		};
		for (String k : manager.getEnvironmentConfigurations()) {
			openConfigurationMenu.addMenuItem(k, k);
		}
		configurationsMenu.addMenuItem("Open", openConfigurationMenu, null);
		if (env.getEnvironmentConfiguration() != null) {
			configurationsMenu.addMenuItem("Edit", Boolean.TRUE);
			configurationsMenu.addMenuItem("Close", Boolean.FALSE);
		}
		menu.addMenuItem("Configurations", configurationsMenu, null);

		screen.addElement(menu);
		return menu;
	}

	private void newEnvironment() {
		changeEnvironment("New Environment");

	}

	private void editEnvironment() {
		SkiesSwitcherAppState sas = app.getStateManager().getState(SkiesSwitcherAppState.class);
		changeEnvironment("Edit Environment").setEnvironment(manager.getEnvironments(sas.getEnvironment()));
		// sas.setEdit(true);
	}

	private void newEnvironmentConfiguration(final Class<? extends AbstractEnvironmentConfiguration> clazz) {

		final FancyInputBox dialog = new FancyInputBox(screen, new Vector2f(15, 15), FancyWindow.Size.LARGE, true) {
			@Override
			public void onButtonCancelPressed(MouseButtonEvent evt, boolean toggled) {
				hideWindow();
			}

			@Override
			public void onButtonOkPressed(MouseButtonEvent evt, String text, boolean toggled) {
				manager.newConfiguration(text, clazz);
				hideWindow();
				SkiesSwitcherAppState sas = app.getStateManager().getState(SkiesSwitcherAppState.class);
				sas.setEnvironment(EnvPriority.VIEWING, text);
				sas.setEdit(true);
			}
		};
		dialog.setDestroyOnHide(true);
		dialog.getDragBar().setFontColor(screen.getStyle("Common").getColorRGBA("warningColor"));
		dialog.setWindowTitle("New Environment");
		dialog.setButtonOkText("Create");
		dialog.setMsg("");
		dialog.setWidth(300);
		dialog.setIsResizable(false);
		dialog.setIsMovable(false);
		dialog.sizeToContent();
		UIUtil.center(screen, dialog);
		screen.addElement(dialog, null, true);
		dialog.showAsModal(true);
	}

	private void editEnvironmentConfiguration() {
		SkiesSwitcherAppState sas = app.getStateManager().getState(SkiesSwitcherAppState.class);
		String config = sas.getEnvironmentConfiguration();
		AbstractEnvironmentConfiguration envConfig = manager.getEnvironmentConfiguration(config);
		if (envConfig.isEditable()) {
			sas.setEdit(true);
		} else {
			error("This type of environment is not currently editable using the sky editor. You may be able to manually create them (for example 'Legacy' environments can be created using a JME3 .material file).");
		}
	}

	private EnvironmentEditWindow changeEnvironment(String title) {
		return new EnvironmentEditWindow(title, screen)
		// {
		//
		// @Override
		// protected void onSave(String key, EnvironmentGroupConfiguration data)
		// {
		// File customEnvScript = ((IcesceneApp) app).getAssets()
		// .getExternalAssetFile(String.format("%s/%s", "Environment",
		// "Environment_Local.js"));
		//
		// try {
		// LocalEnvironments le = new LocalEnvironments(customEnvScript);
		// data.setKey(key);
		// le.env(data);
		// le.write();
		//
		// environments.remove(key);
		// environments.env(data);
		//
		// EditableEnvironmentSwitcherAppState eesa = app.getStateManager()
		// .getState(EditableEnvironmentSwitcherAppState.class);
		// if (eesa != null) {
		// eesa.reload();
		// }
		//
		// info(String.format("Saved local environment to %s",
		// customEnvScript));
		// } catch (Exception e) {
		// LOG.log(Level.SEVERE, "Failed to save local environment script.", e);
		// error("Failed to save local environment script.", e);
		// }
		//
		// }
		// }
		;
	}

	private void exitApp() {
		// if (loader.isNeedsSave()) {
		// final FancyDialogBox dialog = new FancyDialogBox(screen, new
		// Vector2f(15, 15), FancyWindow.Size.LARGE, true) {
		// @Override
		// public void onButtonCancelPressed(MouseButtonEvent evt, boolean
		// toggled) {
		// hideWindow();
		// }
		//
		// @Override
		// public void onButtonOkPressed(MouseButtonEvent evt, boolean toggled)
		// {
		// app.stop();
		// }
		// };
		// dialog.setDestroyOnHide(true);
		// dialog.getDragBar().setFontColor(screen.getStyle("Common").getColorRGBA("warningColor"));
		// dialog.setWindowTitle("Confirm Exit");
		// dialog.setButtonOkText("Exit");
		// dialog.setMsg("You have unsaved edits! Are you sure you wish to
		// exit?");
		//
		// dialog.setIsResizable(false);
		// dialog.setIsMovable(false);
		// UIUtil.center(screen, dialog);
		// dialog.showAsModal(true);
		// screen.addElement(dialog);
		// dialog.pack(false);
		// } else {
		// if (cloning) {
		// LOG.info("Interrupting cloneing");
		// cloneThread.interrupt();
		// LOG.info("Interrupted cloneing");
		// }
		app.stop();
		// }
	}

	protected void clearUpClonedDirectory() {
		if (cloningTerrainDirFile != null) {
			try {
				LOG.info(String.format("Clearing up partially cloned directory %s", cloningTerrainDirFile));
				FileUtils.deleteDirectory(cloningTerrainDirFile);
			} catch (IOException ex) {
				LOG.log(Level.SEVERE, String.format("Failed to clearing up partially cloned directory.%", cloningTerrainDirFile));
			} finally {
				cloningTerrainDirFile = null;
			}

		}
	}
}

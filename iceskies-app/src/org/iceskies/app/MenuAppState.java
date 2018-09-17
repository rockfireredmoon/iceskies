package org.iceskies.app;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import org.icelib.AppInfo;
import org.icelib.Icelib;
import org.icelib.XDesktop;
import org.icescene.IcemoonAppState;
import org.icescene.IcesceneApp;
import org.icescene.SceneConstants;
import org.icescene.ServiceRef;
import org.icescene.configuration.TerrainTemplateConfiguration;
import org.icescene.environment.EnvironmentLight;
import org.icescene.environment.EnvironmentPhase;
import org.icescene.help.HelpAppState;
import org.icescene.options.OptionsAppState;
import org.iceskies.environment.AbstractEnvironmentConfiguration;
import org.iceskies.environment.EditableEnvironmentSwitcherAppState;
import org.iceskies.environment.EnvironmentEditWindow;
import org.iceskies.environment.EnvironmentManager;
import org.iceskies.environment.EnvironmentSwitcherAppState;
import org.iceskies.environment.EnvironmentSwitcherAppState.EnvPriority;
import org.iceskies.environment.Environments;
import org.iceskies.environment.enhanced.EnhancedEnvironmentConfiguration;
import org.iceskies.environment.legacy.LegacyEnvironmentConfiguration;
import org.iceui.actions.ActionAppState;
import org.iceui.actions.ActionMenu;
import org.iceui.actions.ActionMenuBar;
import org.iceui.actions.AppAction;
import org.iceui.controls.ElementStyle;

import com.jme3.input.event.MouseButtonEvent;
import com.jme3.math.Vector2f;
import com.jme3.scene.Node;

import icetone.controls.containers.Frame;
import icetone.core.layout.ScreenLayoutConstraints;
import icetone.extras.windows.AlertBox;
import icetone.extras.windows.InputBox;

public class MenuAppState extends IcemoonAppState<IcemoonAppState<?>> {

	public enum CloneType {
		official, iceclient
	}

	public interface OnCloneCallback {
		void run(TerrainTemplateConfiguration targetTemplate, Frame input);
	}

	private static final Logger LOG = Logger.getLogger(MenuAppState.class.getName());

	@ServiceRef
	protected static Environments environments;

	private EnvironmentManager manager;
	private boolean loading;

	private ActionMenuBar menuBar;
	private AppAction close;

	private AppAction timeOfDay;

	public MenuAppState(Preferences prefs) {
		super(prefs);
	}

	@Override
	protected void postInitialize() {
		manager = EnvironmentManager.get(assetManager);

		ActionAppState appState = app.getStateManager().getState(ActionAppState.class);
		menuBar = appState.getMenuBar();
		menuBar.invalidate();

		/* Menus */
		menuBar.addActionMenu(new ActionMenu("File", 0));
		menuBar.addActionMenu(new ActionMenu("Environment", 10));
		menuBar.addActionMenu(new ActionMenu("Help", 20));

		/* Actions */
		menuBar.addAction(new AppAction("Options", evt -> toggleOptions()).setMenu("File").setMenuGroup(80));
		menuBar.addAction(new AppAction("Exit", evt -> exitApp()).setMenu("File").setMenuGroup(99));

		/* Environment menu */
		menuBar.addAction(new AppAction("New", evt -> newEnvironment()).setMenu("Environment"));
		menuBar.addAction(new AppAction(new ActionMenu("Open")).setMenu("Environment"));
		menuBar.addAction(new AppAction("Edit", evt -> editEnvironment()).setMenu("Environment"));
		menuBar.addAction(close = new AppAction("Close", evt -> stateManager.getState(EnvironmentSwitcherAppState.class)
				.setEnvironment(EnvPriority.VIEWING, null)).setMenu("Environment"));
		menuBar.addAction(new AppAction(new ActionMenu("Configurations")).setMenu("Environment"));
		menuBar.addAction(timeOfDay = new AppAction(new ActionMenu("Time Of Day")).setMenu("Environment"));

		/* Time Of Day */
		menuBar.addAction(
				new AppAction(Icelib.toEnglish(EnvironmentPhase.SUNRISE), evt -> setPhase(EnvironmentPhase.SUNRISE))
						.setMenu("Time Of Day"));
		menuBar.addAction(new AppAction(Icelib.toEnglish(EnvironmentPhase.DAY), evt -> setPhase(EnvironmentPhase.DAY))
				.setMenu("Time Of Day"));
		menuBar.addAction(
				new AppAction(Icelib.toEnglish(EnvironmentPhase.SUNSET), evt -> setPhase(EnvironmentPhase.SUNSET))
						.setMenu("Time Of Day"));
		menuBar.addAction(
				new AppAction(Icelib.toEnglish(EnvironmentPhase.NIGHT), evt -> setPhase(EnvironmentPhase.NIGHT))
						.setMenu("Time Of Day"));

		/* Environment configurations */
		menuBar.addAction(new AppAction(new ActionMenu("New")).setMenu("Configurations"));
		menuBar.addAction(
				new AppAction("Enhanced", evt -> newEnvironmentConfiguration(EnhancedEnvironmentConfiguration.class))
						.setMenu("New"));
		menuBar.addAction(
				new AppAction("Legacy", evt -> newEnvironmentConfiguration(LegacyEnvironmentConfiguration.class))
						.setMenu("New"));
		menuBar.addAction(new AppAction("Edit", evt -> editEnvironmentConfiguration()).setMenu("Configurations"));
		menuBar.addAction(new AppAction(new ActionMenu("Open Configuration")).setMenu("Configurations"));

		/* Help Actions */
		menuBar.addAction(new AppAction("Contents", evt -> help()).setMenu("Help"));
		menuBar.addAction(new AppAction("About", evt -> helpAbout()).setMenu("Help"));

		menuBar.validate();

		/* Initial availability */
		loading = true;
		setAvailable();

		/* Background load the terrain menu */

		app.getWorldLoaderExecutorService().execute(new Runnable() {

			@Override
			public String toString() {
				return "Loading available terrain and environments";
			}

			@Override
			public void run() {

				final List<AppAction> actions = new ArrayList<>();

				for (String k : manager.getEnvironments()) {
					actions.add(new AppAction(k, evt -> stateManager.getState(EnvironmentSwitcherAppState.class)
							.setEnvironment(EnvPriority.VIEWING, k)).setMenu("Open"));
				}

				List<String> envs = manager.getEnvironmentConfigurations();
				for (String k : envs) {
					actions.add(new AppAction(k, evt -> {
						final EnvironmentSwitcherAppState env = stateManager
								.getState(EnvironmentSwitcherAppState.class);
						if (env instanceof EditableEnvironmentSwitcherAppState
								&& ((EditableEnvironmentSwitcherAppState) env).isEdit())
							env.setEnvironment(EnvPriority.EDITING, k);
						else
							env.setEnvironment(EnvPriority.VIEWING, k);
					}).setMenu("Open Configuration"));
				}

				app.enqueue(new Callable<Void>() {
					@Override
					public Void call() throws Exception {
						menuBar.invalidate();
						actions.forEach((a) -> menuBar.addAction(a));
						menuBar.validate();
						loading = false;
						setAvailable();
						return null;
					}
				});
			}
		});

	}

	protected void setPhase(EnvironmentPhase phase) {
		EnvironmentSwitcherAppState sas = app.getStateManager().getState(EnvironmentSwitcherAppState.class);
		sas.setPhase(phase);
	}

	@Override
	protected void onCleanup() {
	}

	private void newEnvironmentConfiguration(final Class<? extends AbstractEnvironmentConfiguration> clazz) {
		final InputBox dialog = new InputBox(screen, new Vector2f(15, 15), true) {
			{
				setStyleClass("large");
			}

			@Override
			public void onButtonCancelPressed(MouseButtonEvent evt, boolean toggled) {
				hide();
			}

			@Override
			public void onButtonOkPressed(MouseButtonEvent evt, String text, boolean toggled) {
				manager.newConfiguration(text, clazz);
				hide();
				EditableEnvironmentSwitcherAppState sas = app.getStateManager()
						.getState(EditableEnvironmentSwitcherAppState.class);
				sas.setEnvironment(EnvPriority.VIEWING, text);
				sas.setEdit(true);
			}
		};
		dialog.setDestroyOnHide(true);
		ElementStyle.warningColor(dialog.getDragBar());
		dialog.setWindowTitle("New Environment");
		dialog.setButtonOkText("Create");
		dialog.setMsg("");
		dialog.setModal(true);
		screen.showElement(dialog, ScreenLayoutConstraints.center);
	}

	private void newEnvironment() {
		changeEnvironment("New Environment");

	}

	private void editEnvironmentConfiguration() {
		EditableEnvironmentSwitcherAppState sas = app.getStateManager()
				.getState(EditableEnvironmentSwitcherAppState.class);
		String config = sas.getEnvironmentConfiguration();
		AbstractEnvironmentConfiguration envConfig = manager.getEnvironmentConfiguration(config);
		if (envConfig.isEditable()) {
			sas.setEdit(true);
		} else {
			error("This type of environment is not currently editable using the sky editor.");
		}
	}

	private void editEnvironment() {
		EnvironmentSwitcherAppState sas = app.getStateManager().getState(EnvironmentSwitcherAppState.class);
		changeEnvironment("Edit Environment").setEnvironment(manager.getEnvironments(sas.getEnvironment()));
		// sas.setEdit(true);
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

	private void helpAbout() {
		AlertBox alert = new AlertBox(screen, true) {

			@Override
			public void onButtonOkPressed(MouseButtonEvent evt, boolean toggled) {
				hide();
			}
		};
		alert.setModal(true);
		alert.setTitle("About");
		alert.setText("<h1>" + AppInfo.getName() + "</h1><h4>Version " + AppInfo.getVersion() + "</h4>");
		screen.showElement(alert, ScreenLayoutConstraints.center);
	}

	private void help() {
		HelpAppState has = app.getStateManager().getState(HelpAppState.class);
		if (has == null) {
			app.getStateManager().attach(new HelpAppState(prefs));
		} else {
			app.getStateManager().detach(has);
		}
	}

	private void exitApp() {
		app.stop();
	}

	private void setAvailable() {
		menuBar.setEnabled(!loading);
		EnvironmentSwitcherAppState env = app.getStateManager().getState(EnvironmentSwitcherAppState.class);
		timeOfDay.setEnabled(env.getEnvironment() == null);
		close.setEnabled(env.getEnvironment() != null);
	}

	private void toggleOptions() {
		final OptionsAppState state = stateManager.getState(OptionsAppState.class);
		if (state == null) {
			stateManager.attach(new OptionsAppState(prefs));
		} else {
			stateManager.detach(state);
		}
	}

	private File getTerrainFolder() {
		return new File(((IcesceneApp) app).getAssets().getExternalAssetsFolder(), SceneConstants.TERRAIN_PATH);
	}

	protected void openTerrainFolder() {
		final File terrainFolder = getTerrainFolder();
		try {
			XDesktop.getDesktop().open(terrainFolder);
		} catch (IOException ex) {
			LOG.log(Level.SEVERE, String.format("Failed to open terrain folder %s", terrainFolder), ex);
			error(String.format("Failed to open terrain folder %s", terrainFolder), ex);
		}
	}
}

package org.iceskies.environment;

import java.util.prefs.Preferences;

import org.icelib.UndoManager;
import org.icescene.environment.EnvironmentLight;

import com.jme3.scene.Node;

public class EditableEnvironmentSwitcherAppState extends EnvironmentSwitcherAppState {

	private boolean edit;
	private UndoManager undoManager;

	public EditableEnvironmentSwitcherAppState(UndoManager undoManager, Preferences appPrefs, String defaultEnvironment,
			EnvironmentLight el, Node gameNode, Node weatherNode) {
		super(appPrefs, defaultEnvironment, el, gameNode, weatherNode);
		this.undoManager = undoManager;
	}

	public boolean isEdit() {
		return edit;
	}

	public void setEdit(boolean edit) {
		this.edit = edit;
		EnvironmentAppState state = getState();
		// List<AbstractEnvironmentConfiguration> environments =
		// state.getEnvironments();
		if (state != null && edit && !(state instanceof EnvironmentEditorAppState)) {
			if (environments.size() > 0) {
				environments.put(EnvPriority.EDITING, environments.remove(environments.keySet().iterator().next()));
			}
			app.getStateManager().detach(state);
			EnvironmentEditorAppState editor = new EnvironmentEditorAppState(undoManager, prefs, el, gameNode);
			// editor.setEnvironments(environments);
			app.getStateManager().attach(editor);
			editor.setWeatherNode(weatherNode);
			checkEnvironment(true);
		} else if (state != null && !edit && (state instanceof EnvironmentEditorAppState)) {
			app.getStateManager().detach(state);
			environments.remove(EnvPriority.EDITING);
			EnvironmentAppState viewer = new EnvironmentAppState(prefs, el, gameNode);
			// viewer.setEnvironments(environments);
			app.getStateManager().attach(viewer);
			viewer.setWeatherNode(weatherNode);
			checkEnvironment(true);
		}
	}

	public void reload() {
		checkEnvironment(true);
	}

}

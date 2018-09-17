package org.iceskies.app;

import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.Preferences;

import org.icescene.environment.EnvironmentLight;
import org.iceskies.environment.EditableEnvironmentSwitcherAppState;
import org.iceskies.environment.EnvironmentAppState;
import org.iceskies.environment.EnvironmentManager;

import com.jme3.scene.Node;

import icetone.core.undo.UndoManager;

public class SkiesSwitcherAppState extends EditableEnvironmentSwitcherAppState {

	public SkiesSwitcherAppState(UndoManager undoManager, Preferences appPrefs, String defaultEnvironment, EnvironmentLight el, Node gameNode,
			Node weatherNode) {
		super(undoManager, appPrefs, defaultEnvironment, el, gameNode, weatherNode);
		addPrefKeyPattern(SkiesConfig.ENVIRONMENT_EDITOR + ".*");
		setFollowCamera(SkiesConfig.get().getBoolean(SkiesConfig.FOLLOW_CAMERA, SkiesConfig.FOLLOW_CAMERA_DEFAULT));
	}

	@Override
	protected void handlePrefUpdateSceneThread(PreferenceChangeEvent evt) {
		super.handlePrefUpdateSceneThread(evt);
		if (evt.getKey().equals(SkiesConfig.FOLLOW_CAMERA)) {
			EnvironmentAppState as = app.getStateManager().getState(EnvironmentAppState.class);
			if (as != null) {
				as.setFollowCamera(SkiesConfig.get().getBoolean(SkiesConfig.FOLLOW_CAMERA, SkiesConfig.FOLLOW_CAMERA_DEFAULT));
			}
		}
	}

}

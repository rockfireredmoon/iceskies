package org.iceskies.app;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.Preferences;

import org.icescene.environment.EnvironmentLight;
import org.icescene.environment.EnvironmentPhase;
import org.icescene.scene.AbstractSceneUIAppState;
import org.iceskies.environment.AbstractEnvironmentConfiguration;
import org.iceskies.environment.EnvironmentSwitcherAppState;
import org.iceskies.environment.EnvironmentSwitcherAppState.Listener;
import org.iceui.actions.AppAction;
import org.iceui.actions.AppAction.Style;

import com.jme3.font.BitmapFont.Align;
import com.jme3.math.Vector3f;

import icetone.controls.buttons.PushButton;
import icetone.core.StyledContainer;
import icetone.core.layout.FlowLayout;
import icetone.core.layout.mig.MigLayout;
import icetone.core.undo.UndoManager;
import icetone.extras.controls.Vector3fControl;

public class UIAppState extends AbstractSceneUIAppState implements PropertyChangeListener, Listener {

	private Vector3fControl lightDir;
	private EnvironmentLight light;
	private EnvironmentSwitcherAppState switcher;
	private StyledContainer phases;
	private AppAction followCameraAction;

	public UIAppState(UndoManager undoManager, Preferences prefs, EnvironmentLight light) {
		super(undoManager, prefs);
		addPrefKeyPattern(SkiesConfig.ENVIRONMENT_EDITOR + ".*");
		this.light = light;
	}

	@Override
	protected void postInitialize() {
		super.postInitialize();

		if (menuBar != null) {
			menuBar.addAction(followCameraAction = new AppAction("Follow Camera", evt -> {
				prefs.putBoolean(SkiesConfig.FOLLOW_CAMERA, evt.getSourceAction().isActive());
			}).setMenu("View").setStyle(Style.TOGGLE)
					.setActive(prefs.getBoolean(SkiesConfig.FOLLOW_CAMERA, SkiesConfig.FOLLOW_CAMERA_DEFAULT)));
		}

		switcher = app.getStateManager().getState(EnvironmentSwitcherAppState.class);
		switcher.addListener(this);

		light.addPropertyChangeListener(this);
		lightDir.setValue(light.getSunDirection());

		setAvailable();
	}

	@Override
	protected void onCleanup() {
		super.onCleanup();
		switcher.removeListener(this);
		light.removePropertyChangeListener(this);
	}

	protected void addAfter() {
		lightDir = new Vector3fControl(screen, -1, 1, 0.1f, new Vector3f(), false, false);
		lightDir.onChange(evt -> light.setSunDirection(evt.getNewValue()));
		layer.addElement(lightDir);

		phases = new StyledContainer(screen);
		phases.setLayoutManager(new FlowLayout(4, Align.Left));

		for (EnvironmentPhase p : EnvironmentPhase.phases()) {
			PushButton ba = new PushButton(screen) {
				{
					setStyleClass("fancy");
				}
			};
			ba.onMouseReleased(evt -> {
				EnvironmentSwitcherAppState esw = app.getStateManager().getState(EnvironmentSwitcherAppState.class);
				esw.setPhase(p);
			});
			ba.setButtonIcon(String.format("Interface/Styles/Gold/Icons/%s.png", p.name().toLowerCase()));
			ba.setToolTipText(String.format("Set the environment to the %s phase", p.name()));
			phases.addElement(ba);
		}
		layer.addElement(phases, "span 5, al left");
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals(EnvironmentLight.PROP_SUN_DIRECTION)) {
			lightDir.setValue(light.getSunDirection());
		}
	}

	@Override
	public void environmentChanged(String environment) {
		setAvailable();
	}

	@Override
	public void phaseChanged(EnvironmentPhase phase) {
		setAvailable();
	}

	@Override
	public void environmentConfigurationChanged(AbstractEnvironmentConfiguration topEnv) {
		setAvailable();
	}

	protected void setAvailable() {
		phases.setEnabled(switcher.getEnvironment() != null);
	}

	@Override
	protected MigLayout createLayout() {
		return new MigLayout(screen, "fill, wrap 2", "[][]", "push[]");
	}

	@Override
	protected void handlePrefUpdateSceneThread(PreferenceChangeEvent evt) {
		super.handlePrefUpdateSceneThread(evt);
		if (evt.getKey().equals(SkiesConfig.FOLLOW_CAMERA)) {
			followCameraAction
					.setActive(prefs.getBoolean(SkiesConfig.FOLLOW_CAMERA, SkiesConfig.FOLLOW_CAMERA_DEFAULT));
		}
	}
}

package org.iceskies.app;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.Preferences;

import org.icelib.UndoManager;
import org.icescene.environment.EnvironmentLight;
import org.icescene.environment.EnvironmentPhase;
import org.icescene.scene.AbstractSceneUIAppState;
import org.iceskies.environment.AbstractEnvironmentConfiguration;
import org.iceskies.environment.EnvironmentSwitcherAppState;
import org.iceskies.environment.EnvironmentSwitcherAppState.Listener;
import org.iceui.controls.FancyButton;
import org.iceui.controls.Vector3fControl;

import com.jme3.font.BitmapFont.Align;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.math.Vector3f;

import icetone.controls.buttons.CheckBox;
import icetone.core.Container;
import icetone.core.layout.FlowLayout;
import icetone.core.layout.mig.MigLayout;

public class UIAppState extends AbstractSceneUIAppState implements PropertyChangeListener, Listener {

	private CheckBox followCamera;
	private Vector3fControl lightDir;
	private EnvironmentLight light;
	private EnvironmentSwitcherAppState switcher;
	private Container phases;

	public UIAppState(UndoManager undoManager, Preferences prefs, EnvironmentLight light) {
		super(undoManager, prefs);
		addPrefKeyPattern(SkiesConfig.ENVIRONMENT_EDITOR + ".*");
		this.light = light;
	}

	@Override
	protected void postInitialize() {
		super.postInitialize();

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

	@Override
	protected void addBefore() {
		followCamera = new CheckBox(screen) {
			@Override
			public void onButtonMouseLeftUp(MouseButtonEvent evt, boolean toggled) {
				prefs.putBoolean(SkiesConfig.FOLLOW_CAMERA, toggled);
			}
		};
		followCamera.setIsCheckedNoCallback(prefs.getBoolean(SkiesConfig.FOLLOW_CAMERA, SkiesConfig.FOLLOW_CAMERA_DEFAULT));
		followCamera.setLabelText("Follow Camera");
		followCamera.setToolTipText("Have the environment dome follow the camera (as in production use)");
		layer.addChild(followCamera, "span 2");

	}

	protected void addAfter() {
		lightDir = new Vector3fControl(screen, -1, 1, 0.1f, new Vector3f(), false, false) {

			@Override
			protected void onChangeVector(Vector3f newValue) {
				light.setSunDirection(newValue);
			}
		};
		layer.addChild(lightDir);

		phases = new Container(screen);
		phases.setLayoutManager(new FlowLayout(4, Align.Left));

		for (EnvironmentPhase p : EnvironmentPhase.phases()) {
			FancyButton ba = new FancyButton(screen) {
				@Override
				public void onButtonMouseLeftUp(MouseButtonEvent evt, boolean toggled) {
					EnvironmentSwitcherAppState esw = app.getStateManager().getState(EnvironmentSwitcherAppState.class);
					esw.setPhase(p);
				}
			};
			ba.setButtonIcon(String.format("Interface/Styles/Gold/Icons/%s.png", p.name().toLowerCase()));
			ba.setToolTipText(String.format("Set the environment to the %s phase", p.name()));
			phases.addChild(ba);
		}
		layer.addChild(phases, "span 5, al left");
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
		phases.setIsEnabled(switcher.getEnvironment() != null);
	}

	@Override
	protected MigLayout createLayout() {
		return new MigLayout(screen, "fill, wrap 6", "[][]push[][][][]", "[]push[]");
	}

	@Override
	protected void handlePrefUpdateSceneThread(PreferenceChangeEvent evt) {
		super.handlePrefUpdateSceneThread(evt);
		if (evt.getKey().equals(SkiesConfig.FOLLOW_CAMERA)) {
			followCamera.setIsCheckedNoCallback(prefs.getBoolean(SkiesConfig.FOLLOW_CAMERA, SkiesConfig.FOLLOW_CAMERA_DEFAULT));
		}
	}
}

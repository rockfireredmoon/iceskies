package org.iceskies.environment;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import org.icescene.IcemoonAppState;
import org.icescene.environment.EnvironmentLight;
import org.icescene.environment.EnvironmentPhase;

import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

public class EnvironmentSwitcherAppState extends IcemoonAppState<IcemoonAppState<IcemoonAppState<?>>> {
	final static Logger LOG = Logger.getLogger(EnvironmentSwitcherAppState.class.getName());

	public enum EnvPriority {
		EDITING, USER, VIEWING, SPHERE, TILE, SERVER, DEFAULT_FOR_TERRAIN, GLOBAL
	}

	public interface Listener {
		void phaseChanged(EnvironmentPhase phase);

		void environmentChanged(String environment);

		void environmentConfigurationChanged(AbstractEnvironmentConfiguration topEnv);

	}

	private List<Listener> listeners = new ArrayList<>();
	protected Map<EnvPriority, EnvironmentSelection> environments = new TreeMap<>();
	protected EnvironmentLight el;
	protected Node gameNode;
	protected Node weatherNode;
	private boolean followCamera = true;
	private String environment;
	private String environmentConfiguration;
	private boolean audioEnabled = true;
	private Vector3f targetDirection;
	private Vector3f origDirection;
	private float blendTime;
	private float blendProgress;
	private List<AbstractEnvironmentConfiguration> configs = null;

	public EnvironmentSwitcherAppState(Preferences appPrefs, String defaultEnvironment, EnvironmentLight el, Node gameNode,
			Node weatherNode) {
		super(appPrefs);
		this.el = el;
		this.weatherNode = weatherNode;
		this.gameNode = gameNode;
		if (defaultEnvironment != null) {
			environments.put(EnvPriority.GLOBAL, new EnvironmentSelection(EnvironmentPhase.DAY, defaultEnvironment));
		}
	}

	public boolean isAudioEnabled() {
		return audioEnabled;
	}

	public void setAudioEnabled(boolean audioEnabled) {
		this.audioEnabled = audioEnabled;
		EnvironmentAppState eas = getState();
		if (eas != null) {
			eas.setAudioEnabled(audioEnabled);
		}
	}

	public boolean isFollowCamera() {
		return followCamera;
	}

	public void setFollowCamera(boolean followCamera) {
		this.followCamera = followCamera;
	}

	public EnvironmentPhase getPhase() {
		Map.Entry<EnvPriority, EnvironmentSelection> top = environments.isEmpty() ? null
				: environments.entrySet().iterator().next();
		return top == null ? null : top.getValue().getPhase();
	}

	/**
	 * Set the phase of the highest priority environment (i.e. that which is
	 * currently active).
	 * 
	 * @param phase
	 *            phase to set
	 */
	public void setPhase(EnvironmentPhase phase) {
		EnvironmentPhase currentPhase = getPhase();
		if (!Objects.equals(phase, currentPhase)) {
			if (!environments.isEmpty())
				environments.entrySet().iterator().next().getValue().setPhase(phase);

			checkEnvironment(false);
			for (Listener l : listeners) {
				l.phaseChanged(phase);
			}
		}
	}

	public void addListener(Listener l) {
		listeners.add(l);
	}

	public void removeListener(Listener l) {
		listeners.remove(l);
	}

	@Override
	protected void postInitialize() {
		checkEnvironment(false);
	}

	public void setEnvironment(EnvPriority priority, String environment) {
		setEnvironment(priority, environment, getPhase());
	}

	public void setEnvironment(EnvPriority priority, String environment, EnvironmentPhase phase) {
		if (environment == null) {
			environments.remove(priority);
		} else {
			environments.put(priority, new EnvironmentSelection(phase, environment));
		}
		checkEnvironment(false);
	}

	/**
	 * Get the key for <strong>Environment</strong>, this will be the grouping
	 * of environment configurations. If the environment was set from a key that
	 * contained an <strong>Environment Configuration</strong> instead, this
	 * will be null.
	 * 
	 * @return
	 */
	public String getEnvironment() {
		return environment;
	}

	/**
	 * Get the key for <strong>Environment Configuration</strong>, this will be
	 * the actual environment settings. If the environment was set from a key
	 * that contained just an an <strong>Environment</strong> instead (i.e. the
	 * group of configurations), this will be null.
	 * 
	 * @return
	 */
	public String getEnvironmentConfiguration() {
		return environmentConfiguration;
	}

	protected void checkEnvironment(boolean force) {
		if (app == null) {
			return;
		}
		EnvironmentAppState eas = getState();
		environmentConfiguration = null;

		String oldEnvironment = environment;
		EnvironmentPhase oldPhase = getPhase();

		environment = null;
		targetDirection = null;
		origDirection = null;
		blendProgress = 0;
		List<AbstractEnvironmentConfiguration> configs = new ArrayList<>();

		LOG.info(String.format("Checking %d environments", environments.size()));
		if (environments.size() > 0) {
			for (Map.Entry<EnvPriority, EnvironmentSelection> en : environments.entrySet()) {
				LOG.info(String.format("Priority %s is %s", en.getKey(), en.getValue()));
			}
			if (eas == null) {
				eas = startEnvironment();
			}
			Iterator<Entry<EnvPriority, EnvironmentSelection>> iterator = environments.entrySet().iterator();
			while (iterator.hasNext()) {
				Entry<EnvPriority, EnvironmentSelection> next = iterator.next();
				String env = next.getValue().getEnvironment();
				EnvironmentPhase phase = next.getValue().getPhase();
				if (EnvironmentManager.get(assetManager).isEnvironment(env)) {

					if (environment == null && environmentConfiguration == null) {
						// The first one becomes the currently set environment
						environment = env;
					}

					if (phase == null) {
						phase = EnvironmentPhase.DAY;
					}

					EnvironmentGroupConfiguration envConfigs = EnvironmentManager.get(assetManager).getEnvironments(env);
					if (envConfigs.getPhases().containsKey(phase)) {
						env = envConfigs.getPhases().get(phase);
					} else {
						throw new RuntimeException(String.format("Missing environment %s for phase %s", env, phase));
					}
					targetDirection = phase.getLightDirection();
					origDirection = el.getSunDirection();

				} else {
					if (environmentConfiguration == null && environment == null) {
						environmentConfiguration = env;
					}
				}
				AbstractEnvironmentConfiguration e = EnvironmentManager.get(assetManager).getEnvironmentConfiguration(env);
				if (e == null) {
					throw new RuntimeException(String.format("No environment %s for phase %s", env, phase));
				}
				configs.add(e);
			}

			if (force || !Objects.equals(configs, this.configs)) {
				AbstractEnvironmentConfiguration topEnv = configs.get(0);
				String env = topEnv.getKey();
				blendTime = topEnv.getBlendTime();
				LOG.info(String.format("Switching to highest priority environment %s (blend %f)", env, blendTime));
				try {
					eas.setEnvironment(topEnv);
					for (Listener l : listeners) {
						l.environmentConfigurationChanged(topEnv);
					}
				} catch (Exception e) {
					LOG.log(Level.SEVERE, String.format("Failed to load environment %s", env), e);
					error(String.format("Failed to load environment %s", env), e);
				}
			}
		} else {
			if (eas != null) {
				LOG.info("No environments");
				app.getStateManager().detach(eas);
				for (Listener l : listeners) {
					l.environmentConfigurationChanged(null);
				}
			}
		}
		this.configs = configs;

		if (!Objects.equals(environment, oldEnvironment)) {
			for (Listener l : listeners) {
				l.environmentChanged(environment);
			}
		}

		EnvironmentPhase newPhase = getPhase();
		if (!Objects.equals(getPhase(), oldPhase)) {
			for (Listener l : listeners) {
				l.phaseChanged(newPhase);
			}
		}
	}

	@Override
	public void update(float tpf) {
		super.update(tpf);
		if (targetDirection != null) {
			blendProgress += tpf;
			if (blendProgress >= blendTime) {
				el.setSunDirection(targetDirection);
				targetDirection = null;
				origDirection = null;
				blendProgress = 0;
			} else {
				float amt = blendProgress / blendTime;
				el.setSunDirection(origDirection.clone().interpolate(targetDirection, amt));
			}
		}
	}

	protected EnvironmentAppState getState() {
		return app == null || app.getStateManager() == null ? null : app.getStateManager().getState(EnvironmentAppState.class);
	}

	protected EnvironmentAppState startEnvironment() {
		// Environment
		EnvironmentAppState eas = new EnvironmentAppState(prefs, el, gameNode);
		eas.setAudioEnabled(audioEnabled);
		eas.setWeatherNode(weatherNode);
		eas.setFollowCamera(followCamera);
		stateManager.attach(eas);
		return eas;
	}

	@Override
	protected void onCleanup() {
		super.onCleanup();
		environments.clear();
		checkEnvironment(false);
	}

	public static class EnvironmentSelection {
		private EnvironmentPhase phase;
		private String environment;

		public EnvironmentSelection(EnvironmentPhase phase, String environment) {
			this.phase = phase;
			this.environment = environment;
		}

		public void setPhase(EnvironmentPhase phase) {
			this.phase = phase;
		}

		public EnvironmentPhase getPhase() {
			return phase;
		}

		public String getEnvironment() {
			return environment;
		}

		@Override
		public String toString() {
			return "EnvironmentSelection [phase=" + phase + ", environment=" + environment + "]";
		}
	}

}

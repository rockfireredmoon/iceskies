package org.iceskies.environment;

import java.util.logging.Logger;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.Preferences;

import org.icescene.IcemoonAppState;
import org.icescene.SceneConfig;
import org.icescene.audio.AudioAppState;
import org.icescene.environment.EnvironmentLight;
import org.iceskies.environment.enhanced.EnhancedEnvironment;
import org.iceskies.environment.enhanced.EnhancedEnvironmentConfiguration;
import org.iceskies.environment.legacy.LegacyEnvironment;
import org.iceskies.environment.legacy.LegacyEnvironmentConfiguration;

import com.jme3.asset.AssetManager;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Node;

import jme3utilities.sky.SkyControl;

public class EnvironmentAppState extends IcemoonAppState<IcemoonAppState<?>> {

	private final static Logger LOG = Logger.getLogger(EnvironmentAppState.class.getName());
	protected AbstractEnvironmentConfiguration environmentConfiguration;
	protected SkyControl skyDome;
	Node gameNode;
	private boolean editMode;
	private ColorRGBA vpColor;
	protected float cycleDuration = Float.MIN_VALUE;
	protected boolean setNow;
	protected AudioAppState audio;
	// private NetworkListenerAdapter networkListener;
	// private NetworkAppState network;
	protected final EnvironmentLight environmentLight;
	// private String environmentType;
	private Node weatherNode;
	private boolean followCamera = true;
	private boolean audioEnabled = true;

	private EnvironmentImpl skyImpl;

	protected EnvironmentAppState(Preferences prefs, EnvironmentLight environmentLight, Node gameNode) {
		super(prefs);
		this.environmentLight = environmentLight;
		this.gameNode = gameNode;
		addPrefKeyPattern(SceneConfig.AUDIO + ".*");
		addPrefKeyPattern(SceneConfig.SCENE_LIGHT_BEAMS);
	}

	public boolean isAudioEnabled() {
		return audioEnabled;
	}

	public void setAudioEnabled(boolean audioEnabled) {
		this.audioEnabled = audioEnabled;
		if (skyImpl != null) {
			skyImpl.setAudioEnabled(audioEnabled);
		}
	}

	public boolean isFollowCamera() {
		return followCamera;
	}

	public void setFollowCamera(boolean followCamera) {
		this.followCamera = followCamera;
		if (skyImpl != null) {
			skyImpl.setFollowCamera(followCamera);
		}
	}

	public Node getWeatherNode() {
		return weatherNode;
	}

	public void setWeatherNode(Node weatherNode) {
		this.weatherNode = weatherNode;
	}

	public AbstractEnvironmentConfiguration getEnvironment() {
		return environmentConfiguration;
	}

	public void setEnvironment(AbstractEnvironmentConfiguration environmentConfiguration) {
		this.environmentConfiguration = environmentConfiguration;

		// Only actually set up the environment if the appstate has been
		// initialized.
		// It's possible this will get called before then
		if (stateManager != null) {
			createAll();
		}
	}

	public AssetManager getAssetManager() {
		return assetManager;
	}

	@Override
	public void update(float tpf) {
		if (skyImpl != null) // Temp
			skyImpl.update(tpf);

	}

	public void createAll() {
		if (environmentConfiguration == null) {
			LOG.info("No environment");
		} else {
			LOG.info("Intialising environment");
			if (environmentConfiguration instanceof EnhancedEnvironmentConfiguration) {
				if (skyImpl == null || !(skyImpl instanceof EnhancedEnvironment)) {
					skyImpl = new EnhancedEnvironment(app, audio, camera, stateManager, assetManager, environmentLight,
							gameNode);
				}
			} else if (environmentConfiguration instanceof LegacyEnvironmentConfiguration) {
				if (skyImpl == null || !(skyImpl instanceof LegacyEnvironment)) {
					skyImpl = new LegacyEnvironment(app, audio, camera, stateManager, assetManager, environmentLight,
							gameNode);
				}
			} else if (environmentConfiguration == null && skyImpl != null) {
				//
			} else {
				throw new UnsupportedOperationException();
			}
			skyImpl.setFollowCamera(followCamera);
			skyImpl.setAudioEnabled(audioEnabled);
			skyImpl.setEnvironment(environmentConfiguration);
		}
	}

	//
	// Environment editing
	//

	public boolean isEditMode() {
		return editMode;
	}

	@Override
	protected void postInitialize() {
		setNow = true;

		// Initial environment
		vpColor = app.getViewPort().getBackgroundColor();
		// try {
		// if (environmentType != null) {
		// environmentConfiguration =
		// EnvironmentConfiguration.loadByName(assetManager, environmentType,
		// null);
		// }
		// } catch (AssetNotFoundException anfe) {
		// LOG.log(Level.SEVERE,
		// String.format("Failed to load requested environment %s. Falling back
		// to DefaultDay",
		// environmentType), anfe);
		// }
		// if (environmentConfiguration == null) {
		// environmentConfiguration =
		// EnvironmentConfiguration.loadByName(assetManager, "Default",
		// EnvironmentPhase.DAY);
		// }
		// cycleDuration = network.getTimeRemainingInCycle();
		// LOG.info(String.format("Starting with environment %s (%s), with %4.2f
		// seconds remaining before the next environment change.",
		// environmentConfiguration.getName(),
		// environmentConfiguration.getPhase(), cycleDuration));

		// createAll();

		// Listeners
		// TODO
		// network.addListener(networkListener = new NetworkListenerAdapter() {
		// @Override
		// public void environmentChange(float duration, final String name,
		// final EnvironmentConfiguration.Type type) {
		// cycleDuration = duration;
		// app.enqueue(new Callable<Void>() {
		// public Void call() throws Exception {
		// doEnvironmentChange(name, type);
		// return null;
		// }
		// });
		// }
		// });

		// If there is already an environment set, activate it now
		if (environmentConfiguration != null) {
			createAll();
		}
	}

	// protected void doEnvironmentChange(String name, EnvironmentPhase type) {
	// String n = name;
	// if (n == null) {
	// n = environmentConfiguration.getKey().getName();
	// }
	// oldEnvironment = environmentConfiguration;
	// environmentConfiguration =
	// EnvironmentManager.get(app.getAssetManager()).getSky(new
	// EnvironmentKey(name, type));
	// createAll();
	// }

	@Override
	protected void onStateAttached() {
		audio = stateManager.getState(AudioAppState.class);
		if (audio == null) {
			throw new IllegalStateException(AudioAppState.class + " needs to be attached.");
		}
	}

	protected void onStateDetached() {
		// Clean up the actual sky implementation
		if (skyImpl != null) {
			skyImpl.onDetached();
		}
	}

	@Override
	protected void onCleanup() {
		// network.removeListener(networkListener);

		// Clean up the actual sky implementation
		if (skyImpl != null) {
			skyImpl.onCleanup();
			skyImpl = null;
		}

		// Turn off the lights
		environmentLight.setDirectionalEnabled(false);
		environmentLight.setAmbientEnabled(false);

		// We now have no skydome, return the viewport to its original colour
		app.getViewPort().setBackgroundColor(vpColor);

	}

	@Override
	protected void handlePrefUpdateSceneThread(PreferenceChangeEvent evt) {
		skyImpl.handlePrefUpdateSceneThread(evt);
	}

	public void emote(long id, String sender, String emote) {
	}
}

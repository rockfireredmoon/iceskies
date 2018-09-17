package org.iceskies.environment.enhanced;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.concurrent.Callable;
import java.util.logging.Logger;
import java.util.prefs.PreferenceChangeEvent;

import org.icescene.IcesceneApp;
import org.icescene.audio.AudioAppState;
import org.icescene.audio.AudioQueue;
import org.icescene.audio.AudioQueueHandler;
import org.icescene.audio.QueuedAudio;
import org.icescene.environment.EnvironmentLight;
import org.icescene.environment.PostProcessAppState;
import org.icescene.environment.PostProcessAppState.FogFilterMode;
import org.iceskies.environment.AbstractEnvironmentConfiguration;
import org.iceskies.environment.AbstractEnvironmentImpl;
import org.iceskies.environment.EnvironmentAppState;
import org.iceskies.environment.PlaylistType;

import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.post.filters.FogFilter;
import com.jme3.renderer.Camera;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

import icetone.extras.util.ExtrasUtil;

public class EnhancedEnvironment extends AbstractEnvironmentImpl<EnhancedEnvironmentConfiguration> implements PropertyChangeListener {

	private final static Logger LOG = Logger.getLogger(EnhancedEnvironment.class.getName());
	private final static AudioQueue[] AUDIO_QUEUES = { AudioQueue.AMBIENT, AudioQueue.MUSIC, AudioQueue.WEATHER };
	private Node sky;
	private AbstractWeather weather;
	private String lastSkyMap;
	private String lastCelestialBodyMap;
	private String lastCloudsMap;
	private String lastFogAlphaMap;
	private Node weatherNode;
	protected EnhancedEnvironmentConfiguration environmentConfiguration;
	protected SkyDomeControl skyDome;
	protected float cycleDuration = Float.MIN_VALUE;
	protected boolean setNow;
	protected Sun sun;

	public EnhancedEnvironment(IcesceneApp app, AudioAppState audio, Camera camera, AppStateManager stateManager,
			AssetManager assetManager, EnvironmentLight environmentLight, Node gameNode) {
		super(app, audio, camera, stateManager, assetManager, environmentLight, gameNode);
	}

	public Node getWeatherNode() {
		return weatherNode;
	}

	public void setWeatherNode(Node weatherNode) {
		this.weatherNode = weatherNode;
	}

	public SkyDomeControl getSkyDome() {
		return skyDome;
	}

	public EnhancedEnvironmentConfiguration getEnvironment() {
		return environmentConfiguration;
	}

	// public void setEnvironment(String requiredEnvironment) {
	// setEnvironment(EnhancedEnvironmentConfiguration.loadByName(assetManager,
	// network.getClient().getZone().getEnvironmentType(), null));
	// }
	public void setEnvironment(EnhancedEnvironmentConfiguration environmentConfiguration) {

		if (this.environmentConfiguration != null) {
			this.environmentConfiguration.removePropertyChangeListener(this);
		}
		this.environmentConfiguration = environmentConfiguration;
		environmentConfiguration.addPropertyChangeListener(this);
		createAll();
	}

	@Override
	public void update(float tpf) {
		keepSunAtDistanceFromCamera(tpf);
	}

	public void createAll() {
		LOG.info(("Intialising environment " + environmentConfiguration.getKey()));
		createFog();
		createSky();
		createWeather();
		createSound();
		checkSun();
	}

	//
	// Environment editing
	//

	public void propertyChange(PropertyChangeEvent evt) {
		LOG.info("PCE : " + evt);
		if (evt.getPropertyName().equals(EnhancedEnvironmentConfiguration.PROP_CLOUDS)) {
			if (skyDome != null) {
				LOG.info(String.format("Setting clouds to %s", environmentConfiguration.isClouds()));
				skyDome.setClouds(environmentConfiguration.isClouds());
			}
		} else if (evt.getPropertyName().equals(EnhancedEnvironmentConfiguration.PROP_SKY_DOME)) {
			createSky();
		} else if (evt.getPropertyName().equals(EnvironmentLight.PROP_SUN_ENABLED)) {
			checkSun();
		} else if (evt.getPropertyName().equals(EnvironmentLight.PROP_SUN_POSITION) && sun != null) {
			sun.setLocalTranslation((Vector3f) evt.getNewValue());
		} else if (evt.getPropertyName().equals(EnvironmentLight.PROP_SUN_COLOR) && sun != null) {
			sun.setSunColor(((ColorRGBA) evt.getNewValue()));
		} else if (evt.getPropertyName().equals(EnhancedEnvironmentConfiguration.PROP_CELESTIAL_BODY_ALPHA)) {
			LOG.info(String.format("New celestial body alpha is %4.2f",
					environmentConfiguration.getCelestialBodyAlpha()));
			skyDome.setCelestialBodyAlpha((Float) environmentConfiguration.getCelestialBodyAlpha());
			skyDome.completeTransition();
		} else if (evt.getPropertyName()
				.equalsIgnoreCase(EnhancedEnvironmentConfiguration.PROP_CELESTIAL_BODY_DIRECTION)) {
			LOG.info(String.format("Celestial Body direction now %3.3f",
					environmentConfiguration.getCelestialBodyDirection()));
			skyDome.setCelestialBodyDirection(environmentConfiguration.getCelestialBodyDirection());
		} else if (evt.getPropertyName().equalsIgnoreCase(EnhancedEnvironmentConfiguration.PROP_CELESTIAL_BODY_SPEED)) {
			LOG.info(String.format("Celestial Body speed now %3.3f", environmentConfiguration.getCelestialBodySpeed()));
			skyDome.setCelestialBodySpeed(environmentConfiguration.getCelestialBodySpeed());
		} else if (evt.getPropertyName().equalsIgnoreCase(EnhancedEnvironmentConfiguration.PROP_CELESTIAL_BODY_SCALE)) {
			LOG.info(String.format("Celestial Body scale now %3.3f", environmentConfiguration.getCelestialBodyScale()));
			skyDome.setCelestialBodyScale(environmentConfiguration.getCelestialBodyScale());
		} else if (evt.getPropertyName().equals(EnhancedEnvironmentConfiguration.PROP_CONTROL_FOG)
				|| evt.getPropertyName().equals(EnhancedEnvironmentConfiguration.PROP_FOG)) {
			createFog();
			createSky();
		} else if (evt.getPropertyName().equals(EnhancedEnvironmentConfiguration.PROP_FOG_DISTANCE)
				|| evt.getPropertyName().equals(EnhancedEnvironmentConfiguration.PROP_FOG)
				|| evt.getPropertyName().equals(EnhancedEnvironmentConfiguration.PROP_FOG_DENSITY)) {
			LOG.info(String.format("Fog distance %1.3f", environmentConfiguration.getFogDistance()));
			LOG.info(String.format("Fog density %1.3f", environmentConfiguration.getFogDensity()));
			createFog();
		} else if (evt.getPropertyName().equals(EnhancedEnvironmentConfiguration.PROP_SKY_MAP_USE_AS_MAP)) {
			skyDome.setSkyMapUseAsMap(environmentConfiguration.isSkyMapUseAsMap());
		} else if (evt.getPropertyName().equals(EnhancedEnvironmentConfiguration.PROP_CLOUDS_MAP_USE_AS_MAP)) {
			skyDome.setCloudsMapUseAsMap(environmentConfiguration.isCloudsMapUseAsMap());
		} else if (evt.getPropertyName().equals(EnhancedEnvironmentConfiguration.PROP_SKY_MAP_SCALE)) {
			skyDome.setSkyMapScale(environmentConfiguration.getSkyMapScale());
		} else if (evt.getPropertyName().equals(EnhancedEnvironmentConfiguration.PROP_CLOUDS_MAP_SCALE)) {
			skyDome.setCloudsMapScale(environmentConfiguration.getCloudsMapScale());
		} else if (evt.getPropertyName().equals(EnhancedEnvironmentConfiguration.PROP_FOG_COLOR)) {
			skyDome.setFogColor(environmentConfiguration.getFogColor());
			skyDome.completeTransition();
		} else if (evt.getPropertyName().equals(EnhancedEnvironmentConfiguration.PROP_SKY_COLOR)) {
			LOG.info(String.format("New sky color is %s", environmentConfiguration.getSkyColor()));
			skyDome.setSkyColor(environmentConfiguration.getSkyColor());
			skyDome.completeTransition();
		} else if (evt.getPropertyName().equals(EnhancedEnvironmentConfiguration.PROP_CLOUD_MAX_OPACITY)) {
			LOG.info(String.format("Max cloud max opacity now %1.3f", environmentConfiguration.getCloudMaxOpacity()));
			skyDome.setCloudMaxOpacity(environmentConfiguration.getCloudMaxOpacity());
			skyDome.setClouds(environmentConfiguration.getCloudMaxOpacity() > 0);
		} else if (evt.getPropertyName().equals(EnhancedEnvironmentConfiguration.PROP_CLOUD_MIN_OPACITY)) {
			LOG.info(String.format("Max cloud min opacity now %1.3f", environmentConfiguration.getCloudMinOpacity()));
			skyDome.setCloudMinOpacity(environmentConfiguration.getCloudMinOpacity());
			skyDome.setClouds(environmentConfiguration.getCloudMaxOpacity() > 0);
		} else if (evt.getPropertyName().equals(EnhancedEnvironmentConfiguration.PROP_SKY_MAP_ALPHA)) {
			LOG.info(String.format("New sky map alpha is %4.2f", environmentConfiguration.getSkyMapAlpha()));
			skyDome.setSkyMapAlpha(environmentConfiguration.getSkyMapAlpha());
			skyDome.completeTransition();
		} else if (evt.getPropertyName().equals(EnhancedEnvironmentConfiguration.PROP_TRANSITION_SPEED)) {
			LOG.info(String.format("Transition speed now %4.3f", environmentConfiguration.getTransitionSpeed()));
			skyDome.setTransitionSpeed(environmentConfiguration.getTransitionSpeed());
		} else if (evt.getPropertyName().equals(EnhancedEnvironmentConfiguration.PROP_CLOUD_CYCLE_SPEED)) {
			LOG.info(String.format("Cloud cycle speed now %2.3f", environmentConfiguration.getCloudCycleSpeed()));
			skyDome.setCloudCycleSpeed(environmentConfiguration.getCloudCycleSpeed());
		} else if (evt.getPropertyName().equals(EnhancedEnvironmentConfiguration.PROP_CLOUDS_FAR_SPEED)) {
			LOG.info(String.format("Cloud far speed now %2.3f", environmentConfiguration.getCloudsFarSpeed()));
			skyDome.setCloudsFarSpeed(environmentConfiguration.getCloudsFarSpeed());
		} else if (evt.getPropertyName().equals(EnhancedEnvironmentConfiguration.PROP_CLOUDS_NEAR_SPEED)) {
			LOG.info(String.format("Cloud near speed now %2.3f", environmentConfiguration.getCloudsNearSpeed()));
			skyDome.setCloudsNearSpeed(environmentConfiguration.getCloudsNearSpeed());
		} else if (evt.getPropertyName().equals(EnhancedEnvironmentConfiguration.PROP_CLOUDS_FAR_ROTATION)) {
			LOG.info(String.format("Clouds far rotate now %3.0f", environmentConfiguration.getCloudsFarRotation()));
			skyDome.setCloudsFarRotation(environmentConfiguration.getCloudsFarRotation());
		} else if (evt.getPropertyName().equals(EnhancedEnvironmentConfiguration.PROP_CLOUDS_NEAR_ROTATION)) {
			LOG.info(String.format("Clouds near rotate now %3.0f", environmentConfiguration.getCloudsNearRotation()));
			skyDome.setCloudsNearRotation(environmentConfiguration.getCloudsNearRotation());
		} else if (evt.getPropertyName().equals(EnhancedEnvironmentConfiguration.PROP_FOG_COLOR)) {
			LOG.info(String.format("Fog color is now %s", environmentConfiguration.getFogColor()));
			if (environmentConfiguration.isSkyDome() && environmentConfiguration.isControlFog()) {
				skyDome.setFogColor(environmentConfiguration.getFogColor());
			} else {
				PostProcessAppState post = stateManager.getState(PostProcessAppState.class);
				if (post != null) {
					post.setFogFilterMode(FogFilterMode.JME3);
					((FogFilter) post.getFogFilter()).setFogColor(environmentConfiguration.getFogColor());
				}
			}
		} else if (evt.getPropertyName().equals(EnhancedEnvironmentConfiguration.PROP_AMBIENT_ENABLED)) {
			if (environmentConfiguration.isSkyDome()) {
				skyDome.setAmbientEnabled(environmentConfiguration.isAmbientEnabled());
			} else {
				createSky();
			}
		} else if (evt.getPropertyName().equals(EnhancedEnvironmentConfiguration.PROP_AMBIENT_COLOR)) {
			if (environmentConfiguration.isSkyDome()) {
				skyDome.setAmbientLight(environmentConfiguration.getAmbientColor());
			} else {
				environmentLight.setAmbientColor(environmentConfiguration.getAmbientColor());
			}
		} else if (evt.getPropertyName().equals(EnhancedEnvironmentConfiguration.PROP_DIRECTIONAL_ENABLED)) {
			if (environmentConfiguration.isSkyDome()) {
				skyDome.setDirectionalEnabled(environmentConfiguration.isDirectionalEnabled());
			} else {
				createSky();
			}
		} else if (evt.getPropertyName().equals(EnhancedEnvironmentConfiguration.PROP_DIRECTIONAL_COLOR)) {
			LOG.info(String.format("Altered color is %s", environmentConfiguration.getDirectionalColor()));
			if (environmentConfiguration.isSkyDome()) {
				skyDome.setDirectionalLight(environmentConfiguration.getDirectionalColor());
			} else {
				environmentLight.setDirectionalColor(environmentConfiguration.getDirectionalColor());
			}
		} else if (evt.getPropertyName().equals(EnhancedEnvironmentConfiguration.PROP_START_DIRECTIONAL_ANGLE)
				|| evt.getPropertyName().equals(EnhancedEnvironmentConfiguration.PROP_END_DIRECTIONAL_ANGLE)) {
			LOG.info(String.format("Start sun angle %1.3f", environmentConfiguration.getStartDirectionalAngle()));
			LOG.info(String.format("Endsun angle %1.3f", environmentConfiguration.getEndDirectionalAngle()));
			createSky();
		} else if (evt.getPropertyName().equals(EnhancedEnvironmentConfiguration.PROP_CONTROL_DIRECTIONAL_POSITION)) {
			createSky();
		} else if (evt.getPropertyName().startsWith(AbstractEnvironmentConfiguration.PROP_PLAYLIST)) {
			int idx = evt.getPropertyName().lastIndexOf("-");
			PlaylistType t = PlaylistType.valueOf(evt.getPropertyName().substring(idx + 1));
			reloadPlaylist(environmentConfiguration, t);
		} else if (evt.getPropertyName().equals(EnhancedEnvironmentConfiguration.PROP_WEATHER)) {
			createWeather();
		} else if (evt.getPropertyName().equals(EnhancedEnvironmentConfiguration.PROP_WEATHER_INTENSITY)) {
			LOG.info(String.format("WeatherIntensity %1.3f", environmentConfiguration.getWeatherIntensity()));
			createWeather();
		} else if (evt.getPropertyName().equals(EnhancedEnvironmentConfiguration.PROP_WEATHER_DENSITY)) {
			LOG.info(String.format("WeatherSpeed %1.3f", environmentConfiguration.getWeatherDensity()));
			createWeather();
		} else if (evt.getPropertyName().equals(EnhancedEnvironmentConfiguration.PROP_WIND)) {
			createWeather();
		} else if (evt.getPropertyName().equals(EnhancedEnvironmentConfiguration.PROP_WEATHER_MUSIC_GAIN)) {
			LOG.info(String.format("New weather music gain is %1.3f", environmentConfiguration.getWeatherMusicGain()));
			updatePlaylist(environmentConfiguration, PlaylistType.AMBIENT_NOISE);
		} else if (evt.getPropertyName().equals(EnhancedEnvironmentConfiguration.PROP_FOG_ALPHA_MAP)) {
			LOG.info(String.format("New fog alpha map is %s", environmentConfiguration.getFogAlphaMap()));
			createSky();
		} else if (evt.getPropertyName().equals(EnhancedEnvironmentConfiguration.PROP_SKY_MAP)) {
			LOG.info(String.format("New sky map is %s", environmentConfiguration.getSkyMap()));
			createSky();
		} else if (evt.getPropertyName().equals(EnhancedEnvironmentConfiguration.PROP_SKY_MAP_WRAP)) {
			skyDome.setSkyMapWrap(environmentConfiguration.getSkyMapWrap());
		} else if (evt.getPropertyName().equals(EnhancedEnvironmentConfiguration.PROP_CELESTIAL_BODY_WRAP)) {
			skyDome.setCelestialBodyWrap(environmentConfiguration.getSkyMapWrap());
		} else if (evt.getPropertyName().equals(EnhancedEnvironmentConfiguration.PROP_CELESTIAL_BODY_MAP)) {
			LOG.info(String.format("New celstial body map is %s", environmentConfiguration));
			createSky();
		} else if (evt.getPropertyName().equals(EnhancedEnvironmentConfiguration.PROP_CLOUDS_MAP)) {
			LOG.info(String.format("New clouds body map is %s", environmentConfiguration.getCloudsMap()));
			createSky();
		}

	}

	public void createSky() {
		if (!environmentConfiguration.isSkyDome()) {
			removeSky();
			app.getViewPort().setBackgroundColor(environmentConfiguration.getViewportColor());
			environmentLight.setDirectionalEnabled(environmentConfiguration.isDirectionalEnabled());
			environmentLight.setDirectionalColor(environmentConfiguration.getDirectionalColor());
			environmentLight.setAmbientColor(environmentConfiguration.getAmbientColor());
			environmentLight.setSunToLocation(environmentConfiguration.getDirectionalPosition());
		} else {
			configureSkydome();
		}

	}

	protected void keepSunAtDistanceFromCamera(float tpf) {
		if (sun != null && (!environmentConfiguration.isControlDirectionalPosition()
				|| !environmentConfiguration.isSkyDome())) {
			sun.setLocalTranslation(environmentLight.getSunRepresentationPosition());
		}
	}

	@Override
	protected void onEnvironmentDetached() {
		updateAudio(null);
	}

	@Override
	protected void onEnvironmentCleanup() {
		if (environmentConfiguration != null) {
			environmentConfiguration.removePropertyChangeListener(this);
		}
		this.environmentConfiguration = null;

		// Clean up any scene elements
		removeSun();
		removeSky();
		stopWeather();

	}

	@Override
	public void handlePrefUpdateSceneThread(PreferenceChangeEvent evt) {
	}

	protected void attachWeatherToPlayer(Spatial weather) {
		// For when there is a player
		if (weatherNode != null) {
			weatherNode.attachChild(weather);
		}
	}

	protected void createWeather() {
		LOG.info(String.format("Setting weather to %s", environmentConfiguration.getWeather()));
		switch (environmentConfiguration.getWeather()) {
		case RAIN:
			if (weather == null || !(weather instanceof Rain)) {
				stopWeather();
				weather = new Rain("rain", assetManager, environmentConfiguration.getWeatherIntensity(),
						environmentConfiguration.getWeatherDensity(), environmentConfiguration.isWind());
				attachWeatherToPlayer(weather);
			} else {
				((Rain) weather).setSpeed(environmentConfiguration.getWeatherIntensity());
				((Rain) weather).setDensity(environmentConfiguration.getWeatherDensity());
				((Rain) weather).setWind(environmentConfiguration.isWind());
				((Rain) weather).reconfigure();
			}
			break;
		case SNOW:
			if (weather == null || !(weather instanceof Snow)) {
				stopWeather();
				weather = new Snow("snow", assetManager, environmentConfiguration.getWeatherIntensity(),
						environmentConfiguration.getWeatherDensity(), environmentConfiguration.isWind());
				attachWeatherToPlayer(weather);
			} else {
				((Snow) weather).setSpeed(environmentConfiguration.getWeatherIntensity());
				((Snow) weather).setDensity(environmentConfiguration.getWeatherDensity());
				((Snow) weather).setWind(environmentConfiguration.isWind());
				((Snow) weather).reconfigure();
			}
			break;
		default:
			stopWeather();
			break;
		}

		reloadPlaylist(environmentConfiguration, PlaylistType.AMBIENT_NOISE);
	}

	// protected void updateWeatherMusic(EnhancedEnvironmentConfiguration
	// environmentConfiguration) {
	// audio.clearQueues(AudioQueue.WEATHER);
	// audio.stopAudio(true, AudioQueue.WEATHER);
	// List<String> weatherMusic = environmentConfiguration.getWeatherMusic();
	// if (!Objects.equal(weatherMusic, this.weatherMusic)) {
	// this.weatherMusic = weatherMusic;
	// updateSoundList("%s", environmentConfiguration.getAmbientMusicDelay(),
	// true, weatherMusic, AudioQueue.WEATHER);
	// }
	// }

	protected void createSound() {
		audio.clearQueues(AUDIO_QUEUES);
		audio.stopAudio(true, AUDIO_QUEUES);
		updateAudio(environmentConfiguration);
	}

	// protected void updateAudio(AbstractEnvironmentConfiguration
	// environmentConfiguration) {
	// updateAmbientNose(environmentConfiguration);
	// updateActivateMusic(environmentConfiguration);
	// updateAmbientMusic(environmentConfiguration);
	// }
	//
	// private void updateAmbientMusic(AbstractEnvironmentConfiguration
	// environmentConfiguration) {
	// List<String> ambientMusic = environmentConfiguration.getAmbientMusic();
	// if (!Objects.equal(ambientMusic, this.ambientMusic)) {
	// this.ambientMusic = ambientMusic;
	// updateSoundList("%1$s", environmentConfiguration.getAmbientMusicDelay(),
	// true, ambientMusic, AudioQueue.DEFAULT);
	// }
	// }
	//
	// private void updateActivateMusic(AbstractEnvironmentConfiguration
	// environmentConfiguration) {
	// // Activate Music Comes first, then ambient music
	// List<String> activateMusic = environmentConfiguration.getActivateMusic();
	// if (!Objects.equal(activateMusic, this.activateMusic)) {
	// this.activateMusic = activateMusic;
	// updateSoundList("%1$s", environmentConfiguration.getActivateMusicDelay(),
	// true, activateMusic, AudioQueue.DEFAULT);
	//
	// }
	// }
	//
	// private void updateAmbientNose(AbstractEnvironmentConfiguration
	// environmentConfiguration) {
	// // Ambient Noise
	// List<String> ambientNoise = environmentConfiguration.getAmbientNoise();
	// if (!Objects.equal(ambientNoise, this.ambientNoise)) {
	// this.ambientNoise = ambientNoise;
	// updateSoundList("%1$s", 0, true, ambientNoise, AudioQueue.AMBIENT);
	// }
	// }

	protected void createFog() {
		LOG.info("Creating fog on post processor");
		PostProcessAppState pp = stateManager.getState(PostProcessAppState.class);
		if (pp == null) {
			LOG.info("No post processor enabled, no fog");
		} else {
			pp.setFogFilterMode(FogFilterMode.JME3);
			final FogFilter fogFilter = (FogFilter) pp.getFogFilter();
			fogFilter.setEnabled(environmentConfiguration.isFog()
					|| (environmentConfiguration.isControlFog() && environmentConfiguration.isSkyDome()));
			fogFilter.setFogDensity(environmentConfiguration.getFogDensity());
			fogFilter.setFogDistance(environmentConfiguration.getFogDistance());
			if (fogFilter.isEnabled()) {
				fogFilter.setFogColor(environmentConfiguration.getFogColor());
			}
			LOG.info("Add fog post processor");
		}
	}

	protected void play(String play, AudioQueue queue) {
		play(play, queue, 1f);
	}

	protected void play(String play, AudioQueue queue, float gain) {
		play(play, queue, false, gain);
	}

	protected void play(String play, AudioQueue queue, boolean loop, float gain) {
		if (play != null && !play.equals("")) {
			AudioQueueHandler q = audio.getQueue(queue);
			if (q != null && (q.isQueued(play) || q.isPlaying(play))) {
				// Hasn't changed
				if (loop != q.isLoop(play)) {
					q.setLoop(play, loop);
				} else if (gain != q.getGain(play)) {
					q.setGain(play, gain);
				}
			} else {
				playNew(queue, play, loop, gain);
			}
		} else {
			audio.stopAudio(true, queue);
		}

	}

	protected void playNew(AudioQueue queue, String play, boolean loop, float gain) {
		audio.getQueue(queue);
		audio.clearQueues(queue);
		audio.stopAudio(true, queue);
		LOG.info(String.format("Added %s to queue", play));
		audio.queue(new QueuedAudio(this, play, 0, loop, queue, gain));
	}

	protected String checkNone(String val) {
		return val == null ? "None" : val;
	}

	private void stopWeather() {
		if (weather != null) {
			LOG.info("Removing current weather");
			// Stop nicely, let particles finish
			weather.stopNow(false);
			final AbstractWeather w = weather;
			app.getAlarm().timed(new Callable<Void>() {
				public Void call() throws Exception {
					// Completely remove from scene
					w.stopNow(true);
					return null;
				}
			}, 10);
			weather.removeFromParent();
			weather = null;
		}
	}

	private void removeSky() {
		if (skyDome != null) {
			LOG.info("Removing sky");
			skyDome.setEnabled(false);
			skyDome = null;
		}
		if (sky != null) {
			sky.removeFromParent();
			sky = null;
		}
	}

	private void checkSun() {
		if (environmentLight.isDirectionalEnabled()) {
			if (sun == null) {
				gameNode.attachChild(sun = new Sun(environmentLight, assetManager) {
					@Override
					protected void onSunApply() {
						EnvironmentAppState env = stateManager.getState(EnvironmentAppState.class);
						((EnhancedEnvironmentConfiguration) env.getEnvironment())
								.setDirectionalPosition(getLocalTranslation());
					}
				});
			}
			sun.setSunColor(environmentLight.getSunColor());
			sun.setLocalTranslation(environmentLight.getSunRepresentationPosition());
			LOG.info(String.format("Added sun representation at %s (dir is %s) with color of %s",
					sun.getLocalTranslation(), environmentLight.getSunDirection(), environmentLight.getSunColor()));
		} else if (!environmentLight.isDirectionalEnabled() && sun != null) {
			removeSun();
		}
	}

	private void removeSun() {
		if (sun != null) {
			LOG.info("Removing sun representation");
			sun.removeFromParent();
			sun = null;
		}
	}

	private void configureSkydome() {
		PostProcessAppState postProcess = stateManager.getState(PostProcessAppState.class);
		FogFilter fogFilter = postProcess == null ? null : (FogFilter) postProcess.getFogFilter();

		// Have to replace the whole thing when images change
		if (skyDome != null && (lastSkyMap == null || !lastSkyMap.equals(environmentConfiguration.getSkyMap())
				|| lastCloudsMap == null || !lastCloudsMap.equals(environmentConfiguration.getCloudsMap())
				|| lastFogAlphaMap == null || !lastFogAlphaMap.equals(environmentConfiguration.getFogAlphaMap())
				|| lastCelestialBodyMap == null
				|| !lastCelestialBodyMap.equals(environmentConfiguration.getCelestialBodyMap()))) {
			removeSky();
		}

		if (skyDome == null) {
			LOG.info("Creating sky");
			lastSkyMap = environmentConfiguration.getSkyMap();
			lastCelestialBodyMap = environmentConfiguration.getCelestialBodyMap();
			lastCloudsMap = environmentConfiguration.getCloudsMap();
			lastFogAlphaMap = environmentConfiguration.getFogAlphaMap();
			skyDome = new SkyDomeControl(assetManager, camera, "Environment/SkyDome.j3o",
					environmentConfiguration.absolutize(lastSkyMap),
					environmentConfiguration.absolutize(lastCelestialBodyMap),
					environmentConfiguration.absolutize(lastCloudsMap),
					environmentConfiguration.absolutize(lastFogAlphaMap)) {
				@Override
				protected void setSunColor(ColorRGBA color) {
					// Update the environment light model
					environmentLight.setDirectionalColor(color);
				}

				@Override
				protected void setAmbientColor(ColorRGBA color) {
					// Update the environment light model
					environmentLight.setAmbientColor(color);
				}
			};

			// Leave the skydome to control the lighting.
			environmentLight.setAmbientColor(ColorRGBA.Gray);
			environmentLight.setDirectionalColor(ColorRGBA.Gray);
			environmentLight.setAmbientEnabled(true);
			environmentLight.setDirectionalEnabled(true);

			// Attach the lights to the skydome
			skyDome.setLight(environmentLight);
			skyDome.setEnabled(true);
		} else {
			LOG.info(("Updating current sky"));
			// skyDome.setFogAlphaMap(environmentConfiguration.getFogAlphaMap());
			// skyDome.setMoonMap(environmentConfiguration.getMoonMap());
			// skyDome.setCloudsMap(environmentConfiguration.getCloudsMap());
			// skyDome.setNightSkyMap(environmentConfiguration.getNightSkyMap());

		}

		// Configure light. We always
		if (!environmentConfiguration.isControlDirectionalPosition()) {
			environmentLight.setSunToLocation(environmentConfiguration.getDirectionalPosition());
		}

		// Cycle clouds in or out
		if (skyDome.getIsClouds() && !environmentConfiguration.isClouds()) {
			skyDome.cycleCloudsOut();
		} else if (!skyDome.getIsClouds() && environmentConfiguration.isClouds()) {
			skyDome.cycleCloudsIn();
		}

		/*
		 * The following are dependent the duration of the day. E.g. the sun
		 * moving from east to west needs to know how long the day is
		 */
		skyDome.setDuration(cycleDuration);
		skyDome.setDirectionalStartPosition(environmentConfiguration.getDirectionalPosition());
		skyDome.setControlDirectionalPosition(environmentConfiguration.isControlDirectionalPosition());
		skyDome.setStartDirectionalAngle(environmentConfiguration.getStartDirectionalAngle() * FastMath.DEG_TO_RAD);
		skyDome.setEndDirectionalAngle(environmentConfiguration.getEndDirectionalAngle() * FastMath.DEG_TO_RAD);

		/*
		 * Set sky dome parameters, most should smoothly transition to the new
		 * values (at the transition speed)
		 */
		skyDome.setTransitionSpeed(environmentConfiguration.getTransitionSpeed());
		skyDome.setFogFilter(fogFilter, app.getViewPort());
		skyDome.setControlFog(environmentConfiguration.isControlFog());
		skyDome.setFogColor(environmentConfiguration.getFogColor());
		skyDome.setCelestialBodyAlpha(environmentConfiguration.getCelestialBodyAlpha());
		skyDome.setCelestialBodyDirection(environmentConfiguration.getCelestialBodyDirection());
		skyDome.setCelestialBodyScale(environmentConfiguration.getCelestialBodyScale());
		skyDome.setCelestialBodySpeed(environmentConfiguration.getCelestialBodySpeed());
		skyDome.setCelestialBodyWrap(environmentConfiguration.getCelestialBodyWrap());
		skyDome.setSkyMapAlpha(environmentConfiguration.getSkyMapAlpha());
		skyDome.setSkyMapUseAsMap(environmentConfiguration.isSkyMapUseAsMap());
		skyDome.setSkyMapScale(environmentConfiguration.getSkyMapScale());
		skyDome.setSkyMapWrap(environmentConfiguration.getSkyMapWrap());
		skyDome.setCloudsMapUseAsMap(environmentConfiguration.isCloudsMapUseAsMap());
		skyDome.setCloudsMapScale(environmentConfiguration.getCloudsMapScale());
		skyDome.setSkyColor(environmentConfiguration.getSkyColor());
		skyDome.setCloudCycleSpeed(environmentConfiguration.getCloudCycleSpeed());
		skyDome.setCloudMaxOpacity(environmentConfiguration.getCloudMaxOpacity());
		skyDome.setCloudMinOpacity(environmentConfiguration.getCloudMinOpacity());
		skyDome.setCloudsFarRotation(environmentConfiguration.getCloudsFarRotation());
		skyDome.setCloudsFarSpeed(environmentConfiguration.getCloudsFarSpeed());
		skyDome.setCloudsNearRotation(environmentConfiguration.getCloudsNearRotation());
		skyDome.setCloudsNearSpeed(environmentConfiguration.getCloudsNearSpeed());
		skyDome.setAmbientEnabled(environmentConfiguration.isAmbientEnabled());
		skyDome.setDirectionalEnabled(environmentConfiguration.isDirectionalEnabled());
		skyDome.setDirectionalLight(environmentConfiguration.getDirectionalColor());
		skyDome.setAmbientLight(environmentConfiguration.getAmbientColor());
		// skyDome.setMoonRotation(environmentConfiguration.getMoonRotation());

		LOG.info(String.format("     Day/Night Duration: %4.3f", cycleDuration));
		LOG.info(String.format("     Start Angle: %4.3f", environmentConfiguration.getStartDirectionalAngle()));
		LOG.info(String.format("     End Angle: %4.3f", environmentConfiguration.getEndDirectionalAngle()));
		LOG.info(String.format("     Day/Night Speed: %4.3f", environmentConfiguration.getTransitionSpeed()));
		LOG.info(String.format("     Celestial Body Speed: %4.3f", environmentConfiguration.getCelestialBodySpeed()));
		LOG.info(String.format("     Celestial Body Direction: %4.3f",
				environmentConfiguration.getCelestialBodyDirection()));
		LOG.info(String.format("     Celestial Body Scale: %4.3f", environmentConfiguration.getCelestialBodyScale()));
		LOG.info(String.format("     Day: %s", ExtrasUtil.toHexString(environmentConfiguration.getSkyColor())));
		LOG.info(String.format("     Cloud Cycle Speed: %4.3f", environmentConfiguration.getCloudCycleSpeed()));
		LOG.info(String.format("     Cloud Max Opacity: %4.3f", environmentConfiguration.getCloudMaxOpacity()));
		LOG.info(String.format("     Cloud Min Opacity: %4.3f", environmentConfiguration.getCloudMinOpacity()));
		LOG.info(String.format("     Cloud Far Rotation: %4.3f", environmentConfiguration.getCloudsFarRotation()));
		LOG.info(String.format("     Cloud Far Speed: %4.3f", environmentConfiguration.getCloudsFarSpeed()));
		LOG.info(String.format("     Cloud Near Rotation: %4.3f", environmentConfiguration.getCloudsNearRotation()));
		LOG.info(String.format("     Cloud Near Speed: %4.3f", environmentConfiguration.getCloudsNearSpeed()));
		LOG.info(String.format("         Directional: %s",
				ExtrasUtil.toHexString(environmentConfiguration.getDirectionalColor())));
		LOG.info(String.format("         Ambient: %s", ExtrasUtil.toHexString(environmentConfiguration.getAmbientColor())));
		LOG.info(String.format("     Fog: %s", environmentConfiguration.isFog()));
		LOG.info(String.format("     Control Fog: %s", environmentConfiguration.isControlFog()));
		LOG.info(String.format("         Color: %s", ExtrasUtil.toHexString(environmentConfiguration.getFogColor())));
		LOG.info(String.format("     Directional: %s (%s)", environmentConfiguration.getDirectionalColor(),
				environmentConfiguration.isDirectionalEnabled()));
		LOG.info(String.format("     Ambient: %s (%s)", environmentConfiguration.getAmbientColor(),
				environmentConfiguration.isAmbientEnabled()));

		if (sky == null) {
			LOG.info("    Attaching sky to world");
			sky = new Node();
			sky.setQueueBucket(Bucket.Sky);
			sky.addControl(skyDome);
			sky.setCullHint(Spatial.CullHint.Dynamic);
			sky.setShadowMode(RenderQueue.ShadowMode.Off);
			gameNode.attachChild(sky);
		}
	}

	public void emote(long id, String sender, String emote) {
	}

	@Override
	public void setFollowCamera(boolean followCamera) {
		// TODO
	}

}

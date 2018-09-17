package org.iceskies.environment.enhanced;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.prefs.Preferences;

import org.icescene.environment.EnvironmentPhase;
import org.icescene.environment.Weather;
import org.iceskies.environment.AbstractEnvironmentConfiguration;
import org.iceskies.environment.AbstractEnvironmentConfigurationEditorPanel;

import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.texture.Texture.WrapMode;

import icetone.core.BaseScreen;
import icetone.core.undo.UndoManager;

public class EnhancedEnvironmentConfiguration extends AbstractEnvironmentConfiguration {

	private static final long serialVersionUID = 1L;

	public final static String PROP_CLOUDS = "Clouds";
	public final static String PROP_SKY_DOME = "SkyDome";
	public final static String PROP_CELESTIAL_BODY_ALPHA = "CelestialBodyAlpha";
	public final static String PROP_CELESTIAL_BODY_DIRECTION = "CelestialBodyDirection";
	public final static String PROP_CELESTIAL_BODY_SPEED = "CelestialBodySpeed";
	public final static String PROP_CELESTIAL_BODY_SCALE = "CelestialBodyScale";
	public final static String PROP_CELESTIAL_BODY_WRAP = "CelestialBodyWrap";
	public final static String PROP_ACTIVATE_MUSIC_DELAY = "ActivateMusicDelay";
	public final static String PROP_AMBIENT_COLOR = "AmbientColor";
	public final static String PROP_AMBIENT_MUSIC_DELAY = "AmbientMusic";
	public final static String PROP_CELESTIAL_BODY_MAP = "CelestialBodyMap";
	public final static String PROP_END_DIRECTIONAL_ANGLE = "EndDirectionalAngle";
	public final static String PROP_START_DIRECTIONAL_ANGLE = "StartDirectionalAngle";
	public final static String PROP_CONTROL_DIRECTIONAL_POSITION = "ControlDirectionalPosition";
	public final static String PROP_DIRECTIONAL_ENABLED = "DirectionalEnabled";
	public final static String PROP_AMBIENT_ENABLED = "AmbientEnabled";
	public final static String PROP_LIGHT_BEAMS = "LightBeams";
	public final static String PROP_VIEWPORT_COLOR = "ViewportColor";
	public final static String PROP_CLOUDS_MAP = "CloudsMap";
	public final static String PROP_CLOUDS_MAP_WRAP = "CloudsMapWrap";
	public final static String PROP_CLOUDS_MAP_USE_AS_MAP = "CloudsMapUseAsMap";
	public final static String PROP_CLOUDS_MAP_SCALE = "CloudsMapScale";
	public final static String PROP_FOG_ALPHA_MAP = "CogAlphaMap";
	public final static String PROP_SKY_MAP = "SkyMap";
	public final static String PROP_SKY_MAP_WRAP = "SkyMapWrap";
	public final static String PROP_SKY_MAP_USE_AS_MAP = "SkyMapUseAsMap";
	public final static String PROP_SKY_MAP_SCALE = "SkyMapScale";
	public final static String PROP_WIND = "Wind";
	public final static String PROP_WEATHER_MUSIC_GAIN = "WeatherMusicGain";
	public final static String PROP_WEATHER_INTENSITY = "WeatherIntensity";
	public final static String PROP_WEATHER_DENSITY = "WeatherDensity";
	public final static String PROP_WEATHER = "Weather";
	public final static String PROP_DIRECTIONAL_COLOR = "DirectionalColor";
	public final static String PROP_FOG_DENSITY = "FogDensity";
	public final static String PROP_FOG_DISTANCE = "FogDistance";
	public final static String PROP_FOG = "Fog";
	public final static String PROP_TEXTURE = "Texture";
	public final static String PROP_WEATHER_MUSIC = "WeatherMusic";
	public final static String PROP_SKY_MAP_ALPHA = "SkyMapAlpha";
	public final static String PROP_FOG_COLOR = "FogColor";
	public final static String PROP_SKY_COLOR = "SkyColor";
	public final static String PROP_TRANSITION_SPEED = "TransitionSpeed";
	public final static String PROP_CONTROL_FOG = "ControlFog";
	public final static String PROP_CLOUDS_NEAR_SPEED = "CloudsNearSpeed";
	public final static String PROP_CLOUDS_NEAR_ROTATION = "CloudsNearRotation";
	public final static String PROP_CLOUDS_FAR_SPEED = "CloudsFarSpeed";
	public final static String PROP_CLOUDS_FAR_ROTATION = "CloudsFarRotation";
	public final static String PROP_CLOUD_MIN_OPACITY = "CloudMinOpacity";
	public final static String PROP_CLOUD_MAX_OPACITY = "CloudMaxOpacity";
	public final static String PROP_CLOUD_CYCLE_SPEED = "CloudCycleSpeed";

	private boolean controlDirectionPosition = true;
	private float startDirectionalAngle = 0;
	private float endDirectionalAngle = 180;
	private boolean lighBeams = true;
	private boolean clouds = true;
	private float fogDensity = 1.0f;
	private boolean skyDome = true;
	private List<String> ambientMusic = new ArrayList<String>();
	private List<String> ambientNoise = new ArrayList<String>();
	private List<String> activateMusic = new ArrayList<String>();
	private boolean fog = false;
	private float fogDistance = 155f;
	private ColorRGBA viewportColour = ColorRGBA.Black.clone();
	private ColorRGBA ambientColour = ColorRGBA.White.clone();
	private ColorRGBA skyColour = ColorRGBA.White.clone();
	private ColorRGBA directionalColour = ColorRGBA.White.clone();
	private ColorRGBA fogColour = ColorRGBA.Gray.clone();
	private float cloudCycleSpeed = 0.125f;
	private float cloudMinOpacity = 0;
	private float cloudMaxOpacity = 1;
	private float transitionSpeed = 0.125f;
	private boolean controlFog = false;
	private float cloudsNearSpeed = 0.1f;
	private float cloudsNearRotation = 0f;
	private float cloudsFarSpeed = 0.25f;
	private float cloudsFarRotation = FastMath.HALF_PI + 0.02f;
	private float celestialBodyDirection = 75f;
	private float celestialBodySpeed = .0185f;
	private float celestialBodyAlpha = 1.0f;
	private float celstialBodyScale = 1.0f;
	private WrapMode celstialBodyWrap = WrapMode.Repeat;
	private float skyMapAlpha = 1.0f;
	private boolean wind = false;
	private Weather weather = Weather.FINE;
	private float weatherIntensity = 5f;
	private float weatherDensity = 5f;
	private List<String> weatherMusic = new ArrayList<String>();
	private int ambientMusicDelay = 0;
	private int activateMusicDelay = 0;
	private float weatherMusicGain = 1f;
	private String skyMap = "Enhanced/SkyNight_L.png";
	private boolean skyMapUseAsMap = true;
	private float skyMapScale = 1f;
	private WrapMode skyMapWrap = WrapMode.Repeat;
	private String celstialBodyMap = "Enhanced/Moon.png";
	private String fogAlphaMap = "Enhanced/Fog_Alpha.png";
	private String cloudsMap = "Enhanced/Clouds_L.png";
	private boolean cloudsMapUseAsMap = true;
	private float cloudsMapScale = 1f;
	private boolean ssao = false;
	private boolean ambientEnabled = true;
	private boolean directionalEnabled = true;

	public static void save(String filename, EnvironmentPhase type, EnhancedEnvironmentConfiguration environmentConfiguration) {
		// String finalName = filename + (type == EnvironmentPhase.NONE ? "" :
		// Icelib.toEnglish(type));
		// File dir = new File("assets" + File.separator + "Environment");
		// dir.mkdirs();
		// File file = new File(dir, finalName + ".cfg");
		// LOG.info(String.format("Writing %s to %s", finalName, file));
		// try {
		// FileOutputStream fos = new FileOutputStream(file);
		// try {
		// environmentConfiguration.write(fos, false);
		// } finally {
		// fos.close();
		// }
		// } catch (IOException ioe) {
		// LOG.log(Level.SEVERE, "Failed to save environment.", ioe);
		// }
		throw new UnsupportedOperationException();
	}

	public EnhancedEnvironmentConfiguration(String key) {
		super(key);
	}

	@Override
	public AbstractEnvironmentConfigurationEditorPanel<?> createEditor(UndoManager undoManager, BaseScreen screen,
			Preferences prefs) {
		return new EnhancedEnvironmentConfigurationEditorPanel(undoManager, screen, prefs, this);
	}

	public boolean isEditable() {
		return true;
	}

	public void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
		if (oldValue != null && newValue != null && oldValue.equals(newValue)) {
			return;
		}
		changeSupport.firePropertyChange(new PropertyChangeEvent(this, propertyName, oldValue, newValue));
	}

	public String getKey() {
		return key;
	}

	public boolean isSkyDome() {
		return skyDome;
	}

	public void setSkyDome(boolean skyDome) {
		boolean wasSkyDome = isSkyDome();
		if (wasSkyDome != skyDome) {
			this.skyDome = skyDome;
			firePropertyChange(PROP_SKY_DOME, wasSkyDome, skyDome);
		}
	}

	public boolean isClouds() {
		return clouds;
	}

	public void setClouds(boolean clouds) {
		boolean was = isClouds();
		if (was != clouds) {
			this.clouds = clouds;
			firePropertyChange(PROP_CLOUDS, was, clouds);
		}
	}

	public boolean isSsao() {
		return ssao;
	}

	public void setSsao(boolean ssao) {
		boolean was = isSsao();
		if (was != ssao) {
			this.ssao = ssao;
			firePropertyChange(PROP_FOG, was, ssao);
		}
	}

	public boolean isFog() {
		return fog;
	}

	public void setFog(boolean fog) {
		boolean was = isFog();
		if (was != fog) {
			this.fog = fog;
			firePropertyChange(PROP_FOG, was, fog);
		}
	}

	public boolean isControlFog() {
		return controlFog;
	}

	public void setControlFog(boolean controlFog) {
		boolean was = isControlFog();
		if (was != controlFog) {
			this.controlFog = controlFog;
			firePropertyChange(PROP_CONTROL_FOG, was, controlFog);
		}
	}

	public boolean isWind() {
		return wind;
	}

	public void setWind(boolean wind) {
		boolean was = isWind();
		if (was != wind) {
			this.wind = wind;
			firePropertyChange(PROP_WIND, was, wind);
		}
	}

	public boolean isLightBeams() {
		return lighBeams;
	}

	public void setLightBeams(boolean lightBeams) {
		boolean was = isLightBeams();
		if (was != lightBeams) {
			this.lighBeams = lightBeams;
			firePropertyChange(PROP_LIGHT_BEAMS, was, lightBeams);
		}
	}

	public boolean isAmbientEnabled() {
		return ambientEnabled;
	}

	public void setAmbientEnabled(boolean ambientEnabled) {
		boolean was = isAmbientEnabled();
		if (was != ambientEnabled) {
			this.ambientEnabled = ambientEnabled;
			firePropertyChange(PROP_AMBIENT_ENABLED, was, ambientEnabled);
		}
	}

	public boolean isDirectionalEnabled() {
		return directionalEnabled;
	}

	public void setDirectionalEnabled(boolean directionalEnabled) {
		boolean was = isDirectionalEnabled();
		if (was != directionalEnabled) {
			this.directionalEnabled = directionalEnabled;
			firePropertyChange(PROP_DIRECTIONAL_ENABLED, was, directionalEnabled);
		}
	}

	public boolean isControlDirectionalPosition() {
		return controlDirectionPosition;
	}

	public void setControlDirectionalPosition(boolean controlDirectionalPosition) {
		boolean was = isControlDirectionalPosition();
		if (was != controlDirectionalPosition) {
			this.controlDirectionPosition = controlDirectionalPosition;
			firePropertyChange(PROP_CONTROL_DIRECTIONAL_POSITION, was, controlDirectionalPosition);
		}
	}

	public float getCloudCycleSpeed() {
		return cloudCycleSpeed;
	}

	public void setCloudCycleSpeed(float cloudCycleSpeed) {
		float was = getCloudCycleSpeed();
		if (was != cloudCycleSpeed) {
			this.cloudCycleSpeed = cloudCycleSpeed;
			firePropertyChange(PROP_CLOUD_CYCLE_SPEED, was, cloudCycleSpeed);
		}
	}

	public float getCloudMaxOpacity() {
		return cloudMaxOpacity;
	}

	public void setCloudMaxOpacity(float cloudMaxOpacity) {
		float was = getCloudMaxOpacity();
		if (was != cloudMaxOpacity) {
			this.cloudMaxOpacity = cloudMaxOpacity;
			firePropertyChange(PROP_CLOUD_MAX_OPACITY, was, cloudMaxOpacity);
		}
	}

	public float getCloudMinOpacity() {
		return cloudMinOpacity;
	}

	public void setCloudMinOpacity(float cloudMinOpacity) {
		float was = getCloudMinOpacity();
		if (was != cloudMinOpacity) {
			this.cloudMinOpacity = cloudMinOpacity;
			firePropertyChange(PROP_CLOUD_MIN_OPACITY, was, cloudMinOpacity);
		}
	}

	public float getCloudsFarRotation() {
		return cloudsFarRotation;
	}

	public void setCloudsFarRotation(float cloudsFarRotation) {
		float was = getCloudsFarRotation();
		if (was != cloudsFarRotation) {
			this.cloudsFarRotation = cloudsFarRotation;
			firePropertyChange(PROP_CLOUDS_FAR_ROTATION, was, cloudsFarRotation);
		}
	}

	public float getCloudsFarSpeed() {
		return cloudsFarSpeed;
	}

	public void setCloudsFarSpeed(float cloudsFarSpeed) {
		float was = getCloudsFarSpeed();
		if (was != cloudsFarSpeed) {
			this.cloudsFarSpeed = cloudsFarSpeed;
			firePropertyChange(PROP_CLOUDS_FAR_SPEED, was, cloudsFarSpeed);
		}
	}

	public float getCloudsNearRotation() {
		return cloudsNearRotation;
	}

	public void setCloudsNearRotation(float cloudsNearRotation) {
		float was = getCloudsNearRotation();
		if (was != cloudsNearRotation) {
			this.cloudsNearRotation = cloudsNearRotation;
			firePropertyChange(PROP_CLOUDS_NEAR_ROTATION, was, cloudsNearRotation);
		}
	}

	public float getCloudsNearSpeed() {
		return cloudsNearSpeed;
	}

	public void setCloudsNearSpeed(float cloudsNearSpeed) {
		float was = getCloudsNearSpeed();
		if (was != cloudsNearSpeed) {
			this.cloudsNearSpeed = cloudsNearSpeed;
			firePropertyChange(PROP_CLOUDS_NEAR_SPEED, was, cloudsNearSpeed);
		}
	}

	public float getTransitionSpeed() {
		return transitionSpeed;
	}

	public void setTransitionSpeed(float transitionSpeed) {
		float was = getTransitionSpeed();
		if (was != transitionSpeed) {
			this.transitionSpeed = transitionSpeed;
			firePropertyChange(PROP_TRANSITION_SPEED, was, transitionSpeed);
		}
	}

	public float getCelestialBodyDirection() {
		return celestialBodyDirection;
	}

	public void setCelestialBodyDirection(float celestialBodyDirection) {
		float was = getCelestialBodyDirection();
		if (was != celestialBodyDirection) {
			this.celestialBodyDirection = celestialBodyDirection;
			firePropertyChange(PROP_CELESTIAL_BODY_DIRECTION, was, celestialBodyDirection);
		}
	}

	public float getCelestialBodySpeed() {
		return celestialBodySpeed;
	}

	public void setCelestialBodySpeed(float celestialBodySpeed) {
		float was = getCelestialBodySpeed();
		if (was != celestialBodySpeed) {
			this.celestialBodySpeed = celestialBodySpeed;
			firePropertyChange(PROP_CELESTIAL_BODY_SPEED, was, celestialBodySpeed);
		}
	}

	public float getCelestialBodyScale() {
		return celstialBodyScale;
	}

	public void setCelestialBodyScale(float celestialBodyScale) {
		float was = getCelestialBodyScale();
		if (was != celestialBodyScale) {
			this.celstialBodyScale = celestialBodyScale;
			firePropertyChange(PROP_CELESTIAL_BODY_SCALE, was, celestialBodyScale);
		}
	}

	public float getSkyMapAlpha() {
		return skyMapAlpha;
	}

	public void setSkyMapAlpha(float skyMapAlpha) {
		float was = getSkyMapAlpha();
		if (was != skyMapAlpha) {
			this.skyMapAlpha = skyMapAlpha;
			firePropertyChange(PROP_SKY_MAP_ALPHA, was, skyMapAlpha);
		}
	}

	public float getCelestialBodyAlpha() {
		return celestialBodyAlpha;
	}

	public void setCelestialBodyAlpha(float celestialBodyAlpha) {
		float was = getCelestialBodyAlpha();
		if (was != celestialBodyAlpha) {
			this.celestialBodyAlpha = celestialBodyAlpha;
			firePropertyChange(PROP_CELESTIAL_BODY_ALPHA, was, celestialBodyAlpha);
		}
	}

	public float getFogDistance() {
		return fogDistance;
	}

	public void setFogDistance(float fogDistance) {
		float was = getFogDistance();
		if (was != fogDistance) {
			this.fogDistance = fogDistance;
			firePropertyChange(PROP_FOG_DISTANCE, was, fogDistance);
		}
	}

	public float getFogDensity() {
		return fogDensity;
	}

	public void setFogDensity(float fogDensity) {
		float was = getFogDensity();
		if (was != fogDensity) {
			this.fogDensity = fogDensity;
			firePropertyChange(PROP_FOG_DENSITY, was, fogDensity);
		}
	}

	public float getWeatherIntensity() {
		return weatherIntensity;
	}

	public void setWeatherIntensity(float weatherIntensity) {
		float was = getWeatherIntensity();
		if (was != weatherIntensity) {
			this.weatherIntensity = weatherIntensity;
			firePropertyChange(PROP_WEATHER_INTENSITY, was, weatherIntensity);
		}
	}

	public float getWeatherDensity() {
		return weatherDensity;
	}

	public void setWeatherDensity(float weatherDensity) {
		float was = getWeatherDensity();
		if (was != weatherDensity) {
			this.weatherDensity = weatherDensity;
			firePropertyChange(PROP_WEATHER_DENSITY, was, weatherDensity);
		}
	}

	public float getWeatherMusicGain() {
		return weatherMusicGain;
	}

	public void setWeatherMusicGain(float weatherMusicGain) {
		float was = getWeatherMusicGain();
		if (was != weatherMusicGain) {
			this.weatherMusicGain = weatherMusicGain;
			firePropertyChange(PROP_WEATHER_MUSIC_GAIN, was, weatherMusicGain);
		}
	}

	public float getStartDirectionalAngle() {
		return startDirectionalAngle;
	}

	public void setStartDirectionalAngle(float startDirectionalAngle) {
		float was = getStartDirectionalAngle();
		if (was != startDirectionalAngle) {
			this.startDirectionalAngle = startDirectionalAngle;
			firePropertyChange(PROP_START_DIRECTIONAL_ANGLE, was, startDirectionalAngle);
		}
	}

	public float getEndDirectionalAngle() {
		return endDirectionalAngle;
	}

	public void setEndDirectionalAngle(float endDirectionalAngle) {
		float was = getEndDirectionalAngle();
		if (was != endDirectionalAngle) {
			this.endDirectionalAngle = endDirectionalAngle;
			firePropertyChange(PROP_END_DIRECTIONAL_ANGLE, was, endDirectionalAngle);
		}
	}

	public ColorRGBA getAmbientColor() {
		return ambientColour;
	}

	public void setAmbientColor(ColorRGBA ambientColor) {
		ColorRGBA was = getAmbientColor();
		if (!Objects.equals(was, ambientColor)) {
			this.ambientColour = ambientColor;
			firePropertyChange(PROP_AMBIENT_COLOR, was, ambientColor);
		}
	}

	public ColorRGBA getDirectionalColor() {
		return directionalColour;
	}

	public void setDirectionalColor(ColorRGBA directionalColor) {
		ColorRGBA was = getDirectionalColor();
		if (!Objects.equals(was, directionalColor)) {
			this.directionalColour = directionalColor;
			firePropertyChange(PROP_DIRECTIONAL_COLOR, was, directionalColor);
		}
	}

	public ColorRGBA getViewportColor() {
		return viewportColour;
	}

	public void setViewportColor(ColorRGBA viewportColor) {
		ColorRGBA was = getViewportColor();
		if (!Objects.equals(was, viewportColor)) {
			this.viewportColour = viewportColor;
			firePropertyChange(PROP_VIEWPORT_COLOR, was, viewportColor);
		}
	}

	public ColorRGBA getSkyColor() {
		return skyColour;
	}

	public void setSkyColor(ColorRGBA skyColor) {
		ColorRGBA was = getSkyColor();
		if (!Objects.equals(was, skyColor)) {
			this.skyColour = skyColor;
			firePropertyChange(PROP_SKY_COLOR, was, skyColor);
		}
	}

	public ColorRGBA getFogColor() {
		return fogColour;
	}

	public void setFogColor(ColorRGBA fogColor) {
		ColorRGBA was = getFogColor();
		if (!Objects.equals(was, fogColor)) {
			this.fogColour = fogColor;
			firePropertyChange(PROP_FOG_COLOR, was, fogColor);
		}
	}

	// public List<String> getAmbientMusic() {
	// return ambientMusic;
	// }
	//
	// public void setAmbientMusic(List<String> ambientMusic) {
	// List<String> was = getAmbientMusic();
	// if (!Objects.equals(was, ambientMusic)) {
	// this.ambientMusic = ambientMusic;
	// firePropertyChange(PROP_AMBIENT_MUSIC, was, ambientMusic);
	// }
	// }
	//
	// public List<String> getAmbientNoise() {
	// return ambientNoise;
	// }
	//
	// public void setAmbientNoise(List<String> ambientNoise) {
	// List<String> was = getAmbientNoise();
	// if (!Objects.equals(was, ambientNoise)) {
	// this.ambientNoise = ambientNoise;
	// firePropertyChange(PROP_AMBIENT_NOISE, was, ambientNoise);
	// }
	// }

	public int getAmbientMusicDelay() {
		return ambientMusicDelay;
	}

	public void setAmbientMusicDelay(int ambientMusicDelay) {
		int was = getAmbientMusicDelay();
		if (was != ambientMusicDelay) {
			this.ambientMusicDelay = ambientMusicDelay;
			firePropertyChange(PROP_AMBIENT_MUSIC_DELAY, was, ambientMusicDelay);
		}
	}

	public int getActivateMusicDelay() {
		return activateMusicDelay;
	}

	public void setActivateMusicMusicDelay(int activateMusicDelay) {
		int was = getActivateMusicDelay();
		if (was != activateMusicDelay) {
			this.activateMusicDelay = activateMusicDelay;
			firePropertyChange(PROP_ACTIVATE_MUSIC_DELAY, was, activateMusicDelay);
		}
	}

	// public List<String> getActivateMusic() {
	// return activateMusic;
	// }
	//
	// public void setActivateMusic(List<String> activateMusic) {
	// List<String> was = getActivateMusic();
	// if (!Objects.equals(was, activateMusic)) {
	// this.activateMusic = activateMusic;
	// firePropertyChange(PROP_ACTIVATE_MUSIC, was, activateMusic);
	// }
	// }
	//
	// public List<String> getWeatherMusic() {
	// return weatherMusic;
	// }
	//
	// public void setWeatherMusic(List<String> weatherMusic) {
	// List<String> was = getWeatherMusic();
	// if (!Objects.equals(was, weatherMusic)) {
	// this.weatherMusic = weatherMusic;
	// firePropertyChange(PROP_WEATHER_MUSIC, was, weatherMusic);
	// }
	// }

	public Weather getWeather() {
		return weather;
	}

	public void setWeather(Weather weather) {
		Weather was = getWeather();
		if (!Objects.equals(was, weather)) {
			this.weather = weather;
			firePropertyChange(PROP_WEATHER, was, weather);
		}
	}

	public String getSkyMap() {
		return skyMap;
	}

	public void setSkyMap(String skyMap) {
		String was = getSkyMap();
		if (!Objects.equals(was, skyMap)) {
			this.skyMap = skyMap;
			firePropertyChange(PROP_SKY_MAP, was, skyMap);
		}
	}

	public boolean isSkyMapUseAsMap() {
		return skyMapUseAsMap;
	}

	public void setSkyMapUseAsMap(boolean skyMapUseAsMap) {
		boolean was = isSkyMapUseAsMap();
		if (was != skyMapUseAsMap) {
			this.skyMapUseAsMap = skyMapUseAsMap;
			firePropertyChange(PROP_SKY_MAP_USE_AS_MAP, was, skyMapUseAsMap);
		}
	}

	public WrapMode getSkyMapWrap() {
		return skyMapWrap;
	}

	public void setSkyMapWrap(WrapMode skyMapWrap) {
		WrapMode was = getCelestialBodyWrap();
		if (!Objects.equals(was, skyMapWrap)) {
			this.skyMapWrap = skyMapWrap;
			firePropertyChange(PROP_SKY_MAP_WRAP, was, skyMapWrap);
		}
	}

	public float getSkyMapScale() {
		return skyMapScale;
	}

	public void setSkyMapScale(float skyMapScale) {
		float was = getSkyMapScale();
		if (was != skyMapScale) {
			this.skyMapScale = skyMapScale;
			firePropertyChange(PROP_SKY_MAP_SCALE, was, skyMapScale);
		}
	}

	public String getCelestialBodyMap() {
		return celstialBodyMap;
	}

	public void setCelestialBodyMap(String celestialBodyMap) {
		String was = getCelestialBodyMap();
		if (!Objects.equals(was, celestialBodyMap)) {
			this.celstialBodyMap = celestialBodyMap;
			firePropertyChange(PROP_CELESTIAL_BODY_MAP, was, celestialBodyMap);
		}
	}

	public WrapMode getCelestialBodyWrap() {
		return celstialBodyWrap;
	}

	public void setCelestialBodyWrap(WrapMode celestialBodyWrap) {
		WrapMode was = getCelestialBodyWrap();
		if (!Objects.equals(was, celestialBodyWrap)) {
			this.celstialBodyWrap = celestialBodyWrap;
			firePropertyChange(PROP_CELESTIAL_BODY_WRAP, was, celestialBodyWrap);
		}
	}

	public String getFogAlphaMap() {
		return fogAlphaMap;
	}

	public void setFogAlphaMap(String fogAlphaMap) {
		String was = getFogAlphaMap();
		if (!Objects.equals(was, fogAlphaMap)) {
			this.fogAlphaMap = fogAlphaMap;
			firePropertyChange(PROP_FOG_ALPHA_MAP, was, fogAlphaMap);
		}
	}

	public String getCloudsMap() {
		return cloudsMap;
	}

	public boolean isCloudsMapUseAsMap() {
		return cloudsMapUseAsMap;
	}

	public void setCloudsMapUseAsMap(boolean cloudsMapUseAsMap) {
		boolean was = isCloudsMapUseAsMap();
		if (was != cloudsMapUseAsMap) {
			this.cloudsMapUseAsMap = cloudsMapUseAsMap;
			firePropertyChange(PROP_CLOUDS_MAP_USE_AS_MAP, was, cloudsMapUseAsMap);
		}
	}

	public float getCloudsMapScale() {
		return cloudsMapScale;
	}

	public void setCloudsMapScale(float cloudsMapScale) {
		float was = getCloudsMapScale();
		if (was != cloudsMapScale) {
			this.cloudsMapScale = cloudsMapScale;
			firePropertyChange(PROP_CLOUDS_MAP_SCALE, was, cloudsMapScale);
		}
	}

	public void setCloudsMap(String cloudsMap) {
		String was = getCloudsMap();
		if (!Objects.equals(was, cloudsMap)) {
			this.cloudsMap = cloudsMap;
			firePropertyChange(PROP_CLOUDS_MAP, was, cloudsMap);
		}
	}

	public String absolutize(String path) {
		return "Environment/" + path;
	}

	public String relativize(String newResource) {
		if (newResource.startsWith("Environment/")) {
			return newResource.substring(12);
		} else {
			return newResource;
		}
	}

	@Override
	public Object clone() {
		throw new UnsupportedOperationException();
	}
}

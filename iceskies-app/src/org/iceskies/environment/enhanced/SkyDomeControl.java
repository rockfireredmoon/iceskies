package org.iceskies.environment.enhanced;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.icelib.Icelib;
import org.icescene.SceneConstants;
import org.icescene.environment.EnvironmentLight;
import org.iceskies.app.SkiesConstants;
import org.iceui.IceUI;

import com.jme3.asset.AssetManager;
import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.material.RenderState.FaceCullMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.post.filters.FogFilter;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.Control;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.MagFilter;
import com.jme3.texture.Texture.MinFilter;
import com.jme3.texture.Texture.WrapMode;

/**
 * @author t0neg0d
 *
 * @author Emerald Icemoon - Rewrote a bunch of stuff. I only wanted one state,
 *         with the colours being changed externally. This control just looks
 *         after the transitions from the old state to the new state.
 */
public class SkyDomeControl implements Control {

	private final static Logger LOG = Logger.getLogger(SkyDomeControl.class.getName());
	private ViewPort viewPort;
	private Spatial spatial;
	private AssetManager assetManager;
	private Node skyNight;
	private Camera cam;
	private boolean enabled = true;
	private FogFilter fog = null;
	private ColorRGBA directionalLight = ColorRGBA.Gray;
	private ColorRGBA ambientLight = ColorRGBA.Gray;
	private boolean controlFog = false;
	private String model, skyMap, celestialBodyMap, cloudsMap, fogAlphaMap;
	private boolean cycleCI = false, cycleCO = false;
	private float cloudMaxAlpha = 1f, cloudMinAlpha = 0f, cloudsAlpha = 1;
	private float cloudCycleSpeed = .125f;
	private float cloud1Rotation = FastMath.HALF_PI + 0.02f;
	private float cloud1Speed = .025f;
	private float cloud2Rotation = FastMath.HALF_PI + 0.023f;
	private float cloud2Speed = .05f;
	private float celestialBodyRotation = 75f;
	private float celestialBodySpeed = .0185f;
	private ColorRGBA fogColor = new ColorRGBA(0.7f, 0.7f, 0.7f, 0.6f);
	private ColorRGBA skyColor = new ColorRGBA(.7f, .7f, 1.0f, 1.0f);
	private Texture tex_Sky, tex_CelestialBody, tex_FogAlpha, tex_Clouds;
	private Material mat_Sky;
	private ColorRGBA targetSkyColor;
	private ColorRGBA targetFogColor;
	private float targetSkyMapAlpha = Float.MIN_VALUE;
	private float targetCelestialBodyAlpha = Float.MIN_VALUE;
	private EnvironmentLight environmentLight;
	private float duration;
	private float transitionProgress;
	private Vector3f directionalStart;
	private float startDirectionalAngle = 0;
	private float endDirectionalAngle = FastMath.PI;
	private boolean resetAfterHalfCircle = true;
	private float directionalPositionUpdateTimer;
	private boolean controlDirectionalPosition;
	private ColorRGBA currentFog;
	private float celestialBodyAlpha = 1.0f;
	private float skyMapAlpha;
	private float currentSkyMapAlpha = Float.MIN_VALUE;
	private boolean directionalEnabled;
	private boolean ambientEnabled;
	private ColorRGBA curentSkyColor;
	private float cycleSpeed;
	private ColorRGBA startDirectionalLight;
	private ColorRGBA startAmbientLight;
	private float directionalProgress;
	private float celestialBodyScale = 1f;
	private boolean cloudsMapUseAsMap;
	private float cloudsMapScale = 1f;
	private boolean skyMapUseAsMap;
	private float skyMapScale = 1f;
	private WrapMode skyMapWrap = WrapMode.Repeat;;
	private WrapMode celestialBodyWrap = WrapMode.BorderClamp;

	/**
	 * Creates a new SkyDome control
	 *
	 * @param assetManager
	 *            A pointer to the JME application AssetManager
	 * @param cam
	 *            A pointer to the default Camera of the JME application
	 * @param model
	 *            j3o to use as the Sky Dome
	 * @param nightSkyMap
	 *            The string value of the texture asset for night time sky
	 * @param moonMap
	 *            The string value of the texture asset for the moon. This is
	 *            the only param that accepts null
	 * @param cloudsMap
	 *            The string value of the texture asset for the clouds
	 * @param fogAlphaMap
	 *            The string value of the texture asset for the blending alpha
	 *            map for fog coloring
	 */
	public SkyDomeControl(AssetManager assetManager, Camera cam, String model, String skyMap, String celestialBodyMap,
			String cloudsMap, String fogAlphaMap) {
		this.assetManager = assetManager;
		this.cam = cam;

		this.model = model;
		this.skyMap = skyMap;
		this.celestialBodyMap = celestialBodyMap;
		this.cloudsMap = cloudsMap;
		this.fogAlphaMap = fogAlphaMap;

		tex_FogAlpha = assetManager.loadTexture(fogAlphaMap);
		tex_FogAlpha.setMinFilter(MinFilter.NearestNoMipMaps);
		tex_FogAlpha.setMagFilter(MagFilter.Nearest);
		tex_FogAlpha.setWrap(WrapMode.Repeat);

		tex_Sky = assetManager.loadTexture(skyMap);
		tex_Sky.setMinFilter(MinFilter.BilinearNearestMipMap);
		tex_Sky.setMagFilter(MagFilter.Bilinear);
		tex_Sky.setWrap(skyMapWrap);

		if (celestialBodyMap != null) {
			tex_CelestialBody = assetManager.loadTexture(celestialBodyMap);
			tex_CelestialBody.setMinFilter(MinFilter.BilinearNearestMipMap);
			tex_CelestialBody.setMagFilter(MagFilter.Bilinear);
			tex_CelestialBody.setWrap(celestialBodyWrap);
		}

		tex_Clouds = assetManager.loadTexture(cloudsMap);
		tex_Clouds.setMinFilter(MinFilter.BilinearNoMipMaps);
		tex_Clouds.setMagFilter(MagFilter.Bilinear);
		tex_Clouds.setWrap(WrapMode.Repeat);

		mat_Sky = new Material(assetManager, "MatDefs/SkyDome/SkyDome2.j3md");
		mat_Sky.setTexture("SkyMap", tex_Sky);
		if (celestialBodyMap != null) {
			mat_Sky.setTexture("CelestialBodyMap", tex_CelestialBody);
			mat_Sky.setFloat("CelestialBodyDirection", celestialBodyRotation);
			mat_Sky.setFloat("CelestialBodySpeed", celestialBodySpeed);
			mat_Sky.setFloat("CelestialBodyAlpha", celestialBodyAlpha);
			mat_Sky.setFloat("CelestialBodyScale", celestialBodyScale);
		}
		mat_Sky.setFloat("SkyMapAlpha", skyMapAlpha);
		mat_Sky.setFloat("SkyMapScale", skyMapScale);
		mat_Sky.setBoolean("SkyMapUseAsMap", skyMapUseAsMap);
		mat_Sky.setColor("Color", skyColor);
		mat_Sky.setTexture("FogAlphaMap", tex_FogAlpha);
		mat_Sky.setTexture("CloudMap1", tex_Clouds);
		mat_Sky.setFloat("CloudMap1Scale", cloudsMapScale);
		mat_Sky.setBoolean("CloudMap1UseAsMap", cloudsMapUseAsMap);
		mat_Sky.setFloat("CloudDirection1", cloud1Rotation);
		mat_Sky.setFloat("CloudSpeed1", cloud1Speed);
		mat_Sky.setFloat("CloudDirection2", cloud2Rotation);
		mat_Sky.setFloat("CloudSpeed2", cloud2Speed);
		mat_Sky.setFloat("CloudsAlpha", cloudsAlpha);
		mat_Sky.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Off);
		mat_Sky.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
		mat_Sky.getAdditionalRenderState().setDepthWrite(false);

		skyNight = (Node) assetManager.loadModel(model);
		skyNight.setCullHint(Spatial.CullHint.Never);
		skyNight.setLocalScale(5f, 5f, 5f);
		skyNight.setMaterial(mat_Sky);
	}

	public boolean isResetAfterHalfCircle() {
		return resetAfterHalfCircle;
	}

	public void setResetAfterHalfCircle(boolean resetAfterHalfCircle) {
		this.resetAfterHalfCircle = resetAfterHalfCircle;
	}

	public void setDirectionalStartPosition(Vector3f directionalStart) {
		this.directionalStart = directionalStart.clone();
		directionalProgress = 0f;
	}

	public void setLight(EnvironmentLight environmentLight) {
		this.environmentLight = environmentLight;
		directionalLight = environmentLight.getSunColor();
		ambientLight = environmentLight.getAmbientColor();
		directionalEnabled = environmentLight.isDirectionalEnabled();
		ambientEnabled = environmentLight.isAmbientEnabled();
		transitionProgress = 0;
		LOG.info("Light has been set on the skydome");
		LOG.info(String.format("    Directional: %s     (enabled = %s", directionalLight, directionalEnabled));
		LOG.info(String.format("    Ambient: %s     (enabled = %s", ambientLight, ambientEnabled));
	}

	public float getCelestialBodyAlpha() {
		return targetSkyMapAlpha == Float.MIN_VALUE ? celestialBodyAlpha : targetSkyMapAlpha;
	}

	public void setCelestialBodyAlpha(float celestialBodyAlpha) {
		this.targetCelestialBodyAlpha = celestialBodyAlpha;
		this.transitionProgress = 0f;
	}

	public void setSkyMapAlpha(float skyMapAlpha) {
		this.targetSkyMapAlpha = skyMapAlpha;
		this.transitionProgress = 0f;
	}

	public void setDuration(float duration) {
		this.duration = duration;
		directionalProgress = 0f;
	}

	public void setSpatial(Spatial spatial) {
		this.spatial = spatial;
		((Node) spatial).attachChild(skyNight);
	}

	/**
	 * Enable the SkyDome control
	 *
	 * @param enabled
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	/**
	 * Returns if the SkyDome control is enabled
	 *
	 * @return enabled
	 */
	public boolean isEnabled() {
		return this.enabled;
	}

	// Transitions
	public boolean getIsClouds() {
		return cloudsAlpha > cloudMinAlpha;
	}

	/**
	 * Sets the speed at which the transition between colours and alphas happens
	 *
	 * @param cycleSpeed
	 *            Default value is .125f
	 */
	public void setTransitionSpeed(float cycleSpeed) {
		this.cycleSpeed = cycleSpeed;
		transitionProgress = 0;
	}

	/**
	 * Gets the speed at which the transition between colours and alphas happens
	 *
	 * @return cycleSpeed Default value is .125f
	 */
	public float getTransitionSpeed() {
		return this.cycleSpeed;
	}

	// Fog
	/**
	 * Sets a pointer to the fog filter used by the JME application that
	 * initialized the SkyDome control
	 *
	 * @param fog
	 *            The FogFilter to adjust during transitions
	 * @param viewPort
	 *            The default ViewPort for background color manipulation used
	 *            for fog blending
	 */
	public void setFogFilter(FogFilter fog, ViewPort viewPort) {
		this.fog = fog;
		this.viewPort = viewPort;
	}

	/**
	 * Sets the color to use
	 *
	 * @param fogColor
	 *            Default value is 0.7f, 0.7f, 0.7f, 0.6f
	 */
	public void setFogColor(ColorRGBA fogColor) {
		this.targetFogColor = fogColor.clone();
		this.transitionProgress = 0f;
	}

	/**
	 * Gets the fog color
	 *
	 * @return fogColor Default value is 0.7f, 0.7f, 0.7f, 0.6f
	 */
	public ColorRGBA getFogColor() {
		return this.fogColor;
	}

	/**
	 * Enable SkyDome to control the JME application FogFilter
	 *
	 * @param controlFog
	 *            Default value is false
	 */
	public void setControlFog(boolean controlFog) {
		this.controlFog = controlFog;
	}

	/**
	 * Returns if SkyDome controls the JME application FogFilter
	 *
	 * @return controlFog Default value is false
	 */
	public boolean getControlFog() {
		return this.controlFog;
	}

	/**
	 * Sets the color used for the directional light
	 *
	 * @param directionalLight
	 *            Default value is 1f, 1f, 1f, 1f
	 */
	public void setDirectionalLight(ColorRGBA directionalLight) {
		if (startDirectionalLight == null) {
			startDirectionalLight = environmentLight.getSunColor();
		}
		this.directionalLight = directionalLight.clone();
		this.transitionProgress = 0f;
	}

	/**
	 * Sets the color used by the ambient light during day time
	 *
	 * @param ambientDayLight
	 *            Default value is 1f, 1f, 1f, 1f
	 */
	public void setAmbientLight(ColorRGBA ambientLight) {
		if (startAmbientLight == null) {
			startAmbientLight = environmentLight.getAmbientColor();
		}
		this.ambientLight = ambientLight.clone();
		this.transitionProgress = 0f;
	}

	/**
	 * Enable SkyDome to control the position of the JME application
	 * DirectionLight
	 *
	 * @param controlDirectionalPosition
	 *            Default value is false
	 */
	public void setControlDirectionalPosition(boolean controlDirectionalPosition) {
		this.controlDirectionalPosition = controlDirectionalPosition;
		directionalProgress = 0;
	}

	// Day time color
	/**
	 * Sets the color used for the sky
	 *
	 * @param skyColor
	 *            Default value is .7f, .7f, 1f, 1f
	 */
	public void setSkyColor(ColorRGBA skyColor) {
		this.targetSkyColor = skyColor.clone();
		this.transitionProgress = 0f;
	}

	/**
	 * Gets the color used for day time sky
	 *
	 * @return dayColor Default value is .7f, .7f, 1f, 1f
	 */
	public ColorRGBA getSkyColor() {
		return targetSkyColor == null ? this.skyColor : targetSkyColor;
	}

	// Moon
	/**
	 * Sets the rotation/direction the celestial body moves in
	 *
	 * @param celestialBodyRotation
	 *            Default value 75f
	 */
	public void setCelestialBodyDirection(float celestialBodyRotation) {
		this.celestialBodyRotation = celestialBodyRotation;
		if (mat_Sky != null) {
			mat_Sky.setFloat("CelestialBodyDirection", celestialBodyRotation);
		}
	}

	/**
	 * Gets the rotation/direction the celestial body moves in
	 *
	 * @return celestialBodyRotation Default value 75f
	 */
	public float getCelestialBodyRotation() {
		return this.celestialBodyRotation;
	}

	/**
	 * Sets the speed the celestial body moves
	 *
	 * @param celestialBodySpeed
	 *            Default value .0185f
	 */
	public void setCelestialBodySpeed(float celestialBodySpeed) {
		this.celestialBodySpeed = celestialBodySpeed;
		if (mat_Sky != null) {
			mat_Sky.setFloat("CelestialBodySpeed", celestialBodySpeed);
		}
	}

	/**
	 * Gets the speed the moon moves
	 *
	 * @return celestialBodySpeed Default value .0185f
	 */
	public float getCelestialBodySpeed() {
		return this.celestialBodySpeed;
	}

	public void setCelestialBodyScale(float celestialBodyScale) {
		this.celestialBodyScale = celestialBodyScale;
		if (mat_Sky != null) {
			mat_Sky.setFloat("CelestialBodyScale", celestialBodyScale);
		}
	}
	
	public WrapMode getCelestialBodyWrap() {
		return celestialBodyWrap;
	}

	public void setCelestialBodyWrap(WrapMode celestialBodyWrap) {
		this.celestialBodyWrap = celestialBodyWrap;
		if (tex_CelestialBody != null) {
			tex_CelestialBody.setWrap(celestialBodyWrap);
		}
	}
	
	public WrapMode getSkyMapWrap() {
		return skyMapWrap;
	}

	public void setSkyMapWrap(WrapMode skyMapWrap) {
		this.skyMapWrap = skyMapWrap;
		if (tex_Sky != null) {
			tex_Sky.setWrap(skyMapWrap);
		}
	}

	public float getSkyMapScale() {
		return skyMapScale;
	}

	public void setSkyMapScale(float skyMapScale) {
		this.skyMapScale = skyMapScale;
		if (mat_Sky != null) {
			mat_Sky.setFloat("SkyMapScale", skyMapScale);
		}
	}

	public float getCloudsMapScale() {
		return cloudsMapScale;
	}

	public void setCloudsMapScale(float cloudsMapScale) {
		this.cloudsMapScale = cloudsMapScale;
		if (mat_Sky != null) {
			mat_Sky.setFloat("CloudMap1Scale", cloudsMapScale);
		}
	}

	public boolean isCloudsMapUseAsMap() {
		return cloudsMapUseAsMap;
	}

	public void setCloudsMapUseAsMap(boolean cloudsMapUseAsMap) {
		this.cloudsMapUseAsMap = cloudsMapUseAsMap;
		if (mat_Sky != null) {
			mat_Sky.setBoolean("CloudMap1UseAsMap", cloudsMapUseAsMap);
		}
	}

	public boolean isSkyMapUseAsMap() {
		return skyMapUseAsMap;
	}

	public void setSkyMapUseAsMap(boolean skyMapUseAsMap) {
		this.skyMapUseAsMap = skyMapUseAsMap;
		if (mat_Sky != null) {
			mat_Sky.setBoolean("SkyMapUseAsMap", skyMapUseAsMap);
		}
	}

	public float getCelestialBodyScale() {
		return celestialBodyScale;
	}

	// Clouds
	/**
	 * Sets the near cloud layer movement rotation/direction
	 *
	 * @param cloudRotation
	 *            Default value FastMath.HALF_PI+0.02f
	 */
	public void setCloudsNearRotation(float cloudRotation) {
		this.cloud2Rotation = cloudRotation;
		if (mat_Sky != null) {
			mat_Sky.setFloat("CloudDirection2", cloudRotation);
		}
	}

	/**
	 * Gets the near cloud layer movement rotation/direction
	 *
	 * @return cloud2Rotation Default value FastMath.HALF_PI+0.02f
	 */
	public float getCloudsNearRotation() {
		return this.cloud2Rotation;
	}

	/**
	 * Sets the near cloud layer movement speed
	 *
	 * @param cloudSpeed
	 *            Default value .05f
	 */
	public void setCloudsNearSpeed(float cloudSpeed) {
		this.cloud2Speed = cloudSpeed;
		if (mat_Sky != null) {
			mat_Sky.setFloat("CloudSpeed2", cloudSpeed);
		}
	}

	/**
	 * Gets the near cloud layer movement speed
	 *
	 * @param cloud2Speed
	 *            Default value .05f
	 */
	public float getCloudsNearSpeed() {
		return this.cloud2Speed;
	}

	/**
	 * Sets the far cloud layer movement rotation/direction
	 *
	 * @param cloudRotation
	 *            Default value FastMath.HALF_PI+0.023f
	 */
	public void setCloudsFarRotation(float cloudRotation) {
		this.cloud1Rotation = cloudRotation;
		if (mat_Sky != null) {
			mat_Sky.setFloat("CloudDirection1", cloudRotation);
		}
	}

	/**
	 * Gets the near cloud layer movement rotation/direction
	 *
	 * @return cloud1Rotation Default value FastMath.HALF_PI+0.02f
	 */
	public float getCloudsFarRotation() {
		return this.cloud1Rotation;
	}

	/**
	 * Sets the far cloud layer movement speed
	 *
	 * @param cloudSpeed
	 *            Default value .025f
	 */
	public void setCloudsFarSpeed(float cloudSpeed) {
		this.cloud1Speed = cloudSpeed;
		if (mat_Sky != null) {
			mat_Sky.setFloat("CloudSpeed1", cloudSpeed);
		}
	}

	/**
	 * Gets the far cloud layer movement speed
	 *
	 * @return cloud1Speed Default value .025f
	 */
	public float getCloudsFarSpeed() {
		return this.cloud1Speed;
	}

	/**
	 * Sets the near and far cloud layers maximum opacity for cycling clouds
	 * in/out
	 *
	 * @param cloudMaxOpacity
	 *            Default value 1f
	 */
	public void setCloudMaxOpacity(float cloudMaxOpacity) {
		this.cloudMaxAlpha = cloudMaxOpacity;
	}

	/**
	 * Gets the near and far cloud layers maximum opacity for cycling clouds
	 * in/out
	 *
	 * @return cloudMaxOpacity Default value 1f
	 */
	public float getCloudMaxOpacity() {
		return this.cloudMaxAlpha;
	}

	/**
	 * Sets the near and far cloud layers minimum opacity for cycling clouds
	 * in/out
	 *
	 * @param cloudMinOpacity
	 *            Default value 0f
	 */
	public void setCloudMinOpacity(float cloudMinOpacity) {
		this.cloudMinAlpha = cloudMinOpacity;
	}

	/**
	 * Gets the near and far cloud layers minimum opacity for cycling clouds
	 * in/out
	 *
	 * @return cloudMinOpacity Default value 0f
	 */
	public float getCloudMinOpacity() {
		return this.cloudMinAlpha;
	}

	/**
	 * Sets the speed at which the near and far cloud layers are cycled in/out
	 *
	 * @param cloudCycleSpeed
	 *            Default value .125f
	 */
	public void setCloudCycleSpeed(float cloudCycleSpeed) {
		this.cloudCycleSpeed = cloudCycleSpeed;
	}

	/**
	 * Gets the speed at which the near and far cloud layers are cycled in/out
	 *
	 * @return cloudCycleSpeed Default value .125f
	 */
	public float getCloudCycleSpeed() {
		return this.cloudCycleSpeed;
	}

	// Color mix function
	/**
	 * Blends two ColorRGBAs by the amount passed in
	 *
	 * @param c1
	 *            The color being blended into
	 * @param c2
	 *            The color to blend
	 * @param amount
	 *            The amount of c2 to blend into c1
	 * @return r The resulting ColorRGBA
	 */
	private ColorRGBA mix(ColorRGBA c1, ColorRGBA c2, float amount) {
		ColorRGBA r = new ColorRGBA();
		r.interpolate(c1, c2, amount);
		return r;
	}

	/**
	 * Begin cycle clouds in
	 */
	public void cycleCloudsIn() {
		this.cycleCI = true;
		this.cycleCO = false;
	}

	/**
	 * Begin cycle clouds out
	 */
	public void cycleCloudsOut() {
		this.cycleCI = false;
		this.cycleCO = true;
	}

	public void setClouds(boolean clouds) {
		if (clouds) {
			this.cycleCI = true;
			this.cycleCO = false;
			this.cloudsAlpha = cloudMaxAlpha;
		} else {
			this.cycleCI = false;
			this.cycleCO = true;
			this.cloudsAlpha = cloudMinAlpha;
		}
	}

	public void update(float tpf) {
		if (spatial != null && enabled) {

			// Directional light position update
			directionalProgress = Math.min(duration, directionalProgress + tpf);
			directionalPositionUpdateTimer += tpf;
			if (controlDirectionalPosition && environmentLight != null
					&& directionalPositionUpdateTimer > SkiesConstants.SUN_POSITION_UPDATE_INTERVAL) {
				updateSunPosition(directionalProgress / duration);
			}

			// Position the sky geometry
			Vector3f camLoc = cam.getLocation();
			float[] camLF = camLoc.toArray(null);
			spatial.setLocalTranslation(camLF[0], camLF[1] + .25f, camLF[2]);

			// Transitions
			transitionProgress += tpf * cycleSpeed;
			transitionProgress = Math.min(transitionProgress, 1);
			transitions(transitionProgress);

			// Cloud cycle
			cloudCycle(tpf);
		}
	}

	protected void setSunColor(ColorRGBA color) {
		environmentLight.setDirectionalColor(color);
	}

	protected void setAmbientColor(ColorRGBA color) {
		environmentLight.setAmbientColor(color);
	}

	public void render(RenderManager rm, ViewPort vp) {
	}

	public Control cloneForSpatial(Spatial spatial) {
		SkyDomeControl control = new SkyDomeControl(this.assetManager, this.cam, this.model, this.skyMap, this.celestialBodyMap,
				this.cloudsMap, this.fogAlphaMap);
		control.spatial.addControl(control);
		return control;
	}

	public void write(JmeExporter ex) throws IOException {
		OutputCapsule oc = ex.getCapsule(this);
		oc.write(enabled, "enabled", true);
		oc.write(spatial, "spatial", null);
	}

	public void read(JmeImporter im) throws IOException {
		InputCapsule ic = im.getCapsule(this);
		enabled = ic.readBoolean("enabled", true);
		spatial = (Spatial) ic.readSavable("spatial", null);
	}

	public void setStartDirectionalAngle(float f) {
		this.startDirectionalAngle = f;
		directionalProgress = 0;
	}

	public void setEndDirectionalAngle(float f) {
		this.endDirectionalAngle = f;
		directionalProgress = 0;
	}

	public void setDirectionalEnabled(boolean directionalEnabled) {
		if (directionalEnabled != this.directionalEnabled) {
			this.directionalEnabled = directionalEnabled;
			this.transitionProgress = 0f;
			if (startDirectionalLight == null) {
				startDirectionalLight = environmentLight.getSunColor();
			}
		}
	}

	public void setAmbientEnabled(boolean ambientEnabled) {
		if (ambientEnabled != this.ambientEnabled) {
			this.ambientEnabled = ambientEnabled;
			this.transitionProgress = 0f;
			if (startAmbientLight == null) {
				startAmbientLight = environmentLight.getAmbientColor();
			}
		}
	}

	public void completeTransition() {
		transitionProgress = 1f;
	}

	private ColorRGBA getTargetDirectionalColor() {
		return directionalEnabled ? directionalLight : ColorRGBA.Black;
	}

	private ColorRGBA getTargetAmbientColor() {
		return ambientEnabled ? ambientLight : ColorRGBA.Black;
	}

	private void transitions(float progress) {

		if (targetSkyMapAlpha != Float.MIN_VALUE) {
			transitionSkyMapAlpha(progress);
		}

		if (targetCelestialBodyAlpha != Float.MIN_VALUE) {
			transitionCelestialBodyAlpha(progress);
		}

		// Day fog -> Day fog
		if (targetFogColor != null) {
			transitionFogColor(progress);
		}

		// Ambient day -> Ambient day
		if (startAmbientLight != null) {
			transitionAmbientChange(progress);
		}

		// Sun day -> Sun day
		if (startDirectionalLight != null) {
			transitionDirectionalChange(progress);
		}

		// Sky color
		if (targetSkyColor != null) {
			transitionSkyColor(progress);
		}
	}

	private void cloudCycle(float tpf) {
		// Clouds Cycle
		if (cycleCI) {
			if (cloudsAlpha < cloudMaxAlpha) {
				cloudsAlpha += tpf * cloudCycleSpeed;
				mat_Sky.setFloat("CloudsAlpha", cloudsAlpha);
			} else {
				cloudsAlpha = cloudMaxAlpha;
				mat_Sky.setFloat("CloudsAlpha", cloudsAlpha);
				cycleCI = false;
			}
		} else if (cycleCO) {
			if (cloudsAlpha > cloudMinAlpha) {
				cloudsAlpha -= tpf * cloudCycleSpeed;
				mat_Sky.setFloat("CloudsAlpha", cloudsAlpha);
			} else {
				cloudsAlpha = cloudMinAlpha;
				mat_Sky.setFloat("CloudsAlpha", cloudsAlpha);
				cycleCO = false;
			}
		}
	}

	private void updateSunPosition(float progress) {
		// The actual sun starting position
		Vector3f actualPos = directionalStart;

		// Rotate around 0,0,0 - maybe make this the camera position
		Vector3f origin = cam.getLocation();

		// Let's have the sun rise in the east and set in the west
		actualPos.x = Math.abs(SceneConstants.DIRECTIONAL_LIGHT_SOURCE_DISTANCE) * -1;
		actualPos.y = 0;
		actualPos.z = 0;

		// Rotate between these angles
		float maxAngle = endDirectionalAngle - startDirectionalAngle;

		// Rotate the sun. The sun moves from horizon to horizon once
		// LOG.info("Sun progress " + factorOfProgress + " max angle: " +
		// maxAngle + " progress angle; " + progressAngle + " origin: " + origin
		// + " lightOnHorizon: " + lightOnHorizon + " st: " + directionalStart +
		// " horizon: " + horizon + " sun:" + sunDir );

		// Work out actual angle. This is PI - (angle) as we want to rise in the
		// east
		final float progressAngle = (((maxAngle * (progress)) + startDirectionalAngle));

		Quaternion roll = new Quaternion();
		roll.fromAngleAxis(progressAngle * -1, Vector3f.UNIT_Z);
		Vector3f newPos = roll.toRotationMatrix().mult(actualPos).addLocal(origin);

		environmentLight.setSunToLocation(newPos);
	}

	private void transitionSkyMapAlpha(float progress) {
		float n;
		if (Icelib.close(targetSkyMapAlpha, skyMapAlpha)) {
			if (LOG.isLoggable(Level.FINE)) {
				LOG.fine(String.format("Completed sky map  alpha transition to %s", targetCelestialBodyAlpha));
			}
			n = skyMapAlpha = targetSkyMapAlpha;
			targetSkyMapAlpha = Float.MIN_VALUE;
		} else {
			float amt = progress;
			n = (1 - amt) * skyMapAlpha + amt * targetSkyMapAlpha;
		}
		mat_Sky.setFloat("SkyMapAlpha", n);
	}

	private void transitionCelestialBodyAlpha(float progress) {
		float n;
		if (Icelib.close(targetCelestialBodyAlpha, celestialBodyAlpha)) {
			if (LOG.isLoggable(Level.FINE)) {
				LOG.fine(String.format("Completed celestial body alpha transition to %s", targetCelestialBodyAlpha));
			}
			n = celestialBodyAlpha = targetCelestialBodyAlpha;
			targetCelestialBodyAlpha = Float.MIN_VALUE;
		} else {
			float amt = progress;
			n = (1 - amt) * celestialBodyAlpha + amt * targetCelestialBodyAlpha;
		}
		mat_Sky.setFloat("CelestialBodyAlpha", n);
	}

	private void transitionFogColor(float progress) {
		if (IceUI.colorClose(currentFog, targetFogColor)) {
			if (LOG.isLoggable(Level.FINE)) {
				LOG.fine(String.format("Completed fog colour transition to %s", targetFogColor));
			}
			currentFog = fogColor = targetFogColor;
			targetFogColor = null;
		} else {
			currentFog = fogColor.clone();
			currentFog.interpolate(targetFogColor, progress);
			if (LOG.isLoggable(Level.FINE)) {
				LOG.fine(String.format("Setting fog color to %s", currentFog));
			}
		}
		mat_Sky.setColor("FogColor", currentFog);
		if (controlFog && currentFog != null) {
			fog.setFogColor(currentFog);
		}
		if (viewPort != null) {
			viewPort.setBackgroundColor(currentFog);
		}
	}

	private void transitionAmbientChange(float progress) {
		ColorRGBA target = getTargetAmbientColor();
		ColorRGBA current = startAmbientLight.clone();
		if (IceUI.colorClose(environmentLight.getAmbientColor(), target)) {
			if (LOG.isLoggable(Level.FINE)) {
				LOG.fine(String.format("Completed ambient light colour transition to %s from %s for progress %4.3f", target,
						startAmbientLight, progress));
			}
			startAmbientLight = null;
			current = target;
		} else {
			current.interpolate(target, progress);
			if (LOG.isLoggable(Level.FINE)) {
				LOG.fine(String.format("Setting ambient light color to %s from %s for progress %4.3f", current, startAmbientLight,
						progress));
			}
		}
		setAmbientColor(current);
	}

	private void transitionDirectionalChange(float progress) {
		ColorRGBA target = getTargetDirectionalColor();
		ColorRGBA current = startDirectionalLight.clone();
		if (IceUI.colorClose(environmentLight.getSunColor(), target)) {
			if (LOG.isLoggable(Level.FINE)) {
				LOG.fine(String.format("Completed directional light colour transition to %s from %s for progress %4.3f", target,
						startDirectionalLight, progress));
			}
			startDirectionalLight = null;
			current = target;
		} else {
			current.interpolate(target, progress);
			if (LOG.isLoggable(Level.FINE)) {
				LOG.fine(String.format("Setting directional light color to %s from %s for progress %4.3f", current,
						startDirectionalLight, progress));
			}
		}
		setSunColor(current);
	}

	private void transitionSkyColor(float progress) {
		if (IceUI.colorClose(curentSkyColor, targetSkyColor)) {
			if (LOG.isLoggable(Level.FINE)) {
				LOG.info(String.format("Completed sky colour transition to %s", targetSkyColor));
			}
			curentSkyColor = skyColor = targetSkyColor;
			targetSkyColor = null;
		} else {
			curentSkyColor = skyColor.clone();
			curentSkyColor.interpolate(targetSkyColor, progress);
			if (LOG.isLoggable(Level.FINE)) {
				LOG.info(String.format("Setting sky color to %s", curentSkyColor));
			}
		}
		mat_Sky.setColor("Color", curentSkyColor);
	}
}

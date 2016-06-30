package org.iceskies.environment.legacy;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.prefs.PreferenceChangeEvent;

import org.icescene.IcesceneApp;
import org.icescene.SceneConstants;
import org.icescene.audio.AudioAppState;
import org.icescene.environment.EnvironmentLight;
import org.icescene.environment.PostProcessAppState;
import org.icescene.environment.PostProcessAppState.FogFilterMode;
import org.icescene.fog.FogFilter;
import org.icescene.fog.FogFilter.FogMode;
import org.iceskies.app.SkiesConstants;
import org.iceskies.environment.AbstractEnvironmentConfiguration;
import org.iceskies.environment.AbstractEnvironmentImpl;

import com.google.common.base.Objects;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.asset.ModelKey;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.Camera;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.texture.Texture.WrapMode;

public class LegacyEnvironment extends AbstractEnvironmentImpl implements PropertyChangeListener {
	private static final float DEFAULT_FOG_START = 0.2f;
	private static final float DEFAULT_FOG_END = 0.7f;
	private final static Logger LOG = Logger.getLogger(LegacyEnvironment.class.getName());

	private LegacyEnvironmentConfiguration environmentConfiguration;
	private Node skyNode;
	private boolean followCamera = true;
	private float blendProgress = -1f;
	private List<String> skyMaterials;
	private LegacyFogConfig fog;
	private PostProcessAppState pp;
	private List<Interpolator<?>> interpolators = new ArrayList<LegacyEnvironment.Interpolator<?>>();
	private FogFilterMode oldFogFilterMode;

	public LegacyEnvironment(IcesceneApp app, AudioAppState audio, Camera camera, AppStateManager stateManager,
			AssetManager assetManager, EnvironmentLight environmentLight, Node gameNode) {
		super(app, audio, camera, stateManager, assetManager, environmentLight, gameNode);
		PostProcessAppState pp = stateManager.getState(PostProcessAppState.class);
		oldFogFilterMode = pp == null ? null : pp.getFogFilterMode();
	}

	@Override
	public void update(float tpf) {
		checkNotCleanedUp();
		if (followCamera && skyNode != null) {
			skyNode.setLocalTranslation(camera.getLocation().x, SkiesConstants.DOME_Y_OFFSET, camera.getLocation().z);
		}
		if (blendProgress > -1) {
			blendProgress += tpf;
			updateBlending();
		}
	}

	@Override
	public void onEnvironmentCleanup() {
		if (environmentConfiguration != null)
			environmentConfiguration.removePropertyChangeListener(this);
		if (this.environmentConfiguration != null) {
			updateAudio(null);
		}
		if (skyNode != null) {
			LOG.info("Removing sky node");
			skyNode.removeFromParent();
		}
		if (pp != null) {
			FogFilter fogFilter = (FogFilter) pp.getFogFilter();
			if (fogFilter != null)
				fogFilter.setEnabled(false);
			pp.setFogFilterMode(oldFogFilterMode);
		}
	}

	@Override
	public void handlePrefUpdateSceneThread(PreferenceChangeEvent evt) {
	}

	@Override
	public void setEnvironment(AbstractEnvironmentConfiguration environmentConfiguration) {
		checkNotCleanedUp();
		pp = stateManager.getState(PostProcessAppState.class);
		if (!Objects.equal(this.environmentConfiguration, environmentConfiguration)) {
			LOG.info("Environment configurations change (" + environmentConfiguration + ")");
			if (this.environmentConfiguration != null)
				this.environmentConfiguration.removePropertyChangeListener(this);
			this.environmentConfiguration = (LegacyEnvironmentConfiguration) environmentConfiguration;
			environmentConfiguration.addPropertyChangeListener(this);
			updateAll();
		}
	}

	@Override
	public void setFollowCamera(boolean followCamera) {
		this.followCamera = followCamera;
		if (!followCamera && skyNode != null) {
			skyNode.setLocalTranslation(0, SkiesConstants.DOME_Y_OFFSET, 0);
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		checkNotCleanedUp();
		updateAll();
	}

	private void updateBlending() {
		float fac = Math.min(1.0f, blendProgress / environmentConfiguration.getBlendTime());
		for (Interpolator<?> p : interpolators) {
			p.interpolate(fac);
		}
		if (fac >= 1) {
			LOG.info("Interpolators done");
			interpolators.clear();
			blendProgress = -1;
		}
	}

	private void updateAll() {
		pp = stateManager.getState(PostProcessAppState.class);
		// updateLighting = -1;

		/* Some interpolators should not just be removed, as they may need to person some final operation
		 * such as removing a skynode. 
		 */
		for (Interpolator<?> ip : interpolators) {
			ip.abort();
		}

		interpolators.clear();

		// Light
		if (environmentLight != null) {

			/*
			 * Ambient light. If none is provided by this configuration, search
			 * up through any parents until one is found
			 */
			ColorRGBA ambient = null;
			LegacyEnvironmentConfiguration parent = environmentConfiguration;
			while (ambient == null && parent != null) {
				ambient = parent.getAmbient();
				parent = parent.getDelegate();
			}

			ColorRGBA currentAmbient = environmentLight.isAmbientEnabled() ? environmentLight.getAmbientColor().clone()
					: ColorRGBA.Black;
			if (!Objects.equal(currentAmbient, ambient)) {
				LOG.info(String.format("Ambient changed to %s, interpolating current lighting towards this to value", ambient));
				interpolators.add(new AmbientLightInterpolator(currentAmbient, ambient, environmentLight));
			}

			/*
			 * Directional light. If none is provided by this configuration,
			 * search up through any parents until one is found
			 */

			ColorRGBA directional = null;
			parent = environmentConfiguration;
			while (directional == null && parent != null) {
				directional = parent.getSun();
				parent = parent.getDelegate();
			}

			ColorRGBA currentDirectional = environmentLight.isDirectionalAllowed() ? environmentLight.getSunColor().clone()
					: ColorRGBA.Black;
			if (!Objects.equal(currentDirectional, directional)) {
				LOG.info(String.format("Directional changed to %s, interpolating current lighting towards this to value",
						directional));
				interpolators.add(new DirectionalLightInterpolator(currentDirectional, directional, environmentLight));
			}

		}
		if (pp == null) {
			LOG.info("No post processor enabled, no fog");
		} else {

			LegacyFogConfig fog = null;
			LegacyEnvironmentConfiguration parent = environmentConfiguration;
			while (fog == null && parent != null) {
				fog = parent.getFog();
				parent = parent.getDelegate();
			}

			final FogFilter fogFilter = (FogFilter) pp.getFogFilter();

			if (this.fog != null && (fog == null || !fog.isEnabled())) {
				// Disabled fog
				interpolators.add(new DisableFogInterpolator(fogFilter.getFogColor().clone(), fogFilter));
//				interpolators.add(new FogColourInterpolator(this.fog.getColor(), ColorRGBA.Black, fogFilter));
				fogFilter.setEnabled(true);
			} else if (fog != null && fog.isEnabled() && this.fog == null) {
				// Enabled
				fogFilter.setFogColor(ColorRGBA.Black);
				if (fog.getExp() > 0) {
					interpolators.add(new FogStartInterpolator(0f, fog.getStart(), fogFilter));
					interpolators.add(new FogEndInterpolator(0f, DEFAULT_FOG_END, fogFilter));
					fogFilter.setFogMode(FogMode.EXP2_DISTANCE_TO_INFINITY);
					interpolators.add(new FogExpInterpolator(0f, fog.getExp(), fogFilter));
				} else {
					interpolators.add(new FogStartInterpolator(0f, fog.getStart(), fogFilter));
					interpolators.add(new FogEndInterpolator(0f, fog.getEnd(), fogFilter));
					fogFilter.setFogMode(FogMode.LINEAR);
				}
				// fogFilter.setExcludeSky(true);
				// fogFilter.setExcludeSky(fog.isExcludeSky());
				// fogFilter.setExcludeSky(false);

				interpolators.add(new FogColourInterpolator(ColorRGBA.Black, fog.getColor().clone(), fogFilter));
				fogFilter.setEnabled(true);

			} else if (this.fog != null && fog != null && fog.isEnabled()) {
				// Updated
				if (!Objects.equal(fogFilter.getFogColor(), fog.getColor())) {
					interpolators.add(new FogColourInterpolator(fogFilter.getFogColor(), fog.getColor().clone(), fogFilter));
				}
				fogFilter.setEnabled(true);
				float fogStart = fog.getStart();
				float fogEnd = fog.getEnd();
				float fogExp = fog.getExp();
				if (fogExp == 0) {
					fogFilter.setFogMode(FogMode.LINEAR);
				} else {
					fogStart = DEFAULT_FOG_START;
					fogEnd = DEFAULT_FOG_END;
					fogFilter.setFogMode(FogMode.EXP2_DISTANCE_TO_INFINITY);
				}

				// fogFilter.setExcludeSky(true);
				// fogFilter.setExcludeSky(fog.isExcludeSky());
				// fogFilter.setExcludeSky(false);

				if (fogFilter.getFogDensity() != fogExp) {
					interpolators.add(new FogExpInterpolator(fogFilter.getFogDensity(), fogExp, fogFilter));
				}
				if (fogFilter.getFogStartDistance() != fogStart) {
					interpolators.add(new FogStartInterpolator(fogFilter.getFogStartDistance(), fogStart, fogFilter));
				}
				if (fogFilter.getFogEndDistance() != fogEnd) {
					interpolators.add(new FogEndInterpolator(fogFilter.getFogEndDistance(), fogEnd, fogFilter));
				}
			}

			if (fog != null) {
				fogFilter.setExcludeSky(fog.isExcludeSky());
			}

			this.fog = fog;
		}
		/*
		 * Skydome . If none is provided by this configuration, search up
		 * through any parents until one is found
		 */
		float off = 0;

		List<String> skyMaterials = null;
		LegacyEnvironmentConfiguration parent = environmentConfiguration;
		while (skyMaterials == null && parent != null) {
			skyMaterials = parent.getSky();
			parent = parent.getDelegate();
		}

		if (skyMaterials == null || skyMaterials.isEmpty()) {
			/* If there is now no sky, fade it out */
			if (skyNode != null) {
				LOG.info("No more sky materials, removing dome");
				interpolators.add(new SkyAlphaInterpolator(1f, 0f, skyNode));
				skyNode = null;
			}
		} else {
			/*
			 * If the list of materials has changed, fade out the old (if any)
			 * and fade in the new
			 */
			if (!Objects.equal(skyMaterials, this.skyMaterials)) {
				LOG.info("Sky materials changed, adjusting dome");
				this.skyMaterials = skyMaterials;
				if (skyNode != null) {
					interpolators.add(new SkyAlphaInterpolator(1f, 0f, skyNode));
				}

				skyNode = new Node("Skies");

				for (String meshName : skyMaterials) {
					String path = String.format("Env-%s.j3m", meshName);
					LOG.info(String.format("Loading skydome %s", path));
					Material material = assetManager.loadMaterial(SceneConstants.MATERIALS_PATH + "/" + path);
					Node s = (Node) assetManager.loadModel(new ModelKey("Environment/SkyDome.j3o"));
					s.setCullHint(Spatial.CullHint.Never);
					s.setMaterial(material);
					s.move(0, off, 0);
					s.scale(1 + off);

					if (material.getParam("CloudMap1") != null) {
						material.getTextureParam("CloudMap1").getTextureValue().setWrap(WrapMode.Repeat);
					}
					if (material.getParam("SkyMap") != null) {
						material.getTextureParam("SkyMap").getTextureValue().setWrap(WrapMode.Repeat);
					}
					if (material.getParam("CloudMap1Alpha") != null) {
						material.getTextureParam("CloudMap1Alpha").getTextureValue().setWrap(WrapMode.Repeat);
					}
					if (material.getParam("ColorMap") != null) {
						material.getTextureParam("ColorMap").getTextureValue().setWrap(WrapMode.Repeat);
					}
					if (material.getParam("AlphaMap") != null) {
						material.getTextureParam("AlphaMap").getTextureValue().setWrap(WrapMode.Repeat);
					}
					if (material.getParam("CelestialBodyMap") != null) {
						material.getTextureParam("CelestialBodyMap").getTextureValue().setWrap(WrapMode.BorderClamp);
					}

					off -= SkiesConstants.DOME_OFFSET_SCALE;
					skyNode.attachChild(s);
				}
				skyNode.setShadowMode(ShadowMode.Off);
				skyNode.setLocalScale(SkiesConstants.DOME_SCALE);
				skyNode.setQueueBucket(Bucket.Sky);

				gameNode.attachChild(skyNode);
				interpolators.add(new SkyAlphaInterpolator(0f, 1f, skyNode));
			}
		}

		// Start blending
		if (!interpolators.isEmpty()) {
			LOG.info(String.format("Environment has changed (%d interpolators to run), triggering reblend", interpolators.size()));
			blendProgress = 0;
		}

		updateAudio(environmentConfiguration);
	}

	public interface Interpolator<T> {
		void interpolate(float factor);

		void abort();
	}

	public abstract class AbstractInterpolator<T> implements Interpolator<T> {
		protected T start;
		protected T end;

		public AbstractInterpolator(T start, T end) {
			this.start = start;
			this.end = end;
		}

		public void abort() {
		}
	}

	public class FogColourInterpolator extends AbstractInterpolator<ColorRGBA> {
		private FogFilter fogFilter;

		public FogColourInterpolator(ColorRGBA start, ColorRGBA end, FogFilter fogFilter) {
			super(start, end);
			this.fogFilter = fogFilter;
		}

		@Override
		public void interpolate(float factor) {
			ColorRGBA a = start.clone();
			a.interpolate(end, factor);
			fogFilter.setFogColor(a);
		}

	}

	public class FogStartInterpolator extends AbstractInterpolator<Float> {
		private FogFilter fogFilter;

		public FogStartInterpolator(Float start, Float end, FogFilter fogFilter) {
			super(start, end);
			this.fogFilter = fogFilter;
		}

		@Override
		public void interpolate(float factor) {
			fogFilter.setFogStartDistance(start + ((end - start) * factor));
		}

	}

	public class FogEndInterpolator extends AbstractInterpolator<Float> {
		private FogFilter fogFilter;

		public FogEndInterpolator(Float start, Float end, FogFilter fogFilter) {
			super(start, end);
			this.fogFilter = fogFilter;
		}

		@Override
		public void interpolate(float factor) {
			fogFilter.setFogEndDistance(start + ((end - start) * factor));
		}

	}

	public class FogExpInterpolator extends AbstractInterpolator<Float> {
		private FogFilter fogFilter;

		public FogExpInterpolator(Float start, Float end, FogFilter fogFilter) {
			super(start, end);
			this.fogFilter = fogFilter;
		}

		@Override
		public void interpolate(float factor) {
			fogFilter.setFogDensity(start + ((end - start) * factor));
		}

	}

	public class DirectionalLightInterpolator extends AbstractInterpolator<ColorRGBA> {
		private EnvironmentLight light;

		public DirectionalLightInterpolator(ColorRGBA start, ColorRGBA end, EnvironmentLight light) {
			super(start, end);
			this.light = light;
		}

		@Override
		public void interpolate(float factor) {
			ColorRGBA a = start.clone();
			a.interpolate(end, factor);
			if (a.equals(ColorRGBA.Black) && light.isDirectionalEnabled()) {
				light.setDirectionalEnabled(false);
			} else if (!a.equals(ColorRGBA.Black) && !light.isDirectionalEnabled()) {
				light.setDirectionalEnabled(true);
			}
			light.setDirectionalColor(a);
		}

	}

	public class SkyAlphaInterpolator extends AbstractInterpolator<Float> {
		private Node skyNode;

		public SkyAlphaInterpolator(Float start, Float end, Node skyNode) {
			super(start, end);
			this.skyNode = skyNode;
		}

		@Override
		public void abort() {
			if (end == 0) {
				LOG.info("Removing sky node");
				skyNode.removeFromParent();
			}
		}

		@Override
		public void interpolate(float factor) {
			float a = start + ((end - start) * factor);
			for (Spatial s : skyNode.getChildren()) {
				for (Spatial g : ((Node) s).getChildren()) {
					((Geometry) g).getMaterial().setFloat("GlobalAlpha", a);
				}
			}
			if (factor >= 1f && end == 0) {
				LOG.info("Removing sky node");
				skyNode.removeFromParent();
			}
		}

	}

	public class DisableFogInterpolator extends AbstractInterpolator<ColorRGBA> {

		private FogFilter fogFilter;
		private ColorRGBA colStart;

		public DisableFogInterpolator(ColorRGBA start, FogFilter fogFilter) {
			super(start, null);
			this.fogFilter = fogFilter;
			colStart = fogFilter.getFogColor();
		}

		@Override
		public void interpolate(float factor) {
			ColorRGBA a = colStart.clone();
			a.a = (colStart.a - (colStart.a * factor));
			System.out.println("FC: " + a);
			fogFilter.setFogColor(a);
			if (factor == 1) {
				System.out.println("Fog disabled");
				fogFilter.setEnabled(false);
			}
		}
	}

	public class AmbientLightInterpolator extends AbstractInterpolator<ColorRGBA> {
		private EnvironmentLight light;

		public AmbientLightInterpolator(ColorRGBA start, ColorRGBA end, EnvironmentLight light) {
			super(start, end);
			this.light = light;
		}

		@Override
		public void interpolate(float factor) {
			ColorRGBA a = start.clone();
			a.interpolate(end, factor);
			if (a.equals(ColorRGBA.Black) && light.isAmbientEnabled()) {
				light.setAmbientEnabled(false);
			} else if (!a.equals(ColorRGBA.Black) && !light.isAmbientEnabled()) {
				light.setAmbientEnabled(true);
			}
			light.setAmbientColor(a);
		}
	}

}

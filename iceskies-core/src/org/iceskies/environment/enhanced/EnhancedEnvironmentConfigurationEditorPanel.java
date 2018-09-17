package org.iceskies.environment.enhanced;

import java.util.Set;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import org.icelib.Icelib;
import org.icescene.audio.AudioField;
import org.icescene.audio.AudioQueue;
import org.icescene.environment.Weather;
import org.icescene.ui.Playlist;
import org.iceskies.environment.AbstractEnvironmentConfigurationEditorPanel;
import org.iceskies.environment.PlaylistType;
import org.iceui.controls.ImageFieldControl;
import org.iceui.controls.SoundFieldControl.Type;
import org.iceui.controls.TabPanelContent;

import com.jme3.math.ColorRGBA;
import com.jme3.texture.Texture.WrapMode;

import icemoon.iceloader.ServerAssetManager;
import icetone.controls.buttons.CheckBox;
import icetone.controls.containers.TabControl;
import icetone.controls.lists.ComboBox;
import icetone.controls.lists.FloatRangeSpinnerModel;
import icetone.controls.lists.IntegerRangeSpinnerModel;
import icetone.controls.lists.Spinner;
import icetone.controls.text.Label;
import icetone.core.BaseElement;
import icetone.core.BaseScreen;
import icetone.core.Form;
import icetone.core.Orientation;
import icetone.core.StyledContainer;
import icetone.core.ToolKit;
import icetone.core.layout.FillLayout;
import icetone.core.layout.mig.MigLayout;
import icetone.core.undo.UndoManager;
import icetone.extras.chooser.ColorFieldControl;
import icetone.extras.chooser.StringChooserModel;

public class EnhancedEnvironmentConfigurationEditorPanel
		extends AbstractEnvironmentConfigurationEditorPanel<EnhancedEnvironmentConfiguration> {

	private static final Logger LOG = Logger.getLogger(EnhancedEnvironmentConfigurationEditorPanel.class.getName());
	private ImageFieldControl cloudsMapOption;
	private ImageFieldControl celestialBodyMap;
	private ImageFieldControl fogAlphaMapOption;
	private ImageFieldControl skyMapOption;
	private CheckBox enableSkyDome;
	private CheckBox enableClouds;
	private Spinner<Float> celestialBodyAlpha;
	private Spinner<Float> celestialBodyScale;
	private Spinner<Float> celestialBodyRotation;
	private Spinner<Float> celestialBodySpeed;
	private Spinner<Float> skyMapAlpha;
	private Spinner<Float> transitionSpeed;
	private Spinner<Float> cloudCycleSpeed;
	private Spinner<Float> cloudFarSpeed;
	private Spinner<Float> cloudNearSpeed;
	private Spinner<Float> cloudFarRotation;
	private Spinner<Float> cloudNearRotation;
	private Spinner<Float> cloudMaxOpacity;
	private Spinner<Float> cloudMinOpacity;
	private ColorFieldControl viewportColor;
	private ColorFieldControl skyColor;
	private ColorFieldControl skyShade;
	private CheckBox controlFog;
	private CheckBox enableFog;
	private Spinner<Float> fogDistance;
	private Spinner<Float> fogDensity;
	private ColorFieldControl fogColor;
	private ComboBox<Weather> outlook;
	private Spinner<Float> weatherIntensity;
	private Spinner<Float> weatherDensity;
	private CheckBox wind;
	private AudioField weatherMusic;
	private Set<String> soundResources;
	private Spinner<Float> weatherMusicGain;
	private CheckBox ambientEnabled;
	private ColorFieldControl ambientLight;
	private CheckBox directionalEnabled;
	private CheckBox controlDirectionalPosition;
	private ColorFieldControl directionalLight;
	private Spinner<Float> startDirectionalAngle;
	private Spinner<Float> endDirectionalAngle;
	private Playlist ambientMusic;
	private AudioField activateMusic;
	private AudioField music;
	private Spinner<Integer> ambientMusicDelay;
	private Spinner<Integer> activateMusicDelay;
	private Set<String> imageResources;
	private Spinner<Float> cloudsMapScale;
	private Spinner<Float> skyMapScale;
	private CheckBox skyMapUseAsMap;
	private CheckBox cloudsMapUseAsMap;
	private ComboBox<WrapMode> skyMapWrap;
	private ComboBox<WrapMode> celestialBodyWrap;

	public EnhancedEnvironmentConfigurationEditorPanel(UndoManager undoManager, BaseScreen screen, Preferences prefs,
			EnhancedEnvironmentConfiguration environmentConfiguration) {
		super(undoManager, screen, prefs, environmentConfiguration);
		setLayoutManager(new FillLayout());

		soundResources = ((ServerAssetManager) screen.getApplication().getAssetManager())
				.getAssetNamesMatching("Sounds/.*\\.ogg");
		imageResources = ((ServerAssetManager) screen.getApplication().getAssetManager())
				.getAssetNamesMatching("Environment/.*\\.png");

		TabControl envTabs = new TabControl(screen);
		envTabs.setUseSlideEffect(true);

		skyTab(envTabs, 0);
		skyColorsTab(envTabs, 1);
		fogTab(envTabs, 2);
		weatherTab(envTabs, 3);
		lightTab(envTabs, 4);
		soundTab(envTabs, 5);
		imagesTab(envTabs, 6);

		addElement(envTabs);
		rebuild();
	}

	private void skyTab(TabControl tabs, int tabIndex) {
		BaseElement el = createTabPanel();
		Form f = new Form(screen);

		// Checkboxes
		StyledContainer ccont = new StyledContainer(screen);
		ccont.setLayoutManager(new MigLayout(screen, "", "[fill,grow][fill,grow]", "[align top]"));

		// Skydome
		enableSkyDome = new CheckBox(screen);
		enableSkyDome.onChange(evt -> {
			if (!evt.getSource().isAdjusting()) {
				environmentConfiguration.setSkyDome(evt.getNewValue());
				checkVisible();
			}
		});
		f.addFormElement(enableSkyDome);
		enableSkyDome.setText("Enable Skydome");
		ccont.addElement(enableSkyDome, "growx");
		// Clouds
		enableClouds = new CheckBox(screen);
		enableClouds.onChange(evt -> {
			if (!evt.getSource().isAdjusting())
				environmentConfiguration.setClouds(evt.getNewValue());
		});
		enableClouds.setText("Enable Clouds");
		f.addFormElement(enableClouds);
		ccont.addElement(enableClouds, "growx");
		el.addElement(ccont, "span 2, growx");

		// Celestial body alpha
		Label l1 = new Label(screen);
		l1.setText("Celestial Body Alpha");
		el.addElement(l1);
		celestialBodyAlpha = new Spinner<Float>(screen, Orientation.HORIZONTAL, true);
		celestialBodyAlpha.setLabel(l1);
		celestialBodyAlpha.onChange(evt -> environmentConfiguration.setCelestialBodyAlpha(evt.getNewValue()));
		celestialBodyAlpha.setSpinnerModel(new FloatRangeSpinnerModel(0, 1, 0.05f, 0));
		celestialBodyAlpha.setFormatterString("%1.2f");
		f.addFormElement(celestialBodyAlpha);
		el.addElement(celestialBodyAlpha, "growx");

		// Celestial body rotation
		l1 = new Label(screen);
		l1.setText("Celestial Body Rotation");
		el.addElement(l1);
		celestialBodyRotation = new Spinner<Float>(screen, Orientation.HORIZONTAL, true);
		celestialBodyRotation.onChange(evt -> environmentConfiguration.setCelestialBodyDirection(evt.getNewValue()));
		celestialBodyRotation.setLabel(l1);
		celestialBodyRotation.setSpinnerModel(new FloatRangeSpinnerModel(0, 360, 10f, 0));
		celestialBodyRotation.setFormatterString("%3.0f");
		f.addFormElement(celestialBodyRotation);
		el.addElement(celestialBodyRotation, "growx");

		// Celestial body speed
		l1 = new Label(screen);
		l1.setText("Celestial Body Speed");
		el.addElement(l1);
		celestialBodySpeed = new Spinner<Float>(screen, Orientation.HORIZONTAL, true);
		celestialBodySpeed.onChange(evt -> environmentConfiguration.setCelestialBodySpeed(evt.getNewValue()));
		celestialBodySpeed.setLabel(l1);
		celestialBodySpeed.setSpinnerModel(new FloatRangeSpinnerModel(-8, 8, 0.1f, 0));
		celestialBodySpeed.setFormatterString("%3.1f");
		f.addFormElement(celestialBodySpeed);
		el.addElement(celestialBodySpeed, "growx");

		// Sky map alpha
		l1 = new Label(screen);
		l1.setText("Sky Map Alpha");
		el.addElement(l1);
		skyMapAlpha = new Spinner<Float>(screen, Orientation.HORIZONTAL, true);
		skyMapAlpha.onChange(evt -> environmentConfiguration.setSkyMapAlpha(evt.getNewValue()));
		skyMapAlpha.setLabel(l1);
		skyMapAlpha.setSpinnerModel(new FloatRangeSpinnerModel(0, 1, 0.1f, 1));
		skyMapAlpha.setFormatterString("%1.3f");
		f.addFormElement(skyMapAlpha);
		el.addElement(skyMapAlpha, "growx");

		// Transition speed
		l1 = new Label(screen);
		l1.setText("Transition Speed");
		el.addElement(l1);
		transitionSpeed = new Spinner<Float>(screen, Orientation.HORIZONTAL, true);
		transitionSpeed.onChange(evt -> environmentConfiguration.setTransitionSpeed(evt.getNewValue()));
		transitionSpeed.setLabel(l1);
		transitionSpeed.setSpinnerModel(new FloatRangeSpinnerModel(0, 20, 0.1f, 0));
		transitionSpeed.setFormatterString("%1.3f");
		f.addFormElement(transitionSpeed);
		el.addElement(transitionSpeed, "growx");

		// Cloud cycle speed
		l1 = new Label(screen);
		l1.setText("Cloud Cycle Speed");
		el.addElement(l1);
		cloudCycleSpeed = new Spinner<Float>(screen, Orientation.HORIZONTAL, true);
		cloudCycleSpeed.onChange(evt -> environmentConfiguration.setCloudCycleSpeed(evt.getNewValue()));
		cloudCycleSpeed.setLabel(l1);
		cloudCycleSpeed.setSpinnerModel(new FloatRangeSpinnerModel(0, 10, 0.1f, 0));
		cloudCycleSpeed.setFormatterString("%1.3f");
		f.addFormElement(cloudCycleSpeed);
		el.addElement(cloudCycleSpeed, "growx");

		// Cloud far speed
		l1 = new Label(screen);
		l1.setText("Cloud Far Speed");
		el.addElement(l1);
		cloudFarSpeed = new Spinner<Float>(screen, Orientation.HORIZONTAL, true);
		cloudFarSpeed.onChange(evt -> environmentConfiguration.setCloudsFarSpeed(evt.getNewValue()));
		cloudFarSpeed.setLabel(l1);
		cloudFarSpeed.setSpinnerModel(new FloatRangeSpinnerModel(-4, 4, 0.1f, 0));
		cloudFarSpeed.setFormatterString("%1.3f");
		f.addFormElement(cloudFarSpeed);
		el.addElement(cloudFarSpeed, "growx");

		// Cloud near speed
		l1 = new Label(screen);
		l1.setText("Cloud Near Speed");
		el.addElement(l1);
		cloudNearSpeed = new Spinner<Float>(screen, Orientation.HORIZONTAL, true);
		cloudNearSpeed.onChange(evt -> environmentConfiguration.setCloudsNearSpeed(evt.getNewValue()));
		cloudNearSpeed.setLabel(l1);
		cloudNearSpeed.setSpinnerModel(new FloatRangeSpinnerModel(-4, 4, 0.1f, 0));
		cloudNearSpeed.setFormatterString("%1.3f");
		f.addFormElement(cloudNearSpeed);
		el.addElement(cloudNearSpeed, "growx");

		// Cloud far rotation
		l1 = new Label(screen);
		l1.setText("Cloud Far Rotation");
		el.addElement(l1);
		cloudFarRotation = new Spinner<Float>(screen, Orientation.HORIZONTAL, true);
		cloudFarRotation.onChange(evt -> environmentConfiguration.setCloudsFarRotation(evt.getNewValue().intValue()));
		cloudFarRotation.setLabel(l1);
		cloudFarRotation.setSpinnerModel(new FloatRangeSpinnerModel(0, 360, 1f, 0));
		cloudFarRotation.setFormatterString("%3.0f");
		f.addFormElement(cloudFarRotation);
		el.addElement(cloudFarRotation, "growx");

		// Cloud near rotation
		l1 = new Label(screen);
		l1.setText("Cloud Near Rotation");
		el.addElement(l1);
		cloudNearRotation = new Spinner<Float>(screen, Orientation.HORIZONTAL, true);
		cloudNearRotation.onChange(evt -> environmentConfiguration.setCloudsNearRotation(evt.getNewValue().intValue()));
		cloudNearRotation.setLabel(l1);
		cloudNearRotation.setSpinnerModel(new FloatRangeSpinnerModel(0, 360, 1f, 0));
		cloudNearRotation.setFormatterString("%3.0f");
		f.addFormElement(cloudNearRotation);
		el.addElement(cloudNearRotation, "growx");

		// Min cloud opacity
		l1 = new Label(screen);
		l1.setText("Cloud Min Opacity");
		el.addElement(l1);
		cloudMinOpacity = new Spinner<Float>(screen, Orientation.HORIZONTAL, true);
		cloudMinOpacity.onChange(evt -> environmentConfiguration.setCloudMinOpacity(evt.getNewValue()));
		cloudMinOpacity.setLabel(l1);
		cloudMinOpacity.setSpinnerModel(new FloatRangeSpinnerModel(0, 1, 0.05f, 0));
		cloudMinOpacity.setFormatterString("%1.2f");
		f.addFormElement(cloudMinOpacity);
		el.addElement(cloudMinOpacity, "growx");

		// max cloud opacity
		l1 = new Label(screen);
		l1.setText("Cloud Max Opacity");
		el.addElement(l1);
		cloudMaxOpacity = new Spinner<Float>(screen, Orientation.HORIZONTAL, true);
		cloudMaxOpacity.onChange(evt -> environmentConfiguration.setCloudMaxOpacity(evt.getNewValue()));
		cloudMaxOpacity.setLabel(l1);
		cloudMaxOpacity.setSpinnerModel(new FloatRangeSpinnerModel(0, 1, 0.05f, 0));
		cloudMaxOpacity.setFormatterString("%1.2f");
		f.addFormElement(cloudMaxOpacity);
		el.addElement(cloudMaxOpacity, "growx");

		tabs.addTab("Sky");
		tabs.addTabChild(tabIndex, el);

	}

	private void skyColorsTab(TabControl tabs, int tabIndex) {
		BaseElement el = createTabPanel();
		Form f = new Form(screen);
		tabs.addTab("Sky Colors");
		tabs.addTabChild(tabIndex, el);

		// viewport color
		Label l1 = new Label(screen);
		l1.setText("Viewport Color");
		el.addElement(l1);
		viewportColor = new ColorFieldControl(screen, ColorRGBA.White);
		viewportColor.onChange(evt -> {
			LOG.info(String.format("New viewport color is %s", evt.getNewValue()));
			environmentConfiguration.setViewportColor(evt.getNewValue());
			ToolKit.get().getApplication().getViewPort().setBackgroundColor(evt.getNewValue());
		});
		viewportColor.setLabel(l1);
		f.addFormElement(viewportColor);
		el.addElement(viewportColor, "growx");

		// sky color
		l1 = new Label(screen);
		l1.setText("Sky");
		el.addElement(l1);
		skyColor = new ColorFieldControl(screen, ColorRGBA.White);
		skyColor.onChange(evt -> environmentConfiguration.setSkyColor(evt.getNewValue()));
		skyColor.setLabel(l1);
		f.addFormElement(skyColor);
		el.addElement(skyColor, "growx");

		// sky shade
		l1 = new Label(screen);
		l1.setText("Sky (secondary)");
		el.addElement(l1);
		skyShade = new ColorFieldControl(screen, ColorRGBA.White);
		skyShade.onChange(evt -> {
			LOG.info(String.format("New sky shade is %s", evt.getNewValue()));
			environmentConfiguration.setFogColor(evt.getNewValue());
		});
		skyShade.setLabel(l1);
		f.addFormElement(skyShade);
		el.addElement(skyShade, "growx");

	}

	private BaseElement createTabPanel() {
		BaseElement el = new TabPanelContent(screen);
		el.setIgnoreMouse(true);
		el.setResizable(false);
		el.setMovable(false);
		el.setLayoutManager(new MigLayout(screen, "hidemode 2, wrap 2", "[grow, fill][grow]"));
		return el;
	}

	private void fogTab(TabControl tabs, int tabIndex) {
		BaseElement el = createTabPanel();
		tabs.addTab("Fog");
		tabs.addTabChild(tabIndex, el);

		Form f = new Form(screen);

		// Control Fog
		controlFog = new CheckBox(screen);
		controlFog.onChange(evt -> {
			if (!evt.getSource().isAdjusting()) {
				environmentConfiguration.setControlFog(evt.getNewValue());
				checkVisible();
			}
		});
		controlFog.setText("Control Fog");
		el.addElement(controlFog, "span 2, growx");
		f.addFormElement(controlFog);

		// Enable Fog
		enableFog = new CheckBox(screen);
		enableFog.onChange(evt -> {
			if (!evt.getSource().isAdjusting()) {
				environmentConfiguration.setFog(evt.getNewValue());
				checkVisible();
			}
		});
		enableFog.setText("Enable Fog");
		el.addElement(enableFog, "span 2, growx");
		f.addFormElement(enableFog);

		// Fog Distance
		Label l1 = new Label(screen);
		l1.setText("Distance");
		el.addElement(l1, "gapleft 20");
		fogDistance = new Spinner<Float>(screen, Orientation.HORIZONTAL, false);
		fogDistance.onChange(evt -> environmentConfiguration.setFogDistance(evt.getNewValue()));
		fogDistance.setLabel(l1);
		fogDistance.setSpinnerModel(new FloatRangeSpinnerModel(0, 30000, 50f, 0));
		fogDistance.setFormatterString("%1.0f");
		el.addElement(fogDistance, "growx");
		f.addFormElement(fogDistance);

		// Fog Density
		l1 = new Label(screen);
		l1.setText("Density");
		el.addElement(l1, "gapleft 20");
		fogDensity = new Spinner<Float>(screen, Orientation.HORIZONTAL, false);
		fogDensity.onChange(evt -> environmentConfiguration.setFogDensity(evt.getNewValue()));
		fogDensity.setLabel(l1);
		fogDensity.setSpinnerModel(new FloatRangeSpinnerModel(0, 4, 0.25f, 0));
		fogDensity.setFormatterString("%1.3f");
		el.addElement(fogDensity, "growx");
		f.addFormElement(fogDensity);

		// Fog color
		l1 = new Label(screen);
		l1.setText("Color");
		el.addElement(l1, "gapleft 20");
		fogColor = new ColorFieldControl(screen, ColorRGBA.White);
		fogColor.onChange(evt -> environmentConfiguration.setFogColor(evt.getNewValue()));
		fogColor.setLabel(l1);
		el.addElement(fogColor, "growx");
		f.addFormElement(fogColor);

	}

	private void lightTab(TabControl tabs, int tabIndex) {
		BaseElement el = createTabPanel();
		Form f = new Form(screen);
		tabs.addTab("Light");
		tabs.addTabChild(tabIndex, el);

		// Enable Ambient
		ambientEnabled = new CheckBox(screen);
		ambientEnabled.onChange(evt -> {
			if (!evt.getSource().isAdjusting()) {
				environmentConfiguration.setAmbientEnabled(evt.getNewValue());
				checkVisible();
			}
		});
		ambientEnabled.setText("Enable Ambient");
		f.addFormElement(ambientEnabled);
		el.addElement(ambientEnabled, "span 2, growx");

		// Ambient light
		Label l1 = new Label(screen);
		l1.setText("Color");
		el.addElement(l1, "gapleft 20");
		ambientLight = new ColorFieldControl(screen, ColorRGBA.White);
		ambientLight.onChange(evt -> environmentConfiguration.setAmbientColor(evt.getNewValue()));
		ambientLight.setLabel(l1);
		f.addFormElement(ambientLight);
		el.addElement(ambientLight, "growx");

		// Enable Directional
		directionalEnabled = new CheckBox(screen);
		directionalEnabled.setText("Enable Directional");
		directionalEnabled.onChange(evt -> {
			if (!evt.getSource().isAdjusting()) {
				environmentConfiguration.setDirectionalEnabled(evt.getNewValue());
				checkVisible();
			}
		});
		f.addFormElement(directionalEnabled);
		el.addElement(directionalEnabled, "gaptop 20, span 2, growx");

		// Directional light
		l1 = new Label(screen);
		l1.setText("Color");
		el.addElement(l1, "gapleft 20");
		directionalLight = new ColorFieldControl(screen, ColorRGBA.White);
		directionalLight.onChange(evt -> environmentConfiguration.setDirectionalColor(evt.getNewValue()));
		directionalLight.setLabel(l1);
		f.addFormElement(directionalLight);
		el.addElement(directionalLight, "growx");

		// Start directional angle
		l1 = new Label(screen);
		l1.setText("Start Angle");
		el.addElement(l1, "gapleft 20");
		startDirectionalAngle = new Spinner<Float>(screen, Orientation.HORIZONTAL, true);
		startDirectionalAngle.onChange(evt -> environmentConfiguration.setStartDirectionalAngle(evt.getNewValue()));
		startDirectionalAngle.setLabel(l1);
		startDirectionalAngle.setSpinnerModel(new FloatRangeSpinnerModel(0, 180, 1, 0f));
		startDirectionalAngle.setFormatterString("%3.0f");
		f.addFormElement(startDirectionalAngle);
		el.addElement(startDirectionalAngle);

		// End directional angle
		l1 = new Label(screen);
		l1.setText("End Angle");
		el.addElement(l1, "gapleft 20");
		endDirectionalAngle = new Spinner<Float>(screen, Orientation.HORIZONTAL, true);
		endDirectionalAngle.onChange(evt -> environmentConfiguration.setEndDirectionalAngle(evt.getNewValue()));
		endDirectionalAngle.setLabel(l1);
		endDirectionalAngle.setSpinnerModel(new FloatRangeSpinnerModel(0, 180, 1, 0f));
		endDirectionalAngle.setFormatterString("%3.0f");
		f.addFormElement(endDirectionalAngle);
		el.addElement(endDirectionalAngle);

		// Control Directional
		controlDirectionalPosition = new CheckBox(screen);
		controlDirectionalPosition.onChange(e -> {
			if (!e.getSource().isAdjusting())
				environmentConfiguration.setControlDirectionalPosition(e.getNewValue());
		});
		controlDirectionalPosition.setText("Control Position");
		f.addFormElement(controlDirectionalPosition);
		el.addElement(controlDirectionalPosition, "gapleft 20, span 2, growx");
	}

	private void soundTab(TabControl tabs, int tabIndex) {
		BaseElement el = createTabPanel();
		tabs.addTab("Sound");
		tabs.addTabChild(tabIndex, el);

		Form f = new Form(screen);

		// Ambient Music
		Label l1 = new Label(screen);
		l1.setText("Ambient Music");
		el.addElement(l1);

		ambientMusic = new Playlist(screen, Type.ALL, AudioQueue.MUSIC, prefs, new StringChooserModel(soundResources));
		// ambientMusic = new AudioField(screen, null, soundResources, prefs,
		// AudioQueue.MUSIC) {
		// @Override
		// protected void onResourceChosen(String newResource) {
		// environmentConfiguration.setAmbientMusic(value == null ||
		// value.equals("") ? null : (String) value);
		// }
		// };
		ambientMusic.setLabel(l1);
		el.addElement(ambientMusic, "growx");
		f.addFormElement(ambientMusic);

		// Ambient Music Delay
		l1 = new Label(screen);
		l1.setText("Ambient Music Delay");
		el.addElement(l1);
		ambientMusicDelay = new Spinner<Integer>(screen, Orientation.HORIZONTAL, false);
		ambientMusicDelay.onChange(evt -> {
			if (!evt.getSource().isAdjusting()) {
				environmentConfiguration.setAmbientMusicDelay(evt.getNewValue());
			}
		});
		ambientMusicDelay.setLabel(l1);

		ambientMusicDelay.setSpinnerModel(new IntegerRangeSpinnerModel(0, 6000, 60, 0));
		el.addElement(ambientMusicDelay);

		f.addFormElement(ambientMusicDelay);

		// Activate Music
		l1 = new Label(screen);
		l1.setText("Activate Music");
		el.addElement(l1);

		activateMusic = new AudioField(screen, Type.ALL, null, new StringChooserModel(soundResources), prefs,
				AudioQueue.MUSIC) {
			@Override
			protected void onResourceChosen(String newResource) {
				// environmentConfiguration.setActivateMusic(value == null ||
				// value.equals("") ? null : (String) value);
			}
		};
		activateMusic.setLabel(l1);
		el.addElement(activateMusic, "growx");
		f.addFormElement(activateMusic);

		// Ambient Music Delay
		l1 = new Label(screen);
		l1.setText("Activate Music Delay");
		el.addElement(l1);
		activateMusicDelay = new Spinner<Integer>(screen, Orientation.HORIZONTAL, false);
		activateMusicDelay.onChange(evt -> {
			if (!evt.getSource().isAdjusting()) {
				environmentConfiguration.setActivateMusicMusicDelay(evt.getNewValue());
			}
		});
		activateMusicDelay.setLabel(l1);

		activateMusicDelay.setSpinnerModel(new IntegerRangeSpinnerModel(0, 6000, 60, 0));
		el.addElement(activateMusicDelay);
		f.addFormElement(activateMusicDelay);

		// Music
		l1 = new Label(screen);
		l1.setText("Music");
		el.addElement(l1);

		music = new AudioField(screen, Type.ALL, null, new StringChooserModel(soundResources), prefs,
				AudioQueue.MUSIC) {
			@Override
			protected void onResourceChosen(String newResource) {
				// environmentConfiguration.setMusic(newResource == null ||
				// newResource.equals("") ? null : (String) newResource);
			}
		};
		// music = new StyledSelectBox(screen) {
		// @Override
		// public void onChange(int selectedIndex, Object value) {
		// if (!adjusting) {
		// environmentConfiguration.setMusic(value.equals("") ? null : (String)
		// value);
		// }
		// }
		// };
		music.setLabel(l1);
		// populateMusic(music);
		el.addElement(music, "growx");
		f.addFormElement(music);
	}

	private void weatherTab(TabControl tabs, int tabIndex) {
		BaseElement el = createTabPanel();
		Form f = new Form(screen);
		tabs.addTab("Weather");
		tabs.addTabChild(tabIndex, el);

		// Outlook
		Label l1 = new Label(screen);
		l1.setText("Outlook");
		el.addElement(l1);
		outlook = new ComboBox<Weather>(screen);
		outlook.onChange(evt -> {
			if (!evt.getSource().isAdjusting()) {
				environmentConfiguration.setWeather(evt.getNewValue());
				checkVisible();
			}
		});
		outlook.setLabel(l1);
		for (Weather w : Weather.values()) {
			outlook.addListItem(Icelib.toEnglish(w), w);
		}
		f.addFormElement(outlook);
		el.addElement(outlook, "growx");

		// Weather Intensity
		l1 = new Label(screen);
		l1.setText("Intensity");
		el.addElement(l1);
		weatherIntensity = new Spinner<Float>(screen, Orientation.HORIZONTAL, false);
		weatherIntensity.onChange(evt -> {
			if (!evt.getSource().isAdjusting()) {
				environmentConfiguration.setWeatherIntensity(evt.getNewValue());
			}
		});
		weatherIntensity.setLabel(l1);
		weatherIntensity.setSpinnerModel(new FloatRangeSpinnerModel(0, 40, .1f, 0));
		weatherIntensity.setFormatterString("%.1f");
		weatherIntensity.setInterval(50);
		f.addFormElement(weatherIntensity);
		el.addElement(weatherIntensity, "growx");

		// Weather Speed
		l1 = new Label(screen);
		l1.setText("Density");
		el.addElement(l1);
		weatherDensity = new Spinner<Float>(screen, Orientation.HORIZONTAL, false);
		weatherDensity.onChange(evt -> environmentConfiguration.setWeatherDensity(evt.getNewValue()));
		weatherDensity.setLabel(l1);
		weatherDensity.setSpinnerModel(new FloatRangeSpinnerModel(0, 40, 0.1f, 0));
		weatherDensity.setFormatterString("%.1f");
		weatherDensity.setInterval(50);
		f.addFormElement(weatherDensity);
		el.addElement(weatherDensity, "growx");

		// Wind
		wind = new CheckBox(screen);
		wind.onChange(evt -> {
			if (!evt.getSource().isAdjusting())
				environmentConfiguration.setWind(evt.getNewValue());
		});
		wind.setText("Wind");
		wind.setLabel(l1);
		wind.setToolTipText("Adds randomly changing wind affecting weather particles");
		f.addFormElement(wind);
		el.addElement(wind, "gapleft 20, gaptop 8, span 2, growx");

		// Weather sound
		l1 = new Label(screen);
		l1.setText("Sound");
		el.addElement(l1, "gaptop 8");
		weatherMusic = new AudioField(screen, Type.ALL, null, new StringChooserModel(soundResources), prefs,
				AudioQueue.MUSIC) {
			@Override
			protected void onResourceChosen(String newResource) {
				// environmentConfiguration.setWeatherMusic(value == null ||
				// value.equals("") ? null : (String) value);
			}
		};
		weatherMusic.setLabel(l1);
		f.addFormElement(weatherMusic);
		el.addElement(weatherMusic, "gaptop 8, growx");

		// Weather sound gain
		l1 = new Label(screen);
		l1.setText("Sound Gain");
		el.addElement(l1);
		weatherMusicGain = new Spinner<Float>(screen, Orientation.HORIZONTAL, true);
		weatherMusicGain.onChange(evt -> environmentConfiguration.setWeatherMusicGain(evt.getNewValue()));
		weatherMusicGain.setLabel(l1);
		f.addFormElement(weatherMusicGain);
		weatherMusicGain.setSpinnerModel(new FloatRangeSpinnerModel(0, 2, 0.1f, 0));
		weatherMusicGain.setFormatterString("%1.3f");
		el.addElement(weatherMusicGain, "growx");

	}

	private void imagesTab(TabControl tabs, int tabIndex) {
		BaseElement el = createTabPanel();
		tabs.addTab("Images");
		tabs.addTabChild(tabIndex, el);

		// fog alpha map
		Label l1 = new Label(screen);
		l1.setText("Fog Alpha Map");
		el.addElement(l1, "growx 0");
		fogAlphaMapOption = new ImageFieldControl(screen, null, new StringChooserModel(imageResources), prefs) {
			@Override
			protected void onResourceChosen(String newResource) {
				environmentConfiguration.setFogAlphaMap(environmentConfiguration.relativize(newResource));
			}
		};
		fogAlphaMapOption.setLabel(l1);
		el.addElement(fogAlphaMapOption, "growx");

		// sky map

		l1 = new Label(screen);
		l1.setText("Sky Map");
		el.addElement(l1, "growx 0");
		skyMapOption = new ImageFieldControl(screen, null, new StringChooserModel(imageResources), prefs) {
			@Override
			protected void onResourceChosen(String newResource) {
				environmentConfiguration.setSkyMap(environmentConfiguration.relativize(newResource));
			}
		};
		skyMapOption.setLabel(l1);
		el.addElement(skyMapOption, "growx");

		// Sky Map body scale
		l1 = new Label(screen);
		l1.setText("Sky Map Scale");
		el.addElement(l1);
		skyMapScale = new Spinner<Float>(screen, Orientation.HORIZONTAL, true);
		skyMapScale.onChange(evt -> environmentConfiguration.setSkyMapScale(evt.getNewValue()));
		skyMapScale.setLabel(l1);
		skyMapScale.setSpinnerModel(new FloatRangeSpinnerModel(0, 16, 0.25f, 0));
		skyMapScale.setFormatterString("%1.2f");
		el.addElement(skyMapScale, "growx");

		// Sky Map wrap
		l1 = new Label(screen);
		l1.setText("Sky Map Wrap Mode");
		el.addElement(l1);
		skyMapWrap = new ComboBox<WrapMode>(screen);
		skyMapWrap.onChange(evt -> environmentConfiguration.setSkyMapWrap(evt.getNewValue()));
		skyMapWrap.setLabel(l1);
		for (WrapMode wm : WrapMode.values()) {
			skyMapWrap.addListItem(Icelib.toEnglish(wm), wm);
		}
		el.addElement(skyMapWrap, "growx");

		// Sky Map Use As Map
		skyMapUseAsMap = new CheckBox(screen);
		skyMapUseAsMap.onChange(evt -> {
			if (!evt.getSource().isAdjusting())
				environmentConfiguration.setSkyMapUseAsMap(evt.getNewValue());
		});
		skyMapUseAsMap.setText("Sky map is an alpha map");
		el.addElement(skyMapUseAsMap, "growx, span 2");

		// celestial body

		l1 = new Label(screen);
		l1.setText("Celestial Body");
		el.addElement(l1, "growx 0");
		celestialBodyMap = new ImageFieldControl(screen, null, new StringChooserModel(imageResources), prefs) {
			@Override
			protected void onResourceChosen(String newResource) {
				environmentConfiguration.setCelestialBodyMap(environmentConfiguration.relativize(newResource));
			}
		};
		celestialBodyMap.setLabel(l1);
		el.addElement(celestialBodyMap, "growx");

		// Celestial body scale
		l1 = new Label(screen);
		l1.setText("Celestial Body Scale");
		el.addElement(l1);
		celestialBodyScale = new Spinner<Float>(screen, Orientation.HORIZONTAL, true);
		celestialBodyScale.onChange(evt -> environmentConfiguration.setCelestialBodyScale(evt.getNewValue()));
		celestialBodyScale.setLabel(l1);
		celestialBodyScale.setSpinnerModel(new FloatRangeSpinnerModel(0, 16, 0.25f, 0));
		celestialBodyScale.setFormatterString("%1.2f");
		el.addElement(celestialBodyScale, "growx");
		// Sky Map wrap
		l1 = new Label(screen);
		l1.setText("Celestial Body Wrap Mode");
		el.addElement(l1);
		celestialBodyWrap = new ComboBox<WrapMode>(screen);
		celestialBodyWrap.onChange(evt -> environmentConfiguration.setCelestialBodyWrap(evt.getNewValue()));
		celestialBodyWrap.setLabel(l1);
		for (WrapMode wm : WrapMode.values()) {
			celestialBodyWrap.addListItem(Icelib.toEnglish(wm), wm);
		}
		el.addElement(celestialBodyWrap, "growx");

		// clouds
		l1 = new Label(screen);
		l1.setText("Clouds Map");
		el.addElement(l1, "growx 0");
		cloudsMapOption = new ImageFieldControl(screen, null, new StringChooserModel(imageResources), prefs) {
			@Override
			protected void onResourceChosen(String newResource) {
				environmentConfiguration.setCloudsMap(environmentConfiguration.relativize(newResource));
			}
		};
		cloudsMapOption.setLabel(l1);
		el.addElement(cloudsMapOption, "growx");

		// Sky Map body scale
		l1 = new Label(screen);
		l1.setText("Cloud Map Scale");
		el.addElement(l1);
		cloudsMapScale = new Spinner<Float>(screen, Orientation.HORIZONTAL, true);
		cloudsMapScale.onChange(evt -> environmentConfiguration.setCloudsMapScale(evt.getNewValue()));
		cloudsMapScale.setLabel(l1);
		cloudsMapScale.setSpinnerModel(new FloatRangeSpinnerModel(0, 16, 0.25f, 0));
		cloudsMapScale.setFormatterString("%1.2f");
		el.addElement(cloudsMapScale, "growx");

		// Clouds Map Use As Map
		cloudsMapUseAsMap = new CheckBox(screen);
		cloudsMapUseAsMap.onChange(evt -> {
			if (!evt.getSource().isAdjusting())
				environmentConfiguration.setCloudsMapUseAsMap(evt.getNewValue());
		});
		el.addElement(cloudsMapUseAsMap, "growx, span 2");
	}

	private void checkVisible() {
		final boolean useSkyDome = environmentConfiguration.isSkyDome();
		final boolean controlFogEnabled = environmentConfiguration.isControlFog();
		final boolean ambient = environmentConfiguration.isAmbientEnabled();
		final boolean directional = environmentConfiguration.isDirectionalEnabled();
		final boolean fog = environmentConfiguration.isFog();
		final boolean weather = !environmentConfiguration.getWeather().equals(Weather.FINE);
		LOG.info(String.format("Checking visible  sky: %s, controlFog: %s, fog: %s", useSkyDome, controlFog, fog));
		// final Element findElementByName =
		// screen.findElementByName("viewportColorChooser");
		// findElementByName.setVisible(!useSkyDome || !controlFog);

		enableClouds.setVisible(useSkyDome);
		celestialBodyRotation.setVisible(useSkyDome);
		celestialBodyAlpha.setVisible(useSkyDome);
		celestialBodyScale.setVisible(useSkyDome);
		celestialBodyWrap.setVisible(useSkyDome);
		skyMapAlpha.setVisible(useSkyDome);
		transitionSpeed.setVisible(useSkyDome);
		celestialBodySpeed.setVisible(useSkyDome);
		cloudCycleSpeed.setVisible(useSkyDome);
		cloudFarSpeed.setVisible(useSkyDome);
		cloudFarRotation.setVisible(useSkyDome);
		cloudNearSpeed.setVisible(useSkyDome);
		cloudNearRotation.setVisible(useSkyDome);
		cloudMinOpacity.setVisible(useSkyDome);
		cloudMaxOpacity.setVisible(useSkyDome);
		cloudsMapUseAsMap.setVisible(useSkyDome);
		skyMapUseAsMap.setVisible(useSkyDome);
		skyMapWrap.setVisible(useSkyDome);
		cloudsMapScale.setVisible(useSkyDome);
		skyMapScale.setVisible(useSkyDome);

		skyColor.setVisible(useSkyDome);
		skyShade.setVisible(useSkyDome && (!controlFogEnabled || !fog));
		viewportColor.setVisible(!useSkyDome);

		fogColor.setVisible(fog || (controlFogEnabled && useSkyDome));
		controlFog.setVisible(useSkyDome);
		fogDensity.setVisible(fog || (controlFogEnabled && useSkyDome));
		fogDistance.setVisible(fog || (controlFogEnabled && useSkyDome));

		weatherIntensity.setVisible(weather);
		weatherDensity.setVisible(weather);
		wind.setVisible(weather);

		directionalLight.setVisible(directional);
		ambientLight.setVisible(ambient);
		startDirectionalAngle.setVisible(useSkyDome && directional);
		endDirectionalAngle.setVisible(useSkyDome && directional);
		controlDirectionalPosition.setVisible(useSkyDome && directional);

		fogAlphaMapOption.setVisible(useSkyDome);
		celestialBodyMap.setVisible(useSkyDome);
		skyMapOption.setVisible(useSkyDome);
		cloudsMapOption.setVisible(useSkyDome);

		// environmentEditWindow.getContentArea().getLayoutManager().layout(environmentEditWindow.getContentArea());
		// environmentEditWindow.getLayoutManager().layout(environmentEditWindow);

	}

	public final void rebuild() {

		enableSkyDome.runAdjusting(() -> enableSkyDome.setChecked(environmentConfiguration.isSkyDome()));
		enableClouds.runAdjusting(() -> enableClouds.setChecked(environmentConfiguration.isClouds()));
		celestialBodyAlpha.setSelectedValue(environmentConfiguration.getCelestialBodyAlpha());
		celestialBodyScale.setSelectedValue(environmentConfiguration.getCelestialBodyScale());
		celestialBodyRotation.setSelectedValue(environmentConfiguration.getCelestialBodyDirection());
		celestialBodySpeed.setSelectedValue(environmentConfiguration.getCelestialBodySpeed());
		celestialBodyWrap.runAdjusting(
				() -> celestialBodyWrap.setSelectedByValue(environmentConfiguration.getCelestialBodyWrap()));
		skyMapAlpha.setSelectedValue(environmentConfiguration.getSkyMapAlpha());
		skyMapWrap.runAdjusting(() -> skyMapWrap.setSelectedByValue(environmentConfiguration.getSkyMapWrap()));
		skyMapScale.setSelectedValue(environmentConfiguration.getSkyMapScale());
		skyMapUseAsMap.runAdjusting(() -> skyMapUseAsMap.setChecked(environmentConfiguration.isSkyMapUseAsMap()));
		transitionSpeed.setSelectedValue(environmentConfiguration.getTransitionSpeed());
		cloudCycleSpeed.setSelectedValue(environmentConfiguration.getCloudCycleSpeed());
		cloudFarSpeed.setSelectedValue(environmentConfiguration.getCloudsFarSpeed());
		cloudNearSpeed.setSelectedValue(environmentConfiguration.getCloudsNearSpeed());
		cloudFarRotation.setSelectedValue(environmentConfiguration.getCloudsFarRotation());
		cloudNearRotation.setSelectedValue(environmentConfiguration.getCloudsNearRotation());
		cloudMinOpacity.setSelectedValue(environmentConfiguration.getCloudMinOpacity());
		cloudMaxOpacity.setSelectedValue(environmentConfiguration.getCloudMaxOpacity());
		viewportColor.setValue(environmentConfiguration.getViewportColor());
		skyColor.setValue(environmentConfiguration.getSkyColor());
		skyShade.setValue(environmentConfiguration.getFogColor());
		controlFog.runAdjusting(() -> controlFog.setChecked(environmentConfiguration.isControlFog()));
		enableFog.runAdjusting(() -> enableFog.setChecked(environmentConfiguration.isFog()));
		fogDistance.setSelectedValue(environmentConfiguration.getFogDistance());
		fogDensity.setSelectedValue(environmentConfiguration.getFogDensity());
		fogColor.setValue(environmentConfiguration.getFogColor());
		outlook.runAdjusting(() -> outlook.setSelectedByValue(environmentConfiguration.getWeather()));
		weatherIntensity
				.runAdjusting(() -> weatherIntensity.setSelectedValue(environmentConfiguration.getWeatherIntensity()));
		weatherDensity.setSelectedValue(environmentConfiguration.getWeatherDensity());
		wind.runAdjusting(() -> wind.setChecked(environmentConfiguration.isWind()));
		weatherMusic.setValue(Icelib.nonNull(environmentConfiguration.getPlaylist(PlaylistType.AMBIENT_NOISE)));
		weatherMusicGain.setSelectedValue(environmentConfiguration.getWeatherMusicGain());

		ambientEnabled.runAdjusting(() -> ambientEnabled.setChecked(environmentConfiguration.isAmbientEnabled()));
		ambientLight.setValue(environmentConfiguration.getAmbientColor());
		directionalEnabled
				.runAdjusting(() -> directionalEnabled.setChecked(environmentConfiguration.isDirectionalEnabled()));
		directionalLight.setValue(environmentConfiguration.getDirectionalColor());
		controlDirectionalPosition.runAdjusting(
				() -> controlDirectionalPosition.setChecked(environmentConfiguration.isControlDirectionalPosition()));
		startDirectionalAngle.setSelectedValue(environmentConfiguration.getStartDirectionalAngle());
		endDirectionalAngle.setSelectedValue(environmentConfiguration.getEndDirectionalAngle());

		ambientMusic.setAudio(environmentConfiguration.getPlaylist(PlaylistType.AMBIENT_MUSIC));
		activateMusic.setValue(Icelib.nonNull(environmentConfiguration.getPlaylist(PlaylistType.AMBIENT_MUSIC)));
		// music.setValue(Icelib.nonNull(environmentConfiguration.getMusic()));
		ambientMusicDelay.runAdjusting(
				() -> ambientMusicDelay.setSelectedValue(environmentConfiguration.getAmbientMusicDelay()));
		activateMusicDelay.runAdjusting(
				() -> activateMusicDelay.setSelectedValue(environmentConfiguration.getAmbientMusicDelay()));

		cloudsMapOption.setValue(environmentConfiguration.absolutize(environmentConfiguration.getCloudsMap()));
		cloudsMapScale.setSelectedValue(environmentConfiguration.getCloudsMapScale());
		cloudsMapUseAsMap
				.runAdjusting(() -> cloudsMapUseAsMap.setChecked(environmentConfiguration.isCloudsMapUseAsMap()));
		celestialBodyMap.setValue(environmentConfiguration.absolutize(environmentConfiguration.getCelestialBodyMap()));
		fogAlphaMapOption.setValue(environmentConfiguration.absolutize(environmentConfiguration.getFogAlphaMap()));
		skyMapOption.setValue(environmentConfiguration.absolutize(environmentConfiguration.getSkyMap()));

		checkVisible();
	}
}

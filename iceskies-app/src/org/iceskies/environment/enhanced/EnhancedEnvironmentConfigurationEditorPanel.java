package org.iceskies.environment.enhanced;

import java.util.Set;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import org.icelib.Icelib;
import org.icelib.UndoManager;
import org.icescene.audio.AudioField;
import org.icescene.audio.AudioQueue;
import org.icescene.environment.Weather;
import org.icescene.ui.Playlist;
import org.iceskies.environment.AbstractEnvironmentConfigurationEditorPanel;
import org.iceskies.environment.PlaylistType;
import org.iceui.XTabPanelContent;
import org.iceui.controls.ImageFieldControl;
import org.iceui.controls.SoundFieldControl.Type;
import org.iceui.controls.XTabControl;
import org.iceui.controls.color.ColorFieldControl;

import com.jme3.input.event.MouseButtonEvent;
import com.jme3.math.ColorRGBA;
import com.jme3.texture.Texture.WrapMode;

import icemoon.iceloader.ServerAssetManager;
import icetone.controls.buttons.CheckBox;
import icetone.controls.form.Form;
import icetone.controls.lists.ComboBox;
import icetone.controls.lists.FloatRangeSpinnerModel;
import icetone.controls.lists.Spinner;
import icetone.controls.text.Label;
import icetone.controls.windows.TabControl;
import icetone.core.Container;
import icetone.core.Element;
import icetone.core.ElementManager;
import icetone.core.layout.FillLayout;
import icetone.core.layout.mig.MigLayout;

public class EnhancedEnvironmentConfigurationEditorPanel
		extends AbstractEnvironmentConfigurationEditorPanel<EnhancedEnvironmentConfiguration> {

	private static final Logger LOG = Logger.getLogger(EnhancedEnvironmentConfigurationEditorPanel.class.getName());
	private ImageFieldControl cloudsMapOption;
	private ImageFieldControl celestialBodyMap;
	private ImageFieldControl fogAlphaMapOption;
	private ImageFieldControl skyMapOption;
	private boolean adjusting;
	private CheckBox enableSkyDome;
	private CheckBox enableClouds;
	private Spinner celestialBodyAlpha;
	private Spinner celestialBodyScale;
	private Spinner celestialBodyRotation;
	private Spinner celestialBodySpeed;
	private Spinner skyMapAlpha;
	private Spinner transitionSpeed;
	private Spinner cloudCycleSpeed;
	private Spinner cloudFarSpeed;
	private Spinner cloudNearSpeed;
	private Spinner cloudFarRotation;
	private Spinner cloudNearRotation;
	private Spinner cloudMaxOpacity;
	private Spinner cloudMinOpacity;
	private ColorFieldControl viewportColor;
	private ColorFieldControl skyColor;
	private ColorFieldControl skyShade;
	private CheckBox controlFog;
	private CheckBox enableFog;
	private Spinner fogDistance;
	private Spinner fogDensity;
	private ColorFieldControl fogColor;
	private ComboBox<Weather> outlook;
	private Spinner weatherIntensity;
	private Spinner weatherDensity;
	private CheckBox wind;
	private AudioField weatherMusic;
	private Set<String> soundResources;
	private Spinner weatherMusicGain;
	private CheckBox ambientEnabled;
	private ColorFieldControl ambientLight;
	private CheckBox directionalEnabled;
	private CheckBox controlDirectionalPosition;
	private ColorFieldControl directionalLight;
	private Spinner startDirectionalAngle;
	private Spinner endDirectionalAngle;
	private Playlist ambientMusic;
	private AudioField activateMusic;
	private AudioField music;
	private Spinner ambientMusicDelay;
	private Spinner activateMusicDelay;
	private Set<String> imageResources;
	private Spinner<Float> cloudsMapScale;
	private Spinner<Float> skyMapScale;
	private CheckBox skyMapUseAsMap;
	private CheckBox cloudsMapUseAsMap;
	private ComboBox<WrapMode> skyMapWrap;
	private ComboBox<WrapMode> celestialBodyWrap;

	public EnhancedEnvironmentConfigurationEditorPanel(UndoManager undoManager, ElementManager screen, Preferences prefs,
			EnhancedEnvironmentConfiguration environmentConfiguration) {
		super(undoManager, screen, prefs, environmentConfiguration);
		setLayoutManager(new FillLayout());

		soundResources = ((ServerAssetManager) screen.getApplication().getAssetManager()).getAssetNamesMatching("Sounds/.*\\.ogg");
		imageResources = ((ServerAssetManager) screen.getApplication().getAssetManager())
				.getAssetNamesMatching("Environment/.*\\.png");

		TabControl envTabs = new XTabControl(screen);
		envTabs.setUseSlideEffect(true);

		adjusting = true;
		skyTab(envTabs, 0);
		skyColorsTab(envTabs, 1);
		fogTab(envTabs, 2);
		weatherTab(envTabs, 3);
		lightTab(envTabs, 4);
		soundTab(envTabs, 5);
		imagesTab(envTabs, 6);
		adjusting = false;

		addChild(envTabs);
		rebuild();
	}

	private void skyTab(TabControl tabs, int tabIndex) {
		Element el = createTabPanel();
		Form f = new Form(screen);

		// Checkboxes
		Container ccont = new Container(screen);
		ccont.setLayoutManager(new MigLayout(screen, "", "[fill,grow][fill,grow]", "[align top]"));

		// Skydome
		enableSkyDome = new CheckBox(screen) {
			@Override
			public void onButtonMouseLeftUp(MouseButtonEvent evt, boolean toggled) {
				environmentConfiguration.setSkyDome(getIsChecked());
				checkVisible();

			}
		};
		f.addFormElement(enableSkyDome);
		enableSkyDome.setLabelText("Enable Skydome");
		ccont.addChild(enableSkyDome, "growx");
		// Clouds
		enableClouds = new CheckBox(screen) {
			@Override
			public void onButtonMouseLeftUp(MouseButtonEvent evt, boolean toggled) {
				environmentConfiguration.setClouds(getIsChecked());
			}
		};
		enableClouds.setLabelText("Enable Clouds");
		f.addFormElement(enableClouds);
		ccont.addChild(enableClouds, "growx");
		el.addChild(ccont, "span 2, growx");

		// Celestial body alpha
		Label l1 = new Label(screen);
		l1.setText("Celestial Body Alpha");
		el.addChild(l1);
		celestialBodyAlpha = new Spinner(screen, Orientation.HORIZONTAL, true) {
			@Override
			public void onChange(Object value) {
				environmentConfiguration.setCelestialBodyAlpha((Float) value);
			}
		};
		celestialBodyAlpha.setLabel(l1);
		celestialBodyAlpha.setSpinnerModel(new FloatRangeSpinnerModel(0, 1, 0.05f, 0));
		celestialBodyAlpha.setFormatterString("%1.2f");
		f.addFormElement(celestialBodyAlpha);
		el.addChild(celestialBodyAlpha, "growx");

		// Celestial body rotation
		l1 = new Label(screen);
		l1.setText("Celestial Body Rotation");
		el.addChild(l1);
		celestialBodyRotation = new Spinner(screen, Orientation.HORIZONTAL, true) {
			@Override
			public void onChange(Object value) {
				environmentConfiguration.setCelestialBodyDirection((Float) value);
			}
		};
		celestialBodyRotation.setLabel(l1);
		celestialBodyRotation.setSpinnerModel(new FloatRangeSpinnerModel(0, 360, 10f, 0));
		celestialBodyRotation.setFormatterString("%3.0f");
		f.addFormElement(celestialBodyRotation);
		el.addChild(celestialBodyRotation, "growx");

		// Celestial body speed
		l1 = new Label(screen);
		l1.setText("Celestial Body Speed");
		el.addChild(l1);
		celestialBodySpeed = new Spinner(screen, Orientation.HORIZONTAL, true) {
			@Override
			public void onChange(Object value) {
				environmentConfiguration.setCelestialBodySpeed((Float) value);
			}
		};
		celestialBodySpeed.setLabel(l1);
		celestialBodySpeed.setSpinnerModel(new FloatRangeSpinnerModel(-8, 8, 0.1f, 0));
		celestialBodySpeed.setFormatterString("%3.1f");
		f.addFormElement(celestialBodySpeed);
		el.addChild(celestialBodySpeed, "growx");

		// Sky map alpha
		l1 = new Label(screen);
		l1.setText("Sky Map Alpha");
		el.addChild(l1);
		skyMapAlpha = new Spinner(screen, Orientation.HORIZONTAL, true) {
			@Override
			public void onChange(Object value) {
				environmentConfiguration.setSkyMapAlpha((Float) value);
			}
		};
		skyMapAlpha.setLabel(l1);
		skyMapAlpha.setSpinnerModel(new FloatRangeSpinnerModel(0, 1, 0.1f, 1));
		skyMapAlpha.setFormatterString("%1.3f");
		f.addFormElement(skyMapAlpha);
		el.addChild(skyMapAlpha, "growx");

		// Transition speed
		l1 = new Label(screen);
		l1.setText("Transition Speed");
		el.addChild(l1);
		transitionSpeed = new Spinner(screen, Orientation.HORIZONTAL, true) {
			@Override
			public void onChange(Object value) {
				environmentConfiguration.setTransitionSpeed((Float) value);
			}
		};
		transitionSpeed.setLabel(l1);
		transitionSpeed.setSpinnerModel(new FloatRangeSpinnerModel(0, 20, 0.1f, 0));
		transitionSpeed.setFormatterString("%1.3f");
		f.addFormElement(transitionSpeed);
		el.addChild(transitionSpeed, "growx");

		// Cloud cycle speed
		l1 = new Label(screen);
		l1.setText("Cloud Cycle Speed");
		el.addChild(l1);
		cloudCycleSpeed = new Spinner(screen, Orientation.HORIZONTAL, true) {
			@Override
			public void onChange(Object value) {
				environmentConfiguration.setCloudCycleSpeed((Float) value);
			}
		};
		cloudCycleSpeed.setLabel(l1);
		cloudCycleSpeed.setSpinnerModel(new FloatRangeSpinnerModel(0, 10, 0.1f, 0));
		cloudCycleSpeed.setFormatterString("%1.3f");
		f.addFormElement(cloudCycleSpeed);
		el.addChild(cloudCycleSpeed, "growx");

		// Cloud far speed
		l1 = new Label(screen);
		l1.setText("Cloud Far Speed");
		el.addChild(l1);
		cloudFarSpeed = new Spinner(screen, Orientation.HORIZONTAL, true) {
			@Override
			public void onChange(Object value) {
				environmentConfiguration.setCloudsFarSpeed((Float) value);
			}
		};
		cloudFarSpeed.setLabel(l1);
		cloudFarSpeed.setSpinnerModel(new FloatRangeSpinnerModel(-4, 4, 0.1f, 0));
		cloudFarSpeed.setFormatterString("%1.3f");
		f.addFormElement(cloudFarSpeed);
		el.addChild(cloudFarSpeed, "growx");

		// Cloud near speed
		l1 = new Label(screen);
		l1.setText("Cloud Near Speed");
		el.addChild(l1);
		cloudNearSpeed = new Spinner(screen, Orientation.HORIZONTAL, true) {
			@Override
			public void onChange(Object value) {
				environmentConfiguration.setCloudsNearSpeed((Float) value);
			}
		};
		cloudNearSpeed.setLabel(l1);
		cloudNearSpeed.setSpinnerModel(new FloatRangeSpinnerModel(-4, 4, 0.1f, 0));
		cloudNearSpeed.setFormatterString("%1.3f");
		f.addFormElement(cloudNearSpeed);
		el.addChild(cloudNearSpeed, "growx");

		// Cloud far rotation
		l1 = new Label(screen);
		l1.setText("Cloud Far Rotation");
		el.addChild(l1);
		cloudFarRotation = new Spinner(screen, Orientation.HORIZONTAL, true) {
			@Override
			public void onChange(Object value) {
				environmentConfiguration.setCloudsFarRotation(((Float) value).intValue());
			}
		};
		cloudFarRotation.setLabel(l1);
		cloudFarRotation.setSpinnerModel(new FloatRangeSpinnerModel(0, 360, 1f, 0));
		cloudFarRotation.setFormatterString("%3.0f");
		f.addFormElement(cloudFarRotation);
		el.addChild(cloudFarRotation, "growx");

		// Cloud near rotation
		l1 = new Label(screen);
		l1.setText("Cloud Near Rotation");
		el.addChild(l1);
		cloudNearRotation = new Spinner(screen, Orientation.HORIZONTAL, true) {
			@Override
			public void onChange(Object value) {
				environmentConfiguration.setCloudsNearRotation(((Float) value).intValue());
			}
		};
		cloudNearRotation.setLabel(l1);
		cloudNearRotation.setSpinnerModel(new FloatRangeSpinnerModel(0, 360, 1f, 0));
		cloudNearRotation.setFormatterString("%3.0f");
		f.addFormElement(cloudNearRotation);
		el.addChild(cloudNearRotation, "growx");

		// Min cloud opacity
		l1 = new Label(screen);
		l1.setText("Cloud Min Opacity");
		el.addChild(l1);
		cloudMinOpacity = new Spinner(screen, Orientation.HORIZONTAL, true) {
			@Override
			public void onChange(Object value) {
				environmentConfiguration.setCloudMinOpacity((Float) value);
			}
		};
		cloudMinOpacity.setLabel(l1);
		cloudMinOpacity.setSpinnerModel(new FloatRangeSpinnerModel(0, 1, 0.05f, 0));
		cloudMinOpacity.setFormatterString("%1.2f");
		f.addFormElement(cloudMinOpacity);
		el.addChild(cloudMinOpacity, "growx");

		// max cloud opacity
		l1 = new Label(screen);
		l1.setText("Cloud Max Opacity");
		el.addChild(l1);
		cloudMaxOpacity = new Spinner(screen, Orientation.HORIZONTAL, true) {
			@Override
			public void onChange(Object value) {
				environmentConfiguration.setCloudMaxOpacity((Float) value);
			}
		};
		cloudMaxOpacity.setLabel(l1);
		cloudMaxOpacity.setSpinnerModel(new FloatRangeSpinnerModel(0, 1, 0.05f, 0));
		cloudMaxOpacity.setFormatterString("%1.2f");
		f.addFormElement(cloudMaxOpacity);
		el.addChild(cloudMaxOpacity, "growx");

		tabs.addTab("Sky");
		tabs.addTabChild(tabIndex, el);

	}

	private void skyColorsTab(TabControl tabs, int tabIndex) {
		Element el = createTabPanel();
		Form f = new Form(screen);
		tabs.addTab("Sky Colors");
		tabs.addTabChild(tabIndex, el);

		// viewport color
		Label l1 = new Label(screen);
		l1.setText("Viewport Color");
		el.addChild(l1);
		viewportColor = new ColorFieldControl(screen, ColorRGBA.White) {
			@Override
			protected void onChangeColor(ColorRGBA newColor) {
				LOG.info(String.format("New viewport color is %s", newColor));
				environmentConfiguration.setViewportColor(newColor);
				app.getViewPort().setBackgroundColor(newColor);

			}
		};
		viewportColor.setLabel(l1);
		f.addFormElement(viewportColor);
		el.addChild(viewportColor, "growx");

		// sky color
		l1 = new Label(screen);
		l1.setText("Sky");
		el.addChild(l1);
		skyColor = new ColorFieldControl(screen, ColorRGBA.White) {
			@Override
			protected void onChangeColor(ColorRGBA newColor) {
				environmentConfiguration.setSkyColor(newColor);

			}
		};
		skyColor.setLabel(l1);
		f.addFormElement(skyColor);
		el.addChild(skyColor, "growx");

		// sky shade
		l1 = new Label(screen);
		l1.setText("Sky (secondary)");
		el.addChild(l1);
		skyShade = new ColorFieldControl(screen, ColorRGBA.White) {
			@Override
			protected void onChangeColor(ColorRGBA newColor) {
				LOG.info(String.format("New sky shade is %s", newColor));
				environmentConfiguration.setFogColor(newColor);
			}
		};
		skyShade.setLabel(l1);
		f.addFormElement(skyShade);
		el.addChild(skyShade, "growx");

	}

	private Element createTabPanel() {
		Element el = new XTabPanelContent(screen) {
			@Override
			public void childShow() {
				super.childShow();
				// TODO eugh. symptom of a wider problem with how element
				// visibility is dealt with
				checkVisible();
			}
		};
		el.setScaleNS(true);
		el.setScaleEW(true);
		el.setIgnoreMouse(true);
		el.setIsResizable(false);
		el.setIsMovable(false);
		el.setLayoutManager(new MigLayout(screen, "hidemode 2, wrap 2", "[grow, fill][grow]"));
		return el;
	}

	private void fogTab(TabControl tabs, int tabIndex) {
		Element el = createTabPanel();
		tabs.addTab("Fog");
		tabs.addTabChild(tabIndex, el);

		Form f = new Form(screen);

		// Control Fog
		controlFog = new CheckBox(screen) {
			@Override
			public void onButtonMouseLeftUp(MouseButtonEvent evt, boolean toggled) {
				environmentConfiguration.setControlFog(getIsChecked());
				checkVisible();
			}
		};
		controlFog.setLabelText("Control Fog");
		el.addChild(controlFog, "span 2, growx");
		f.addFormElement(controlFog);

		// Enable Fog
		enableFog = new CheckBox(screen) {
			@Override
			public void onButtonMouseLeftUp(MouseButtonEvent evt, boolean toggled) {
				environmentConfiguration.setFog(getIsChecked());
				checkVisible();
			}
		};
		enableFog.setLabelText("Enable Fog");
		el.addChild(enableFog, "span 2, growx");
		f.addFormElement(enableFog);

		// Fog Distance
		Label l1 = new Label(screen);
		l1.setText("Distance");
		el.addChild(l1, "gapleft 20");
		fogDistance = new Spinner(screen, Orientation.HORIZONTAL, false) {
			@Override
			public void onChange(Object value) {
				environmentConfiguration.setFogDistance((Float) value);

			}
		};
		fogDistance.setLabel(l1);
		fogDistance.setSpinnerModel(new FloatRangeSpinnerModel(0, 30000, 50f, 0));
		fogDistance.setFormatterString("%1.0f");
		el.addChild(fogDistance, "growx");
		f.addFormElement(fogDistance);

		// Fog Density
		l1 = new Label(screen);
		l1.setText("Density");
		el.addChild(l1, "gapleft 20");
		fogDensity = new Spinner(screen, Orientation.HORIZONTAL, false) {
			@Override
			public void onChange(Object value) {
				environmentConfiguration.setFogDensity((Float) value);
			}
		};
		fogDensity.setLabel(l1);
		fogDensity.setSpinnerModel(new FloatRangeSpinnerModel(0, 4, 0.25f, 0));
		fogDensity.setFormatterString("%1.3f");
		el.addChild(fogDensity, "growx");
		f.addFormElement(fogDensity);

		// Fog color
		l1 = new Label(screen);
		l1.setText("Color");
		el.addChild(l1, "gapleft 20");
		fogColor = new ColorFieldControl(screen, ColorRGBA.White) {
			@Override
			protected void onChangeColor(ColorRGBA newColor) {
				environmentConfiguration.setFogColor(newColor);

			}
		};
		fogColor.setLabel(l1);
		el.addChild(fogColor, "growx");
		f.addFormElement(fogColor);

	}

	private void lightTab(TabControl tabs, int tabIndex) {
		Element el = createTabPanel();
		Form f = new Form(screen);
		tabs.addTab("Light");
		tabs.addTabChild(tabIndex, el);

		// Enable Ambient
		ambientEnabled = new CheckBox(screen) {
			@Override
			public void onButtonMouseLeftUp(MouseButtonEvent evt, boolean toggled) {
				environmentConfiguration.setAmbientEnabled(getIsChecked());
				checkVisible();
			}
		};
		ambientEnabled.setLabelText("Enable Ambient");
		f.addFormElement(ambientEnabled);
		el.addChild(ambientEnabled, "span 2, growx");

		// Ambient light
		Label l1 = new Label(screen);
		l1.setText("Color");
		el.addChild(l1, "gapleft 20");
		ambientLight = new ColorFieldControl(screen, ColorRGBA.White) {
			@Override
			protected void onChangeColor(ColorRGBA newColor) {
				environmentConfiguration.setAmbientColor(newColor);
			}
		};
		ambientLight.setLabel(l1);
		f.addFormElement(ambientLight);
		el.addChild(ambientLight, "growx");

		// Enable Directional
		directionalEnabled = new CheckBox(screen) {
			@Override
			public void onButtonMouseLeftUp(MouseButtonEvent evt, boolean toggled) {
				environmentConfiguration.setDirectionalEnabled(getIsChecked());
				checkVisible();
			}
		};
		directionalEnabled.setLabelText("Enable Directional");
		f.addFormElement(directionalEnabled);
		el.addChild(directionalEnabled, "gaptop 20, span 2, growx");

		// Directional light
		l1 = new Label(screen);
		l1.setText("Color");
		el.addChild(l1, "gapleft 20");
		directionalLight = new ColorFieldControl(screen, ColorRGBA.White) {
			@Override
			protected void onChangeColor(ColorRGBA newColor) {
				environmentConfiguration.setDirectionalColor(newColor);
			}
		};
		directionalLight.setLabel(l1);
		f.addFormElement(directionalLight);
		el.addChild(directionalLight, "growx");

		// Start directional angle
		l1 = new Label(screen);
		l1.setText("Start Angle");
		el.addChild(l1, "gapleft 20");
		startDirectionalAngle = new Spinner(screen, Orientation.HORIZONTAL, true) {
			@Override
			public void onChange(Object value) {
				environmentConfiguration.setStartDirectionalAngle((Float) value);
			}
		};
		startDirectionalAngle.setLabel(l1);
		startDirectionalAngle.setSpinnerModel(new FloatRangeSpinnerModel(0, 180, 1, 0f));
		startDirectionalAngle.setFormatterString("%3.0f");
		f.addFormElement(startDirectionalAngle);
		el.addChild(startDirectionalAngle);

		// End directional angle
		l1 = new Label(screen);
		l1.setText("End Angle");
		el.addChild(l1, "gapleft 20");
		endDirectionalAngle = new Spinner(screen, Orientation.HORIZONTAL, true) {
			@Override
			public void onChange(Object value) {
				environmentConfiguration.setEndDirectionalAngle((Float) value);
			}
		};
		endDirectionalAngle.setLabel(l1);
		endDirectionalAngle.setSpinnerModel(new FloatRangeSpinnerModel(0, 180, 1, 0f));
		endDirectionalAngle.setFormatterString("%3.0f");
		f.addFormElement(endDirectionalAngle);
		el.addChild(endDirectionalAngle);

		// Control Directional
		controlDirectionalPosition = new CheckBox(screen) {
			@Override
			public void onButtonMouseLeftUp(MouseButtonEvent evt, boolean toggled) {
				environmentConfiguration.setControlDirectionalPosition(getIsChecked());
			}
		};
		controlDirectionalPosition.setLabelText("Control Position");
		f.addFormElement(controlDirectionalPosition);
		el.addChild(controlDirectionalPosition, "gapleft 20, span 2, growx");
	}

	private void soundTab(TabControl tabs, int tabIndex) {
		Element el = createTabPanel();
		tabs.addTab("Sound");
		tabs.addTabChild(tabIndex, el);

		Form f = new Form(screen);

		// Ambient Music
		Label l1 = new Label(screen);
		l1.setText("Ambient Music");
		el.addChild(l1);

		ambientMusic = new Playlist(screen, Type.ALL, AudioQueue.MUSIC, prefs, soundResources);
		// ambientMusic = new AudioField(screen, null, soundResources, prefs,
		// AudioQueue.MUSIC) {
		// @Override
		// protected void onResourceChosen(String newResource) {
		// environmentConfiguration.setAmbientMusic(value == null ||
		// value.equals("") ? null : (String) value);
		// }
		// };
		ambientMusic.setLabel(l1);
		el.addChild(ambientMusic, "growx");
		f.addFormElement(ambientMusic);

		// Ambient Music Delay
		l1 = new Label(screen);
		l1.setText("Ambient Music Delay");
		el.addChild(l1);
		ambientMusicDelay = new Spinner(screen, Orientation.HORIZONTAL, false) {
			@Override
			public void onChange(Object value) {
				if (!adjusting) {
					environmentConfiguration.setAmbientMusicDelay((Integer) value);
				}
			}
		};
		ambientMusicDelay.setLabel(l1);

		ambientMusicDelay.setSpinnerModel(new FloatRangeSpinnerModel(0, 6000, 60, 0));
		el.addChild(ambientMusicDelay);

		f.addFormElement(ambientMusicDelay);

		// Activate Music
		l1 = new Label(screen);
		l1.setText("Activate Music");
		el.addChild(l1);

		activateMusic = new AudioField(screen, Type.ALL, null, soundResources, prefs, AudioQueue.MUSIC) {
			@Override
			protected void onResourceChosen(String newResource) {
				// environmentConfiguration.setActivateMusic(value == null ||
				// value.equals("") ? null : (String) value);
			}
		};
		activateMusic.setLabel(l1);
		el.addChild(activateMusic, "growx");
		f.addFormElement(activateMusic);

		// Ambient Music Delay
		l1 = new Label(screen);
		l1.setText("Activate Music Delay");
		el.addChild(l1);
		activateMusicDelay = new Spinner(screen, Orientation.HORIZONTAL, false) {
			@Override
			public void onChange(Object value) {
				if (!adjusting) {
					environmentConfiguration.setActivateMusicMusicDelay((Integer) value);
				}
			}
		};
		activateMusicDelay.setLabel(l1);

		activateMusicDelay.setSpinnerModel(new FloatRangeSpinnerModel(0, 6000, 60, 0));
		el.addChild(activateMusicDelay);
		f.addFormElement(activateMusicDelay);

		// Music
		l1 = new Label(screen);
		l1.setText("Music");
		el.addChild(l1);

		music = new AudioField(screen, Type.ALL, null, soundResources, prefs, AudioQueue.MUSIC) {
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
		el.addChild(music, "growx");
		f.addFormElement(music);
	}

	private void weatherTab(TabControl tabs, int tabIndex) {
		Element el = createTabPanel();
		Form f = new Form(screen);
		tabs.addTab("Weather");
		tabs.addTabChild(tabIndex, el);

		// Outlook
		Label l1 = new Label(screen);
		l1.setText("Outlook");
		el.addChild(l1);
		outlook = new ComboBox<Weather>(screen) {
			@Override
			public void onChange(int selectedIndex, Weather value) {
				if (!adjusting) {
					environmentConfiguration.setWeather(value);
					checkVisible();
				}
			}
		};
		outlook.setLabel(l1);
		for (Weather w : Weather.values()) {
			outlook.addListItem(Icelib.toEnglish(w), w);
		}
		f.addFormElement(outlook);
		el.addChild(outlook, "growx");

		// Weather Intensity
		l1 = new Label(screen);
		l1.setText("Intensity");
		el.addChild(l1);
		weatherIntensity = new Spinner(screen, Orientation.HORIZONTAL, false) {
			@Override
			public void onChange(Object value) {
				if (!adjusting) {
					environmentConfiguration.setWeatherIntensity((Float) value);
				}
			}
		};
		weatherIntensity.setLabel(l1);
		weatherIntensity.setSpinnerModel(new FloatRangeSpinnerModel(0, 40, .1f, 0));
		weatherIntensity.setFormatterString("%.1f");
		weatherIntensity.setInterval(50);
		f.addFormElement(weatherIntensity);
		el.addChild(weatherIntensity, "growx");

		// Weather Speed
		l1 = new Label(screen);
		l1.setText("Density");
		el.addChild(l1);
		weatherDensity = new Spinner(screen, Orientation.HORIZONTAL, false) {
			@Override
			public void onChange(Object value) {
				environmentConfiguration.setWeatherDensity((Float) value);
			}
		};
		weatherDensity.setLabel(l1);
		weatherDensity.setSpinnerModel(new FloatRangeSpinnerModel(0, 40, 0.1f, 0));
		weatherDensity.setFormatterString("%.1f");
		weatherDensity.setInterval(50);
		f.addFormElement(weatherDensity);
		el.addChild(weatherDensity, "growx");

		// Wind
		wind = new CheckBox(screen) {
			@Override
			public void onButtonMouseLeftUp(MouseButtonEvent evt, boolean toggled) {
				environmentConfiguration.setWind(getIsChecked());
			}
		};
		wind.setLabelText("Wind");
		wind.setLabel(l1);
		wind.setToolTipText("Adds randomly changing wind affecting weather particles");
		f.addFormElement(wind);
		el.addChild(wind, "gapleft 20, gaptop 8, span 2, growx");

		// Weather sound
		l1 = new Label(screen);
		l1.setText("Sound");
		el.addChild(l1, "gaptop 8");
		weatherMusic = new AudioField(screen, Type.ALL, null, soundResources, prefs, AudioQueue.MUSIC) {
			@Override
			protected void onResourceChosen(String newResource) {
				// environmentConfiguration.setWeatherMusic(value == null ||
				// value.equals("") ? null : (String) value);
			}
		};
		weatherMusic.setLabel(l1);
		f.addFormElement(weatherMusic);
		el.addChild(weatherMusic, "gaptop 8, growx");

		// Weather sound gain
		l1 = new Label(screen);
		l1.setText("Sound Gain");
		el.addChild(l1);
		weatherMusicGain = new Spinner(screen, Orientation.HORIZONTAL, true) {
			@Override
			public void onChange(Object value) {
				environmentConfiguration.setWeatherMusicGain((Float) value);
			}
		};
		weatherMusicGain.setLabel(l1);
		f.addFormElement(weatherMusicGain);
		weatherMusicGain.setSpinnerModel(new FloatRangeSpinnerModel(0, 2, 0.1f, 0));
		weatherMusicGain.setFormatterString("%1.3f");
		el.addChild(weatherMusicGain, "growx");

	}

	private void imagesTab(TabControl tabs, int tabIndex) {
		Element el = createTabPanel();
		tabs.addTab("Images");
		tabs.addTabChild(tabIndex, el);

		// fog alpha map
		Label l1 = new Label(screen);
		l1.setText("Fog Alpha Map");
		el.addChild(l1, "growx 0");
		fogAlphaMapOption = new ImageFieldControl(screen, null, imageResources, prefs) {
			@Override
			protected void onResourceChosen(String newResource) {
				environmentConfiguration.setFogAlphaMap(environmentConfiguration.relativize(newResource));
			}
		};
		fogAlphaMapOption.setLabel(l1);
		el.addChild(fogAlphaMapOption, "growx");

		// sky map

		l1 = new Label(screen);
		l1.setText("Sky Map");
		el.addChild(l1, "growx 0");
		skyMapOption = new ImageFieldControl(screen, null, imageResources, prefs) {
			@Override
			protected void onResourceChosen(String newResource) {
				environmentConfiguration.setSkyMap(environmentConfiguration.relativize(newResource));
			}
		};
		skyMapOption.setLabel(l1);
		el.addChild(skyMapOption, "growx");

		// Sky Map body scale
		l1 = new Label(screen);
		l1.setText("Sky Map Scale");
		el.addChild(l1);
		skyMapScale = new Spinner(screen, Orientation.HORIZONTAL, true) {
			@Override
			public void onChange(Object value) {
				environmentConfiguration.setSkyMapScale((Float) value);
			}
		};
		skyMapScale.setLabel(l1);
		skyMapScale.setSpinnerModel(new FloatRangeSpinnerModel(0, 16, 0.25f, 0));
		skyMapScale.setFormatterString("%1.2f");
		el.addChild(skyMapScale, "growx");

		// Sky Map wrap
		l1 = new Label(screen);
		l1.setText("Sky Map Wrap Mode");
		el.addChild(l1);
		skyMapWrap = new ComboBox<WrapMode>(screen) {

			@Override
			public void onChange(int selectedIndex, WrapMode value) {
				environmentConfiguration.setSkyMapWrap(value);

			}
		};
		skyMapWrap.setLabel(l1);
		for (WrapMode wm : WrapMode.values()) {
			skyMapWrap.addListItem(Icelib.toEnglish(wm), wm);
		}
		el.addChild(skyMapWrap, "growx");

		// Sky Map Use As Map
		skyMapUseAsMap = new CheckBox(screen) {
			@Override
			public void onButtonMouseLeftUp(MouseButtonEvent evt, boolean toggled) {
				environmentConfiguration.setSkyMapUseAsMap(toggled);
			}

		};
		skyMapUseAsMap.setLabelText("Sky map is an alpha map");
		el.addChild(skyMapUseAsMap, "growx, span 2");

		// celestial body

		l1 = new Label(screen);
		l1.setText("Celestial Body");
		el.addChild(l1, "growx 0");
		celestialBodyMap = new ImageFieldControl(screen, null, imageResources, prefs) {
			@Override
			protected void onResourceChosen(String newResource) {
				environmentConfiguration.setCelestialBodyMap(environmentConfiguration.relativize(newResource));
			}
		};
		celestialBodyMap.setLabel(l1);
		el.addChild(celestialBodyMap, "growx");

		// Celestial body scale
		l1 = new Label(screen);
		l1.setText("Celestial Body Scale");
		el.addChild(l1);
		celestialBodyScale = new Spinner(screen, Orientation.HORIZONTAL, true) {
			@Override
			public void onChange(Object value) {
				environmentConfiguration.setCelestialBodyScale((Float) value);
			}
		};
		celestialBodyScale.setLabel(l1);
		celestialBodyScale.setSpinnerModel(new FloatRangeSpinnerModel(0, 16, 0.25f, 0));
		celestialBodyScale.setFormatterString("%1.2f");
		el.addChild(celestialBodyScale, "growx");
		// Sky Map wrap
		l1 = new Label(screen);
		l1.setText("Celestial Body Wrap Mode");
		el.addChild(l1);
		celestialBodyWrap = new ComboBox<WrapMode>(screen) {

			@Override
			public void onChange(int selectedIndex, WrapMode value) {
				environmentConfiguration.setCelestialBodyWrap((WrapMode) value);

			}
		};
		celestialBodyWrap.setLabel(l1);
		for (WrapMode wm : WrapMode.values()) {
			celestialBodyWrap.addListItem(Icelib.toEnglish(wm), wm);
		}
		el.addChild(celestialBodyWrap, "growx");

		// clouds
		l1 = new Label(screen);
		l1.setText("Clouds Map");
		el.addChild(l1, "growx 0");
		cloudsMapOption = new ImageFieldControl(screen, null, imageResources, prefs) {
			@Override
			protected void onResourceChosen(String newResource) {
				environmentConfiguration.setCloudsMap(environmentConfiguration.relativize(newResource));
			}
		};
		cloudsMapOption.setLabel(l1);
		el.addChild(cloudsMapOption, "growx");

		// Sky Map body scale
		l1 = new Label(screen);
		l1.setText("Cloud Map Scale");
		el.addChild(l1);
		cloudsMapScale = new Spinner(screen, Orientation.HORIZONTAL, true) {
			@Override
			public void onChange(Object value) {
				environmentConfiguration.setCloudsMapScale((Float) value);
			}
		};
		cloudsMapScale.setLabel(l1);
		cloudsMapScale.setSpinnerModel(new FloatRangeSpinnerModel(0, 16, 0.25f, 0));
		cloudsMapScale.setFormatterString("%1.2f");
		el.addChild(cloudsMapScale, "growx");

		// Clouds Map Use As Map
		cloudsMapUseAsMap = new CheckBox(screen) {
			@Override
			public void onButtonMouseLeftUp(MouseButtonEvent evt, boolean toggled) {
				environmentConfiguration.setCloudsMapUseAsMap(toggled);
			}

		};
		el.addChild(cloudsMapUseAsMap, "growx, span 2");
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

		enableClouds.setIsVisible(useSkyDome);
		celestialBodyRotation.setIsVisible(useSkyDome);
		celestialBodyAlpha.setIsVisible(useSkyDome);
		celestialBodyScale.setIsVisible(useSkyDome);
		celestialBodyWrap.setIsVisible(useSkyDome);
		skyMapAlpha.setIsVisible(useSkyDome);
		transitionSpeed.setIsVisible(useSkyDome);
		celestialBodySpeed.setIsVisible(useSkyDome);
		cloudCycleSpeed.setIsVisible(useSkyDome);
		cloudFarSpeed.setIsVisible(useSkyDome);
		cloudFarRotation.setIsVisible(useSkyDome);
		cloudNearSpeed.setIsVisible(useSkyDome);
		cloudNearRotation.setIsVisible(useSkyDome);
		cloudMinOpacity.setIsVisible(useSkyDome);
		cloudMaxOpacity.setIsVisible(useSkyDome);
		cloudsMapUseAsMap.setIsVisible(useSkyDome);
		skyMapUseAsMap.setIsVisible(useSkyDome);
		skyMapWrap.setIsVisible(useSkyDome);
		cloudsMapScale.setIsVisible(useSkyDome);
		skyMapScale.setIsVisible(useSkyDome);

		skyColor.setIsVisible(useSkyDome);
		skyShade.setIsVisible(useSkyDome && (!controlFogEnabled || !fog));
		viewportColor.setIsVisible(!useSkyDome);

		fogColor.setIsVisible(fog || (controlFogEnabled && useSkyDome));
		controlFog.setIsVisible(useSkyDome);
		fogDensity.setIsVisible(fog || (controlFogEnabled && useSkyDome));
		fogDistance.setIsVisible(fog || (controlFogEnabled && useSkyDome));

		weatherIntensity.setIsVisible(weather);
		weatherDensity.setIsVisible(weather);
		wind.setIsVisible(weather);

		directionalLight.setIsVisible(directional);
		ambientLight.setIsVisible(ambient);
		startDirectionalAngle.setIsVisible(useSkyDome && directional);
		endDirectionalAngle.setIsVisible(useSkyDome && directional);
		controlDirectionalPosition.setIsVisible(useSkyDome && directional);

		fogAlphaMapOption.setIsVisible(useSkyDome);
		celestialBodyMap.setIsVisible(useSkyDome);
		skyMapOption.setIsVisible(useSkyDome);
		cloudsMapOption.setIsVisible(useSkyDome);

		// environmentEditWindow.getContentArea().getLayoutManager().layout(environmentEditWindow.getContentArea());
		// environmentEditWindow.getLayoutManager().layout(environmentEditWindow);

	}

	public final void rebuild() {
		adjusting = true;

		enableSkyDome.setIsCheckedNoCallback(environmentConfiguration.isSkyDome());
		enableClouds.setIsCheckedNoCallback(environmentConfiguration.isClouds());
		celestialBodyAlpha.setSelectedValue(environmentConfiguration.getCelestialBodyAlpha());
		celestialBodyScale.setSelectedValue(environmentConfiguration.getCelestialBodyScale());
		celestialBodyRotation.setSelectedValue(environmentConfiguration.getCelestialBodyDirection());
		celestialBodySpeed.setSelectedValue(environmentConfiguration.getCelestialBodySpeed());
		celestialBodyWrap.setSelectedByValue(environmentConfiguration.getCelestialBodyWrap(), false);
		skyMapAlpha.setSelectedValue(environmentConfiguration.getSkyMapAlpha());
		skyMapWrap.setSelectedByValue(environmentConfiguration.getSkyMapWrap(), false);
		skyMapScale.setSelectedValue(environmentConfiguration.getSkyMapScale());
		skyMapUseAsMap.setIsCheckedNoCallback(environmentConfiguration.isSkyMapUseAsMap());
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
		controlFog.setIsCheckedNoCallback(environmentConfiguration.isControlFog());
		enableFog.setIsCheckedNoCallback(environmentConfiguration.isFog());
		fogDistance.setSelectedValue(environmentConfiguration.getFogDistance());
		fogDensity.setSelectedValue(environmentConfiguration.getFogDensity());
		fogColor.setValue(environmentConfiguration.getFogColor());
		outlook.setSelectedByValue(environmentConfiguration.getWeather(), false);
		weatherIntensity.setSelectedValue(environmentConfiguration.getWeatherIntensity());
		weatherDensity.setSelectedValue(environmentConfiguration.getWeatherDensity());
		wind.setIsCheckedNoCallback(environmentConfiguration.isWind());
		weatherMusic.setValue(Icelib.nonNull(environmentConfiguration.getPlaylist(PlaylistType.AMBIENT_NOISE)));
		weatherMusicGain.setSelectedValue(environmentConfiguration.getWeatherMusicGain());

		ambientEnabled.setIsCheckedNoCallback(environmentConfiguration.isAmbientEnabled());
		ambientLight.setValue(environmentConfiguration.getAmbientColor());
		directionalEnabled.setIsCheckedNoCallback(environmentConfiguration.isDirectionalEnabled());
		directionalLight.setValue(environmentConfiguration.getDirectionalColor());
		controlDirectionalPosition.setIsCheckedNoCallback(environmentConfiguration.isControlDirectionalPosition());
		startDirectionalAngle.setSelectedValue(environmentConfiguration.getStartDirectionalAngle());
		endDirectionalAngle.setSelectedValue(environmentConfiguration.getEndDirectionalAngle());

		ambientMusic.setAudio(environmentConfiguration.getPlaylist(PlaylistType.AMBIENT_MUSIC));
		activateMusic.setValue(Icelib.nonNull(environmentConfiguration.getPlaylist(PlaylistType.AMBIENT_MUSIC)));
		// music.setValue(Icelib.nonNull(environmentConfiguration.getMusic()));
		ambientMusicDelay.setSelectedValue(environmentConfiguration.getAmbientMusicDelay());
		activateMusicDelay.setSelectedValue(environmentConfiguration.getAmbientMusicDelay());

		cloudsMapOption.setValue(environmentConfiguration.absolutize(environmentConfiguration.getCloudsMap()));
		cloudsMapScale.setSelectedValue(environmentConfiguration.getCloudsMapScale());
		cloudsMapUseAsMap.setIsCheckedNoCallback(environmentConfiguration.isCloudsMapUseAsMap());
		celestialBodyMap.setValue(environmentConfiguration.absolutize(environmentConfiguration.getCelestialBodyMap()));
		fogAlphaMapOption.setValue(environmentConfiguration.absolutize(environmentConfiguration.getFogAlphaMap()));
		skyMapOption.setValue(environmentConfiguration.absolutize(environmentConfiguration.getSkyMap()));

		adjusting = false;
		checkVisible();
	}
}

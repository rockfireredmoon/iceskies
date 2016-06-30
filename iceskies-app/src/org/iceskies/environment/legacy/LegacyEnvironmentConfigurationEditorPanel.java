package org.iceskies.environment.legacy;

import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;

import org.apache.commons.io.FilenameUtils;
import org.icelib.UndoManager;
import org.icescene.SceneConstants;
import org.icescene.audio.AudioQueue;
import org.icescene.ui.Playlist;
import org.iceskies.environment.AbstractEnvironmentConfiguration;
import org.iceskies.environment.AbstractEnvironmentConfigurationEditorPanel;
import org.iceskies.environment.EnvironmentManager;
import org.iceskies.environment.PlaylistType;
import org.iceui.controls.ChooserFieldControl;
import org.iceui.controls.ChooserFieldControl.ChooserPathTranslater;
import org.iceui.controls.ElementStyle;
import org.iceui.controls.FancyButton;
import org.iceui.controls.ItemList;
import org.iceui.controls.MaterialFieldControl;
import org.iceui.controls.SoundFieldControl.Type;
import org.iceui.controls.color.ColorFieldControl;

import com.jme3.input.event.MouseButtonEvent;
import com.jme3.material.MaterialList;
import com.jme3.math.ColorRGBA;

import icemoon.iceloader.ServerAssetManager;
import icetone.controls.buttons.CheckBox;
import icetone.controls.form.Form;
import icetone.controls.lists.ComboBox;
import icetone.controls.lists.FloatRangeSpinnerModel;
import icetone.controls.lists.IntegerRangeSpinnerModel;
import icetone.controls.lists.Spinner;
import icetone.controls.text.Label;
import icetone.controls.windows.TabControl;
import icetone.core.Container;
import icetone.core.Element;
import icetone.core.ElementManager;
import icetone.core.layout.FillLayout;
import icetone.core.layout.mig.MigLayout;

public class LegacyEnvironmentConfigurationEditorPanel
		extends AbstractEnvironmentConfigurationEditorPanel<LegacyEnvironmentConfiguration> {
	
	enum FogType {
		INHERIT, ENABLED, DISABLED
	}

	private boolean adjusting;
	private ColorFieldControl ambient;
	private ColorFieldControl sun;
	private ComboBox<FogType> fogType;
	private Spinner<Float> start;
	private ColorFieldControl fogColor;
	private Spinner<Float> end;
	private Spinner<Float> exp;
	private CheckBox excludeSky;
	private Spinner<Float> blendTime;
	private Playlist playlist;
	private Set<String> musicResources;
	private Set<String> soundResources;
	private ComboBox<PlaylistType> playlistType;
	private String dir = SceneConstants.MUSIC_PATH;
	private Spinner<Integer> delay;
	private Spinner<Integer> cooldown;
	private ItemList<String, ChooserFieldControl> sky;
	private Set<String> skyResources;
	private ComboBox<LegacyEnvironmentConfiguration> delegate;

	public LegacyEnvironmentConfigurationEditorPanel(UndoManager undoManager, ElementManager screen, Preferences prefs,
			LegacyEnvironmentConfiguration environmentConfiguration) {
		super(undoManager, screen, prefs, environmentConfiguration);

		musicResources = ((ServerAssetManager) screen.getApplication().getAssetManager())
				.getAssetNamesMatching(SceneConstants.MUSIC_PATH + "/.*\\.ogg");
		soundResources = ((ServerAssetManager) screen.getApplication().getAssetManager())
				.getAssetNamesMatching(SceneConstants.SOUND_PATH + "/.*\\.ogg");
		skyResources = ((ServerAssetManager) screen.getApplication().getAssetManager())
				.getAssetNamesMatching(SceneConstants.MATERIALS_PATH + "/Env-.*\\.j3m");

		setLayoutManager(new FillLayout());

		TabControl envTabs = new TabControl(screen);
		envTabs.setUseSlideEffect(true);

		adjusting = true;
		lightTab(envTabs, 0);
		skyTab(envTabs, 1);
		audioTab(envTabs, 2);
		adjusting = false;

		addChild(envTabs);
		rebuild();
	}

	private Element createTabPanel() {
		Element el = new Container(screen);
		el.setLayoutManager(new MigLayout(screen, "hidemode 2, wrap 2", "[grow, fill][grow]"));
		return el;
	}

	private void audioTab(TabControl tabs, int tabIndex) {
		Element el = createTabPanel();

		Form f = new Form(screen);

		//
		el.addChild(ElementStyle.medium(screen, new Label("Playlist", screen)), "wrap 1, growx");

		Label l1 = new Label("Type", screen);
		el.addChild(l1, "");
		playlistType = new ComboBox<PlaylistType>(screen, PlaylistType.values()) {

			@Override
			public void onChange(int selectedIndex, PlaylistType value) {
				if (!adjusting) {
					setAudioForPlaylist();
					checkVisible();
				}
			}
		};
		playlistType.setLabel(l1);
		el.addChild(playlistType, "growx");

		playlist = new Playlist(screen, Type.RESOURCE, AudioQueue.MUSIC, prefs, musicResources) {
			@Override
			protected void onAudioChanged(List<String> audio) {
				PlaylistType pl = getSelectedPlaylistType();
				if (undoManager == null) {
					setAudioList(environmentConfiguration, pl, audio);
				} else {
					undoManager.storeAndExecute(new SetAudioCommand(pl, environmentConfiguration, audio,
							LegacyEnvironmentConfigurationEditorPanel.this.getAudio(pl)));
				}
			}
		};
		playlist.setChooserPathTranslater(new ChooserPathTranslater() {

			@Override
			public String getValueForChooserPath(String chooserPath) {
				return FilenameUtils.getName(chooserPath);
			}

			@Override
			public String getChooserPathForValue(String value) {
				return dir + "/" + FilenameUtils.getBaseName(value) + "/" + value;
			}
		});
		el.addChild(playlist, "span 2");

		l1 = new Label(screen);
		l1.setText("Delay");
		el.addChild(l1, "");
		delay = new Spinner<Integer>(screen, Orientation.HORIZONTAL, false) {
			@Override
			public void onChange(Integer value) {
				if (!adjusting) {
					PlaylistType pt = getSelectedPlaylistType();
					if (undoManager == null) {
						switch (pt) {
						case ACTIVATE_MUSIC:
							environmentConfiguration.setActivateMusicDelay(value);
							break;
						case AMBIENT_MUSIC:
							environmentConfiguration.setAmbientMusicDelay(value);
							break;
						default:
							break;
						}
					} else {
						undoManager.storeAndExecute(new SetAudioDelayCommand(pt, environmentConfiguration, value,
								pt == PlaylistType.ACTIVATE_MUSIC ? environmentConfiguration.getActivateMusicDelay()
										: environmentConfiguration.getAmbientMusicDelay()));
					}
				}
			}
		};
		delay.setLabel(l1);
		delay.setSpinnerModel(
				new IntegerRangeSpinnerModel(0, Integer.MAX_VALUE, 1, environmentConfiguration.getAmbientMusicDelay()));
		delay.setInterval(50);
		el.addChild(delay, "growx");
		f.addFormElement(delay);

		l1 = new Label(screen);
		l1.setText("Cooldown");
		el.addChild(l1, "");
		cooldown = new Spinner<Integer>(screen, Orientation.HORIZONTAL, false) {
			@Override
			public void onChange(Integer value) {
				if (!adjusting) {
					if (undoManager == null) {
						environmentConfiguration.setActivateMusicCooldown(value);
					} else {
						undoManager.storeAndExecute(new SetAudioCooldownCommand(environmentConfiguration, value,
								environmentConfiguration.getActivateMusicCooldown()));
					}
				}
			}
		};
		cooldown.setLabel(l1);
		cooldown.setSpinnerModel(new IntegerRangeSpinnerModel(0, Integer.MAX_VALUE, 1, 0));
		cooldown.setInterval(50);
		el.addChild(cooldown, "growx");
		f.addFormElement(cooldown);
		tabs.addTab("Audio", el);
	}

	protected void setAudioList(LegacyEnvironmentConfiguration environmentConfiguration, PlaylistType type, List<String> audio) {
		environmentConfiguration.setPlaylist(type, audio);
	}

	private void skyTab(TabControl tabs, int tabIndex) {
		Element el = createTabPanel();
		Form f = new Form(screen);
		sky = new SkyList(screen);
		f.addFormElement(sky);
		el.addChild(sky, "span 2");
		tabs.addTab("Sky", el);
	}

	private void lightTab(TabControl tabs, int tabIndex) {
		Element el = createTabPanel();
		Form f = new Form(screen);

		Label l1 = new Label(screen);
		l1.setText("Delegate");
		el.addChild(l1, "");
		delegate = new ComboBox<LegacyEnvironmentConfiguration>(screen) {
			@Override
			protected void onChange(int selectedIde, LegacyEnvironmentConfiguration value) {
				if (!adjusting) {
					if (undoManager == null)
						environmentConfiguration.setDelegate(value);
					else
						undoManager.storeAndExecute(
								new SetDelegateCommand(environmentConfiguration, value, environmentConfiguration.getDelegate()));
				}
			}
		};
		delegate.setLabel(l1);
		EnvironmentManager environmentManager = EnvironmentManager.get(app.getAssetManager());
		for (String c : environmentManager.getEnvironmentConfigurations()) {
			AbstractEnvironmentConfiguration cfg = environmentManager.getEnvironmentConfiguration(c);
			if (cfg instanceof LegacyEnvironmentConfiguration) {
				delegate.addListItem(c, (LegacyEnvironmentConfiguration) cfg);
			}
		}
		el.addChild(delegate, "growx");
		f.addFormElement(delegate);

		l1 = new Label(screen);
		l1.setText("Blend Time");
		el.addChild(l1, "");
		blendTime = new Spinner<Float>(screen, Orientation.HORIZONTAL, false) {
			@Override
			public void onChange(Float value) {
				if (!adjusting) {
					if (undoManager == null)
						environmentConfiguration.setBlendTime(value);
					else
						undoManager.storeAndExecute(
								new SetBlendTimeCommand(environmentConfiguration, value, environmentConfiguration.getBlendTime()));
				}
			}
		};
		blendTime.setLabel(l1);
		blendTime.setSpinnerModel(new FloatRangeSpinnerModel(0, 100, 0.1f, 0));
		blendTime.setFormatterString("%.3f");
		blendTime.setInterval(50);
		el.addChild(blendTime, "growx");
		f.addFormElement(blendTime);

		// Ambient light
		l1 = new Label("Ambient", screen);
		el.addChild(l1);
		ambient = new ColorFieldControl(screen, ColorRGBA.White) {
			@Override
			protected void onChangeColor(ColorRGBA newColor) {
				if (undoManager == null)
					environmentConfiguration.setAmbient(newColor);
				else
					undoManager.storeAndExecute(
							new SetAmbientCommand(environmentConfiguration, newColor, environmentConfiguration.getAmbient()));
			}
		};
		ambient.setLabel(l1);
		f.addFormElement(ambient);
		el.addChild(ambient, "growx");

		// Directional light
		l1 = new Label("Sun", screen);
		el.addChild(l1);
		sun = new ColorFieldControl(screen, ColorRGBA.White) {
			@Override
			protected void onChangeColor(ColorRGBA newColor) {
				if (undoManager == null)
					environmentConfiguration.setSun(newColor);
				else
					undoManager.storeAndExecute(
							new SetSunCommand(environmentConfiguration, newColor, environmentConfiguration.getAmbient()));
			}
		};
		sun.setLabel(l1);
		f.addFormElement(sun);
		el.addChild(sun, "growx");

		//
		el.addChild(ElementStyle.medium(screen, new Label("Fog", screen)), "wrap 1, growx");

		// Enable Fog
		el.addChild(new Label("Mode", screen));
		fogType = new ComboBox<FogType>(screen, FogType.values()) {
			@Override
			protected void onChange(int selectedIndex, FogType value) {
				if (!adjusting) {
					if (undoManager == null) {
						setFogType(value);
					} else {
						undoManager.storeAndExecute(
								new SetFogCommand(environmentConfiguration, value, getFogType()));
					}
				}
			}
			
		};
		el.addChild(fogType, "growx");
		f.addFormElement(fogType);
		
		// Exclude Sky
		excludeSky = new CheckBox(screen) {
			@Override
			public void onButtonMouseLeftUp(MouseButtonEvent evt, boolean toggled) {
				if (!adjusting) {
					if (undoManager == null) {
						environmentConfiguration.getFog().setExcludeSky(toggled);
						checkVisible();
					} else {
						undoManager.storeAndExecute(new SetExcludeSkyCommand(environmentConfiguration, toggled,
								environmentConfiguration.getFog() != null && environmentConfiguration.getFog().isExcludeSky()));
					}
				}
			}
		};
		excludeSky.setLabelText("Exclude Sky");
		el.addChild(excludeSky, "span 2, growx");
		f.addFormElement(excludeSky);

		// Fog Start
		l1 = new Label(screen);
		l1.setText("Start");
		el.addChild(l1, "gapleft 20");
		start = new Spinner<Float>(screen, Orientation.HORIZONTAL, false) {
			@Override
			public void onChange(Float value) {
				if (!adjusting)
					if (undoManager == null)
						environmentConfiguration.getFog().setStart(value);
					else
						undoManager.storeAndExecute(new SetFogStartCommand(environmentConfiguration, value,
								environmentConfiguration.getFog().getEnd()));

			}
		};
		start.setLabel(l1);
		start.setSpinnerModel(new FloatRangeSpinnerModel(0, 1, 0.0001f, 0));
		start.setFormatterString("%f");
		start.setInterval(100000);
		el.addChild(start, "growx");
		f.addFormElement(start);

		// Fog End
		l1 = new Label("End", screen);
		el.addChild(l1, "gapleft 20");
		end = new Spinner<Float>(screen, Orientation.HORIZONTAL, false) {
			@Override
			public void onChange(Float value) {
				if (!adjusting) {
					if (undoManager == null)
						environmentConfiguration.getFog().setEnd(value);
					else
						undoManager.storeAndExecute(
								new SetFogEndCommand(environmentConfiguration, value, environmentConfiguration.getFog().getEnd()));
				}
			}
		};
		end.setLabel(l1);
		end.setSpinnerModel(new FloatRangeSpinnerModel(0, 1, 0.0001f, 0));
		end.setFormatterString("%f");
		end.setInterval(100000);
		el.addChild(end, "growx");
		f.addFormElement(end);

		// Fog Exponential (sort of)
		l1 = new Label(screen);
		l1.setText("Exp");
		el.addChild(l1, "gapleft 20");
		exp = new Spinner<Float>(screen, Orientation.HORIZONTAL, false) {
			@Override
			public void onChange(Float value) {
				if (!adjusting)
					if (undoManager == null)
						environmentConfiguration.getFog().setExp(value);
					else
						undoManager.storeAndExecute(
								new SetFogExpCommand(environmentConfiguration, value, environmentConfiguration.getFog().getExp()));
			}
		};
		exp.setLabel(l1);
		exp.setSpinnerModel(new FloatRangeSpinnerModel(0, 1, 0.0001f, 0));
		exp.setFormatterString("%.4f");
		exp.setInterval(500);
		el.addChild(exp, "growx");
		f.addFormElement(exp);

		// Fog color
		l1 = new Label("Color", screen);
		el.addChild(l1, "gapleft 20");
		fogColor = new ColorFieldControl(screen, ColorRGBA.White) {
			@Override
			protected void onChangeColor(ColorRGBA newColor) {
				if (!adjusting)
					if (undoManager == null)
						environmentConfiguration.getFog().setColor(newColor);
					else
						undoManager.storeAndExecute(new SetFogColourCommand(environmentConfiguration, newColor,
								environmentConfiguration.getFog().getColor()));

			}
		};
		fogColor.setLabel(l1);
		el.addChild(fogColor, "growx");
		f.addFormElement(fogColor);
		tabs.addTab("Environment", el);

	}
	
	protected FogType getFogType() {
		return environmentConfiguration.getFog() == null ? FogType.INHERIT : ( environmentConfiguration.getFog().isEnabled() ? FogType.ENABLED : FogType.DISABLED);
	}

	protected void setFogType(FogType value) {
		switch(value) {
		case INHERIT:
			environmentConfiguration.setFog(null);
			break;
		case ENABLED:
		case DISABLED:
			if(environmentConfiguration.getFog() == null) {
				LegacyFogConfig fog = new LegacyFogConfig();
				fog.setColor(fogColor.getValue());
				fog.setStart(((Number) start.getSpinnerModel().getCurrentValue()).floatValue());
				fog.setEnd(((Number) end.getSpinnerModel().getCurrentValue()).floatValue());
				fog.setExp(((Number) exp.getSpinnerModel().getCurrentValue()).floatValue());
				environmentConfiguration.setFog(fog);
			}
			environmentConfiguration.getFog().setEnabled(value == FogType.ENABLED);
			break;
		}
		checkVisible();
	}

	private List<String> getAudio(PlaylistType type) {
		return environmentConfiguration.getPlaylist(type);
	}

	private void setAudioForPlaylist() {
		PlaylistType type = getSelectedPlaylistType();
		playlist.setAudio(getAudio(type));
		switch (type) {
		case ACTIVATE_MUSIC:
			dir = SceneConstants.MUSIC_PATH;
			delay.setSelectedValue(environmentConfiguration.getActivateMusicDelay());
			cooldown.setSelectedValue(environmentConfiguration.getActivateMusicCooldown());
			playlist.setResources(musicResources);
			break;
		case AMBIENT_MUSIC:
			delay.setSelectedValue(environmentConfiguration.getAmbientMusicDelay());
			cooldown.setSelectedValue(0);
			dir = SceneConstants.MUSIC_PATH;
			playlist.setResources(musicResources);
			break;
		default:
			delay.setSelectedValue(0);
			cooldown.setSelectedValue(0);
			dir = SceneConstants.SOUND_PATH;
			playlist.setResources(soundResources);
			break;
		}
	}

	protected PlaylistType getSelectedPlaylistType() {
		return (PlaylistType) playlistType.getSelectedListItem().getValue();
	}

	private void checkVisible() {
		FogType ft = getFogType();
		start.setIsEnabled(ft == FogType.ENABLED);
		end.setIsEnabled(ft == FogType.ENABLED);
		exp.setIsEnabled(ft == FogType.ENABLED);
		fogColor.setIsEnabled(ft == FogType.ENABLED);
		excludeSky.setIsEnabled(ft == FogType.ENABLED);
		delay.setIsEnabled(getSelectedPlaylistType() == PlaylistType.ACTIVATE_MUSIC
				|| getSelectedPlaylistType() == PlaylistType.AMBIENT_MUSIC);
		cooldown.setIsEnabled(getSelectedPlaylistType() == PlaylistType.ACTIVATE_MUSIC);
	}

	public final void rebuild() {
		adjusting = true;
		delegate.setSelectedByValue(environmentConfiguration.getDelegate(), false);
		ambient.setValue(environmentConfiguration.getAmbient());
		sun.setValue(environmentConfiguration.getSun());
		blendTime.setSelectedValue(environmentConfiguration.getBlendTime());
		LegacyFogConfig fog = environmentConfiguration.getFog();
		if (fog == null) {
			fogColor.setValue(ColorRGBA.White);
			start.setSelectedValue(0f);
			end.setSelectedValue(0f);
			exp.setSelectedValue(0f);
			excludeSky.setIsChecked(true);
		} else {
			fogType.setSelectedByValue(getFogType(), false);
			fogColor.setValue(fog.getColor());
			start.setSelectedValue(fog.getStart());
			end.setSelectedValue(fog.getEnd());
			exp.setSelectedValue(fog.getExp());
			excludeSky.setIsChecked(fog.isExcludeSky());
		}
		setAudioForPlaylist();
		sky.setValues(environmentConfiguration.getSky());

		adjusting = false;
		checkVisible();
	}

	@SuppressWarnings("serial")
	abstract class AbstractEnvironmentCommand<N> implements UndoManager.UndoableCommand {
		@SuppressWarnings("unused")
		private LegacyEnvironmentConfiguration environmentConfiguration;
		private N o;
		private N n;

		AbstractEnvironmentCommand(LegacyEnvironmentConfiguration environmentConfiguration, N n, N o) {
			this.environmentConfiguration = environmentConfiguration;
			this.o = o;
			this.n = n;
		}

		public final void undoCommand() {
			set(o);
		}

		public final void doCommand() {
			set(n);
		}

		abstract void set(N n);
	}

	@SuppressWarnings("serial")
	class SetAmbientCommand extends AbstractEnvironmentCommand<ColorRGBA> {

		public SetAmbientCommand(LegacyEnvironmentConfiguration environmentConfiguration, ColorRGBA colour, ColorRGBA oldColour) {
			super(environmentConfiguration, colour, oldColour);
		}

		@Override
		protected void set(ColorRGBA rgba) {
			environmentConfiguration.setAmbient(rgba);
			ambient.setValue(rgba);
		}

	}

	@SuppressWarnings("serial")
	class SetSunCommand extends AbstractEnvironmentCommand<ColorRGBA> {

		public SetSunCommand(LegacyEnvironmentConfiguration environmentConfiguration, ColorRGBA colour, ColorRGBA oldColour) {
			super(environmentConfiguration, colour, oldColour);
		}

		@Override
		protected void set(ColorRGBA rgba) {
			environmentConfiguration.setSun(rgba);
			sun.setValue(rgba);
		}

	}

	@SuppressWarnings("serial")
	class SetAudioCommand extends AbstractEnvironmentCommand<List<String>> {

		private PlaylistType playlistType;

		public SetAudioCommand(PlaylistType playlistType, LegacyEnvironmentConfiguration environmentConfiguration,
				List<String> audio, List<String> oldAudio) {
			super(environmentConfiguration, audio, oldAudio);
			this.playlistType = playlistType;
		}

		protected void set(List<String> a) {
			setAudioList(environmentConfiguration, playlistType, a);
			setAudioForPlaylist();
		}
	}

	@SuppressWarnings("serial")
	class SetFogCommand extends AbstractEnvironmentCommand<FogType> {

		public SetFogCommand(LegacyEnvironmentConfiguration environmentConfiguration, FogType newVal, FogType oldVal) {
			super(environmentConfiguration, newVal, oldVal);
		}

		@Override
		void set(FogType n) {
			setFogType(n);
			fogType.setSelectedByValue(n, false);
//			boolean isChecked = n && environmentConfiguration.getFog().isExcludeSky();
//			excludeSky.setColorMap(screen.getStyle("CheckBox").getString("defaultImg"));
//			excludeSky.setIsCheckedNoCallback(isChecked);
			checkVisible();
		}
	}

	@SuppressWarnings("serial")
	class SetExcludeSkyCommand extends AbstractEnvironmentCommand<Boolean> {

		public SetExcludeSkyCommand(LegacyEnvironmentConfiguration environmentConfiguration, boolean newVal, boolean oldVal) {
			super(environmentConfiguration, newVal, oldVal);
		}

		@Override
		void set(Boolean n) {
			if (environmentConfiguration.getFog() != null)
				environmentConfiguration.getFog().setExcludeSky(n);
			System.out.println("Setting es: " + n + " (was " + excludeSky.getIsChecked() + ")");
			excludeSky.setIsCheckedNoCallback(n);
			checkVisible();
		}
	}

	@SuppressWarnings("serial")
	class SetBlendTimeCommand extends AbstractEnvironmentCommand<Float> {

		public SetBlendTimeCommand(LegacyEnvironmentConfiguration environmentConfiguration, float newVal, float oldVal) {
			super(environmentConfiguration, newVal, oldVal);
		}

		@Override
		void set(Float n) {
			environmentConfiguration.setBlendTime(n);
			blendTime.setSelectedValue(n);
		}
	}

	@SuppressWarnings("serial")
	class SetFogStartCommand extends AbstractEnvironmentCommand<Float> {

		public SetFogStartCommand(LegacyEnvironmentConfiguration environmentConfiguration, float newVal, float oldVal) {
			super(environmentConfiguration, newVal, oldVal);
		}

		@Override
		void set(Float n) {
			environmentConfiguration.getFog().setStart(n);
			start.setSelectedValue(n);
		}
	}

	@SuppressWarnings("serial")
	class SetFogEndCommand extends AbstractEnvironmentCommand<Float> {

		public SetFogEndCommand(LegacyEnvironmentConfiguration environmentConfiguration, float newVal, float oldVal) {
			super(environmentConfiguration, newVal, oldVal);
		}

		@Override
		void set(Float n) {
			environmentConfiguration.getFog().setEnd(n);
			end.setSelectedValue(n);
		}
	}

	@SuppressWarnings("serial")
	class SetFogExpCommand extends AbstractEnvironmentCommand<Float> {

		public SetFogExpCommand(LegacyEnvironmentConfiguration environmentConfiguration, float newVal, float oldVal) {
			super(environmentConfiguration, newVal, oldVal);
		}

		@Override
		void set(Float n) {
			environmentConfiguration.getFog().setExp(n);
			exp.setSelectedValue(n);
		}
	}

	@SuppressWarnings("serial")
	class SetFogColourCommand extends AbstractEnvironmentCommand<ColorRGBA> {

		public SetFogColourCommand(LegacyEnvironmentConfiguration environmentConfiguration, ColorRGBA colour, ColorRGBA oldColour) {
			super(environmentConfiguration, colour, oldColour);
		}

		@Override
		protected void set(ColorRGBA rgba) {
			environmentConfiguration.getFog().setColor(rgba);
			fogColor.setValue(rgba);
		}

	}

	@SuppressWarnings("serial")
	class SetAudioDelayCommand extends AbstractEnvironmentCommand<Integer> {

		private PlaylistType type;

		public SetAudioDelayCommand(PlaylistType type, LegacyEnvironmentConfiguration environmentConfiguration, int newVal,
				int oldVal) {
			super(environmentConfiguration, newVal, oldVal);
			this.type = type;
		}

		@Override
		void set(Integer n) {
			if (type == PlaylistType.ACTIVATE_MUSIC)
				environmentConfiguration.setActivateMusicDelay(n);
			else
				environmentConfiguration.setAmbientMusicDelay(n);
			delay.setSelectedValue(n);
		}
	}

	@SuppressWarnings("serial")
	class SetAudioCooldownCommand extends AbstractEnvironmentCommand<Integer> {

		public SetAudioCooldownCommand(LegacyEnvironmentConfiguration environmentConfiguration, int newVal, int oldVal) {
			super(environmentConfiguration, newVal, oldVal);
		}

		@Override
		void set(Integer n) {
			environmentConfiguration.setActivateMusicCooldown(n);
			cooldown.setSelectedValue(n);
		}
	}

	@SuppressWarnings("serial")
	class SetDelegateCommand extends AbstractEnvironmentCommand<LegacyEnvironmentConfiguration> {

		public SetDelegateCommand(LegacyEnvironmentConfiguration environmentConfiguration, LegacyEnvironmentConfiguration val,
				LegacyEnvironmentConfiguration oldVal) {
			super(environmentConfiguration, val, oldVal);
		}

		protected void set(LegacyEnvironmentConfiguration a) {
			environmentConfiguration.setDelegate(a);
			delegate.setSelectedByValue(a, false);
		}
	}

	@SuppressWarnings("serial")
	class SetSkyCommand extends AbstractEnvironmentCommand<List<String>> {

		public SetSkyCommand(LegacyEnvironmentConfiguration environmentConfiguration, List<String> val, List<String> oldVal) {
			super(environmentConfiguration, val, oldVal);
		}

		protected void set(List<String> a) {
			environmentConfiguration.setSky(a);
			sky.setValues(a);
		}
	}

	public final class SkyList extends ItemList<String, ChooserFieldControl> implements ChooserPathTranslater {
		public SkyList(ElementManager screen) {
			super(screen, prefs, skyResources);
		}

		@Override
		protected ChooserFieldControl createChooser() {

			MaterialFieldControl materialFieldControl = new MaterialFieldControl(screen, null, skyResources, prefs) {
				@Override
				protected void createChooserButton() {
					chooserButton = new FancyButton(screen) {
						@Override
						public void onButtonMouseLeftUp(MouseButtonEvent evt, boolean toggled) {
							showChooser(evt.getX(), evt.getY());
						}
					};
					chooserButton.getMinDimensions().x = 64;
					chooserButton.setButtonIcon(16, 16, "Interface/Styles/Gold/Common/Icons/edit.png");
					chooserButton.setToolTipText("Edit Item");
					addChild(chooserButton, "wrap");

				}

				@Override
				protected void onResourceChosen(String newResource) {
					List<String> existing = getValues();
					String name = FilenameUtils.getName(newResource);
					if (row == -1) {
						if (!existing.contains(name))
							addChoice(name);
					} else {
						items.getRow(row).setValue(newResource);
						row = 1;
					}
					onValuesChanged(getValues());
					setAvailable();
				}

				@Override
				public MaterialList getMaterialList(String path) {
					return null;
				}
			};
			materialFieldControl.setChooserPathTranslater(this);
			return materialFieldControl;
		}

		@Override
		protected void onValuesChanged(List<String> values) {
			if (!adjusting) {
				if (undoManager == null) {
					environmentConfiguration.setSky(values);
				} else {
					undoManager.storeAndExecute(
							new SetSkyCommand(environmentConfiguration, values, environmentConfiguration.getSky()));
				}
			}
		}

		@Override
		public String getChooserPathForValue(String value) {
			return SceneConstants.MATERIALS_PATH + "/Env-" + value + ".j3m";
		}

		@Override
		public String getValueForChooserPath(String chooserPath) {
			return FilenameUtils.getBaseName(chooserPath).substring(4);
		}
	}

}

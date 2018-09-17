package org.iceskies.environment.legacy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.prefs.Preferences;

import org.apache.commons.io.FilenameUtils;
import org.icescene.SceneConstants;
import org.icescene.audio.AudioQueue;
import org.icescene.ui.Playlist;
import org.iceskies.environment.AbstractEnvironmentConfiguration;
import org.iceskies.environment.AbstractEnvironmentConfigurationEditorPanel;
import org.iceskies.environment.EnvironmentManager;
import org.iceskies.environment.PlaylistType;
import org.iceskies.environment.legacy.LegacyEnvironmentConfiguration.AdjustChannel;
import org.iceui.controls.ChooserFieldControl;
import org.iceui.controls.ChooserFieldControl.ChooserPathTranslater;
import org.iceui.controls.ElementStyle;
import org.iceui.controls.ItemList;
import org.iceui.controls.MaterialFieldControl;
import org.iceui.controls.SoundFieldControl.Type;

import com.jme3.material.MaterialList;
import com.jme3.math.ColorRGBA;

import icemoon.iceloader.ServerAssetManager;
import icetone.controls.buttons.CheckBox;
import icetone.controls.buttons.PushButton;
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
import icetone.core.undo.UndoableCommand;
import icetone.extras.chooser.ColorFieldControl;
import icetone.extras.chooser.StringChooserModel;

public class LegacyEnvironmentConfigurationEditorPanel
		extends AbstractEnvironmentConfigurationEditorPanel<LegacyEnvironmentConfiguration> {

	enum FogType {
		INHERIT, ENABLED, DISABLED
	}

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
	private ComboBox<PlaylistType> playlistType;
	private String dir = SceneConstants.MUSIC_PATH;
	private Spinner<Integer> delay;
	private Spinner<Integer> cooldown;
	private ItemList<String, ChooserFieldControl<String>> sky;
	private Set<String> skyResources;
	private ComboBox<LegacyEnvironmentConfiguration> delegate;
	private Spinner<Float> gain;

	public LegacyEnvironmentConfigurationEditorPanel(UndoManager undoManager, BaseScreen screen, Preferences prefs,
			LegacyEnvironmentConfiguration environmentConfiguration) {
		super(undoManager, screen, prefs, environmentConfiguration);

		musicResources = ((ServerAssetManager) screen.getApplication().getAssetManager())
				.getAssetNamesMatching(SceneConstants.MUSIC_PATH + "/.*\\.ogg");
		skyResources = ((ServerAssetManager) screen.getApplication().getAssetManager())
				.getAssetNamesMatching(SceneConstants.MATERIALS_PATH + "/Env-.*\\.j3m");

		setLayoutManager(new FillLayout());

		TabControl envTabs = new TabControl(screen);
		envTabs.setUseSlideEffect(true);

		lightTab(envTabs, 0);
		skyTab(envTabs, 1);
		audioTab(envTabs, 2);

		addElement(envTabs);
		rebuild();
	}

	private BaseElement createTabPanel() {
		BaseElement el = new StyledContainer(screen);
		el.setLayoutManager(new MigLayout(screen, "hidemode 2, wrap 2", "[grow, fill][grow]"));
		return el;
	}

	private void audioTab(TabControl tabs, int tabIndex) {
		BaseElement el = createTabPanel();

		Form f = new Form(screen);

		//
		el.addElement(ElementStyle.medium(new Label("Playlist", screen)), "wrap 1, growx");

		Label l1 = new Label("Type", screen);
		el.addElement(l1, "");
		playlistType = new ComboBox<PlaylistType>(screen, PlaylistType.values());
		playlistType.onChange(evt -> {
			if (!isAdjusting()) {
				setAudioForPlaylist();
				checkVisible();
			}
		});
		playlistType.setLabel(l1);
		el.addElement(playlistType, "growx");

		playlist = new Playlist(screen, Type.RESOURCE, AudioQueue.MUSIC, prefs,
				new StringChooserModel(musicResources)) {
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

			@Override
			protected void onSelectionChanged(String path) {
				setGainForSelection();
				checkVisible();
			}
		};
		playlist.setChooserPathTranslater(new ChooserPathTranslater<String>() {

			@Override
			public String getValueForChooserPath(String chooserPath) {
				return FilenameUtils.getName(chooserPath);
			}

			@Override
			public String getChooserPathForValue(String value) {
				return dir + "/" + FilenameUtils.getBaseName(value) + "/" + value;
			}
		});
		el.addElement(playlist, "span 2");

		l1 = new Label(screen);
		l1.setText("Delay");
		el.addElement(l1, "");
		delay = new Spinner<Integer>(screen, Orientation.HORIZONTAL, false);
		delay.onChange(evt -> {
			if (!isAdjusting()) {
				PlaylistType pt = getSelectedPlaylistType();
				if (undoManager == null) {
					switch (pt) {
					case ACTIVATE_MUSIC:
						environmentConfiguration.setActivateMusicDelay(evt.getNewValue());
						break;
					case AMBIENT_MUSIC:
						environmentConfiguration.setAmbientMusicDelay(evt.getNewValue());
						break;
					default:
						break;
					}
				} else {
					undoManager
							.storeAndExecute(new SetAudioDelayCommand(pt, environmentConfiguration, evt.getNewValue(),
									pt == PlaylistType.ACTIVATE_MUSIC ? environmentConfiguration.getActivateMusicDelay()
											: environmentConfiguration.getAmbientMusicDelay()));
				}
			}
		});
		delay.setLabel(l1);
		delay.setSpinnerModel(
				new IntegerRangeSpinnerModel(0, Integer.MAX_VALUE, 1, environmentConfiguration.getAmbientMusicDelay()));
		delay.setInterval(50);
		el.addElement(delay, "growx");
		f.addFormElement(delay);

		l1 = new Label(screen);
		l1.setText("Cooldown");
		el.addElement(l1, "");
		cooldown = new Spinner<Integer>(screen, Orientation.HORIZONTAL, false);
		cooldown.onChange(evt -> {
			if (!isAdjusting()) {
				if (undoManager == null) {
					environmentConfiguration.setActivateMusicCooldown(evt.getNewValue());
				} else {
					undoManager.storeAndExecute(new SetAudioCooldownCommand(environmentConfiguration, evt.getNewValue(),
							environmentConfiguration.getActivateMusicCooldown()));
				}
			}
		});
		cooldown.setLabel(l1);
		cooldown.setSpinnerModel(new IntegerRangeSpinnerModel(0, Integer.MAX_VALUE, 1, 0));
		cooldown.setInterval(50);
		el.addElement(cooldown, "growx");
		f.addFormElement(cooldown);

		l1 = new Label(screen);
		l1.setText("Gain");
		el.addElement(l1, "");
		gain = new Spinner<Float>(screen, Orientation.HORIZONTAL, false);
		gain.onChange(evt -> {
			if (!evt.getSource().isAdjusting()) {
				AdjustChannel r = getSelectedAdjustChannel();
				if (undoManager == null) {
					Map<String, AdjustChannel> adjustChannels = environmentConfiguration.getAdjustChannels();
					adjustChannels.get(r.getName()).setAmount(evt.getNewValue());
					environmentConfiguration.setAdjustedChannels(new HashMap<String, AdjustChannel>(adjustChannels));
				} else
					undoManager.storeAndExecute(new SetChannelGainCommand(environmentConfiguration, r.getName(),
							evt.getNewValue(), r.getAmount()));
			}
		});
		gain.setSpinnerModel(new FloatRangeSpinnerModel(0, 100, 0.1f, 0));
		gain.setFormatterString("%.1f");
		gain.setInterval(50);
		el.addElement(gain, "growx");
		f.addFormElement(gain);

		tabs.addTab("Audio", el);
	}

	protected AdjustChannel getSelectedAdjustChannel() {
		return environmentConfiguration.getAdjustChannelForPath(playlist.getSelected(),
				ToolKit.get().getApplication().getAssetManager());
	}

	protected void setAudioList(LegacyEnvironmentConfiguration environmentConfiguration, PlaylistType type,
			List<String> audio) {
		environmentConfiguration.setPlaylist(type, audio);
	}

	private void skyTab(TabControl tabs, int tabIndex) {
		BaseElement el = createTabPanel();
		Form f = new Form(screen);
		sky = new SkyList(screen);
		f.addFormElement(sky);
		el.addElement(sky, "span 2");
		tabs.addTab("Sky", el);
	}

	private void lightTab(TabControl tabs, int tabIndex) {
		BaseElement el = createTabPanel();
		Form f = new Form(screen);

		Label l1 = new Label(screen);
		l1.setText("Delegate");
		el.addElement(l1, "");
		delegate = new ComboBox<LegacyEnvironmentConfiguration>(screen);
		delegate.onChange(evt -> {
			if (!evt.getSource().isAdjusting()) {
				if (undoManager == null)
					environmentConfiguration.setDelegate(evt.getNewValue());
				else
					undoManager.storeAndExecute(new SetDelegateCommand(environmentConfiguration, evt.getNewValue(),
							environmentConfiguration.getDelegate()));
			}
		});
		delegate.setLabel(l1);
		EnvironmentManager environmentManager = EnvironmentManager
				.get(ToolKit.get().getApplication().getAssetManager());
		for (String c : environmentManager.getEnvironmentConfigurations()) {
			AbstractEnvironmentConfiguration cfg = environmentManager.getEnvironmentConfiguration(c);
			if (cfg instanceof LegacyEnvironmentConfiguration) {
				delegate.addListItem(c, (LegacyEnvironmentConfiguration) cfg);
			}
		}
		el.addElement(delegate, "growx");
		f.addFormElement(delegate);

		l1 = new Label(screen);
		l1.setText("Blend Time");
		el.addElement(l1, "");
		blendTime = new Spinner<Float>(screen, Orientation.HORIZONTAL, false);
		blendTime.onChange(evt -> {
			if (!evt.getSource().isAdjusting()) {
				if (undoManager == null)
					environmentConfiguration.setBlendTime(evt.getNewValue());
				else
					undoManager.storeAndExecute(new SetBlendTimeCommand(environmentConfiguration, evt.getNewValue(),
							environmentConfiguration.getBlendTime()));
			}
		});
		blendTime.setLabel(l1);
		blendTime.setSpinnerModel(new FloatRangeSpinnerModel(0, 100, 0.1f, 0));
		blendTime.setFormatterString("%.3f");
		blendTime.setInterval(50);
		el.addElement(blendTime, "growx");
		f.addFormElement(blendTime);

		// Ambient light
		l1 = new Label("Ambient", screen);
		el.addElement(l1);
		ambient = new ColorFieldControl(screen, ColorRGBA.White);
		ambient.onChange(evt -> {
			if (undoManager == null)
				environmentConfiguration.setAmbient(evt.getNewValue());
			else
				undoManager.storeAndExecute(new SetAmbientCommand(environmentConfiguration, evt.getNewValue(),
						environmentConfiguration.getAmbient()));
		});
		ambient.setLabel(l1);
		f.addFormElement(ambient);
		el.addElement(ambient, "growx");

		// Directional light
		l1 = new Label("Sun", screen);
		el.addElement(l1);
		sun = new ColorFieldControl(screen, ColorRGBA.White);
		sun.onChange(evt -> {
			if (undoManager == null)
				environmentConfiguration.setSun(evt.getNewValue());
			else
				undoManager.storeAndExecute(new SetSunCommand(environmentConfiguration, evt.getNewValue(),
						environmentConfiguration.getAmbient()));
		});
		sun.setLabel(l1);
		f.addFormElement(sun);
		el.addElement(sun, "growx");

		//
		el.addElement(ElementStyle.medium(new Label("Fog", screen)), "wrap 1, growx");

		// Enable Fog
		el.addElement(new Label("Mode", screen));
		fogType = new ComboBox<FogType>(screen, FogType.values());
		fogType.onChange(evt -> {
			if (!evt.getSource().isAdjusting()) {
				if (undoManager == null) {
					setFogType(evt.getNewValue());
				} else {
					undoManager.storeAndExecute(
							new SetFogCommand(environmentConfiguration, evt.getNewValue(), getFogType()));
				}
			}
		});
		el.addElement(fogType, "growx");
		f.addFormElement(fogType);

		// Exclude Sky
		excludeSky = new CheckBox(screen);
		excludeSky.onChange(evt -> {
			if (!evt.getSource().isAdjusting()) {
				if (undoManager == null) {
					environmentConfiguration.getFog().setExcludeSky(evt.getNewValue());
					checkVisible();
				} else {
					undoManager.storeAndExecute(new SetExcludeSkyCommand(environmentConfiguration, evt.getNewValue(),
							environmentConfiguration.getFog() != null
									&& environmentConfiguration.getFog().isExcludeSky()));
				}
			}
		});
		excludeSky.setText("Exclude Sky");
		el.addElement(excludeSky, "span 2, growx");
		f.addFormElement(excludeSky);

		// Fog Start
		l1 = new Label(screen);
		l1.setText("Start");
		el.addElement(l1, "gapleft 20");
		start = new Spinner<Float>(screen, Orientation.HORIZONTAL, false);
		start.onChange(evt -> {
			if (!evt.getSource().isAdjusting())
				if (undoManager == null)
					environmentConfiguration.getFog().setStart(evt.getNewValue());
				else
					undoManager.storeAndExecute(new SetFogStartCommand(environmentConfiguration, evt.getNewValue(),
							environmentConfiguration.getFog().getEnd()));
		});
		start.setLabel(l1);
		start.setSpinnerModel(new FloatRangeSpinnerModel(0, 1, 0.0001f, 0));
		start.setFormatterString("%f");
		start.setInterval(100000);
		el.addElement(start, "growx");
		f.addFormElement(start);

		// Fog End
		l1 = new Label("End", screen);
		el.addElement(l1, "gapleft 20");
		end = new Spinner<Float>(screen, Orientation.HORIZONTAL, false);
		end.onChange(evt -> {
			if (!evt.getSource().isAdjusting()) {
				if (undoManager == null)
					environmentConfiguration.getFog().setEnd(evt.getNewValue());
				else
					undoManager.storeAndExecute(new SetFogEndCommand(environmentConfiguration, evt.getNewValue(),
							environmentConfiguration.getFog().getEnd()));
			}
		});
		end.setLabel(l1);
		end.setSpinnerModel(new FloatRangeSpinnerModel(0, 1, 0.0001f, 0));
		end.setFormatterString("%f");
		end.setInterval(100000);
		el.addElement(end, "growx");
		f.addFormElement(end);

		// Fog Exponential (sort of)
		l1 = new Label(screen);
		l1.setText("Exp");
		el.addElement(l1, "gapleft 20");
		exp = new Spinner<Float>(screen, Orientation.HORIZONTAL, false);
		exp.onChange(evt -> {
			if (!evt.getSource().isAdjusting())
				if (undoManager == null)
					environmentConfiguration.getFog().setExp(evt.getNewValue());
				else
					undoManager.storeAndExecute(new SetFogExpCommand(environmentConfiguration, evt.getNewValue(),
							environmentConfiguration.getFog().getExp()));
		});
		exp.setLabel(l1);
		exp.setSpinnerModel(new FloatRangeSpinnerModel(0, 1, 0.0001f, 0));
		exp.setFormatterString("%.4f");
		exp.setInterval(500);
		el.addElement(exp, "growx");
		f.addFormElement(exp);

		// Fog color
		l1 = new Label("Color", screen);
		el.addElement(l1, "gapleft 20");
		fogColor = new ColorFieldControl(screen, ColorRGBA.White);
		fogColor.onChange(evt -> {
			if (!isAdjusting())
				if (undoManager == null)
					environmentConfiguration.getFog().setColor(evt.getNewValue());
				else
					undoManager.storeAndExecute(new SetFogColourCommand(environmentConfiguration, evt.getNewValue(),
							environmentConfiguration.getFog().getColor()));
		});
		fogColor.setLabel(l1);
		el.addElement(fogColor, "growx");
		f.addFormElement(fogColor);
		tabs.addTab("Environment", el);

	}

	protected FogType getFogType() {
		return environmentConfiguration.getFog() == null ? FogType.INHERIT
				: (environmentConfiguration.getFog().isEnabled() ? FogType.ENABLED : FogType.DISABLED);
	}

	protected void setFogType(FogType value) {
		switch (value) {
		case INHERIT:
			environmentConfiguration.setFog(null);
			break;
		case ENABLED:
		case DISABLED:
			if (environmentConfiguration.getFog() == null) {
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
			playlist.setResources(new StringChooserModel(musicResources));
			break;
		case AMBIENT_MUSIC:
			delay.setSelectedValue(environmentConfiguration.getAmbientMusicDelay());
			cooldown.setSelectedValue(0);
			dir = SceneConstants.MUSIC_PATH;
			playlist.setResources(new StringChooserModel(musicResources));
			break;
		default:
			delay.setSelectedValue(0);
			cooldown.setSelectedValue(0);
			dir = SceneConstants.SOUND_PATH;
			playlist.setResources(new StringChooserModel(musicResources));
			break;
		}
	}

	protected PlaylistType getSelectedPlaylistType() {
		return (PlaylistType) playlistType.getSelectedListItem().getValue();
	}

	private void checkVisible() {
		FogType ft = getFogType();
		start.setEnabled(ft == FogType.ENABLED);
		end.setEnabled(ft == FogType.ENABLED);
		exp.setEnabled(ft == FogType.ENABLED);
		fogColor.setEnabled(ft == FogType.ENABLED);
		excludeSky.setEnabled(ft == FogType.ENABLED);
		delay.setEnabled(getSelectedPlaylistType() == PlaylistType.ACTIVATE_MUSIC
				|| getSelectedPlaylistType() == PlaylistType.AMBIENT_MUSIC);
		cooldown.setEnabled(getSelectedPlaylistType() == PlaylistType.ACTIVATE_MUSIC);
		gain.setEnabled(getSelectedAdjustChannel() != null);
	}

	public final void rebuild() {
		delegate.runAdjusting(() -> delegate.setSelectedByValue(environmentConfiguration.getDelegate()));
		ambient.runAdjusting(() -> ambient.setValue(environmentConfiguration.getAmbient()));
		sun.runAdjusting(() -> sun.setValue(environmentConfiguration.getSun()));
		blendTime.runAdjusting(() -> blendTime.setSelectedValue(environmentConfiguration.getBlendTime()));
		LegacyFogConfig fog = environmentConfiguration.getFog();
		if (fog == null) {
			fogColor.runAdjusting(() -> fogColor.setValue(ColorRGBA.White));
			start.runAdjusting(() -> start.setSelectedValue(0f));
			end.runAdjusting(() -> end.setSelectedValue(0f));
			exp.runAdjusting(() -> exp.setSelectedValue(0f));
			excludeSky.runAdjusting(() -> excludeSky.setChecked(true));
		} else {
			fogType.runAdjusting(() -> fogType.setSelectedByValue(getFogType()));
			fogColor.runAdjusting(() -> fogColor.setValue(fog.getColor()));
			start.runAdjusting(() -> start.setSelectedValue(fog.getStart()));
			end.runAdjusting(() -> end.setSelectedValue(fog.getEnd()));
			exp.runAdjusting(() -> exp.setSelectedValue(fog.getExp()));
			excludeSky.runAdjusting(() -> excludeSky.setChecked(fog.isExcludeSky()));
		}
		setAudioForPlaylist();
		sky.runAdjusting(() -> sky.setValues(environmentConfiguration.getSky()));
		setGainForSelection();
		checkVisible();
	}

	protected void setGainForSelection() {
		gain.runAdjusting(() -> {
			AdjustChannel r = getSelectedAdjustChannel();
			if (r != null) {
				gain.setSelectedValue(r.getAmount());
			} else
				gain.setSelectedValue(1f);
		});
	}

	@SuppressWarnings("serial")
	abstract class AbstractEnvironmentCommand<N> implements UndoableCommand {
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

		public SetAmbientCommand(LegacyEnvironmentConfiguration environmentConfiguration, ColorRGBA colour,
				ColorRGBA oldColour) {
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

		public SetSunCommand(LegacyEnvironmentConfiguration environmentConfiguration, ColorRGBA colour,
				ColorRGBA oldColour) {
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
			fogType.runAdjusting(() -> fogType.setSelectedByValue(n));
			// boolean isChecked = n &&
			// environmentConfiguration.getFog().isExcludeSky();
			// excludeSky.setColorMap(screen.getStyle("CheckBox").getString("defaultImg"));
			// excludeSky.setIsCheckedNoCallback(isChecked);
			checkVisible();
		}
	}

	@SuppressWarnings("serial")
	class SetExcludeSkyCommand extends AbstractEnvironmentCommand<Boolean> {

		public SetExcludeSkyCommand(LegacyEnvironmentConfiguration environmentConfiguration, boolean newVal,
				boolean oldVal) {
			super(environmentConfiguration, newVal, oldVal);
		}

		@Override
		void set(Boolean n) {
			if (environmentConfiguration.getFog() != null)
				environmentConfiguration.getFog().setExcludeSky(n);
			System.out.println("Setting es: " + n + " (was " + excludeSky.isChecked() + ")");
			excludeSky.runAdjusting(() -> excludeSky.setChecked(n));
			checkVisible();
		}
	}

	@SuppressWarnings("serial")
	class SetBlendTimeCommand extends AbstractEnvironmentCommand<Float> {

		public SetBlendTimeCommand(LegacyEnvironmentConfiguration environmentConfiguration, float newVal,
				float oldVal) {
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

		public SetFogColourCommand(LegacyEnvironmentConfiguration environmentConfiguration, ColorRGBA colour,
				ColorRGBA oldColour) {
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

		public SetAudioDelayCommand(PlaylistType type, LegacyEnvironmentConfiguration environmentConfiguration,
				int newVal, int oldVal) {
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

		public SetAudioCooldownCommand(LegacyEnvironmentConfiguration environmentConfiguration, int newVal,
				int oldVal) {
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

		public SetDelegateCommand(LegacyEnvironmentConfiguration environmentConfiguration,
				LegacyEnvironmentConfiguration val, LegacyEnvironmentConfiguration oldVal) {
			super(environmentConfiguration, val, oldVal);
		}

		protected void set(LegacyEnvironmentConfiguration a) {
			environmentConfiguration.setDelegate(a);
			delegate.runAdjusting(() -> delegate.setSelectedByValue(a));
		}
	}

	@SuppressWarnings("serial")
	class SetChannelGainCommand extends AbstractEnvironmentCommand<Float> {

		private String channel;

		public SetChannelGainCommand(LegacyEnvironmentConfiguration environmentConfiguration, String channel, float val,
				float oldVal) {
			super(environmentConfiguration, val, oldVal);
			this.channel = channel;
		}

		protected void set(Float a) {
			Map<String, AdjustChannel> adjustChannels = environmentConfiguration.getAdjustChannels();
			adjustChannels.get(channel).setAmount(a);
			environmentConfiguration.setAdjustedChannels(new HashMap<String, AdjustChannel>(adjustChannels));
		}
	}

	@SuppressWarnings("serial")
	class SetSkyCommand extends AbstractEnvironmentCommand<List<String>> {

		public SetSkyCommand(LegacyEnvironmentConfiguration environmentConfiguration, List<String> val,
				List<String> oldVal) {
			super(environmentConfiguration, val, oldVal);
		}

		protected void set(List<String> a) {
			environmentConfiguration.setSky(a);
			sky.setValues(a);
		}
	}

	public final class SkyList extends ItemList<String, ChooserFieldControl<String>>
			implements ChooserPathTranslater<String> {
		public SkyList(BaseScreen screen) {
			super(screen, prefs, skyResources);
		}

		@Override
		protected ChooserFieldControl<String> createChooser() {

			MaterialFieldControl materialFieldControl = new MaterialFieldControl(screen, null,
					new StringChooserModel(skyResources), prefs) {
				@Override
				protected void createChooserButton() {
					chooserButton = new PushButton(screen) {
						{
							setStyleClass("fancy");
						}
					};
					chooserButton.onMouseReleased(evt -> showChooser(evt.getX(), evt.getY()));
					chooserButton.getButtonIcon().setStyleClass("icon icon-edit");
					chooserButton.setToolTipText("Edit Item");
					addElement(chooserButton, "wrap");

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
			if (!isAdjusting()) {
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

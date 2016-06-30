package org.iceskies.environment.legacy;

import java.beans.PropertyChangeListener;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.prefs.Preferences;

import org.icelib.Icelib;
import org.icelib.UndoManager;
import org.icelib.beans.MappedMap;
import org.icelib.beans.ObjectDelegate;
import org.iceskies.environment.AbstractEnvironmentConfiguration;
import org.iceskies.environment.AbstractEnvironmentConfigurationEditorPanel;
import org.iceskies.environment.PlaylistType;
import org.icesquirrel.runtime.SquirrelArray;
import org.icesquirrel.runtime.SquirrelPrintWriter;
import org.icesquirrel.runtime.SquirrelPrinter;
import org.icesquirrel.runtime.SquirrelTable;

import com.jme3.math.ColorRGBA;

import icetone.core.ElementManager;

public class LegacyEnvironmentConfiguration extends AbstractEnvironmentConfiguration
		implements ObjectDelegate<LegacyEnvironmentConfiguration> {

	private static final long serialVersionUID = 1L;
	private static final String PROP_ACTIVATE_MUSIC_COOLDOWN = "ActivateMusicCooldown";
	private static final String PROP_SUN = "Sun";
	private static final String PROP_AMBIENT = "Ambient";
	private static final String PROP_ACTIVATE_MUSIC_DELAY = "ActivateMusicDelay";
	private static final String PROP_AMBIENT_MUSIC_DELAY = "AambientMusicDelay";
	private static final String PROP_SKY = "Sky";
	private static final String PROP_FOG = "Fog";
	private static final String PROP_ADJUSTED_CHANNELS = "AdjustedChannels";

	private ColorRGBA sun = ColorRGBA.Black.clone();
	private ColorRGBA ambient = ColorRGBA.Black.clone();
	private Map<String, AdjustChannel> adjustedChannels = new MappedMap<String, AdjustChannel>(String.class, AdjustChannel.class);
	private int activateMusicDelay;
	private int activateMusicCooldown;
	private int ambientMusicDelay;
	private List<String> sky = new ArrayList<String>();
	private LegacyFogConfig fog;
	private LegacyEnvironmentConfiguration delegate;

	public LegacyEnvironmentConfiguration(String key) {
		super(key);
	}

	@Override
	public AbstractEnvironmentConfigurationEditorPanel<?> createEditor(UndoManager undoManager, ElementManager screen,
			Preferences prefs) {
		return new LegacyEnvironmentConfigurationEditorPanel(undoManager, screen, prefs, this);
	}

	@Override
	public void write(OutputStream out, Format format) {
		if (format == Format.SQUIRREL) {
			SquirrelTable st = new SquirrelTable();

			// LegacyEnvironmentConfiguration delegate = get

			if (sun != null)
				st.insert("Sun", toSquirrelRGB(sun));
			if (ambient != null)
				st.insert("Ambient", toSquirrelRGB(ambient));
			if (sky != null && !sky.isEmpty()) {
				st.insert("Sky", new SquirrelArray((List<String>) sky));
			}
			if (fog != null) {
				st.insert("Fog", fog.to(format));
			}
			for (Map.Entry<PlaylistType, List<String>> en : playlists.entrySet())
				if (!en.getValue().isEmpty())
					st.insert(Icelib.toEnglish(en.getKey(), true).replace(" ", "_"),
							new SquirrelArray((List<String>) en.getValue()));
			st.insert("Blend_Time", blendTime);
			if (ambientMusicDelay > 0)
				st.insert("Ambient_Music_Delay", ambientMusicDelay);
			if (activateMusicCooldown > 0)
				st.insert("Activate_Music_Cooldown", activateMusicCooldown);
			if (activateMusicDelay > 0)
				st.insert("Activate_Music_Delay", activateMusicDelay);

			if (!adjustedChannels.isEmpty()) {
				SquirrelArray act = new SquirrelArray();
				for (Map.Entry<String, AdjustChannel> en : adjustedChannels.entrySet())
					act.add(new SquirrelArray(en.getKey(), en.getValue().amount));
				st.insert("Adjust_Channels", act);
			}

			SquirrelPrintWriter pw = new SquirrelPrintWriter(out, true);
			pw.print("::Environments." + key + " <- ");
			SquirrelPrinter.format(pw, st, 1);
			pw.println(";");
			return;
		}

		super.write(out, format);
	}

	static SquirrelTable toSquirrelRGB(ColorRGBA c) {
		SquirrelTable t = new SquirrelTable();
		t.insert("r", c.r);
		t.insert("g", c.g);
		t.insert("b", c.b);
		return t;
	}

	@Override
	public Format[] getOutputFormats() {
		return new Format[] { Format.SQUIRREL };
	}

	@Override
	public Object clone() {
		LegacyEnvironmentConfiguration le = new LegacyEnvironmentConfiguration(key);
		le.sun = sun == null ? null : sun.clone();
		le.ambient = ambient == null ? null : ambient.clone();
		for (Map.Entry<String, AdjustChannel> ce : adjustedChannels.entrySet()) {
			le.adjustedChannels.put(ce.getKey(), (AdjustChannel) ce.getValue().clone());
		}
		le.activateMusicCooldown = activateMusicCooldown;
		le.activateMusicDelay = activateMusicDelay;
		le.ambientMusicDelay = ambientMusicDelay;
		le.delegate = delegate;
		le.fog = fog == null ? null : fog.clone();
		le.sky.addAll(sky);
		configureClone(le);
		return le;
	}

	public boolean isEditable() {
		return true;
	}

	public ColorRGBA getSun() {
		return sun;
	}

	public int getActivateMusicCooldown() {
		return activateMusicCooldown;
	}

	public void setActivateMusicCooldown(int activateMusicCooldown) {
		int was = getActivateMusicCooldown();
		if (!Objects.equals(was, activateMusicCooldown)) {
			this.activateMusicCooldown = activateMusicCooldown;
			firePropertyChange(PROP_ACTIVATE_MUSIC_COOLDOWN, was, activateMusicCooldown);
		}

	}

	public Map<String, AdjustChannel> getAdjustedChannels() {
		return adjustedChannels;
	}

	public void setSun(ColorRGBA sun) {
		ColorRGBA was = getSun();
		if (!Objects.equals(was, sun)) {
			this.sun = sun;
			firePropertyChange(PROP_SUN, was, sun);
		}
	}

	public void setAmbient(ColorRGBA ambient) {
		ColorRGBA was = getAmbient();
		if (!Objects.equals(was, ambient)) {
			this.ambient = ambient;
			firePropertyChange(PROP_AMBIENT, was, ambient);
		}
	}

	public void setActivateMusicDelay(int activateMusicDelay) {
		int was = getActivateMusicDelay();
		if (!Objects.equals(was, activateMusicDelay)) {
			this.activateMusicDelay = activateMusicDelay;
			firePropertyChange(PROP_ACTIVATE_MUSIC_DELAY, was, activateMusicDelay);
		}
	}

	public void setAmbientMusicDelay(int ambientMusicDelay) {
		int was = getAmbientMusicDelay();
		if (!Objects.equals(was, ambientMusicDelay)) {
			this.ambientMusicDelay = ambientMusicDelay;
			firePropertyChange(PROP_AMBIENT_MUSIC_DELAY, was, ambientMusicDelay);
		}
	}

	public ColorRGBA getAmbient() {
		return ambient;
	}

	public Map<String, AdjustChannel> getAdjustChannels() {
		return adjustedChannels;
	}

	public int getActivateMusicDelay() {
		return activateMusicDelay;
	}

	public int getAmbientMusicDelay() {
		return ambientMusicDelay;
	}

	public List<String> getSky() {
		return sky;
	}

	public void setSky(List<String> sky) {
		List<String> was = getSky();
		if (!Objects.equals(was, sky)) {
			this.sky = sky;
			firePropertyChange(PROP_SKY, was, sky);
		}
	}

	public LegacyFogConfig getFog() {
		return fog;
	}

	public void setFog(LegacyFogConfig fog) {
		LegacyFogConfig was = getFog();
		if (!Objects.equals(was, fog)) {
			if (was != null) {
				for (PropertyChangeListener l : changeSupport.getPropertyChangeListeners()) {
					was.removePropertyChangeListener(l);
				}
			}
			this.fog = fog;
			if (fog != null) {
				for (PropertyChangeListener l : changeSupport.getPropertyChangeListeners()) {
					fog.addPropertyChangeListener(l);
				}
			}
			firePropertyChange(PROP_FOG, was, fog);
		}
	}

	public void setAdjustedChannels(Map<String, AdjustChannel> adjustedChannels) {
		Map<String, AdjustChannel> was = getAdjustedChannels();
		if (!Objects.equals(was, adjustedChannels)) {
			this.adjustedChannels = adjustedChannels;
			firePropertyChange(PROP_ADJUSTED_CHANNELS, was, adjustedChannels);
		}
	}

	@Override
	protected void onAddPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		if (fog != null) {
			if (propertyName == null)
				fog.addPropertyChangeListener(listener);
			else
				fog.addPropertyChangeListener(propertyName, listener);
		}
	}

	@Override
	protected void onRemovePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		if (fog != null) {
			if (propertyName == null)
				fog.removePropertyChangeListener(listener);
			else
				fog.removePropertyChangeListener(propertyName, listener);
		}
	}

	@SuppressWarnings("serial")
	public static class AdjustChannel implements Serializable, Cloneable {
		private float amount;
		private final String name;

		public AdjustChannel(String name) {
			this.name = name;
		}

		public Object clone() {
			AdjustChannel ac = new AdjustChannel(name);
			ac.amount = amount;
			return ac;
		}

		public String getName() {
			return name;
		}

		public float getAmount() {
			return amount;
		}

		public void setAmount(float amount) {
			this.amount = amount;
		}

	}

	@Override
	public void setDelegate(LegacyEnvironmentConfiguration delegate) {
		this.delegate = delegate;
	}

	@Override
	public LegacyEnvironmentConfiguration getDelegate() {
		return delegate;
	}

}
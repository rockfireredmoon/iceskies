package org.iceskies.environment;

import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.prefs.Preferences;

import org.icelib.beans.AbstractPropertyChangeSupport;
import org.icescene.environment.EnvironmentLight;

import com.google.gson.annotations.Expose;
import com.jme3.math.Vector3f;

import icetone.core.BaseScreen;
import icetone.core.undo.UndoManager;

public abstract class AbstractEnvironmentConfiguration extends AbstractPropertyChangeSupport implements Serializable, Cloneable {

	public enum Format {
		SQUIRREL, JAVASCRIPT, JSON;

		public String toExtension() {
			switch (this) {
			case SQUIRREL:
				return "nut";
			case JAVASCRIPT:
				return "js";
			case JSON:
				return "json";
			}
			return "txt";
		}
	}


	private static final String PROP_BLEND_TIME = "BlendTime";

	private static final long serialVersionUID = 1L;
	@Expose(serialize = false)
	protected String key;

	public static final String PROP_PLAYLIST = "Playlist";
	public final static String PROP_DIRECTIONAL_POSITION = "DirectionalPositional";

	protected Map<PlaylistType, List<String>> playlists = new HashMap<PlaylistType, List<String>>();
	private Vector3f directionalPostion = EnvironmentLight.SUN_POSITION_DEFAULT.clone();
	protected float blendTime = 5;

	public AbstractEnvironmentConfiguration(String key) {
		super();
		this.key = key;
	}

	public AbstractEnvironmentConfigurationEditorPanel<? extends AbstractEnvironmentConfiguration> createEditor(
			UndoManager undoManager, BaseScreen screen, Preferences prefs) {
		throw new UnsupportedOperationException();
	}

	public void write(OutputStream out, Format format) {
		throw new UnsupportedOperationException();
	}

	public Format[] getOutputFormats() {
		return new Format[0];
	}

	protected void configureClone(AbstractEnvironmentConfiguration le) {
		le.blendTime = blendTime;
		for (Map.Entry<PlaylistType, List<String>> ce : playlists.entrySet()) {
			le.playlists.put(ce.getKey(), new ArrayList<String>(ce.getValue()));
		}
	}

	public abstract Object clone();

	public String getKey() {
		return key;
	}

	public boolean isEditable() {
		return false;
	}
	public float getBlendTime() {
		return blendTime;
	}

	public void setBlendTime(float blendTime) {
		float was = getBlendTime();
		if (!Objects.equals(was, blendTime)) {
			this.blendTime = blendTime;
			firePropertyChange(PROP_BLEND_TIME, was, blendTime);
		}
	}

	public Vector3f getDirectionalPosition() {
		return directionalPostion;
	}

	public void setDirectionalPosition(Vector3f directionalPosition) {
		Vector3f was = getDirectionalPosition();
		if (!Objects.equals(was, directionalPosition)) {
			this.directionalPostion = directionalPosition;
			firePropertyChange(PROP_DIRECTIONAL_POSITION, was, directionalPosition);
		}
	}

	public List<String> getPlaylist(PlaylistType type) {
		List<String> list = playlists.get(type);
		if (list == null) {
			list = new ArrayList<String>();
			playlists.put(type, list);
		}
		return list;
	}

	public void setPlaylist(PlaylistType type, List<String> newList) {
		List<String> was = getPlaylist(type);
		if (!Objects.equals(was, newList)) {
			playlists.put(type, newList);
			firePropertyChange(PROP_PLAYLIST + "-" + type.name(), was, newList);
		}
	}

	public void setActivateMusic(List<String> activateMusic) {
		setPlaylist(PlaylistType.ACTIVATE_MUSIC, activateMusic);
	}

	public void setAmbientMusic(List<String> ambientMusic) {
		setPlaylist(PlaylistType.AMBIENT_MUSIC, ambientMusic);
	}

	public void setAmbientNoise(List<String> ambientNoise) {
		setPlaylist(PlaylistType.AMBIENT_NOISE, ambientNoise);
	}

	public void setAmbientSound(List<String> ambientSound) {
		setPlaylist(PlaylistType.AMBIENT_SOUND, ambientSound);
	}

	public List<String> getActivateMusic() {
		return getPlaylist(PlaylistType.ACTIVATE_MUSIC);
	}

	public List<String> getAmbientMusic() {
		return getPlaylist(PlaylistType.AMBIENT_MUSIC);
	}

	public List<String> getAmbientNoise() {
		return getPlaylist(PlaylistType.AMBIENT_NOISE);
	}

	public List<String> getAmbientSound() {
		return getPlaylist(PlaylistType.AMBIENT_SOUND);
	}
	
	public void copyFrom(AbstractEnvironmentConfiguration source) {
		this.key = source.key;
		setBlendTime(source.getBlendTime());
		for(PlaylistType pt : PlaylistType.values()) {
			setPlaylist(pt, new ArrayList<String>(source.getPlaylist(pt)));
		}
	}

	public abstract int getActivateMusicDelay();

	public abstract int getAmbientMusicDelay();
}

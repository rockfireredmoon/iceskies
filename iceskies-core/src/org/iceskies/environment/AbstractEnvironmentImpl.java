package org.iceskies.environment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;

import org.icescene.IcesceneApp;
import org.icescene.audio.AudioAppState;
import org.icescene.audio.AudioQueue;
import org.icescene.audio.AudioQueueHandler;
import org.icescene.environment.EnvironmentLight;
import org.iceskies.environment.legacy.LegacyEnvironment;

import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;

public abstract class AbstractEnvironmentImpl<C extends AbstractEnvironmentConfiguration> implements EnvironmentImpl<C> {
	private final static Logger LOG = Logger.getLogger(LegacyEnvironment.class.getName());
	protected AudioAppState audio;
	protected AssetManager assetManager;
	protected Camera camera;
	protected EnvironmentLight environmentLight;
	protected AppStateManager stateManager;
	protected Node gameNode;
	protected IcesceneApp app;
	protected boolean audioEnabled = true;
	protected Map<PlaylistType, List<String>> audioPaths = new HashMap<PlaylistType, List<String>>();
	private boolean cleanedUp;

	public AbstractEnvironmentImpl(IcesceneApp app, AudioAppState audio, Camera camera, AppStateManager stateManager,
			AssetManager assetManager, EnvironmentLight environmentLight, Node gameNode) {
		assert (audio != null);

		this.audio = audio;
		this.app = app;
		this.camera = camera;
		this.environmentLight = environmentLight;
		this.assetManager = assetManager;
		this.stateManager = stateManager;
		this.gameNode = gameNode;
	}

	public final void onCleanup() {
		LOG.info("Cleaning up sky implementation");
		onEnvironmentCleanup();
	}

	public final void onDetached() {
		LOG.info("Detaching sky implementation");
		onEnvironmentDetached();
	}

	@Override
	public void setAudioEnabled(boolean audioEnabled) {
		this.audioEnabled = audioEnabled;
	}

	protected void checkNotCleanedUp() {
		if (cleanedUp)
			throw new IllegalStateException();
	}

	protected void onEnvironmentCleanup() {
	}

	protected void onEnvironmentDetached() {
	}

	protected void updatePlaylistGain(C cfg, PlaylistType type, float gain) {
		// TODO
	}

	protected float getInterval(AudioQueue queue, PlaylistType type, String resource) {
		return 0;
	}

	protected float getGain(AudioQueue queue, PlaylistType type, String resource) {
		return 1f;
	}

	protected boolean getLoop(AudioQueue queue, PlaylistType type, String resource) {
		return false;
	}

	protected void updatePlaylist(C cfg, PlaylistType type) {
		checkNotCleanedUp();
		List<String> list = audioPaths.get(type);
		AudioQueue q = type.toQueue();
		AudioQueueHandler aqh = audio.getQueue(q);
		for (String s : list) {
			aqh.setGain(s, getGain(q, type, s));
			aqh.setLoop(s, getLoop(q, type, s));
		}
	}

	protected void reloadPlaylist(C cfg, PlaylistType type) {
		checkNotCleanedUp();
		List<String> oldList = audioPaths.get(type);
		List<String> newList = cfg == null || !audioEnabled ? null : new ArrayList<String>(cfg.getPlaylist(type));
		if (!Objects.equals(oldList, newList)) {
			AudioQueue q = type.toQueue();

			if (oldList != null) {
				for (String s : oldList) {
					if (newList == null || !newList.contains(s)) {
						audio.fadeAndRemove(q, s);
					}
				}
			}

			if (newList != null) {
				for (String s : newList) {
					if (oldList == null || !oldList.contains(s)) {
						audio.queue(q, this, s, getInterval(q, type, s), getGain(q, type, s));
					}
				}
			}
			audioPaths.put(type, newList);
		}
	}

	protected void updateAudio(C cfg) {
		checkNotCleanedUp();
		for (PlaylistType p : PlaylistType.values()) {
			reloadPlaylist(cfg, p);
		}
	}

}

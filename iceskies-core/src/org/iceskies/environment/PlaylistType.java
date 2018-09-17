package org.iceskies.environment;

import org.icescene.audio.AudioQueue;

public enum PlaylistType {
	AMBIENT_MUSIC, ACTIVATE_MUSIC, AMBIENT_NOISE, AMBIENT_SOUND;

	AudioQueue toQueue() {
		switch (this) {
		case AMBIENT_MUSIC:
		case ACTIVATE_MUSIC:
			return AudioQueue.MUSIC;
		case AMBIENT_NOISE:
			return AudioQueue.AMBIENT;
		case AMBIENT_SOUND:
			return AudioQueue.AMBIENT2;
		default:
			throw new IllegalArgumentException();
		}
	}
}
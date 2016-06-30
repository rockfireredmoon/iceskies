package org.iceskies.environment;

import java.util.prefs.PreferenceChangeEvent;

public interface EnvironmentImpl {

	void update(float tpf);
	
    void onCleanup();
    
	void handlePrefUpdateSceneThread(PreferenceChangeEvent evt);

	void setEnvironment(AbstractEnvironmentConfiguration environmentConfiguration);

	void setFollowCamera(boolean followCamera);

	void setAudioEnabled(boolean audioEnabled);
}

package org.iceskies.environment;

import java.util.prefs.PreferenceChangeEvent;

public interface EnvironmentImpl<C extends AbstractEnvironmentConfiguration> {

	void update(float tpf);
    
	void handlePrefUpdateSceneThread(PreferenceChangeEvent evt);

	void setEnvironment(C environmentConfiguration);

	void setFollowCamera(boolean followCamera);

	void setAudioEnabled(boolean audioEnabled);

	void onDetached();
	
    void onCleanup();
}

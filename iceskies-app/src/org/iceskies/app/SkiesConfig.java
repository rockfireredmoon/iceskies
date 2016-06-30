package org.iceskies.app;

import java.util.prefs.Preferences;

import org.icelib.AbstractConfig;
import org.icescene.SceneConfig;

public class SkiesConfig extends SceneConfig {
    
    public final static String ENVIRONMENT_EDITOR = "editEnvironment";

    // Debug particles
    public final static String FOLLOW_CAMERA = ENVIRONMENT_EDITOR + "FollowCamera";
    public final static boolean FOLLOW_CAMERA_DEFAULT = true;
    
    public static Object getDefaultValue(String key) {
        return AbstractConfig.getDefaultValue(SkiesConfig.class, key);
    }

    public static Preferences get() {
        return Preferences.userRoot().node(SkiesConstants.APPSETTINGS_NAME).node("game");
    }
}

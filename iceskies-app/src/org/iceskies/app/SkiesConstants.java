package org.iceskies.app;

import com.jme3.math.Vector3f;

public class SkiesConstants {

	public static final Vector3f DOME_SCALE = new Vector3f(3840, 3840, 3840);
	public static final float DOME_Y_OFFSET = 500f;
	public static final float DOME_OFFSET_SCALE = 0.01f;
	/**
	 * How much to mulitple the direction light direction indicator arrow (in
	 * edit mode)
	 */
	public static final float DIRECTIONAL_LIGHT_ARROW_SCALE = 10f;

	/**
	 * The preference key app settings are stored under
	 */
	public static String APPSETTINGS_NAME = "iceskies";

	/**
	 * The distance the sun representation is kept from the camera when in build
	 * mode
	 */
	public static float SUN_REPRESENTATION_DISTANCE = 3010f;
	/**
	 * Physical size of sun
	 */
	public static float SUN_SIZE = 50f;
	/**
	 * How often the environment can update the position of the sun (well,
	 * directional light)
	 */
	public static float SUN_POSITION_UPDATE_INTERVAL = 0.25f;

}

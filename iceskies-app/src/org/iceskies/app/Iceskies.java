package org.iceskies.app;

import java.io.File;
import java.util.logging.Logger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.icelib.AppInfo;
import org.icelib.PageLocation;
import org.icelib.UndoManager;
import org.icescene.HUDMessageAppState;
import org.icescene.IcesceneApp;
import org.icescene.SceneConfig;
import org.icescene.SceneConstants;
import org.icescene.assets.Assets;
import org.icescene.audio.AudioAppState;
import org.icescene.console.ConsoleAppState;
import org.icescene.debug.LoadScreenAppState;
import org.icescene.environment.EnvironmentLight;
import org.icescene.environment.EnvironmentPhase;
import org.icescene.environment.PostProcessAppState;
import org.icescene.io.ModifierKeysAppState;
import org.icescene.io.MouseManager;
import org.icescene.options.OptionsAppState;
import org.icescene.ui.WindowManagerAppState;
import org.lwjgl.opengl.Display;

import com.jme3.bullet.BulletAppState;
import com.jme3.input.controls.ActionListener;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

import icemoon.iceloader.ServerAssetManager;

public class Iceskies extends IcesceneApp implements ActionListener {

	static {
		System.setProperty("iceloader.assetCache", System.getProperty("user.home") + File.separator + ".cache" + File.separator
				+ "iceskies" + File.separator + "assets");
	}

	private final static String MAPPING_OPTIONS = "Options";
	private final static String MAPPING_CONSOLE = "Console";

	private static final Logger LOG = Logger.getLogger(Iceskies.class.getName());

	public static void main(String[] args) throws Exception {
		AppInfo.context = Iceskies.class;

		// Parse command line
		Options opts = createOptions();
		Assets.addOptions(opts);

		CommandLine cmdLine = parseCommandLine(opts, args);

		// A single argument must be supplied, the URL (which is used to
		// determine router, which in turn locates simulator)
		if (cmdLine.getArgList().isEmpty()) {
			throw new Exception("No URL supplied.");
		}
		Iceskies app = new Iceskies(cmdLine);
		startApp(app, cmdLine, "PlanetForever - " + AppInfo.getName() + " - " + AppInfo.getVersion(),
				SkiesConstants.APPSETTINGS_NAME);
	}

	private Vector3f lastLocation;
	private Node weatherNode;

	private Iceskies(CommandLine commandLine) {
		super(SkiesConfig.get(), commandLine, SkiesConstants.APPSETTINGS_NAME, "META-INF/TerrainAssets.cfg");
		setUseUI(true);
		setPauseOnLostFocus(false);
	}

	@Override
	public void restart() {
		Display.setResizable(true);
		super.restart();
	}

	@Override
	public void destroy() {
		super.destroy();
		LOG.info("Destroyed application");
	}

	@Override
	public void onSimpleInitApp() {
		super.onSimpleInitApp();

		// Undo manager
		UndoManager undoManager = new UndoManager();

		getCamera().setFrustumFar(SceneConstants.WORLD_FRUSTUM);

		/*
		 * The scene heirarchy is roughly :-
		 * 
		 * MainCamera MapCamera | | / \ | / \ GameNode |\______ MappableNode |
		 * |\_________TerrainNode | \__________SceneryNode | \_______ WorldNode
		 * |\________ClutterNode \_________CreaturesNode
		 */

		flyCam.setMoveSpeed(prefs.getFloat(SceneConfig.BUILD_MOVE_SPEED, SceneConfig.BUILD_MOVE_SPEED_DEFAULT));
		flyCam.setRotationSpeed(prefs.getFloat(SceneConfig.BUILD_ROTATE_SPEED, SceneConfig.BUILD_ROTATE_SPEED_DEFAULT));
		flyCam.setZoomSpeed(prefs.getFloat(SceneConfig.BUILD_ZOOM_SPEED, SceneConfig.BUILD_ZOOM_SPEED_DEFAULT));
		flyCam.setDragToRotate(true);
		flyCam.setEnabled(true);
		setPauseOnLostFocus(false);

		// Scene
		Node gameNode = new Node("Game");
		Node mappableNode = new Node("Mappable");
		gameNode.attachChild(mappableNode);
		Node worldNode = new Node("World");
		gameNode.attachChild(worldNode);
		rootNode.attachChild(gameNode);
		
		// Download progress
		LoadScreenAppState load = new LoadScreenAppState(prefs);
		load.setAutoShowOnDownloads(true);
		load.setAutoShowOnTasks(true);
		stateManager.attach(load);

		// Environment needs audio (we can also set UI volume now)
		final AudioAppState audioAppState = new AudioAppState(prefs);
		stateManager.attach(audioAppState);
		screen.setUIAudioVolume(audioAppState.getActualUIVolume());

		// Some windows need management
		stateManager.attach(new WindowManagerAppState(prefs));

		// For error messages and stuff
		stateManager.attach(new HUDMessageAppState());

		// Mouse manager requires modifier keys to be monitored
		stateManager.attach(new ModifierKeysAppState());

		// Light
		EnvironmentLight el = new EnvironmentLight(cam, gameNode, prefs);

		// Scene UI
		stateManager.attach(new UIAppState(undoManager, prefs, el));
		stateManager.attach(new SkiesAppState(prefs, gameNode, el));

		// Mouse manager for dealing with clicking, dragging etc.
		final MouseManager mouseManager = new MouseManager(rootNode, getAlarm());
		stateManager.attach(mouseManager);

		// Need the post processor for pretty water
		stateManager.attach(new PostProcessAppState(prefs, el));

		SkiesSwitcherAppState state = new SkiesSwitcherAppState(undoManager, prefs, null, el, gameNode, weatherNode);
		state.setPhase(EnvironmentPhase.DAY);
		stateManager.attach(state);

		// A node that follows that camera, and is used to attach weather to
		weatherNode = new Node("Weather");
		gameNode.attachChild(weatherNode);

		// A menu
		stateManager.attach(new MenuAppState(prefs));

	}

	@Override
	public void registerAllInput() {
		super.registerAllInput();

		// Input
		getKeyMapManager().addMapping(MAPPING_OPTIONS);
		getKeyMapManager().addMapping(MAPPING_CONSOLE);
		getKeyMapManager().addListener(this, MAPPING_OPTIONS, MAPPING_CONSOLE);
	}

	@Override
	protected void configureAssetManager(ServerAssetManager serverAssetManager) {
		getAssets().setAssetsExternalLocation(
				System.getProperty("user.home") + File.separator + "Documents" + File.separator + "Iceskies");
	}

	@Override
	protected void onUpdate(float tpf) {
		super.onUpdate(tpf);
		if (lastLocation == null || !cam.getLocation().equals(lastLocation)) {
			weatherNode.setLocalTranslation(cam.getLocation());
			lastLocation = cam.getLocation().clone();
//			lastRotation = cam.getRotation().clone();
			// b
			// updateLocationPreferencesTimer = getAlarm().timed(new
			// Callable<Void>() {
			// public Void call() throws Exception {
			// TerrainTemplateConfiguration template =
			// terrainLoader.getDefaultTerrainTemplate();
			// if (template != null) {
			// String templateName = template.getBaseTemplateName();
			// Preferences node = prefs.node(templateName);
			// node.putFloat("cameraLocationX", lastLocation.x);
			// node.putFloat("cameraLocationY", lastLocation.y);
			// node.putFloat("cameraLocationZ", lastLocation.z);
			// node.putFloat("cameraRotationX", lastRotation.getX());
			// node.putFloat("cameraRotationY", lastRotation.getY());
			// node.putFloat("cameraRotationZ", lastRotation.getZ());
			// node.putFloat("cameraRotationW", lastRotation.getW());
			// }
			//
			// return null;
			// }
			// }, 5f);
			// PageLocation viewTile = getViewTile();
		}
	}

	private PageLocation getViewTile() {
		// TerrainTemplateConfiguration template =
		// terrainLoader.getTerrainTemplate();
		// if (template == null || terrainLoader.isGlobalTerrainTemplate()) {
		// return PageLocation.UNSET;
		// } else {
		// Vector3f loc = lastLocation;
		// return loc == null ? PageLocation.UNSET :
		// template.getTile(IceUI.toVector2fXZ(loc));
		// }
		return PageLocation.UNSET;
	}

	public void onAction(String name, boolean isPressed, float tpf) {
		if (getKeyMapManager().isMapped(name, MAPPING_OPTIONS)) {
			if (!isPressed) {
				final OptionsAppState state = stateManager.getState(OptionsAppState.class);
				if (state == null) {
					stateManager.attach(new OptionsAppState(prefs));
				} else {
					stateManager.detach(state);
				}
			}
		} else if (getKeyMapManager().isMapped(name, MAPPING_CONSOLE)) {
			if (!isPressed) {
				final ConsoleAppState state = stateManager.getState(ConsoleAppState.class);
				if (state == null) {
					ConsoleAppState console = new ConsoleAppState(prefs);
					stateManager.attach(console);
					console.show();
				} else {
					stateManager.detach(state);
				}
			}
		}
	}
}

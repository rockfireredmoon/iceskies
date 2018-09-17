package org.iceskies.app;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.prefs.Preferences;

import org.icescene.IcemoonAppState;
import org.icescene.IcesceneApp;
import org.icescene.environment.EnvironmentLight;
import org.icescene.scene.AbstractDebugSceneAppState;

import com.jme3.app.state.AppStateManager;
import com.jme3.material.Material;
import com.jme3.material.RenderState.FaceCullMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.debug.Arrow;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Cylinder;
import com.jme3.scene.shape.Sphere;
import com.jme3.scene.shape.Torus;

public class SkiesAppState extends AbstractDebugSceneAppState implements PropertyChangeListener {

	private EnvironmentLight light;
	private Arrow sunArrow;
	private Geometry sunGeometry;
	private Node shapesNode;

	public SkiesAppState(Preferences prefs, Node parentNode, EnvironmentLight light) {
		super(prefs, parentNode);
		addPrefKeyPattern(SkiesConfig.ENVIRONMENT_EDITOR + ".*");
		this.light = light;
	}

	@Override
	protected IcemoonAppState<?> onInitialize(AppStateManager stateManager, IcesceneApp app) {
		light.addPropertyChangeListener(this);
		return super.onInitialize(stateManager, app);
	}

	@Override
	protected void onCleanup() {
		light.removePropertyChangeListener(this);
		super.onCleanup();
	}

	protected void checkGrid() {
		super.checkGrid();
		if (gridGeom == null) {
			sunArrow = null;
			sunGeometry = null;
			if (shapesNode != null) {
				shapesNode.removeFromParent();
			}
		} else {
			if (light.isDirectionalAllowed() && sunArrow == null) {
				sunArrow = new Arrow(light.getSunDirection().mult(IceskiesConstants.DIRECTIONAL_LIGHT_ARROW_SCALE));
				sunGeometry = putShape(arrowNode, sunArrow, light.getSunColor(), 2);
				sunGeometry.getLocalTranslation().y = IceskiesConstants.DIRECTIONAL_LIGHT_ARROW_SCALE;
			} else if (!light.isDirectionalEnabled() && sunArrow != null) {
				sunGeometry.removeFromParent();
				sunGeometry = null;
				sunArrow = null;
			} else if (light.isDirectionalAllowed() && sunArrow != null) {
				sunArrow.setArrowExtent(light.getSunDirection().mult(IceskiesConstants.DIRECTIONAL_LIGHT_ARROW_SCALE));
				sunGeometry.getMaterial().setColor("Color", light.getSunColor());
			}

			if (shapesNode == null) {
				Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
				mat.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Off);
				mat.setBoolean("UseMaterialColors", true);
				mat.setBoolean("UseVertexColor", true);
				mat.setColor("Diffuse", ColorRGBA.White);

				// Material mat = new Material(assetManager,
				// "Common/MatDefs/Misc/Unshaded.j3md");
				// mat.setColor("Color", ColorRGBA.White);

				Geometry boxGeom = new Geometry("BoxTest", new Box(0.5f, 0.5f, 0.5f));
				boxGeom.setMaterial(mat);
				boxGeom.setLocalScale(5f);
				boxGeom.setLocalTranslation(25f, 2.5f, 25f);

				Geometry cylGeom = new Geometry("CylTest", new Cylinder(16, 16, 0.5f, 0.5f));
				cylGeom.setMaterial(mat);
				cylGeom.setLocalScale(5f);
				cylGeom.rotate(FastMath.HALF_PI, 0, 0);
				cylGeom.setLocalTranslation(-25f, 2.5f, 25f);

				Geometry sphereGeom = new Geometry("SphereTest", new Sphere(16, 16, 0.5f));
				sphereGeom.setMaterial(mat);
				sphereGeom.setLocalScale(5f);
				sphereGeom.setLocalTranslation(0f, 2.5f, 25f);

				Geometry torusGeom = new Geometry("TorusTest", new Torus(16, 16, 0.25f, 0.5f));
				torusGeom.setMaterial(mat);
				torusGeom.setLocalScale(5f);
				torusGeom.setLocalTranslation(-25f, 2.5f, 0f);

				Geometry torus2Geom = new Geometry("TorusTest2", new Torus(16, 16, 0.25f, 0.5f));
				torus2Geom.setMaterial(mat);
				torus2Geom.setLocalScale(5f);
				torus2Geom.rotate(FastMath.HALF_PI, 0, 0);
				torus2Geom.setLocalTranslation(25f, 2.5f, 0f);

				shapesNode = new Node();
				shapesNode.attachChild(boxGeom);
				shapesNode.attachChild(cylGeom);
				shapesNode.attachChild(sphereGeom);
				shapesNode.attachChild(torusGeom);
				shapesNode.attachChild(torus2Geom);
			}
			parentNode.attachChild(shapesNode);
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals(EnvironmentLight.PROP_SUN_DIRECTION)
				|| evt.getPropertyName().equals(EnvironmentLight.PROP_SUN_POSITION)
				|| evt.getPropertyName().equals(EnvironmentLight.PROP_SUN_ENABLED)
				|| evt.getPropertyName().equals(EnvironmentLight.PROP_SUN_COLOR)) {
			checkGrid();
		}
	}
}

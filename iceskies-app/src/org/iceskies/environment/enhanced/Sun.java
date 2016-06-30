package org.iceskies.environment.enhanced;

import org.icescene.environment.EnvironmentLight;
import org.iceskies.app.SkiesConstants;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Sphere;

public class Sun extends Node {
    private final Material mat;
//    private final WireframeEditorSelectionMaterial sphereMat;

    public Sun( final EnvironmentLight light, AssetManager assetManager) {
        super("Sun");

        // Outer wireframe 
//        sphereMat = new WireframeEditorSelectionMaterial(assetManager);
//        Sphere sphereMesh = new Sphere(16, 16, 1);
//        Geometry sphereGeom = new Geometry("SunFrame", sphereMesh);
//        sphereGeom.setMaterial(sphereMat);
//        sphereGeom.scaleBuildable(Constants.SUN_SIZE * 1.2f);
//        attachChild(sphereGeom);

        // Inner burning sun :)   
//        Material mat = new Material(assetManager, "MatDefs/Terrain/TerrainWater.j3md");
//        final Texture.WrapMode wrapMode = Texture.WrapMode.Repeat;
//        mat.setFloat("GlobalAnimSpeed", 0.25f);
//
//        Texture tex1 = assetManager.loadTexture(new TextureKey("Terrain/Terrain-Common/Terrain_LavaA.png", false));
//        tex1.setWrap(wrapMode);
//        mat.setTexture("ColorMap1", tex1);
//        mat.setFloat("ScrollAnim1X", 0.05f);
//        mat.setFloat("Alpha1", 1f);
//
//        Texture tex2 = assetManager.loadTexture(new TextureKey("Terrain/Terrain-Common/Terrain_LavaB.png", false));
//        tex2.setWrap(wrapMode);
//        mat.setTexture("ColorMap2", tex2);
//        mat.setFloat("ScrollAnim2Y", 0.05f);
//        mat.setFloat("Alpha2", 0.75f);
//
//        Texture tex3 = assetManager.loadTexture(new TextureKey("Terrain/Terrain-Common/Terrain_LavaA.png", false));
//        tex3.setWrap(wrapMode);
//        mat.setTexture("ColorMap3", tex3);
//        mat.setFloat("Scroll3X", -0.5f);
//        mat.setFloat("Scroll3Y", -0.5f);
//        mat.setFloat("ScrollAnim3X", -0.05f);
//        mat.setFloat("Alpha3", 0.75f);
//
//        Texture tex4 = assetManager.loadTexture(new TextureKey("Terrain/Terrain-Common/Terrain_LavaB.png", false));
//        tex4.setWrap(wrapMode);
//        mat.setTexture("ColorMap4", tex4);
//        mat.setFloat("Scroll4X", -0.5f);
//        mat.setFloat("Scroll4Y", -0.5f);
//        mat.setFloat("ScrollAnim4Y", -0.05f);
//        mat.setFloat("Alpha4", 0.75f);
//

        mat = new Material(assetManager, // Create new material and...
                "Common/MatDefs/Light/Lighting.j3md"); // ... specify .j3md file to use (illuminated).
        mat.setBoolean("UseMaterialColors", true);  // Set some parameters, e.g. blue.
        mat.setColor("Ambient", light.getSunColor());   // ... color of this object
        mat.setColor("Diffuse", light.getSunColor());   // ... color of light being reflected
        mat.setFloat("Shininess", 10f);

        Sphere sunMesh = new Sphere(16, 16, 1);
        Geometry sunGeom = new Geometry("Sun", sunMesh);
        sunGeom.setMaterial(mat);
        sunGeom.scale(SkiesConstants.SUN_SIZE);
        attachChild(sunGeom);

        // The sun is buildable, we can update the environments directional light source using it
//        addControl(new BuildableControl(assetManager) {
//            @Override
//            protected void onApply(BuildableControl actualBuildable) {
//                light.setSunToLocation(actualBuildable.getSpatial().getLocalTranslation());
//                onSunApply();
//            }
//        });
    }

    public void setSunColor(ColorRGBA color) {
//        sphereMat.setColor("Color", color);
        mat.setColor("Ambient", color);
        mat.setColor("Diffuse", color);   // ... color of light being reflected
    }

    protected void onSunApply() {
    }
}

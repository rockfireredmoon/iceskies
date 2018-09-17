package org.iceskies.environment.enhanced;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.renderer.queue.RenderQueue;

public class Rain extends AbstractWaterWeather {

    public Rain(String name, AssetManager assetManager, float weather, float density, boolean wind) {
        super(name, weather, density, wind);
        Material mat = new Material(assetManager,
                "Common/MatDefs/Misc/Particle.j3md");
        mat.setTexture(
                "Texture", assetManager.loadTexture(
                "Textures/raindropbw.png"));
        mat.getAdditionalRenderState().setDepthWrite(true);
        points.setMaterial(mat);        
        points.setQueueBucket(RenderQueue.Bucket.Translucent);
        attachChild(points);
    }
}

package org.iceskies.environment.enhanced;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.queue.RenderQueue;

public class Snow extends AbstractWaterWeather {

    public Snow(String name, AssetManager assetManager, float weather, float speed, boolean wind) {
        super(name, weather, speed, wind);
        points.setImagesX(5);
        points.setImagesY(2);
        points.setRandomAngle(true);
        points.setSelectRandomImage(true);
        points.setStartSize(0.25f);
        points.setEndSize(0.25f);
        points.setStartColor(new ColorRGBA(1, 1, 1, 0.6f));
        points.setEndColor(new ColorRGBA(1f, 1f, 1f, 0.6f));
        points.setRotateSpeed(weather);
        Material mat = new Material(assetManager,
                "Common/MatDefs/Misc/Particle.j3md");
        mat.setTexture(
                "Texture", assetManager.loadTexture(
                "Textures/snowflake.png"));
        points.setMaterial(mat);
        points.setQueueBucket(RenderQueue.Bucket.Translucent);
        attachChild(points);
    }

}

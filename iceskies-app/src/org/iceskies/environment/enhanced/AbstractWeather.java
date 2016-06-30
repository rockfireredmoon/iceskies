package org.iceskies.environment.enhanced;

import com.jme3.effect.ParticleEmitter;
import com.jme3.scene.Node;

public class AbstractWeather extends Node {

    protected ParticleEmitter points;

    public AbstractWeather(String name) {
        super(name);
    }

    public void stopNow(boolean remove) {
        if (remove) {
            points.killAllParticles();
            points.removeFromParent();
            removeFromParent();
        }
        else {
            // Just stop emitting
            points.setParticlesPerSec(0);
        }
    }
}

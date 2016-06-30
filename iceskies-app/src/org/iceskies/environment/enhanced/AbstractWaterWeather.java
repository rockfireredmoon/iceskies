package org.iceskies.environment.enhanced;

import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh;
import com.jme3.effect.shapes.EmitterSphereShape;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.control.AbstractControl;

public class AbstractWaterWeather extends AbstractWeather {

    private float gravity = 10f;
    private float radius = 150f;
    private float height = 50f;
    private float particlesPerSec = 800;
    private float maxLife = 5f;
    private float speed;
    private float density;

    public AbstractWaterWeather(String name, float speed, float density, boolean wind) {
        super(name);

        this.speed = speed;
        this.density = density;

        points = new ParticleEmitter(
                "rainPoints", ParticleMesh.Type.Triangle, (int) (particlesPerSec * speed));
        points.setShape(new EmitterSphereShape(Vector3f.ZERO, radius));
        points.setLocalTranslation(new Vector3f(0f, height, 0f));
        points.getParticleInfluencer().setVelocityVariation(0.1f);
        points.setImagesX(1);
        points.setImagesY(1);
        points.setLowLife(maxLife / 2 / gravity);
        points.setHighLife(maxLife / gravity);
        points.setStartSize(1f * speed / 10);
        points.setEndSize(0.9f * speed / 10);
        points.setStartColor(new ColorRGBA(0.6f, 0.6f, 06.0f, 0.8f));
        points.setEndColor(new ColorRGBA(0.8f, 0.8f, 0.8f, 0.6f));
        points.setFacingVelocity(false);
        points.setRotateSpeed(0.0f);
        points.setShadowMode(RenderQueue.ShadowMode.Off);
        setSpeed(speed);
        setWind(wind);
    }

    public float getDensity() {
        return density;
    }

    public void setDensity(float density) {
        this.density = density;
    }

    public final void setWind(boolean wind) {
        if (wind && points.getControl(WindControl.class) == null) {
            points.addControl(new WindControl());
        } else if (!wind && points.getControl(WindControl.class) != null) {
            points.removeControl(WindControl.class);
        }
    }

    public final void setSpeed(float speed) {
        this.speed = speed;
    }

    public void reconfigure() {
        points.setParticlesPerSec(particlesPerSec * density * maxLife);
        points.setNumParticles((int) (particlesPerSec * density));
        points.setGravity(0, gravity * speed, 0);
        points.getParticleInfluencer().setInitialVelocity(new Vector3f(0.0f, -gravity * speed, 0.0f));
    }

    class WindControl extends AbstractControl {

        private float change = 0;

        @Override
        protected void controlUpdate(float tpf) {
            change += tpf;
            if (change > 10) {
                change = 0;
                float halfWeather = speed / 2f;
                float x = ((-halfWeather) + (FastMath.rand.nextFloat() * halfWeather)) * gravity;
                float z = ((-halfWeather) + (FastMath.rand.nextFloat() * halfWeather)) * gravity;
                float y = -gravity * speed + (-gravity * (FastMath.rand.nextFloat() * gravity));
                if (FastMath.rand.nextFloat() < 0.1) {
                    x = 0;
                    z = 0;
                    y = -(gravity * speed);
                }
//                LOG.info("Change to " + x + "," + y + "," + z);
                points.getParticleInfluencer().setInitialVelocity(new Vector3f(x, y, z));
            }
        }

        @Override
        protected void controlRender(RenderManager rm, ViewPort vp) {
        }
    }
}

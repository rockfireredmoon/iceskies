/*
 Copyright (c) 2013-2014, Stephen Gold
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright
 notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright
 notice, this list of conditions and the following disclaimer in the
 documentation and/or other materials provided with the distribution.
 * Stephen Gold's name may not be used to endorse or promote products
 derived from this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL STEPHEN GOLD BE LIABLE FOR ANY
 DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package jme3utilities;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;

import jme3utilities.math.MyVector3f;

/**
 * Utility methods which operate on jME3 cameras. Aside from test cases, all
 * methods should be public and static.
 *
 * @author Stephen Gold <sgold@sonic.net>
 */
final public class MyCamera {
    // *************************************************************************
    // constants

    /**
     * message logger for this class
     */
    final private static Logger logger =
            Logger.getLogger(MyCamera.class.getName());
    // *************************************************************************
    // constructors

    /**
     * A private constructor to inhibit instantiation of this class.
     */
    private MyCamera() {
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Compute a camera's aspect ratio.
     *
     * @param camera camera to measure (not null)
     * @return width divided by height (&gt;0)
     */
    public static float aspectRatio(Camera camera) {
        float height = camera.getHeight();
        assert height > 0f : height;

        float width = camera.getWidth();
        assert width > 0f : width;

        float ratio = width / height;

        return ratio;
    }

    /**
     * Compute a camera's azimuth angle.
     *
     * @param camera camera to measure (not null)
     * @return radians east of north
     */
    public static float azimuth(Camera camera) {
        Vector3f direction = camera.getDirection();
        float azimuth = MyVector3f.azimuth(direction);
        return azimuth;
    }

    /**
     * Compute a camera's vertical field-of-view angle.
     *
     * @param camera camera to measure (not null)
     * @return radians from top of frustum to bottom of frustum
     */
    public static float fovY(Camera camera) {
        if (camera.isParallelProjection()) {
            return 0f;
        }

        float near = camera.getFrustumNear();
        assert near > 0f : near;

        float top = camera.getFrustumTop();
        assert top > 0f : top;

        float fovY = 2f * FastMath.atan(top / near);

        return fovY;
    }

    /**
     * Rotate a camera without changing its location, setting its "up" direction
     * automatically.
     *
     * @param camera camera to rotate (not null)
     * @param direction (not null, positive length, unaffected)
     */
    public static void look(Camera camera, Vector3f direction) {
        if (MyVector3f.isZeroLength(direction)) {
            throw new IllegalArgumentException(
                    "direction should have positive length");
        }

        if (direction.x == 0f && direction.z == 0f) {
            /*
             * When looking straight up or down, use +X as the up direction.
             */
            camera.lookAtDirection(direction, Vector3f.UNIT_X);
        } else {
            camera.lookAtDirection(direction, Vector3f.UNIT_Y);
        }
    }

    /**
     * Alter a camera's near and far planes without affecting its aspect ratio
     * or field-of-view.
     *
     * @param camera camera to alter (not null)
     * @param newNear distance to the near clipping plane (&lt;newFar, &gt;0)
     * @param newFar distance to the far clipping plane (&gt;newNear)
     */
    public static void setNearFar(Camera camera, float newNear, float newFar) {
        Validate.nonNull(camera, "camera");
        Validate.positive(newNear, "near");
        if (!(newFar > newNear)) {
            logger.log(Level.SEVERE, "far={0} near={1}",
                    new Object[]{newFar, newNear});
            throw new IllegalArgumentException(
                    "far should be greater than near");
        }

        if (camera.isParallelProjection()) {
            camera.setFrustumFar(newFar);
            camera.setFrustumNear(newNear);
        } else {
            float aspectRatio = aspectRatio(camera);
            float yDegrees = yDegrees(camera);
            camera.setFrustumPerspective(yDegrees, aspectRatio, newNear,
                    newFar);
        }
    }

    /**
     * Alter a camera's field-of-view tangents.
     *
     * @param camera camera to alter (not null)
     * @param newTangent tangent of the vertical field-of-view half-angle
     * (&gt;0)
     */
    public static void setYTangent(Camera camera, float newTangent) {
        Validate.nonNull(camera, "camera");
        Validate.positive(newTangent, "tangent");

        float yTangent = yTangent(camera);
        float factor = newTangent / yTangent;
        zoom(camera, factor);
    }

    /**
     * Compute a camera's vertical field-of-view in degrees.
     *
     * @param camera camera to measure (not null)
     * @return vertical angle in degrees (&gt;0)
     */
    public static float yDegrees(Camera camera) {
        Validate.nonNull(camera, "camera");
        if (camera.isParallelProjection()) {
            return 0f;
        }

        float yTangent = yTangent(camera);
        float yRadians = 2f * FastMath.atan(yTangent);
        float yDegrees = yRadians * FastMath.RAD_TO_DEG;

        return yDegrees;
    }

    /**
     * Compute a camera's vertical field-of-view tangent.
     *
     * @param camera camera to measure (not null)
     * @return tangent of the vertical field-of-view half-angle (&gt;0)
     */
    public static float yTangent(Camera camera) {
        float near = camera.getFrustumNear();
        assert near > 0f : near;

        float top = camera.getFrustumTop();
        assert top > 0f : top;

        float yTangent = top / near;

        return yTangent;
    }

    /**
     * Increase a camera's field-of-view tangents by a specified factor.
     *
     * @param camera camera to alter (not null)
     * @param factor amount to reduce both field-of-view tangents (&gt;0)
     */
    public static void zoom(Camera camera, float factor) {
        Validate.positive(factor, "factor");

        float bottom = camera.getFrustumBottom();
        camera.setFrustumBottom(bottom * factor);

        float left = camera.getFrustumLeft();
        camera.setFrustumLeft(left * factor);

        float right = camera.getFrustumRight();
        camera.setFrustumRight(right * factor);

        float top = camera.getFrustumTop();
        camera.setFrustumTop(top * factor);
    }
}
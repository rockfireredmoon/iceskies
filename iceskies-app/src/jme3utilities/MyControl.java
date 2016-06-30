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

import java.util.Collection;
import java.util.logging.Logger;

import com.jme3.animation.AnimControl;
import com.jme3.animation.Animation;
import com.jme3.animation.SkeletonControl;
import com.jme3.bullet.control.GhostControl;
import com.jme3.bullet.control.KinematicRagdollControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.effect.ParticleEmitter;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;

/**
 * Utility methods which operate on jME3 scene-graph controls in general. Aside
 * from test cases, all methods here should be public and static.
 *
 * @author Stephen Gold <sgold@sonic.net>
 */
public class MyControl {
    // *************************************************************************
    // constants

    /**
     * message logger for this class
     */
    final private static Logger logger =
            Logger.getLogger(MyControl.class.getName());
    // *************************************************************************
    // constructors

    /**
     * A private constructor to inhibit instantiation of this class.
     */
    private MyControl() {
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Describe a scene-graph control.
     *
     * @param control (not null)
     * @return textual description
     */
    public static String describe(Object control) {
        String name = control.getClass().getSimpleName();
        if (name.endsWith("Control")) {
            int length = name.length() - "Control".length();
            name = name.substring(0, length);
        }

        String result = name;
        if (control instanceof RigidBodyControl) {
            RigidBodyControl rbc = (RigidBodyControl) control;
            float mass = rbc.getMass();
            if (mass == 0f) {
                result += "[static]";
            } else if (rbc.isKinematic()) {
                result += "[kinematic]";
            } else {
                result += String.format("[%.2f kg]", mass);
            }

        } else if (control instanceof AnimControl) {
            AnimControl ac = (AnimControl) control;
            Spatial spatial = ac.getSpatial();
            Collection<String> nameCollection = ac.getAnimationNames();
            String[] array = MyString.toArray(nameCollection);
            for (int iAnimation = 0; iAnimation < array.length; iAnimation++) {
                String animationName = array[iAnimation];
                Animation animation = ac.getAnim(animationName);
                array[iAnimation] = MyAnimation.describe(animation, spatial);
            }
            String names = MyString.join(array);
            result += String.format("[%s]", names);

        } else if (control instanceof SkeletonControl) {
            SkeletonControl sc = (SkeletonControl) control;
            int boneCount = sc.getSkeleton().getBoneCount();
            result += String.format("[%d]", boneCount);
        }

        return result;
    }

    /**
     * Test whether a scene-graph control is enabled.
     *
     * @param control control to test (not null)
     * @return true if the control is enabled, otherwise false
     */
    public static boolean isEnabled(Object control) {
        if (control instanceof AbstractControl) {
            return ((AbstractControl) control).isEnabled();
        } else if (control instanceof GhostControl) {
            return ((GhostControl) control).isEnabled();
        } else if (control instanceof KinematicRagdollControl) {
            return ((KinematicRagdollControl) control).isEnabled();
        } else if (control instanceof RigidBodyControl) {
            return ((RigidBodyControl) control).isEnabled();
        } else if (control instanceof ParticleEmitter.ParticleEmitterControl) {
            return ((ParticleEmitter.ParticleEmitterControl) control)
                    .isEnabled();
        } else {
            assert false : control.getClass();
            return false;
        }
    }

    /**
     * Check whether a scene-graph control is compatible with isEnabled() and
     * setEnabled().
     *
     * @param control control to validate
     * @return true if it's compatible, otherwise false
     */
    public static boolean isValid(Object control) {
        return control instanceof AbstractControl
                || control instanceof GhostControl
                || control instanceof KinematicRagdollControl
                || control instanceof RigidBodyControl
                || control instanceof ParticleEmitter.ParticleEmitterControl;
    }

    /**
     * Alter the enabled state of a scene-graph control.
     *
     * @param control control to alter
     * @param newState true means enable the control, false means disable it
     */
    public static void setEnabled(Object control, boolean newState) {
        if (control instanceof AbstractControl) {
            ((AbstractControl) control).setEnabled(newState);
        } else if (control instanceof GhostControl) {
            ((GhostControl) control).setEnabled(newState);
        } else if (control instanceof KinematicRagdollControl) {
            ((KinematicRagdollControl) control).setEnabled(newState);
        } else if (control instanceof RigidBodyControl) {
            ((RigidBodyControl) control).setEnabled(newState);
        } else if (control instanceof ParticleEmitter.ParticleEmitterControl) {
            ((ParticleEmitter.ParticleEmitterControl) control)
                    .setEnabled(newState);
        } else {
            assert false : control.getClass();
        }
    }
}
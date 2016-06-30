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

import java.io.IOException;

import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;

/**
 * Simplified abstract control which does not implement serialization or
 * cloning.
 * <p>
 * Although this is an abstract class, it defines all required methods in order
 * to simplify the development of subclasses -- unlike AbstractControl.
 * <p>
 * Each instance is enabled at creation.
 *
 * @author Stephen Gold <sgold@sonic.net>
 */
abstract public class SimpleControl
        extends AbstractControl {
    // *************************************************************************
    // new methods exposed

    /**
     * Toggle the enabled status of this control.
     */
    public void toggleEnabled() {
        setEnabled(!enabled);
    }
    // *************************************************************************
    // AbstractControl methods

    /**
     * Callback invoked when the spatial is about to be rendered to a view port.
     * <p>
     * Only performs checks. Meant to be overridden.
     *
     * @param renderManager renderer which is rendering the spatial (not null)
     * @param viewPort view port where the spatial will be rendered (not null)
     */
    @Override
    protected void controlRender(RenderManager renderManager,
            ViewPort viewPort) {
        Validate.nonNull(renderManager, "render manager");
        Validate.nonNull(viewPort, "view port");
        if (!enabled) {
            throw new IllegalStateException("should be enabled");
        }
    }

    /**
     * Callback invoked when the spatial's geometric state is about to be
     * updated, once per frame while attached and enabled.
     * <p>
     * Only performs checks. Meant to be overridden.
     *
     * @param updateInterval time interval between updates (in seconds, &ge;0)
     */
    @Override
    protected void controlUpdate(float updateInterval) {
        Validate.nonNegative(updateInterval, "interval");
        if (!enabled) {
            throw new IllegalStateException("should be enabled");
        }
    }

    /**
     * De-serialize this control, for example when loading from a J3O file.
     *
     * @param importer (not null)
     */
    @Override
    public void read(JmeImporter importer)
            throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Serialize this control, for example when saving to a J3O file.
     *
     * @param exporter (not null)
     */
    @Override
    public void write(JmeExporter exporter)
            throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    // *************************************************************************
    // Control methods

    /**
     * Clone this control for a different spatial.
     *
     * @param spatial spatial to clone for (not null)
     * @return new control
     */
    @Override
    public Control cloneForSpatial(Spatial spatial) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
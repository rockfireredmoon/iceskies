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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import com.jme3.app.state.AppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.SceneProcessor;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.util.IntMap;

/**
 * Miscellaneous utility methods. Aside from test cases, all methods here should
 * be public and static.
 *
 * @author Stephen Gold <sgold@sonic.net>
 */
public class Misc {
    // *************************************************************************
    // constants

    /**
     * blend time for animations (in real seconds, &ge;0)
     */
    final public static float blendTime = 0.3f;
    /**
     * message logger for this class
     */
    final private static Logger logger =
            Logger.getLogger(Misc.class.getName());
    /**
     * asset path of the shaded material definition
     */
    final public static String shadedMaterialAssetPath =
            "Common/MatDefs/Light/Lighting.j3md";
    /**
     * asset path to the Unshaded material definition
     */
    final public static String unshadedMaterialAssetPath =
            "Common/MatDefs/Misc/Unshaded.j3md";
    // *************************************************************************
    // constructors

    /**
     * A private constructor to inhibit instantiation of this class.
     */
    private Misc() {
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Detach all app states which are subclasses of a specified class.
     *
     * @param stateManager (not null)
     * @param whichClass (not null)
     */
    public static <T extends AppState> void detachAll(
            AppStateManager stateManager, Class<T> whichClass) {
        Validate.nonNull(whichClass, "class");

        AppState state = stateManager.getState(whichClass);
        while (state != null) {
            stateManager.detach(state);
            state = stateManager.getState(whichClass);
        }
    }

    /**
     * Get the existing filter post-processor for a viewport, or if it has none
     * add a new one to it.
     *
     * @param viewPort (not null)
     * @param assetManager (not null)
     * @return not null
     */
    public static FilterPostProcessor getFpp(ViewPort viewPort,
            AssetManager assetManager) {
        Validate.nonNull(assetManager, "asset manager");

        for (SceneProcessor processor : viewPort.getProcessors()) {
            if (processor instanceof FilterPostProcessor) {
                return (FilterPostProcessor) processor;
            }
        }
        /*
         * Add a new filter post-processor.
         */
        FilterPostProcessor fpp = new FilterPostProcessor(assetManager);
        viewPort.addProcessor(fpp);
        return fpp;
    }

    /**
     * Generate the filesystem path to a file in the user's home directory.
     *
     * @param fileName file name to use (not null)
     * @return generated path
     */
    public static String getUserPath(String fileName) {
        Validate.nonNull(fileName, "file name");

        String homePath = System.getProperty("user.home");
        assert homePath != null;
        String result = String.format("%s/%s", homePath, fileName);
        return result;
    }

    /**
     * Read the verbose version string for this package.
     *
     * @return package name, branch, and revision of this file
     */
    public static String getVersion() {//
        return "jme3-utilities trunk $Rev: 434 $";
    }

    /**
     * Read the terse version string for this package.
     *
     * @return branch and revision of this file
     */
    public static String getVersionShort() {
        String verbose = getVersion();
        String[] words = verbose.split("\\s+");
        String result = String.format("%s %s", words[1], words[3]);

        return result;
    }

    /**
     * Test whether a mesh has texture (U-V) coordinates.
     *
     * @param mesh mesh to test (not null)
     * @return true if the mesh has texture coordinates, otherwise false
     */
    public static boolean hasUV(Mesh mesh) {
        IntMap<VertexBuffer> buffers = mesh.getBuffers();
        int key = Type.TexCoord.ordinal();
        boolean result = buffers.containsKey(key);
        return result;
    }

    /**
     * Set a specified grayscale pixel to a specified brightness.
     *
     * @param graphics rendering context of the pixel (not null)
     * @param x pixel's 1st coordinate (&lt;width, &ge;0)
     * @param y pixel's 2nd coordinate (&lt;height, &ge;0)
     * @param brightness (&le;1, &ge;0, 0 &rarr; black, 1 &rarr; white)
     *
     */
    public static void setGrayPixel(Graphics2D graphics, int x, int y,
            float brightness) {
        if (x < 0 || x >= graphics.getDeviceConfiguration().getBounds().width) {
            logger.log(Level.SEVERE, "x={0}", x);
            throw new IllegalArgumentException("X coordinate out of bounds");
        }
        if (y < 0 || y >= graphics.getDeviceConfiguration().getBounds().height) {
            logger.log(Level.SEVERE, "y={0}", y);
            throw new IllegalArgumentException("Y coordinate out of bounds");
        }
        Validate.fraction(brightness, "brightness");

        Color color = new Color(brightness, brightness, brightness);
        graphics.setColor(color);
        graphics.fillRect(x, y, 1, 1);
    }

    /**
     * Alter the logging levels of all known loggers.
     *
     * @param newLevel (not null)
     */
    public static void setLoggingLevels(Level newLevel) {
        Validate.nonNull(newLevel, "level");
        Logger.getLogger("").setLevel(newLevel);
    }

    /**
     * Write an image to a PNG file, attempting to overwrite any pre-existing
     * file.
     *
     * @param filePath path to the output file (not null)
     * @param image image to be written (not null)
     */
    public static void writeMap(String filePath, RenderedImage image)
            throws IOException {
        Validate.nonNull(filePath, "path");
        Validate.nonNull(image, "image");

        File textureFile = new File(filePath);
        try {
            ImageIO.write(image, "png", textureFile);
            logger.log(Level.INFO, "wrote texture to {0}",
                    MyString.quote(filePath));

        } catch (IOException exception) {
            logger.log(Level.SEVERE, "write to {0} failed",
                    MyString.quote(filePath));
            boolean success = textureFile.delete();
            if (success) {
                logger.log(Level.INFO, "deleted file {0}",
                        MyString.quote(filePath));
            } else {
                logger.log(Level.SEVERE, "delete of {0} failed",
                        MyString.quote(filePath));
            }
            throw exception;
        }
    }
}
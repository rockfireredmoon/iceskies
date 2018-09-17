/*
 Copyright (c) 2014, Stephen Gold
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
package jme3utilities.math;

import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import jme3utilities.Validate;

/**
 * Random and noise utility methods. Aside from test cases, all methods should
 * be public and static.
 *
 * @author Stephen Gold <sgold@sonic.net>
 */
final public class Noise {
    // *************************************************************************
    // constants

    /**
     * message logger for this class
     */
    final private static Logger logger =
            Logger.getLogger(Noise.class.getName());
    /**
     * shared pseudo-random generator
     */
    final private static Random generator = new Random();
    // *************************************************************************
    // constructors

    /**
     * A private constructor to inhibit instantiation of this class.
     */
    private Noise() {
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Sample fractional Brownian motion (FBM) noise in two dimensions.
     *
     * @param generator base noise generator (not null)
     * @param sampleX 1st coordinate of the sample point
     * @param sampleY 2nd coordinate of the sample point
     * @param numOctaves number of noise components (&gt;0)
     * @param fundamental frequency for the 1st component (&gt;0)
     * @param gain amplitude ratio between octaves (&gt;0, &lt;1)
     * @param lacunarity frequency ratio between octaves (&gt;1)
     * @return noise value (range depends on parameters)
     */
    public static float fbmNoise(Noise2 generator, float sampleX,
            float sampleY, int numOctaves, float fundamental, float gain,
            float lacunarity) {
        Validate.nonNull(generator, "generator");
        Validate.positive(numOctaves, "octaves");
        Validate.positive(fundamental, "fundamental");
        if (!(gain > 0f && gain < 1f)) {
            logger.log(Level.SEVERE, "gain={0}", gain);
            throw new IllegalArgumentException(
                    "gain should be between 0 and 1");
        }
        if (!(lacunarity > 1f)) {
            logger.log(Level.SEVERE, "lacunarity={0}", lacunarity);
            throw new IllegalArgumentException(
                    "lacunarity should be greater than 1");
        }

        float amplitude = 1f;
        float frequency = fundamental;
        float total = 0f;
        for (int octave = 0; octave < numOctaves; octave++) {
            float sample = generator.sampleNormalized(sampleX * frequency,
                    sampleY * frequency);
            total += amplitude * sample;
            frequency *= lacunarity;
            amplitude *= gain;
        }

        return total;
    }

    /**
     * Get the next uniformly distributed, pseudo-random, single-precision value
     * from the shared generator.
     *
     * @return float value (&ge;0, &lt;1)
     */
    public static float nextFloat() {
        return generator.nextFloat();
    }

    /**
     * Pick a pseudo-random member from a list using the specified generator.
     *
     * @param list list to select from (not null)
     * @param generator generator to use (not null)
     * @return member of list or null if it's empty
     */
    @SuppressWarnings("rawtypes")
    public static Object pick(List list, Random generator) {
        Validate.nonNull(generator, "generator");
        Validate.nonNull(list, "list");

        int count = list.size();
        assert count >= 0 : count;
        if (count == 0) {
            return null;
        }
        int index = generator.nextInt();
        index = MyMath.modulo(index, count);
        Object result = list.get(index);

        return result;
    }

    /**
     * Re-seed the shared pseudo-random generator.
     *
     * @param newSeed
     */
    public static void reseedGenerator(long newSeed) {
        generator.setSeed(newSeed);
    }
}
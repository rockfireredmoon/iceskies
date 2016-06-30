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

import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Cyclic permutation of the integers from 0 to length-1, for use in noise
 * functions.
 *
 * @author Stephen Gold <sgold@sonic.net>
 */
public class Permutation {
    // *************************************************************************
    // constants

    /**
     * message logger for this class
     */
    final private static Logger logger =
            Logger.getLogger(Permutation.class.getName());
    // *************************************************************************
    // fields
    /**
     * indices which make up the permutation
     */
    private int[] indices = null;
    // *************************************************************************
    // constructors

    /**
     * Construct a permutation with the specified length from a specified seed.
     *
     * @param length length of the permutation (&gt;1)
     * @param seed
     */
    public Permutation(int length, long seed) {
        if (length <= 1) {
            logger.log(Level.SEVERE, "length={0}", length);
            throw new IllegalArgumentException(
                    "length should be greater than 1");
        }
        /*
         * Initialize the permutation to identity.
         */
        indices = new int[length];
        for (int i = 0; i < length; i++) {
            indices[i] = i;
        }
        /*
         * Shuffle the permutation by performing one pass of
         * pseudo-random swaps.
         */
        Random swapGenerator = new Random(seed);
        for (int sequential = 0; sequential < length; sequential++) {
            int nextInt = swapGenerator.nextInt();
            int random = MyMath.modulo(nextInt, length);
            swapTableEntries(sequential, random);
        }
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Permute an index, wrapping at the end of the table.
     *
     * @param index input
     * @return (&lt;length, &ge;0)
     */
    public int permute(int index) {
        int wrapped = MyMath.modulo(index, indices.length);
        int result = indices[wrapped];

        assert result >= 0 : result;
        assert result < indices.length : result;
        return result;
    }
    // *************************************************************************
    // private methods

    /**
     * Swap two elements in the permutation.
     *
     * @param first index of the 1st element (&lt;length, &ge;0)
     * @param second index of the 2nd element (&lt;length, &ge;0)
     */
    private void swapTableEntries(int first, int second) {
        int savedValue = indices[first];
        indices[first] = indices[second];
        indices[second] = savedValue;
    }
}
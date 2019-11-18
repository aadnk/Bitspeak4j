/*
 * Copyright (C) 2019  Kristian S. Stangeland
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 */

package com.comphenix.bitspeak;

import java.util.Arrays;

class TestPatterns {
    public static byte[] generatePattern(int value, int length) {
        byte[] array = new byte[length];
        Arrays.fill(array, (byte) value);
        return array;
    }
}

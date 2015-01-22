/*
 * Copyright (C) 2009 Cyril Jaquier
 *
 * This file is part of NetCounter.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; If not, see <http://www.gnu.org/licenses/>.
 */

package net.lmag.connectornot.model;

import android.util.Log;

/**
 * @author Louis
 *
 */
public class MyLog {

    private static final boolean LOGGING = true; //false to disable logging

    public static void d(String tag, String message) {
        if (LOGGING) {
            Log.d(tag, message);
        }
    }

    public static void i(String tag, String message) {
        if (LOGGING) {
            Log.i(tag, message);
        }
    }
    
    public static void i(String tag, String message, Throwable tr) {
        if (LOGGING) {
            Log.i(tag, message, tr);
        }
    }
    
    public static void e(String tag, String message) {
        if (LOGGING) {
            Log.e(tag, message);
        }
    }
    
    public static void e(String tag, String message, Throwable tr) {
        if (LOGGING) {
            Log.e(tag, message, tr);
        }
    }
    
    public static void w(String tag, String message) {
        if (LOGGING) {
            Log.w(tag, message);
        }
    }
}

//============================================================================
// Name        : ShapetheWorldAttribuite.java
// Author      : Mahendra Gunawardena
// Date        : 10/1/2017
// Version     : Rev 0.01
// Copyright   : Your copyright notice
// Description : MainActivity UI for the BLE Scanner
//============================================================================
/*
 * ShapetheWorldAttribuite.java
 * Implementation of a ShapetheWorldAttribuite class to interface to scan for
 *
 *
 * Copyright Mahendra Gunawardena, Mitisa LLC
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED ''AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
 * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL I
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */


package shapetheworld.application.shapetheworld;

import java.util.HashMap;
import java.util.UUID;

public class ShapetheWorldAttribute {

    private static HashMap<String, String> gattAttributes = new HashMap();
    
    public static String EDX_SERVICE = "0000FFF0-0000-1000-8000-00805F9B34FB";
    public static String PLOT_STATE = "0000FFF1-0000-1000-8000-00805F9B34FB";
    public static String TIME_STATE = "0000FFF2-0000-1000-8000-00805F9B34FB";
    public static String SOUND_STATE = "0000FFF3-0000-1000-8000-00805F9B34FB";
    //public static String TEMPERATURE_STATE = "0000FFF4-0000-1000-8000-00805F9B34FB";
    public static String LIGHT_STATE = "0000FFF5-0000-1000-8000-00805F9B34FB";
    public static String EDX_STATE = "0000FFF6-0000-1000-8000-00805F9B34FB";
    public static String NUMBER_OF_STEPS = "0000FFF7-0000-1000-8000-00805F9B34FB";
    public static String STRING_CHAR = "F0001131-0451-4000-B000-000000000000";
    public static String STREAM_CHAR = "F0001132-0451-4000-B000-000000000000";

    public final static UUID UUID_CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"); // UUID for notification descriptor
    public final static UUID UUID_NUMBER_OF_STEPS = UUID.fromString(NUMBER_OF_STEPS);



    static {
        // Services
        gattAttributes.put(EDX_SERVICE.toLowerCase(), "EDX Service");
        // Characteristics
        gattAttributes.put(PLOT_STATE.toLowerCase(), "PlotState");
        gattAttributes.put(TIME_STATE.toLowerCase(), "Time");
        gattAttributes.put(SOUND_STATE.toLowerCase(), "Sound");
        //gattAttributes.put(TEMPERATURE_STATE.toLowerCase(), "Temperature");
        gattAttributes.put(LIGHT_STATE.toLowerCase(), "Light");
        gattAttributes.put(NUMBER_OF_STEPS.toLowerCase(), "Number of Steps");

    }

    /**
     * Search the map for the attribute name of a given UUID
     *
     * @param uuid        UUID to search for
     * @param defaultName Name to return if the UUID is not found in the map
     *
     * @return Name of attribute with given UUID
     */
    public static String lookup(String uuid, String defaultName) {
        String name = gattAttributes.get(uuid);
        return name == null ? defaultName : name;
    }

    /**
     * @return Map of UUIDs and attribute names used in the Project Zero demo
     */
    public static HashMap<String, String> gattAttributes(){
        return gattAttributes;
    }

}

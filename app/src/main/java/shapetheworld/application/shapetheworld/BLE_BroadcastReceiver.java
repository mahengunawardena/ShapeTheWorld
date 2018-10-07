//============================================================================
// Name        : BLE_BroadcastReceiver.java
// Author      : Mahendra Gunawardena
// Date        : 10/3/2017
// Version     : Rev 0.01
// Copyright   : Your copyright notice
// Description : BLE_Service for managing the BLE device
//============================================================================
/*
 * BLE_BroadcastReceiver.java
 * Implementation of a BLE_BroadcastReceiver to receive and process Broadcast Messages  *
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

import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import java.util.HashMap;
import java.util.List;

import static java.lang.String.valueOf;

public class BLE_BroadcastReceiver extends BroadcastReceiver {

    private final static String TAG = BLE_BroadcastReceiver.class.getSimpleName();
    private boolean mConnected;
    private boolean mServiceDiscovered;
    private IntentFilter intentFilter;
    private int connection_count = 0;
    private List<BluetoothGattService> gattServices;
    private HashMap<String, String> BLE_statusMap = new HashMap<String, String>();

    public BLE_BroadcastReceiver() {
        mConnected = false;
        mServiceDiscovered = false;
        intentFilter = new IntentFilter();
        makeGattUpdateIntentFilter();
    }

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    /*  private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() { */
    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();

        connection_count++;
        switch (action) {
            case BLE_Service.ACTION_GATT_CONNECTED:
                mConnected = true;
                Log.i(TAG, "connected " + String.valueOf(mConnected));
                BLE_statusMap.put("connected", String.valueOf(mConnected));
                try {
                    DeviceControlActivity.getInstace().updateBLEStatus(BLE_statusMap);
                } catch (Exception e) {

                }
                break;
            case BLE_Service.ACTION_GATT_DISCONNECTED:
                mConnected = false;
                mServiceDiscovered = false;
                BLE_statusMap.put("connected", String.valueOf(mConnected));
                BLE_statusMap.put("Service_Discovered", String.valueOf(mServiceDiscovered));
                try {
                    DeviceControlActivity.getInstace().updateBLEStatus(BLE_statusMap);
                } catch (Exception e) {

                }
                break;
            case BLE_Service.ACTION_GATT_SERVICES_DISCOVERED:
                mServiceDiscovered = true;
                BLE_statusMap.put("Service_Discovered", String.valueOf(mServiceDiscovered));
                try {
                    DeviceControlActivity.getInstace().updateBLEStatus(BLE_statusMap);
                } catch (Exception e) {

                }
                break;
            case BLE_Service.ACTION_DATA_AVAILABLE:
                Log.i(TAG, "BLE Broadcast Receiver Action Data available :" + action);

                if (intent.hasExtra(BLE_Service.EXTRA_TIME)) {
                    Log.i(TAG, "BLE Broadcast Receiver Extra Time :" + intent.getExtras().getString("EXTRA_TIME"));
                    DeviceControlActivity.getInstace().updateUI();
                    //setToggleButtonState(R.id.button0_value, intent.getIntExtra(MainActivity.EXTRA_BUTTON0, 0));
                } else if (intent.hasExtra(BLE_Service.EXTRA_SOUND)) {
                    Log.i(TAG, "BLE Broadcast Receiver Extra Sound :" + intent.getExtras().getString("EXTRA_SOUND"));
                    DeviceControlActivity.getInstace().updateSoundUI();
                } else if (intent.hasExtra(BLE_Service.EXTRA_LIGHT)) {
                    Log.i(TAG, "BLE Broadcast Receiver Extra Light :" + intent.getExtras().getString("EXTRA_LIGHT"));
                    DeviceControlActivity.getInstace().updateLightUI();
                }
                else if(intent.hasExtra(BLE_Service.EXTRA_NOTIFICATION)) {
                    DeviceControlActivity.getInstace().updateSteps(intent.getStringExtra(BLE_Service.EXTRA_NOTIFICATION));
                }

 /*                   else if(intent.hasExtra(MainActivity.EXTRA_NOTIFICATION)) {
                        displayData1(intent.getStringExtra(MainActivity.EXTRA_NOTIFICATION));
                    }

                    else {
                        displayData(intent.getStringExtra(MainActivity.EXTRA_DATA));
                    }*/
                break;
            case BLE_Service.ACTION_WRITE_SUCCESS:
                //displayData("");
                break;
        }

        try {
            //DeviceControlActivity.getInstace().updateConnectionStatus(valueOf(connection_count));
        } catch (Exception e) {

        }
        //       }
    }

    public boolean getConneted() {
        return mConnected;
    }

    private void makeGattUpdateIntentFilter() {

        intentFilter.addAction(BLE_Service.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BLE_Service.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BLE_Service.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BLE_Service.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(BLE_Service.ACTION_WRITE_SUCCESS);
    }

    public IntentFilter getIntentFilter() {
        return intentFilter;
    }
}

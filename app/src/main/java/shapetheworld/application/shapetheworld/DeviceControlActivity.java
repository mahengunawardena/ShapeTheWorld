//============================================================================
// Name        : DeviceControlActivity.java
// Author      : Mahendra Gunawardena
// Date        : 10/2/2017
// Version     : Rev 0.01
// Copyright   : Your copyright notice
// Description : DeviceControlActivity UI for controlling the BLE device
//============================================================================
/*
 * DeviceControlActivity.java
 * Implementation of a DeviceControlActivity control class to interface to scan for View and Control BLE Devices
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

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;


public class DeviceControlActivity extends AppCompatActivity {

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    private final static String TAG = DeviceControlActivity.class.getSimpleName();

    private String mDeviceName;
    private String mDeviceAddress;
    private TextView mConnectionState;
    private TextView tvTime;
    private TextView tvSound;
    private TextView tvTemperature;
    private TextView tvLight;
    private TextView tvNumberOfSteps;

    private BluetoothGattCharacteristic characteristic_time;
    private BluetoothGattCharacteristic characteristic_sound;
    private BluetoothGattCharacteristic characteristic_light;
    private BluetoothGattCharacteristic characteristic_plot;
    private BluetoothGattCharacteristic characteristic_number_of_steps;


    private EditText mDataField;
    private BLE_Service mBLE_Service;
    private BLE_BroadcastReceiver mbroadcastReceiver;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    private HashMap<String, BluetoothGattCharacteristic> mGattCharacteristicMap = new HashMap<>();

    private static DeviceControlActivity ins;

    private LineGraphSeries<DataPoint> series;
    private GraphView graph;
    private DataPoint[] iGraphData;
    private int index = 0;
    private int count =100;

    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.devicecontrolactivity);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ins = this;

        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
        ((TextView) findViewById(R.id.tv_address)).setText(mDeviceAddress);
        //((TextView) findViewById(R.id.tv_device_name)).setText(mDeviceName);

        final Button btnTime = (Button) findViewById(R.id.btnTime);
        final Button btnSound = (Button) findViewById(R.id.btnSound);
        final Button btnTemperture = (Button) findViewById(R.id.btnTemperature);
        final Button btnLight = (Button) findViewById(R.id.btnLight);

        tvTime = (TextView) findViewById(R.id.tvTime);
        tvSound = (TextView) findViewById(R.id.tvSound);
        tvTemperature = (TextView) findViewById(R.id.tvTemperature);
        tvLight = (TextView) findViewById(R.id.tvLight);
        tvNumberOfSteps = (TextView) findViewById(R.id.tv_steps);

        iGraphData = new DataPoint[count];
        graph = (GraphView) findViewById(R.id.graph);
        series = new LineGraphSeries<DataPoint>();
        initGraph();

        mbroadcastReceiver = new BLE_BroadcastReceiver();
        Intent gattServiceIntent = new Intent(getApplicationContext(), BLE_Service.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        btnTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTime();
            }
        });

        btnSound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSound();
            }
        });

        btnTemperture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTemperatue();
            }
        });

        btnLight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLight();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mbroadcastReceiver, mbroadcastReceiver.getIntentFilter());
        if (mBLE_Service != null) {
            final boolean result = mBLE_Service.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
        Log.i(TAG, "Reached onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mbroadcastReceiver);
    }

    public static DeviceControlActivity getInstace() {
        return ins;
    }

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            BLE_Service.LocalBinder mLocalBinder = (BLE_Service.LocalBinder) service;
            mBLE_Service = mLocalBinder.getService();
            //mBLE_Service = ((BLE_Service.LocalBinder) service).getService();

            if (!mBLE_Service.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBLE_Service.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBLE_Service.close();
            mBLE_Service = null;
        }
    };

    public void updateUI() {
        //para to function
        //final HashMap<String, String> mBLE_statusMap
        DeviceControlActivity.this.runOnUiThread(new Runnable() {
            public void run() {
                final byte[] data = characteristic_time.getValue();
                if (data != null && data.length > 0) {
                    final StringBuilder stringBuilder = new StringBuilder(data.length);
                    int value = 0;
                    int number = 0;
                    for (byte byteChar : data) {
                        stringBuilder.append(String.format("%02x ", byteChar));
                        number = byteChar;
                        value = value * 255;
                        if (number < 0) {
                            number = 256 + number;
                        }
                        value = value + number;
                    }

                    if (index < count) {
                        updateGraphData(index, value);
                    }
                    if (data != null) {
                        tvTime.setText(Integer.toString(value));
                    } else {
                        tvTime.setText("0");
                    }
                    index++;
                }
            }
        });
        if (index >= count) {
            updateGraph();
        }
    }

    public void updateSteps(String text) {
        //para to function
        //final HashMap<String, String> mBLE_statusMap
/*        DeviceControlActivity.this.runOnUiThread(new Runnable() {
            public void run() { */

        String input = text.replace(" ", "");
        int len = input.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(input.charAt(i), 16) << 4)
                    + Character.digit(input.charAt(i + 1), 16));
        }
        //final byte[] data = input.getBytes();//characteristic_time.getValue();
        if (data != null && data.length > 0) {
            final StringBuilder stringBuilder = new StringBuilder(data.length);
            int value = 0;
            int number = 0;
            for (byte byteChar : data) {
                stringBuilder.append(String.format("%02x ", byteChar));
                number = byteChar;
                value = value * 255;
                if (number < 0) {
                    number = 256 + number;
                }
                value = value + number;
            }
            if (data != null) {
                tvNumberOfSteps.setText("Number of Steps: " + Integer.toString(value));
            } else {
                tvNumberOfSteps.setText("0");
            }
        }
    }


    public void updateSoundUI() {
        //para to function
        //final HashMap<String, String> mBLE_statusMap
        DeviceControlActivity.this.runOnUiThread(new Runnable() {
            public void run() {
                final byte[] data = characteristic_sound.getValue();
                if (data != null && data.length > 0) {
                    final StringBuilder stringBuilder = new StringBuilder(data.length);
                    int value = 0;
                    int number = 0;
                    for (byte byteChar : data) {
                        stringBuilder.append(String.format("%02x ", byteChar));
                        number = byteChar;
                        value = value * 255;
                        if (number < 0) {
                            number = 256 + number;
                        }
                        value = value + number;
                    }
                    //intent.putExtra(EXTRA_DATA, new String(data) + "\n" + stringBuilder.toString());

                    if (index < count) {
                        updateGraphData(index, value);
                    }
                    if (data != null) {
                        tvSound.setText(Integer.toString(value));
                    } else {
                        tvSound.setText("0");
                    }
                    index++;
                }
            }
        });
        if (index >= count) {
            updateGraph();
        }
    }

    public void updateLightUI() {
        //para to function
        //final HashMap<String, String> mBLE_statusMap
        DeviceControlActivity.this.runOnUiThread(new Runnable() {
            public void run() {
                final byte[] data = characteristic_light.getValue();
                if (data != null && data.length > 0) {
                    final StringBuilder stringBuilder = new StringBuilder(data.length);
                    int value = 0;
                    int number = 0;
                    for (byte byteChar : data) {
                        stringBuilder.append(String.format("%02x ", byteChar));
                        number = byteChar;
                        value = value * 255;
                        if (number < 0) {
                            number = 256 + number;
                        }
                        value = value + number;
                    }
                    if (index < count) {
                        updateGraphData(index, value);
                    }
                    if (data != null) {
                        tvLight.setText(Integer.toString(value));
                    } else {
                        tvLight.setText("0");
                    }
                    index++;
                }
            }
        });
        if (index >= count) {
            updateGraph();
        }
    }

    public void updateBLEStatus(final HashMap<String, String> mBLE_statusMap) {
        DeviceControlActivity.this.runOnUiThread(new Runnable() {
            public void run() {
                if (mBLE_statusMap.containsKey("connected")) {
                    TextView textV1 = (TextView) findViewById(R.id.tv_connected);
                    textV1.setText("Connected :" + mBLE_statusMap.get("connected"));
                }
                if (mBLE_statusMap.containsKey("Service_Discovered")) {
                    initGattServiceUI(mBLE_Service.getGattServices());
                }
/*
                TextView textV1 = (TextView) findViewById(R.id.textConnected);
                textV1.setText("Connected :" +t);
*/
            }
        });
    }

    /**
     * Iterate through the supported GATT Services/Characteristics,
     * and initialize UI elements displaying them.
     * <p>
     * Display Gatt Service
     */
    private void initGattServiceUI(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        String uuid = null;
        String unknownServiceString = getResources().getString(R.string.unknown_service);
        String unknownCharaString = getResources().getString(R.string.unknown_characteristic);
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData = new ArrayList<ArrayList<HashMap<String, String>>>();
        mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData = new HashMap<String, String>();
            uuid = gattService.getUuid().toString();
            currentServiceData.put(LIST_NAME, ShapetheWorldAttribute.lookup(uuid, unknownServiceString));
            currentServiceData.put(LIST_UUID, uuid);
            gattServiceData.add(currentServiceData);

            ArrayList<HashMap<String, String>> gattCharacteristicGroupData = new ArrayList<HashMap<String, String>>();
            List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas = new ArrayList<BluetoothGattCharacteristic>();

            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                charas.add(gattCharacteristic);
                HashMap<String, String> currentCharaData = new HashMap<String, String>();
                uuid = gattCharacteristic.getUuid().toString();
                currentCharaData.put(LIST_NAME, ShapetheWorldAttribute.lookup(uuid, unknownCharaString));
                currentCharaData.put(LIST_UUID, uuid);
                gattCharacteristicGroupData.add(currentCharaData);
                mGattCharacteristicMap.put(ShapetheWorldAttribute.lookup(uuid, unknownCharaString), gattCharacteristic);
            }
            mGattCharacteristics.add(charas);
            gattCharacteristicData.add(gattCharacteristicGroupData);
        }
        Log.i(TAG, "Init Gatt Service UI");
        characteristic_time = mGattCharacteristicMap.get("Time");
        characteristic_sound = mGattCharacteristicMap.get("Sound");
        characteristic_light = mGattCharacteristicMap.get("Light");
        characteristic_number_of_steps = mGattCharacteristicMap.get("Number of Steps");
    }

    public void updateConnectionStatus(final int resourceId) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int resourceId;
                if (mbroadcastReceiver.getConneted()) {
                    resourceId = R.string.connected;
                } else {
                    resourceId = R.string.disconnected;
                }
                mConnectionState.setText(resourceId);
                invalidateOptionsMenu();
            }
        });
    }

    private void clearUI() {
        //mGattServicesList.setAdapter((SimpleExpandableListAdapter) null);
        mDataField.setText(R.string.no_data);
    }


    private void showTime() {

        Log.i(TAG, "Show Time");

        final BluetoothGattCharacteristic characteristic_select = mGattCharacteristicMap.get("PlotState");
        if (characteristic_select != null) {
            final int charaProp = characteristic_select.getProperties();
            if ((charaProp | BluetoothGattCharacteristic.PROPERTY_WRITE) > 0) {
                String strData = "00";
                int len = strData.length();
                byte[] data = new byte[len / 2];
                for (int i = 0; i < len; i += 2) {
                    data[i / 2] = (byte) ((Character.digit(strData.charAt(i), 16) << 4)
                            + Character.digit(strData.charAt(i + 1), 16));
                }
                characteristic_select.setValue(data);
                mBLE_Service.writeCharacteristic(characteristic_select);
            }
        }

        index = 0;

        if (characteristic_time != null) {
            final int charaProp = characteristic_time.getProperties();
            if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                for (int i = 0; i < count; i++) {
                    mBLE_Service.readCharacteristic(characteristic_time);
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {

                        }
                    }, 5000);
                }
            }
        }
    }

    private void showSound() {

        Log.i(TAG, "Show Sound");

        if (characteristic_time != null) {
            final int charaProp = characteristic_sound.getProperties();
            if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                for (int i = 0; i < count; i++) {
                    mBLE_Service.readCharacteristic(characteristic_sound);
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {

                        }
                    }, 5000);
                }
            }
        }

        index = 0;

        final BluetoothGattCharacteristic characteristic_select = mGattCharacteristicMap.get("PlotState");
        if (characteristic_select != null) {
            final int charaProp = characteristic_select.getProperties();
            if ((charaProp | BluetoothGattCharacteristic.PROPERTY_WRITE) > 0) {
                String strData = "01";
                int len = strData.length();
                byte[] data = new byte[len / 2];
                for (int i = 0; i < len; i += 2) {
                    data[i / 2] = (byte) ((Character.digit(strData.charAt(i), 16) << 4)
                            + Character.digit(strData.charAt(i + 1), 16));
                }
                characteristic_select.setValue(data);
                mBLE_Service.writeCharacteristic(characteristic_select);
            }
        }
    }

    private void showTemperatue() {

    }

    private void showLight() {
        Log.i(TAG, "Show Light");
        final BluetoothGattCharacteristic characteristic_select = mGattCharacteristicMap.get("PlotState");
        if (characteristic_select != null) {
            final int charaProp = characteristic_select.getProperties();
            if ((charaProp | BluetoothGattCharacteristic.PROPERTY_WRITE) > 0) {
                String strData = "03";
                int len = strData.length();
                byte[] data = new byte[len / 2];
                for (int i = 0; i < len; i += 2) {
                    data[i / 2] = (byte) ((Character.digit(strData.charAt(i), 16) << 4)
                            + Character.digit(strData.charAt(i + 1), 16));
                }
                characteristic_select.setValue(data);
                mBLE_Service.writeCharacteristic(characteristic_select);
            }
        }

        if (characteristic_light != null) {
            final int charaProp = characteristic_light.getProperties();
            if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                for (int i = 0; i < count; i++) {
                    mBLE_Service.readCharacteristic(characteristic_light);
                    final byte[] data = characteristic_light.getValue();
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                        }
                    }, 1000);
                }
            }
        }
        index = 0;
    }

    private void initGraph() {
        int value;
        value = 1;
        for (int i = 0; i < count; i++) {
            series.appendData(new DataPoint(i, value), true, 1000);
            value++;
        }
        graph.addSeries(series);
    }

    private void updateGraphData(int index, int value) {
        DataPoint v = new DataPoint(index, value);
        iGraphData[index] = v;
    }

    private void updateGraph() {

        series.resetData(iGraphData);
    }

}


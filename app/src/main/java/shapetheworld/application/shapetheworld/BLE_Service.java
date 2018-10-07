//============================================================================
// Name        : BLE_Service.java
// Author      : Mahendra Gunawardena
// Date        : 10/3/2017
// Version     : Rev 0.01
// Copyright   : Your copyright notice
// Description : BLE_Service for managing the BLE device
//============================================================================
/*
 * DeviceControlActivity.java
 * Implementation of a BLE_Service for managing the BLE device. This includes Initialzation
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

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;

import static android.bluetooth.BluetoothAdapter.STATE_CONNECTING;

public class BLE_Service extends Service {

    private final static String TAG = BLE_Service.class.getSimpleName();
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothManager mBluetoothManager;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;
    private List<BluetoothGattService> mGattServices;

    // Queue for both charateristic and descriptors
    private Queue<BluetoothGattDescriptor> descriptorWriteQueue = new LinkedList<>();
    private Queue<BluetoothGattCharacteristic> characteristicQueue = new LinkedList<>();
    private Queue<BluetoothGattCharacteristic> characteristicWriteQueue = new LinkedList<>();

    private int mConnectionState = STATE_DISCONNECTED;

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    public final static String ACTION_GATT_CONNECTED =
            "shapetheworld.application.shapetheworld.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "shapetheworld.application.shapetheworld.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "shapetheworld.application.shapetheworld.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "shapetheworld.application.shapetheworld.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "shapetheworld.application.shapetheworld.EXTRA_DATA";
    public final static String ACTION_WRITE_SUCCESS =
            "shapetheworld.application.shapetheworld.ACTION_WRITE_SUCCESS";

    // Intent extras
    public final static String EXTRA_TIME =
            "shapetheworld.application.shapetheworld.EXTRA_TIME";
    public final static String EXTRA_SOUND =
            "shapetheworld.application.shapetheworld.EXTRA_SOUND";
    public final static String EXTRA_LIGHT =
            "shapetheworld.application.shapetheworld.EXTRA_LIGHT";
    public final static String EXTRA_NOTIFICATION =
            "shapetheworld.application.shapetheworld.EXTRA_NOTIFICATION";

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        return true;
    }

    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = ACTION_GATT_CONNECTED;
                mConnectionState = STATE_CONNECTED;
                broadcastUpdate(intentAction);
                Log.i(TAG, "Connected to GATT server.");
                // Attempts to discover services after successful connection.
                Log.i(TAG, "Attempting to start service discovery:" +
                        mBluetoothGatt.discoverServices());

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;
                Log.i(TAG, "Disconnected from GATT server.");
                broadcastUpdate(intentAction);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
                // Enable notification on button services
                enableNumberOfStepsNotifications(gatt);
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            Log.w(TAG, "onServicesDiscovered received: " + status);
            // Read action has finished, remove from queue
            characteristicQueue.remove();

            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }

            // Handle the next element from the queues
            if(characteristicQueue.size() > 0)
                mBluetoothGatt.readCharacteristic(characteristicQueue.element());
            else if (characteristicWriteQueue.size()>0)
                mBluetoothGatt.writeCharacteristic(characteristicWriteQueue.element());
            else if(descriptorWriteQueue.size() > 0)
                mBluetoothGatt.writeDescriptor(descriptorWriteQueue.element());
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            Log.w(TAG, "onCharacteristicWrite :" + status);

            characteristicWriteQueue.remove();

            if (status==BluetoothGatt.GATT_SUCCESS){
                broadcastUpdate(ACTION_WRITE_SUCCESS, characteristic);
                Log.w(TAG, "onCharacteristicWrite (Succcess):" + status);
            } else {
                Log.w(TAG, "onCharacteristicWrite (Failed):" + status);
            }

            if (characteristicWriteQueue.size()>0)
                mBluetoothGatt.writeCharacteristic(characteristicWriteQueue.element());
            else if(characteristicQueue.size() > 0)
                mBluetoothGatt.readCharacteristic(characteristicQueue.element());
            else if(descriptorWriteQueue.size() > 0)
                mBluetoothGatt.writeDescriptor(descriptorWriteQueue.element());
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
        }

        /**
         * Enable notifications automatic Callbacks
         */
        private void enableNumberOfStepsNotifications(BluetoothGatt gatt) {
            // Loop through the characteristics for the button service
            for(BluetoothGattCharacteristic characteristic : gatt.getService(UUID.fromString(ShapetheWorldAttribute.EDX_SERVICE)).getCharacteristics()){
                // Enable notification on the characteristic
                final int charaProp = characteristic.getProperties();
                //if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                if ((charaProp & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0) {
                    setCharacteristicNotification(characteristic, true);
                }
            }
        }
    };

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     * @return Return true if the connection is initiated successfully. The connection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public boolean connect(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        // Previously connected device.  Try to reconnect.
        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                mConnectionState = STATE_CONNECTING;
                return true;
            } else {
                return false;
            }
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        //mGattServices = mBluetoothGatt.getServices();
        Log.d(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        mConnectionState = STATE_CONNECTING;
        return true;
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
    }


    public class LocalBinder extends Binder {
        BLE_Service getService() {
            return BLE_Service.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }



    @Override
    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        close();
        return super.onUnbind(intent);
    }

    private final IBinder mBinder = new LocalBinder();

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);

        if ((UUID.fromString(ShapetheWorldAttribute.PLOT_STATE)).equals(characteristic.getUuid())) {
            // State of button 0 has changed. Add id and value to broadcast
            //intent.putExtra(EXTRA_BUTTON0, characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8,0));
        }
        else if ((UUID.fromString(ShapetheWorldAttribute.TIME_STATE)).equals(characteristic.getUuid())) {
            //  Seeking the new Time value, Add id, get request and broadcast
            intent.putExtra(EXTRA_TIME, characteristic.getValue());
        }
        else if ((UUID.fromString(ShapetheWorldAttribute.SOUND_STATE)).equals(characteristic.getUuid())) {
            //  Seeking the new Sound value, Add id, get request and broadcast
            intent.putExtra(EXTRA_SOUND, characteristic.getValue());
        }
        else if ((UUID.fromString(ShapetheWorldAttribute.LIGHT_STATE)).equals(characteristic.getUuid())) {
            //  Seeking the new light value, Add id, get request and broadcast
            intent.putExtra(EXTRA_LIGHT, characteristic.getValue());
        }
        else if ((UUID.fromString(ShapetheWorldAttribute.NUMBER_OF_STEPS)).equals(characteristic.getUuid())) {
            // State of led 0 has changed. Add id and value to broadcast
            final byte[] data = characteristic.getValue();
            if (data != null && data.length > 0) {
                final StringBuilder stringBuilder = new StringBuilder(data.length);
                for(byte byteChar : data)
                    stringBuilder.append(String.format("%02X ", byteChar));
                intent.putExtra(EXTRA_NOTIFICATION, new String(stringBuilder.toString()));
            }
        }


        // For all other profiles, writes the data formatted in HEX.
/*
        final byte[] data = characteristic.getValue();
        if (data != null && data.length > 0) {
            final StringBuilder stringBuilder = new StringBuilder(data.length);
            for (byte byteChar : data)
                stringBuilder.append(String.format("%02X ", byteChar));
            intent.putExtra(EXTRA_DATA, new String(data) + "\n" + stringBuilder.toString());
        }
*/
        sendBroadcast(intent);
    }


    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);

        sendBroadcast(intent);
    }

    public List<BluetoothGattService> getGattServices() {
        if (mBluetoothGatt == null) return null;

        return mBluetoothGatt.getServices();
    }

    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read result is reported
     * asynchronously through the {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }

        // Queue the characteristic to read, since several reads are done on startup
        characteristicQueue.add(characteristic);

        Log.w(TAG, "readCharacteristic & Queue Size: " + characteristicQueue.size());

        // If there is only 1 item in the queue, then read it. If more than 1, it is handled
        // asynchronously in the callback
        if((characteristicQueue.size() <= 1)) {
            Log.w(TAG, "readCharacteristic & Queue Size <=1 : " + characteristicQueue.size());
            mBluetoothGatt.readCharacteristic(characteristic);
        }
        //mBluetoothGatt.readCharacteristic(characteristic);
    }



    /**
     * Request a write on a given {@code BluetoothGattCharacteristic}. The write result is reported
     * asynchronously through the
     * {@code BluetoothGattCallback#onCharacteristicWrite(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic The characteristic to write to.
     */
    public void writeCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }

        // Queue the characteristic to read, since several reads are done on startup
        characteristicWriteQueue.add(characteristic);

        Boolean status;
        status = mBluetoothGatt.writeCharacteristic(characteristic);
        Log.w(TAG, "writeCharateristic :" + status);
    }

    /**
     * Enable or disable notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enable If true, enable notification. Otherwise, disable it.
     */
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                              boolean enable) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "Bluetooth not initialized");
            return;
        }
        // Enable/disable notification
        mBluetoothGatt.setCharacteristicNotification(characteristic, enable);
        // Write descriptor for notification
        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(ShapetheWorldAttribute.UUID_CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR);
        descriptor.setValue(enable ? BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE : BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
        writeGattDescriptor(descriptor);
    }

    /**
     * Write gatt descriptor if queue is ready.
     */
    private void writeGattDescriptor(BluetoothGattDescriptor d){
        // Add descriptor to the write queue
        descriptorWriteQueue.add(d);
        // If there is only 1 item in the queue, then write it. If more than 1, it will be handled
        // in the onDescriptorWrite callback
        if(descriptorWriteQueue.size() == 1){
            mBluetoothGatt.writeDescriptor(d);
        }
    }
}

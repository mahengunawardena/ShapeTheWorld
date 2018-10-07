//============================================================================
// Name        : BLE_Device.java
// Author      : Mahendra Gunawardena
// Date        : 10/3/2017
// Version     : Rev 0.01
// Copyright   : Your copyright notice
// Description : BLE_Service for managing the BLE device
//============================================================================
/*
 * BLE_Device.java
 * Implementation of a BLE_Device class to store and manage BLE devices druing the BLE Scann process
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

import android.bluetooth.BluetoothDevice;

public class BLE_Device {
    private BluetoothDevice mBluetoothDevice;
    private  int mRSSI;

    public BLE_Device(BluetoothDevice BluetoothDevice) {
        this.mBluetoothDevice = BluetoothDevice;
    }

    public String getAddress() {
        return mBluetoothDevice.getAddress();
    }

    public String getName() {
        return mBluetoothDevice.getName();
    }

    public void setRSSI (int rssi){
        this.mRSSI = rssi;
    }

    public int getRSSI (){
        return mRSSI;
    }

}

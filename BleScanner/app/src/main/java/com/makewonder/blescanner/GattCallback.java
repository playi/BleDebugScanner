package com.makewonder.blescanner;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.util.Log;

/**
 * Created by levis on 5/15/17.
 */

public class GattCallback extends BluetoothGattCallback {

    private final String TAG = "BleScanner";
    private boolean mStartedDiscovery = false;


    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        super.onConnectionStateChange(gatt, status, newState);
        Log.d(TAG,"onConnectionStateChange " + status + ", " + newState);
        if (!mStartedDiscovery) {
            mStartedDiscovery = true;
            gatt.discoverServices();
        }
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        super.onServicesDiscovered(gatt, status);
        Log.d(TAG,"onServicesDiscovered " + status + ", " + gatt.getServices().size());
    }
}

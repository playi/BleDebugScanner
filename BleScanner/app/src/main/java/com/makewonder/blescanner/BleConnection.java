package com.makewonder.blescanner;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.util.Log;
import android.widget.Toast;

import static android.bluetooth.BluetoothGatt.GATT_SUCCESS;
import static android.bluetooth.BluetoothProfile.STATE_CONNECTED;
import static android.bluetooth.BluetoothProfile.STATE_DISCONNECTED;
import static com.makewonder.blescanner.BleConnection.State.CONNECTED;

/**
 * Created by levis on 5/16/17.
 */

public class BleConnection extends BluetoothGattCallback {
    private final String TAG = "BleScanner";

    public enum State {
        INIT,
        CONNECTING,
        CONNECTED_DISCOVERING_SERVICES,
        CONNECTED,
        DISCONNECTING,
        DISCONNECTED
    }

    private final MainActivity mContext;
    private final BluetoothDevice mDevice;

    public int mSuccessCount = 0;
    public int mConnectedCount = 0;
    public int mDisconnectedCount = 0;
    public int mServiceCount = 0;
    public String mDeviceDescription;
    public State mState;
    public BluetoothGatt mGatt;

    public BleConnection(MainActivity context, BluetoothDevice device) {
        mContext = context;
        mDevice = device;

        StringBuilder builder = new StringBuilder();
        builder.append(mDevice.getName());
        builder.append(" ").append(mDevice.getAddress());
        mDeviceDescription = builder.toString();

        mState = State.INIT;
    }

    public void connect() {
        mState = State.CONNECTING;
        mGatt = mDevice.connectGatt(mContext, false, this);
        mContext.updateConnectionsList();
    }

    public void disconnect() {
        mState = State.DISCONNECTING;
        mGatt.disconnect();
        mContext.updateConnectionsList();
        Toast.makeText(mContext, "Disconnecting from  " + mDeviceDescription
                + ".  Tap again to remove from list",
                Toast.LENGTH_LONG).show();
    }

    public void forceDisconnect() {
        mState = State.DISCONNECTED;
        mGatt.close();
        mContext.updateConnectionsList();
        Toast.makeText(mContext, "Forcably disconnected  " + mDeviceDescription
                        + ".  Connection or device may be in indeterminate state",
                Toast.LENGTH_LONG).show();
    }

    public String getAddress() {
        return mDevice.getAddress();
    }




    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        super.onConnectionStateChange(gatt, status, newState);
        Log.d(TAG,"onConnectionStateChange " + mDeviceDescription + ", " + status + ", " + newState);

        if (status == GATT_SUCCESS) {
            mSuccessCount += 1;
        }
        if (newState == STATE_CONNECTED) {
            mConnectedCount += 1;
        } else {
            mDisconnectedCount += 1;
            mGatt.close();
        }

        switch (mState) {
            case CONNECTING:
                if (newState == STATE_CONNECTED) {
                    mState = State.CONNECTED_DISCOVERING_SERVICES;
                    gatt.discoverServices();
                    mContext.updateConnectionsList();
                }
                break;
            default:
                if (newState == STATE_DISCONNECTED) {
                    mState = State.DISCONNECTED;
                    mContext.updateConnectionsList();
                }
        }
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        super.onServicesDiscovered(gatt, status);
        Log.d(TAG,"onServicesDiscovered " + status + ", " + gatt.getServices().size());
        if (mState == State.CONNECTED_DISCOVERING_SERVICES) {
            mState = CONNECTED;
        }
        mServiceCount = gatt.getServices().size();
        mContext.updateConnectionsList();
    }
}

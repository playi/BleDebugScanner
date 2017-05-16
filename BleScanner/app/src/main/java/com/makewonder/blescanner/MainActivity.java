package com.makewonder.blescanner;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "BleScanner";
    private final int SCAN_INTERVAL_MS = 10 * 1000;
    private final int SCAN_TIME_MS = 5 * 1000;

    private BluetoothAdapter mBluetoothAdapter;
    private TextView mScanningText;
    private Handler mHandler;
    private BluetoothLeScanner mBluetoothLeScanner;

    private Runnable mRepeatingScan = new Runnable() {
        @Override
        public void run() {
            try {
                startScanning();
            } finally {
                mHandler.postDelayed(mRepeatingScan, SCAN_INTERVAL_MS);
            }
        }
    };

    private ListView mScanResultsListView;
    private ArrayList<BluetoothDevice> mScannedItems = new ArrayList<BluetoothDevice>();
    private BleScanResultsAdapter mScanResultsAdapter;

    private ListView mConnectionsListView;
    private ArrayList<BleConnection> mConnections = new ArrayList<BleConnection>();
    private BleConnectionsAdapter mConnectionsAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        boolean packageBle = getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
        TextView packageBleText = (TextView) findViewById(R.id.package_ble);
        packageBleText.setText("Package Manager reports BLE System Feature: "
                + (packageBle ? "YES" : "NO"));

        if (!packageBle) return;

        mBluetoothAdapter = ((BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE))
                .getAdapter();

        boolean enabled = mBluetoothAdapter.isEnabled();
        TextView bluetoothEnabledText = (TextView) findViewById(R.id.bluetooth_enabled);
        bluetoothEnabledText.setText(
                "Bluetooth " + (enabled ? "is" : "is not") + " enabled.");
        if (!enabled) return;

//        int permissionCheck = ContextCompat.checkSelfPermission(this, Mainfest.permission);


        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();

        mScanningText = (TextView) findViewById(R.id.scanning);
        mHandler = new Handler();

        mScanResultsAdapter = new BleScanResultsAdapter(this, mScannedItems);
        mScanResultsListView = (ListView) findViewById(R.id.scan_results_list);
        mScanResultsListView.setAdapter(mScanResultsAdapter);
        mScanResultsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                BluetoothDevice device = (BluetoothDevice) adapterView.getAdapter().getItem(i);
                connect(device);
            }
        });

        mConnectionsAdapter = new BleConnectionsAdapter(this, mConnections);
        mConnectionsListView = (ListView) findViewById(R.id.connection_list);
        mConnectionsListView.setAdapter(mConnectionsAdapter);
        mConnectionsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                BleConnection connection = (BleConnection) adapterView.getAdapter().getItem(i);
                if (connection.mState == BleConnection.State.DISCONNECTED) {
                    removeConnection(connection);
                } else if (connection.mState == BleConnection.State.DISCONNECTING) {
                    connection.forceDisconnect();;
                    removeConnection(connection);
                } else {
                    connection.disconnect();
                }
            }
        });


        mRepeatingScan.run();

    }

    private boolean deviceInConnectionList(BluetoothDevice device) {
        for (BleConnection connection : mConnections) {
            if (device.getAddress().equals(connection.getAddress())) {
                return true;
            }
        }
        return false;
    }

    private void connect(BluetoothDevice device) {
        BleConnection newConnection = new BleConnection(this, device);
        if (deviceInConnectionList(device)) {
            Toast.makeText(this, "Already connecting to "
                    + newConnection.mDeviceDescription
                    + ".  Click on device in right side to disconnect or close", Toast.LENGTH_LONG).show();
            return;
        }

        Toast.makeText(this, "Attempting connection to " + newConnection.mDeviceDescription,
                Toast.LENGTH_SHORT).show();
        Log.d(TAG,"Attempting connection to " + newConnection.mDeviceDescription);
        addConnection(newConnection);
        newConnection.connect();
    }

    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);

            ScanRecord record = result.getScanRecord();
            final BluetoothDevice device = result.getDevice();

            if (!mScannedItems.contains(device) && (!deviceInConnectionList(device))) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mScannedItems.add(device);
                        mScanResultsAdapter.notifyDataSetChanged();
                    }
                });
                Log.d(TAG,"Scanned: " + device.toString());
            }

        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
        }
    };

    private void startScanning() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mBluetoothLeScanner.stopScan(mScanCallback);
                mScanningText.setText("Scanned " + mScannedItems.size() + " item(s).");
            }
        }, SCAN_TIME_MS);
        mScannedItems.clear();
        mScanResultsAdapter.notifyDataSetChanged();
        mBluetoothLeScanner.startScan(mScanCallback);
        mScanningText.setText("Scanning...");

    }

    private void addConnection(final BleConnection connection) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mConnections.add(connection);
                mConnectionsAdapter.notifyDataSetChanged();
            }
        });
    }

    private void removeConnection(final BleConnection connection) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mConnections.remove(connection);
                mConnectionsAdapter.notifyDataSetChanged();
            }
        });
    }

    public void updateConnectionsList() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mConnectionsAdapter.notifyDataSetChanged();
            }
        });
    }
}

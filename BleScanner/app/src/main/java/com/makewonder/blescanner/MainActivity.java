package com.makewonder.blescanner;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.internal.widget.AdapterViewCompat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

    private final ParcelUuid D1_UUID = ParcelUuid.fromString("c1d9850e-cade-491f-8661-9d87777723af");
    private final ParcelUuid D2_UUID = ParcelUuid.fromString("c1d9850e-cade-491f-8661-9d87787723af");

    private GattCallback mGattCallback;
    private BluetoothGatt mGatt;


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
//        mScanResultsAdapter = new ArrayAdapter<BluetoothDevice>(this, R.layout.scanned_item, R.id.scanned_item_name, mScannedItems);

        mScanResultsListView = (ListView) findViewById(R.id.scan_results_list);
        mScanResultsListView.setAdapter(mScanResultsAdapter);
        mScanResultsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                BluetoothDevice device = (BluetoothDevice) adapterView.getAdapter().getItem(i);
                Connect(device);
            }
        });

        mGattCallback = new GattCallback();

        mRepeatingScan.run();

    }

    private void Connect(BluetoothDevice device) {
        Toast.makeText(this, "Attempting connection to " + device.getName(), Toast.LENGTH_SHORT).show();
        Log.d(TAG,"Attempting connection to " + device.getName());
        mGatt = device.connectGatt(this, false, mGattCallback);
    }

    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);

            ScanRecord record = result.getScanRecord();
            final BluetoothDevice device = result.getDevice();

            boolean foundValidUuid = false;

            ParcelUuid[] uuids = device.getUuids();

            if (uuids == null) {
                Log.d(TAG, "Null UUIDs for device " + device.getName() + " " + device.toString());
                return;
            }

            for (ParcelUuid uuid : uuids) {
                if (uuid.equals(D1_UUID) || uuid.equals(D2_UUID)) {
                    foundValidUuid = true;
                    break;
                }
            }
            if (!foundValidUuid) {
                Log.d(TAG, "No valid UUIDs for device " + device.getName() + " " + device.toString());
                for (ParcelUuid uuid : uuids) {
                    Log.d(TAG, device.getName() + ": " + uuid);
                }
                return;
            }

            if (!mScannedItems.contains(device)) {
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
        mBluetoothLeScanner.startScan(mScanCallback);
        mScanningText.setText("Scanning...");

    }


}

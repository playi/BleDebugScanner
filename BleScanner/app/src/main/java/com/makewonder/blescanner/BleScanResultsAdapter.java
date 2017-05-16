package com.makewonder.blescanner;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by levis on 5/15/17.
 */

public class BleScanResultsAdapter extends BaseAdapter {

    private final ArrayList<BluetoothDevice> mDeviceArray;
    private final Context mContext;

    public BleScanResultsAdapter(Context context, ArrayList<BluetoothDevice> deviceArray) {
        mContext = context;
        mDeviceArray = deviceArray;
    }

    @Override
    public int getCount() {
        return mDeviceArray.size();
    }

    @Override
    public Object getItem(int i) {
        return mDeviceArray.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.scanned_item, viewGroup, false);
        }
        TextView textView = (TextView) view.findViewById(R.id.scanned_item_name);
        textView.setText(mDeviceArray.get(i).getName() + " " + mDeviceArray.get(i).toString());
        return view;
    }
}

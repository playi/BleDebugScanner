package com.makewonder.blescanner;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by levis on 5/16/17.
 */

public class BleConnectionsAdapter extends BaseAdapter {
    private final ArrayList<BleConnection> mConnectionArray;
    private Context mContext;

    public BleConnectionsAdapter(Context context, ArrayList<BleConnection> connectionArray) {
        mContext = context;
        mConnectionArray = connectionArray;
    }

    @Override
    public int getCount() {
        return mConnectionArray.size();
    }

    @Override
    public Object getItem(int i) {
        return mConnectionArray.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.connection_item, viewGroup, false);
        }
        BleConnection connection = (BleConnection) mConnectionArray.get(i);

        TextView textView = (TextView) view.findViewById(R.id.connection_description);
        textView.setText(connection.mDeviceDescription);

        textView = (TextView) view.findViewById(R.id.connection_state);
        if (connection.mState == BleConnection.State.CONNECTED) {
            textView.setText("Connection state: " + connection.mState.toString()
                    + " reporting " + connection.mServiceCount + " services");
        } else {
            textView.setText("Connection state: " + connection.mState.toString());
        }

        textView = (TextView) view.findViewById(R.id.success_count);
        textView.setText("Operation success count: " + connection.mSuccessCount);

        textView = (TextView) view.findViewById(R.id.connected_count);
        textView.setText("Connection message count: " + connection.mConnectedCount);

        textView = (TextView) view.findViewById(R.id.disconnected_count);
        textView.setText("Disconnected message count: " + connection.mDisconnectedCount);

        return view;
    }
}

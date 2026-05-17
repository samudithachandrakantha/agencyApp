package com.hfad.agencyapp.utils;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.OutputStream;
import java.util.UUID;

public class BluetoothPrinterHelper {

    private static final String TAG = "BluetoothPrinter";
    private static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private BluetoothSocket socket;

    @SuppressLint("MissingPermission")
    public boolean connect(BluetoothDevice device) {
        try {
            socket = device.createRfcommSocketToServiceRecord(SPP_UUID);
            socket.connect();
            Log.d(TAG, "Connected to " + device.getName());
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Connection failed", e);
            return false;
        }
    }

    public boolean printText(String text) {
        if (socket == null || !socket.isConnected()) {
            Log.e(TAG, "Socket not connected");
            return false;
        }

        try {
            OutputStream os = socket.getOutputStream();
            os.write(text.getBytes());
            os.flush();
            Log.d(TAG, "Text printed successfully");
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Print failed", e);
            return false;
        }
    }

    public void disconnect() {
        try {
            if (socket != null) {
                socket.close();
                socket = null;
                Log.d(TAG, "Disconnected");
            }
        } catch (Exception e) {
            Log.e(TAG, "Disconnect failed", e);
        }
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected();
    }

    @SuppressLint("MissingPermission")
    public static BluetoothDevice findPairedPrinter(BluetoothAdapter adapter, String namePrefix) {
        if (adapter == null) return null;
        for (BluetoothDevice device : adapter.getBondedDevices()) {
            if (device.getName() != null && device.getName().startsWith(namePrefix)) {
                return device;
            }
        }
        return null;
    }

    @SuppressLint("MissingPermission")
    public static BluetoothDevice findPrinterByName(BluetoothAdapter adapter, String name) {
        if (adapter == null) return null;
        for (BluetoothDevice device : adapter.getBondedDevices()) {
            if (device.getName() != null && device.getName().equals(name)) {
                return device;
            }
        }
        return null;
    }
}




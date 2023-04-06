package com.nosugar.ble

import android.bluetooth.BluetoothDevice


class ScannedDevice(val device: BluetoothDevice, var lastSeen: Long, var rssi: Int)

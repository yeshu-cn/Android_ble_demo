package com.nosugar.ble

import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.Context
import android.util.Log
import java.util.*

@SuppressLint("MissingPermission")
class MyGattClientCallback(private val onReceiveMessage: (message: String) -> Unit) : BluetoothGattCallback() {
    private var mGatt: BluetoothGatt? = null
    private var mService: BluetoothGattService? = null
    private var mCharacteristics: BluetoothGattCharacteristic? = null

    fun connectToGattServer(context: Context, device: BluetoothDevice) {
        mGatt = device.connectGatt(context, false, this)
    }

    fun disconnect() {
        mGatt?.disconnect()
    }

    override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
        super.onConnectionStateChange(gatt, status, newState)
        Log.d(TAG, "onConnectionStateChange: $newState")
        if (newState == BluetoothProfile.STATE_CONNECTED) {
            Log.d(TAG, "Connected to GATT server.")
            gatt.discoverServices()
        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
            Log.d(TAG, "Disconnected from GATT server.")
        }
    }

    override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
        super.onServicesDiscovered(gatt, status)
        Log.d(TAG, "onServicesDiscovered.")
        mService = gatt.getService(UUID.fromString(BleConfig.SERVICE_UUID))
        mCharacteristics = mService?.getCharacteristic(UUID.fromString(BleConfig.CHARACTERISTIC_UUID))
    }

    override fun onCharacteristicChanged(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic
    ) {
        Log.d(TAG, "onCharacteristicChanged")
        super.onCharacteristicChanged(gatt, characteristic)
        val response = String(characteristic.value)
        onReceiveMessage(response)
    }

    override fun onCharacteristicRead(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        value: ByteArray,
        status: Int
    ) {
        Log.d(TAG, "onCharacteristicRead")
        super.onCharacteristicRead(gatt, characteristic, value, status)
    }

    override fun onCharacteristicWrite(
        gatt: BluetoothGatt?,
        characteristic: BluetoothGattCharacteristic?,
        status: Int
    ) {
        Log.d(TAG, "onCharacteristicWrite")
    }

    fun sendMsg(msg: String) {
        mCharacteristics!!.value = msg.toByteArray()
        mGatt!!.setCharacteristicNotification(mCharacteristics, true)
        mGatt!!.writeCharacteristic(mCharacteristics)
    }

    companion object {
        private const val TAG = "MyGattClientCallback"
    }
}

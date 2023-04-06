package com.nosugar.ble

import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.Context
import android.util.Log
import java.util.*


@SuppressLint("MissingPermission")
class MyGattServerCallback(private val onReadCallback: (message: String) -> Unit, private val onWriteCallback: (message: String) -> Unit) :
    BluetoothGattServerCallback() {
    private lateinit var mGattServer: BluetoothGattServer

    fun startGattServer(context: Context) {
        val bluetoothManager =
            context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        mGattServer = bluetoothManager.openGattServer(context, this)
        val service = BluetoothGattService(
            UUID.fromString(BleConfig.SERVICE_UUID),
            BluetoothGattService.SERVICE_TYPE_PRIMARY
        )
        val writeCharacteristic = BluetoothGattCharacteristic(
            UUID.fromString(BleConfig.CHARACTERISTIC_UUID),
            BluetoothGattCharacteristic.PROPERTY_READ or BluetoothGattCharacteristic.PROPERTY_WRITE or BluetoothGattCharacteristic.PROPERTY_NOTIFY,
            BluetoothGattCharacteristic.PERMISSION_READ or BluetoothGattCharacteristic.PERMISSION_WRITE
        )
        val descriptor = BluetoothGattDescriptor(
            UUID.fromString(BleConfig.CHARACTERISTIC_DESCRIPTOR_UUID),
            BluetoothGattDescriptor.PERMISSION_WRITE or BluetoothGattDescriptor.PERMISSION_READ
        )
        writeCharacteristic.addDescriptor(descriptor)

        service.addCharacteristic(writeCharacteristic)
        mGattServer.addService(service)
    }

    fun stopGattServer() {
        mGattServer.close()
    }

    override fun onConnectionStateChange(device: BluetoothDevice?, status: Int, newState: Int) {
        Log.i("MyGattServerCallback", "onConnectionStateChange")
        if (newState == BluetoothProfile.STATE_CONNECTED) {
            Log.i("MyGattServerCallback", "Device connected")
        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
            Log.i("MyGattServerCallback", "Device disconnected")
        }
    }

    override fun onCharacteristicReadRequest(
        device: BluetoothDevice?, requestId: Int, offset: Int,
        characteristic: BluetoothGattCharacteristic
    ) {
        Log.i("MyGattServerCallback", "onCharacteristicReadRequest")
        if (characteristic.uuid == UUID.fromString(BleConfig.CHARACTERISTIC_UUID)) {
            val response = characteristic.value
            mGattServer.sendResponse(
                device,
                requestId,
                BluetoothGatt.GATT_SUCCESS,
                offset,
                response
            )
            onReadCallback(String(response))
        }
    }

    override fun onCharacteristicWriteRequest(
        device: BluetoothDevice?, requestId: Int,
        characteristic: BluetoothGattCharacteristic, preparedWrite: Boolean,
        responseNeeded: Boolean, offset: Int, value: ByteArray?
    ) {
        Log.i("MyGattServerCallback", "onCharacteristicWriteRequest")
        if (characteristic.uuid == UUID.fromString(BleConfig.CHARACTERISTIC_UUID)) {
            val msg = String(value!!)
            val response = "BLE:$msg,$msg,$msg".toByteArray()
            characteristic.value = response
            mGattServer.notifyCharacteristicChanged(device, characteristic, false)
            Log.i("MyGattServerCallback", "onCharacteristicWriteRequest: ${String(response)}")
            mGattServer.sendResponse(
                device,
                requestId,
                BluetoothGatt.GATT_SUCCESS,
                offset,
                response
            )
            onWriteCallback(String(response))
        }
    }
}

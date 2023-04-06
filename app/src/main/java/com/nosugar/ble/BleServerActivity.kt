package com.nosugar.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.nosugar.ble_server.R


class BleServerActivity : AppCompatActivity() {
    private lateinit var tvReadMessage: TextView
    private lateinit var tvWriteMessage: TextView
    private lateinit var mGattServerCallback: MyGattServerCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ble_server)
        tvWriteMessage = findViewById(R.id.tv_write_messages)
        tvReadMessage = findViewById(R.id.tv_read_messages)
        findViewById<TextView>(R.id.btn_start).setOnClickListener {
            startServer()
        }
    }

    private fun onReadCallback(message: String) {
        tvReadMessage.text = "${tvReadMessage.text}\n$message"
    }

    private fun onWriteCallback(message: String) {
        tvWriteMessage.text = "${tvWriteMessage.text}\n$message"
    }

    @SuppressLint("MissingPermission")
    private fun startServer() {
        // 开始广播
        val advSetting = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setConnectable(true)
            .setTimeout(0)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
            .build()
        val advData = AdvertiseData.Builder()
            .setIncludeDeviceName(true)
            .setIncludeTxPowerLevel(true)
            .build()
        val bluetoothAdapter = (getSystemService(BLUETOOTH_SERVICE) as BluetoothManager).adapter
        val bluetoothLeAdvertiser = bluetoothAdapter.bluetoothLeAdvertiser
        bluetoothLeAdvertiser.startAdvertising(advSetting, advData, advertiseCallback)
        mGattServerCallback = MyGattServerCallback(this::onReadCallback, this::onWriteCallback)

        // 开始服务器
        mGattServerCallback.startGattServer(this)
    }

    @SuppressLint("MissingPermission")
    override fun onDestroy() {
        super.onDestroy()
        // 停止广播
        val bluetoothAdapter = (getSystemService(BLUETOOTH_SERVICE) as BluetoothManager).adapter
        val bluetoothLeAdvertiser = bluetoothAdapter.bluetoothLeAdvertiser
        bluetoothLeAdvertiser.stopAdvertising(advertiseCallback)
        // 停止服务器
        mGattServerCallback.stopGattServer()
    }

    private val advertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            super.onStartSuccess(settingsInEffect)
            Toast.makeText(this@BleServerActivity, "Server started success", Toast.LENGTH_SHORT).show()
        }

        override fun onStartFailure(errorCode: Int) {
            super.onStartFailure(errorCode)
            Toast.makeText(this@BleServerActivity, "Server started failed", Toast.LENGTH_SHORT).show()
        }
    }
}
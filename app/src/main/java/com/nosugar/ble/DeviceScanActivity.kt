package com.nosugar.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nosugar.ble_server.R

// https://developer.android.com/guide/topics/connectivity/bluetooth/find-ble-devices
@SuppressLint("MissingPermission")
class DeviceListActivity : AppCompatActivity() {
    private val mBluetoothAdapter: BluetoothAdapter? by lazy(LazyThreadSafetyMode.NONE) { BluetoothAdapter.getDefaultAdapter() }
    private val mScannedDevices: HashMap<String, ScannedDevice> = HashMap()
    private val mDeviceExpirationTime = (30 * 1000).toLong()
    private var mScanning = false
    private lateinit var mHandler: Handler
    private val SCAN_DURATION = 10000L


    private lateinit var deviceAdapter: DeviceAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_list)
        mHandler = Handler(Looper.getMainLooper())

        val deviceList = findViewById<RecyclerView>(R.id.device_list)
        deviceList.layoutManager = LinearLayoutManager(this)
        deviceAdapter = DeviceAdapter(mutableListOf()) { device -> connectToDevice(device) }
        deviceList.adapter = deviceAdapter
    }

    override fun onStart() {
        super.onStart()
        if (mBluetoothAdapter == null) {
            // 设备不支持蓝牙
            Toast.makeText(this, "This device does not support Bluetooth", Toast.LENGTH_SHORT)
                .show()
            return
        } else {
            if (mBluetoothAdapter!!.bluetoothLeScanner == null) {
                // 蓝牙没打开
                Toast.makeText(this, "This device does not enable Bluetooth", Toast.LENGTH_SHORT).show()
            } else {
                scanLeDevice()
            }
        }
    }

    // 为了节省电量，10s后就停止扫描
    private fun scanLeDevice() {
        if (!mScanning) {
            mHandler.postDelayed(Runnable {
                mScanning = false
                mBluetoothAdapter!!.bluetoothLeScanner.stopScan(mScanCallback)
            }, SCAN_DURATION)
            mScanning = true
            val scanSettings = ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                .build()
            mBluetoothAdapter!!.bluetoothLeScanner.startScan(null, scanSettings, mScanCallback)
        } else {
            mScanning = false
            mBluetoothAdapter!!.bluetoothLeScanner.stopScan(mScanCallback)
        }
    }

    private val mScanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device: BluetoothDevice = result.device
            if (device.name == null) {
                return
            }
            mScannedDevices[device.address] = ScannedDevice(device, System.currentTimeMillis(), result.rssi)
            updateDeviceList()
        }

        override fun onBatchScanResults(results: List<ScanResult?>) {

        }

        override fun onScanFailed(errorCode: Int) {
            // 处理扫描失败
            Toast.makeText(this@DeviceListActivity, "Scan failed with error code $errorCode", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateDeviceList() {
        val currentTime = System.currentTimeMillis()
        val expiredDevices: MutableList<ScannedDevice> = ArrayList()
        val deviceList: MutableList<BluetoothDevice> = ArrayList()
        for (scannedDevice in mScannedDevices.values) {
            if (currentTime - scannedDevice.lastSeen > mDeviceExpirationTime) {
                expiredDevices.add(scannedDevice)
            } else {
                deviceList.add(scannedDevice.device)
            }
        }
        for (expiredDevice in expiredDevices) {
            mScannedDevices.remove(expiredDevice.device.address)
        }
        deviceAdapter.updateDevices(mScannedDevices.values.toList())
    }



    private fun connectToDevice(device: BluetoothDevice) {
        startActivity(Intent(this, BleClientActivity::class.java).apply {
            putExtra("device", device)
        })
//        startActivity(Intent(this, DeviceControlActivity::class.java).apply {
//            putExtra("device_address", device.address)
//        })
    }

    override fun onStop() {
        super.onStop()
        if (mScanning) {
            mBluetoothAdapter!!.bluetoothLeScanner.stopScan(mScanCallback)
        }
    }

}

@SuppressLint("MissingPermission")
class DeviceAdapter(private val devices: MutableList<ScannedDevice>, private val onDeviceClickListener: (device: BluetoothDevice) -> Unit) :
    RecyclerView.Adapter<DeviceAdapter.ViewHolder>() {

    fun updateDevices(devices: List<ScannedDevice>) {
        this.devices.clear()
        this.devices.addAll(devices)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_device, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val device = devices[position].device
        holder.deviceName.text = device.name ?: "Unknown Device"
        val text = "${device.address} 信号强度: ${devices[position].rssi}"
        holder.deviceAddress.text = text
        holder.itemView.setOnClickListener { onDeviceClickListener(device) }
    }

    override fun getItemCount() = devices.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val deviceName: TextView = itemView.findViewById(R.id.device_name)
        val deviceAddress: TextView = itemView.findViewById(R.id.device_address)
    }
}

package com.nosugar.ble

import android.bluetooth.BluetoothDevice
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.nosugar.ble_server.R

class BleClientActivity : AppCompatActivity() {
    private lateinit var tvResult: TextView
    private lateinit var mCallback: MyGattClientCallback
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ble_client)
        val edMessage = findViewById<EditText>(R.id.et_message)
        tvResult = findViewById(R.id.tv_received_messages)
        mCallback = MyGattClientCallback(this::onReceiveMessage)


        findViewById<Button>(R.id.btn_send).setOnClickListener{
            mCallback.sendMsg(edMessage.text.toString().trim())
        }
    }

    override fun onStart() {
        super.onStart()
        // 判断版本号
        val device = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("device", BluetoothDevice::class.java)!!
        } else {
            intent.getParcelableExtra("device")!!
        }
        mCallback.connectToGattServer(applicationContext, device)
    }

    private fun onReceiveMessage(message: String) {
        tvResult.text = "${tvResult.text}\n$message"
    }

    override fun onDestroy() {
        super.onDestroy()
        mCallback.disconnect()
    }
}
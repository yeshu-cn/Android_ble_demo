package com.nosugar.ble

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.nosugar.ble_server.R

class MainActivity : AppCompatActivity() {
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                if (permissions.all { it.value }) {
                    // All permissions were granted
                } else {
                    Toast.makeText(this, "权限被拒绝,无法正常使用蓝牙功能", Toast.LENGTH_SHORT).show();
                }
            }

        val clientPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                if (permissions.all { it.value }) {
                    // All permissions were granted
                    startActivity(Intent(this, DeviceListActivity::class.java))
                } else {
                    Toast.makeText(this, "权限被拒绝,无法正常使用蓝牙功能", Toast.LENGTH_SHORT).show();
                }
            }
        val serverPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                if (permissions.all { it.value }) {
                    // All permissions were granted
                    startActivity(Intent(this, BleServerActivity::class.java))
                } else {
                    Toast.makeText(this, "权限被拒绝,无法正常使用蓝牙功能", Toast.LENGTH_SHORT).show();
                }
            }
        findViewById<Button>(R.id.btn_server).setOnClickListener {
            checkPermission(
                { startActivity(Intent(this, BleServerActivity::class.java)) },
                serverPermissionLauncher
            )
        }

        findViewById<Button>(R.id.btn_client).setOnClickListener {
            checkPermission(
                { startActivity(Intent(this, DeviceListActivity::class.java)) },
                clientPermissionLauncher
            )
        }
    }

    private fun checkPermission(
        onPermissionGranted: () -> Unit,
        launcher: ActivityResultLauncher<Array<String>>
    ) {
        // 检查 Android 版本是否大于等于 31（即 Android 12）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // 在 Android 12 及更高版本中检查 BLUETOOTH_CONNECT 权限
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_SCAN
                ) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_ADVERTISE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                // 已授予 BLUETOOTH_CONNECT 权限，执行需要该权限的操作
                onPermissionGranted()
            } else {
                // 未授予 BLUETOOTH_CONNECT 权限，请求权限
                launcher.launch(
                    arrayOf(
                        Manifest.permission.BLUETOOTH_CONNECT,
                        Manifest.permission.BLUETOOTH_SCAN,
                        Manifest.permission.BLUETOOTH_ADVERTISE,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                )
            }
        } else {
            // 在 Android 12 以下版本中不需要检查权限，直接执行需要该权限的操作
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                // 已授予 BLUETOOTH_CONNECT 权限，执行需要该权限的操作
                onPermissionGranted()
            } else {
                launcher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
            }
        }

    }
}
package com.telltale.ipc.app

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Color
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.telltale.ipc.app.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var telltaleService: ITelltaleService? = null
    private var isBound = false

    private val inactiveColor = Color.parseColor("#E5E5E7")
    private val activeGreen = Color.parseColor("#34C759")
    private val activeBlue = Color.parseColor("#007AFF")
    private val activeRed = Color.parseColor("#FF3B30")

    private val serviceCallback = object : ITelltaleCallback.Stub() {
        override fun onTelltaleStateChanged(id: Int, state: Int) {
            runOnUiThread {
                updateIndicator(id, state)
            }
        }
    }

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            telltaleService = ITelltaleService.Stub.asInterface(service)
            isBound = true
            try {
                telltaleService?.registerCallback(serviceCallback)
                binding.tvServiceStatus.text = "● Service Connected"
                binding.tvServiceStatus.setTextColor(activeGreen)
            } catch (e: Exception) {
                Log.e("IPC_APP", "Failed to register callback", e)
            }
        }

        override fun onServiceDisconnected(name: ComponentName) {
            telltaleService = null
            isBound = false
            binding.tvServiceStatus.text = "○ Service Disconnected"
            binding.tvServiceStatus.setTextColor(activeRed)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupButtons()
    }

    private fun setupButtons() {
        binding.btnPauseSim.setOnClickListener {
            if (isBound) {
                try {
                    telltaleService?.pauseSimulation(true)
                    binding.btnPauseSim.visibility = View.GONE
                    binding.btnResumeSim.visibility = View.VISIBLE
                } catch (e: Exception) {
                    Log.e("IPC_APP", "Pause failed", e)
                }
            }
        }

        binding.btnResumeSim.setOnClickListener {
            if (isBound) {
                try {
                    telltaleService?.pauseSimulation(false)
                    binding.btnResumeSim.visibility = View.GONE
                    binding.btnPauseSim.visibility = View.VISIBLE
                } catch (e: Exception) {
                    Log.e("IPC_APP", "Resume failed", e)
                }
            }
        }

        binding.btnStopSim.setOnClickListener {
            if (binding.btnStopSim.text == "Restart Simulation") {
                startServiceConnection()
                binding.btnStopSim.text = "Stop Simulation"
                binding.btnPauseSim.isEnabled = true
                binding.btnPauseSim.visibility = View.VISIBLE
                binding.btnResumeSim.visibility = View.GONE
            } else {
                stopServiceConnection()
                resetIndicators()
                binding.btnStopSim.text = "Restart Simulation"
                binding.btnPauseSim.isEnabled = false
                binding.btnResumeSim.isEnabled = false
            }
        }
    }

    private fun startServiceConnection() {
        val intent = Intent(this, TelltaleService::class.java).apply {
            setPackage(packageName)
        }
        bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    private fun stopServiceConnection() {
        if (isBound) {
            try {
                telltaleService?.unregisterCallback(serviceCallback)
            } catch (e: Exception) {
                Log.e("IPC_APP", "Unregister failed", e)
            }
            unbindService(connection)
            isBound = false
            telltaleService = null
            binding.tvServiceStatus.text = "○ Service Disconnected"
            binding.tvServiceStatus.setTextColor(activeRed)
        }
    }

    override fun onStart() {
        super.onStart()
        startServiceConnection()
    }

    override fun onStop() {
        super.onStop()
        stopServiceConnection()
    }

    private fun resetIndicators() {
        binding.tvLeftTurnIcon.setTextColor(inactiveColor)
        binding.tvRightTurnIcon.setTextColor(inactiveColor)
        binding.tvHighBeamIcon.setTextColor(inactiveColor)
        binding.tvBrakeIcon.setTextColor(inactiveColor)
    }

    private fun updateIndicator(id: Int, state: Int) {
        val color = if (state == 1) getIndicatorColor(id) else inactiveColor
        
        when (id) {
            0x01 -> binding.tvLeftTurnIcon.setTextColor(color)
            0x02 -> binding.tvRightTurnIcon.setTextColor(color)
            0x03 -> binding.tvHighBeamIcon.setTextColor(color)
            0x04 -> binding.tvBrakeIcon.setTextColor(color)
        }
    }

    private fun getIndicatorColor(id: Int): Int {
        return when (id) {
            0x01, 0x02 -> activeGreen
            0x03 -> activeBlue
            0x04 -> activeRed
            else -> Color.BLACK
        }
    }
}

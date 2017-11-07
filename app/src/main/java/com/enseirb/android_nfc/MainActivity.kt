package com.enseirb.android_nfc

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.nfc.NfcAdapter
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.TextView
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*
import java.util.logging.Logger


class MainActivity : AppCompatActivity() {

    private val SERIAL_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")
    private var mNfcAdapter: NfcAdapter? = null
    private var socket: BluetoothSocket? = null
    private var mBluetoothAdapter: BluetoothAdapter? = null
    private var outputStream: OutputStream? = null
    private var inStream: InputStream? = null
    private var Log = Logger.getLogger(MainActivity::class.java.name)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    private fun write(msg: String) {
        try {
            outputStream?.let {
                it.write(msg.toByteArray())
            }
        } catch (e: IOException){
            Log.warning("Romain closed the socket")
        }
    }

    override fun onStart() {
        super.onStart()
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this)
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        this.connectBluetooth()
        val res = NFCUtil.retrieveNFCMessage(intent)
        Log.warning(res)
        val text = findViewById(R.id.resultTextView) as TextView
        text.text = res
        if(res == "chambre 1"){
            write("change")
        }
    }

    override fun onResume() {
        super.onResume()
        mNfcAdapter?.let {
            NFCUtil.enableNFCInForeground(it, this, javaClass)
        }
    }

    override fun onPause() {
        super.onPause()
        mNfcAdapter?.let {
            NFCUtil.disableNFCInForeground(it, this)
        }
    }

    override fun onStop() {
        super.onStop()
        write("close")
        socket!!.close()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        val res = NFCUtil.retrieveNFCMessage(intent)
        Log.warning(res)
        val text = findViewById(R.id.resultTextView) as TextView
        text.text = res
        if(res == "chambre 1"){
            write("change")
        }
    }

    fun connectBluetooth() {
        val text = findViewById(R.id.resultTextView) as TextView
        mBluetoothAdapter?.let {
            val pairedDevices: Set<BluetoothDevice>  = it.bondedDevices
            Log.warning("" + pairedDevices.size)

            for (device in pairedDevices) {
                val deviceName = device.name
                if(deviceName == "raspberrypi") {
                    try {
                        socket = device.createRfcommSocketToServiceRecord(SERIAL_UUID)
                    } catch (e: Exception) {
                        Log.warning("Error creating socket")
                    }

                    if(!socket!!.isConnected){
                        try {

                            socket!!.connect()
                            Log.warning("Connected")
                        } catch (e: IOException) {
                            Log.warning(e.message)
                            try {
                                Log.warning("trying fallback...")

                                socket = device.javaClass.getMethod("createRfcommSocket", *arrayOf<Class<*>>(Int::class.java)).invoke(device, 1) as BluetoothSocket

                                socket?.let {
                                    it.connect()
                                }
                                Log.warning("Connected")
                                text.text = "Connected"
                                socket?.let {
                                    outputStream = it.outputStream
                                    //inStream = it.inputStream
                                }

                            } catch (e2: Exception) {
                                Log.warning("Couldn't establish Bluetooth connection!")
                                text.text = "Couldn't establish Bluetooth connection!"
                            }

                        }
                    }

                }
            }
        }
    }
}
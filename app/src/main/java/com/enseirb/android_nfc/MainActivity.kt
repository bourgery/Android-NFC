package com.enseirb.android_nfc

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.TextView
import java.io.IOException
import java.io.OutputStream
import java.util.*
import java.util.logging.Logger


class MainActivity : AppCompatActivity() {

    private val SERIAL_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")
    private var mNfcAdapter: NfcAdapter? = null
    private var socket: BluetoothSocket? = null
    private var mBluetoothAdapter: BluetoothAdapter? = null
    private var outputStream: OutputStream? = null
    private var Log = Logger.getLogger(MainActivity::class.java.name)
    private var theadRead: BluetoothReceive? = null

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
        val test = "{'list': [{'type': 'wifi', 'service': 'networkWifi'," +
                "'name':'réseau wifi', 'ssid': 'RSR'," +
                "'password': 'rsrL4s3r!'}, {'type': 'toto'}]}"
        val pathPrefix = "nfc.com:clientNFC"
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        this.connectBluetooth()
        val res = NFCUtil.retrieveNFCMessage(intent)
        val nfcRecord = NdefRecord(NdefRecord.TNF_EXTERNAL_TYPE, pathPrefix.toByteArray(),
                ByteArray(0), test.toByteArray())
        val nfcMessage = NdefMessage(arrayOf(nfcRecord))
        if(mNfcAdapter != null) {
            Log.warning("ok");
            mNfcAdapter?.setNdefPushMessage(nfcMessage, this)
        }
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
        Log.warning("pause")
        mNfcAdapter?.let {
            NFCUtil.disableNFCInForeground(it, this)
        }
    }

    override fun onStop() {
        super.onStop()
        Log.warning("stop")
        write("close")
        theadRead?.interrupt()
        if(socket != null)
            socket!!.close()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        val test = "{'list': [{'type': 'wifi', 'service': 'networkWifi'," +
                "'name':'réseau wifi', 'ssid': 'RSR'," +
                "'password': 'rsrL4s3r!'}, {'type': 'toto'}]}"
        val pathPrefix = "nfc.com:clientNFC"
        val res = NFCUtil.retrieveNFCMessage(intent)
        val nfcRecord = NdefRecord(NdefRecord.TNF_EXTERNAL_TYPE, pathPrefix.toByteArray(),
                ByteArray(0), test.toByteArray())
        val nfcMessage = NdefMessage(arrayOf(nfcRecord))
        if(mNfcAdapter != null) {
            Log.warning("ok");
            mNfcAdapter?.setNdefPushMessage(nfcMessage, this)
        }
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
                                    theadRead = BluetoothReceive(it.inputStream, mNfcAdapter, this)
                                    theadRead?.start()
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
        text.text = "ok"
    }
}
package com.enseirb.android_nfc

import android.app.Activity
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import java.io.IOException
import java.io.InputStream
import java.util.logging.Logger

/**
 * Created by yoann on 28/11/2017.
 */
class BluetoothReceive(private val inStream: InputStream?, private val mNfcAdapter: NfcAdapter?, private val activity: Activity): Thread() {
    private var Log = Logger.getLogger(MainActivity::class.java.name)
    override fun run() {
        val pathPrefix = "enseirb.com:clientNFC"
        while(true){
            try {
                val bytes: Int?;
                val buffer = ByteArray(1024)
                bytes = inStream?.read(buffer)
                if (bytes != null) {
                    Log.warning("" + String(buffer.copyOf(bytes)))
                    val nfcRecord = NdefRecord(NdefRecord.TNF_EXTERNAL_TYPE, pathPrefix.toByteArray(),
                            ByteArray(0), buffer)
                    val nfcMessage = NdefMessage(arrayOf(nfcRecord))
                    mNfcAdapter?.let {
                        Log.warning("Send" + nfcMessage)
                        it.setNdefPushMessage(nfcMessage, activity)

                    }
                    Log.warning("toto")
                }
            }catch (e: IOException){
                return;
            }
        }
    }
}

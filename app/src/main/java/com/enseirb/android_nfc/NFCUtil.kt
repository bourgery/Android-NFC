package com.enseirb.android_nfc
import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.tech.Ndef
import android.nfc.tech.NdefFormatable
import java.util.logging.Logger


object NFCUtil {

    fun retrieveNFCMessage(intent: Intent?): String {
        intent?.let {
            val Log = Logger.getLogger(MainActivity::class.java.name)
            if (NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action || NfcAdapter.ACTION_TECH_DISCOVERED == intent.action) {
                val nDefMessages = getNDefMessages(intent)
                Log.warning("" + nDefMessages.size)
                nDefMessages[0].records?.let {
                    it.forEach {
                        it?.payload.let {
                            it?.let {
                                return String(it)

                            }
                        }
                    }
                }

            } else {
                return ""
            }
        }
        return ""
    }


    private fun getNDefMessages(intent: Intent): Array<NdefMessage> {
        val rawMessage = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
        rawMessage?.let {
            return rawMessage.map {
                it as NdefMessage
            }.toTypedArray()
        }
        // Unknown tag type
        val empty = byteArrayOf()
        val record = NdefRecord(NdefRecord.TNF_UNKNOWN, empty, empty, empty)
        val msg = NdefMessage(arrayOf(record))
        return arrayOf(msg)
    }

    fun disableNFCInForeground(nfcAdapter: NfcAdapter, activity: Activity) {
        nfcAdapter.disableForegroundDispatch(activity)
    }

    fun <T> enableNFCInForeground(nfcAdapter: NfcAdapter, activity: Activity, classType: Class<T>) {
        val pendingIntent = PendingIntent.getActivity(activity, 0,
                Intent(activity, classType).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0)
        val nfcIntentFilter = IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED)
        val filters = arrayOf(nfcIntentFilter)

        val TechLists = arrayOf(arrayOf(Ndef::class.java.name), arrayOf(NdefFormatable::class.java.name))

        nfcAdapter.enableForegroundDispatch(activity, pendingIntent, filters, TechLists)
    }
}
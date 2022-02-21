package com.example.lte_signal_test


import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.telephony.CellInfoLte
import android.telephony.*
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import org.apache.commons.net.ftp.FTPClient
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.thread


class MainActivity : AppCompatActivity() {
    private var rsrp = 0
    private var rsrq = 0
    private var snr = 0
    private var lteantlevel = 0
    private var pci = 0
    private var earfcn = 0
    private var TAG = "LTESIGNAL"
    val scope = CoroutineScope(Main)
    private var permissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.INTERNET,
        Manifest.permission.ACCESS_COARSE_LOCATION,
    )

    @SuppressLint("SetTextI18n", "SimpleDateFormat")
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkPermissions()

        val textBox = findViewById<TextView>(R.id.textView)
        val saveButton = findViewById<Button>(R.id.button)

        var cellInfoList: List<CellSignalStrength>
        val tm = getSystemService(TELEPHONY_SERVICE) as TelephonyManager
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        var time: String = format.format(Date())
        thread {
            while (true) {
                time= format.format(Date())
                cellInfoList = tm.signalStrength!!.cellSignalStrengths
                for (cellInfo in cellInfoList) {
                    if (cellInfo is CellSignalStrengthLte) {
                        rsrp = cellInfo.rsrp
                        rsrq = cellInfo.rsrq
                        snr = cellInfo.rssnr
                        lteantlevel = cellInfo.level
                        Log.e(TAG, "snr : $cellInfo")
                    }
                }
                val cellIdentityList: List<CellInfo> = tm.allCellInfo
                for (cellinfo in cellIdentityList) {
                    if (cellinfo is CellInfoLte) {
                        pci = cellinfo.cellIdentity.pci
                        earfcn = cellinfo.cellIdentity.earfcn
                    }
                }
                scope.launch {
                    textBox.text =
                        "TIME : $time \nPCI : $pci \nEARFCN : $earfcn \nRSRP : $rsrp \nRSRQ : $rsrq \nSNR : $snr \nANR LEVEL : $lteantlevel"
                }
                Thread.sleep(5000L)
            }
        }
        // 데이터 CSV 파일로 저장
        saveButton.setOnClickListener {
            val filePath = filesDir.toString()
            val csvHelper = CsvHelper(filePath)
            val dataList = arrayListOf<Array<String>>()
            if (!File("$filePath/LTESignal").exists())
            {
                dataList.add(arrayOf("TIME", "PCI", "Earfcn", "RSRP", "RSRQ", "SNR", "Ant Level"))
            }
            dataList.add(arrayOf(time,pci.toString(),earfcn.toString(),rsrp.toString(),rsrp.toString(),rsrq.toString(),snr.toString(),lteantlevel.toString()))

            csvHelper.writeData("LTESignal", dataList)
            Toast.makeText(this,"현재 로그 저장",Toast.LENGTH_SHORT).show()
        }
        Log.e(TAG, "pci : $pci")
        Log.e(TAG, "earfcn : $earfcn")
        Log.e(TAG, "rsrp : $rsrp")
        Log.e(TAG, "rsrq : $rsrq")
        Log.e(TAG, "snr : $snr")
        Log.e(TAG, "ant level : $lteantlevel")





    }
    private fun checkPermissions() {
        //거절되었거나 아직 수락하지 않은 권한(퍼미션)을 저장할 문자열 배열 리스트
        val rejectedPermissionList = ArrayList<String>()

        //필요한 퍼미션들을 하나씩 끄집어내서 현재 권한을 받았는지 체크
        for(permission in permissions){
            if(ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                //만약 권한이 없다면 rejectedPermissionList에 추가
                rejectedPermissionList.add(permission)
            }
        }
        //거절된 퍼미션이 있다면...
        if(rejectedPermissionList.isNotEmpty()){
            //권한 요청!
            val array = arrayOfNulls<String>(rejectedPermissionList.size)
            ActivityCompat.requestPermissions(this, rejectedPermissionList.toArray(array), 1032)
        }
    }
}
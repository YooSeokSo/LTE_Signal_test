package com.example.lte_signal_test


import android.Manifest
import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.telephony.*
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import fr.bmartel.speedtest.SpeedTestReport
import fr.bmartel.speedtest.SpeedTestSocket
import fr.bmartel.speedtest.inter.ISpeedTestListener
import fr.bmartel.speedtest.model.SpeedTestError
import fr.bmartel.speedtest.model.SpeedTestMode
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import java.io.File
import java.math.RoundingMode
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread


class MainActivity : AppCompatActivity() {

    private lateinit var job: Job
    val speedTestSocket = SpeedTestSocket()

    //network state
    private var rsrp = 0
    private var rsrq = 0
    private var snr = 0
    private var lteantlevel = 0
    private var pci = 0
    private var earfcn = 0

    private var TAG = "LTESIGNAL"

    //thread
    val scope = CoroutineScope(Main)

    //permission
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

        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        var time = format.format(Date())

        var dlspeed = ""
        var upspeed = ""

        val textBox = findViewById<TextView>(R.id.textView)
        val startButton = findViewById<Button>(R.id.button)
        val stopButton = findViewById<Button>(R.id.button2)
        val durationEditText = findViewById<EditText>(R.id.password)
        val repeatCountEditText = findViewById<EditText>(R.id.repeatCount)
        val dlspeedText = findViewById<TextView>(R.id.textView6)
        val ulspeedText = findViewById<TextView>(R.id.textView7)

        val Nformat = NumberFormat.getInstance()
        Nformat.maximumFractionDigits = 6
        Nformat.roundingMode = RoundingMode.HALF_EVEN
        speedTestSocket.addSpeedTestListener(object : ISpeedTestListener {
            override fun onCompletion(report: SpeedTestReport) {
                // called when download/upload is finished
                Log.v("speedtest", "[COMPLETED] rate in octet/s : " + report.transferRateOctet)
                Log.v("speedtest", "[COMPLETED] rate in bit/s   : " + report.transferRateBit)
                CoroutineScope(Main).launch {
                    var speed = Nformat.format(report.transferRateBit)
                    Log.d(TAG, "$speed")
                    if (report.speedTestMode == SpeedTestMode.DOWNLOAD) {
                        dlspeedText.text = Nformat.format(report.transferRateBit)
                        dlspeed = Nformat.format(report.transferRateBit)
                    } else {
                        ulspeedText.text = Nformat.format(report.transferRateBit)
                        upspeed = Nformat.format(report.transferRateBit)
                        saveDate(dlspeed, upspeed, time)
                    }
                }
            }
            override fun onError(speedTestError: SpeedTestError, errorMessage: String) {
                // called when a download/upload error occur
                Log.v("speedtest", "error")
            }
            override fun onProgress(percent: Float, report: SpeedTestReport) {
                // called to notify download/upload progress
                Log.v("speedtest", "[PROGRESS] progress : $percent%")
                Log.v("speedtest", "[PROGRESS] rate in octet/s : " + report.transferRateOctet)
                Log.v("speedtest", "[PROGRESS] rate in bit/s   : " + report.transferRateBit)
            }
        })


        // get UID
        val packageManager = packageManager
        val applicationId = packageManager.getApplicationInfo(
            "com.example.lte_signal_test",
            PackageManager.GET_META_DATA
        )
        Log.d(TAG, applicationId.uid.toString())


        // Network state
        var cellInfoList: List<CellSignalStrength>
        val tm = getSystemService(TELEPHONY_SERVICE) as TelephonyManager

        thread {
            while (true) {
                if (tm.signalStrength == null) {
                    break
                }
                cellInfoList = tm.signalStrength!!.cellSignalStrengths
                for (cellInfo in cellInfoList) {
                    if (cellInfo is CellSignalStrengthLte) {
                        rsrp = cellInfo.rsrp
                        rsrq = cellInfo.rsrq
                        snr = cellInfo.rssnr
                        lteantlevel = cellInfo.level
                        //Log.d(TAG, "snr : $cellInfo")
                    }
                }
                val cellIdentityList: List<CellInfo> = tm.allCellInfo
                for (cellinfo in cellIdentityList) {
                    if (cellinfo is CellInfoLte) {
                        pci = cellinfo.cellIdentity.pci
                        earfcn = cellinfo.cellIdentity.earfcn
                    }
                }
                time = format.format(Date())
                scope.launch {
                    textBox.text =
                        "TIME : $time \nPCI : $pci \nEARFCN : $earfcn \nRSRP : $rsrp \nRSRQ : $rsrq \nSNR : $snr \nANR LEVEL : $lteantlevel"
                }
                Thread.sleep(5000L)
            }
        }


        startButton.setOnClickListener {
            job = CoroutineScope(IO).launch {
                if(isActive){
                    stopButton.isClickable = true
                }
                repeat(Integer.parseInt(repeatCountEditText.text.toString())*1) { i ->
                    speedTestSocket.startFixedDownload(
                        "http://downloadtest.kdatacenter.com/100MB",
                        10000
                    )
                    delay(15000)
                    speedTestSocket.startFixedUpload("http://ipv4.ikoula.testdebit.info/", 1000000,10000)
                    delay(11000)
                    Log.d(TAG,"repeat: $i")
                    delay(Integer.parseInt(durationEditText.text.toString())*1000L)
                }

            }
        }
        stopButton.setOnClickListener{
            job.cancel()
            stopButton.isClickable = false
        }
    }
    // 데이터 CSV 파일로 저장
    fun saveDate(dlspeed: String, upspeed: String, time: String){
        val filePath = filesDir.toString()
        val csvHelper = CsvHelper(filePath)
        val dataList = arrayListOf<Array<String>>()
        if (!File("$filePath/LTESignal").exists())
        {
            dataList.add(arrayOf("TIME","Dspeed","Uspeed", "PCI", "Earfcn", "RSRP", "RSRQ", "SNR", "Ant Level"))
        }
        dataList.add(arrayOf(time,dlspeed,upspeed,pci.toString(),earfcn.toString(),rsrp.toString(),rsrp.toString(),rsrq.toString(),snr.toString(),lteantlevel.toString()))
        csvHelper.writeData("LTESignal", dataList)

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
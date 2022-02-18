package com.example.lte_signal_test


import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.telephony.CellInfoLte
import android.telephony.*
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


class MainActivity : AppCompatActivity() {

    var rsrp = 0
    var rsrq = 0
    var snr = 0
    var lteantlevel = 0
    var pci = 0
    var earfcn = 0
    var TAG = "LTESIGNAL"

    var permissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.INTERNET,
        Manifest.permission.ACCESS_COARSE_LOCATION,
    )

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val cellInfoList: List<CellSignalStrength>
        val tm = getSystemService(TELEPHONY_SERVICE) as TelephonyManager
        cellInfoList = tm.signalStrength!!.cellSignalStrengths
        for (cellInfo in cellInfoList) {
            if (cellInfo is CellSignalStrengthLte) {
                rsrp = cellInfo.rsrp
                rsrq = cellInfo.rsrq
                snr = cellInfo.rssnr
                lteantlevel = cellInfo.level
                Log.e(TAG, "snr : " + cellInfo.toString())
            }
        }
        checkPermissions()
        val cellIdentityList: List<CellInfo> = tm.allCellInfo
        for (cellinfo in cellIdentityList){
            if(cellinfo is CellInfoLte){
                pci = cellinfo.cellIdentity.pci
                earfcn = cellinfo.cellIdentity.earfcn
            }
        }
        Log.e(TAG, "pci : $pci")
        Log.e(TAG, "earfcn : $earfcn")
        Log.e(TAG, "rsrp : $rsrp")
        Log.e(TAG, "rsrq : $rsrq")
        Log.e(TAG, "snr : $snr")
        Log.e(TAG, "ant level : $lteantlevel")



        // 데이터 CSV 파일로 저장
        val filePath = filesDir.toString()

        val csvHelper = CsvHelper(filePath)

        val dataList = arrayListOf<Array<String>>()

        dataList.add(arrayOf("TIME","PCI","Earfcn","RSRP","RSRQ","SNR","Ant Level"))
        //dataList.add(arrayOf("서유석","서유석"))

        csvHelper.writeData("LTESignal", dataList)
    }
    private fun checkPermissions() {
        //거절되었거나 아직 수락하지 않은 권한(퍼미션)을 저장할 문자열 배열 리스트
        var rejectedPermissionList = ArrayList<String>()

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
package com.example.lte_signal_test

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val filePath = filesDir.toString()

        val csvHelper = CsvHelper(filePath)

        val dataList = arrayListOf<Array<String>>()

        dataList.add(arrayOf("TIME","PCI","Earfcn","RSRP","RSRQ","SNR","Ant Level"))
    }
}
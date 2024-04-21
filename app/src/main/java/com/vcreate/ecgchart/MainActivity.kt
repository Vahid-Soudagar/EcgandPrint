package com.vcreate.ecgchart

import android.R.attr
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStreamReader


class MainActivity : AppCompatActivity() {

    private var fileUri: String = ""
    private lateinit var ecgGraphView: EcgGraphView
    private lateinit var button: Button

    private var selectedFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ecgGraphView = findViewById(R.id.wave1)
//        button = findViewById(R.id.button)

        // Generate 2500 random numbers
        val randomNumbers = generateRandomNumbers(2500)
        Log.d("ListOf", randomNumbers.toString())

        // Process each random number to get millivolts and store them in a list
        val millivoltsList = ArrayList<Float>()

        for (number in randomNumbers) {
            val millivolt = processECGData(number)
            millivoltsList.add(millivolt)
        }

        Log.d("ListOf", millivoltsList.toString())

        // Draw the waveform on the EcgGraphView
      //  ecgGraphView.addAmp(millivoltsList)


//        button.setOnClickListener {
//            openFileManager()
//        }
//
//        val wave1Data = readWave1DataFromFile()
//
//        // Process each data point to get millivolts and store them in a list
//        val millivoltsList = ArrayList<Float>()
//        for (data in wave1Data) {
//            val millivolt = processECGData(data)
//            millivoltsList.add(millivolt)
//        }
//
//        // Draw the waveform on the EcgGraphView
//        ecgGraphView.addAmp(millivoltsList)
    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//
//        if (requestCode == FILE_SELECT_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
//            data?.data?.let { uri ->
//                selectedFile = File(uri.path)
//            }
//        }
//    }


    private fun generateRandomNumbers(count: Int): List<Int> {
        return List(count) { (1..255).random() } // Generates a list of count random numbers between 1 and 255
    }

    private fun processECGData(data: Int): Float {
        val meanPoint = 128
        val mvPerUnit = 1f / 70f // 1 millivolt is represented by a change of 70 units
        return (data - meanPoint) * mvPerUnit;
    }


//    private fun openFileManager() {
//        val intent = Intent(Intent.ACTION_GET_CONTENT)
//        intent.type = "*/*" // You can restrict the type of files allowed to be selected here if needed
//        startActivityForResult(intent, FILE_SELECT_REQUEST_CODE)
//    }
//
//
//    private fun readWave1DataFromFile(): List<Int> {
//        val destFile = selectedFile?.toURI()?.let { File(it) }
//        val dataList = mutableListOf<Int>()
//        try {
//            val inputStream = FileInputStream(destFile)
//            val reader = BufferedReader(InputStreamReader(inputStream))
//            var line: String?
//            while (reader.readLine().also { line = it } != null) {
//                dataList.add(line!!.toInt())
//            }
//            reader.close()
//        } catch (e: IOException) {
//            Log.e("MainActivityData", "Error reading data from file: ${destFile?.absoluteFile}", e)
//        }
//        return dataList
//    }
//
//    companion object {
//        private const val FILE_SELECT_REQUEST_CODE = 123
//    }

}

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
    private lateinit var ecgGraphView1: EcgGraphView
    private lateinit var ecgGraphView2: EcgGraphView
    private lateinit var ecgGraphView3: EcgGraphView
    private lateinit var button: Button


    private var selectedFilePath: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ecgGraphView1 = findViewById(R.id.wave1)
        ecgGraphView2 = findViewById(R.id.wave2)
        ecgGraphView3 = findViewById(R.id.wave3)
        button = findViewById(R.id.open)

        // Generate 2500 random numbers
        val randomNumbers = generateRandomNumbers(2500)
        Log.d("ListOf", randomNumbers.toString())

        // Process each random number to get millivolts and store them in a list
//        val millivoltsList = ArrayList<Float>()
//
//        for (number in randomNumbers) {
//            val millivolt = processECGData(number)
//            millivoltsList.add(millivolt)
//        }
//
//        Log.d("ListOf", millivoltsList.toString())

        button.setOnClickListener {
            openFileManager()
        }

    }



    private fun generateRandomNumbers(count: Int): List<Int> {
        return List(count) { (1..255).random() } // Generates a list of count random numbers between 1 and 255
    }

    private fun processECGData(data: Int): Float {
        val meanPoint = 128
        val mvPerUnit = 1f / 70f // 1 millivolt is represented by a change of 70 units
        return (data - meanPoint) * mvPerUnit;
    }

    private fun openFileManager() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"
        startActivityForResult(Intent.createChooser(intent, "Select File"), FILE_PICKER_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FILE_PICKER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                selectedFilePath = uri.toString()


                val wave1 = parseFirstNumbersFromTextFile(uri, 0)
                val wave2 = parseFirstNumbersFromTextFile(uri, 1)
                val wave3 = parseFirstNumbersFromTextFile(uri, 2)

                val millivoltsList1 = ArrayList<Float>()
                val millivoltsList2 = ArrayList<Float>()
                val millivoltsList3 = ArrayList<Float>()

                for (number in wave1) {
                    val millivolt = processECGData(number)
                    millivoltsList1.add(millivolt)
                }

                for (number in wave2) {
                    val millivolt = processECGData(number)
                    millivoltsList2.add(millivolt)
                }

                for (number in wave3) {
                    val millivolt = processECGData(number)
                    millivoltsList3.add(millivolt)
                }

                ecgGraphView1.addAmp(millivoltsList1)
                ecgGraphView2.addAmp(millivoltsList2)
                ecgGraphView3.addAmp(millivoltsList3)
            }
        }
    }

//    private fun parseFirstNumbersFromTextFile(uri: Uri, index: Int): List<Int> {
//        val inputStream = contentResolver.openInputStream(uri)
//        val reader = BufferedReader(InputStreamReader(inputStream))
//        val numbersList = mutableListOf<Int>()
//
//        reader.useLines { lines ->
//            lines.forEach { line ->
//                val firstNumber = line.split(",")[index].trim().toIntOrNull()
//                firstNumber?.let {
//                    numbersList.add(it)
//                }
//            }
//        }
//
//        return numbersList
//    }

    private fun parseFirstNumbersFromTextFile(uri: Uri, index: Int): List<Int> {
        val inputStream = contentResolver.openInputStream(uri)
        val reader = BufferedReader(InputStreamReader(inputStream))
        val numbersList = mutableListOf<Int>()

        reader.useLines { lines ->
            lines.forEach { line ->
                val numbers = line.split(",").map { it.trim().toIntOrNull() }
                if (index < numbers.size && numbers[index] != null) {
                    numbersList.add(numbers[index]!!)
                }
            }
        }

        return numbersList
    }



    companion object {
        private const val FILE_PICKER_REQUEST_CODE = 123 // Arbitrary request code
    }
}

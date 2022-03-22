package com.wind.gradle.plugin.autotrackclick

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView

import com.wind.gradle.plugin.autotrackclick.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        val button = findViewById<Button>(R.id.button)
        button.setOnClickListener {
            clickMe();
            //println("click me")
           // asmTest()
        }


    }

    fun clickMe(){
        println("click me")
    }


    @ASMTime
    fun asmTest() {

        for (i in 0..1) {
            println("i:" + i)
        }

    }
}
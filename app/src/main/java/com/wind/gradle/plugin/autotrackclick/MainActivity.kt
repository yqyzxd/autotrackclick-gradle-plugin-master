package com.wind.gradle.plugin.autotrackclick

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.CheckBox
import android.widget.TextView
import com.hi.dhl.binding.viewbind
import com.wind.gradle.plugin.autotrackclick.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    val binding:ActivityMainBinding by viewbind()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       // var binding=ActivityMainBinding.inflate(layoutInflater)
      //  setContentView(binding.root)
       // setContentView(R.layout.activity_main)
        with(binding){
            button.setOnClickListener {
                println("click me")
                asmTest()
            }
        }

        var cb= CheckBox(this)

       if ( cb is TextView){
           println("cb is TextView")
       }else{
           println("cb is not TextView")
       }
    }


    @ASMTime
    fun asmTest(){

        for(i in 0..1){
            println("i:"+i)
        }

    }
}
package com.github.jarofcolor.emulatorcheck.demo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.github.jarofcolor.emulatorcheck.EmulatorCheck
import com.github.jarofcolor.emulatorcheck.demo.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (EmulatorCheck.isEmulator()) {
            binding.text.text = "当前为模拟器：(${EmulatorCheck.getEmulatorName()})"
        }
    }
}
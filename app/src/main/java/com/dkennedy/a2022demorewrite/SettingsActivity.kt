package com.dkennedy.a2022demorewrite

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import com.example.a2022demorewrite.R
import com.example.a2022demorewrite.databinding.ActivityMainBinding
import com.example.a2022demorewrite.databinding.ActivitySettingsBinding

class SettingsActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportFragmentManager.beginTransaction().replace(android.R.id.content, SettingsFragment()).commit()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}
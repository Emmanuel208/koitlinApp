package com.emmanuelobasi.maps

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)



        Handler().postDelayed({
            startActivity(Intent(this@SplashActivity,
                MapsActivity::class.java))
            finish()
        }, 1200)

    }
}

/*
* The objective of this assessment is to develop a mobile app
* to showcase map places of literary importance in Ireland and
* surrounding areas related to poets, patrons, and publishers in Ireland
* who lived in the 16th and 17th centuries.
* The requirements are as follows, and all requirements carry equal weightage: */
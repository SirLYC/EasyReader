package com.lyc.readertestapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bt_test_link_main.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("lycer://home")
            try {
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(this, "打开主页失败", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

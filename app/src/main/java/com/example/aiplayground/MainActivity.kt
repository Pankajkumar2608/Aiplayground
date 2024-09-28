package com.example.aiplayground

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val chat : Button = findViewById(R.id.chatWithAi)
        val contact: Button = findViewById(R.id.contactUs)
        val use : Button = findViewById(R.id.howTOUse)


        chat.setOnClickListener{
            val intent = Intent(this, ChatWithAi::class.java)
            startActivity(intent)
        }
        use.setOnClickListener{
            val intent  = Intent(this,help::class.java)
            startActivity(intent)
        }
        contact.setOnClickListener{
            val webIntent: Intent = Uri.parse("https://t.me/unofficial_g_o_d").let { webpage ->
                Intent(Intent.ACTION_VIEW, webpage)
            }
            startActivity(webIntent)
        }
    }
}
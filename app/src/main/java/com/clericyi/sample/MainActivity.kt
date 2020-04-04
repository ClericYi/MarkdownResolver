package com.clericyi.sample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.LinkMovementMethod
import androidx.core.app.ActivityCompat
import com.clericyi.resolver.Markdown
import com.clericyi.resolver.Parser
import kotlinx.android.synthetic.main.activity_main.*
import java.util.jar.Manifest

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ActivityCompat.requestPermissions(this,
            arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE,android.Manifest.permission.READ_EXTERNAL_STORAGE), 1)
        text.text = Markdown.parser("/mnt/sdcard/download/md.md")
        text.movementMethod = LinkMovementMethod.getInstance()
    }
}

package com.example.myapplication

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.ui.AppBarConfiguration
import com.example.myapplication.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonFirst.setOnClickListener { view ->
            tryDownloadFileButCancelIfTimeout("https://speed.hetzner.de/1GB.bin", 5000)
        }
    }

private fun tryDownloadFileButCancelIfTimeout(url: String, millisecondsToTimeoutAfter : Long) {
    val downloadManager = this.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    val downloadUri = Uri.parse(url)
    val request = DownloadManager.Request(downloadUri).apply {
        try {
            setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
                .setAllowedOverRoaming(true)
                .setTitle(url.substring(url.lastIndexOf("/") + 1))
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                .setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS,
                    url.substring(url.lastIndexOf("/") + 1)
                )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    val downloadId = downloadManager.enqueue(request)
    // Schedule a new thread that will cancel the download after 5 seconds.
    Handler(Looper.getMainLooper()).postDelayed({
        val downloadQuery = DownloadManager.Query().setFilterById(downloadId)
        val cursor = downloadManager.query(downloadQuery)
        val downloadStatusColumn = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
        cursor.moveToPosition(0)
        val downloadStatus = cursor.getInt(downloadStatusColumn)
        if (downloadStatus != DownloadManager.STATUS_SUCCESSFUL) {
            downloadManager.remove(downloadId)
        }

    }, millisecondsToTimeoutAfter)
}

}

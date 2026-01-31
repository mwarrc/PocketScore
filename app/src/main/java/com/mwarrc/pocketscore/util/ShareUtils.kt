package com.mwarrc.pocketscore.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.mwarrc.pocketscore.domain.model.PocketScoreShare
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

object ShareUtils {

    private val json = Json { 
        ignoreUnknownKeys = true 
        prettyPrint = true
    }

    /**
     * Exports data to a temporary file and returns a sharing intent.
     */
    fun getShareIntent(context: Context, shareData: PocketScoreShare, fileName: String = "pocketscore_backup.pscore"): Intent {
        val jsonString = json.encodeToString(shareData)
        val tempFile = File(context.cacheDir, fileName)
        tempFile.writeText(jsonString)

        val contentUri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            tempFile
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/octet-stream"
            putExtra(Intent.EXTRA_STREAM, contentUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        return Intent.createChooser(intent, "Share PocketScore Data")
    }

    /**
     * Decodes a PocketScoreShare object from a URI.
     */
    fun decodeFromUri(context: Context, uri: Uri): PocketScoreShare? {
        return try {
            val jsonString = context.contentResolver.openInputStream(uri)?.use { 
                it.bufferedReader().readText() 
            } ?: return null
            json.decodeFromString<PocketScoreShare>(jsonString)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

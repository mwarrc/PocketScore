package com.mwarrc.pocketscore.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import com.mwarrc.pocketscore.domain.model.PocketScoreShare
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Utilities for sharing and importing PocketScore data.
 * 
 * Handles serialization, file creation, and Intent generation for data sharing.
 */
object ShareUtils {

    private const val TAG = "ShareUtils"
    private const val DEFAULT_FILE_NAME = "pocketscore_backup.pscore"
    private const val MIME_TYPE = "application/octet-stream"
    
    private val json = Json { 
        ignoreUnknownKeys = true 
        prettyPrint = true
        encodeDefaults = true
    }

    /**
     * Creates a sharing intent for PocketScore data.
     * 
     * Serializes data to JSON, writes to temporary file, and creates share intent.
     * 
     * @param context Application context
     * @param shareData Data to share
     * @param fileName Output filename (default: pocketscore_backup.pscore)
     * @return Chooser intent for sharing
     * @throws IOException if file creation fails
     * @throws SerializationException if JSON encoding fails
     */
    fun getShareIntent(
        context: Context,
        shareData: PocketScoreShare,
        fileName: String? = null
    ): Intent {
        return try {
            val finalFileName = fileName ?: generateTimestampedFileName()
            val jsonString = json.encodeToString(shareData)
            val tempFile = createTempFile(context, finalFileName, jsonString)
            val contentUri = getFileUri(context, tempFile)
            
            createShareIntent(contentUri)
        } catch (e: SerializationException) {
            Log.e(TAG, "Failed to serialize share data", e)
            throw e
        } catch (e: IOException) {
            Log.e(TAG, "Failed to create temp file", e)
            throw e
        }
    }

    /**
     * Decodes PocketScore data from a URI.
     * 
     * @param context Application context
     * @param uri URI pointing to .pscore file
     * @return Decoded data, or null if decoding fails
     */
    fun decodeFromUri(context: Context, uri: Uri): PocketScoreShare? {
        return try {
            val jsonString = readUriContent(context, uri)
            json.decodeFromString<PocketScoreShare>(jsonString)
        } catch (e: SerializationException) {
            Log.e(TAG, "Failed to deserialize data from URI", e)
            null
        } catch (e: IOException) {
            Log.e(TAG, "Failed to read URI content", e)
            null
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error decoding from URI", e)
            null
        }
    }
    
    /**
     * Creates a temporary file with the given content.
     * 
     * @param context Application context
     * @param fileName File name
     * @param content File content
     * @return Created file
     */
    private fun createTempFile(context: Context, fileName: String, content: String): File {
        val sanitizedFileName = sanitizeFileName(fileName)
        val tempFile = File(context.cacheDir, sanitizedFileName)
        
        tempFile.writeText(content)
        Log.d(TAG, "Created temp file: ${tempFile.absolutePath}")
        
        return tempFile
    }
    
    /**
     * Gets a content URI for a file using FileProvider.
     * 
     * @param context Application context
     * @param file File to get URI for
     * @return Content URI
     */
    private fun getFileUri(context: Context, file: File): Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }
    
    /**
     * Creates a share intent with the given content URI.
     * 
     * @param contentUri URI to share
     * @return Chooser intent
     */
    private fun createShareIntent(contentUri: Uri): Intent {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = MIME_TYPE
            putExtra(Intent.EXTRA_STREAM, contentUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        return Intent.createChooser(intent, "Share PocketScore Data")
    }
    
    /**
     * Reads content from a URI.
     * 
     * @param context Application context
     * @param uri URI to read from
     * @return File content as string
     */
    private fun readUriContent(context: Context, uri: Uri): String {
        return context.contentResolver.openInputStream(uri)?.use { inputStream ->
            inputStream.bufferedReader().use { it.readText() }
        } ?: throw IOException("Failed to open input stream for URI: $uri")
    }
    
    /**
     * Sanitizes a filename to prevent path traversal and invalid characters.
     * 
     * @param fileName Original filename
     * @return Sanitized filename
     */
    private fun sanitizeFileName(fileName: String): String {
        return fileName
            .replace(Regex("[^a-zA-Z0-9._-]"), "_")
            .take(255)
            .ifBlank { DEFAULT_FILE_NAME }
    }
    
    /**
     * Cleans up old temporary share files.
     * 
     * Should be called periodically to prevent cache buildup.
     * 
     * @param context Application context
     * @param maxAgeMillis Maximum age of files to keep (default: 24 hours)
     */
    fun cleanupOldFiles(context: Context, maxAgeMillis: Long = 24 * 60 * 60 * 1000) {
        try {
            val cacheDir = context.cacheDir
            val now = System.currentTimeMillis()
            
            cacheDir.listFiles()?.forEach { file ->
                if (file.name.endsWith(".pscore") && now - file.lastModified() > maxAgeMillis) {
                    if (file.delete()) {
                        Log.d(TAG, "Deleted old temp file: ${file.name}")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning up old files", e)
        }
    }
    
    /**
     * Generates a filename with the current timestamp.
     * Format: pocketscore_backup_yyyyMMdd_HHmmss.pscore
     */
    private fun generateTimestampedFileName(): String {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        return "pocketscore_backup_$timestamp.pscore"
    }
}
package com.example.listsqre_revamped

import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import java.io.BufferedReader
import java.io.BufferedWriter
import android.content.Context

// ----- ----- ----- CD text file handler ----- ----- ----- //

// use card title as file name
fun createTextFile(context: Context, fileName: String) {
    val file = File(context.filesDir, fileName)
    try {
        file.createNewFile()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

// use card title as file name
fun deleteTextFile(context: Context, fileName: String) {
    val file = File(context.filesDir, fileName)
    if (file.exists()) {
        file.delete()
    } else {
        // do nothing
    }
}

// use card title as file name
fun deleteSelTextFile(context: Context, list: List<Card>) {
    for(item in list) {
        deleteTextFile(context, item.title)
    }
}

// ----- ----- ----- individual text file handler ----- ----- ----- //

fun storeDataToFile(context: Context, fileName: String?, data: String) {
    val file = fileName?.let { File(context.filesDir, it) }
    try {
        val writer = BufferedWriter(FileWriter(file, true))
        writer.write(data)
        writer.flush()
        writer.close()
    } catch (e: IOException) {
        e.printStackTrace()
    }
}

fun readFromFile(context: Context, fileName: String?) {
    val file = fileName?.let { File(context.filesDir, it) }
    try {
        val reader = BufferedReader(FileReader(file))
        val fileContent = StringBuilder()
        var currentChar: Int
        while (reader.read().also { currentChar = it } != GlobalVar.EOF) {
            fileContent.append(currentChar.toChar())
        }
        reader.close()
        val items = fileContent.toString().split(GlobalVar.ITEM_DELIMITER).filter { it.isNotBlank() }
        for (item in items) {
            val fields = item.split(GlobalVar.DELIMITER).map { it.trim() }
            if (fields.size >= 3) {
                // val id = fields.getOrNull(0)?.toIntOrNull() ?: 0
                val title = fields.getOrNull(1) ?: ""
                val description = fields.getOrNull(2) ?: ""
                // val isSelected = fields.getOrNull(3)?.toBoolean() ?: false
                val isPinned = fields.getOrNull(4)?.toBoolean() == true
                TextFileObjectHandler.AddItem(title, description, isPinned)
            } else {
                /* do nothing */
            }
        }
    } catch (e: IOException) {
        e.printStackTrace()
    } catch (e: NumberFormatException) {
        e.printStackTrace()
    }
}

fun updateTextFile(context: Context, fileName: String?) {
    val file = fileName?.let { File(context.filesDir, it) }
    try {
        if((file != null) && file.exists()) {
            if(TextFileObjectHandler.GetEntireList().isNotEmpty() && !TextFileObjectHandler.empty) {
                val fileWriter = FileWriter(file, false)
                fileWriter.close()
                for(item in TextFileObjectHandler.GetEntireList()) {
                    storeDataToFile(context, fileName, item.ItemDelimiterString())
                }
            } else if(TextFileObjectHandler.GetEntireList().isEmpty() && TextFileObjectHandler.empty) {
                val fileWriter = FileWriter(file, false)
                fileWriter.close()
            } else { /* do nothing */ }
        } else { /* do nothing */ }
    } catch (e: IOException) {
        e.printStackTrace()
    }
}

/* --- obsolete, no use case ---
fun clearTextFile(context: Context, fileName: String?) {
    val file = fileName?.let { File(context.filesDir, it) }
    try {
        if (file != null) {
            if(file.exists()) {
                val fileWriter = FileWriter(file, false)
                fileWriter.close()
            }
        }
    } catch (e: IOException) {
        e.printStackTrace()
    }
}
*/
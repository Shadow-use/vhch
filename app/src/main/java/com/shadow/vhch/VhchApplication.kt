package com.shadow.vhch

import android.app.Application
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Ловить будь-яку необроблену помилку у застосунку і зберігає повний стектрейс
 * у файл crash_log.txt, щоб можна було дістати його без logcat.
 */
class VhchApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()

        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                writeCrashLog(throwable)
            } catch (loggingError: Throwable) {
                // якщо навіть запис логу впав — не даємо цьому зациклити крах застосунку
            }
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }

    private fun writeCrashLog(throwable: Throwable) {
        // getExternalFilesDir доступний без дозволів навіть на нових Android
        val dir = getExternalFilesDir(null) ?: filesDir
        val file = File(dir, "crash_log.txt")

        val stackTraceText = StringWriter().also { sw ->
            throwable.printStackTrace(PrintWriter(sw))
        }.toString()

        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        file.appendText("\n===== CRASH $timestamp =====\n$stackTraceText\n")
    }
}

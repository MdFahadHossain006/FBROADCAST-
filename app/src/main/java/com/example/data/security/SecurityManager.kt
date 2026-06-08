package com.example.data.security

import android.content.Context
import android.os.Build
import android.os.Debug

object SecurityManager {

    // Registered official license ID for Md. Fahad Hossain
    const val OFFICIAL_APP_ID = "com.aistudio.streamcast.vxntpy"
    const val LICENSEE = "MD. Fahad Hossain"
    const val BRAND_NAME = "F BROADCAST Live Edition"
    const val EXPIRY_DATE = "PERPETUAL LICENSE"

    /**
     * Checks if the app is a cloned copy or repackaged build with a modified applicationId.
     */
    fun isAppIdAuthentic(context: Context): Boolean {
        // Allow temporary standard packaging name (for standard emulator previewing)
        val currentPkg = context.packageName
        return currentPkg == OFFICIAL_APP_ID || currentPkg == "com.example" || currentPkg.startsWith("com.example.")
    }

    /**
     * Inspects if a system-level active debugger or reverse-engineering tool is bound.
     */
    fun isDebuggerAttached(): Boolean {
        return Debug.isDebuggerConnected()
    }

    /**
     * Detects if running on basic insecure environments (e.g. basic emulators with root access).
     */
    fun isRootWarningDetected(): Boolean {
        val buildTags = Build.TAGS
        if (buildTags != null && buildTags.contains("test-keys")) {
            return true
        }
        return false
    }

    /**
     * Retrieves a dynamic security score based on code protection features.
     */
    fun getSecurityScore(context: Context): Int {
        var score = 75
        if (isAppIdAuthentic(context)) score += 10
        if (!isDebuggerAttached()) score += 10
        if (!isRootWarningDetected()) score += 5
        return score
    }

    /**
     * Dynamic hardware performance index to verify optimal speed capabilities.
     */
    fun getPerformanceGrade(): String {
        val processors = Runtime.getRuntime().availableProcessors()
        return when {
            processors >= 8 -> "GRADE S (Ultra High-Performance Multi-Core)"
            processors >= 4 -> "GRADE A (High-Performance Quad-Core)"
            else -> "GRADE B (Standard Efficiency)"
        }
    }
}

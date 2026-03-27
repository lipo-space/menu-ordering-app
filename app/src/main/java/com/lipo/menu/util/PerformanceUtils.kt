package com.lipo.menu.util

import android.util.Log
import kotlin.system.measureTimeMillis

object PerformanceUtils {
    const val TAG = "Performance"

    inline fun <T> measurePerformance(operation: String, block: () -> T): T {
        var result: T
        val time = measureTimeMillis {
            result = block()
        }

        if (time > 100) {
            Log.w(TAG, "$operation took ${time}ms (slow)")
        } else {
            Log.d(TAG, "$operation took ${time}ms")
        }

        return result
    }

    inline fun <T> measureAndAssert(operation: String, maxTimeMs: Long, block: () -> T): T {
        var result: T
        val time = measureTimeMillis {
            result = block()
        }

        check(time <= maxTimeMs) {
            "$operation took ${time}ms, expected <= ${maxTimeMs}ms"
        }

        return result
    }
}

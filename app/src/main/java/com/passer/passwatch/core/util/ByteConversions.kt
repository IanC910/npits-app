package com.passer.passwatch.core.util

import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder


/**
 * Utility function to convert any type of value to a byte array.
 *
 * @param value The value of any type (e.g., Double, Int, String) to be converted.
 * @return The byte array representation of the value.
 */
fun <T> convertToBytes(value: T): ByteArray {
    return when (value) {
        is Double -> {
            val buffer = ByteBuffer.allocate(8)
            buffer.order(ByteOrder.LITTLE_ENDIAN)  // BLE typically uses little-endian byte order
            buffer.putDouble(value)
            buffer.array()
        }
        is Float -> {
            val buffer = ByteBuffer.allocate(4)
            buffer.order(ByteOrder.LITTLE_ENDIAN)
            buffer.putFloat(value)
            buffer.array()
        }
        is Int -> {
            val buffer = ByteBuffer.allocate(4)
            buffer.order(ByteOrder.LITTLE_ENDIAN)
            buffer.putInt(value)
            buffer.array()
        }
        is Long -> {
            val buffer = ByteBuffer.allocate(8)
            buffer.order(ByteOrder.LITTLE_ENDIAN)
            buffer.putLong(value)
            buffer.array()
        }
        is String -> {
            value.toByteArray(Charsets.UTF_8)
        }
        else -> throw IllegalArgumentException("Unsupported type: ${value!!::class.java}")
    }
}

/**
 * Converts a ByteArray to a specified type.
 */
inline fun <reified T> convertFromBytes(bytes: ByteArray?): T? {
    if (bytes == null) return null
    return try {
        when (T::class) {
            Int::class -> ByteBuffer.wrap(bytes).int as T
            Float::class -> ByteBuffer.wrap(bytes).float as T
            Double::class -> ByteBuffer.wrap(bytes).double as T
            String::class -> String(bytes) as T
            ByteArray::class -> bytes as T
            else -> throw IllegalArgumentException("Unsupported conversion type: ${T::class}")
        }
    } catch (e: Exception) {
        Log.i("BLECharacteristic", "Error converting bytes to type ${T::class}: ${e.message}")
        null
    }
}
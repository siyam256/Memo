package com.example.data

import androidx.compose.ui.geometry.Offset

object SignatureSerializer {
    fun serialize(strokes: List<List<Offset>>): String {
        return strokes.joinToString("|") { stroke ->
            stroke.joinToString(";") { offset ->
                "${offset.x},${offset.y}"
            }
        }
    }

    fun deserialize(serialized: String): List<List<Offset>> {
        if (serialized.isBlank()) return emptyList()
        return try {
            serialized.split("|").map { strokeStr ->
                strokeStr.split(";").mapNotNull { offsetStr ->
                    val coords = offsetStr.split(",")
                    if (coords.size == 2) {
                        val x = coords[0].toFloatOrNull()
                        val y = coords[1].toFloatOrNull()
                        if (x != null && y != null) {
                            Offset(x, y)
                        } else null
                    } else null
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}

/*
 * SPDX-FileCopyrightText: 2024 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

import java.io.File

fun parseEnvFile(file: File): Map<String, String> {
    if (!file.exists()) return mapOf()
    val map = mutableMapOf<String, String>()
    for (line in file.readText().lines()) {
        if (line.isEmpty() || line.startsWith("#")) continue
        val parts = line.split("=", limit = 2)
        map[parts[0]] = parts.getOrNull(1) ?: ""
    }
    return map
}

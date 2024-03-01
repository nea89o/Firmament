/*
 * SPDX-FileCopyrightText: 2024 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.util

import java.io.InputStream
import kotlin.io.path.inputStream
import kotlin.jvm.optionals.getOrNull
import net.minecraft.util.Identifier
import moe.nea.firmament.repo.RepoDownloadManager


fun Identifier.openFirmamentResource(): InputStream {
    val resource = MC.resourceManager.getResource(this).getOrNull()
    if (resource == null) {
        if (namespace == "neurepo")
            return RepoDownloadManager.repoSavedLocation.resolve(path).inputStream()
        error("Could not read resource $this")
    }
    return resource.inputStream
}


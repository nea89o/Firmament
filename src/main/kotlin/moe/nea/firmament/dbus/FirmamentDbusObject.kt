/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.dbus

import moe.nea.firmament.repo.RepoManager

object FirmamentDbusObject : FirmamentDbusInterface {
    override fun sayHello(): String {
        return "Hello from Firmanet"
    }

    override fun getCurrentRepoCommit(): String {
        return RepoManager.currentDownloadedSha ?: "none"
    }

    override fun requestRepoReDownload() {
        RepoManager.launchAsyncUpdate()
    }

    override fun getObjectPath(): String {
        return "/moe/nea/Firmament"
    }
}

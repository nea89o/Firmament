package moe.nea.notenoughupdates.dbus

import moe.nea.notenoughupdates.repo.RepoManager

object NEUDbusObject : NEUDbusInterface {
    override fun sayHello(): String {
        return "Hello from NEU"
    }

    override fun getCurrentRepoCommit(): String {
        return RepoManager.currentDownloadedSha ?: "none"
    }

    override fun requestRepoReDownload() {
        RepoManager.launchAsyncUpdate()
    }

    override fun getObjectPath(): String {
        return "/moe/nea/NotEnoughUpdates"
    }
}

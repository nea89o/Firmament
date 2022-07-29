package moe.nea.notenoughupdates.repo

import moe.nea.notenoughupdates.NotEnoughUpdates

object RepoDownloadManager {

    val repoSavedLocation = NotEnoughUpdates.DATA_DIR.resolve("repo-extracted")
    val repoMetadataLocation = NotEnoughUpdates.DATA_DIR.resolve("loaded-repo.json")

    data class RepoMetadata(
        var latestCommit: String,
        var user: String,
        var repository: String,
        var branch: String,
    )

}

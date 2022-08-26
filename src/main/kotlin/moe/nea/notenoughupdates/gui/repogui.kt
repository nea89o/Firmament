package moe.nea.notenoughupdates.gui

import moe.nea.notenoughupdates.repo.RepoManager
import net.minecraft.text.Text

fun repoGui(): ConfigGui<RepoManager.Config> {
    return ConfigGui(RepoManager) {
        title(Text.literal("NotEnoughUpdates Repo Settings"))
        toggle(Text.literal("Auto Update"), RepoManager.Config::autoUpdate)
        textfield(
            Text.literal("Repo Username"),
            Text.literal("<github user>"),
            RepoManager.Config::user,
            maxLength = 255
        )
        textfield(
            Text.literal("Repo Name"),
            Text.literal("<repo name>"),
            RepoManager.Config::repo
        )
        textfield(
            Text.literal("Repo Branch"),
            Text.literal("<repo branch>"),
            RepoManager.Config::branch
        )
        button(
            Text.literal("Reset to Defaults"),
            Text.literal("Reset"),
        ) {
            RepoManager.config.user = "NotEnoughUpdates"
            RepoManager.config.repo = "NotEnoughUpdates-REPO"
            RepoManager.config.branch = "dangerous"
            reload()
        }
    }
}

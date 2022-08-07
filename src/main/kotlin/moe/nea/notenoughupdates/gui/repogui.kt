package moe.nea.notenoughupdates.gui

import moe.nea.notenoughupdates.repo.RepoManager
import net.minecraft.network.chat.Component

fun repoGui(): ConfigGui<RepoManager.Config> {
    return ConfigGui(RepoManager) {
        title(Component.literal("NotEnoughUpdates Repo Settings"))
        toggle(Component.literal("Auto Update"), RepoManager.Config::autoUpdate)
        textfield(
            Component.literal("Repo Username"),
            Component.literal("<github user>"),
            RepoManager.Config::user,
            maxLength = 255
        )
        textfield(
            Component.literal("Repo Name"),
            Component.literal("<repo name>"),
            RepoManager.Config::repo
        )
        textfield(
            Component.literal("Repo Branch"),
            Component.literal("<repo branch>"),
            RepoManager.Config::branch
        )
        button(
            Component.literal("Reset to Defaults"),
            Component.literal("Reset"),
        ) {
            RepoManager.config.user = "NotEnoughUpdates"
            RepoManager.config.repo = "NotEnoughUpdates-REPO"
            RepoManager.config.branch = "dangerous"
            reload()
        }
    }
}

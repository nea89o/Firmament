package moe.nea.notenoughupdates.gui

import net.minecraft.text.Text
import moe.nea.notenoughupdates.repo.RepoManager

fun repoGui(): ConfigGui<RepoManager.Config> {
    return ConfigGui(RepoManager) {
        title(Text.translatable("notenoughupdates.gui.repo.title"))
        toggle(Text.translatable("notenoughupdates.gui.repo.autoupdate"), RepoManager.Config::autoUpdate)
        textfield(
            Text.translatable("notenoughupdates.gui.repo.username"),
            Text.translatable("notenoughupdates.gui.repo.hint.username"),
            RepoManager.Config::user,
            maxLength = 255
        )
        textfield(
            Text.translatable("notenoughupdates.gui.repo.reponame"),
            Text.translatable("notenoughupdates.gui.repo.hint.reponame"),
            RepoManager.Config::repo
        )
        textfield(
            Text.translatable("notenoughupdates.gui.repo.branch"),
            Text.translatable("notenoughupdates.gui.repo.hint.branch"),
            RepoManager.Config::branch
        )
        button(
            Text.translatable("notenoughupdates.gui.repo.reset.label"),
            Text.translatable("notenoughupdates.gui.repo.reset"),
        ) {
            RepoManager.config.user = "NotEnoughUpdates"
            RepoManager.config.repo = "NotEnoughUpdates-REPO"
            RepoManager.config.branch = "dangerous"
            reload()
        }
    }
}

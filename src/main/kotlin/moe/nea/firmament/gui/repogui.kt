package moe.nea.firmament.gui

import net.minecraft.text.Text
import moe.nea.firmament.repo.RepoManager

fun repoGui(): ConfigGui<RepoManager.Config> {
    return ConfigGui(RepoManager) {
        title(Text.translatable("firmament.gui.repo.title"))
        toggle(Text.translatable("firmament.gui.repo.autoupdate"), RepoManager.Config::autoUpdate)
        textfield(
            Text.translatable("firmament.gui.repo.username"),
            Text.translatable("firmament.gui.repo.hint.username"),
            RepoManager.Config::user,
            maxLength = 255
        )
        textfield(
            Text.translatable("firmament.gui.repo.reponame"),
            Text.translatable("firmament.gui.repo.hint.reponame"),
            RepoManager.Config::repo
        )
        textfield(
            Text.translatable("firmament.gui.repo.branch"),
            Text.translatable("firmament.gui.repo.hint.branch"),
            RepoManager.Config::branch
        )
        button(
            Text.translatable("firmament.gui.repo.reset.label"),
            Text.translatable("firmament.gui.repo.reset"),
        ) {
            RepoManager.data.user = "NotEnoughUpdates"
            RepoManager.data.repo = "NotEnoughUpdates-REPO"
            RepoManager.data.branch = "dangerous"
            reload()
        }
    }
}

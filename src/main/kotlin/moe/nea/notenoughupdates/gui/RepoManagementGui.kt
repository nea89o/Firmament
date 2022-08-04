package moe.nea.notenoughupdates.gui

import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription
import io.github.cottonmc.cotton.gui.widget.WButton
import io.github.cottonmc.cotton.gui.widget.WLabel
import io.github.cottonmc.cotton.gui.widget.WTextField
import io.github.cottonmc.cotton.gui.widget.WToggleButton
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment
import io.github.cottonmc.cotton.gui.widget.data.Insets
import io.github.cottonmc.cotton.gui.widget.data.VerticalAlignment
import moe.nea.notenoughupdates.repo.RepoManager
import net.minecraft.network.chat.Component
import java.util.function.Consumer

class RepoManagementGui : LightweightGuiDescription() {
    init {
        val root = WGridPanelWithPadding(verticalPadding = 5)
        setRootPanel(root)
        root.setSize(0, 0)
        root.insets = Insets.ROOT_PANEL
        var col = 0

        WLabel(Component.literal("NotEnoughUpdates Repo Settings")).apply {
            root.add(this, 0, col, 11, 1)
            this.verticalAlignment = VerticalAlignment.TOP
            this.horizontalAlignment = HorizontalAlignment.CENTER
        }
        col += 1

        WLabel(Component.literal("Auto Update")).apply {
            root.add(this, 0, col, 5, 1)
            this.verticalAlignment = VerticalAlignment.CENTER
        }

        WToggleButton(Component.literal("Auto Update")).apply {
            this.toggle = RepoManager.config.autoUpdate
            this.onToggle = Consumer {
                RepoManager.config.autoUpdate = it
                RepoManager.markDirty()
            }
            root.add(this, 5, col, 1, 1)
        }
        col += 1

        WLabel(Component.literal("Repo Username")).apply {
            root.add(this, 0, col, 5, 1)
            this.verticalAlignment = VerticalAlignment.CENTER

        }

        val userName = WTextField(Component.literal("username")).apply {
            this.isEditable = true
            this.text = RepoManager.config.user
            this.setChangedListener {
                RepoManager.config.user = it
                RepoManager.markDirty()
            }
            root.add(this, 5, col, 6, 1)
        }

        col += 1
        WLabel(Component.literal("Repo Name")).apply {
            root.add(this, 0, col, 5, 1)
            this.verticalAlignment = VerticalAlignment.CENTER
        }

        val repoName = WTextField(Component.literal("repo name")).apply {
            this.isEditable = true
            this.text = RepoManager.config.repo
            this.setChangedListener {
                RepoManager.config.repo = it
                RepoManager.markDirty()
            }
            root.add(this, 5, col, 6, 1)
        }
        col += 1

        WLabel(Component.literal("Repo Branch")).apply {
            root.add(this, 0, col, 5, 1)
            this.verticalAlignment = VerticalAlignment.CENTER
        }

        val branchName = WTextField(Component.literal("repo branch")).apply {
            this.isEditable = true
            this.text = RepoManager.config.branch
            this.setChangedListener {
                RepoManager.config.branch = it
                RepoManager.markDirty()
            }
            root.add(this, 5, col, 6, 1)
        }
        col += 1

        WLabel(Component.literal("Reset to Defaults")).apply {
            root.add(this, 0, col, 5, 1)
            this.verticalAlignment = VerticalAlignment.CENTER
        }

        WButton(Component.literal("Reset")).apply {
            this.setOnClick {
                branchName.text = "master"
                userName.text = "NotEnoughUpdates"
                repoName.text = "NotEnoughUpdates-REPO"
                RepoManager.markDirty()
            }
            root.add(this, 5, col, 6, 1)
        }
    }
}
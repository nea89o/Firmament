package moe.nea.notenoughupdates.gui

import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription
import io.github.cottonmc.cotton.gui.widget.WGridPanel
import io.github.cottonmc.cotton.gui.widget.WLabel
import io.github.cottonmc.cotton.gui.widget.WTextField
import io.github.cottonmc.cotton.gui.widget.WToggleButton
import io.github.cottonmc.cotton.gui.widget.data.Insets
import moe.nea.notenoughupdates.repo.RepoManager
import net.minecraft.network.chat.Component
import java.util.function.Consumer

class RepoManagementGui : LightweightGuiDescription() {
    init {
        val root = WGridPanel()
        setRootPanel(root)

        root.setSize(256, 240)
        root.insets = Insets.ROOT_PANEL

        WLabel(Component.literal("Auto Update")).apply {
            root.add(this, 0, 1, 5, 1)
        }

        WToggleButton(Component.literal("Auto Update")).apply {
            this.toggle = RepoManager.config.autoUpdate
            this.onToggle = Consumer {
                RepoManager.config.autoUpdate = it
                RepoManager.markDirty()
            }
            root.add(this, 5, 1, 1, 1)
        }


        WLabel(Component.literal("Repo Username")).apply {
            root.add(this, 0, 2, 5, 1)
        }

        WTextField(Component.literal("username")).apply {
            this.isEditable = true
            this.text = RepoManager.config.user
            this.setChangedListener {
                RepoManager.config.user = it
                RepoManager.markDirty()
            }
            root.add(this, 5, 2, 6, 1)
        }

        WLabel(Component.literal("Repo Name")).apply {
            root.add(this, 0, 3, 5, 1)
        }

        WTextField(Component.literal("repo name")).apply {
            this.isEditable = true
            this.text = RepoManager.config.repo
            this.setChangedListener {
                RepoManager.config.repo = it
                RepoManager.markDirty()
            }
            root.add(this, 5, 3, 6, 1)
        }

        WLabel(Component.literal("Repo Branch")).apply {
            root.add(this, 0, 4, 5, 1)
        }

        WTextField(Component.literal("repo name")).apply {
            this.isEditable = true
            this.text = RepoManager.config.branch
            this.setChangedListener {
                RepoManager.config.branch = it
                RepoManager.markDirty()
            }
            root.add(this, 5, 4, 6, 1)
        }
    }
}
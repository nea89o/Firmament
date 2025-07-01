package moe.nea.firmament.gui.config

import com.google.auto.service.AutoService
import net.minecraft.client.gui.screen.Screen

@AutoService(FirmamentConfigScreenProvider::class)
class BuiltInConfigScreenProvider : FirmamentConfigScreenProvider {
    override val key: String
        get() = "builtin"

    override fun open(search: String?, parent: Screen?): Screen {
        return AllConfigsGui.makeBuiltInScreen(parent)
    }
}

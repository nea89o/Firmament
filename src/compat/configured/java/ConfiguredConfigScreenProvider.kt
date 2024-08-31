package moe.nea.firmament.compat.configured

import com.google.auto.service.AutoService
import com.mrcrayfish.configured.integration.CatalogueConfigFactory
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.gui.screen.Screen
import moe.nea.firmament.Firmament
import moe.nea.firmament.gui.config.FirmamentConfigScreenProvider

@AutoService(FirmamentConfigScreenProvider::class)
class ConfiguredConfigScreenProvider : FirmamentConfigScreenProvider {
    override val key: String
        get() = "configured"
    override val isEnabled: Boolean
        get() = FabricLoader.getInstance().isModLoaded("configured")

    override fun open(parent: Screen?): Screen {
        return CatalogueConfigFactory.createConfigScreen(
            parent,
            FabricLoader.getInstance().getModContainer(Firmament.MOD_ID).get())
    }
}

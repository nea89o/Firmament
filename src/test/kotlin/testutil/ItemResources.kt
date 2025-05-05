package moe.nea.firmament.test.testutil

import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtOps
import net.minecraft.nbt.StringNbtReader
import net.minecraft.registry.RegistryOps
import net.minecraft.text.Text
import net.minecraft.text.TextCodecs
import moe.nea.firmament.test.FirmTestBootstrap
import moe.nea.firmament.util.MC

object ItemResources {
	init {
		FirmTestBootstrap.bootstrapMinecraft()
	}

	fun loadString(path: String): String {
		require(!path.startsWith("/"))
		return ItemResources::class.java.classLoader
			.getResourceAsStream(path)!!
			.readAllBytes().decodeToString()
	}

	fun loadSNbt(path: String): NbtCompound {
		return StringNbtReader.readCompound(loadString(path))
	}
	fun getNbtOps(): RegistryOps<NbtElement> = MC.currentOrDefaultRegistries.getOps(NbtOps.INSTANCE)

	fun loadText(name: String): Text {
		return TextCodecs.CODEC.parse(getNbtOps(), loadSNbt("testdata/chat/$name.snbt"))
			.getOrThrow { IllegalStateException("Could not load test chat '$name': $it") }
	}

	fun loadItem(name: String): ItemStack {
		// TODO: make the load work with enchantments
		// TODO: use DFU to load older items
		return ItemStack.CODEC.parse(getNbtOps(), loadSNbt("testdata/items/$name.snbt"))
			.getOrThrow { IllegalStateException("Could not load test item '$name': $it") }
	}
}

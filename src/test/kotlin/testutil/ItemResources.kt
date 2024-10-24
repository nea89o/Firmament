package moe.nea.firmament.test.testutil

import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtOps
import net.minecraft.nbt.StringNbtReader
import net.minecraft.text.Text
import net.minecraft.text.TextCodecs
import moe.nea.firmament.test.FirmTestBootstrap

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
		return StringNbtReader.parse(loadString(path))
	}

	fun loadText(name: String): Text {
		return TextCodecs.CODEC.parse(NbtOps.INSTANCE, loadSNbt("testdata/chat/$name.snbt"))
			.getOrThrow { IllegalStateException("Could not load test chat '$name': $it") }
	}

	fun loadItem(name: String): ItemStack {
		// TODO: make the load work with enchantments
		return ItemStack.CODEC.parse(NbtOps.INSTANCE, loadSNbt("testdata/items/$name.snbt"))
			.getOrThrow { IllegalStateException("Could not load test item '$name': $it") }
	}
}

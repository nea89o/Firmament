package moe.nea.firmament.test.testutil

import com.mojang.datafixers.DSL
import com.mojang.datafixers.DataFixUtils
import com.mojang.datafixers.types.templates.Named
import com.mojang.serialization.Dynamic
import com.mojang.serialization.JsonOps
import net.minecraft.SharedConstants
import net.minecraft.datafixer.Schemas
import net.minecraft.datafixer.TypeReferences
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtOps
import net.minecraft.nbt.NbtString
import net.minecraft.nbt.StringNbtReader
import net.minecraft.registry.RegistryOps
import net.minecraft.text.Text
import net.minecraft.text.TextCodecs
import moe.nea.firmament.features.debug.ExportedTestConstantMeta
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

	fun tryMigrateNbt(
		nbtCompound: NbtCompound,
		typ: DSL.TypeReference,
	): NbtElement {
		val source = nbtCompound.get("source", ExportedTestConstantMeta.CODEC)
		nbtCompound.remove("source")
		if (source.isPresent) {
			val wrappedNbtSource = if (typ == TypeReferences.TEXT_COMPONENT && source.get().dataVersion < 4325) {
				// Per 1.21.5 text components are wrapped in a string, which firmament unwrapped in the snbt files
				NbtString.of(
					NbtOps.INSTANCE.convertTo(JsonOps.INSTANCE, nbtCompound)
						.toString())
			} else {
				nbtCompound
			}
			return Schemas.getFixer()
				.update(
					typ,
					Dynamic(NbtOps.INSTANCE, wrappedNbtSource),
					source.get().dataVersion,
					SharedConstants.getGameVersion().saveVersion.id
				).value
		}
		return nbtCompound
	}

	fun loadText(name: String): Text {
		return TextCodecs.CODEC.parse(
			getNbtOps(),
			tryMigrateNbt(loadSNbt("testdata/chat/$name.snbt"), TypeReferences.TEXT_COMPONENT)
		).getOrThrow { IllegalStateException("Could not load test chat '$name': $it") }
	}

	fun loadItem(name: String): ItemStack {
		try {
			val itemNbt = loadSNbt("testdata/items/$name.snbt")
			return ItemStack.CODEC.parse(getNbtOps(), tryMigrateNbt(itemNbt, TypeReferences.ITEM_STACK)).orThrow
		} catch (ex: Exception) {
			throw RuntimeException("Could not load item resource '$name'", ex)
		}
	}
}

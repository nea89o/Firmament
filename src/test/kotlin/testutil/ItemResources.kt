package moe.nea.firmament.test.testutil

import com.mojang.datafixers.DSL
import com.mojang.serialization.Dynamic
import com.mojang.serialization.JsonOps
import net.minecraft.SharedConstants
import net.minecraft.util.datafix.DataFixers
import net.minecraft.util.datafix.fixes.References
import net.minecraft.world.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.Tag
import net.minecraft.nbt.NbtOps
import net.minecraft.nbt.StringTag
import net.minecraft.nbt.TagParser
import net.minecraft.resources.RegistryOps
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.ComponentSerialization
import moe.nea.firmament.features.debug.ExportedTestConstantMeta
import moe.nea.firmament.test.FirmTestBootstrap
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.mc.LazyItemStack
import moe.nea.firmament.util.mc.MCTabListAPI

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

	fun loadSNbt(path: String): CompoundTag {
		return TagParser.parseCompoundFully(loadString(path))
	}

	fun getNbtOps(): RegistryOps<Tag> = MC.currentOrDefaultRegistries.createSerializationContext(NbtOps.INSTANCE)

	fun tryMigrateNbt(
        nbtCompound: CompoundTag,
        typ: DSL.TypeReference?,
	): Tag {
		val source = nbtCompound.read("source", ExportedTestConstantMeta.CODEC)
		nbtCompound.remove("source")
		if (source.isPresent) {
			val wrappedNbtSource = if (typ == References.TEXT_COMPONENT && source.get().dataVersion < 4325) {
				// Per 1.21.5 text components are wrapped in a string, which firmament unwrapped in the snbt files
				StringTag.valueOf(
					NbtOps.INSTANCE.convertTo(JsonOps.INSTANCE, nbtCompound)
						.toString()
				)
			} else {
				nbtCompound
			}
			if (typ != null) {
				return DataFixers.getDataFixer()
					.update(
						typ,
						Dynamic(NbtOps.INSTANCE, wrappedNbtSource),
						source.get().dataVersion,
						SharedConstants.getCurrentVersion().dataVersion().version
					).value
			} else {
				wrappedNbtSource
			}
		}
		return nbtCompound
	}

	fun loadTablist(name: String): MCTabListAPI.CurrentTabList {
		return MCTabListAPI.CurrentTabList.CODEC.parse(
			getNbtOps(),
			tryMigrateNbt(loadSNbt("testdata/tablist/$name.snbt"), null),
		).getOrThrow { IllegalStateException("Could not load tablist '$name': $it") }
	}

	fun loadText(name: String): Component {
		return ComponentSerialization.CODEC.parse(
			getNbtOps(),
			tryMigrateNbt(loadSNbt("testdata/chat/$name.snbt"), References.TEXT_COMPONENT)
		).getOrThrow { IllegalStateException("Could not load test chat '$name': $it") }
	}

	fun loadItem(name: String): LazyItemStack {
		try {
			//MC.currentOrDefaultRegistries.get(ResourceKey.create(Registries.ITEM, Identifier.withDefaultNamespace("diamond")))
			val itemNbt = loadSNbt("testdata/items/$name.snbt")
			return LazyItemStack.CODEC.parse(getNbtOps(), tryMigrateNbt(itemNbt, References.ITEM_STACK)).orThrow
		} catch (ex: Exception) {
			throw RuntimeException("Could not load item resource '$name'", ex)
		}
	}
}

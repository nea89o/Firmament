package moe.nea.firmament.util.mc

import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtList

fun Iterable<NbtElement>.toNbtList() = NbtList().also {
	for(element in this) {
		it.add(element)
	}
}

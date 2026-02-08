package moe.nea.firmament.repo

import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.util.Optional
import net.fabricmc.fabric.api.resource.ModResourcePack
import net.fabricmc.fabric.impl.resource.pack.ModPackResourcesSorter
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.metadata.ModMetadata
import kotlin.io.path.exists
import kotlin.io.path.isRegularFile
import kotlin.io.path.relativeTo
import kotlin.streams.asSequence
import net.minecraft.server.packs.AbstractPackResources
import net.minecraft.server.packs.resources.IoSupplier
import net.minecraft.server.packs.resources.FallbackResourceManager
import net.minecraft.server.packs.resources.Resource
import net.minecraft.server.packs.PackResources
import net.minecraft.server.packs.PackLocationInfo
import net.minecraft.server.packs.repository.PackSource
import net.minecraft.server.packs.PackType
import net.minecraft.server.packs.resources.ResourceMetadata
import net.minecraft.server.packs.metadata.MetadataSectionType
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import net.minecraft.util.FileUtil
import moe.nea.firmament.Firmament

class RepoModResourcePack(val basePath: Path) : ModResourcePack {
	companion object {
		fun append(packs: ModPackResourcesSorter) {
			Firmament.logger.info("Registering mod resource pack")
			packs.addPack(RepoModResourcePack(RepoDownloadManager.repoSavedLocation))
		}

		fun createResourceDirectly(identifier: Identifier): Optional<Resource> {
			val pack = RepoModResourcePack(RepoDownloadManager.repoSavedLocation)
			return Optional.of(
				Resource(
					pack,
					pack.getResource(PackType.CLIENT_RESOURCES, identifier) ?: return Optional.empty()
				) {
					val base =
						pack.getResource(PackType.CLIENT_RESOURCES, identifier.withPath(identifier.path + ".mcmeta"))
					if (base == null)
						ResourceMetadata.EMPTY
					else
						FallbackResourceManager.parseMetadata(base)
				}
			)
		}
	}

	override fun close() {
	}

	override fun getRootResource(vararg segments: String): IoSupplier<InputStream>? {
		return getFile(segments)?.let { IoSupplier.create(it) }
	}

	fun getFile(segments: Array<out String>): Path? {
		FileUtil.validatePath(*segments)
		val path = segments.fold(basePath, Path::resolve)
		if (!path.isRegularFile()) return null
		return path
	}

	override fun getResource(type: PackType, id: Identifier): IoSupplier<InputStream>? {
		if (type != PackType.CLIENT_RESOURCES) return null
		if (id.namespace != "neurepo") return null
		val file = getFile(id.path.split("/").toTypedArray())
		return file?.let { IoSupplier.create(it) }
	}

	override fun listResources(
		type: PackType,
		namespace: String,
		prefix: String,
		consumer: PackResources.ResourceOutput
	) {
		if (namespace != "neurepo") return
		if (type != PackType.CLIENT_RESOURCES) return

		val prefixPath = basePath.resolve(prefix)
		if (!prefixPath.exists())
			return
		Files.walk(prefixPath)
			.asSequence()
			.map { it.relativeTo(basePath) }
			.forEach {
				consumer.accept(
					Identifier.tryBuild("neurepo", it.toString()) ?: return@forEach,
					IoSupplier.create(it)
				)
			}
	}

	override fun getNamespaces(type: PackType): Set<String> {
		if (type != PackType.CLIENT_RESOURCES) return emptySet()
		return setOf("neurepo")
	}

	override fun <T : Any> getMetadataSection(metadataSerializer: MetadataSectionType<T>): T? {
		return AbstractPackResources.getMetadataFromStream(
			metadataSerializer, """
{
    "pack": {
        "pack_format": 12,
        "description": "NEU Repo Resources"
    }
}
""".trimIndent().byteInputStream(), location()
		)
	}


	override fun location(): PackLocationInfo {
		return PackLocationInfo("neurepo", Component.literal("NEU Repo"), PackSource.BUILT_IN, Optional.empty())
	}

	override fun getFabricModMetadata(): ModMetadata {
		return FabricLoader.getInstance().getModContainer("firmament")
			.get().metadata
	}

	override fun createOverlay(overlay: String): ModResourcePack {
		return RepoModResourcePack(basePath.resolve(overlay))
	}
}

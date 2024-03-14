/*
 * SPDX-FileCopyrightText: 2024 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.repo

import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import net.fabricmc.fabric.api.resource.ModResourcePack
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.metadata.ModMetadata
import kotlin.io.path.exists
import kotlin.io.path.isRegularFile
import kotlin.io.path.relativeTo
import kotlin.streams.asSequence
import net.minecraft.resource.AbstractFileResourcePack
import net.minecraft.resource.InputSupplier
import net.minecraft.resource.NamespaceResourceManager
import net.minecraft.resource.Resource
import net.minecraft.resource.ResourcePack
import net.minecraft.resource.ResourceType
import net.minecraft.resource.metadata.ResourceMetadata
import net.minecraft.resource.metadata.ResourceMetadataReader
import net.minecraft.util.Identifier
import net.minecraft.util.PathUtil
import moe.nea.firmament.Firmament

class RepoModResourcePack(val basePath: Path) : ModResourcePack {
    companion object {
        fun append(packs: MutableList<in ModResourcePack>) {
            Firmament.logger.info("Registering mod resource pack")
            packs.add(RepoModResourcePack(RepoDownloadManager.repoSavedLocation))
        }

        fun createResourceDirectly(identifier: Identifier): Optional<Resource> {
            val pack = RepoModResourcePack(RepoDownloadManager.repoSavedLocation)
            return Optional.of(
                Resource(
                    pack,
                    pack.open(ResourceType.CLIENT_RESOURCES, identifier) ?: return Optional.empty()
                ) {
                    val base =
                        pack.open(ResourceType.CLIENT_RESOURCES, identifier.withPath(identifier.path + ".mcmeta"))
                    if (base == null)
                        ResourceMetadata.NONE
                    else
                        NamespaceResourceManager.loadMetadata(base)
                }
            )
        }
    }

    override fun close() {
    }

    override fun openRoot(vararg segments: String): InputSupplier<InputStream>? {
        return getFile(segments)?.let { InputSupplier.create(it) }
    }

    fun getFile(segments: Array<out String>): Path? {
        PathUtil.validatePath(*segments)
        val path = segments.fold(basePath, Path::resolve)
        if (!path.isRegularFile()) return null
        return path
    }

    override fun open(type: ResourceType?, id: Identifier): InputSupplier<InputStream>? {
        if (type != ResourceType.CLIENT_RESOURCES) return null
        if (id.namespace != "neurepo") return null
        val file = getFile(id.path.split("/").toTypedArray())
        return file?.let { InputSupplier.create(it) }
    }

    override fun findResources(
        type: ResourceType?,
        namespace: String,
        prefix: String,
        consumer: ResourcePack.ResultConsumer
    ) {
        if (namespace != "neurepo") return
        if (type != ResourceType.CLIENT_RESOURCES) return

        val prefixPath = basePath.resolve(prefix)
        if (!prefixPath.exists())
            return
        Files.walk(prefixPath)
            .asSequence()
            .map { it.relativeTo(basePath) }
            .forEach {
                consumer.accept(Identifier.of("neurepo", it.toString()), InputSupplier.create(it))
            }
    }

    override fun getNamespaces(type: ResourceType?): Set<String> {
        if (type != ResourceType.CLIENT_RESOURCES) return emptySet()
        return setOf("neurepo")
    }

    override fun <T> parseMetadata(metaReader: ResourceMetadataReader<T>): T? {
        return AbstractFileResourcePack.parseMetadata(
            metaReader, """
{
    "pack": {
        "pack_format": 12,
        "description": "NEU Repo Resources"
    }
}
""".trimIndent().byteInputStream()
        )
    }

    override fun getName(): String {
        return "NEU Repo Resources"
    }

    override fun getFabricModMetadata(): ModMetadata {
        return FabricLoader.getInstance().getModContainer("firmament")
            .get().metadata
    }
}

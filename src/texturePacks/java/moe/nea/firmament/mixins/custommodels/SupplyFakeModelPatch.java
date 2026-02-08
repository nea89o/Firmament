package moe.nea.firmament.mixins.custommodels;

import com.google.gson.JsonObject;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import moe.nea.firmament.Firmament;
import moe.nea.firmament.features.texturepack.CustomSkyBlockTextures;
import moe.nea.firmament.features.texturepack.HeadModelChooser;
import moe.nea.firmament.features.texturepack.PredicateModel;
import moe.nea.firmament.util.ErrorUtil;
import net.minecraft.client.renderer.item.ClientItem;
import net.minecraft.client.resources.model.ClientItemInfoLoader;
import net.minecraft.client.renderer.item.BlockModelWrapper;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.PackResources;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Mixin(ClientItemInfoLoader.class)
public class SupplyFakeModelPatch {

	@ModifyReturnValue(
		method = "scheduleLoad",
		at = @At("RETURN")
	)
	private static CompletableFuture<ClientItemInfoLoader.LoadedClientInfos> injectFakeGeneratedModels(
		CompletableFuture<ClientItemInfoLoader.LoadedClientInfos> original,
		@Local(argsOnly = true) ResourceManager resourceManager,
		@Local(argsOnly = true) Executor executor
	) {
		return original.thenCompose(oldModels -> CompletableFuture.supplyAsync(() -> supplyExtraModels(resourceManager, oldModels), executor));
	}

	@Unique
	private static ClientItemInfoLoader.LoadedClientInfos supplyExtraModels(ResourceManager resourceManager, ClientItemInfoLoader.LoadedClientInfos oldModels) {
		if (!CustomSkyBlockTextures.TConfig.INSTANCE.getEnableLegacyMinecraftCompat()) return oldModels;
		Map<Identifier, ClientItem> newModels = new HashMap<>(oldModels.contents());
		var resources = resourceManager.listResources(
			"models/item",
			id -> (id.getNamespace().equals("firmskyblock") || id.getNamespace().equals("cittofirmgenerated"))
				      && id.getPath().endsWith(".json"));
		for (Map.Entry<Identifier, Resource> model : resources.entrySet()) {
			var resource = model.getValue();
			var itemModelId = model.getKey().withPath(it -> it.substring("models/item/".length(), it.length() - ".json".length()));
			var genericModelId = itemModelId.withPrefix("item/");
			var itemAssetId = itemModelId.withPrefix("items/");
			// TODO: inject tint indexes based on the json data here
			ItemModel.Unbaked unbakedModel = new BlockModelWrapper.Unbaked(genericModelId, List.of());
			// TODO: add a filter using the pack.mcmeta to opt out of this behaviour
			try (var is = resource.open()) {
				var jsonObject = Firmament.INSTANCE.getGson().fromJson(new InputStreamReader(is), JsonObject.class);
				unbakedModel = PredicateModel.Unbaked.fromLegacyJson(jsonObject, unbakedModel);
				unbakedModel = HeadModelChooser.Unbaked.fromLegacyJson(jsonObject, unbakedModel);
			} catch (Exception e) {
				ErrorUtil.INSTANCE.softError("Could not create resource for fake model supplication: " + model.getKey(), e);
			}
			if (resourceManager.getResource(itemAssetId.withSuffix(".json"))
			                   .map(Resource::source)
			                   .map(it -> isResourcePackNewer(resourceManager, it, resource.source()))
			                   .orElse(true)) {
				newModels.put(itemModelId, new ClientItem(
					unbakedModel,
					new ClientItem.Properties(true, true, 1F)
				));
			}
		}
		return new ClientItemInfoLoader.LoadedClientInfos(newModels);
	}

	@Unique
	private static boolean isResourcePackNewer(
            ResourceManager manager,
            PackResources null_, PackResources proposal) {
		var pack = manager.listPacks()
		                  .filter(it -> it == null_ || it == proposal)
		                  .collect(findLast());
		return pack.orElse(null_) != null_;
	}

	@Unique
	private static <T> Collector<T, ?, Optional<T>> findLast() {
		return Collectors.reducing(Optional.empty(), Optional::of,
		                           (left, right) -> right.isPresent() ? right : left);

	}

}

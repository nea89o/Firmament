package moe.nea.firmament.mixins.custommodels;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.item.ItemAsset;
import net.minecraft.client.item.ItemAssetsLoader;
import net.minecraft.client.render.item.model.BasicItemModel;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourcePack;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Mixin(ItemAssetsLoader.class)
public class SupplyFakeModelPatch {

	@ModifyReturnValue(
		method = "load",
		at = @At("RETURN")
	)
	private static CompletableFuture<ItemAssetsLoader.Result> injectFakeGeneratedModels(
		CompletableFuture<ItemAssetsLoader.Result> original,
		@Local(argsOnly = true) ResourceManager resourceManager,
		@Local(argsOnly = true) Executor executor
	) {
		return original.thenCompose(oldModels -> CompletableFuture.supplyAsync(() -> supplyExtraModels(resourceManager, oldModels), executor));
	}

	private static ItemAssetsLoader.Result supplyExtraModels(ResourceManager resourceManager, ItemAssetsLoader.Result oldModels) {
		Map<Identifier, ItemAsset> newModels = new HashMap<>(oldModels.contents());
		var resources = resourceManager.findResources(
			"models/item",
			id -> id.getNamespace().equals("firmskyblock")
				      && id.getPath().endsWith(".json")
				      && !id.getPath().substring("models/item/".length()).contains("/"));
		for (Map.Entry<Identifier, Resource> model : resources.entrySet()) {
			var resource = model.getValue();
			var itemModelId = model.getKey().withPath(it -> it.substring("models/item/".length(), it.length() - ".json".length()));
			// TODO: parse json file here and make use of it in order to generate predicate files.
			// TODO: add a filter using the pack.mcmeta to opt out of this behaviour
			var genericModelId = itemModelId.withPrefixedPath("item/");
			if (resourceManager.getResource(itemModelId)
			                   .map(Resource::getPack)
			                   .map(it -> isResourcePackNewer(resourceManager, it, resource.getPack()))
			                   .orElse(true)) {
				newModels.put(itemModelId, new ItemAsset(
					// TODO: inject tint indexes based on the json data here
					new BasicItemModel.Unbaked(genericModelId, List.of()),
					new ItemAsset.Properties(true)
				));
			}
		}
		return new ItemAssetsLoader.Result(newModels);
	}

	private static boolean isResourcePackNewer(
		ResourceManager manager,
		ResourcePack null_, ResourcePack proposal) {
		var pack = manager.streamResourcePacks()
		                  .filter(it -> it == null_ || it == proposal)
		                  .collect(findLast());
		return pack.orElse(null) == proposal;
	}

	private static <T> Collector<T, ?, Optional<T>> findLast() {
		return Collectors.reducing(Optional.empty(), Optional::of,
		                           (left, right) -> right.isPresent() ? right : left);

	}

}

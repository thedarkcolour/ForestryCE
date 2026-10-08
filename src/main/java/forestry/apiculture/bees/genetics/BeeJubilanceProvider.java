package forestry.apiculture.bees.genetics;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;

import forestry.api.apiculture.IBeeJubilance;

// not in a data package, the jar tasks exclude those and addons reach this through IBeeSpeciesType
public class BeeJubilanceProvider implements DataProvider {
	private final PackOutput.PathProvider pathProvider;
	private final CompletableFuture<HolderLookup.Provider> lookupProvider;
	private final Map<ResourceLocation, IBeeJubilance> jubilances;

	public BeeJubilanceProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, Map<ResourceLocation, IBeeJubilance> jubilances) {
		this.pathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "bee_jubilance");
		this.lookupProvider = lookupProvider;
		this.jubilances = jubilances;
	}

	@Override
	public CompletableFuture<?> run(CachedOutput cache) {
		return this.lookupProvider.thenCompose(provider -> {
			RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, provider);

			var futures = this.jubilances.entrySet().stream().map(entry -> {
				JsonElement json = IBeeJubilance.CODEC.encodeStart(ops, entry.getValue()).getOrThrow();
				return DataProvider.saveStable(cache, json, this.pathProvider.json(entry.getKey()));
			}).toArray(CompletableFuture[]::new);
			return CompletableFuture.allOf(futures);
		});
	}

	@Override
	public String getName() {
		return "Forestry Bee Jubilances";
	}
}

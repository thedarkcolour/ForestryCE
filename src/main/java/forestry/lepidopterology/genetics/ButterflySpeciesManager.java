package forestry.lepidopterology.genetics;

import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;

import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import forestry.Forestry;
import forestry.core.genetics.GeneticsReloadHandler;

/**
 * Datapack loader for butterfly species: a {@link SimpleJsonResourceReloadListener} over the {@code butterfly_species}
 * folder. Decodes each JSON entry via {@link ButterflySpeciesDefinition#codec()} (fail-soft: a bad file is logged and
 * skipped), stores the last-parsed map, and hands it to {@link GeneticsReloadHandler#rebuildButterflySpecies} to swap
 * into the live butterfly species type. Mirrors {@code forestry.arboriculture.genetics.TreeSpeciesManager}.
 * <p>
 * Singleton, registered once as a server reload listener (see {@code ModuleCore#registerReloadListeners}); the
 * client never registers it as a reload listener (it has no datapack access), but reuses the same instance as a
 * plain data holder for the definitions delivered by {@code ButterflySpeciesSyncPacket} on login/reload (see
 * {@link #setDefinitions}).
 */
public final class ButterflySpeciesManager extends SimpleJsonResourceReloadListener {
	public static final ButterflySpeciesManager INSTANCE = new ButterflySpeciesManager();

	private static final String FOLDER = "butterfly_species";

	// Empty until the first load (server: this class's own apply(); client: the sync packet). Never null. Volatile:
	// written from the reload game executor or the network handler thread, read from wherever getDefinitions() is
	// called (e.g. ModuleCore's OnDatapackSyncEvent listener).
	private volatile Map<ResourceLocation, ButterflySpeciesDefinition> definitions = Map.of();

	private ButterflySpeciesManager() {
		super(new Gson(), FOLDER);
	}

	/**
	 * @return The last-parsed species definitions: from the datapack on the server, or the sync packet on the
	 * client. Never {@code null}; empty before the first load.
	 */
	public Map<ResourceLocation, ButterflySpeciesDefinition> getDefinitions() {
		return this.definitions;
	}

	/**
	 * Client-side mirror: stores the definitions received via {@code ButterflySpeciesSyncPacket} directly, without
	 * going through the JSON decode path (the packet already carries decoded {@link ButterflySpeciesDefinition}s,
	 * produced by the registry-aware network stream codec).
	 */
	public void setDefinitions(Map<ResourceLocation, ButterflySpeciesDefinition> definitions) {
		this.definitions = definitions;
	}

	@Override
	protected void apply(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profiler) {
		// getRegistryLookup() (NeoForge ContextAwareReloadListener) carries the current reload's registry access,
		// populated before apply() for both the cold server start and every /reload - NOT ServerLifecycleHooks
		// (null on cold start). See BeeSpeciesManager's apply() for the full rationale.
		RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, getRegistryLookup());

		// Idempotent safety net: the product dispatch codec resolves the optional `type` key against these ids.
		forestry.core.genetics.ProductTypes.registerBuiltins();

		Map<ResourceLocation, ButterflySpeciesDefinition> parsed = new LinkedHashMap<>();
		for (Map.Entry<ResourceLocation, JsonElement> entry : object.entrySet()) {
			ResourceLocation id = entry.getKey();
			DataResult<ButterflySpeciesDefinition> result = ButterflySpeciesDefinition.codec().parse(ops, entry.getValue());
			result.resultOrPartial(error -> Forestry.LOGGER.error("Skipping butterfly species {}: {}", id, error))
				.ifPresent(def -> parsed.put(id, def));
		}

		this.definitions = Map.copyOf(parsed);
		GeneticsReloadHandler.rebuildButterflySpecies(this.definitions);
	}
}

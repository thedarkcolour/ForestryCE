package forestry.apiculture.genetics;

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
 * Datapack loader for bee species: a {@link SimpleJsonResourceReloadListener} over the {@code bee_species} folder.
 * Decodes each JSON entry via {@link BeeSpeciesDefinition#codec()} (fail-soft: a bad file is logged and skipped),
 * stores the last-parsed map, and hands it to {@link GeneticsReloadHandler#rebuildSpecies} to swap into the live
 * bee species type.
 * <p>
 * Singleton, registered once as a server reload listener (see {@code ModuleCore#registerReloadListeners}); the
 * client never registers it as a reload listener (it has no datapack access), but reuses the same instance as a
 * plain data holder for the definitions delivered by {@code BeeSpeciesSyncPacket} on login/reload (see
 * {@link #setDefinitions}).
 */
public final class BeeSpeciesManager extends SimpleJsonResourceReloadListener {
	public static final BeeSpeciesManager INSTANCE = new BeeSpeciesManager();

	private static final String FOLDER = "bee_species";

	// Empty until the first load (server: this class's own apply(); client: the sync packet). Never null. Volatile:
	// written from the reload game executor or the network handler thread, read from wherever getDefinitions() is
	// called (e.g. ModuleCore's OnDatapackSyncEvent listener).
	private volatile Map<ResourceLocation, BeeSpeciesDefinition> definitions = Map.of();

	private BeeSpeciesManager() {
		super(new Gson(), FOLDER);
	}

	/**
	 * @return The last-parsed species definitions: from the datapack on the server, or the sync packet on the
	 * client. Never {@code null}; empty before the first load.
	 */
	public Map<ResourceLocation, BeeSpeciesDefinition> getDefinitions() {
		return this.definitions;
	}

	/**
	 * Client-side mirror: stores the definitions received via {@code BeeSpeciesSyncPacket} directly, without going
	 * through the JSON decode path (the packet already carries decoded {@link BeeSpeciesDefinition}s, produced by
	 * the registry-aware network stream codec).
	 */
	public void setDefinitions(Map<ResourceLocation, BeeSpeciesDefinition> definitions) {
		this.definitions = definitions;
	}

	@Override
	protected void apply(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profiler) {
		// getRegistryLookup() (from NeoForge's ContextAwareReloadListener, a grandparent via
		// SimplePreparableReloadListener) is populated with the *current reload's* registry access before apply()
		// runs, for both the very first datapack load at server start and every subsequent /reload - see
		// ReloadableServerResources#loadResources, which injects it into every ContextAwareReloadListener right
		// after AddReloadListenerEvent fires, before the listeners' apply() phase.
		//
		// This is deliberately NOT ServerLifecycleHooks.getCurrentServer(): that field is only set once
		// handleServerAboutToStart runs, which - for both DedicatedServer and GameTestServer - happens *after*
		// WorldLoader.load() has already built and applied the initial ReloadableServerResources. Using
		// getCurrentServer() here would make apply() silently no-op (0 species loaded) on every cold server start,
		// only working from the second (/reload) pass onward.
		RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, getRegistryLookup());

		// Idempotent safety net: the product dispatch codec resolves the optional `type` key against these ids.
		forestry.core.genetics.ProductTypes.registerBuiltins();

		Map<ResourceLocation, BeeSpeciesDefinition> parsed = new LinkedHashMap<>();
		for (Map.Entry<ResourceLocation, JsonElement> entry : object.entrySet()) {
			ResourceLocation id = entry.getKey();
			DataResult<BeeSpeciesDefinition> result = BeeSpeciesDefinition.codec().parse(ops, entry.getValue());
			result.resultOrPartial(error -> Forestry.LOGGER.error("Skipping bee species {}: {}", id, error))
				.ifPresent(def -> parsed.put(id, def));
		}

		this.definitions = Map.copyOf(parsed);
		GeneticsReloadHandler.rebuildSpecies(this.definitions);
	}
}

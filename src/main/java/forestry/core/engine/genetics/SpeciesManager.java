package forestry.core.engine.genetics;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;

import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import forestry.Forestry;

public abstract class SpeciesManager<D extends ISpeciesDefinition> extends SimpleJsonResourceReloadListener {
	private final String folder;
	// a supplier because the codec needs the karyotype, which does not exist at class init
	private final Supplier<Codec<D>> codec;
	private final Consumer<Map<ResourceLocation, D>> rebuild;

	private Map<ResourceLocation, D> definitions = Map.of();

	protected SpeciesManager(String folder, Supplier<Codec<D>> codec, Consumer<Map<ResourceLocation, D>> rebuild) {
		super(new Gson(), folder);
		this.folder = folder;
		this.codec = codec;
		this.rebuild = rebuild;
	}

	public Map<ResourceLocation, D> getDefinitions() {
		return this.definitions;
	}

	// used by the client to store synced definitions
	public void setDefinitions(Map<ResourceLocation, D> definitions) {
		this.definitions = definitions;
	}

	@Override
	protected void apply(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profiler) {
		// not ServerLifecycleHooks, the server is still null during the first load
		RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, getRegistryLookup());
		Codec<D> codec = this.codec.get();

		Map<ResourceLocation, D> parsed = new LinkedHashMap<>();
		for (Map.Entry<ResourceLocation, JsonElement> entry : object.entrySet()) {
			ResourceLocation id = entry.getKey();
			codec.parse(ops, entry.getValue())
				.resultOrPartial(error -> Forestry.LOGGER.error("Skipping {} {}: {}", this.folder, id, error))
				.ifPresent(def -> parsed.put(id, def));
		}

		this.definitions = Map.copyOf(parsed);
		this.rebuild.accept(this.definitions);
	}
}

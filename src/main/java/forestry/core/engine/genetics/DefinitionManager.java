package forestry.core.engine.genetics;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

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

public abstract class DefinitionManager<T> extends SimpleJsonResourceReloadListener {
	private final String folder;
	private final Codec<T> codec;
	private final Consumer<Map<ResourceLocation, T>> rebuild;

	private Map<ResourceLocation, T> definitions = Map.of();

	protected DefinitionManager(String folder, Codec<T> codec, Consumer<Map<ResourceLocation, T>> rebuild) {
		super(new Gson(), folder);
		this.folder = folder;
		this.codec = codec;
		this.rebuild = rebuild;
	}

	public Map<ResourceLocation, T> getDefinitions() {
		return this.definitions;
	}

	// used by the client to store synced definitions
	public void setDefinitions(Map<ResourceLocation, T> definitions) {
		this.definitions = definitions;
	}

	@Override
	protected void apply(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profiler) {
		// not ServerLifecycleHooks, the server is still null during the first load
		RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, getRegistryLookup());

		Map<ResourceLocation, T> parsed = new LinkedHashMap<>();
		for (Map.Entry<ResourceLocation, JsonElement> entry : object.entrySet()) {
			ResourceLocation id = entry.getKey();
			this.codec.parse(ops, entry.getValue())
				.resultOrPartial(error -> Forestry.LOGGER.error("Skipping {} {}: {}", this.folder, id, error))
				.ifPresent(def -> parsed.put(id, def));
		}

		this.definitions = Map.copyOf(parsed);
		this.rebuild.accept(this.definitions);
	}
}

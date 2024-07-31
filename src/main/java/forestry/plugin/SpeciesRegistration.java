package forestry.plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.function.Consumer;

import net.minecraft.resources.ResourceLocation;

import forestry.api.plugin.IBeeSpeciesBuilder;

public abstract class SpeciesRegistration<I, B extends I> {
	private final LinkedHashMap<ResourceLocation, B> speciesBuilders = new LinkedHashMap<>();
	private final HashMap<ResourceLocation, ArrayList<Consumer<IBeeSpeciesBuilder>>> modifiedSpecies = new HashMap<>();

	protected abstract B createSpeciesBuilder(ResourceLocation id, String genus, String species, MutationsRegistration mutations);

	protected I register(ResourceLocation id, String genus, String species) {
		if (this.speciesBuilders.containsKey(id)) {
			throw new IllegalStateException("A species is already registered with that ID: " + id);
		} else {
			B builder = createSpeciesBuilder(id, genus, species, new MutationsRegistration(id));

			this.speciesBuilders.put(id, builder);

			return builder;
		}
	}

	public void modifySpecies(ResourceLocation id, Consumer<IBeeSpeciesBuilder> action) {
		this.modifiedSpecies.computeIfAbsent(id, key -> new ArrayList<>()).add(action);
	}
}
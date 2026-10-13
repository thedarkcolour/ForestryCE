package forestry.arboriculture.trees.genetics;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

import net.minecraft.resources.ResourceLocation;

import forestry.Forestry;
import forestry.api.arboriculture.ITreeSpecies;
import forestry.api.arboriculture.genetics.ITreeSpeciesType;
import forestry.arboriculture.trees.TreeSpecies;
import forestry.core.engine.genetics.SpeciesType;
import forestry.core.platform.util.SpeciesUtil;

public final class ArboricultureReloadHandler {
	@SuppressWarnings("unchecked")
	public static void rebuildTreeSpecies(Map<ResourceLocation, TreeSpeciesDefinition> defs) {
		ITreeSpeciesType type = SpeciesUtil.TREE_TYPE.get();
		ImmutableMap.Builder<ResourceLocation, ITreeSpecies> builder = ImmutableMap.builderWithExpectedSize(defs.size());
		for (Map.Entry<ResourceLocation, TreeSpeciesDefinition> entry : defs.entrySet()) {
			ResourceLocation id = entry.getKey();
			TreeSpecies species = TreeSpeciesProjector.project(type, id, entry.getValue());
			if (species != null) {
				builder.put(id, species);
			}
		}
		ImmutableMap<ResourceLocation, ITreeSpecies> allSpecies = builder.build();
		((SpeciesType<ITreeSpecies, ?>) type).setSpecies(allSpecies);
		Forestry.LOGGER.info("Loaded {} tree species", allSpecies.size());
	}

	private ArboricultureReloadHandler() {
	}
}

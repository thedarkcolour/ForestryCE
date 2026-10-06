package forestry.apiculture.bees.genetics;

import java.util.LinkedHashMap;
import java.util.Map;

import com.google.common.collect.ImmutableMap;

import net.minecraft.resources.ResourceLocation;

import forestry.Forestry;
import forestry.api.apiculture.IBeeJubilance;
import forestry.api.apiculture.IFlowerType;
import forestry.api.apiculture.genetics.IBeeEffect;
import forestry.api.apiculture.genetics.IBeeSpecies;
import forestry.api.apiculture.genetics.IBeeSpeciesType;
import forestry.apiculture.bees.BeeSpecies;
import forestry.core.engine.genetics.SpeciesType;
import forestry.core.platform.util.SpeciesUtil;

public final class ApicultureReloadHandler {
	@SuppressWarnings("unchecked")
	public static void rebuildSpecies(Map<ResourceLocation, BeeSpeciesDefinition> defs) {
		IBeeSpeciesType type = SpeciesUtil.BEE_TYPE.get();
		ImmutableMap.Builder<ResourceLocation, IBeeSpecies> builder = ImmutableMap.builderWithExpectedSize(defs.size());
		for (Map.Entry<ResourceLocation, BeeSpeciesDefinition> entry : defs.entrySet()) {
			ResourceLocation id = entry.getKey();
			BeeSpecies species = BeeSpeciesProjector.project(type, id, entry.getValue());
			if (species != null) {
				builder.put(id, species);
			}
		}
		ImmutableMap<ResourceLocation, IBeeSpecies> allSpecies = builder.build();
		((SpeciesType<IBeeSpecies, ?>) type).setSpecies(allSpecies);
		Forestry.LOGGER.info("Loaded {} bee species", allSpecies.size());
	}

	public static void rebuildBeeEffects(Map<ResourceLocation, IBeeEffect> dataDefinitions) {
		BeeSpeciesType type = (BeeSpeciesType) SpeciesUtil.BEE_TYPE.get();
		Map<ResourceLocation, IBeeEffect> effective = new LinkedHashMap<>(type.getCodeBeeEffects());
		// datapack effects replace code-registered effects with the same ID
		effective.putAll(dataDefinitions);
		type.setBeeEffects(ImmutableMap.copyOf(effective));
	}

	public static void rebuildJubilances(Map<ResourceLocation, IBeeJubilance> dataDefinitions) {
		BeeSpeciesType type = (BeeSpeciesType) SpeciesUtil.BEE_TYPE.get();
		Map<ResourceLocation, IBeeJubilance> effective = new LinkedHashMap<>(type.getCodeJubilances());
		// datapack jubilances replace code-registered jubilances with the same ID
		effective.putAll(dataDefinitions);
		type.setJubilances(ImmutableMap.copyOf(effective));
	}

	private ApicultureReloadHandler() {
	}
}

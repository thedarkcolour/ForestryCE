package forestry.lepidopterology.butterflies.genetics;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import forestry.Forestry;
import forestry.api.lepidopterology.genetics.IButterflySpecies;
import forestry.api.lepidopterology.genetics.IButterflySpeciesType;
import forestry.core.engine.genetics.SpeciesType;
import forestry.core.platform.util.SpeciesUtil;
import forestry.lepidopterology.butterflies.ButterflySpecies;
import forestry.lepidopterology.entities.EntityButterfly;

public final class LepidopterologyReloadHandler {
	@SuppressWarnings("unchecked")
	public static void rebuildButterflySpecies(Map<ResourceLocation, ButterflySpeciesDefinition> defs) {
		IButterflySpeciesType type = SpeciesUtil.BUTTERFLY_TYPE.get();
		ImmutableMap.Builder<ResourceLocation, IButterflySpecies> builder = ImmutableMap.builderWithExpectedSize(defs.size());
		for (Map.Entry<ResourceLocation, ButterflySpeciesDefinition> entry : defs.entrySet()) {
			ResourceLocation id = entry.getKey();
			ButterflySpecies species = ButterflySpeciesProjector.project(type, id, entry.getValue());
			if (species != null) {
				builder.put(id, species);
			}
		}
		ImmutableMap<ResourceLocation, IButterflySpecies> allSpecies = builder.build();
		((SpeciesType<IButterflySpecies, ?>) type).setSpecies(allSpecies);
		Forestry.LOGGER.info("Loaded {} butterfly species", allSpecies.size());

		// loaded butterflies cache their species, a stale instance finds no mutations in MutationManager
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		// the server is still null during the first load
		if (server != null) {
			for (ServerLevel level : server.getAllLevels()) {
				for (EntityButterfly entity : level.getEntities(EntityTypeTest.forClass(EntityButterfly.class), e -> true)) {
					entity.refreshSpeciesFromReload();
				}
			}
		}
	}

	private LepidopterologyReloadHandler() {
	}
}

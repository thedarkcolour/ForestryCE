package forestry.apiculture.bees.genetics;

import javax.annotation.Nullable;

import net.minecraft.resources.ResourceLocation;

import forestry.Forestry;
import forestry.api.apiculture.genetics.IBeeSpeciesType;
import forestry.api.core.genetics.IGenome;
import forestry.api.core.genetics.alleles.IKaryotype;
import forestry.apiculture.bees.BeeSpecies;
import forestry.core.engine.genetics.SpeciesProjection;

// fails soft, a bad datapack species is logged and skipped so it cannot crash species loading
public final class BeeSpeciesProjector {
	private BeeSpeciesProjector() {
	}

	@Nullable
	public static BeeSpecies project(IBeeSpeciesType type, ResourceLocation id, BeeSpeciesDefinition def) {
		try {
			if (type.getJubilanceSafe(def.jubilance()) == null) {
				Forestry.LOGGER.warn("Skipping bee species {}: unknown jubilance {}", id, def.jubilance());
				return null;
			}
			IKaryotype karyotype = type.getKaryotype();
			IGenome genome = SpeciesProjection.buildGenome(karyotype, id, def);
			return new BeeSpecies(id, type, genome, new DefinitionBeeSpeciesBuilder(def));
		} catch (Exception e) {
			Forestry.LOGGER.error("Failed to project bee species {}", id, e);
			return null;
		}
	}
}

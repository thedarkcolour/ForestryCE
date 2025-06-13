package forestry.apiculture.blocks;

import forestry.api.ForestryConstants;
import forestry.api.apiculture.ForestryBeeSpecies;
import forestry.api.core.IBlockSubtype;
import net.minecraft.resources.ResourceLocation;

import java.util.Locale;

public enum BlockHiveType implements IBlockSubtype {
	FOREST(ForestryBeeSpecies.FOREST),
	MEADOWS(ForestryBeeSpecies.MEADOWS),
	DESERT(ForestryBeeSpecies.MODEST),
	JUNGLE(ForestryBeeSpecies.TROPICAL),
	END(ForestryBeeSpecies.ENDED),
	SNOW(ForestryBeeSpecies.WINTRY),
	SWAMP(ForestryBeeSpecies.MARSHY),
	SAVANNA(ForestryBeeSpecies.SAVANNA),
	LUSH(ForestryBeeSpecies.LUSH),
	AQUATIC(ForestryBeeSpecies.AQUATIC),
	NETHER(ForestryBeeSpecies.EMBITTERED),
	SWARM(ForestryConstants.forestry("none"));

	private final ResourceLocation speciesUid;

	BlockHiveType(ResourceLocation speciesUid) {
		this.speciesUid = speciesUid;
	}

	public ResourceLocation getSpeciesId() {
		return this.speciesUid;
	}

	@Override
	public String identifier() {
		return name().toLowerCase(Locale.ENGLISH);
	}
}

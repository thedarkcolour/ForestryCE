package forestry.apiculture.blocks;

import java.util.Locale;

import net.minecraft.resources.ResourceLocation;

import forestry.api.ForestryConstants;
import forestry.api.apiculture.ForestryBeeSpecies;
import forestry.api.core.IBlockSubtype;

public enum BlockHiveType implements IBlockSubtype {
	FOREST(ForestryBeeSpecies.FOREST),
	MEADOWS(ForestryBeeSpecies.MEADOWS),
	DESERT(ForestryBeeSpecies.MODEST),
	JUNGLE(ForestryBeeSpecies.TROPICAL),
	END(ForestryBeeSpecies.ENDED),
	SNOW(ForestryBeeSpecies.WINTRY),
	SWAMP(ForestryBeeSpecies.MARSHY),
	SAVANNA(ForestryBeeSpecies.SAVANNA),
	SWARM(ForestryConstants.forestry("none"));

	private final ResourceLocation speciesUid;

	BlockHiveType(ResourceLocation speciesUid) {
		this.speciesUid = speciesUid;
	}

	public ResourceLocation getSpeciesId() {
		return this.speciesUid;
	}

	@Override
	public String getSerializedName() {
		return name().toLowerCase(Locale.ENGLISH);
	}
}

package forestry.core.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;

import forestry.api.ForestryTags;

import thedarkcolour.modkit.data.MKTagsProvider;

public class ForestryBiomeTagsProvider {
	public static void addTags(MKTagsProvider<Biome> tags, HolderLookup.Provider lookup) {
		// Climates
		tags.tag(ForestryTags.Biomes.COLD_TEMPERATURE)
				.addTag(BiomeTags.IS_END);
		tags.tag(ForestryTags.Biomes.WARM_TEMPERATURE)
				.add(Biomes.WOODED_BADLANDS)
				.add(Biomes.SAVANNA)
				.add(Biomes.SAVANNA_PLATEAU)
				.add(Biomes.WINDSWEPT_SAVANNA);
		tags.tag(ForestryTags.Biomes.HELLISH_TEMPERATURE)
				.addTag(BiomeTags.IS_NETHER);
		tags.tag(ForestryTags.Biomes.ARID_HUMIDITY)
				.addTag(BiomeTags.IS_END);
		tags.tag(ForestryTags.Biomes.SHATTERED_SAVANNA).add(Biomes.WINDSWEPT_SAVANNA);
	}
}

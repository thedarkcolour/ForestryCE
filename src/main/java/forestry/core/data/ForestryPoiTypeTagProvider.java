package forestry.core.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.tags.PoiTypeTags;
import net.minecraft.world.entity.ai.village.poi.PoiType;

import forestry.apiculture.villagers.ApicultureVillagers;
import forestry.arboriculture.villagers.ArboricultureVillagers;

import thedarkcolour.modkit.data.MKTagsProvider;

public class ForestryPoiTypeTagProvider {
	protected static void addTags(MKTagsProvider<PoiType> tags, HolderLookup.Provider lookup) {
		tags.tag(PoiTypeTags.ACQUIRABLE_JOB_SITE).add(ArboricultureVillagers.POI_TREE_CHEST.get(), ApicultureVillagers.POI_APIARY.get());
	}
}

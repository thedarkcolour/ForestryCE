package forestry.core.data;

import forestry.apiculture.villagers.ApicultureVillagers;
import forestry.arboriculture.villagers.ArboricultureVillagers;
import net.minecraft.core.HolderLookup;
import net.minecraft.tags.PoiTypeTags;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import thedarkcolour.modkit.data.MKTagsProvider;

public class ForestryPoiTypeTagProvider {
	protected static void addTags(MKTagsProvider<PoiType> tags, HolderLookup.Provider lookup) {
		tags.tag(PoiTypeTags.ACQUIRABLE_JOB_SITE).add(ArboricultureVillagers.POI_TREE_CHEST.value(), ApicultureVillagers.POI_ESCRITOIRE.value());
	}
}

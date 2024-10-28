package forestry.core.data;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.PoiTypeTagsProvider;
import net.minecraft.tags.PoiTypeTags;

import net.minecraftforge.common.data.ExistingFileHelper;

import forestry.api.ForestryConstants;
import forestry.apiculture.villagers.ApicultureVillagers;
import forestry.arboriculture.villagers.ArboricultureVillagers;

import org.jetbrains.annotations.Nullable;

public class ForestryPoiTypeTagProvider extends PoiTypeTagsProvider {
	public ForestryPoiTypeTagProvider(DataGenerator generator, @Nullable ExistingFileHelper helper) {
		super(generator, ForestryConstants.MOD_ID, helper);
	}

	@Override
	protected void addTags() {
		tag(PoiTypeTags.ACQUIRABLE_JOB_SITE).add(ArboricultureVillagers.POI_TREE_CHEST.get(), ApicultureVillagers.POI_ESCRITOIRE.get());
	}
}

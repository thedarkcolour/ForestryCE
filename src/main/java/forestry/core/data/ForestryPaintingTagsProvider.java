package forestry.core.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.tags.PaintingVariantTags;
import net.minecraft.world.entity.decoration.PaintingVariant;

import forestry.core.features.CorePaintings;

import thedarkcolour.modkit.data.MKTagsProvider;

public class ForestryPaintingTagsProvider {
	public static void addTags(MKTagsProvider<PaintingVariant> tags, HolderLookup.Provider lookup) {
		tags.tag(PaintingVariantTags.PLACEABLE)
				.add(CorePaintings.MOUSETREE)
				.add(CorePaintings.WASPHOL);
	}
}

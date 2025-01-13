package forestry.core.data;

import forestry.apiculture.features.ApicultureItems;

import thedarkcolour.modkit.data.MKItemModelProvider;

public class ForestryItemModels {
	public static void addModels(MKItemModelProvider models) {
		models.generic2d(ApicultureItems.HONEY_DROP);
		models.generic2d(ApicultureItems.HONEYDEW);
		models.generic2d(ApicultureItems.HONEY_POT);
		models.generic2d(ApicultureItems.HONEYED_SLICE);
		models.generic2d(ApicultureItems.EXPERIENCE_DROP);
	}
}

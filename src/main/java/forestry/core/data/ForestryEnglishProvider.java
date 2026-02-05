package forestry.core.data;

import forestry.api.ForestryConstants;
import forestry.core.features.CoreItems;

import thedarkcolour.modkit.data.MKEnglishProvider;

public class ForestryEnglishProvider {
	// todo flesh this out more in 1.21 and change IDs of everything over for autogen lang
	public static void addTranslations(MKEnglishProvider lang) {
		// fertility
			lang.add("allele." + ForestryConstants.MOD_ID + ".fertility." + 0 + "i", "Infertile");
			lang.add("allele." + ForestryConstants.MOD_ID + ".fertility." + 0 + "id", "Infertile");
		for (int i = 1; i <= 10; ++i) {
			lang.add("allele." + ForestryConstants.MOD_ID + ".fertility." + i + "i", String.valueOf(i));
			lang.add("allele." + ForestryConstants.MOD_ID + ".fertility." + i + "id", String.valueOf(i));
		}

		lang.add(CoreItems.BRONZE_PICKAXE.item(), "Survivalist's Pickaxe");
		lang.add(CoreItems.BRONZE_SHOVEL.item(), "Survivalist's Shovel");
		lang.add(CoreItems.BRONZE_AXE.item(), "Survivalist's Axe");
		lang.add(CoreItems.BRONZE_SWORD.item(), "Survivalist's Sword");
		lang.add(CoreItems.BRONZE_HOE.item(), "Survivalist's Hoe");
	}
}

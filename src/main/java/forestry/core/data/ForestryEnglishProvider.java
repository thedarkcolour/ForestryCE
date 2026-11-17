package forestry.core.data;

import forestry.api.ForestryConstants;
import forestry.core.content.decorative.BigCandleBlockType;
import forestry.core.content.decorative.JumboCandleBlockType;
import forestry.core.content.decorative.MetalPlatingBlockType;
import forestry.core.features.CoreBlocks;
import forestry.core.features.CoreItems;

import thedarkcolour.modkit.data.MKEnglishProvider;

public class ForestryEnglishProvider {
	// todo flesh this out more in 1.21 and change IDs of everything over for autogen lang
	public static void addTranslations(MKEnglishProvider lang) {
		// fertility - new allele-foundation key scheme: allele.forestry.fertility.<value> (value = String.valueOf(int))
		lang.add("allele." + ForestryConstants.MOD_ID + ".fertility.0", "Infertile");
		for (int i = 1; i <= 10; ++i) {
			lang.add("allele." + ForestryConstants.MOD_ID + ".fertility." + i, String.valueOf(i));
		}

		lang.add(CoreItems.SURVIVALISTS_PICKAXE.item(), "Survivalist's Pickaxe");
		lang.add(CoreItems.SURVIVALISTS_SHOVEL.item(), "Survivalist's Shovel");
		lang.add(CoreItems.SURVIVALISTS_AXE.item(), "Survivalist's Axe");
		lang.add(CoreItems.SURVIVALISTS_SWORD.item(), "Survivalist's Sword");
		lang.add(CoreItems.SURVIVALISTS_HOE.item(), "Survivalist's Hoe");

		// autogen would render the possessive in these as "Survivalists", so name them explicitly
		lang.add(CoreItems.BROKEN_SURVIVALISTS_PICKAXE.item(), "Broken Survivalist's Pickaxe");
		lang.add(CoreItems.BROKEN_SURVIVALISTS_SHOVEL.item(), "Broken Survivalist's Shovel");
		lang.add(CoreItems.BROKEN_SURVIVALISTS_AXE.item(), "Broken Survivalist's Axe");
		lang.add(CoreItems.BROKEN_SURVIVALISTS_SWORD.item(), "Broken Survivalist's Sword");
		lang.add(CoreItems.BROKEN_SURVIVALISTS_HOE.item(), "Broken Survivalist's Hoe");

		// Blocks and items that deliberately share one descriptionId across many registered variants
		// (farm materials, pollinated vs default leaves, letter sizes and states). Auto-generation
		// would otherwise pick a name from whichever variant it visits first, which is both wrong
		// (ex. "Farm Hatch Quartz Lines") and nondeterministic across datagen runs. Name them
		// explicitly. Content-jar keys are named here too, because base writes every key
		lang.add("block." + ForestryConstants.MOD_ID + ".farm_plain", "Farm Block");
		lang.add("block." + ForestryConstants.MOD_ID + ".farm_gearbox", "Farm Gearbox");
		lang.add("block." + ForestryConstants.MOD_ID + ".farm_hatch", "Farm Hatch");
		lang.add("block." + ForestryConstants.MOD_ID + ".farm_valve", "Farm Valve");
		lang.add("block." + ForestryConstants.MOD_ID + ".farm_control", "Farm Control");
		lang.add("block." + ForestryConstants.MOD_ID + ".leaves", "Leaves");
		lang.add("item." + ForestryConstants.MOD_ID + ".letter", "Letter");

		// These three families carry their type last in the id, so a generated name reads "Metal Plating Gold"
		// or "Jumbo Candle Red". Named explicitly, in the wording 1.20.1 shipped
		for (MetalPlatingBlockType type : MetalPlatingBlockType.values()) {
			// The six cast from an ingot are named after the metal, the sixteen dyed ones after the lacquer.
			// Deviation from 1.20.1: red read "Red Metal Lacquered Plating" and green and magenta carried a
			// trailing space. All three are typos and are dropped here
			String suffix = type.getDye() == null ? " Metal Plating" : " Lacquered Metal Plating";
			lang.add(CoreBlocks.METAL_PLATING.get(type).item(), titleCase(type.getSerializedName()) + suffix);
		}
		for (JumboCandleBlockType type : JumboCandleBlockType.values()) {
			lang.add(CoreBlocks.JUMBO_CANDLES.get(type).item(), candleName("Jumbo", type.getSerializedName()));
		}
		for (BigCandleBlockType type : BigCandleBlockType.values()) {
			lang.add(CoreBlocks.BIG_CANDLES.get(type).item(), candleName("Big", type.getSerializedName()));
		}

		// The denomination is part of the id, so a generated name reads "Stamp 1n"
		lang.add("item." + ForestryConstants.MOD_ID + ".stamp_1n", "Stamp (1n)");
		lang.add("item." + ForestryConstants.MOD_ID + ".stamp_2n", "Stamp (2n)");
		lang.add("item." + ForestryConstants.MOD_ID + ".stamp_5n", "Stamp (5n)");
		lang.add("item." + ForestryConstants.MOD_ID + ".stamp_10n", "Stamp (10n)");
		lang.add("item." + ForestryConstants.MOD_ID + ".stamp_20n", "Stamp (20n)");
		lang.add("item." + ForestryConstants.MOD_ID + ".stamp_50n", "Stamp (50n)");
		lang.add("item." + ForestryConstants.MOD_ID + ".stamp_100n", "Stamp (100n)");
	}

	/**
	 * Ex. {@code candleName("Big", "light_gray")} -> {@code "Big Light Gray Candle"}
	 *
	 * @param size The size the candle is named after, either Jumbo or Big
	 * @param type The candle's subtype, whose plain form is named after its size alone
	 * @return The English name of one jumbo or big candle
	 */
	private static String candleName(String size, String type) {
		if (type.equals("normal")) {
			return size + " Candle";
		}
		return size + " " + titleCase(type) + " Candle";
	}

	/**
	 * Ex. {@code "light_gray" -> "Light Gray"}
	 *
	 * @param name The serialized name of a subtype
	 * @return The name with underscores read as spaces and every word capitalized
	 */
	private static String titleCase(String name) {
		StringBuilder builder = new StringBuilder(name.length());

		for (String word : name.split("_")) {
			if (!builder.isEmpty()) {
				builder.append(' ');
			}
			builder.append(Character.toUpperCase(word.charAt(0))).append(word, 1, word.length());
		}
		return builder.toString();
	}
}

package forestry.api;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

import forestry.arboriculture.ForestryWoodType;

import org.jetbrains.annotations.ApiStatus;

public class ForestryTags {
	public static class Blocks {
		public static final TagKey<Block> MINEABLE_SCOOP = blockTag("scoop");
		public static final TagKey<Block> MINEABLE_GRAFTER = blockTag("grafter");

		// Blocks that can be used as farmland bases for multiblock farms
		public static final TagKey<Block> VALID_FARM_BASE = blockTag("valid_farm_base");

		public static final TagKey<Block> CHARCOAL_BLOCK = forgeTag("storage_blocks/charcoal");

		public static final TagKey<Block> STORAGE_BLOCKS_APATITE = forgeTag("storage_blocks/apatite");
		public static final TagKey<Block> STORAGE_BLOCKS_TIN = forgeTag("storage_blocks/tin");
		public static final TagKey<Block> STORAGE_BLOCKS_BRONZE = forgeTag("storage_blocks/bronze");

		public static final TagKey<Block> ORES_TIN = forgeTag("ores/tin");
		public static final TagKey<Block> ORES_APATITE = forgeTag("ores/apatite");

		public static final TagKey<Block> STORAGE_BLOCKS_RAW_TIN = forgeTag("storage_blocks/raw_tin");

		public static final TagKey<Block> LARCH_LOGS = ForestryWoodType.LARCH.blockTag;
		public static final TagKey<Block> TEAK_LOGS = ForestryWoodType.TEAK.blockTag;
		public static final TagKey<Block> ACACIA_DESERT_LOGS = ForestryWoodType.ACACIA_DESERT.blockTag;
		public static final TagKey<Block> LIME_LOGS = ForestryWoodType.LIME.blockTag;
		public static final TagKey<Block> CHESTNUT_LOGS = ForestryWoodType.CHESTNUT.blockTag;
		public static final TagKey<Block> WENGE_LOGS = ForestryWoodType.WENGE.blockTag;
		public static final TagKey<Block> BAOBAB_LOGS = ForestryWoodType.BAOBAB.blockTag;
		public static final TagKey<Block> SEQUOIA_LOGS = ForestryWoodType.SEQUOIA.blockTag;
		public static final TagKey<Block> KAPOK_LOGS = ForestryWoodType.KAPOK.blockTag;
		public static final TagKey<Block> EBONY_LOGS = ForestryWoodType.EBONY.blockTag;
		public static final TagKey<Block> MAHOGANY_LOGS = ForestryWoodType.MAHOGANY.blockTag;
		public static final TagKey<Block> BALSA_LOGS = ForestryWoodType.BALSA.blockTag;
		public static final TagKey<Block> WILLOW_LOGS = ForestryWoodType.WILLOW.blockTag;
		public static final TagKey<Block> WALNUT_LOGS = ForestryWoodType.WALNUT.blockTag;
		public static final TagKey<Block> GREENHEART_LOGS = ForestryWoodType.GREENHEART.blockTag;
		public static final TagKey<Block> MAHOE_LOGS = ForestryWoodType.MAHOE.blockTag;
		public static final TagKey<Block> POPLAR_LOGS = ForestryWoodType.POPLAR.blockTag;
		public static final TagKey<Block> PALM_LOGS = ForestryWoodType.PALM.blockTag;
		public static final TagKey<Block> PAPAYA_LOGS = ForestryWoodType.PAPAYA.blockTag;
		public static final TagKey<Block> PINE_LOGS = ForestryWoodType.PINE.blockTag;
		public static final TagKey<Block> PLUM_LOGS = ForestryWoodType.PLUM.blockTag;
		public static final TagKey<Block> MAPLE_LOGS = ForestryWoodType.MAPLE.blockTag;
		public static final TagKey<Block> CITRUS_LOGS = ForestryWoodType.CITRUS.blockTag;
		public static final TagKey<Block> GIGANTEUM_LOGS = ForestryWoodType.GIGANTEUM.blockTag;
		public static final TagKey<Block> IPE_LOGS = ForestryWoodType.IPE.blockTag;
		public static final TagKey<Block> PADAUK_LOGS = ForestryWoodType.PADAUK.blockTag;
		public static final TagKey<Block> COCOBOLO_LOGS = ForestryWoodType.COCOBOLO.blockTag;
		public static final TagKey<Block> ZEBRAWOOD_LOGS = ForestryWoodType.ZEBRAWOOD.blockTag;

		// Categories of flowers
		public static final TagKey<Block> VANILLA_FLOWERS = blockTag("flowers/vanilla");
		public static final TagKey<Block> NETHER_FLOWERS = blockTag("flowers/nether");
		public static final TagKey<Block> CACTI_FLOWERS = blockTag("flowers/cacti");
		public static final TagKey<Block> MUSHROOMS_FLOWERS = blockTag("flowers/mushrooms");
		public static final TagKey<Block> END_FLOWERS = blockTag("flowers/end");
		public static final TagKey<Block> JUNGLE_FLOWERS = blockTag("flowers/jungle");
		public static final TagKey<Block> SNOW_FLOWERS = blockTag("flowers/snow");
		public static final TagKey<Block> WHEAT_FLOWERS = blockTag("flowers/wheat");
		public static final TagKey<Block> GOURD_FLOWERS = blockTag("flowers/gourd");
		public static final TagKey<Block> CAVE_FLOWERS = blockTag("flowers/cave");
		public static final TagKey<Block> ANCIENT_FLOWERS = blockTag("flowers/ancient");
		public static final TagKey<Block> SEA_FLOWERS = blockTag("flowers/sea");
		public static final TagKey<Block> CORAL_FLOWERS = blockTag("flowers/coral");
		public static final TagKey<Block> SCULK_FLOWERS = blockTag("flowers/sculk");

		// Flowers that can grow around hives
		public static final TagKey<Block> PLANTABLE_FLOWERS = blockTag("flowers/plantable");
		// Valid grounds where flowers can be planted around hives
		public static final TagKey<Block> PLANTABLE_FLOWERS_GROUND = blockTag("flowers/plantable_ground");

		public static final TagKey<Block> MODEST_BEE_GROUND = blockTag("hive_grounds/modest");
		public static final TagKey<Block> ENDED_BEE_GROUND = blockTag("hive_grounds/ended");
		public static final TagKey<Block> WINTRY_BEE_GROUND = blockTag("hive_grounds/wintry");
		public static final TagKey<Block> LUSH_BEE_CEILING = blockTag("hive_grounds/lush");
		public static final TagKey<Block> CAVE_EXTRA_REPLACEABLES = blockTag("hive_grounds/cave_extra_replaceable");
		// Blocks where the Alveary Swarmer can spawn hives on top of
		public static final TagKey<Block> SWARM_BEE_GROUND = blockTag("hive_grounds/swarm");
		public static final TagKey<Block> NETHER_EXTRA_REPLACEABLES = blockTag("hive_grounds/nether_extra_replaceable");

		private static TagKey<Block> forgeTag(String name) {
			return BlockTags.create(new ResourceLocation("forge", name));
		}
	}

	public static class Items {
		public static final TagKey<Item> CHARCOAL_BLOCK = forgeTag("storage_blocks/charcoal");

		public static final TagKey<Item> VILLAGE_COMBS = itemTag("village_combs");
		public static final TagKey<Item> BEE_COMBS = itemTag("combs");
		public static final TagKey<Item> PROPOLIS = itemTag("propolis");
		public static final TagKey<Item> DROP_HONEY = itemTag("drop_honey");

		public static final TagKey<Item> INGOTS_BRONZE = forgeTag("ingots/bronze");
		public static final TagKey<Item> INGOTS_TIN = forgeTag("ingots/tin");

		public static final TagKey<Item> GEARS = forgeTag("gears");
		public static final TagKey<Item> GEARS_BRONZE = forgeTag("gears/bronze");
		public static final TagKey<Item> GEARS_COPPER = forgeTag("gears/copper");
		public static final TagKey<Item> GEARS_TIN = forgeTag("gears/tin");
		public static final TagKey<Item> GEARS_STONE = forgeTag("gears/stone");

		public static final TagKey<Item> DUSTS_ASH = forgeTag("dusts/ash");
		public static final TagKey<Item> SAWDUST = forgeTag("sawdust");

		public static final TagKey<Item> GEMS_APATITE = forgeTag("gems/apatite");

		public static final TagKey<Item> STORAGE_BLOCKS_APATITE = forgeTag("storage_blocks/apatite");
		public static final TagKey<Item> STORAGE_BLOCKS_TIN = forgeTag("storage_blocks/tin");
		public static final TagKey<Item> STORAGE_BLOCKS_BRONZE = forgeTag("storage_blocks/bronze");

		public static final TagKey<Item> ORES_TIN = forgeTag("ores/tin");
		public static final TagKey<Item> RAW_MATERIALS_TIN = forgeTag("raw_materials/tin");
		public static final TagKey<Item> ORES_APATITE = forgeTag("ores/apatite");

		public static final TagKey<Item> STORAGE_BLOCKS_RAW_TIN = forgeTag("storage_blocks/raw_tin");

		public static final TagKey<Item> LARCH_LOGS = ForestryWoodType.LARCH.itemTag;
		public static final TagKey<Item> TEAK_LOGS = ForestryWoodType.TEAK.itemTag;
		public static final TagKey<Item> ACACIA_DESERT_LOGS = ForestryWoodType.ACACIA_DESERT.itemTag;
		public static final TagKey<Item> LIME_LOGS = ForestryWoodType.LIME.itemTag;
		public static final TagKey<Item> CHESTNUT_LOGS = ForestryWoodType.CHESTNUT.itemTag;
		public static final TagKey<Item> WENGE_LOGS = ForestryWoodType.WENGE.itemTag;
		public static final TagKey<Item> BAOBAB_LOGS = ForestryWoodType.BAOBAB.itemTag;
		public static final TagKey<Item> SEQUOIA_LOGS = ForestryWoodType.SEQUOIA.itemTag;
		public static final TagKey<Item> KAPOK_LOGS = ForestryWoodType.KAPOK.itemTag;
		public static final TagKey<Item> EBONY_LOGS = ForestryWoodType.EBONY.itemTag;
		public static final TagKey<Item> MAHOGANY_LOGS = ForestryWoodType.MAHOGANY.itemTag;
		public static final TagKey<Item> BALSA_LOGS = ForestryWoodType.BALSA.itemTag;
		public static final TagKey<Item> WILLOW_LOGS = ForestryWoodType.WILLOW.itemTag;
		public static final TagKey<Item> WALNUT_LOGS = ForestryWoodType.WALNUT.itemTag;
		public static final TagKey<Item> GREENHEART_LOGS = ForestryWoodType.GREENHEART.itemTag;
		public static final TagKey<Item> MAHOE_LOGS = ForestryWoodType.MAHOE.itemTag;
		public static final TagKey<Item> POPLAR_LOGS = ForestryWoodType.POPLAR.itemTag;
		public static final TagKey<Item> PALM_LOGS = ForestryWoodType.PALM.itemTag;
		public static final TagKey<Item> PAPAYA_LOGS = ForestryWoodType.PAPAYA.itemTag;
		public static final TagKey<Item> PINE_LOGS = ForestryWoodType.PINE.itemTag;
		public static final TagKey<Item> PLUM_LOGS = ForestryWoodType.PLUM.itemTag;
		public static final TagKey<Item> MAPLE_LOGS = ForestryWoodType.MAPLE.itemTag;
		public static final TagKey<Item> CITRUS_LOGS = ForestryWoodType.CITRUS.itemTag;
		public static final TagKey<Item> GIGANTEUM_LOGS = ForestryWoodType.GIGANTEUM.itemTag;
		public static final TagKey<Item> IPE_LOGS = ForestryWoodType.IPE.itemTag;
		public static final TagKey<Item> PADAUK_LOGS = ForestryWoodType.PADAUK.itemTag;
		public static final TagKey<Item> COCOBOLO_LOGS = ForestryWoodType.COCOBOLO.itemTag;
		public static final TagKey<Item> ZEBRAWOOD_LOGS = ForestryWoodType.ZEBRAWOOD.itemTag;

		public static final TagKey<Item> STAMPS = itemTag("stamps");

		public static final TagKey<Item> SCOOPS = itemTag("scoops");

		public static final TagKey<Item> FORESTRY_FRUITS = itemTag("forestry_fruits");
		public static final TagKey<Item> FRUITS = forgeTag("fruits");
		public static final TagKey<Item> CHERRY = forgeTag("fruits/cherry");
		public static final TagKey<Item> WALNUT = forgeTag("fruits/walnut");
		public static final TagKey<Item> CHESTNUT = forgeTag("fruits/chestnut");
		public static final TagKey<Item> LEMON = forgeTag("fruits/lemon");
		public static final TagKey<Item> PLUM = forgeTag("fruits/plum");
		public static final TagKey<Item> DATE = forgeTag("fruits/date");
		public static final TagKey<Item> PAPAYA = forgeTag("fruits/papaya");

		public static final TagKey<Item> MINER_ALLOW = itemTag("backpack/allow/miner");
		public static final TagKey<Item> MINER_REJECT = itemTag("backpack/reject/miner");

		public static final TagKey<Item> DIGGER_ALLOW = itemTag("backpack/allow/digger");
		public static final TagKey<Item> DIGGER_REJECT = itemTag("backpack/reject/digger");

		public static final TagKey<Item> FORESTER_ALLOW = itemTag("backpack/allow/forester");
		public static final TagKey<Item> FORESTER_REJECT = itemTag("backpack/reject/forester");

		public static final TagKey<Item> ADVENTURER_ALLOW = itemTag("backpack/allow/adventurer");
		public static final TagKey<Item> ADVENTURER_REJECT = itemTag("backpack/reject/adventurer");

		public static final TagKey<Item> BUILDER_ALLOW = itemTag("backpack/allow/builder");
		public static final TagKey<Item> BUILDER_REJECT = itemTag("backpack/reject/builder");

		public static final TagKey<Item> HUNTER_ALLOW = itemTag("backpack/allow/hunter");
		public static final TagKey<Item> HUNTER_REJECT = itemTag("backpack/reject/hunter");

		// needed because forge doesn't have it and mods can't agree on a crafting table tag...
		// todo: remove in 1.21 when Neo merges the tags unification PR
		public static final TagKey<Item> CRAFTING_TABLES = itemTag("crafting_tables");

		public static final TagKey<Item> BEES = itemTag("bees");

		private static TagKey<Item> forgeTag(String name) {
			return ItemTags.create(new ResourceLocation("forge", name));
		}
	}

	public static class Biomes {
		// Do not check directly, use IClimateManager instead
		public static final TagKey<Biome> ARID_HUMIDITY = tag("humidity/arid");
		public static final TagKey<Biome> NORMAL_HUMIDITY = tag("humidity/normal");
		public static final TagKey<Biome> DAMP_HUMIDITY = tag("humidity/damp");

		// Do not check directly, use IClimateManager instead
		public static final TagKey<Biome> ICY_TEMPERATURE = tag("temperature/icy");
		public static final TagKey<Biome> COLD_TEMPERATURE = tag("temperature/cold");
		public static final TagKey<Biome> NORMAL_TEMPERATURE = tag("temperature/normal");
		public static final TagKey<Biome> WARM_TEMPERATURE = tag("temperature/warm");
		public static final TagKey<Biome> HOT_TEMPERATURE = tag("temperature/hot");
		public static final TagKey<Biome> HELLISH_TEMPERATURE = tag("temperature/hellish");

		public static final TagKey<Biome> SHATTERED_SAVANNA = tag("special/shattered_savanna");
		public static final TagKey<Biome> WARPED_FOREST = tag("special/warped_forest");
		public static final TagKey<Biome> DEEP_DARK = tag("special/deep_dark");

		private static TagKey<Biome> tag(String path) {
			return TagKey.create(Registries.BIOME, ForestryConstants.forestry(path));
		}
	}

	public static class Fluids {
		public static final TagKey<Fluid> HONEY = forgeTag("honey");

		private static TagKey<Fluid> forgeTag(String name) {
			return FluidTags.create(new ResourceLocation("forge", name));
		}
	}

	// These have to be outside of Blocks and Items classes so that ForestryWoodType doesn't cause a circular dependency
	@ApiStatus.Internal
	public static TagKey<Block> blockTag(String name) {
		return BlockTags.create(ForestryConstants.forestry(name));
	}

	@ApiStatus.Internal
	public static TagKey<Item> itemTag(String name) {
		return ItemTags.create(ForestryConstants.forestry(name));
	}
}

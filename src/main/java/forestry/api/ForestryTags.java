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

public class ForestryTags {
	public static class Blocks {
		public static final TagKey<Block> MINEABLE_SCOOP = tag("scoop");
		public static final TagKey<Block> MINEABLE_GRAFTER = tag("grafter");

		// Blocks that can be used as farmland bases for multiblock farms
		public static final TagKey<Block> VALID_FARM_BASE = tag("valid_farm_base");

		public static final TagKey<Block> CHARCOAL_BLOCK = forgeTag("storage_blocks/charcoal");

		public static final TagKey<Block> STORAGE_BLOCKS_APATITE = forgeTag("storage_blocks/apatite");
		public static final TagKey<Block> STORAGE_BLOCKS_TIN = forgeTag("storage_blocks/tin");
		public static final TagKey<Block> STORAGE_BLOCKS_BRONZE = forgeTag("storage_blocks/bronze");

		public static final TagKey<Block> ORES_TIN = forgeTag("ores/tin");
		public static final TagKey<Block> ORES_APATITE = forgeTag("ores/apatite");

		public static final TagKey<Block> STORAGE_BLOCKS_RAW_TIN = forgeTag("storage_blocks/raw_tin");

		public static final TagKey<Block> PALM_LOGS = tag("palm_logs");
		public static final TagKey<Block> PAPAYA_LOGS = tag("papaya_logs");

		// Categories of flowers
		public static final TagKey<Block> VANILLA_FLOWERS = tag("flowers/vanilla");
		public static final TagKey<Block> NETHER_FLOWERS = tag("flowers/nether");
		public static final TagKey<Block> CACTI_FLOWERS = tag("flowers/cacti");
		public static final TagKey<Block> MUSHROOMS_FLOWERS = tag("flowers/mushrooms");
		public static final TagKey<Block> END_FLOWERS = tag("flowers/end");
		public static final TagKey<Block> JUNGLE_FLOWERS = tag("flowers/jungle");
		public static final TagKey<Block> SNOW_FLOWERS = tag("flowers/snow");
		public static final TagKey<Block> WHEAT_FLOWERS = tag("flowers/wheat");
		public static final TagKey<Block> GOURD_FLOWERS = tag("flowers/gourd");

		// Flowers that can grow around hives
		public static final TagKey<Block> PLANTABLE_FLOWERS = tag("flowers/plantable");
		// Valid grounds where flowers can be planted around hives
		public static final TagKey<Block> PLANTABLE_FLOWERS_GROUND = tag("flowers/plantable_ground");

		public static final TagKey<Block> MODEST_BEE_GROUND = tag("hive_grounds/modest");
		public static final TagKey<Block> ENDED_BEE_GROUND = tag("hive_grounds/ended");
		public static final TagKey<Block> WINTRY_BEE_GROUND = tag("hive_grounds/wintry");
		// Blocks where the Alveary Swarmer can spawn hives on top of
		public static final TagKey<Block> SWARM_BEE_GROUND = tag("hive_grounds/swarm");

		private static TagKey<Block> tag(String name) {
			return BlockTags.create(ForestryConstants.forestry(name));
		}

		private static TagKey<Block> forgeTag(String name) {
			return BlockTags.create(new ResourceLocation("forge", name));
		}
	}

	public static class Items {
		public static final TagKey<Item> CHARCOAL_BLOCK = forgeTag("storage_blocks/charcoal");

		public static final TagKey<Item> VILLAGE_COMBS = tag("village_combs");
		public static final TagKey<Item> BEE_COMBS = tag("combs");
		public static final TagKey<Item> PROPOLIS = tag("propolis");
		public static final TagKey<Item> DROP_HONEY = tag("drop_honey");

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

		public static final TagKey<Item> STAMPS = tag("stamps");

		public static final TagKey<Item> SCOOPS = tag("scoops");

		public static final TagKey<Item> FORESTRY_FRUITS = tag("forestry_fruits");
		public static final TagKey<Item> FRUITS = forgeTag("fruits");
		public static final TagKey<Item> CHERRY = forgeTag("fruits/cherry");
		public static final TagKey<Item> WALNUT = forgeTag("fruits/walnut");
		public static final TagKey<Item> CHESTNUT = forgeTag("fruits/chestnut");
		public static final TagKey<Item> LEMON = forgeTag("fruits/lemon");
		public static final TagKey<Item> PLUM = forgeTag("fruits/plum");
		public static final TagKey<Item> DATE = forgeTag("fruits/date");
		public static final TagKey<Item> PAPAYA = forgeTag("fruits/papaya");

		public static final TagKey<Item> MINER_ALLOW = tag("backpack/allow/miner");
		public static final TagKey<Item> MINER_REJECT = tag("backpack/reject/miner");

		public static final TagKey<Item> DIGGER_ALLOW = tag("backpack/allow/digger");
		public static final TagKey<Item> DIGGER_REJECT = tag("backpack/reject/digger");

		public static final TagKey<Item> FORESTER_ALLOW = tag("backpack/allow/forester");
		public static final TagKey<Item> FORESTER_REJECT = tag("backpack/reject/forester");

		public static final TagKey<Item> ADVENTURER_ALLOW = tag("backpack/allow/adventurer");
		public static final TagKey<Item> ADVENTURER_REJECT = tag("backpack/reject/adventurer");

		public static final TagKey<Item> BUILDER_ALLOW = tag("backpack/allow/builder");
		public static final TagKey<Item> BUILDER_REJECT = tag("backpack/reject/builder");

		public static final TagKey<Item> HUNTER_ALLOW = tag("backpack/allow/hunter");
		public static final TagKey<Item> HUNTER_REJECT = tag("backpack/reject/hunter");

		// needed because forge doesn't have it and mods can't agree on a crafting table tag...
		// todo: remove in 1.21 when Neo merges the tags unification PR
		public static final TagKey<Item> CRAFTING_TABLES = tag("crafting_tables");

		public static final TagKey<Item> BEES = tag("bees");

		private static TagKey<Item> tag(String name) {
			return ItemTags.create(ForestryConstants.forestry(name));
		}

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
}

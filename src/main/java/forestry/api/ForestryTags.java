package forestry.api;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
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
		public static final TagKey<Block> ANCIENT_FLOWERS = blockTag("flowers/ancient");
		public static final TagKey<Block> CAVE_FLOWERS = blockTag("flowers/cave");
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
		public static final TagKey<Block> NETHER_EXTRA_REPLACEABLES = blockTag("hive_grounds/nether_extra_replaceable");
		// Blocks where the Alveary Swarmer can spawn hives on top of
		public static final TagKey<Block> SWARM_BEE_GROUND = blockTag("hive_grounds/swarm");

		private static TagKey<Block> forgeTag(String name) {
			return BlockTags.create(ResourceLocation.fromNamespaceAndPath("c", name));
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

		public static final TagKey<Item> STAMPS = itemTag("stamps");

		public static final TagKey<Item> SCOOPS = itemTag("scoops");
		public static final TagKey<Item> SOLDERING_IRONS = itemTag("soldering_irons");

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

		public static final TagKey<Item> BEES = itemTag("bees");

		private static TagKey<Item> forgeTag(String name) {
			return ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", name));
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

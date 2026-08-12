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
import org.jetbrains.annotations.ApiStatus;

public class ForestryTags {
	public static class Blocks {
		public static final TagKey<Block> MINEABLE_SCOOP = blockTag("scoop");
		public static final TagKey<Block> MINEABLE_GRAFTER = blockTag("grafter");
		public static final TagKey<Block> LEAVES = blockTag("leaves");

		// Blocks that can be used as farmland bases for multiblock farms
		public static final TagKey<Block> VALID_FARM_BASE = blockTag("valid_farm_base");

		// Saplings that grow into a genetic tree. Read by the Arboretum's farm logic, which lives in
		// the agriculture jar and cannot name an arboriculture block. Undefined without the
		// arboriculture jar, which vanilla resolves as empty, so the Arboretum then farms only
		// vanilla saplings
		public static final TagKey<Block> TREE_SAPLINGS = blockTag("tree_saplings");

		public static final TagKey<Block> CHARCOAL_BLOCK = commonTag("storage_blocks/charcoal");

		public static final TagKey<Block> STORAGE_BLOCKS_APATITE = commonTag("storage_blocks/apatite");
		public static final TagKey<Block> STORAGE_BLOCKS_TIN = commonTag("storage_blocks/tin");
		public static final TagKey<Block> STORAGE_BLOCKS_BRONZE = commonTag("storage_blocks/bronze");
		public static final TagKey<Block> STORAGE_BLOCKS_AMBER = commonTag("storage_blocks/amber");

		public static final TagKey<Block> ORES_TIN = commonTag("ores/tin");
		public static final TagKey<Block> ORES_APATITE = commonTag("ores/apatite");

		public static final TagKey<Block> STORAGE_BLOCKS_RAW_TIN = commonTag("storage_blocks/raw_tin");

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

		private static TagKey<Block> commonTag(String name) {
			return BlockTags.create(ResourceLocation.fromNamespaceAndPath("c", name));
		}
	}

	public static class Items {
		public static final TagKey<Item> CHARCOAL_BLOCK = commonTag("storage_blocks/charcoal");

		public static final TagKey<Item> VILLAGE_COMBS = itemTag("village_combs");
		public static final TagKey<Item> BEE_COMBS = itemTag("combs");
		public static final TagKey<Item> PROPOLIS = itemTag("propolis");
		public static final TagKey<Item> DROP_HONEY = itemTag("drop_honey");

		public static final TagKey<Item> INGOTS_BRONZE = commonTag("ingots/bronze");
		public static final TagKey<Item> INGOTS_TIN = commonTag("ingots/tin");

		public static final TagKey<Item> GEARS = commonTag("gears");
		public static final TagKey<Item> GEARS_BRONZE = commonTag("gears/bronze");
		public static final TagKey<Item> GEARS_COPPER = commonTag("gears/copper");
		public static final TagKey<Item> GEARS_TIN = commonTag("gears/tin");
		public static final TagKey<Item> GEARS_STONE = commonTag("gears/stone");

		public static final TagKey<Item> DUSTS_ASH = commonTag("dusts/ash");
		public static final TagKey<Item> SAWDUST = commonTag("sawdust");

		public static final TagKey<Item> GEMS_APATITE = commonTag("gems/apatite");
		public static final TagKey<Item> GEMS_AMBER = commonTag("gems/amber");

		public static final TagKey<Item> STORAGE_BLOCKS_APATITE = commonTag("storage_blocks/apatite");
		public static final TagKey<Item> STORAGE_BLOCKS_TIN = commonTag("storage_blocks/tin");
		public static final TagKey<Item> STORAGE_BLOCKS_BRONZE = commonTag("storage_blocks/bronze");
		public static final TagKey<Item> STORAGE_BLOCKS_AMBER = commonTag("storage_blocks/amber");

		public static final TagKey<Item> ORES_TIN = commonTag("ores/tin");
		public static final TagKey<Item> RAW_MATERIALS_TIN = commonTag("raw_materials/tin");
		public static final TagKey<Item> ORES_APATITE = commonTag("ores/apatite");

		public static final TagKey<Item> STORAGE_BLOCKS_RAW_TIN = commonTag("storage_blocks/raw_tin");

		public static final TagKey<Item> STAMPS = itemTag("stamps");

		public static final TagKey<Item> SCOOPS = itemTag("scoops");

		// A sample carrying a genome. Bees and butterflies each contribute their own, so the genetic
		// filter can name this instead of naming an item out of a jar that may not be installed
		public static final TagKey<Item> GENETIC_SAMPLES = itemTag("genetic_samples");

		public static final TagKey<Item> FORESTRY_FRUITS = itemTag("forestry_fruits");
		public static final TagKey<Item> GRASSES = commonTag("grasses");
		public static final TagKey<Item> FRUITS = commonTag("fruits");
		public static final TagKey<Item> CHERRY = commonTag("fruits/cherry");
		public static final TagKey<Item> WALNUT = commonTag("fruits/walnut");
		public static final TagKey<Item> CHESTNUT = commonTag("fruits/chestnut");
		public static final TagKey<Item> LEMON = commonTag("fruits/lemon");
		public static final TagKey<Item> PLUM = commonTag("fruits/plum");
		public static final TagKey<Item> DATE = commonTag("fruits/date");
		public static final TagKey<Item> PAPAYA = commonTag("fruits/papaya");
		public static final TagKey<Item> PEAR = commonTag("fruits/pear");
		public static final TagKey<Item> ORANGE = commonTag("fruits/orange");
		public static final TagKey<Item> FEIJOA = commonTag("fruits/feijoa");
		public static final TagKey<Item> COCONUT = commonTag("fruits/coconut");
		public static final TagKey<Item> OLIVE = commonTag("fruits/olive");

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

		private static TagKey<Item> commonTag(String name) {
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

	public static class Fluids {
		public static final TagKey<Fluid> HONEY = forgeTag("honey");

		private static TagKey<Fluid> forgeTag(String name) {
			return FluidTags.create(ResourceLocation.fromNamespaceAndPath("forge", name));
		}
	}

	@ApiStatus.Internal
	public static TagKey<Block> blockTag(String name) {
		return BlockTags.create(ForestryConstants.forestry(name));
	}

	@ApiStatus.Internal
	public static TagKey<Item> itemTag(String name) {
		return ItemTags.create(ForestryConstants.forestry(name));
	}
}

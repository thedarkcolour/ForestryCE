package forestry.core.data;

import com.google.common.collect.Streams;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import net.minecraft.core.HolderLookup;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import net.minecraftforge.common.Tags;

import forestry.api.ForestryTags;
import forestry.apiculture.features.ApicultureBlocks;
import forestry.arboriculture.ForestryWoodType;
import forestry.arboriculture.features.ArboricultureBlocks;
import forestry.arboriculture.features.CharcoalBlocks;
import forestry.core.blocks.EnumResourceType;
import forestry.core.features.CoreBlocks;
import forestry.energy.features.EnergyBlocks;
import forestry.factory.features.FactoryBlocks;
import forestry.farming.blocks.EnumFarmMaterial;
import forestry.farming.blocks.FarmBlock;
import forestry.farming.features.FarmingBlocks;
import forestry.mail.features.MailBlocks;
import forestry.modules.features.FeatureBlock;
import forestry.modules.features.FeatureBlockGroup;
import forestry.worktable.features.WorktableBlocks;

import thedarkcolour.modkit.data.MKTagsProvider;

public final class ForestryBlockTagsProvider {
	public static void addTags(MKTagsProvider<Block> tags, HolderLookup.Provider lookup) {
		tags.tag(ForestryTags.Blocks.MINEABLE_SCOOP).add(ApicultureBlocks.BEEHIVE.blockArray());
		tags.tag(ForestryTags.Blocks.MINEABLE_GRAFTER).addTag(BlockTags.LEAVES);

		for (EnumFarmMaterial material : EnumFarmMaterial.values()) {
			tags.tag(ForestryTags.Blocks.VALID_FARM_BASE).add(material.getBase());
		}
		tags.tag(ForestryTags.Blocks.VALID_FARM_BASE).add(Blocks.SMOOTH_STONE);


		tags.tag(BlockTags.MINEABLE_WITH_AXE)
				.add(CoreBlocks.NATURALIST_CHEST.blockArray())
				.add(CharcoalBlocks.LOG_PILE.block())
				.add(WorktableBlocks.WORKTABLE.block());

		tags.tag(BlockTags.MINEABLE_WITH_PICKAXE)
				.add(CoreBlocks.APATITE_ORE.block())
				.add(CoreBlocks.DEEPSLATE_APATITE_ORE.block())
				.add(CoreBlocks.TIN_ORE.block())
				.add(CoreBlocks.DEEPSLATE_TIN_ORE.block())
				.add(CoreBlocks.RAW_TIN_BLOCK.block())
				.add(CharcoalBlocks.CHARCOAL.block())
				.add(EnergyBlocks.ENGINES.blockArray());

		for (FarmBlock block : FarmingBlocks.FARM.getBlocks()) {
			tags.tag(BlockTags.MINEABLE_WITH_PICKAXE).add(block);
		}

		for (Block block : union(CoreBlocks.RESOURCE_STORAGE, FactoryBlocks.PLAIN, FactoryBlocks.TESR, MailBlocks.BASE)) {
			tags.tag(BlockTags.MINEABLE_WITH_PICKAXE).add(block);
		}

		tags.tag(BlockTags.MINEABLE_WITH_SHOVEL)
				.add(CoreBlocks.HUMUS.block())
				.add(CoreBlocks.BOG_EARTH.block())
				.add(CoreBlocks.PEAT.block());

		for (Block block : union(
				CoreBlocks.BASE,
				ApicultureBlocks.ALVEARY, ApicultureBlocks.BASE,
				ArboricultureBlocks.DOORS,
				ArboricultureBlocks.PLANKS, ArboricultureBlocks.PLANKS_FIREPROOF, ArboricultureBlocks.PLANKS_VANILLA_FIREPROOF,
				ArboricultureBlocks.LOGS, ArboricultureBlocks.LOGS_FIREPROOF, ArboricultureBlocks.LOGS_VANILLA_FIREPROOF,
				ArboricultureBlocks.STRIPPED_LOGS, ArboricultureBlocks.STRIPPED_WOOD, ArboricultureBlocks.STRIPPED_LOGS_FIREPROOF, ArboricultureBlocks.STRIPPED_WOOD_FIREPROOF,
				ArboricultureBlocks.FENCES, ArboricultureBlocks.FENCES_FIREPROOF, ArboricultureBlocks.FENCES_VANILLA_FIREPROOF)) {
			tags.tag(BlockTags.MINEABLE_WITH_AXE).add(block);
		}


		tags.tag(BlockTags.MINEABLE_WITH_PICKAXE)
				.addTag(ForestryTags.Blocks.ORES_TIN)
				.addTag(ForestryTags.Blocks.ORES_APATITE)
				.addTag(ForestryTags.Blocks.STORAGE_BLOCKS_RAW_TIN);
		tags.tag(BlockTags.NEEDS_STONE_TOOL)
				.addTag(ForestryTags.Blocks.ORES_TIN)
				.addTag(ForestryTags.Blocks.ORES_APATITE)
				.addTag(ForestryTags.Blocks.STORAGE_BLOCKS_RAW_TIN);

		tags.tag(ForestryTags.Blocks.CHARCOAL_BLOCK).add(CharcoalBlocks.CHARCOAL.block());
		tags.tag(Tags.Blocks.CHESTS).add(CoreBlocks.NATURALIST_CHEST.getBlocks().toArray(Block[]::new));
		tags.tag(BlockTags.PLANKS).add(ArboricultureBlocks.PLANKS.blockArray());

		for (ForestryWoodType woodType : ForestryWoodType.VALUES) {
			tags.tag(woodType.blockTag).add(Streams.concat(woodType.getFireproof(), woodType.getBurnables()).map(FeatureBlock::block).toArray(Block[]::new));
			tags.tag(BlockTags.LOGS).addTag(woodType.blockTag);
			tags.tag(BlockTags.LOGS_THAT_BURN).add(woodType.getBurnables().map(FeatureBlock::block).toArray(Block[]::new));
			tags.tag(BlockTags.OVERWORLD_NATURAL_LOGS).add(ArboricultureBlocks.LOGS.get(woodType).block());
		}

		tags.tag(BlockTags.STAIRS).add(ArboricultureBlocks.STAIRS.blockArray());
		tags.tag(BlockTags.WOODEN_STAIRS).add(ArboricultureBlocks.STAIRS.blockArray());
		tags.tag(BlockTags.FENCES).add(ArboricultureBlocks.FENCES.blockArray());
		tags.tag(BlockTags.WOODEN_FENCES).add(ArboricultureBlocks.FENCES.blockArray());
		tags.tag(Tags.Blocks.FENCES).add(ArboricultureBlocks.FENCES.blockArray());
		tags.tag(Tags.Blocks.FENCE_GATES).add(ArboricultureBlocks.FENCE_GATES.blockArray());
		tags.tag(Tags.Blocks.FENCE_GATES_WOODEN).add(ArboricultureBlocks.FENCE_GATES.blockArray());
		tags.tag(BlockTags.SLABS).add(ArboricultureBlocks.SLABS.blockArray());
		tags.tag(BlockTags.WOODEN_SLABS).add(ArboricultureBlocks.SLABS.blockArray());
		tags.tag(BlockTags.DOORS).add(ArboricultureBlocks.DOORS.blockArray());
		tags.tag(BlockTags.WOODEN_DOORS).add(ArboricultureBlocks.DOORS.blockArray());

		tags.tag(BlockTags.PLANKS).add(ArboricultureBlocks.PLANKS_FIREPROOF.blockArray());
		tags.tag(BlockTags.STAIRS).add(ArboricultureBlocks.STAIRS_FIREPROOF.blockArray());
		tags.tag(BlockTags.WOODEN_STAIRS).add(ArboricultureBlocks.STAIRS_FIREPROOF.blockArray());
		tags.tag(BlockTags.FENCES).add(ArboricultureBlocks.FENCES_FIREPROOF.blockArray());
		tags.tag(Tags.Blocks.FENCES).add(ArboricultureBlocks.FENCES_FIREPROOF.blockArray());
		tags.tag(BlockTags.WOODEN_FENCES).add(ArboricultureBlocks.FENCES_FIREPROOF.blockArray());
		tags.tag(Tags.Blocks.FENCE_GATES).add(ArboricultureBlocks.FENCE_GATES_FIREPROOF.blockArray());
		tags.tag(Tags.Blocks.FENCE_GATES_WOODEN).add(ArboricultureBlocks.FENCE_GATES_FIREPROOF.blockArray());
		tags.tag(BlockTags.SLABS).add(ArboricultureBlocks.SLABS_FIREPROOF.blockArray());
		tags.tag(BlockTags.WOODEN_SLABS).add(ArboricultureBlocks.SLABS_FIREPROOF.blockArray());

		tags.tag(BlockTags.PLANKS).add(ArboricultureBlocks.PLANKS_VANILLA_FIREPROOF.blockArray());
		tags.tag(BlockTags.LOGS).add(ArboricultureBlocks.LOGS_VANILLA_FIREPROOF.blockArray());
		tags.tag(BlockTags.STAIRS).add(ArboricultureBlocks.STAIRS_VANILLA_FIREPROOF.blockArray());
		tags.tag(BlockTags.WOODEN_STAIRS).add(ArboricultureBlocks.STAIRS_VANILLA_FIREPROOF.blockArray());
		tags.tag(BlockTags.FENCES).add(ArboricultureBlocks.FENCES_VANILLA_FIREPROOF.blockArray());
		tags.tag(Tags.Blocks.FENCES).add(ArboricultureBlocks.FENCES_VANILLA_FIREPROOF.blockArray());
		tags.tag(BlockTags.WOODEN_FENCES).add(ArboricultureBlocks.FENCES_VANILLA_FIREPROOF.blockArray());
		tags.tag(Tags.Blocks.FENCE_GATES).add(ArboricultureBlocks.FENCE_GATES_VANILLA_FIREPROOF.blockArray());
		tags.tag(Tags.Blocks.FENCE_GATES_WOODEN).add(ArboricultureBlocks.FENCE_GATES_VANILLA_FIREPROOF.blockArray());
		tags.tag(BlockTags.SLABS).add(ArboricultureBlocks.SLABS_VANILLA_FIREPROOF.blockArray());
		tags.tag(BlockTags.WOODEN_SLABS).add(ArboricultureBlocks.SLABS_VANILLA_FIREPROOF.blockArray());

		tags.tag(BlockTags.SAPLINGS).add(ArboricultureBlocks.SAPLING_GE.block());
		tags.tag(BlockTags.LEAVES).add(ArboricultureBlocks.LEAVES.block()).add(ArboricultureBlocks.LEAVES_DEFAULT_FRUIT.blockArray()).add(ArboricultureBlocks.LEAVES_DEFAULT.blockArray()).add(ArboricultureBlocks.LEAVES_DECORATIVE.blockArray());

		tags.tag(Tags.Blocks.ORES).addTags(ForestryTags.Blocks.ORES_TIN, ForestryTags.Blocks.ORES_APATITE);
		tags.tag(ForestryTags.Blocks.ORES_TIN).add(CoreBlocks.TIN_ORE.block(), CoreBlocks.DEEPSLATE_TIN_ORE.block());
		tags.tag(ForestryTags.Blocks.ORES_APATITE).add(CoreBlocks.APATITE_ORE.block(), CoreBlocks.DEEPSLATE_APATITE_ORE.block());
		tags.tag(ForestryTags.Blocks.STORAGE_BLOCKS_RAW_TIN).add(CoreBlocks.RAW_TIN_BLOCK.block());

		tags.tag(Tags.Blocks.STORAGE_BLOCKS).addTags(ForestryTags.Blocks.STORAGE_BLOCKS_APATITE, ForestryTags.Blocks.STORAGE_BLOCKS_BRONZE, ForestryTags.Blocks.STORAGE_BLOCKS_TIN);
		tags.tag(ForestryTags.Blocks.STORAGE_BLOCKS_APATITE).add(CoreBlocks.RESOURCE_STORAGE.get(EnumResourceType.APATITE).block());
		tags.tag(ForestryTags.Blocks.STORAGE_BLOCKS_TIN).add(CoreBlocks.RESOURCE_STORAGE.get(EnumResourceType.TIN).block());
		tags.tag(ForestryTags.Blocks.STORAGE_BLOCKS_BRONZE).add(CoreBlocks.RESOURCE_STORAGE.get(EnumResourceType.BRONZE).block());

		tags.tag(BlockTags.DIRT).add(CoreBlocks.HUMUS.block());

		tags.tag(ForestryTags.Blocks.VANILLA_FLOWERS).addTag(BlockTags.FLOWERS);
		tags.tag(ForestryTags.Blocks.NETHER_FLOWERS).add(Blocks.NETHER_WART, Blocks.WARPED_FUNGUS, Blocks.POTTED_WARPED_FUNGUS, Blocks.CRIMSON_FUNGUS, Blocks.POTTED_CRIMSON_FUNGUS, Blocks.CRIMSON_ROOTS, Blocks.POTTED_CRIMSON_ROOTS, Blocks.WARPED_ROOTS, Blocks.POTTED_WARPED_ROOTS);
		tags.tag(ForestryTags.Blocks.CACTI_FLOWERS).add(Blocks.CACTUS);
		// todo is there a mushroom tag in later versions? should i add the nether fungi to this tag?
		tags.tag(ForestryTags.Blocks.MUSHROOMS_FLOWERS).add(Blocks.RED_MUSHROOM, Blocks.POTTED_RED_MUSHROOM, Blocks.BROWN_MUSHROOM, Blocks.POTTED_BROWN_MUSHROOM);
		tags.tag(ForestryTags.Blocks.END_FLOWERS).add(Blocks.DRAGON_EGG, Blocks.CHORUS_PLANT, Blocks.CHORUS_FLOWER);
		tags.tag(ForestryTags.Blocks.JUNGLE_FLOWERS).add(Blocks.VINE, Blocks.CAVE_VINES, Blocks.CAVE_VINES_PLANT, Blocks.FERN, Blocks.LARGE_FERN, Blocks.POTTED_FERN);
		// todo what belongs in this tag?
		tags.tag(ForestryTags.Blocks.SNOW_FLOWERS).addTag(BlockTags.FLOWERS);
		tags.tag(ForestryTags.Blocks.WHEAT_FLOWERS).add(Blocks.WHEAT);
		tags.tag(ForestryTags.Blocks.GOURD_FLOWERS).add(Blocks.MELON_STEM, Blocks.ATTACHED_MELON_STEM, Blocks.PUMPKIN_STEM, Blocks.ATTACHED_PUMPKIN_STEM);
		tags.tag(ForestryTags.Blocks.CAVE_FLOWERS).add(Blocks.CAVE_VINES,Blocks.CAVE_VINES_PLANT,Blocks.SPORE_BLOSSOM);
		tags.tag(ForestryTags.Blocks.ANCIENT_FLOWERS).add(Blocks.TORCHFLOWER,Blocks.PITCHER_PLANT);
		tags.tag(ForestryTags.Blocks.SEA_FLOWERS).add(Blocks.KELP,Blocks.KELP_PLANT);
		tags.tag(ForestryTags.Blocks.CORAL_FLOWERS).addTags(BlockTags.CORALS).addTags(BlockTags.WALL_CORALS);
		tags.tag(ForestryTags.Blocks.SCULK_FLOWERS).add(Blocks.SCULK_SHRIEKER,Blocks.SCULK_CATALYST,Blocks.SCULK_SENSOR);

		tags.tag(ForestryTags.Blocks.PLANTABLE_FLOWERS)
				.addTag(BlockTags.FLOWERS)
				.add(Blocks.RED_MUSHROOM, Blocks.BROWN_MUSHROOM)
				.add(Blocks.FERN)
				.add(Blocks.CRIMSON_FUNGUS, Blocks.WARPED_FUNGUS, Blocks.WARPED_ROOTS, Blocks.CRIMSON_ROOTS)
				.add(Blocks.CAVE_VINES,Blocks.SPORE_BLOSSOM)
				.add(Blocks.TORCHFLOWER,Blocks.PITCHER_PLANT)
				.addTags(BlockTags.CORALS)
				.add(Blocks.KELP);

		tags.tag(ForestryTags.Blocks.PLANTABLE_FLOWERS_GROUND).add(Blocks.DIRT, Blocks.GRASS_BLOCK, Blocks.SNOW, Blocks.SAND, Blocks.SANDSTONE);

		//why is there no tag for cactus plantable, but there is for dead bush?
		tags.tag(ForestryTags.Blocks.MODEST_BEE_GROUND).addTag(BlockTags.SAND).addTag(BlockTags.TERRACOTTA);
		tags.tag(ForestryTags.Blocks.WINTRY_BEE_GROUND).addTag(BlockTags.DIRT).addTag(BlockTags.SNOW);
		tags.tag(ForestryTags.Blocks.LUSH_BEE_CEILING).add(Blocks.MOSS_BLOCK).add(Blocks.ROOTED_DIRT);
		tags.tag(ForestryTags.Blocks.CAVE_EXTRA_REPLACEABLES).add(Blocks.POINTED_DRIPSTONE).add(Blocks.CAVE_VINES).add(Blocks.CAVE_VINES_PLANT).add(Blocks.HANGING_ROOTS).add(Blocks.GLOW_LICHEN);
		tags.tag(ForestryTags.Blocks.NETHER_EXTRA_REPLACEABLES).add(Blocks.WEEPING_VINES,Blocks.WEEPING_VINES_PLANT,Blocks.TWISTING_VINES,Blocks.TWISTING_VINES_PLANT);
	}

	private static Collection<Block> union(FeatureBlockGroup<?, ?>... features) {
		Set<Block> set = new LinkedHashSet<>();

		for (FeatureBlockGroup<?, ?> feature : features) {
			set.addAll(feature.getBlocks());
		}

		return set;
	}
}

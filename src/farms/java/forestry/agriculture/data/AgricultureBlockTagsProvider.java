package forestry.agriculture.data;

import forestry.agriculture.features.FarmingBlocks;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import thedarkcolour.modkit.data.MKTagsProvider;

import forestry.agriculture.multifarm.blocks.MultifarmMaterialType;
import forestry.agriculture.multifarm.blocks.MultifarmBlock;
import forestry.api.ForestryTags;

/**
 * Generates the block tag entries the farms jar contributes. The game merges a tag across every pack
 * that names it, so core ships the rest of {@code mineable/pickaxe} from its own file. The farm base
 * tag is read by the farm multiblock alone, so it ships whole from here.
 */
public class AgricultureBlockTagsProvider {
	public static void addTags(MKTagsProvider<Block> tags) {
		for (MultifarmMaterialType material : MultifarmMaterialType.values()) {
			tags.tag(ForestryTags.Blocks.VALID_FARM_BASE).add(material.getBase());
		}
		tags.tag(ForestryTags.Blocks.VALID_FARM_BASE).add(Blocks.SMOOTH_STONE);

		for (MultifarmBlock block : FarmingBlocks.FARM.getBlocks()) {
			tags.tag(BlockTags.MINEABLE_WITH_PICKAXE).add(block);
		}
	}
}

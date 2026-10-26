package forestry.agriculture.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;

import forestry.agriculture.features.FarmingBlocks;

/**
 * Generates the block drop loot tables for the farms jar. Every farm block and every planter drops
 * itself.
 */
public class AgricultureBlockLootTables extends BlockLootSubProvider {
	protected AgricultureBlockLootTables(HolderLookup.Provider registries) {
		super(Set.of(), FeatureFlags.DEFAULT_FLAGS, registries);
	}

	@Override
	protected void generate() {
		for (Block block : getKnownBlocks()) {
			dropSelf(block);
		}
	}

	@Override
	protected List<Block> getKnownBlocks() {
		List<Block> blocks = new ArrayList<>(FarmingBlocks.FARM.getBlocks());
		blocks.addAll(List.of(FarmingBlocks.MANAGED_MINIFARM.blockArray()));
		blocks.addAll(List.of(FarmingBlocks.MANUAL_MINIFARM.blockArray()));
		return blocks;
	}
}

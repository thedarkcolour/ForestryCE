package forestry.apiculture;

import forestry.api.ForestryTags;
import forestry.api.apiculture.IFlowerType;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

import java.util.*;

public class FlowerType implements IFlowerType {
	private final TagKey<Block> acceptableFlowers;
	private final boolean dominant;

	public FlowerType(TagKey<Block> acceptableFlowers, boolean dominant) {
		this.acceptableFlowers = acceptableFlowers;
		this.dominant = dominant;
	}

	@Override
	public boolean isAcceptableFlower(Level level, BlockPos pos) {
		// for debugging purposes
		//level.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, pos, Block.getId(Blocks.REDSTONE_BLOCK.defaultBlockState()));
		return level.getBlockState(pos).is(this.acceptableFlowers);
	}

	@Override
	public boolean plantRandomFlower(Level level, BlockPos pos, List<BlockState> nearbyFlowers) {
		if (level.hasChunkAt(pos) && isPlantablePosition(level, pos)) {
			// nearbyFlowers can contain duplicate flowers, but we don't want biased flower selection
			ObjectArrayList<BlockState> uniqueNearbyFlowers = new ObjectArrayList<>(new HashSet<>(nearbyFlowers));
			Util.shuffle(uniqueNearbyFlowers, level.random);

			for (BlockState state : uniqueNearbyFlowers) {
				if (state.is(ForestryTags.Blocks.PLANTABLE_FLOWERS) && state.canSurvive(level, pos)) {
					if (state.hasProperty(DoublePlantBlock.HALF)) {
						BlockPos topPos = pos.above();

						if (level.isEmptyBlock(topPos)) {
							return level.setBlockAndUpdate(pos, state.setValue(DoublePlantBlock.HALF, DoubleBlockHalf.LOWER))
								&& level.setBlockAndUpdate(topPos, state.setValue(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER));
						}
					} else {
						return level.setBlockAndUpdate(pos, state);
					}
				}
			}
		}
		return false;
	}

	public boolean isPlantablePosition(Level level, BlockPos pos) {
		return level.isEmptyBlock(pos);
	}

	@Override
	public boolean isDominant() {
		return this.dominant;
	}

	public TagKey<Block> getAcceptableFlowers() {
		return this.acceptableFlowers;
	}
}

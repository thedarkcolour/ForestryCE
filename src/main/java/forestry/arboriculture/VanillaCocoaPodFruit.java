package forestry.arboriculture;

import forestry.api.IForestryApi;
import forestry.api.arboriculture.genetics.IPodFruit;
import forestry.api.core.IProduct;
import forestry.api.genetics.IGenome;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class VanillaCocoaPodFruit extends Fruit implements IPodFruit {
	public VanillaCocoaPodFruit(List<IProduct> products) {
		super(false, 2, products);
	}

	@Override
	public boolean canSurviveOn(BlockState state) {
		return state.is(BlockTags.JUNGLE_LOGS) || state.is(IForestryApi.INSTANCE.getTreeManager().getWoodAccess().getLogBlockTag(VanillaWoodType.JUNGLE, true));
	}

	@Override
	public boolean tryPlace(LevelAccessor level, BlockPos pos, IGenome genome) {
		Direction facing = PodFruit.getValidPodFacing(level, pos, this);
		if (facing == null) {
			return false;
		}

		BlockState state = Blocks.COCOA.defaultBlockState().setValue(CocoaBlock.FACING, facing);

		return level.setBlock(pos, state, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
	}
}

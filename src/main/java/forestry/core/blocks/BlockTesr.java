package forestry.core.blocks;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;

public class BlockTesr<P extends IBlockType> extends BlockBase<P> {
	public BlockTesr(P blockType) {
		super(blockType, Block.Properties.of().sound(SoundType.WOOD).noOcclusion());
	}

	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.ENTITYBLOCK_ANIMATED;
	}
}

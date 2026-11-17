package forestry.core.platform.block;

import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;

public class SpecialRendererBlock<P extends IBlockType> extends BaseBlock<P> {
	public SpecialRendererBlock(P blockType, Properties properties) {
		super(properties.noOcclusion(), blockType);
	}

	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.ENTITYBLOCK_ANIMATED;
	}
}

package forestry.factory.blocks;

import forestry.core.blocks.BlockBase;
import net.minecraft.world.level.block.Block;

public class BlockFactoryTESR extends BlockBase<BlockTypeFactoryTesr> {
	public BlockFactoryTESR(BlockTypeFactoryTesr type) {
		super(type, Block.Properties.of());
	}
}

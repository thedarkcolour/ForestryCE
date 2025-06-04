package forestry.factory.blocks;

import forestry.core.blocks.BlockBase;
import net.minecraft.world.level.block.Block;

public class BlockFactoryPlain extends BlockBase<BlockTypeFactoryPlain> {
	public BlockFactoryPlain(BlockTypeFactoryPlain type) {
		super(type, Block.Properties.of());
	}
}

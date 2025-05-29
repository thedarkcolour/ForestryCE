package forestry.core.blocks;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;

public class BlockCore extends BlockBase<BlockTypeCoreTesr> {
	public BlockCore(BlockTypeCoreTesr blockType) {
		super(blockType, Block.Properties.of().sound(SoundType.WOOD).noOcclusion());
	}
}

package forestry.core.blocks;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;

public class BlockResourceStorage extends Block {
	public BlockResourceStorage() {
		super(Block.Properties.of().strength(3f, 5f).sound(SoundType.METAL));
	}
}

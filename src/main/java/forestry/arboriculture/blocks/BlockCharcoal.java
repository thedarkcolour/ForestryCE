package forestry.arboriculture.blocks;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;

public class BlockCharcoal extends Block {
	public BlockCharcoal() {
		super(Block.Properties.of().strength(5.0f, 10.0f).sound(SoundType.STONE));
	}
}

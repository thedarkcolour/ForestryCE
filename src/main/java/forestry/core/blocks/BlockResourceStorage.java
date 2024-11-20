package forestry.core.blocks;

import net.minecraft.world.level.block.Block;

public class BlockResourceStorage extends Block {
	private final EnumResourceType type;

	public BlockResourceStorage(EnumResourceType type) {
		super(Block.Properties.of().strength(3f, 5f));
		this.type = type;
	}

	public EnumResourceType getType() {
		return this.type;
	}
}

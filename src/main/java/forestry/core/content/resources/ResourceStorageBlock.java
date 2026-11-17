package forestry.core.content.resources;

import net.minecraft.world.level.block.Block;

// todo remove
public class ResourceStorageBlock extends Block {
	private final ResourceStorageBlockType type;

	public ResourceStorageBlock(ResourceStorageBlockType type) {
		super(Block.Properties.of().strength(3f, 5f));
		this.type = type;
	}

	public ResourceStorageBlockType getType() {
		return this.type;
	}
}

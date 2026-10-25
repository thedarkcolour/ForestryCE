package forestry.core.platform.block;

import net.minecraft.world.level.block.state.BlockBehaviour;

public class CoreMachineBlock extends BaseBlock<CoreMachineBlockType> {
	public CoreMachineBlock(CoreMachineBlockType blockType, BlockBehaviour.Properties properties) {
		super(properties, blockType);
	}
}

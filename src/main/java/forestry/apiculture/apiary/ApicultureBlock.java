package forestry.apiculture.apiary;

import forestry.core.platform.block.BaseBlock;
import net.minecraft.world.level.block.SoundType;

public class ApicultureBlock extends BaseBlock<ApicultureBlockType> {
	public ApicultureBlock(ApicultureBlockType type) {
		super(Properties.of().sound(SoundType.WOOD), type);
	}
}

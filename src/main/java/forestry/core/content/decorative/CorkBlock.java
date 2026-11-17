package forestry.core.content.decorative;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class CorkBlock extends BurnableBlock {
	public CorkBlock(Properties properties) {
		super(properties);
	}

	@Override
	public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, float distance) {
		entity.causeFallDamage(distance, 0.2f, level.damageSources().fall());
	}
}

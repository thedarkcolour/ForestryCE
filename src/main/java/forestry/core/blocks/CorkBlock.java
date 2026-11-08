package forestry.core.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;

public class CorkBlock extends BlockBurnable {
	public CorkBlock() {
		super(Properties.copy(Blocks.OAK_PLANKS)
			.sound(SoundType.CHERRY_WOOD)
			.strength(2.0F, 3.0F)
			.ignitedByLava());
	}

	public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, float distance) {
		entity.causeFallDamage(distance, 0.2F, level.damageSources().fall());
	}
}

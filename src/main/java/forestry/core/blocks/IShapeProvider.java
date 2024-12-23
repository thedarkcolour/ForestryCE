package forestry.core.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

@FunctionalInterface
public interface IShapeProvider {
	VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context);
}

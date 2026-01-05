package forestry.core.blocks;

import forestry.core.tiles.IForestryTicker;
import forestry.core.tiles.TileForestry;
import net.minecraft.core.BlockPos;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public interface IMachineProperties<T extends TileForestry> extends StringRepresentable, IShapeProvider {
	BlockEntityType<? extends T> getTeType();

	@Nullable
	BlockEntity createTileEntity(BlockPos pos, BlockState state);

	@Nullable
	IForestryTicker<? extends T> getClientTicker();

	@Nullable
	IForestryTicker<? extends T> getServerTicker();

	void setBlock(Block block);

	@Nullable
	Block getBlock();
}

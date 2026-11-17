package forestry.core.platform.block;

import forestry.core.platform.tile.IForestryTicker;
import forestry.core.platform.tile.ForestryBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public interface IMachineProperties<T extends ForestryBlockEntity> extends StringRepresentable, IShapeProvider {
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

	TankLayout getTankLayout();

	// How many fluid tanks a machine's model shows, and which slots they fill
	enum TankLayout {
		NONE,
		RESOURCE,
		PRODUCT,
		BOTH
	}
}

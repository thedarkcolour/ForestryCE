package forestry.agriculture.multifarm.tiles;

import forestry.api.core.multiblock.IFarmComponent;
import forestry.core.platform.inventory.AdjacentInventoryCache;
import forestry.core.platform.tile.AdjacentTileCache;
import forestry.core.platform.util.InventoryUtil;
import forestry.agriculture.features.MultifarmBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import net.neoforged.neoforge.items.wrapper.SidedInvWrapper;

import javax.annotation.Nullable;

public class MultifarmHatchBlockEntity extends MultifarmBlockEntity implements WorldlyContainer, IFarmComponent.Active {
	private final AdjacentTileCache tileCache;
	private final AdjacentInventoryCache inventoryCache;

	public MultifarmHatchBlockEntity(BlockPos pos, BlockState state) {
		super(MultifarmBlockEntities.HATCH.tileType(), pos, state);
		this.tileCache = new AdjacentTileCache(this);
		this.inventoryCache = new AdjacentInventoryCache(this, this.tileCache, tile -> !(tile instanceof MultifarmBlockEntity) && tile.getBlockPos().getY() < getBlockPos().getY());
	}

	@Override
	public boolean allowsAutomation() {
		return true;
	}

	@Override
	public void updateServer(int tickCount) {
		if (tickCount % 40 == 0) {
			Container productInventory = getMultiblockLogic().getController().getFarmInventory().getProductInventory();
			IItemHandler productItemHandler = new InvWrapper(productInventory);

			InventoryUtil.moveItemStack(productItemHandler, this.inventoryCache.getAdjacentInventories());
		}
	}

	@Override
	public void updateClient(int tickCount) {

	}

	@Nullable
	public IItemHandler getItemHandler(@Nullable Direction facing) {
		if (facing != null) {
			return new SidedInvWrapper(this, facing);
		}
		return new InvWrapper(this);
	}
}

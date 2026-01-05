package forestry.farming.tiles;

import forestry.api.multiblock.IFarmComponent;
import forestry.core.inventory.AdjacentInventoryCache;
import forestry.core.tiles.AdjacentTileCache;
import forestry.core.utils.InventoryUtil;
import forestry.farming.features.FarmingTiles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.items.wrapper.SidedInvWrapper;

import javax.annotation.Nullable;

public class TileFarmHatch extends TileFarm implements WorldlyContainer, IFarmComponent.Active {

	private final AdjacentTileCache tileCache;
	private final AdjacentInventoryCache inventoryCache;

	public TileFarmHatch(BlockPos pos, BlockState state) {
		super(FarmingTiles.HATCH.tileType(), pos, state);
		this.tileCache = new AdjacentTileCache(this);
		this.inventoryCache = new AdjacentInventoryCache(this, this.tileCache, tile -> !(tile instanceof TileFarm) && tile.getBlockPos().getY() < getBlockPos().getY());
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

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction facing) {
		if (capability == ForgeCapabilities.ITEM_HANDLER) {
			SidedInvWrapper sidedInvWrapper = new SidedInvWrapper(this, facing);
			return LazyOptional.of(() -> sidedInvWrapper).cast();
		}
		return super.getCapability(capability, facing);
	}
}

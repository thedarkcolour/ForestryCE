package forestry.sorting.tiles;

import forestry.api.genetics.capability.IIndividualHandlerItem;
import forestry.api.genetics.filter.FilterData;
import forestry.core.inventory.AdjacentInventoryCache;
import forestry.core.inventory.InventoryAdapterTile;
import forestry.core.network.IStreamableGui;
import forestry.core.tiles.TileForestry;
import forestry.core.tiles.TileUtil;
import forestry.core.utils.ItemStackUtil;
import forestry.sorting.FilterLogic;
import forestry.sorting.features.SortingTiles;
import forestry.sorting.gui.GeneticFilterMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import java.util.ArrayList;
import java.util.List;

public class TileGeneticFilter extends TileForestry implements IStreamableGui {
	private static final int TRANSFER_DELAY = 5;

	private final FilterLogic logic;
	private final AdjacentInventoryCache inventoryCache;

	public TileGeneticFilter(BlockPos pos, BlockState state) {
		super(SortingTiles.GENETIC_FILTER.tileType(), pos, state);
		this.inventoryCache = new AdjacentInventoryCache(this, getTileCache());
		this.logic = new FilterLogic(this, (logic1, level, player) -> sendToPlayers(level, player));
		setInternalInventory(new InventoryAdapterTile<>(this, 6, "Items"));
	}

	@Override
	public void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.saveAdditional(nbt, registries);

		nbt.put("Logic", this.logic.write(new CompoundTag(), registries));
	}

	@Override
	public void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);

		this.logic.read(nbt.getCompound("Logic"), registries);
	}

	@Override
	public void writeGuiData(RegistryFriendlyByteBuf buffer) {
		this.logic.writeGuiData(buffer);
	}

	@Override
	public void readGuiData(RegistryFriendlyByteBuf buffer) {
		this.logic.readGuiData(buffer);
	}

	private void sendToPlayers(ServerLevel server, Player filterChanger) {
		for (Player player : server.players()) {
			if (player != filterChanger && player.containerMenu instanceof GeneticFilterMenu) {
				if (((GeneticFilterMenu) filterChanger.containerMenu).hasSameTile((GeneticFilterMenu) player.containerMenu)) {
					((GeneticFilterMenu) player.containerMenu).setGuiNeedsUpdate(true);
				}
			}
		}
	}

	@Override
	public void serverTick(Level level, BlockPos pos, BlockState state) {
		if (updateOnInterval(TRANSFER_DELAY)) {
			for (Direction facing : Direction.VALUES) {
				ItemStack stack = getItem(facing.get3DDataValue());
				if (stack.isEmpty()) {
					continue;
				}
				ItemStack transferredStack = transferItem(stack, facing);
				int remaining = stack.getCount() - transferredStack.getCount();
				if (remaining > 0) {
					stack = stack.copy();
					stack.setCount(remaining);
					ItemStackUtil.dropItemStackAsEntity(stack.copy(), level, this.worldPosition.getX(), this.worldPosition.getY() + 0.5F, this.worldPosition.getZ());
				}
				setItem(facing.get3DDataValue(), ItemStack.EMPTY);
			}
		}
	}

	public boolean isConnected(Direction facing) {
		if (this.inventoryCache.getAdjacentInventory(facing) != null) {
			return true;
		}
		BlockEntity tileEntity = this.level.getBlockEntity(this.worldPosition.relative(facing));
		return TileUtil.getInventoryFromTile(tileEntity, facing.getOpposite()) != null;
	}

	private ItemStack transferItem(ItemStack stack, Direction facing) {
		IItemHandler itemHandler = this.inventoryCache.getAdjacentInventory(facing);
		if (itemHandler == null) {
			return ItemStack.EMPTY;
		}
		ItemStack transferredStack = ItemHandlerHelper.insertItemStacked(itemHandler, stack.copy(), true);
		if (transferredStack.getCount() == stack.getCount()) {
			return ItemStack.EMPTY;
		}
		transferredStack = ItemHandlerHelper.insertItemStacked(itemHandler, stack.copy(), false);
		if (transferredStack.isEmpty()) {
			return stack;
		}
		ItemStack copy = stack.copy();
		copy.setCount(stack.getCount() - transferredStack.getCount());
		return copy;
	}

	public List<Direction> getValidDirections(ItemStack stack, Direction from) {
		IIndividualHandlerItem handler = IIndividualHandlerItem.get(stack);

		if (handler == null) {
			return List.of();
		}

		FilterData filterData = new FilterData(handler.getIndividual(), handler.getStage());
		List<Direction> validFacings = new ArrayList<>();

		for (Direction facing : Direction.VALUES) {
			if (facing == from) {
				continue;
			}
			if (isValidFacing(facing, stack, filterData)) {
				validFacings.add(facing);
			}
		}

		return validFacings;
	}

	private boolean isValidFacing(Direction facing, ItemStack stack, FilterData filterData) {
		return this.inventoryCache.getAdjacentInventory(facing) != null && this.logic.isValid(facing, stack, filterData);
	}

	public FilterLogic getLogic() {
		return this.logic;
	}

	@Override
	public AbstractContainerMenu createMenu(int windowId, Inventory inv, Player player) {
		return new GeneticFilterMenu(windowId, player.getInventory(), this);
	}
}

package forestry.core.content.sorting.tiles;

import forestry.api.core.genetics.IIndividual;
import forestry.api.core.genetics.ILifeStage;
import forestry.api.core.genetics.filter.FilterData;
import forestry.core.platform.inventory.AdjacentInventoryCache;
import forestry.core.platform.inventory.BlockEntityInventoryAdapter;
import forestry.api.core.genetics.capability.IIndividualHandlerItem;
import forestry.core.platform.network.IStreamableGui;
import forestry.core.platform.tile.ForestryBlockEntity;
import forestry.core.platform.tile.TileUtil;
import forestry.core.platform.util.ItemStackUtil;
import forestry.core.content.sorting.FilterLogic;
import forestry.core.content.sorting.features.SortingBlockEntities;
import forestry.core.content.sorting.gui.GeneticFilterMenu;
import forestry.core.content.sorting.inventory.ItemHandlerFilter;
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

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class GeneticFilterBlockEntity extends ForestryBlockEntity implements IStreamableGui {
	private static final int TRANSFER_DELAY = 5;

	private final FilterLogic logic;
	private final AdjacentInventoryCache inventoryCache;

	public GeneticFilterBlockEntity(BlockPos pos, BlockState state) {
		super(SortingBlockEntities.GENETIC_FILTER.tileType(), pos, state);
		this.inventoryCache = new AdjacentInventoryCache(this, getTileCache());
		this.logic = new FilterLogic(this, (logic1, level, player) -> sendToPlayers(level, player));
		setInternalInventory(new BlockEntityInventoryAdapter<>(this, 6, "Items"));
	}

	@Override
	public void saveAdditional(CompoundTag data, HolderLookup.Provider registries) {
		super.saveAdditional(data, registries);

		data.put("Logic", this.logic.write(new CompoundTag(), registries));
	}

	@Override
	public void loadAdditional(CompoundTag data, HolderLookup.Provider registries) {
		super.loadAdditional(data, registries);

        this.logic.read(data.getCompound("Logic"), registries);
	}

	@Override
	public void writeGuiData(RegistryFriendlyByteBuf data) {
        this.logic.writeGuiData(data);
	}

	@Override
	public void readGuiData(RegistryFriendlyByteBuf data) {
        this.logic.readGuiData(data);
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

	private ItemStack transferItem(ItemStack itemStack, Direction facing) {
		IItemHandler itemHandler = this.inventoryCache.getAdjacentInventory(facing);
		if (itemHandler == null) {
			return ItemStack.EMPTY;
		}
		ItemStack transferredStack = ItemHandlerHelper.insertItemStacked(itemHandler, itemStack.copy(), true);
		if (transferredStack.getCount() == itemStack.getCount()) {
			return ItemStack.EMPTY;
		}
		transferredStack = ItemHandlerHelper.insertItemStacked(itemHandler, itemStack.copy(), false);
		if (transferredStack.isEmpty()) {
			return itemStack;
		}
		ItemStack copy = itemStack.copy();
		copy.setCount(itemStack.getCount() - transferredStack.getCount());
		return copy;
	}

	public List<Direction> getValidDirections(ItemStack stack, Direction from) {
		IIndividual individual = IIndividualHandlerItem.getIndividual(stack);
		ILifeStage stage = IIndividualHandlerItem.getLifeStage(stack);
		if (individual == null || stage == null) {
			return List.of();
		}

		FilterData filterData = new FilterData(individual, stage);
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

	private boolean isValidFacing(Direction facing, ItemStack itemStack, FilterData filterData) {
		return this.inventoryCache.getAdjacentInventory(facing) != null && this.logic.isValid(facing, itemStack, filterData);
	}

	public FilterLogic getLogic() {
		return this.logic;
	}

	@Nullable
	@Override
	public AbstractContainerMenu createMenu(int windowId, Inventory inv, Player player) {
		return new GeneticFilterMenu(windowId, player.getInventory(), this);
	}

	@Nullable
	public IItemHandler getItemHandler(@Nullable Direction facing) {
		if (facing != null) {
			return new ItemHandlerFilter(this, facing);
		}
		return null;
	}
}

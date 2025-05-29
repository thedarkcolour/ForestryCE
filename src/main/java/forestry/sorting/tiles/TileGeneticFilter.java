package forestry.sorting.tiles;

import forestry.api.ForestryCapabilities;
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
import forestry.sorting.gui.ContainerGeneticFilter;
import forestry.sorting.inventory.ItemHandlerFilter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nullable;
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
	public void saveAdditional(CompoundTag data) {
		super.saveAdditional(data);

		data.put("Logic", this.logic.write(new CompoundTag()));
	}

	@Override
	public void load(CompoundTag data) {
		super.load(data);

        this.logic.read(data.getCompound("Logic"));
	}

	@Override
	public void writeGuiData(RegistryFriendlyByteBuf data) {
        this.logic.writeGuiData(data);
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public void readGuiData(RegistryFriendlyByteBuf data) {
        this.logic.readGuiData(data);
	}

	private void sendToPlayers(ServerLevel server, Player filterChanger) {
		for (Player player : server.players()) {
			if (player != filterChanger && player.containerMenu instanceof ContainerGeneticFilter) {
				if (((ContainerGeneticFilter) filterChanger.containerMenu).hasSameTile((ContainerGeneticFilter) player.containerMenu)) {
					((ContainerGeneticFilter) player.containerMenu).setGuiNeedsUpdate(true);
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

	private boolean isValidFacing(Direction facing, ItemStack itemStack, FilterData filterData) {
		return this.inventoryCache.getAdjacentInventory(facing) != null && this.logic.isValid(facing, itemStack, filterData);
	}

	public FilterLogic getLogic() {
		return this.logic;
	}

	@Nullable
	@Override
	public AbstractContainerMenu createMenu(int windowId, Inventory inv, Player player) {
		return new ContainerGeneticFilter(windowId, player.getInventory(), this);
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction facing) {
		if (capability == ForgeCapabilities.ITEM_HANDLER && facing != null) {
			return LazyOptional.of(() -> new ItemHandlerFilter(this, facing)).cast();
		} else if (capability == ForestryCapabilities.FILTER_LOGIC) {
			return LazyOptional.of(() -> this.logic).cast();
		}
		return super.getCapability(capability, facing);
	}
}

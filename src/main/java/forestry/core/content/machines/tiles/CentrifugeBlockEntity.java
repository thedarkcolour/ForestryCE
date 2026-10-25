package forestry.core.content.machines.tiles;

import forestry.api.IForestryApi;
import forestry.api.core.circuits.ForestryCircuitSocketTypes;
import forestry.api.core.circuits.ICircuitBoard;
import forestry.api.core.ForestryError;
import forestry.api.core.IErrorLogic;
import forestry.api.core.machines.ICentrifugeRecipe;
import forestry.core.engine.circuits.ISocketable;
import forestry.core.platform.config.Constants;
import forestry.api.core.IInventoryAdapter;
import forestry.core.platform.inventory.InventoryAdapter;
import forestry.core.platform.tile.IItemStackDisplay;
import forestry.core.platform.tile.PoweredBlockEntity;
import forestry.core.platform.tile.TileUtil;
import forestry.core.platform.util.InventoryUtil;
import forestry.core.platform.util.RecipeUtils;
import forestry.core.content.machines.features.FactoryBlockEntities;
import forestry.core.content.machines.gui.CentrifugeMenu;
import forestry.core.content.machines.inventory.CentrifugeInventory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.Collection;

public class CentrifugeBlockEntity extends PoweredBlockEntity implements ISocketable, WorldlyContainer, IItemStackDisplay {
	private static final int STEPS_PER_RECIPE_TIME = 1;
	private static final int ENERGY_PER_WORK_CYCLE = 3200;
	private static final int ENERGY_PER_RECIPE_TIME = ENERGY_PER_WORK_CYCLE / 20;

	private final InventoryAdapter sockets = new InventoryAdapter(1, "sockets");
	private final ResultContainer craftPreviewInventory;
	private final ArrayDeque<ItemStack> pendingProducts = new ArrayDeque<>();
	@Nullable
	private ICentrifugeRecipe currentRecipe;

	public CentrifugeBlockEntity(BlockPos pos, BlockState state) {
		super(FactoryBlockEntities.CENTRIFUGE.tileType(), pos, state, 800, Constants.MACHINE_MAX_ENERGY);
		setInternalInventory(new CentrifugeInventory(this));
		this.craftPreviewInventory = new ResultContainer();
	}

	/* LOADING & SAVING */

	@Override
	public void saveAdditional(CompoundTag compound, HolderLookup.Provider registries) {
		super.saveAdditional(compound, registries);

		this.sockets.write(compound, registries);

		ItemStack[] offspring = this.pendingProducts.toArray(new ItemStack[0]);
		compound.put("PendingProducts", TileUtil.saveItemsToList(registries, offspring));
	}

	@Override
	public void loadAdditional(CompoundTag compound, HolderLookup.Provider registries) {
		super.loadAdditional(compound, registries);

		ListTag nbttaglist = compound.getList("PendingProducts", 10);
		for (int i = 0; i < nbttaglist.size(); i++) {
			CompoundTag CompoundNBT1 = nbttaglist.getCompound(i);
			this.pendingProducts.add(ItemStack.parse(registries, CompoundNBT1).orElse(ItemStack.EMPTY));
		}
		this.sockets.read(compound, registries);

		ItemStack chip = this.sockets.getItem(0);
		if (!chip.isEmpty()) {
			ICircuitBoard chipset = IForestryApi.INSTANCE.getCircuitManager().getCircuitBoard(chip);
			if (chipset != null) {
				chipset.onLoad(this);
			}
		}
	}

	@Override
	public void writeGuiData(RegistryFriendlyByteBuf data) {
		super.writeGuiData(data);
		this.sockets.writeData(data);
	}

	@Override
	public void readGuiData(RegistryFriendlyByteBuf data) {
		super.readGuiData(data);
		this.sockets.readData(data);
	}

	@Override
	public boolean workCycle() {
		if (!this.pendingProducts.isEmpty()) {
			tryAddPending();
			return this.pendingProducts.isEmpty();
		}

		if (this.currentRecipe == null) {
			return false;
		}

		// We are done, add products to queue
		Collection<ItemStack> products = this.currentRecipe.getProducts(level.random, this.outputMultiplier);
		this.pendingProducts.addAll(products);

		//Add Item to preview slot.
		ItemStack previewStack = getInternalInventory().getItem(CentrifugeInventory.SLOT_RESOURCE).copy();
		previewStack.setCount(1);
		this.craftPreviewInventory.setItem(0, previewStack);

		getInternalInventory().removeItem(CentrifugeInventory.SLOT_RESOURCE, 1);
		tryAddPending();
		return this.pendingProducts.isEmpty();
	}

	private void checkRecipe() {
		ItemStack resource = getItem(CentrifugeInventory.SLOT_RESOURCE);
		ICentrifugeRecipe matchingRecipe = RecipeUtils.getCentrifugeRecipe(getLevel().getRecipeManager(), resource);

		if (this.currentRecipe != matchingRecipe) {
			this.currentRecipe = matchingRecipe;
			if (this.currentRecipe != null) {
				int recipeTime = this.currentRecipe.getProcessingTime();
				setStepsPerWorkCycle(recipeTime * STEPS_PER_RECIPE_TIME);
				setEnergyPerWorkCycle(recipeTime * ENERGY_PER_RECIPE_TIME);
			}
		}
	}

	private boolean tryAddPending() {
		if (this.pendingProducts.isEmpty()) {
			return false;
		}

		ItemStack next = this.pendingProducts.peekFirst();

		boolean added = InventoryUtil.tryAddStack(this, next, CentrifugeInventory.SLOT_PRODUCT_1, CentrifugeInventory.SLOT_PRODUCT_COUNT, true);

		if (added) {
			this.pendingProducts.removeFirst();
			if (this.pendingProducts.isEmpty()) {
				this.craftPreviewInventory.setItem(0, ItemStack.EMPTY);
			}
		}

		getErrorLogic().setCondition(!added, ForestryError.NO_SPACE_INVENTORY);
		return added;
	}

	@Override
	public boolean hasResourcesMin(float percentage) {
		IInventoryAdapter inventory = getInternalInventory();
		if (inventory.getItem(CentrifugeInventory.SLOT_RESOURCE).isEmpty()) {
			return false;
		}

		return (float) inventory.getItem(CentrifugeInventory.SLOT_RESOURCE).getCount() / (float) inventory.getItem(CentrifugeInventory.SLOT_RESOURCE).getMaxStackSize() > percentage;
	}

	@Override
	public boolean hasWork() {
		if (!this.pendingProducts.isEmpty()) {
			return true;
		}
		checkRecipe();

		boolean hasResource = !getItem(CentrifugeInventory.SLOT_RESOURCE).isEmpty();

		IErrorLogic errorLogic = getErrorLogic();
		errorLogic.setCondition(!hasResource, ForestryError.NO_RESOURCE);

		return hasResource;
	}

	/* ISocketable */
	@Override
	public int getSocketCount() {
		return this.sockets.getContainerSize();
	}

	@Override
	public ItemStack getSocket(int slot) {
		return this.sockets.getItem(slot);
	}

	@Override
	public void setSocket(int slot, ItemStack stack) {

		if (!stack.isEmpty() && !IForestryApi.INSTANCE.getCircuitManager().isCircuitBoard(stack)) {
			return;
		}

		// Dispose correctly of old chipsets
		if (!this.sockets.getItem(slot).isEmpty()) {
			if (IForestryApi.INSTANCE.getCircuitManager().isCircuitBoard(this.sockets.getItem(slot))) {
				ICircuitBoard chipset = IForestryApi.INSTANCE.getCircuitManager().getCircuitBoard(this.sockets.getItem(slot));
				if (chipset != null) {
					chipset.onRemoval(this);
				}
			}
		}

		this.sockets.setItem(slot, stack);
		// sockets has no tile link of its own
		setChanged();
		if (stack.isEmpty()) {
			return;
		}

		ICircuitBoard chipset = IForestryApi.INSTANCE.getCircuitManager().getCircuitBoard(stack);
		if (chipset != null) {
			chipset.onInsertion(this);
		}
	}

	@Override
	public ResourceLocation getSocketType() {
		return ForestryCircuitSocketTypes.MACHINE;
	}

	@Override
	public AbstractContainerMenu createMenu(int windowId, Inventory inv, Player player) {
		return new CentrifugeMenu(windowId, player.getInventory(), this);
	}

	public Container getCraftPreviewInventory() {
		return this.craftPreviewInventory;
	}

	@Override
	public void handleItemStackForDisplay(ItemStack itemStack) {
		this.craftPreviewInventory.setItem(0, itemStack);
	}
}

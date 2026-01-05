package forestry.factory.tiles;

import forestry.api.IForestryApi;
import forestry.api.circuits.ForestryCircuitSocketTypes;
import forestry.api.circuits.ICircuitBoard;
import forestry.api.core.ForestryError;
import forestry.api.core.IErrorLogic;
import forestry.api.recipes.ICentrifugeRecipe;
import forestry.core.circuits.ISocketable;
import forestry.core.config.Constants;
import forestry.core.inventory.IInventoryAdapter;
import forestry.core.inventory.InventoryAdapter;
import forestry.core.tiles.IItemStackDisplay;
import forestry.core.tiles.TilePowered;
import forestry.core.utils.InventoryUtil;
import forestry.core.utils.RecipeUtils;
import forestry.factory.features.FactoryTiles;
import forestry.factory.gui.ContainerCentrifuge;
import forestry.factory.inventory.InventoryCentrifuge;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.Collection;

public class TileCentrifuge extends TilePowered implements ISocketable, WorldlyContainer, IItemStackDisplay {
	private static final int TICKS_PER_RECIPE_TIME = 1;
	private static final int ENERGY_PER_WORK_CYCLE = 3200;
	private static final int ENERGY_PER_RECIPE_TIME = ENERGY_PER_WORK_CYCLE / 20;

	private final InventoryAdapter sockets = new InventoryAdapter(1, "sockets");
	private final ResultContainer craftPreviewInventory;
	@Nullable
	private ICentrifugeRecipe currentRecipe;

	private final ArrayDeque<ItemStack> pendingProducts = new ArrayDeque<>();

	public TileCentrifuge(BlockPos pos, BlockState state) {
		super(FactoryTiles.CENTRIFUGE.tileType(), pos, state, 800, Constants.MACHINE_MAX_ENERGY);
		setInternalInventory(new InventoryCentrifuge(this));
        this.craftPreviewInventory = new ResultContainer();
	}

	/* LOADING & SAVING */

	@Override
	public void saveAdditional(CompoundTag compound) {
		super.saveAdditional(compound);

        this.sockets.write(compound);

		ListTag nbttaglist = new ListTag();
		ItemStack[] offspring = this.pendingProducts.toArray(new ItemStack[0]);
		for (int i = 0; i < offspring.length; i++) {
			if (offspring[i] != null) {
				CompoundTag products = new CompoundTag();
				products.putByte("Slot", (byte) i);
				offspring[i].save(products);
				nbttaglist.add(products);
			}
		}
		compound.put("PendingProducts", nbttaglist);
	}

	@Override
	public void load(CompoundTag compound) {
		super.load(compound);

		ListTag nbttaglist = compound.getList("PendingProducts", 10);
		for (int i = 0; i < nbttaglist.size(); i++) {
			CompoundTag CompoundNBT1 = nbttaglist.getCompound(i);
            this.pendingProducts.add(ItemStack.of(CompoundNBT1));
		}
        this.sockets.read(compound);

		ItemStack chip = this.sockets.getItem(0);
		if (!chip.isEmpty()) {
			ICircuitBoard chipset = IForestryApi.INSTANCE.getCircuitManager().getCircuitBoard(chip);
			if (chipset != null) {
				chipset.onLoad(this);
			}
		}
	}

	@Override
	public void writeGuiData(FriendlyByteBuf data) {
		super.writeGuiData(data);
        this.sockets.writeData(data);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void readGuiData(FriendlyByteBuf data) {
		super.readGuiData(data);
        this.sockets.readData(data);
	}

	@Override
	public boolean workCycle() {
		if (tryAddPending()) {
			return true;
		}

		if (!this.pendingProducts.isEmpty()) {
            this.craftPreviewInventory.setItem(0, ItemStack.EMPTY);
			return false;
		}

		if (this.currentRecipe == null) {
			return false;
		}

		// We are done, add products to queue
		Collection<ItemStack> products = this.currentRecipe.getProducts(this.level.random);
        this.pendingProducts.addAll(products);

		//Add Item to preview slot.
		ItemStack previewStack = getInternalInventory().getItem(InventoryCentrifuge.SLOT_RESOURCE).copy();
		previewStack.setCount(1);
        this.craftPreviewInventory.setItem(0, previewStack);

		getInternalInventory().removeItem(InventoryCentrifuge.SLOT_RESOURCE, 1);
		return true;
	}

	private void checkRecipe() {
		ItemStack resource = getItem(InventoryCentrifuge.SLOT_RESOURCE);
		ICentrifugeRecipe matchingRecipe = RecipeUtils.getCentrifugeRecipe(getLevel().getRecipeManager(), resource);

		if (this.currentRecipe != matchingRecipe) {
            this.currentRecipe = matchingRecipe;
			if (this.currentRecipe != null) {
				int recipeTime = this.currentRecipe.getProcessingTime();
				setTicksPerWorkCycle(recipeTime * TICKS_PER_RECIPE_TIME);
				setEnergyPerWorkCycle(recipeTime * ENERGY_PER_RECIPE_TIME);
			}
		}
	}

	private boolean tryAddPending() {
		if (this.pendingProducts.isEmpty()) {
			return false;
		}

		ItemStack next = this.pendingProducts.peekFirst();

		boolean added = InventoryUtil.tryAddStack(this, next, InventoryCentrifuge.SLOT_PRODUCT_1, InventoryCentrifuge.SLOT_PRODUCT_COUNT, true);

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
		if (inventory.getItem(InventoryCentrifuge.SLOT_RESOURCE).isEmpty()) {
			return false;
		}

		return (float) inventory.getItem(InventoryCentrifuge.SLOT_RESOURCE).getCount() / (float) inventory.getItem(InventoryCentrifuge.SLOT_RESOURCE).getMaxStackSize() > percentage;
	}

	@Override
	public boolean hasWork() {
		if (!this.pendingProducts.isEmpty()) {
			return true;
		}
		checkRecipe();

		boolean hasResource = !getItem(InventoryCentrifuge.SLOT_RESOURCE).isEmpty();

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
		return new ContainerCentrifuge(windowId, player.getInventory(), this);
	}

	public Container getCraftPreviewInventory() {
		return this.craftPreviewInventory;
	}

	@Override
	public void handleItemStackForDisplay(ItemStack itemStack) {
        this.craftPreviewInventory.setItem(0, itemStack);
	}
}

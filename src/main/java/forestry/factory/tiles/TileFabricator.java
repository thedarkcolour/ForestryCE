package forestry.factory.tiles;

import forestry.api.core.ForestryError;
import forestry.api.core.IErrorLogic;
import forestry.api.recipes.IFabricatorRecipe;
import forestry.core.fluids.FilteredTank;
import forestry.core.fluids.FluidRecipeFilter;
import forestry.core.fluids.TankManager;
import forestry.core.inventory.IInventoryAdapter;
import forestry.core.inventory.InventoryAdapter;
import forestry.core.inventory.InventoryAdapterTile;
import forestry.core.inventory.InventoryGhostCrafting;
import forestry.core.inventory.watchers.ISlotPickupWatcher;
import forestry.core.inventory.wrappers.InventoryMapper;
import forestry.core.items.definitions.ICraftingPlan;
import forestry.core.tiles.ILiquidTankTile;
import forestry.core.tiles.TilePowered;
import forestry.core.utils.InventoryUtil;
import forestry.core.utils.RecipeUtil;
import forestry.factory.features.FactoryTiles;
import forestry.factory.gui.FabricatorMenu;
import forestry.factory.inventory.InventoryFabricator;
import forestry.factory.recipes.FabricatorSmeltingRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import javax.annotation.Nullable;
import java.util.Optional;

public class TileFabricator extends TilePowered implements ISlotPickupWatcher, ILiquidTankTile, WorldlyContainer {
	private static final int MAX_HEAT = 5000;

	private final InventoryAdapterTile<?> craftingInventory;
	private final TankManager tankManager;
	private final FilteredTank moltenTank;
	private int heat = 0;
	private int meltingPoint = 0;

	public TileFabricator(BlockPos pos, BlockState state) {
		super(FactoryTiles.FABRICATOR.tileType(), pos, state, 1100, 3300);
		setEnergyPerWorkCycle(200);
		this.craftingInventory = new InventoryGhostCrafting<>(this, InventoryGhostCrafting.SLOT_CRAFTING_COUNT);
		setInternalInventory(new InventoryFabricator(this));

		this.moltenTank = new FilteredTank(8 * FluidType.BUCKET_VOLUME, false, true).setFilter(FluidRecipeFilter.FABRICATOR_SMELTING_OUTPUT);

		this.tankManager = new TankManager(this, this.moltenTank);
	}

	/* SAVING & LOADING */

	@Override
	public void saveAdditional(CompoundTag compound, HolderLookup.Provider registries) {
		super.saveAdditional(compound, registries);

		compound.putInt("Heat", this.heat);
		this.tankManager.write(compound, registries);
		this.craftingInventory.write(compound, registries);
	}

	@Override
	public void loadAdditional(CompoundTag compound, HolderLookup.Provider registries) {
		super.loadAdditional(compound, registries);

		this.heat = compound.getInt("Heat");
		this.tankManager.read(compound, registries);
		this.craftingInventory.read(compound, registries);
	}

	@Override
	public void writeData(RegistryFriendlyByteBuf buffer) {
		this.tankManager.writeData(buffer);
	}

	@Override
	public void readData(RegistryFriendlyByteBuf buffer) {
		this.tankManager.readData(buffer);
	}

	@Override
	public void serverTick(Level level, BlockPos pos, BlockState state) {
		super.serverTick(level, pos, state);

		if (!this.moltenTank.isFull()) {
			trySmelting();
		}

		if (!this.moltenTank.isEmpty()) {
			// Remove smelt if we have gone below melting point
			if (this.heat < getMeltingPoint() - 100) {
				this.moltenTank.drain(5, IFluidHandler.FluidAction.EXECUTE);
			}
		}

		if (this.heat > 2500) {
			this.heat -= 2;
		} else if (this.heat > 0) {
			this.heat--;
		}
	}

	private void trySmelting() {
		IInventoryAdapter inventory = getInternalInventory();

		ItemStack smeltResource = inventory.getItem(InventoryFabricator.SLOT_METAL);
		if (smeltResource.isEmpty()) {
			return;
		}

		RecipeHolder<FabricatorSmeltingRecipe> holder = RecipeUtil.getFabricatorMeltingRecipe(this.level.getRecipeManager(), smeltResource);
		if (holder == null) {
			return;
		}
		FabricatorSmeltingRecipe smelt = holder.value();
		if (smelt.meltingPoint() > this.heat) {
			return;
		}

		FluidStack smeltFluid = smelt.result();
		if (this.moltenTank.fillInternal(smeltFluid, IFluidHandler.FluidAction.SIMULATE) == smeltFluid.getAmount()) {
			this.removeItem(InventoryFabricator.SLOT_METAL, 1);
			this.moltenTank.fillInternal(smeltFluid, IFluidHandler.FluidAction.EXECUTE);
			this.meltingPoint = smelt.meltingPoint();
		}
	}

	@Override
	public boolean workCycle() {
		this.heat += 100;
		if (this.heat > MAX_HEAT) {
			this.heat = MAX_HEAT;
		}

		craftResult();

		return true;
	}

	@Nullable
	private IFabricatorRecipe getRecipe() {
		IInventoryAdapter inventory = getInternalInventory();
		ItemStack plan = inventory.getItem(InventoryFabricator.SLOT_PLAN);
		FluidStack liquid = this.moltenTank.getFluid();
		RecipeHolder<IFabricatorRecipe> recipe = RecipeUtil.getFabricatorRecipe(this.level.getRecipeManager(), this.level, liquid, plan, this.craftingInventory);
		if (!liquid.isEmpty() && recipe != null) {
			Optional<SizedFluidIngredient> requiredFluid = recipe.value().getRequiredFluid();
			if (requiredFluid.isPresent() && !requiredFluid.get().test(liquid)) {
				return null;
			}
		}
		return recipe.value();
	}

	public ItemStack getResult(@Nullable IFabricatorRecipe myRecipe) {
		if (myRecipe == null) {
			return ItemStack.EMPTY;
		}

		return myRecipe.getCraftingGridRecipe().getResultItem(this.level.registryAccess()).copy();
	}

	/* ISlotPickupWatcher */
	@Override
	public void onTake(int slotIndex, Player player) {
		if (slotIndex == InventoryFabricator.SLOT_RESULT) {
			removeItem(InventoryFabricator.SLOT_RESULT, 1);
		}
	}

	private void craftResult() {
		IFabricatorRecipe myRecipe = getRecipe();
		ItemStack craftResult = getResult(myRecipe);

		if (myRecipe != null && !craftResult.isEmpty() && getItem(InventoryFabricator.SLOT_RESULT).isEmpty()) {
			// Remove resources
			if (removeFromInventory(myRecipe, false)) {
				Optional<SizedFluidIngredient> liquid = myRecipe.getRequiredFluid();
				boolean consumedLiquid;

				if (liquid.isPresent()) {
					SizedFluidIngredient ingredient = liquid.get();
					int amount = ingredient.amount();
					FluidStack drained = this.moltenTank.drainInternal(amount, IFluidHandler.FluidAction.SIMULATE);

					if (ingredient.test(drained)) {
						this.moltenTank.drainInternal(amount, IFluidHandler.FluidAction.EXECUTE);
						consumedLiquid = true;
					} else {
						consumedLiquid = false;
					}
				} else {
					consumedLiquid = true;
				}

				if (consumedLiquid) {
					removeFromInventory(myRecipe, true);

					// Damage plan
					if (!getItem(InventoryFabricator.SLOT_PLAN).isEmpty()) {
						Item planItem = getItem(InventoryFabricator.SLOT_PLAN).getItem();
						if (planItem instanceof ICraftingPlan plan) {
							ItemStack planUsed = plan.planUsed(getItem(InventoryFabricator.SLOT_PLAN), craftResult);
							setItem(InventoryFabricator.SLOT_PLAN, planUsed);
						}
					}

					setItem(InventoryFabricator.SLOT_RESULT, craftResult);
				}
			}
		}
	}

	private boolean removeFromInventory(IFabricatorRecipe recipe, boolean doRemove) {
		Container inventory = new InventoryMapper(this, InventoryFabricator.SLOT_INVENTORY_1, InventoryFabricator.SLOT_INVENTORY_COUNT);
		return InventoryUtil.consumeIngredients(inventory, recipe.getCraftingGridRecipe().getIngredients(), null, true, false, doRemove);
	}

	@Override
	public boolean hasWork() {
		boolean hasRecipe = true;
		boolean hasLiquidResources = true;
		boolean hasResources = true;

		ItemStack plan = getItem(InventoryFabricator.SLOT_PLAN);
		RecipeHolder<IFabricatorRecipe> holder = RecipeUtil.getFabricatorRecipe(this.level.getRecipeManager(), this.level, this.moltenTank.getFluid(), plan, this.craftingInventory);
		if (holder != null) {
			IFabricatorRecipe recipe = holder.value();
			hasResources = removeFromInventory(recipe, false);

			Optional<SizedFluidIngredient> toDrain = recipe.getRequiredFluid();
			if (toDrain.isPresent()) {
				SizedFluidIngredient requiredFluid = toDrain.get();
				FluidStack drained = this.moltenTank.drainInternal(requiredFluid.amount(), IFluidHandler.FluidAction.SIMULATE);
				hasLiquidResources = requiredFluid.test(drained);
			}
		} else {
			hasRecipe = RecipeUtil.getFabricatorMeltingRecipe(this.level.getRecipeManager(), getItem(InventoryFabricator.SLOT_METAL)) != null;
		}

		IErrorLogic errorLogic = getErrorLogic();
		errorLogic.setCondition(!hasRecipe, ForestryError.NO_RECIPE);
		errorLogic.setCondition(!hasLiquidResources, ForestryError.NO_RESOURCE_LIQUID);
		errorLogic.setCondition(!hasResources, ForestryError.NO_RESOURCE_INVENTORY);

		return hasRecipe;
	}

	public int getHeatScaled(int i) {
		return this.heat * i / MAX_HEAT;
	}

	private int getMeltingPoint() {
		if (!this.getItem(InventoryFabricator.SLOT_METAL).isEmpty()) {
			RecipeHolder<FabricatorSmeltingRecipe> meltingRecipe = RecipeUtil.getFabricatorMeltingRecipe(getLevel().getRecipeManager(), this.getItem(InventoryFabricator.SLOT_METAL));
			return meltingRecipe == null ? 0 : meltingRecipe.value().meltingPoint();
		} else if (this.moltenTank.getFluidAmount() > 0) {
			return this.meltingPoint;
		}

		return 0;
	}

	public int getMeltingPointScaled(int i) {
		int meltingPoint = getMeltingPoint();

		if (meltingPoint <= 0) {
			return 0;
		} else {
			return meltingPoint * i / MAX_HEAT;
		}
	}

	/* SMP */
	public void getGUINetworkData(int i, int j) {
		if (i == 0) {
			this.heat = j;
		} else if (i == 1) {
			this.meltingPoint = j;
		}
	}

	public void sendGUINetworkData(AbstractContainerMenu container, ContainerListener iCrafting) {
		iCrafting.dataChanged(container, 0, this.heat);
		iCrafting.dataChanged(container, 1, getMeltingPoint());
	}

	/**
	 * @return Inaccessible crafting inventory for the craft grid.
	 */
	public InventoryAdapter getCraftingInventory() {
		return this.craftingInventory;
	}

	@Override
	public TankManager getTankManager() {
		return this.tankManager;
	}

	@Override
	public AbstractContainerMenu createMenu(int windowId, Inventory inv, Player player) {
		return new FabricatorMenu(windowId, player.getInventory(), this);
	}
}

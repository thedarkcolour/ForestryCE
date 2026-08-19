package forestry.core.content.machines.tiles;

import forestry.api.IForestryApi;
import forestry.api.core.circuits.ForestryCircuitSocketTypes;
import forestry.api.core.circuits.ICircuitBoard;
import forestry.api.core.ForestryError;
import forestry.api.core.IErrorLogic;
import forestry.api.core.machines.ISqueezerRecipe;
import forestry.core.engine.circuits.IMachineUpgradable;
import forestry.core.engine.circuits.ISocketable;
import forestry.core.platform.config.Constants;
import forestry.core.platform.fluids.StandardTank;
import forestry.core.platform.fluids.TankManager;
import forestry.core.platform.inventory.InventoryAdapter;
import forestry.core.platform.inventory.wrappers.InventoryMapper;
import forestry.core.platform.render.TankRenderInfo;
import forestry.core.platform.tile.ILiquidTankTile;
import forestry.core.platform.tile.TilePowered;
import forestry.core.platform.util.InventoryUtil;
import forestry.core.platform.util.RecipeUtils;
import forestry.core.content.machines.features.FactoryTiles;
import forestry.core.content.machines.gui.ContainerSqueezer;
import forestry.core.content.machines.inventory.InventorySqueezer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import javax.annotation.Nullable;
import java.util.List;

public class TileSqueezer extends TilePowered implements ISocketable, WorldlyContainer, ILiquidTankTile, IMachineUpgradable {
	private static final int STEPS_PER_RECIPE_TIME = 1;
	private static final int ENERGY_PER_WORK_CYCLE = 2000;
	private static final int ENERGY_PER_RECIPE_TIME = ENERGY_PER_WORK_CYCLE / 10;

	private final InventoryAdapter sockets = new InventoryAdapter(1, "sockets");

	private final TankManager tankManager;
	private final StandardTank productTank;
	private final InventorySqueezer inventory;
	@Nullable
	private ISqueezerRecipe currentRecipe;

	public TileSqueezer(BlockPos pos, BlockState state) {
		super(FactoryTiles.SQUEEZER.tileType(), pos, state, 1100, Constants.MACHINE_MAX_ENERGY);
		this.inventory = new InventorySqueezer(this);
		setInternalInventory(this.inventory);
		this.productTank = new StandardTank(Constants.PROCESSOR_TANK_CAPACITY, false, true);
		this.tankManager = new TankManager(this, this.productTank);
	}

	/* LOADING & SAVING */

	@Override
	public void saveAdditional(CompoundTag compoundNBT, HolderLookup.Provider registries) {
		super.saveAdditional(compoundNBT, registries);
		this.tankManager.write(compoundNBT, registries);
		this.sockets.write(compoundNBT, registries);
	}

	@Override
	public void loadAdditional(CompoundTag compoundNBT, HolderLookup.Provider registries) {
		super.loadAdditional(compoundNBT, registries);
		this.tankManager.read(compoundNBT, registries);
		this.sockets.read(compoundNBT, registries);

		ItemStack chip = this.sockets.getItem(0);
		if (!chip.isEmpty()) {
			ICircuitBoard chipset = IForestryApi.INSTANCE.getCircuitManager().getCircuitBoard(chip);
			if (chipset != null) {
				chipset.onLoad(this);
			}
		}
	}

	@Override
	public void writeData(FriendlyByteBuf data) {
		super.writeData(data);
		this.tankManager.writeData(data);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void readData(FriendlyByteBuf data) {
		super.readData(data);
		this.tankManager.readData(data);
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

	// / WORKING
	@Override
	public void serverTick(Level level, BlockPos pos, BlockState state) {
		super.serverTick(level, pos, state);

		if (updateOnInterval(20)) {
			FluidStack fluid = this.productTank.getFluid();
			if (!fluid.isEmpty()) {
				this.inventory.fillContainers(fluid, this.tankManager);
			}
		}
	}

	@Override
	public boolean workCycle() {
		if (this.currentRecipe == null) {
			return false;
		}
		if (!this.inventory.removeResources(this.currentRecipe.getInputs())) {
			return false;
		}

		// createRandomFluidStack folds in any addon-defined per-cycle randomness (variable amount / chance of nothing).
		// It may return less than createFluidStack() (the amount reserved below) or EMPTY, in which case fill is a no-op.
		FluidStack resultFluid = this.currentRecipe.getFluidOutput().createRandomFluidStack(this.level.random);
		this.productTank.fillInternal(resultFluid, IFluidHandler.FluidAction.EXECUTE);

		float roll = this.level.random.nextFloat();
		double threshold = this.currentRecipe.getRemnantsChance() * this.outputMultiplier;

		if (!this.currentRecipe.getRemnants().isEmpty() && roll < threshold) {
			ItemStack remnant = this.currentRecipe.getRemnants().copy();
			this.inventory.addRemnant(remnant, true);
		}

		return true;
	}

	private boolean checkRecipe() {
		ISqueezerRecipe matchingRecipe = null;

		if (this.inventory.hasResources()) {
			List<ItemStack> resources = this.inventory.getResources();

			boolean containsSets = false;

			if (this.currentRecipe != null) {
				Container inventory = new InventoryMapper(this, InventorySqueezer.SLOT_RESOURCE_1, InventorySqueezer.SLOTS_RESOURCE_COUNT);
				containsSets = InventoryUtil.consumeIngredients(inventory, this.currentRecipe.getInputs(), null, false, false, false);
			}

			if (this.currentRecipe != null && containsSets) {
				matchingRecipe = this.currentRecipe;
			} else {
				matchingRecipe = RecipeUtils.getSqueezerRecipe(getLevel().getRecipeManager(), resources);
			}

			if (matchingRecipe == null) {
				for (ItemStack resource : resources) {
					if (matchingRecipe == null) {
						matchingRecipe = RecipeUtils.getSqueezerContainerRecipe(getLevel().getRecipeManager(), resource);
					}
				}
			}

			// A dynamic output (e.g. an addon tag product no loaded mod fills) resolves to an empty representative
			// stack; refuse the recipe rather than consuming the input for no fluid.
			if (matchingRecipe != null && matchingRecipe.getFluidOutput().createFluidStack().isEmpty()) {
				matchingRecipe = null;
			}
		}

		if (this.currentRecipe != matchingRecipe) {
			this.currentRecipe = matchingRecipe;

			if (this.currentRecipe != null) {
				int recipeTime = this.currentRecipe.getProcessingTime();
				setStepsPerWorkCycle(recipeTime * STEPS_PER_RECIPE_TIME);
				setEnergyPerWorkCycle(recipeTime * ENERGY_PER_RECIPE_TIME);
			}
		}

		getErrorLogic().setCondition(this.currentRecipe == null, ForestryError.NO_RECIPE);
		return this.currentRecipe != null;
	}

	@Override
	public boolean hasWork() {
		checkRecipe();

		boolean hasResources = this.inventory.hasResources();
		boolean hasRecipe = true;
		boolean canFill = true;
		boolean canAdd = true;

		if (hasResources) {
			hasRecipe = this.currentRecipe != null;
			if (hasRecipe) {
				// Reserve worst-case space: createFluidStack() is the MAX amount this product can emit, so the machine
				// only starts if the tank can hold it, even if a given cycle later produces less.
				FluidStack resultFluid = this.currentRecipe.getFluidOutput().createFluidStack();
				canFill = this.productTank.fillInternal(resultFluid, IFluidHandler.FluidAction.SIMULATE) == resultFluid.getAmount();

				if (!this.currentRecipe.getRemnants().isEmpty()) {
					canAdd = this.inventory.addRemnant(this.currentRecipe.getRemnants(), false);
				}
			}
		}

		IErrorLogic errorLogic = getErrorLogic();
		errorLogic.setCondition(!hasResources, ForestryError.NO_RESOURCE);
		errorLogic.setCondition(!hasRecipe, ForestryError.NO_RECIPE);
		errorLogic.setCondition(!canFill, ForestryError.NO_SPACE_TANK);
		errorLogic.setCondition(!canAdd, ForestryError.NO_SPACE_INVENTORY);

		return hasResources && hasRecipe && canFill && canAdd;
	}

	@Nullable
	public ISqueezerRecipe getCurrentRecipe() {
		return this.currentRecipe;
	}

	@Override
	public TankRenderInfo getProductTankInfo() {
		return new TankRenderInfo(this.productTank);
	}


	@Override
	public TankManager getTankManager() {
		return this.tankManager;
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
		if (stack.isEmpty() || IForestryApi.INSTANCE.getCircuitManager().isCircuitBoard(stack)) {
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
			if (!stack.isEmpty()) {
				ICircuitBoard chipset = IForestryApi.INSTANCE.getCircuitManager().getCircuitBoard(stack);
				if (chipset != null) {
					chipset.onInsertion(this);
				}
			}
		}
	}

	@Override
	public ResourceLocation getSocketType() {
		return ForestryCircuitSocketTypes.MACHINE;
	}

	public IFluidHandler getFluidHandler(@Nullable Direction facing) {
		return this.tankManager;
	}

	@Override
	public AbstractContainerMenu createMenu(int windowId, Inventory inv, Player player) {
		return new ContainerSqueezer(windowId, inv, this);
	}
}

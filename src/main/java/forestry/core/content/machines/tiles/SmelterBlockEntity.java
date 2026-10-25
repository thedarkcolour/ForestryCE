package forestry.core.content.machines.tiles;

import forestry.api.IForestryApi;
import forestry.api.core.ForestryError;
import forestry.api.core.IErrorLogic;
import forestry.api.core.circuits.ForestryCircuitSocketTypes;
import forestry.api.core.circuits.ICircuitBoard;
import forestry.api.core.machines.ISmelterRecipe;
import forestry.core.content.machines.features.FactoryBlockEntities;
import forestry.core.content.machines.gui.SmelterMenu;
import forestry.core.content.machines.inventory.SmelterInventory;
import forestry.core.engine.circuits.ISocketable;
import forestry.core.platform.block.BaseBlock;
import forestry.core.platform.config.Constants;
import forestry.core.platform.inventory.InventoryAdapter;
import forestry.core.platform.tile.IItemStackDisplay;
import forestry.core.platform.tile.PoweredBlockEntity;
import forestry.core.platform.util.RecipeUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class SmelterBlockEntity extends PoweredBlockEntity implements WorldlyContainer, ISocketable, IItemStackDisplay {
	private static final int STEPS_PER_RECIPE_TIME = 1;
	private static final int ENERGY_PER_WORK_CYCLE = 2000;
	private static final int ENERGY_PER_RECIPE_TIME = ENERGY_PER_WORK_CYCLE / 10;
	// How often the flame and smoke plume is emitted, in game ticks. Deviation from 1.20.1: that tree
	// spelled this WORK_TICK_INTERVAL * 4, and the work tick interval is private to TilePowered here
	private static final int PARTICLE_INTERVAL = 20;

	private final InventoryAdapter sockets = new InventoryAdapter(1, "sockets");
	private final ResultContainer craftPreviewInventory;
	private final SmelterInventory inventory;

	@Nullable
	private ISmelterRecipe currentRecipe;

	public SmelterBlockEntity(BlockPos pos, BlockState state) {
		super(FactoryBlockEntities.SMELTER.tileType(), pos, state, 1100, Constants.MACHINE_MAX_ENERGY);

		this.craftPreviewInventory = new ResultContainer();
		this.inventory = new SmelterInventory(this);
		setInternalInventory(this.inventory);
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

	/* LOADING & SAVING */
	@Override
	public void saveAdditional(CompoundTag compound, HolderLookup.Provider registries) {
		super.saveAdditional(compound, registries);

		this.sockets.write(compound, registries);
	}

	@Override
	public void loadAdditional(CompoundTag compound, HolderLookup.Provider registries) {
		super.loadAdditional(compound, registries);

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

	/* WORKING */
	@Override
	public void serverTick(Level level, BlockPos pos, BlockState state) {
		super.serverTick(level, pos, state);

		// for particles sake
		if (updateOnInterval(PARTICLE_INTERVAL)) {
			if (level instanceof ServerLevel serverLevel) {
				if (getErrorLogic().hasErrors()) {
					return;
				}

				Direction facing = state.getValue(BaseBlock.FACING);

				float posX = pos.getX();
				float posY = pos.getY() + (1f / 4f);
				float posZ = pos.getZ();

				float dX = 0;
				float dY = (1f / 8f);
				float dZ = 0;

				switch (facing) {
					case NORTH -> {
						posX += 0.5f;
						posZ -= (1f / 16f);
						dX = 0.2f;
					}
					case EAST -> {
						posX += (17f / 16f);
						posZ += 0.5f;
						dZ = 0.2f;
					}
					case SOUTH -> {
						posX += 0.5f;
						posZ += (17f / 16f);
						dX = 0.2f;
					}
					case WEST -> {
						posX -= (1f / 16f);
						posZ += 0.5f;
						dZ = 0.2f;
					}
					default -> {
					}
				}

				serverLevel.sendParticles(ParticleTypes.FLAME, posX, posY, posZ, 3, dX, dY, dZ, 0);
				serverLevel.sendParticles(ParticleTypes.SMOKE, posX, posY, posZ, 3, dX, dY, dZ, 0);
			}
		}
	}

	// Called by super.serverTick()
	@Override
	public boolean hasWork() {
		checkRecipe();

		boolean hasResources = this.inventory.hasResources();
		boolean hasRecipe = true;
		boolean canAdd = true;

		if (hasResources) {
			hasRecipe = this.currentRecipe != null;
			if (hasRecipe) {
				if (!this.currentRecipe.getOutput().isEmpty()) {
					canAdd = this.inventory.addResult(this.currentRecipe.getOutput(), false);
				}
			}
		}

		IErrorLogic errorLogic = getErrorLogic();
		errorLogic.setCondition(!hasResources, ForestryError.NO_RESOURCE);
		errorLogic.setCondition(!hasRecipe, ForestryError.NO_RECIPE);
		errorLogic.setCondition(!canAdd, ForestryError.NO_SPACE_INVENTORY);

		return hasResources && hasRecipe && canAdd;
	}

	public boolean checkRecipe() {
		RecipeManager manager = RecipeUtils.getRecipeManager();

		// Look for a recipe if the manager exists
		if (manager != null) {
			ISmelterRecipe matchingRecipe = RecipeUtils.getSmelterRecipe(manager, this.inventory.getResources());

			// If there is a disparity between the current recipe and the found recipe, update it
			if (this.currentRecipe != matchingRecipe) {
				this.currentRecipe = matchingRecipe;

				if (this.currentRecipe != null) {
					handleItemStackForDisplay(this.currentRecipe.getOutput());

					int recipeTime = this.currentRecipe.getProcessingTime();
					setStepsPerWorkCycle(recipeTime * STEPS_PER_RECIPE_TIME);
					setEnergyPerWorkCycle(recipeTime * ENERGY_PER_RECIPE_TIME);
				} else {
					this.craftPreviewInventory.clearContent();
					setStepsPerWorkCycle(0);
				}
			}
		}

		getErrorLogic().setCondition(this.currentRecipe == null, ForestryError.NO_RECIPE);
		return this.currentRecipe != null;
	}

	@Override
	protected boolean workCycle() {
		if (this.currentRecipe == null) {
			return false;
		}

		if (!this.inventory.removeResources(this.currentRecipe.getInputs())) {
			return false;
		}

		this.inventory.addResult(this.currentRecipe.getOutput(), true);
		return true;
	}

	public Container getCraftPreviewInventory() {
		return this.craftPreviewInventory;
	}

	@Override
	public void handleItemStackForDisplay(ItemStack itemStack) {
		this.craftPreviewInventory.setItem(0, itemStack);
	}

	@Override
	public AbstractContainerMenu createMenu(int windowId, Inventory inventory, Player player) {
		return new SmelterMenu(windowId, player.getInventory(), this);
	}
}

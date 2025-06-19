package forestry.apiculture.multiblock;

import forestry.api.climate.IClimateControlled;
import forestry.api.multiblock.IAlvearyComponent;
import forestry.api.recipes.IHygroregulatorRecipe;
import forestry.apiculture.blocks.BlockAlveary;
import forestry.apiculture.gui.AlvearyHygroregulatorMenu;
import forestry.apiculture.inventory.InventoryHygroregulator;
import forestry.core.config.Constants;
import forestry.core.fluids.FilteredTank;
import forestry.core.fluids.FluidHelper;
import forestry.core.fluids.FluidRecipeFilter;
import forestry.core.fluids.TankManager;
import forestry.core.inventory.IInventoryAdapter;
import forestry.core.tiles.ILiquidTankTile;
import forestry.core.utils.RecipeUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import javax.annotation.Nullable;

public class TileAlvearyHygroregulator extends TileAlveary implements Container, ILiquidTankTile, IAlvearyComponent.Climatiser<MultiblockLogicAlveary>, IAlvearyComponent.HasInventory {
	private final TankManager tankManager;
	private final FilteredTank liquidTank;
	private final IInventoryAdapter inventory;

	@Nullable
	private RecipeHolder<IHygroregulatorRecipe> currentRecipe;
	// number of ticks the current temperature change lasts for.
	private int heatTicks;

	public TileAlvearyHygroregulator(BlockPos pos, BlockState state) {
		super(BlockAlveary.Type.HYGRO, pos, state);

		this.inventory = new InventoryHygroregulator(this);
		this.liquidTank = new FilteredTank(Constants.PROCESSOR_TANK_CAPACITY).setFilter(FluidRecipeFilter.HYGROREGULATOR_INPUT);
		this.tankManager = new TankManager(this, this.liquidTank);
	}

	@Override
	public IInventoryAdapter getInternalInventory() {
		return this.inventory;
	}

	@Override
	public boolean allowsAutomation() {
		return true;
	}

	/* UPDATING */
	@Override
	public void changeClimate(int tickCount, IClimateControlled climateControlled) {
		if (this.heatTicks <= 0) {
			FluidStack fluid = this.liquidTank.getFluid();

			if (!fluid.isEmpty()) {
				this.currentRecipe = RecipeUtil.getHygroRegulatorRecipe(this.level.getRecipeManager(), fluid);

				if (this.currentRecipe != null) {
					this.liquidTank.drainInternal(this.currentRecipe.value().getInputFluid().amount(), IFluidHandler.FluidAction.EXECUTE);
					this.heatTicks = 20;
				}
			}
		}

		if (this.heatTicks > 0) {
			this.heatTicks--;
			if (this.currentRecipe != null) {
				climateControlled.addHumidityChange(this.currentRecipe.value().getHumiditySteps());
				climateControlled.addTemperatureChange(this.currentRecipe.value().getTemperatureSteps());
			} else {
				this.heatTicks = 0;
			}
		}

		if (tickCount % 20 == 0) {
			// Check if we have suitable items waiting in the item slot
			FluidHelper.drainContainers(this.tankManager, this, 0);
		}
	}

	/* SAVING & LOADING */
	@Override
	public void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);
		this.tankManager.read(nbt, registries);

		this.heatTicks = nbt.getInt("TransferTime");

		if (nbt.contains("currentRecipe")) {
			this.currentRecipe = RecipeUtil.getRecipe(ResourceLocation.tryParse(nbt.getString("currentRecipe")));
		}
	}


	@Override
	public void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.saveAdditional(nbt, registries);
		this.tankManager.write(nbt, registries);

		nbt.putInt("TransferTime", this.heatTicks);
		if (this.currentRecipe != null) {
			nbt.putString("currentRecipe", this.currentRecipe.id().toString());
		}
	}

	@Override
	public TankManager getTankManager() {
		return this.tankManager;
	}

	@Override
	public AbstractContainerMenu createMenu(int windowId, Inventory inv, Player player) {
		return new AlvearyHygroregulatorMenu(windowId, inv, this);
	}
}

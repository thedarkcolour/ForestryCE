package forestry.factory.tiles;

import forestry.api.core.ForestryError;
import forestry.api.core.IErrorLogic;
import forestry.core.config.Constants;
import forestry.core.fluids.*;
import forestry.core.tiles.ILiquidTankTile;
import forestry.core.tiles.TileBase;
import forestry.factory.features.FactoryTiles;
import forestry.factory.gui.RaintankMenu;
import forestry.factory.inventory.InventoryRaintank;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import javax.annotation.Nullable;

public class TileRaintank extends TileBase implements WorldlyContainer, ILiquidTankTile {
	private static final FluidStack STACK_WATER = new FluidStack(Fluids.WATER, FluidType.BUCKET_VOLUME);
	private static final FluidStack WATER_PER_UPDATE = new FluidStack(Fluids.WATER, Constants.RAINTANK_AMOUNT_PER_UPDATE);

	private final FilteredTank resourceTank;
	private final TankManager tankManager;
	private final ContainerFiller containerFiller;

	@Nullable
	private Boolean canDumpBelow = null;
	private boolean dumpingFluid = false;

	// client
	private int fillingProgress;

	public TileRaintank(BlockPos pos, BlockState state) {
		super(FactoryTiles.RAIN_TANK.tileType(), pos, state);
		setInternalInventory(new InventoryRaintank(this));

		this.resourceTank = new FilteredTank(Constants.RAINTANK_TANK_CAPACITY).setFilter(FluidTagFilter.WATER);

		this.tankManager = new TankManager(this, this.resourceTank);

		this.containerFiller = new ContainerFiller(this.resourceTank, Constants.RAINTANK_FILLING_TIME, this, InventoryRaintank.SLOT_RESOURCE, InventoryRaintank.SLOT_PRODUCT);
	}

	@Override
	public void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.saveAdditional(nbt, registries);
		this.tankManager.write(nbt, registries);
	}

	@Override
	public void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);
		this.tankManager.read(nbt, registries);
	}

	@Override
	public void writeData(RegistryFriendlyByteBuf buffer) {
		super.writeData(buffer);
		this.tankManager.writeData(buffer);
	}

	@Override
	public void readData(RegistryFriendlyByteBuf buffer) {
		super.readData(buffer);
		this.tankManager.readData(buffer);
	}

	@Override
	public void serverTick(Level level, BlockPos pos, BlockState state) {
		if (updateOnInterval(20)) {
			IErrorLogic errorLogic = getErrorLogic();

			Biome biome = level.getBiome(pos).value();
			errorLogic.setCondition(!(biome.getPrecipitationAt(pos) == Biome.Precipitation.RAIN), ForestryError.NO_RAIN_BIOME);

			BlockPos posAbove = pos.above();
			boolean hasSky = level.canSeeSkyFromBelowWater(posAbove);
			errorLogic.setCondition(!hasSky, ForestryError.NO_SKY_RAIN_TANK);

			errorLogic.setCondition(!level.isRainingAt(posAbove), ForestryError.NOT_RAINING);

			if (!errorLogic.hasErrors()) {
				this.resourceTank.fillInternal(WATER_PER_UPDATE, IFluidHandler.FluidAction.EXECUTE);
			}

			this.containerFiller.updateServerSide();
		}

		if (this.canDumpBelow == null) {
			this.canDumpBelow = FluidHelper.canAcceptFluid(level, getBlockPos().below(), Direction.UP, STACK_WATER);
		}

		if (this.canDumpBelow) {
			if (this.dumpingFluid || updateOnInterval(20)) {
				this.dumpingFluid = dumpFluidBelow();
			}
		}
	}

	private boolean dumpFluidBelow() {
		if (!this.resourceTank.isEmpty()) {
			IFluidHandler fluidCap = this.level.getCapability(Capabilities.FluidHandler.BLOCK, this.worldPosition.below(), Direction.UP);
			if (fluidCap != null) {
				return !FluidUtil.tryFluidTransfer(fluidCap, this.tankManager, FluidType.BUCKET_VOLUME / 20, true).isEmpty();
			}
		}
		return false;
	}

	public boolean isFilling() {
		return this.fillingProgress > 0;
	}

	public int getFillProgressScaled(int i) {
		return this.fillingProgress * i / Constants.RAINTANK_FILLING_TIME;
	}

	/* SMP GUI */
	public void getGUINetworkData(int i, int j) {
		switch (i) {
			case 0 -> this.fillingProgress = j;
		}
	}

	public void sendGUINetworkData(AbstractContainerMenu container, ContainerListener iCrafting) {
		iCrafting.dataChanged(container, 0, this.containerFiller.getFillingProgress());
	}

	@Override
	public TankManager getTankManager() {
		return this.tankManager;
	}

	@Override
	public void onNeighborTileChange(Level level, BlockPos pos, BlockPos neighbor) {
		super.onNeighborTileChange(level, pos, neighbor);

		if (neighbor.equals(pos.below())) {
			this.canDumpBelow = FluidHelper.canAcceptFluid(level, neighbor, Direction.UP, STACK_WATER);
		}
	}

	@Override
	public AbstractContainerMenu createMenu(int windowId, Inventory inv, Player player) {
		return new RaintankMenu(windowId, inv, this);
	}
}

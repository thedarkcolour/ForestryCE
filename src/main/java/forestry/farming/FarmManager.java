package forestry.farming;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import forestry.api.core.ForestryError;
import forestry.api.core.IErrorLogic;
import forestry.api.core.INbtReadable;
import forestry.api.core.INbtWritable;
import forestry.api.farming.*;
import forestry.core.config.Constants;
import forestry.core.fluids.FilteredTank;
import forestry.core.fluids.FluidTagFilter;
import forestry.core.fluids.StandardTank;
import forestry.core.fluids.TankManager;
import forestry.core.network.IStreamable;
import forestry.cultivation.IFarmHousingInternal;
import forestry.farming.multiblock.FarmFertilizerManager;
import forestry.farming.multiblock.FarmHydrationManager;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;
import java.util.*;

public class FarmManager implements INbtReadable, INbtWritable, IStreamable, IExtentCache {
	private final Map<Direction, List<FarmTarget>> targets = new EnumMap<>(Direction.class);
	private final Table<Direction, BlockPos, Integer> lastExtents = HashBasedTable.create();
	private final IFarmHousingInternal housing;
	@Nullable
	private IFarmLogic harvestProvider; // The farm logic which supplied the pending crops.
	private final List<ICrop> pendingCrops = new LinkedList<>();
	private final ArrayDeque<ItemStack> pendingProduce = new ArrayDeque<>();

	private FarmingStage stage = FarmingStage.CULTIVATE;

	private final Set<IFarmListener> farmListeners = new HashSet<>();

	private final FarmHydrationManager hydrationManager;
	private final FarmFertilizerManager fertilizerManager;
	private final TankManager tankManager;
	private final StandardTank resourceTank;

	// tick updates can come from multiple gearboxes so keep track of them here
	private int farmWorkTicks = 0;

	public FarmManager(IFarmHousingInternal housing) {
		this.housing = housing;
		this.resourceTank = new FilteredTank(Constants.PROCESSOR_TANK_CAPACITY).setFilter(FluidTagFilter.WATER);

		this.tankManager = new TankManager(housing, this.resourceTank);

		this.hydrationManager = new FarmHydrationManager(housing);
		this.fertilizerManager = new FarmFertilizerManager(housing);
	}

	public FarmHydrationManager getHydrationManager() {
		return this.hydrationManager;
	}

	public TankManager getTankManager() {
		return this.tankManager;
	}

	public FarmFertilizerManager getFertilizerManager() {
		return this.fertilizerManager;
	}

	public StandardTank getResourceTank() {
		return this.resourceTank;
	}

	public void addListener(IFarmListener listener) {
        this.farmListeners.add(listener);
	}

	public void removeListener(IFarmListener listener) {
        this.farmListeners.remove(listener);
	}

	public boolean doWork() {
        this.farmWorkTicks++;
		if (this.targets.isEmpty() || this.farmWorkTicks % 20 == 0) {
            this.housing.setUpFarmlandTargets(this.targets);
		}

		IErrorLogic errorLogic = this.housing.getErrorLogic();

		if (!this.pendingProduce.isEmpty()) {
			boolean added = this.housing.getFarmInventory().tryAddPendingProduce(this.pendingProduce);
			errorLogic.setCondition(!added, ForestryError.NO_SPACE_INVENTORY);
			return added;
		}

		boolean hasFertilizer = this.fertilizerManager.maintainFertilizer();
		if (errorLogic.setCondition(!hasFertilizer, ForestryError.NO_FERTILIZER)) {
			return false;
		}

		// Cull queued crops.
		if (!this.pendingCrops.isEmpty() && this.harvestProvider != null) {
			ICrop first = this.pendingCrops.get(0);
			if (cullCrop(first, this.harvestProvider)) {
                this.pendingCrops.remove(0);
				return true;
			} else {
				return false;
			}
		}

		// Cultivation and collection
		FarmWorkStatus farmWorkStatus = new FarmWorkStatus();

		Level level = this.housing.getLevel();
		ObjectArrayList<Direction> farmDirections = new ObjectArrayList<>(HorizontalDirection.VALUES);
		Util.shuffle(farmDirections, level.random);
		for (Direction farmSide : farmDirections) {
			IFarmLogic logic = this.housing.getFarmLogic(farmSide);
			List<FarmTarget> farmTargets = this.targets.get(farmSide);

			if (this.stage == FarmingStage.CULTIVATE) {
				for (FarmTarget target : farmTargets) {
					if (target.getExtent() > 0) {
						farmWorkStatus.hasFarmland = true;
						break;
					}
				}
			}

			if (FarmHelper.isCycleCanceledByListeners(logic, farmSide, this.farmListeners)) {
				continue;
			}

			// Always try to collect windfall.
			if (collectWindfall(logic)) {
				farmWorkStatus.didWork = true;
			}

			if (this.stage == FarmingStage.HARVEST) {
				Collection<ICrop> harvested = FarmHelper.harvestTargets(level, this.housing, farmTargets, logic, this.farmListeners);
				farmWorkStatus.didWork = !harvested.isEmpty();
				if (!harvested.isEmpty()) {
                    this.pendingCrops.addAll(harvested);
                    this.pendingCrops.sort(FarmHelper.TOP_DOWN_COMPARATOR);
                    this.harvestProvider = logic;
				}
			} else if (this.stage == FarmingStage.CULTIVATE) {
				cultivateTargets(farmWorkStatus, farmTargets, logic, farmSide);
			}

			if (farmWorkStatus.didWork) {
				break;
			}
		}

		if (this.stage == FarmingStage.CULTIVATE) {
			errorLogic.setCondition(!farmWorkStatus.hasFarmland, ForestryError.NO_FARMLAND);
			errorLogic.setCondition(!farmWorkStatus.hasFertilizer, ForestryError.NO_FERTILIZER);
			errorLogic.setCondition(!farmWorkStatus.hasLiquid, ForestryError.NO_LIQUID_FARM);
		}

		// alternate between cultivation and harvest.
        this.stage = this.stage.next();

		return farmWorkStatus.didWork;
	}

	private void cultivateTargets(FarmWorkStatus farmWorkStatus, List<FarmTarget> farmTargets, IFarmLogic logic, Direction farmSide) {
		Level level = this.housing.getLevel();

		if (farmWorkStatus.hasFarmland && !FarmHelper.isCycleCanceledByListeners(logic, farmSide, this.farmListeners)) {
			final float hydrationModifier = this.hydrationManager.getHydrationModifier();
			final int fertilizerConsumption = Math.round(logic.getType().getFertilizerConsumption(this.housing));
			final int liquidConsumption = logic.getType().getWaterConsumption(this.housing, hydrationModifier);
			final FluidStack liquid = new FluidStack(Fluids.WATER, liquidConsumption);

			for (FarmTarget target : farmTargets) {
				// Check fertilizer and water
				if (!this.fertilizerManager.hasFertilizer(fertilizerConsumption)) {
					farmWorkStatus.hasFertilizer = false;
					continue;
				}

				if (liquid.getAmount() > 0 && !this.housing.hasLiquid(liquid)) {
					farmWorkStatus.hasLiquid = false;
					continue;
				}

				if (FarmHelper.cultivateTarget(level, this.housing, target, logic, this.farmListeners)) {
					// Remove fertilizer and water
                    this.fertilizerManager.removeFertilizer(fertilizerConsumption);
                    this.housing.removeLiquid(liquid);

					farmWorkStatus.didWork = true;
				}
			}
		}
	}

	private boolean collectWindfall(IFarmLogic logic) {
		List<ItemStack> collected = logic.collect(this.housing.getLevel(), this.housing);
		if (collected.isEmpty()) {
			return false;
		}

		// Let event handlers know.
		for (IFarmListener listener : this.farmListeners) {
			listener.hasCollected(collected, logic);
		}

        this.housing.getFarmInventory().stowProducts(collected, this.pendingProduce);

		return true;
	}

	private boolean cullCrop(ICrop crop, IFarmLogic provider) {
		// Let event handlers handle the harvest first.
		for (IFarmListener listener : this.farmListeners) {
			if (listener.beforeCropHarvest(crop)) {
				return true;
			}
		}

		int fertilizerConsumption = provider.getType().getFertilizerConsumption(this.housing);

		IErrorLogic errorLogic = this.housing.getErrorLogic();

		// Check fertilizer
		boolean hasFertilizer = this.fertilizerManager.hasFertilizer(fertilizerConsumption);
		if (errorLogic.setCondition(!hasFertilizer, ForestryError.NO_FERTILIZER)) {
			return false;
		}

		// Check water
		float hydrationModifier = this.hydrationManager.getHydrationModifier();
		int waterConsumption = provider.getType().getWaterConsumption(this.housing, hydrationModifier);
		FluidStack requiredLiquid = new FluidStack(Fluids.WATER, waterConsumption);
		boolean hasLiquid = requiredLiquid.getAmount() == 0 || this.housing.hasLiquid(requiredLiquid);

		if (errorLogic.setCondition(!hasLiquid, ForestryError.NO_LIQUID_FARM)) {
			return false;
		}

		List<ItemStack> harvested = crop.harvest();
		if (harvested != null) {
			// Remove fertilizer and water
            this.fertilizerManager.removeFertilizer(fertilizerConsumption);
            this.housing.removeLiquid(requiredLiquid);

			// Let event handlers handle the harvest first.
			for (IFarmListener listener : this.farmListeners) {
				listener.afterCropHarvest(harvested, crop);
			}

            this.housing.getFarmInventory().stowProducts(harvested, this.pendingProduce);
		}
		return true;
	}

	@Override
	public CompoundTag write(CompoundTag data) {
        this.hydrationManager.write(data);
        this.tankManager.write(data);
        this.fertilizerManager.write(data);
		return data;
	}

	@Override
	public void read(CompoundTag data) {
        this.hydrationManager.read(data);
        this.tankManager.read(data);
        this.fertilizerManager.read(data);
	}

	@Override
	public void writeData(RegistryFriendlyByteBuf data) {
        this.tankManager.writeData(data);
        this.hydrationManager.writeData(data);
        this.fertilizerManager.writeData(data);
	}

	@Override
	public void readData(RegistryFriendlyByteBuf data) {
        this.tankManager.readData(data);
        this.hydrationManager.readData(data);
        this.fertilizerManager.readData(data);
	}

	public void clearTargets() {
        this.targets.clear();
	}

	public void addPendingProduct(ItemStack stack) {
		this.pendingProduce.add(stack);
	}

	public BlockPos getFarmCorner(Direction direction) {
		List<FarmTarget> targetList = this.targets.get(direction);
		if (targetList.isEmpty()) {
			return this.housing.getCoords();
		}
		FarmTarget target = targetList.get(0);
		return target.getStart().relative(direction.getOpposite());
	}

	@Override
	public int getExtents(Direction direction, BlockPos pos) {
		if (!this.lastExtents.contains(direction, pos)) {
            this.lastExtents.put(direction, pos, 0);
			return 0;
		}

		return this.lastExtents.get(direction, pos);
	}

	@Override
	public void setExtents(Direction direction, BlockPos pos, int extend) {
        this.lastExtents.put(direction, pos, extend);
	}

	@Override
	public void cleanExtents(Direction direction) {
        this.lastExtents.row(direction).clear();
	}
}

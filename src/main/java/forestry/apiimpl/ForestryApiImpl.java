package forestry.apiimpl;

import forestry.api.IForestryApi;
import forestry.api.apiculture.hives.IHiveManager;
import forestry.api.arboriculture.ITreeManager;
import forestry.api.core.backpacks.IBackpackInterface;
import forestry.api.core.circuits.ICircuitManager;
import forestry.api.core.climate.IClimateManager;
import forestry.api.core.IErrorManager;
import forestry.api.agriculture.IFarmingManager;
import forestry.api.core.genetics.IFlowerTypeManager;
import forestry.api.core.genetics.IGeneticManager;
import forestry.core.engine.genetics.ForestryFlowerTypeManager;
import forestry.api.core.genetics.filter.IFilterManager;
import forestry.api.core.genetics.pollen.IPollenManager;
import forestry.api.modules.IModuleManager;
import forestry.apiimpl.fake.FakeFarmingManager;
import forestry.core.content.backpacks.BackpackInterface;
import forestry.core.engine.circuits.CircuitManager;
import forestry.core.engine.climate.ForestryClimateManager;
import forestry.core.platform.errors.ErrorManager;
import forestry.modules.ForestryModuleManager;
import forestry.core.content.sorting.FilterManager;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

public class ForestryApiImpl implements IForestryApi {
	private final IModuleManager moduleManager = new ForestryModuleManager();
	// Farms is the one optional jar with a manager, so this starts at the no-op and ModuleFarming
	// overwrites it. See IForestryModule.installManagers. Hives and trees are base content and are
	// always installed, so they stay null until they are, and asking early is a bug rather than an
	// empty world
	private IFarmingManager farmingManager = FakeFarmingManager.INSTANCE;
	private final IClimateManager biomeManager = new ForestryClimateManager();
	@Nullable
	private IHiveManager hiveManager;
	@Nullable
	private ITreeManager treeManager;
	@Nullable
	private IGeneticManager geneticManager;
	// Flower types are shared by bees and butterflies, so base always owns a real one, ready before
	// anything can ask for it
	private final ForestryFlowerTypeManager flowerTypeManager = new ForestryFlowerTypeManager();
	@Nullable
	private IErrorManager errorManager;
	@Nullable
	private IFilterManager filterManager;
	@Nullable
	private ICircuitManager circuitManager;
	@Nullable
	private IPollenManager pollenManager;
	private final IBackpackInterface backpackInterface = new BackpackInterface();

	@Override
	public IModuleManager getModuleManager() {
		return this.moduleManager;
	}

	@Override
	public IFarmingManager getFarmingManager() {
		return this.farmingManager;
	}

	@Override
	public IErrorManager getErrorManager() {
		IErrorManager manager = this.errorManager;
		if (manager == null) {
			throw new IllegalStateException("IErrorManager not initialized yet");
		}
		return manager;
	}

	@Override
	public IClimateManager getClimateManager() {
		return this.biomeManager;
	}

	@Override
	public IHiveManager getHiveManager() {
		IHiveManager manager = this.hiveManager;
		if (manager == null) {
			throw new IllegalStateException("IHiveManager not initialized yet");
		}
		return manager;
	}

	@Override
	public ITreeManager getTreeManager() {
		ITreeManager manager = this.treeManager;
		if (manager == null) {
			throw new IllegalStateException("ITreeManager not initialized yet");
		}
		return manager;
	}

	@Override
	public IFilterManager getFilterManager() {
		IFilterManager manager = this.filterManager;
		if (manager == null) {
			throw new IllegalStateException("IFilterManager not initialized yet. Wait until after item registration has finished");
		}
		return manager;
	}

	@Override
	public IFlowerTypeManager getFlowerTypeManager() {
		return this.flowerTypeManager;
	}

	@Override
	public IGeneticManager getGeneticManager() {
		IGeneticManager manager = this.geneticManager;
		if (manager == null) {
			throw new IllegalStateException("IGeneticManager not initialized yet");
		}
		return this.geneticManager;
	}

	@Override
	public ICircuitManager getCircuitManager() {
		ICircuitManager manager = this.circuitManager;
		if (manager == null) {
			throw new IllegalStateException("ICircuitManager not initialized yet. Wait until after item registration has finished");
		}
		return manager;
	}

	@Override
	public IPollenManager getPollenManager() {
		IPollenManager manager = this.pollenManager;
		if (manager == null) {
			throw new IllegalStateException("IPollenManager not initialized yet");
		}
		return manager;
	}

	@Override
	public IBackpackInterface getBackpackInterface() {
		return this.backpackInterface;
	}

	@ApiStatus.Internal
	public void setCircuitManager(CircuitManager circuitManager) {
		this.circuitManager = circuitManager;
	}

	@ApiStatus.Internal
	public void setErrorManager(ErrorManager errorManager) {
		this.errorManager = errorManager;
	}

	@ApiStatus.Internal
	public void setGeneticManager(GeneticManager geneticManager) {
		this.geneticManager = geneticManager;
	}

	@ApiStatus.Internal
	public void setFilterManager(FilterManager filterManager) {
		this.filterManager = filterManager;
	}

	@ApiStatus.Internal
	public void setFarmingManager(IFarmingManager farmingManager) {
		this.farmingManager = farmingManager;
	}

	@ApiStatus.Internal
	public void setHiveManager(IHiveManager hiveManager) {
		this.hiveManager = hiveManager;
	}

	@ApiStatus.Internal
	public void setTreeManager(ITreeManager treeManager) {
		this.treeManager = treeManager;
	}

	@ApiStatus.Internal
	public void setPollenManager(IPollenManager pollenManager) {
		this.pollenManager = pollenManager;
	}
}

package forestry.apiimpl;

import forestry.api.IForestryApi;
import forestry.api.apiculture.hives.IHiveManager;
import forestry.api.arboriculture.ITreeManager;
import forestry.arboriculture.TreeManager;
import forestry.api.circuits.ICircuitManager;
import forestry.api.climate.IClimateManager;
import forestry.api.core.IErrorManager;
import forestry.api.farming.IFarmingManager;
import forestry.api.genetics.IGeneticManager;
import forestry.api.genetics.alleles.IAlleleManager;
import forestry.api.genetics.filter.IFilterManager;
import forestry.api.genetics.pollen.IPollenManager;
import forestry.api.modules.IModuleManager;
import forestry.apiculture.hives.HiveManager;
import forestry.core.circuits.CircuitManager;
import forestry.core.climate.ForestryClimateManager;
import forestry.core.errors.ErrorManager;
import forestry.core.genetics.alleles.AlleleManager;
import forestry.farming.FarmingManager;
import forestry.modules.ForestryModuleManager;
import forestry.sorting.FilterManager;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

public class ForestryApiImpl implements IForestryApi {
	private final IModuleManager moduleManager = new ForestryModuleManager();
	@Nullable
	private IFarmingManager farmingManager;
	private final IClimateManager biomeManager = new ForestryClimateManager();
	@Nullable
	private IHiveManager hiveManager;
	@Nullable
	private ITreeManager treeManager;
	private final IAlleleManager alleleRegistry = new AlleleManager();
	@Nullable
	private IGeneticManager geneticManager;
	@Nullable
	private IErrorManager errorManager;
	@Nullable
	private IFilterManager filterManager;
	@Nullable
	private ICircuitManager circuitManager;
	@Nullable
	private IPollenManager pollenManager;

	@Override
	public IModuleManager getModuleManager() {
		return this.moduleManager;
	}

	@Override
	public IFarmingManager getFarmingManager() {
		IFarmingManager manager = this.farmingManager;
		if (manager == null) {
			throw new IllegalStateException("IFarmingManager not initialized yet");
		}
		return manager;
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
	public IAlleleManager getAlleleManager() {
		return this.alleleRegistry;
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
	public void setFarmingManager(FarmingManager farmingManager) {
		this.farmingManager = farmingManager;
	}

	@ApiStatus.Internal
	public void setHiveManager(HiveManager hiveManager) {
		this.hiveManager = hiveManager;
	}

	@ApiStatus.Internal
	public void setTreeManager(TreeManager treeManager) {
		this.treeManager = treeManager;
	}

	@ApiStatus.Internal
	public void setPollenManager(IPollenManager pollenManager) {
		this.pollenManager = pollenManager;
	}
}

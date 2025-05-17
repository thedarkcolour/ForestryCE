package forestry.api;

import java.util.ServiceLoader;

import forestry.api.apiculture.hives.IHiveManager;
import forestry.api.arboriculture.ITreeManager;
import forestry.api.circuits.ICircuitManager;
import forestry.api.climate.IClimateManager;
import forestry.api.core.IErrorManager;
import forestry.api.farming.IFarmingManager;
import forestry.api.genetics.IGeneticManager;
import forestry.api.genetics.alleles.IAlleleManager;
import forestry.api.genetics.filter.IFilterManager;
import forestry.api.genetics.pollen.IPollenManager;
import forestry.api.modules.IModuleManager;
import forestry.api.plugin.IGeneticRegistration;

/**
 * The Forestry API class is used to query all sorts of data used by Forestry.
 */
public interface IForestryApi {
	IForestryApi INSTANCE = ServiceLoader.load(IForestryApi.class).findFirst().orElseThrow();

	IModuleManager getModuleManager();

	IFarmingManager getFarmingManager();

	/**
	 * @see forestry.api.plugin.IForestryPlugin#registerErrors
	 */
	IErrorManager getErrorManager();

	IClimateManager getClimateManager();

	/**
	 * @see forestry.api.plugin.IApicultureRegistration#registerHive
	 */
	IHiveManager getHiveManager();

	/**
	 * @since 2.6.0
	 */
	ITreeManager getTreeManager();

	/**
	 * @return The genetic manager, used to track taxonomy, mutations, species types, and registered species.
	 * @see forestry.api.plugin.IForestryPlugin#registerGenetics
	 */
	IGeneticManager getGeneticManager();

	/**
	 * @return The allele manager, used to manage allele instances and ensures that there is at most one allele instance representing a certain value.
	 * Also used to create and register chromosomes as well as providing the allele codec.
	 * @see forestry.api.genetics.alleles.ForestryAlleles
	 */
	IAlleleManager getAlleleManager();

	/**
	 * @see IGeneticRegistration#registerFilterRuleType
	 */
	IFilterManager getFilterManager();

	/**
	 * @see forestry.api.plugin.IForestryPlugin#registerCircuits
	 */
	ICircuitManager getCircuitManager();

	IPollenManager getPollenManager();
}

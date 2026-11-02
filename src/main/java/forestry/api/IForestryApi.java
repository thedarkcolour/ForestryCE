package forestry.api;

import forestry.api.apiculture.hives.IHiveManager;
import forestry.api.arboriculture.ITreeManager;
import forestry.api.core.backpacks.IBackpackInterface;
import forestry.api.core.circuits.ICircuitManager;
import forestry.api.core.climate.IClimateManager;
import forestry.api.core.IErrorManager;
import forestry.api.agriculture.IFarmingManager;
import forestry.api.core.genetics.IFlowerTypeManager;
import forestry.api.core.genetics.IGeneticManager;
import forestry.api.core.genetics.filter.IFilterManager;
import forestry.api.core.genetics.pollen.IPollenManager;
import forestry.api.modules.IModuleManager;
import forestry.api.plugin.IGeneticRegistration;

import java.util.ServiceLoader;

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
	 * @return The flower type manager, used to look up the flower types pollinating species search for
	 * @since 2.10.0
	 */
	IFlowerTypeManager getFlowerTypeManager();

	/**
	 * @see IGeneticRegistration#registerFilterRuleType
	 */
	IFilterManager getFilterManager();

	/**
	 * @see forestry.api.plugin.IForestryPlugin#registerCircuits
	 */
	ICircuitManager getCircuitManager();

	IPollenManager getPollenManager();

	/**
	 * @return The backpack interface, used to create backpack items and backpack filters
	 * @since 3.0.0
	 */
	IBackpackInterface getBackpackInterface();
}

package forestry.api.plugin;

import java.util.function.Consumer;

import net.minecraft.resources.ResourceLocation;

import forestry.api.client.plugin.IClientRegistration;

/**
 * Entry point for registering things to the Forestry API. Forestry uses {@link java.util.ServiceLoader} to
 * load IForestryPlugin classes, so the mod jar must include a file named:
 *
 * <pre>{@code
 *     META-INF/services/forestry.api.plugin.IForestryPlugin
 * }</pre>
 * <p>
 * With a line for each IForestryPlugin class to register:
 *
 * <pre>{@code
 *     com.example.examplemod.compat.ExampleForestryPlugin  # You can list multiple plugins
 *     com.example.examplemod.compat.AnotherExampleForestryPlugin
 * }</pre>
 *
 * @see java.util.ServiceLoader For how to implement a service provider for the IForestryPlugin service
 */
public interface IForestryPlugin {
	/**
	 * Called before registerApiculture, registerArboriculture, and registerLepidopterology to set up core genetic data.
	 */
	default void registerGenetics(IGeneticRegistration genetics) {
	}

	/**
	 * Override to register bee species, effects, flower types, hives, etc. at the correct time. Called immediately after item registry.
	 */
	default void registerApiculture(IApicultureRegistration apiculture) {
	}

	/**
	 * Override to register tree species, fruits, etc. at the correct time. Called immediately after item registry.
	 */
	default void registerArboriculture(IArboricultureRegistration arboriculture) {
	}

	/**
	 * Override to register butterfly species at the correct time. Called immediately after item registry.
	 */
	default void registerLepidopterology(ILepidopterologyRegistration lepidopterology) {
	}

	/**
	 * Called after all species are registered. Register circuits and circuit layout types heere.
	 */
	default void registerCircuits(ICircuitRegistration circuits) {
	}

	default void registerErrors(IErrorRegistration errors) {
	}

	default void registerFarming(IFarmingRegistration farming) {
	}

	/**
	 * Called after all species are registered. Register new pollen types here.
	 */
	default void registerPollen(IPollenRegistration pollen) {
	}

	/**
	 * Called after all species are registered to register client-only resources for species.
	 *
	 * @param registrar Accepts a class that handles client registration. Consumer is for maintaining side-safety.
	 */
	default void registerClient(Consumer<Consumer<IClientRegistration>> registrar) {
	}

	/**
	 * @return Unique ID for this plugin.
	 */
	ResourceLocation id();

	/**
	 * @return Whether this plugin should have any of its registerGenetics, registerApiculture, etc. methods called.
	 */
	default boolean shouldLoad() {
		return true;
	}
}

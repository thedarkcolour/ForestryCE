/*******************************************************************************
 * Copyright 2011-2014 SirSengir
 *
 * This work (the API) is licensed under the "MIT" License, see LICENSE.txt for details.
 ******************************************************************************/
package forestry.api.modules;

import java.util.List;
import java.util.function.Consumer;

import net.minecraft.resources.ResourceLocation;

import net.minecraftforge.eventbus.api.IEventBus;

import forestry.api.client.IClientModuleHandler;

/**
 * IF YOU WANT TO ADD BEE SPECIES, FORESTRY COMPATIBILITY, ETC. USE A {@link forestry.api.plugin.IForestryPlugin}.
 * <p>
 * The entry point for a Forestry module. Your mod probably doesn't need this, but it's here if you want to use it.
 * Must be annotated by {@link ForestryModule} to be loaded and must have an empty constructor.
 */
public interface IForestryModule {
	/**
	 * @return The unique identifier for this module. The namespace should be the modid of the mod adding this module.
	 */
	ResourceLocation getId();

	/**
	 * @return A list of identifiers of the modules this module requires in order to load (Apiculture, Mail, etc.)
	 */
	default List<ResourceLocation> getModuleDependencies() {
		return List.of();
	}

	/**
	 * @return A list of identifiers of the mods this module requires in order to load (IC2, BuildCraft, etc.)
	 */
	default List<String> getModDependencies() {
		return List.of();
	}

	/**
	 * Called during mod construction, allowing modules to subscribe to mod bus events using their mod's event bus.
	 * For client-only events, use {@link IForestryModule#registerClientHandler} and {@link IClientModuleHandler#registerEvents}.
	 *
	 * @param modBus The mod-specific event bus for the mod found from the namespace of {@link #getId()}.
	 */
	default void registerEvents(IEventBus modBus) {
	}

	/**
	 * Runs at mod construction on the logical client, after {@link #registerEvents}.
	 */
	default void registerClientHandler(Consumer<IClientModuleHandler> registrar) {
	}

	/**
	 * Note: this is generally not recommended to use.
	 * Called during Forestry's common phase. Initialize any static API.
	 */
	@Deprecated
	default void setupApi() {
	}

	default void registerPackets(IPacketRegistry registry) {
	}

	/**
	 * @return If this module is a "core" module, a dependency of all other modules added by this mod. Loads before other modules.
	 */
	default boolean isCore() {
		return false;
	}
}

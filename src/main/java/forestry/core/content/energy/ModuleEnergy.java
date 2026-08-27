package forestry.core.content.energy;

import forestry.api.client.IClientModuleHandler;
import forestry.api.modules.ForestryModule;
import forestry.api.modules.ForestryModuleIds;
import forestry.core.content.energy.client.EnergyClientHandler;
import forestry.core.content.energy.features.EnergyTiles;
import forestry.modules.BlankForestryModule;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

import java.util.function.Consumer;
import forestry.api.ForestryDataMaps;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;

@ForestryModule
public class ModuleEnergy extends BlankForestryModule {
	private static void registerCapabilities(RegisterCapabilitiesEvent event) {
		event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, EnergyTiles.CLOCKWORK_ENGINE.tileType(), (tile, side) -> tile.getEnergyHandler(side));
		event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, EnergyTiles.BIOGAS_ENGINE.tileType(), (tile, side) -> tile.getEnergyHandler(side));
		event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, EnergyTiles.PEAT_ENGINE.tileType(), (tile, side) -> tile.getEnergyHandler(side));
		// Deviation from 1.20.1: capabilities are registered here instead of overriding getCapability on the tile.
		event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, EnergyTiles.SOLAR_ENGINE.tileType(), (tile, side) -> tile.getEnergyHandler(side));
		event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, EnergyTiles.COMBUSTION_ENGINE.tileType(), (tile, side) -> tile.getEnergyHandler(side));
		event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, EnergyTiles.BIOGAS_ENGINE.tileType(), (tile, side) -> tile.getFluidHandler(side));
		event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, EnergyTiles.COMBUSTION_ENGINE.tileType(), (tile, side) -> tile.getFluidHandler(side));
	}

	@Override
	public ResourceLocation getId() {
		return ForestryModuleIds.ENERGY;
	}

	@Override
	public void registerEvents(IEventBus modBus) {
		modBus.addListener(ModuleEnergy::registerCapabilities);
		modBus.addListener(ModuleEnergy::registerDataMaps);
	}

	private static void registerDataMaps(RegisterDataMapTypesEvent event) {
		event.register(ForestryDataMaps.BIOGAS_FUELS);
		event.register(ForestryDataMaps.COMBUSTION_FUELS);
		event.register(ForestryDataMaps.COMBUSTION_COOLANTS);
		event.register(ForestryDataMaps.PEAT_FUELS);
	}

	@Override
	public void registerClientHandler(Consumer<IClientModuleHandler> registrar) {
		registrar.accept(new EnergyClientHandler());
	}
}

package forestry.energy;

import forestry.api.client.IClientModuleHandler;
import forestry.api.modules.ForestryModule;
import forestry.api.modules.ForestryModuleIds;
import forestry.energy.client.EnergyClientHandler;
import forestry.energy.features.EnergyTiles;
import forestry.energy.tiles.BiogasEngineBlockEntity;
import forestry.energy.tiles.PeatEngineBlockEntity;
import forestry.modules.BlankForestryModule;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

import java.util.function.Consumer;

@ForestryModule
public class ModuleEnergy extends BlankForestryModule {
	@Override
	public ResourceLocation getId() {
		return ForestryModuleIds.ENERGY;
	}

	@Override
	public void registerEvents(IEventBus modBus) {
		modBus.addListener(ModuleEnergy::registerCapabilities);
	}

	private static void registerCapabilities(RegisterCapabilitiesEvent event) {
		event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, EnergyTiles.BIOGAS_ENGINE.tileType(), (tile, facing) -> tile.getEnergyManager());
		event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, EnergyTiles.BIOGAS_ENGINE.tileType(), (tile, facing) -> tile.getTankManager());
		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, EnergyTiles.BIOGAS_ENGINE.tileType(), BiogasEngineBlockEntity::getInventory);

		event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, EnergyTiles.CLOCKWORK_ENGINE.tileType(), (tile, facing) -> tile.getEnergyManager());

		event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, EnergyTiles.PEAT_ENGINE.tileType(), (tile, facing) -> tile.getEnergyManager());
		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, EnergyTiles.PEAT_ENGINE.tileType(), PeatEngineBlockEntity::getInventory);
	}

	@Override
	public void registerClientHandler(Consumer<IClientModuleHandler> registrar) {
		registrar.accept(new EnergyClientHandler());
	}
}

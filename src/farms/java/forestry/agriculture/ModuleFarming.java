package forestry.agriculture;

import forestry.agriculture.client.FarmingClientHandler;
import forestry.agriculture.farmlogic.FarmingManager;
import forestry.agriculture.features.FarmingBlockEntities;
import forestry.agriculture.minifarm.tiles.AbstractMinifarmBlockEntity;
import forestry.agriculture.plugin.FarmingRegistration;
import forestry.api.ForestryDataMaps;
import forestry.api.IForestryApi;
import forestry.api.client.IClientModuleHandler;
import forestry.api.modules.ForestryModule;
import forestry.api.modules.ForestryModuleIds;
import forestry.api.plugin.IForestryPlugin;
import forestry.apiimpl.ForestryApiImpl;
import forestry.apiimpl.plugin.PluginManager;
import forestry.core.platform.tile.ForestryBlockEntity;
import forestry.core.platform.tile.PoweredBlockEntity;
import forestry.modules.BlankForestryModule;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;

import java.util.function.Consumer;

@ForestryModule
public class ModuleFarming extends BlankForestryModule {
	@Override
	public ResourceLocation getId() {
		return ForestryModuleIds.FARMING;
	}

	@Override
	public void registerEvents(IEventBus modBus) {
		modBus.addListener(ModuleFarming::registerCapabilities);
		modBus.addListener(ModuleFarming::registerDataMaps);
	}

	private static void registerCapabilities(RegisterCapabilitiesEvent event) {
		event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, FarmingBlockEntities.GEARBOX.tileType(), (tile, side) -> tile.getEnergyHandler(side));
		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, FarmingBlockEntities.HATCH.tileType(), (tile, side) -> tile.getItemHandler(side));
		event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, FarmingBlockEntities.VALVE.tileType(), (tile, side) -> tile.getFluidHandler(side));

		registerMinifarmCapabilities(event, FarmingBlockEntities.ARBORETUM.tileType());
		registerMinifarmCapabilities(event, FarmingBlockEntities.BOG.tileType());
		registerMinifarmCapabilities(event, FarmingBlockEntities.CROPS.tileType());
		registerMinifarmCapabilities(event, FarmingBlockEntities.ENDER.tileType());
		registerMinifarmCapabilities(event, FarmingBlockEntities.GOURD.tileType());
		registerMinifarmCapabilities(event, FarmingBlockEntities.MUSHROOM.tileType());
		registerMinifarmCapabilities(event, FarmingBlockEntities.NETHER.tileType());
	}

	private static <T extends AbstractMinifarmBlockEntity> void registerMinifarmCapabilities(RegisterCapabilitiesEvent event, BlockEntityType<T> type) {
		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, type, ForestryBlockEntity::getItemHandler);
		event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, type, PoweredBlockEntity::getEnergyHandler);
		event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, type, AbstractMinifarmBlockEntity::getFluidHandler);
	}

	private static void registerDataMaps(RegisterDataMapTypesEvent event) {
		event.register(ForestryDataMaps.FARM_FERTILIZERS);
	}

	@Override
	public void applyPluginRegistration() {
		FarmingRegistration registration = new FarmingRegistration();

		for (IForestryPlugin plugin : PluginManager.getLoadedPlugins()) {
			try {
				plugin.registerFarming(registration);
			} catch (Throwable t) {
				throw new RuntimeException("An error was thrown by plugin " + plugin.id() + " during IForestryPlugin.registerFarming", t);
			}
		}

		// Defensive copy of fertilizers
		FarmingManager manager = new FarmingManager(registration.buildFarmTypes());

		((ForestryApiImpl) IForestryApi.INSTANCE).setFarmingManager(manager);
	}

	@Override
	public void registerClientHandler(Consumer<IClientModuleHandler> registrar) {
		registrar.accept(new FarmingClientHandler());
	}
}

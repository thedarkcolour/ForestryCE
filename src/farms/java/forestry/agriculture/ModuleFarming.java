package forestry.agriculture;

import forestry.api.IForestryApi;
import forestry.api.client.IClientModuleHandler;
import forestry.api.modules.ForestryModule;
import forestry.api.modules.ForestryModuleIds;
import forestry.api.plugin.IForestryPlugin;
import forestry.apiimpl.ForestryApiImpl;
import forestry.apiimpl.plugin.PluginManager;
import forestry.agriculture.client.MultifarmClientHandler;
import forestry.agriculture.farmlogic.FarmingManager;
import forestry.agriculture.features.MultifarmBlockEntities;
import forestry.agriculture.plugin.FarmingRegistration;
import forestry.modules.BlankForestryModule;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

import java.util.function.Consumer;
import forestry.api.ForestryDataMaps;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;

@ForestryModule
public class ModuleFarming extends BlankForestryModule {
	private static void registerCapabilities(RegisterCapabilitiesEvent event) {
		event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, MultifarmBlockEntities.GEARBOX.tileType(), (tile, side) -> tile.getEnergyHandler(side));
		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, MultifarmBlockEntities.HATCH.tileType(), (tile, side) -> tile.getItemHandler(side));
		event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, MultifarmBlockEntities.VALVE.tileType(), (tile, side) -> tile.getFluidHandler(side));
	}

	@Override
	public ResourceLocation getId() {
		return ForestryModuleIds.FARMING;
	}

	@Override
	public void registerEvents(IEventBus modBus) {
		modBus.addListener(ModuleFarming::registerCapabilities);
		modBus.addListener(ModuleFarming::registerDataMaps);
	}

	private static void registerDataMaps(RegisterDataMapTypesEvent event) {
		event.register(ForestryDataMaps.FARM_FERTILIZERS);
	}

	@Override
	public void installManagers() {
		FarmingRegistration registration = new FarmingRegistration();

		for (IForestryPlugin plugin : PluginManager.getLoadedPlugins()) {
			try {
				plugin.registerFarming(registration);
			} catch (Throwable t) {
				throw new RuntimeException("An error was thrown by plugin " + plugin.id() + " during IForestryPlugin.registerFarming", t);
			}
		}

		// Defensive copy of fertilizers
		FarmingManager manager = new FarmingManager(new Object2IntOpenHashMap<>(registration.getFertilizers()), registration.buildFarmTypes());

		((ForestryApiImpl) IForestryApi.INSTANCE).setFarmingManager(manager);
	}

	@Override
	public void registerClientHandler(Consumer<IClientModuleHandler> registrar) {
		registrar.accept(new MultifarmClientHandler());
	}
}

package forestry.core.content.machines;

import forestry.api.client.IClientModuleHandler;
import forestry.api.modules.ForestryModule;
import forestry.api.modules.ForestryModuleIds;
import forestry.api.modules.IPacketRegistry;
import forestry.core.platform.network.PacketIdClient;
import forestry.core.platform.network.PacketIdServer;
import forestry.core.content.machines.client.FactoryClientHandler;
import forestry.core.content.machines.features.FactoryTiles;
import forestry.core.content.machines.network.packets.PacketRecipeTransferRequest;
import forestry.core.content.machines.network.packets.PacketRecipeTransferUpdate;
import forestry.modules.BlankForestryModule;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

import java.util.function.Consumer;
import forestry.api.ForestryDataMaps;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;

@ForestryModule
public class ModuleFactory extends BlankForestryModule {
	private static void registerCapabilities(RegisterCapabilitiesEvent event) {
		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, FactoryTiles.BOTTLER.tileType(), (tile, side) -> tile.getItemHandler(side));
		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, FactoryTiles.CARPENTER.tileType(), (tile, side) -> tile.getItemHandler(side));
		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, FactoryTiles.CENTRIFUGE.tileType(), (tile, side) -> tile.getItemHandler(side));
		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, FactoryTiles.FABRICATOR.tileType(), (tile, side) -> tile.getItemHandler(side));
		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, FactoryTiles.FERMENTER.tileType(), (tile, side) -> tile.getItemHandler(side));
		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, FactoryTiles.RAINMAKER.tileType(), (tile, side) -> tile.getItemHandler(side));
		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, FactoryTiles.MOISTENER.tileType(), (tile, side) -> tile.getItemHandler(side));
		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, FactoryTiles.SMELTER.tileType(), (tile, side) -> tile.getItemHandler(side));
		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, FactoryTiles.SQUEEZER.tileType(), (tile, side) -> tile.getItemHandler(side));
		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, FactoryTiles.STILL.tileType(), (tile, side) -> tile.getItemHandler(side));

		event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, FactoryTiles.BOTTLER.tileType(), (tile, side) -> tile.getEnergyHandler(side));
		event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, FactoryTiles.CARPENTER.tileType(), (tile, side) -> tile.getEnergyHandler(side));
		event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, FactoryTiles.CENTRIFUGE.tileType(), (tile, side) -> tile.getEnergyHandler(side));
		event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, FactoryTiles.FABRICATOR.tileType(), (tile, side) -> tile.getEnergyHandler(side));
		event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, FactoryTiles.FERMENTER.tileType(), (tile, side) -> tile.getEnergyHandler(side));
		event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, FactoryTiles.SMELTER.tileType(), (tile, side) -> tile.getEnergyHandler(side));
		event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, FactoryTiles.SQUEEZER.tileType(), (tile, side) -> tile.getEnergyHandler(side));
		event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, FactoryTiles.STILL.tileType(), (tile, side) -> tile.getEnergyHandler(side));

		event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, FactoryTiles.BOTTLER.tileType(), (tile, side) -> tile.getFluidHandler(side));
		event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, FactoryTiles.CARPENTER.tileType(), (tile, side) -> tile.getFluidHandler(side));
		event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, FactoryTiles.FABRICATOR.tileType(), (tile, side) -> tile.getFluidHandler(side));
		event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, FactoryTiles.FERMENTER.tileType(), (tile, side) -> tile.getFluidHandler(side));
		event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, FactoryTiles.MOISTENER.tileType(), (tile, side) -> tile.getFluidHandler(side));
		event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, FactoryTiles.SQUEEZER.tileType(), (tile, side) -> tile.getFluidHandler(side));
		event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, FactoryTiles.STILL.tileType(), (tile, side) -> tile.getFluidHandler(side));
	}

	@Override
	public ResourceLocation getId() {
		return ForestryModuleIds.FACTORY;
	}

	@Override
	public void registerEvents(IEventBus modBus) {
		modBus.addListener(ModuleFactory::registerCapabilities);
		modBus.addListener(ModuleFactory::registerDataMaps);
	}

	private static void registerDataMaps(RegisterDataMapTypesEvent event) {
		event.register(ForestryDataMaps.FERMENTER_FUELS);
		event.register(ForestryDataMaps.MOISTENER_FUELS);
		event.register(ForestryDataMaps.RAINMAKER_FUELS);
	}

	@Override
	public void registerClientHandler(Consumer<IClientModuleHandler> registrar) {
		registrar.accept(new FactoryClientHandler());
	}

	@Override
	public void registerPackets(IPacketRegistry registry) {
		registry.serverbound(PacketIdServer.RECIPE_TRANSFER_REQUEST, PacketRecipeTransferRequest::encode, PacketRecipeTransferRequest::decode, PacketRecipeTransferRequest::handle);
		registry.clientbound(PacketIdClient.RECIPE_TRANSFER_UPDATE, PacketRecipeTransferUpdate::encode, PacketRecipeTransferUpdate::decode, PacketRecipeTransferUpdate::handle);
	}
}

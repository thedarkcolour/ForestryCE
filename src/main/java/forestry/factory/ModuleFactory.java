package forestry.factory;

import forestry.api.client.IClientModuleHandler;
import forestry.api.fuels.FermenterFuel;
import forestry.api.fuels.FuelManager;
import forestry.api.fuels.MoistenerFuel;
import forestry.api.modules.ForestryModule;
import forestry.api.modules.ForestryModuleIds;
import forestry.api.modules.IPacketRegistry;
import forestry.core.config.Preference;
import forestry.core.features.CoreItems;
import forestry.core.network.PacketIdClient;
import forestry.core.network.PacketIdServer;
import forestry.core.utils.datastructures.ItemStackMap;
import forestry.factory.client.FactoryClientHandler;
import forestry.factory.features.FactoryTiles;
import forestry.factory.network.packets.PacketRecipeTransferRequest;
import forestry.factory.network.packets.PacketRecipeTransferUpdate;
import forestry.modules.BlankForestryModule;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

import java.util.function.Consumer;

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
		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, FactoryTiles.RAIN_TANK.tileType(), (tile, side) -> tile.getItemHandler(side));
		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, FactoryTiles.SQUEEZER.tileType(), (tile, side) -> tile.getItemHandler(side));
		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, FactoryTiles.STILL.tileType(), (tile, side) -> tile.getItemHandler(side));

		event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, FactoryTiles.BOTTLER.tileType(), (tile, side) -> tile.getEnergyHandler(side));
		event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, FactoryTiles.CARPENTER.tileType(), (tile, side) -> tile.getEnergyHandler(side));
		event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, FactoryTiles.CENTRIFUGE.tileType(), (tile, side) -> tile.getEnergyHandler(side));
		event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, FactoryTiles.FABRICATOR.tileType(), (tile, side) -> tile.getEnergyHandler(side));
		event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, FactoryTiles.FERMENTER.tileType(), (tile, side) -> tile.getEnergyHandler(side));
		event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, FactoryTiles.SQUEEZER.tileType(), (tile, side) -> tile.getEnergyHandler(side));
		event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, FactoryTiles.STILL.tileType(), (tile, side) -> tile.getEnergyHandler(side));

		event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, FactoryTiles.BOTTLER.tileType(), (tile, side) -> tile.getFluidHandler(side));
		event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, FactoryTiles.CARPENTER.tileType(), (tile, side) -> tile.getFluidHandler(side));
		event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, FactoryTiles.FABRICATOR.tileType(), (tile, side) -> tile.getFluidHandler(side));
		event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, FactoryTiles.FERMENTER.tileType(), (tile, side) -> tile.getFluidHandler(side));
		event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, FactoryTiles.MOISTENER.tileType(), (tile, side) -> tile.getFluidHandler(side));
		event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, FactoryTiles.RAIN_TANK.tileType(), (tile, side) -> tile.getFluidHandler(side));
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
	}

	@Override
	public void registerClientHandler(Consumer<IClientModuleHandler> registrar) {
		registrar.accept(new FactoryClientHandler());
	}

	@Override
	public void setupApi() {
		FuelManager.fermenterFuel = new ItemStackMap<>();
		FuelManager.moistenerResource = new ItemStackMap<>();

		// Set fuels and resources for the fermenter
		ItemStack fertilizerCompound = CoreItems.FERTILIZER_COMPOUND.stack();
		FuelManager.fermenterFuel.put(fertilizerCompound, new FermenterFuel(fertilizerCompound,
			Preference.FERMENTED_CYCLE_FERTILIZER, Preference.FERMENTATION_DURATION_FERTILIZER));

		int cyclesCompost = Preference.FERMENTATION_DURATION_COMPOST;
		int valueCompost = Preference.FERMENTED_CYCLE_COMPOST;
		ItemStack fertilizerBio = CoreItems.COMPOST.stack();
		ItemStack mulch = CoreItems.MULCH.stack();
		FuelManager.fermenterFuel.put(fertilizerBio, new FermenterFuel(fertilizerBio, valueCompost, cyclesCompost));
		FuelManager.fermenterFuel.put(mulch, new FermenterFuel(mulch, valueCompost, cyclesCompost));

		// Add moistener resources
		ItemStack wheat = new ItemStack(Items.WHEAT);
		ItemStack mouldyWheat = CoreItems.MOULDY_WHEAT.stack();
		ItemStack decayingWheat = CoreItems.DECAYING_WHEAT.stack();
		FuelManager.moistenerResource.put(wheat, new MoistenerFuel(wheat, mouldyWheat, 0, 300));
		FuelManager.moistenerResource.put(mouldyWheat, new MoistenerFuel(mouldyWheat, decayingWheat, 1, 600));
		FuelManager.moistenerResource.put(decayingWheat, new MoistenerFuel(decayingWheat, mulch, 2, 900));
	}

	@Override
	public void registerPackets(IPacketRegistry registry) {
		registry.serverbound(PacketIdServer.RECIPE_TRANSFER_REQUEST, PacketRecipeTransferRequest::encode, PacketRecipeTransferRequest::decode, PacketRecipeTransferRequest::handle);
		registry.clientbound(PacketIdClient.RECIPE_TRANSFER_UPDATE, PacketRecipeTransferUpdate::encode, PacketRecipeTransferUpdate::decode, PacketRecipeTransferUpdate::handle);
	}
}

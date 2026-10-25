package forestry.core.content.sorting;

import forestry.api.client.IClientModuleHandler;
import forestry.api.ForestryCapabilities;
import forestry.api.modules.ForestryModule;
import forestry.api.modules.ForestryModuleIds;
import forestry.api.modules.IPacketRegistry;
import forestry.core.platform.network.PacketIdClient;
import forestry.core.platform.network.PacketIdServer;
import forestry.modules.BlankForestryModule;
import forestry.core.content.sorting.features.SortingBlockEntities;
import forestry.core.content.sorting.client.SortingClientHandler;
import forestry.core.content.sorting.network.packets.PacketFilterChangeGenome;
import forestry.core.content.sorting.network.packets.PacketFilterChangeRule;
import forestry.core.content.sorting.network.packets.PacketGuiFilterUpdate;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

import java.util.function.Consumer;

@ForestryModule
public class ModuleSorting extends BlankForestryModule {
	@Override
	public ResourceLocation getId() {
		return ForestryModuleIds.SORTING;
	}

	@Override
	public void registerEvents(IEventBus modBus) {
		modBus.addListener(ModuleSorting::registerCapabilities);
	}

	@Override
	public void registerPackets(IPacketRegistry registry) {
		registry.serverbound(PacketIdServer.FILTER_CHANGE_RULE, PacketFilterChangeRule::encode, PacketFilterChangeRule::decode, PacketFilterChangeRule::handle);
		registry.serverbound(PacketIdServer.FILTER_CHANGE_GENOME, PacketFilterChangeGenome::encode, PacketFilterChangeGenome::decode, PacketFilterChangeGenome::handle);

		registry.clientbound(PacketIdClient.GUI_UPDATE_FILTER, PacketGuiFilterUpdate::encode, PacketGuiFilterUpdate::decode, PacketGuiFilterUpdate::handle);
	}

	public static void registerCapabilities(RegisterCapabilitiesEvent consumer) {
		consumer.registerBlockEntity(Capabilities.ItemHandler.BLOCK, SortingBlockEntities.GENETIC_FILTER.tileType(), (tile, side) -> tile.getItemHandler(side));
		consumer.registerBlockEntity(ForestryCapabilities.FILTER_LOGIC, SortingBlockEntities.GENETIC_FILTER.tileType(), (tile, side) -> tile.getLogic());
	}

	@Override
	public void registerClientHandler(Consumer<IClientModuleHandler> registrar) {
		registrar.accept(new SortingClientHandler());
	}
}

package forestry.sorting;

import forestry.api.client.IClientModuleHandler;
import forestry.api.genetics.filter.IFilterLogic;
import forestry.api.modules.ForestryModule;
import forestry.api.modules.ForestryModuleIds;
import forestry.core.network.PacketIdClient;
import forestry.core.network.PacketIdServer;
import forestry.modules.BlankForestryModule;
import forestry.sorting.client.SortingClientHandler;
import forestry.sorting.network.packets.PacketFilterChangeGenome;
import forestry.sorting.network.packets.PacketFilterChangeRule;
import forestry.sorting.network.packets.PacketGuiFilterUpdate;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

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
	public void registerPackets(PayloadRegistrar registrar) {
		registrar.serverbound(PacketIdServer.FILTER_CHANGE_RULE, PacketFilterChangeRule.class, PacketFilterChangeRule::decode, PacketFilterChangeRule::handle);
		registrar.serverbound(PacketIdServer.FILTER_CHANGE_GENOME, PacketFilterChangeGenome.class, PacketFilterChangeGenome::decode, PacketFilterChangeGenome::handle);

		registrar.clientbound(PacketIdClient.GUI_UPDATE_FILTER, PacketGuiFilterUpdate.class, PacketGuiFilterUpdate::decode, PacketGuiFilterUpdate::handle);
	}

	public static void registerCapabilities(RegisterCapabilitiesEvent consumer) {
		consumer.register(IFilterLogic.class);
	}

	@Override
	public void registerClientHandler(Consumer<IClientModuleHandler> registrar) {
		registrar.accept(new SortingClientHandler());
	}
}

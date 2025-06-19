package forestry.arboriculture;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import forestry.api.ForestryCapabilities;
import forestry.api.arboriculture.genetics.ITreeSpeciesType;
import forestry.api.arboriculture.genetics.TreeLifeStage;
import forestry.api.client.IClientModuleHandler;
import forestry.api.modules.ForestryModule;
import forestry.api.modules.ForestryModuleIds;
import forestry.arboriculture.capabilities.SpectacleVision;
import forestry.arboriculture.client.ArboricultureClientHandler;
import forestry.arboriculture.commands.CommandTree;
import forestry.arboriculture.features.ArboricultureItems;
import forestry.arboriculture.items.ForestryBoatDispenserBehavior;
import forestry.arboriculture.network.PacketRipeningUpdate;
import forestry.arboriculture.villagers.ArboricultureVillagers;
import forestry.core.features.CoreItems;
import forestry.core.genetics.capability.IndividualHandlerItem;
import forestry.core.network.PacketIdClient;
import forestry.core.utils.SpeciesUtil;
import forestry.modules.BlankForestryModule;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.function.Consumer;

@ForestryModule
public class ModuleArboriculture extends BlankForestryModule {
	@Override
	public ResourceLocation getId() {
		return ForestryModuleIds.ARBORICULTURE;
	}

	@Override
	public void registerEvents(IEventBus modBus) {
		NeoForge.EVENT_BUS.addListener(ArboricultureVillagers::villagerTrades);

		modBus.addListener(ModuleArboriculture::registerCapabilities);
		modBus.addListener(ModuleArboriculture::commonSetup);
	}

	@Override
	public void addToRootCommand(LiteralArgumentBuilder<CommandSourceStack> command) {
		command.then(CommandTree.register());
	}

	private static void registerCapabilities(RegisterCapabilitiesEvent event) {
		event.registerItem(ForestryCapabilities.SPECTACLE_VISION, (stack, v) -> SpectacleVision.INSTANCE, CoreItems.SPECTACLES);

		// Add genetics capabilities to vanilla saplings
		ITreeSpeciesType type = SpeciesUtil.TREE_TYPE.get();
		type.getAllVanillaIndividuals().forEach((item, individual) -> event.registerItem(ForestryCapabilities.INDIVIDUAL_HANDLER_ITEM, (stack, v) -> new IndividualHandlerItem(type, stack, individual, TreeLifeStage.SAPLING), item));
	}

	private static void commonSetup(FMLCommonSetupEvent event) {
		event.enqueueWork(() -> {
			for (ForestryWoodType type : ForestryWoodType.VALUES) {
				DispenserBlock.registerBehavior(ArboricultureItems.BOAT.item(type), new ForestryBoatDispenserBehavior(type, false));
				DispenserBlock.registerBehavior(ArboricultureItems.CHEST_BOAT.item(type), new ForestryBoatDispenserBehavior(type, true));
				WoodType.register(type.getWoodType());
			}
		});
	}

	@Override
	public void registerPackets(PayloadRegistrar registrar) {
		registrar.playToClient(PacketIdClient.RIPENING_UPDATE, StreamCodec.of(PacketRipeningUpdate::encode, PacketRipeningUpdate::decode), PacketRipeningUpdate::handle);
	}

	@Override
	public void registerClientHandler(Consumer<IClientModuleHandler> registrar) {
		registrar.accept(new ArboricultureClientHandler());
	}
}

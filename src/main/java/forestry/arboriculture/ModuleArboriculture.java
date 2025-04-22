/*******************************************************************************
 * Copyright (c) 2011-2014 SirSengir.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Various Contributors including, but not limited to:
 * SirSengir (original work), CovertJaguar, Player, Binnie, MysteriousAges
 ******************************************************************************/
package forestry.arboriculture;

import java.util.Arrays;
import java.util.function.Consumer;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.WoodType;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.eventbus.api.IEventBus;

import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import forestry.api.arboriculture.TreeManager;
import forestry.api.arboriculture.genetics.ITree;
import forestry.api.arboriculture.genetics.ITreeSpeciesType;
import forestry.api.arboriculture.genetics.TreeLifeStage;
import forestry.api.client.IClientModuleHandler;
import forestry.api.core.IArmorNaturalist;
import forestry.api.genetics.IIndividual;
import forestry.api.modules.ForestryModule;
import forestry.api.modules.ForestryModuleIds;
import forestry.api.modules.IPacketRegistry;
import forestry.arboriculture.blocks.BlockDecorativeLeaves;
import forestry.arboriculture.client.ArboricultureClientHandler;
import forestry.arboriculture.commands.CommandTree;
import forestry.arboriculture.features.ArboricultureBlocks;
import forestry.arboriculture.features.ArboricultureItems;
import forestry.arboriculture.items.ForestryBoatDispenserBehavior;
import forestry.arboriculture.network.PacketRipeningUpdate;
import forestry.arboriculture.villagers.ArboricultureVillagers;
import forestry.core.genetics.capability.IndividualHandlerItem;
import forestry.core.network.PacketIdClient;
import forestry.core.utils.SpeciesUtil;
import forestry.modules.BlankForestryModule;
import forestry.modules.features.FeatureBlock;

@ForestryModule
public class ModuleArboriculture extends BlankForestryModule {
	@Override
	public ResourceLocation getId() {
		return ForestryModuleIds.ARBORICULTURE;
	}

	@Override
	public void registerEvents(IEventBus modBus) {
		MinecraftForge.EVENT_BUS.addListener(ArboricultureVillagers::villagerTrades);

		modBus.addListener(ModuleArboriculture::registerCapabilities);
		modBus.addListener(ModuleArboriculture::commonSetup);
		MinecraftForge.EVENT_BUS.addListener(ModuleArboriculture::modifyLeafStateCaches);
		MinecraftForge.EVENT_BUS.addGenericListener(ItemStack.class, ModuleArboriculture::attachCapabilities);
	}

	private static void attachCapabilities(AttachCapabilitiesEvent<ItemStack> event) {
		// Add genetics capabilities to vanilla saplings
		if (!event.getCapabilities().containsKey(IIndividual.CAPABILITY_ID)) {
			ItemStack stack = event.getObject();

			ITreeSpeciesType type = SpeciesUtil.TREE_TYPE.get();
			ITree individual = type.getVanillaIndividual(stack.getItem());

			if (individual != null) {
				event.addCapability(IIndividual.CAPABILITY_ID, new IndividualHandlerItem(type, stack, individual, TreeLifeStage.SAPLING));
			}
		}
	}

	// Called right after Block.rebuildCache to modify BlockStateBase.Cache, preventing fences from connecting to Forestry decorative leaves
	private static void modifyLeafStateCaches(TagsUpdatedEvent event) {
		// Use one copy of cache for all blocks/states to avoid wasting memory
		BlockBehaviour.BlockStateBase.Cache leafCache = ArboricultureBlocks.LEAVES_DECORATIVE.getList().get(0).defaultBlockState().cache;
		Arrays.fill(leafCache.faceSturdy, false);

		for (FeatureBlock<BlockDecorativeLeaves, BlockItem> feature : ArboricultureBlocks.LEAVES_DECORATIVE.getFeatures()) {
			for (BlockState state : feature.block().getStateDefinition().getPossibleStates()) {
				state.cache = leafCache;
			}
		}
	}

	@Override
	public void setupApi() {
		TreeManager.woodAccess = WoodAccess.INSTANCE;
	}

	@Override
	public void addToRootCommand(LiteralArgumentBuilder<CommandSourceStack> command) {
		command.then(CommandTree.register());
	}

	private static void registerCapabilities(RegisterCapabilitiesEvent event) {
		event.register(IArmorNaturalist.class);
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
	public void registerPackets(IPacketRegistry registry) {
		registry.clientbound(PacketIdClient.RIPENING_UPDATE, PacketRipeningUpdate.class, PacketRipeningUpdate::decode, PacketRipeningUpdate::handle);
	}

	@Override
	public void registerClientHandler(Consumer<IClientModuleHandler> registrar) {
		registrar.accept(new ArboricultureClientHandler());
	}
}

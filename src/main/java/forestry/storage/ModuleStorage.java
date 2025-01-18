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

package forestry.storage;

import java.util.function.Consumer;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.IEventBus;

import forestry.api.ForestryTags;
import forestry.api.client.IClientModuleHandler;
import forestry.api.genetics.ForestrySpeciesTypes;
import forestry.api.modules.ForestryModule;
import forestry.api.modules.ForestryModuleIds;
import forestry.api.storage.IBackpackInterface;
import forestry.core.ForestryColors;
import forestry.core.config.ForestryConfig;
import forestry.modules.BlankForestryModule;
import forestry.storage.client.StorageClientHandler;

@ForestryModule
public class ModuleStorage extends BlankForestryModule {
	public static final IBackpackInterface BACKPACK_INTERFACE = new BackpackInterface();

	public static final BackpackDefinition APIARIST = new BackpackDefinition(0xc4923d, ForestryColors.WHITE, BACKPACK_INTERFACE.createNaturalistBackpackFilter(ForestrySpeciesTypes.BEE));
	public static final BackpackDefinition ARBORIST = new BackpackDefinition(0x657e3a, ForestryColors.WHITE, BACKPACK_INTERFACE.createNaturalistBackpackFilter(ForestrySpeciesTypes.TREE));
	public static final BackpackDefinition LEPIDOPTERIST = new BackpackDefinition(0x995b31, ForestryColors.WHITE, BACKPACK_INTERFACE.createNaturalistBackpackFilter(ForestrySpeciesTypes.BUTTERFLY));
	public static final BackpackDefinition MINER = new BackpackDefinition(0x36187d, ForestryColors.WHITE, new BackpackFilter(ForestryTags.Items.MINER_ALLOW, ForestryTags.Items.MINER_REJECT));
	public static final BackpackDefinition DIGGER = new BackpackDefinition(0x363cc5, ForestryColors.WHITE, new BackpackFilter(ForestryTags.Items.DIGGER_ALLOW, ForestryTags.Items.DIGGER_REJECT));
	public static final BackpackDefinition FORESTER = new BackpackDefinition(0x347427, ForestryColors.WHITE, new BackpackFilter(ForestryTags.Items.FORESTER_ALLOW, ForestryTags.Items.FORESTER_REJECT));
	public static final BackpackDefinition HUNTER = new BackpackDefinition(0x412215, ForestryColors.WHITE, new BackpackFilter(ForestryTags.Items.HUNTER_ALLOW, ForestryTags.Items.HUNTER_REJECT));
	public static final BackpackDefinition ADVENTURER = new BackpackDefinition(0x7fb8c2, ForestryColors.WHITE, new BackpackFilter(ForestryTags.Items.ADVENTURER_ALLOW, ForestryTags.Items.ADVENTURER_REJECT));
	public static final BackpackDefinition BUILDER = new BackpackDefinition(0xdd3a3a, ForestryColors.WHITE, new BackpackFilter(ForestryTags.Items.BUILDER_ALLOW, ForestryTags.Items.BUILDER_REJECT));

	@Override
	public ResourceLocation getId() {
		return ForestryModuleIds.STORAGE;
	}

	@Override
	public void registerEvents(IEventBus modBus) {
		MinecraftForge.EVENT_BUS.addListener(ModuleStorage::onItemPickup);
		MinecraftForge.EVENT_BUS.addListener(ModuleStorage::onLevelTick);
	}

	private static void onLevelTick(TickEvent.LevelTickEvent event) {
		// todo use register/unregister on the IEventBus
		if (ForestryConfig.SERVER.enableBackpackResupply.get()) {
			if (event.phase == TickEvent.Phase.END) {
				for (Player player : event.level.players()) {
					BackpackResupplyHandler.resupply(player);
				}
			}
		}
	}

	private static void onItemPickup(EntityItemPickupEvent event) {
		if (event.isCanceled() || event.getResult() == Event.Result.ALLOW) {
			return;
		}

		if (PickupHandlerStorage.onItemPickup(event.getEntity(), event.getItem())) {
			event.setResult(Event.Result.ALLOW);
		}
	}

	@Override
	public void registerClientHandler(Consumer<IClientModuleHandler> registrar) {
		registrar.accept(new StorageClientHandler());
	}
}

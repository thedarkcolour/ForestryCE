package forestry.storage;

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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.function.Consumer;

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
		NeoForge.EVENT_BUS.addListener(ModuleStorage::onItemPickup);
		NeoForge.EVENT_BUS.addListener(ModuleStorage::onLevelTick);
	}

	private static void onLevelTick(LevelTickEvent.Post event) {
		// todo use register/unregister on the IEventBus
		if (ForestryConfig.SERVER.enableBackpackResupply.get()) {
			for (Player player : event.getLevel().players()) {
				BackpackResupplyHandler.resupply(player);
			}
		}
	}

	// TODO TEST
	private static void onItemPickup(ItemEntityPickupEvent.Pre event) {
//		if (/* previous event consumed the item */) {
//			return;
//		}
//
//		if (PickupHandlerStorage.onItemPickup(event.getEntity(), event.getItem())) {
//			event.setCanPickup(/* item should be removed from world, but NOT added to inventory */);
//		}
	}

	@Override
	public void registerClientHandler(Consumer<IClientModuleHandler> registrar) {
		registrar.accept(new StorageClientHandler());
	}
}

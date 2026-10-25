package forestry.core.content.escritoire;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;

import forestry.api.client.ForestrySprites;
import forestry.core.features.CoreMenuTypes;
import forestry.core.platform.gui.slots.SlotFiltered;
import forestry.core.platform.gui.slots.SlotOutput;
import forestry.core.platform.inventory.EscritoireInventory;
import forestry.core.platform.network.packets.PacketGuiStream;
import forestry.core.platform.tile.TileUtil;
import forestry.core.platform.gui.BlockEntityMenu;
import forestry.core.platform.gui.IGuiSelectable;

public class EscritoireMenu extends BlockEntityMenu<EscritoireBlockEntity> implements IGuiSelectable {
	private long lastUpdate;

	public EscritoireMenu(int id, Inventory playerInv, EscritoireBlockEntity tile) {
		super(id, CoreMenuTypes.ESCRITOIRE.menuType(), playerInv, tile, 34, 153);

		// Analyze slot
		addSlot(new SlotFiltered(this.tile, EscritoireInventory.SLOT_ANALYZE, 97, 67).setPickupWatcher(this.tile).setStackLimit(1));

		for (int i = 0; i < EscritoireInventory.SLOTS_INPUT_COUNT; i++) {
			addSlot(new SlotFiltered(this.tile, EscritoireInventory.SLOT_INPUT_1 + i, 17, 49 + i * 18).setBlockedSprite(ForestrySprites.SLOT_BLOCKED_2));
		}

		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 2; j++) {
				addSlot(new SlotOutput(this.tile, EscritoireInventory.SLOT_RESULTS_1 + i * 2 + j, 177 + j * 18, 85 + i * 18));
			}
		}
	}

	public static EscritoireMenu fromNetwork(int windowId, Inventory playerInv, FriendlyByteBuf extraData) {
		EscritoireBlockEntity tile = TileUtil.getTile(playerInv.player.level(), extraData.readBlockPos(), EscritoireBlockEntity.class);
		return new EscritoireMenu(windowId, playerInv, tile);
	}

	@Override
	public void broadcastChanges() {
		super.broadcastChanges();

		long gameLastUpdate = tile.getGame().getLastUpdate();
		if (lastUpdate != gameLastUpdate) {
			lastUpdate = gameLastUpdate;
			sendPacketToListeners(new PacketGuiStream(tile));
		}
	}

	@Override
	public void handleSelectionRequest(ServerPlayer player, int primary, int secondary) {
		EscritoireGame.Status status = tile.getGame().getStatus();
		if (status != EscritoireGame.Status.PLAYING) {
			return;
		}

		if (primary == -1) {
			tile.probe();
		} else {
			tile.choose(player.getGameProfile(), primary);
		}
	}
}

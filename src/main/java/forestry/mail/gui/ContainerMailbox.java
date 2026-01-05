package forestry.mail.gui;

import forestry.core.gui.ContainerTile;
import forestry.core.gui.slots.SlotOutput;
import forestry.core.tiles.TileUtil;
import forestry.core.utils.NetworkUtil;
import forestry.core.utils.SlotUtil;
import forestry.mail.carriers.players.POBox;
import forestry.mail.carriers.players.POBoxInfo;
import forestry.mail.features.MailMenuTypes;
import forestry.mail.network.packets.PacketPOBoxInfoResponse;
import forestry.mail.tiles.TileMailbox;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;

import javax.annotation.Nullable;

public class ContainerMailbox extends ContainerTile<TileMailbox> {

	public static final short SLOT_LETTERS = 0;
	public static final short SLOT_LETTERS_COUNT = 7 * 12;
	@Nullable
	private final POBox mailInventory;


	public static ContainerMailbox fromNetwork(int windowId, Inventory inv, FriendlyByteBuf data) {
		TileMailbox tile = TileUtil.getTile(inv.player.level(), data.readBlockPos(), TileMailbox.class);
		return new ContainerMailbox(windowId, inv, tile);    //TODO nullability.
	}

	public ContainerMailbox(int windowId, Inventory playerInventory, TileMailbox tile) {
		super(windowId, MailMenuTypes.MAILBOX.menuType(), playerInventory, tile, 35, 145);
		Container inventory = tile.getOrCreateMailInventory(playerInventory.player.level(), playerInventory.player.getGameProfile());

		if (inventory instanceof POBox) {
			this.mailInventory = (POBox) inventory;
		} else {
			this.mailInventory = null;
		}

		for (int i = 0; i < 7; i++) {
			for (int j = 0; j < 12; j++) {
				addSlot(new SlotOutput(inventory, j + i * 9, 8 + j * 18, 8 + i * 18));
			}
		}
	}

	@Override
	public void clicked(int slotId, int button, ClickType clickTypeIn, Player player) {
		super.clicked(slotId, button, clickTypeIn, player);

		if (SlotUtil.isSlotInRange(slotId, SLOT_LETTERS, SLOT_LETTERS_COUNT)) {
			if (!player.level().isClientSide && this.mailInventory != null) {
				POBoxInfo info = this.mailInventory.getPOBoxInfo();
				NetworkUtil.sendToPlayer(new PacketPOBoxInfoResponse(info, true), (ServerPlayer) player);
			}
		}
	}
}

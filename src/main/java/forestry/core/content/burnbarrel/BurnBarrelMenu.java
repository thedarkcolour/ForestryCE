package forestry.core.content.burnbarrel;

import forestry.core.features.CoreMenuTypes;
import forestry.core.platform.gui.BlockEntityMenu;
import forestry.core.platform.gui.slots.SlotFiltered;
import forestry.core.platform.inventory.BurnBarrelInventory;
import forestry.core.platform.network.packets.PacketGuiStream;
import forestry.core.platform.tile.TileUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;

public class BurnBarrelMenu extends BlockEntityMenu<BurnBarrelBlockEntity> {
	private static final int[] INPUT_X_COORDS = {70, 88, 70, 88};
	private static final int[] INPUT_Y_COORDS = {24, 24, 42, 42};
	private static final int[] OUTPUT_X_COORDS = {61, 79, 97};

	private int lastAshProgress = 0;
	private int lastBurnTime = 0;
	private int lastMaxBurnTime = 0;

	public static BurnBarrelMenu fromNetwork(int windowId, Inventory playerInv, FriendlyByteBuf extraData) {
		BurnBarrelBlockEntity burnBarrel = TileUtil.getTile(playerInv.player.level(), extraData.readBlockPos(), BurnBarrelBlockEntity.class);
		return new BurnBarrelMenu(windowId, playerInv, burnBarrel);
	}

	public BurnBarrelMenu(int windowId, Inventory player, BurnBarrelBlockEntity tile) {
		super(windowId, CoreMenuTypes.BURN_BARREL.menuType(), player, tile, 8, 120);

		// Input buffer
		for (int i = 0; i < INPUT_X_COORDS.length; i++) {
			this.addSlot(new SlotFiltered(tile, BurnBarrelInventory.SLOT_INPUT_1 + i, INPUT_X_COORDS[i], INPUT_Y_COORDS[i]));
		}

		// Output buffer
		for (int i = 0; i < OUTPUT_X_COORDS.length; i++) {
			this.addSlot(new SlotFiltered(tile, BurnBarrelInventory.SLOT_OUTPUT_1 + i, OUTPUT_X_COORDS[i], 90));
		}
	}

	@Override
	public void broadcastChanges() {
		super.broadcastChanges();

		int currentAshProgress = this.tile.getAshProductionTimer();
		int currentBurnTime = this.tile.getBurnTime();
		int currentMaxBurnTime = this.tile.getCurrentMaxBurnTime();

		if (currentAshProgress != this.lastAshProgress
			|| currentBurnTime != this.lastBurnTime
			|| currentMaxBurnTime != this.lastMaxBurnTime) {
			this.lastAshProgress = currentAshProgress;
			this.lastBurnTime = currentBurnTime;
			this.lastMaxBurnTime = currentMaxBurnTime;

			PacketGuiStream packet = new PacketGuiStream(this.tile);
			sendPacketToListeners(packet);
		}
	}
}

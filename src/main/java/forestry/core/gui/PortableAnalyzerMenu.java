package forestry.core.gui;

import forestry.core.features.CoreMenuTypes;
import forestry.core.gui.slots.SlotFiltered;
import forestry.core.inventory.PortableAnalyzerInventory;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

public class PortableAnalyzerMenu extends ForestryMenu {
	public static PortableAnalyzerMenu fromNetwork(int windowId, Inventory playerInv, FriendlyByteBuf extraData) {
		InteractionHand hand = extraData.readBoolean() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
		Player player = playerInv.player;
		PortableAnalyzerInventory inv = new PortableAnalyzerInventory(player, player.getItemInHand(hand));
		return new PortableAnalyzerMenu(windowId, inv, player);
	}

	public PortableAnalyzerMenu(int windowId, Inventory playerInv, int slotIndex) {
		super(CoreMenuTypes.ALYZER.menuType(), windowId, inventory, playerInv, 43, 156);

		int xPosLeftSlots = 223;

		addSlot(new SlotFiltered(inventory, PortableAnalyzerInventory.SLOT_ENERGY, xPosLeftSlots, 8));

		addSlot(new SlotFiltered(inventory, PortableAnalyzerInventory.SLOT_SPECIMEN, xPosLeftSlots, 26));

		addSlot(new SlotFiltered(inventory, PortableAnalyzerInventory.SLOT_ANALYZE_1, xPosLeftSlots, 57));
		addSlot(new SlotFiltered(inventory, PortableAnalyzerInventory.SLOT_ANALYZE_2, xPosLeftSlots, 75));
		addSlot(new SlotFiltered(inventory, PortableAnalyzerInventory.SLOT_ANALYZE_3, xPosLeftSlots, 93));
		addSlot(new SlotFiltered(inventory, PortableAnalyzerInventory.SLOT_ANALYZE_4, xPosLeftSlots, 111));
		addSlot(new SlotFiltered(inventory, PortableAnalyzerInventory.SLOT_ANALYZE_5, xPosLeftSlots, 129));
	}
}

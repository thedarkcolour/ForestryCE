package forestry.core.content.analyzer;

import forestry.core.features.CoreMenuTypes;
import forestry.core.platform.gui.slots.SlotFiltered;
import forestry.core.platform.inventory.PortableAnalyzerInventory;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import forestry.core.platform.gui.ItemInventoryMenu;

public class PortableAnalyzerMenu extends ItemInventoryMenu<PortableAnalyzerInventory> {
	public static PortableAnalyzerMenu fromNetwork(int windowId, Inventory playerInv, FriendlyByteBuf extraData) {
		InteractionHand hand = extraData.readBoolean() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
		Player player = playerInv.player;
		PortableAnalyzerInventory inv = new PortableAnalyzerInventory(player, player.getItemInHand(hand));
		return new PortableAnalyzerMenu(windowId, inv, player);
	}

	public PortableAnalyzerMenu(int containerId, PortableAnalyzerInventory inventory, Player player) {
		super(containerId, inventory, player.getInventory(), 43, 156, CoreMenuTypes.PORTABLE_ANALYZER.menuType());

		final int xPosLeftSlots = 223;

		this.addSlot(new SlotFiltered(inventory, PortableAnalyzerInventory.SLOT_ENERGY, xPosLeftSlots, 8));

		this.addSlot(new SlotFiltered(inventory, PortableAnalyzerInventory.SLOT_SPECIMEN, xPosLeftSlots, 26));

		this.addSlot(new SlotFiltered(inventory, PortableAnalyzerInventory.SLOT_ANALYZE_1, xPosLeftSlots, 57));
		this.addSlot(new SlotFiltered(inventory, PortableAnalyzerInventory.SLOT_ANALYZE_2, xPosLeftSlots, 75));
		this.addSlot(new SlotFiltered(inventory, PortableAnalyzerInventory.SLOT_ANALYZE_3, xPosLeftSlots, 93));
		this.addSlot(new SlotFiltered(inventory, PortableAnalyzerInventory.SLOT_ANALYZE_4, xPosLeftSlots, 111));
		this.addSlot(new SlotFiltered(inventory, PortableAnalyzerInventory.SLOT_ANALYZE_5, xPosLeftSlots, 129));
	}
}

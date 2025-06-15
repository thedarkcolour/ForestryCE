package forestry.core.circuits;

import forestry.api.modules.IForestryPacketClient;
import forestry.core.features.CoreMenuTypes;
import forestry.core.gui.ItemInventoryMenu;
import forestry.core.gui.IGuiSelectable;
import forestry.core.gui.slots.SlotFiltered;
import forestry.core.gui.slots.SlotOutput;
import forestry.core.inventory.ItemInventorySolderingIron;
import forestry.core.network.packets.PacketGuiLayoutSelect;
import forestry.core.utils.NetworkUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;

public class SolderingIronMenu extends ItemInventoryMenu<ItemInventorySolderingIron> implements IGuiSelectable {
	public static SolderingIronMenu fromNetwork(int windowId, Inventory playerInv, FriendlyByteBuf extraData) {
		InteractionHand hand = extraData.readBoolean() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
		Player player = playerInv.player;
		ItemInventorySolderingIron inv = new ItemInventorySolderingIron(player.getItemInHand(hand));
		return new SolderingIronMenu(windowId, player, inv);
	}

	public SolderingIronMenu(int windowId, Player player, ItemInventorySolderingIron inventory) {
		super(CoreMenuTypes.SOLDERING_IRON.menuType(), windowId, inventory, player.getInventory(), 8, 123);

		// Input
		this.addSlot(new SlotFiltered(inventory, 0, 152, 12));

		// Output
		this.addSlot(new SlotOutput(inventory, 1, 152, 92));

		// Ingredients
		this.addSlot(new SlotFiltered(inventory, 2, 12, 32));
		this.addSlot(new SlotFiltered(inventory, 3, 12, 52));
		this.addSlot(new SlotFiltered(inventory, 4, 12, 72));
		this.addSlot(new SlotFiltered(inventory, 5, 12, 92));
	}

	public ICircuitLayout getLayout() {
		return this.inventory.getLayout();
	}

	public static void advanceSelection(int index) {
		sendSelectionChange(index, 0);
	}

	public static void regressSelection(int index) {
		sendSelectionChange(index, 1);
	}

	private static void sendSelectionChange(int index, int advance) {
		NetworkUtil.sendRecipeClick(index, advance);
	}

	@Override
	public void handleSelectionRequest(ServerPlayer player, int primary, int secondary) {
		if (secondary == 0) {
			if (primary == 0) {
                this.inventory.advanceLayout();
			}
		} else if (primary == 0) {
            this.inventory.regressLayout();
		}

		IForestryPacketClient packetResponse = new PacketGuiLayoutSelect(this.inventory.getLayout().id());
        PacketDistributor.sendToPlayer(player, packetResponse);
    }

	public void setLayout(ICircuitLayout layout) {
        this.inventory.setLayout(layout);
	}
}

package forestry.core.content.energy.menu;

import forestry.core.platform.gui.LiquidTanksSocketedMenu;
import forestry.core.platform.gui.slots.SlotLiquidIn;
import forestry.core.platform.network.packets.PacketGuiStream;
import forestry.core.platform.tile.TileUtil;
import forestry.core.content.energy.features.EnergyMenus;
import forestry.core.content.energy.inventory.BiogasEngineInventory;
import forestry.core.content.energy.tiles.BiogasEngineBlockEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;

public class BiogasEngineMenu extends LiquidTanksSocketedMenu<BiogasEngineBlockEntity> {
	public static BiogasEngineMenu fromNetwork(int windowId, Inventory inv, FriendlyByteBuf extraData) {
		BiogasEngineBlockEntity tile = TileUtil.getTile(inv.player.level(), extraData.readBlockPos(), BiogasEngineBlockEntity.class);
		return new BiogasEngineMenu(windowId, inv, tile);
	}

	public BiogasEngineMenu(int windowId, Inventory player, BiogasEngineBlockEntity engine) {
		super(windowId, EnergyMenus.BIOGAS_ENGINE.menuType(), player, engine, 8, 84);

		this.addSlot(new SlotLiquidIn(engine, BiogasEngineInventory.SLOT_CAN, 143, 40));
	}

	@Override
	public void broadcastChanges() {
		super.broadcastChanges();
		PacketGuiStream packet = new PacketGuiStream(this.tile);
		sendPacketToListeners(packet);
	}
}

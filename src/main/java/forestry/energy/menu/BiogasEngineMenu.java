package forestry.energy.menu;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;

import forestry.core.gui.ContainerLiquidTanks;
import forestry.core.gui.slots.SlotLiquidIn;
import forestry.core.network.packets.PacketGuiStream;
import forestry.core.tiles.TileUtil;
import forestry.energy.features.EnergyMenus;
import forestry.energy.inventory.InventoryEngineBiogas;
import forestry.energy.tiles.BiogasEngineBlockEntity;

public class BiogasEngineMenu extends ContainerLiquidTanks<BiogasEngineBlockEntity> {
	public static BiogasEngineMenu fromNetwork(int windowId, Inventory inv, FriendlyByteBuf extraData) {
		BiogasEngineBlockEntity tile = TileUtil.getTile(inv.player.level(), extraData.readBlockPos(), BiogasEngineBlockEntity.class);
		return new BiogasEngineMenu(windowId, inv, tile);
	}

	public BiogasEngineMenu(int windowId, Inventory player, BiogasEngineBlockEntity engine) {
		super(windowId, EnergyMenus.ENGINE_BIOGAS.menuType(), player, engine, 8, 84);

		this.addSlot(new SlotLiquidIn(engine, InventoryEngineBiogas.SLOT_CAN, 143, 40));
	}

	@Override
	public void broadcastChanges() {
		super.broadcastChanges();
		PacketGuiStream packet = new PacketGuiStream(tile);
		sendPacketToListeners(packet);
	}
}

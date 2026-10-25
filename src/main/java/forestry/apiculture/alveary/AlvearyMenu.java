package forestry.apiculture.alveary;

import forestry.api.ForestryConstants;
import forestry.apiculture.features.ApicultureMenuTypes;
import forestry.apiculture.alveary.multiblock.AbstractAlvearyBlockEntity;
import forestry.core.platform.advancements.AdvancementHelper;
import forestry.core.platform.gui.BlockEntityMenu;
import forestry.core.platform.network.packets.PacketGuiStream;
import forestry.core.platform.tile.TileUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import forestry.apiculture.bees.BeeMenuHelper;

public class AlvearyMenu extends BlockEntityMenu<AbstractAlvearyBlockEntity> {
	private static final ResourceLocation OPEN_ALVEARY_UI = ForestryConstants.forestry("get_alveary");

	public static AlvearyMenu fromNetwork(int windowId, Inventory inv, FriendlyByteBuf data) {
		AbstractAlvearyBlockEntity tile = TileUtil.getTile(inv.player.level(), data.readBlockPos(), AbstractAlvearyBlockEntity.class);
		return new AlvearyMenu(windowId, inv, tile);
	}

	public AlvearyMenu(int windowid, Inventory playerInv, AbstractAlvearyBlockEntity tile) {
		super(windowid, ApicultureMenuTypes.ALVEARY.menuType(), playerInv, tile, 8, 108);
		BeeMenuHelper.addSlots(this, tile, false);

		Player player = playerInv.player;
		if (player != null) {
			AdvancementHelper.tryUnlock(player, OPEN_ALVEARY_UI);
		}

		tile.getBeekeepingLogic().clearCachedValues();
	}

	private int beeProgress = -1;

	@Override
	public void broadcastChanges() {
		super.broadcastChanges();

		int beeProgress = this.tile.getBeekeepingLogic().getBeeProgressPercent();
		if (this.beeProgress != beeProgress) {
			this.beeProgress = beeProgress;
			PacketGuiStream packet = new PacketGuiStream(this.tile);
			sendPacketToListeners(packet);
		}
	}
}

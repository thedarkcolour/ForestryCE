package forestry.apiculture.bees;

import forestry.apiculture.features.ApicultureMenuTypes;
import forestry.core.platform.gui.BlockEntityMenu;
import forestry.core.platform.network.packets.PacketGuiStream;
import forestry.core.platform.tile.TileUtil;
import forestry.core.platform.util.NetworkUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;

import java.util.Objects;

public class BeeHousingMenu extends BlockEntityMenu<AbstractBeeHousingBlockEntity> {
	private final IGuiBeeHousingDelegate delegate;
	private final BeeHousingScreen.Icon icon;

	public static BeeHousingMenu fromNetwork(int windowId, Inventory inv, FriendlyByteBuf buffer) {
		AbstractBeeHousingBlockEntity tile = TileUtil.getTile(inv.player.level(), buffer.readBlockPos(), AbstractBeeHousingBlockEntity.class);
		boolean hasFrames = buffer.readBoolean();
		BeeHousingScreen.Icon icon = NetworkUtil.readEnum(buffer, BeeHousingScreen.Icon.VALUES);
		return new BeeHousingMenu(windowId, inv, Objects.requireNonNull(tile), hasFrames, icon);
	}

	public BeeHousingMenu(int windowId, Inventory playerInv, AbstractBeeHousingBlockEntity tile, boolean hasFrames, BeeHousingScreen.Icon icon) {
		super(windowId, ApicultureMenuTypes.BEE_HOUSING.menuType(), playerInv, tile, 8, 108);
		BeeMenuHelper.addSlots(this, tile, hasFrames);

		tile.getBeekeepingLogic().clearCachedValues();

		this.delegate = tile;
		this.icon = icon;
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

	public IGuiBeeHousingDelegate getDelegate() {
		return this.delegate;
	}

	public BeeHousingScreen.Icon getIcon() {
		return this.icon;
	}
}

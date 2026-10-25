package forestry.mail.blocks;

import forestry.core.platform.block.IBlockType;
import forestry.core.platform.block.IMachineProperties;
import forestry.core.platform.block.MachineProperties;
import forestry.core.platform.tile.IForestryTicker;
import forestry.core.platform.tile.ForestryBlockEntity;
import forestry.mail.features.MailBlockEntities;
import forestry.mail.postoffice.StampCollectorBlockEntity;
import forestry.mail.tradestation.TradeStationBlockEntity;
import forestry.core.platform.registration.FeatureTileType;

import javax.annotation.Nullable;

public enum MailBlockType implements IBlockType {
	MAILBOX(MailBlockEntities.MAILBOX, "mailbox", null),
	TRADE_STATION(MailBlockEntities.TRADER, "trade_station", TradeStationBlockEntity::serverTick),
	STAMP_COLLETOR(MailBlockEntities.STAMP_COLLECTOR, "stamp_collector", StampCollectorBlockEntity::serverTick);

	private final IMachineProperties<?> machineProperties;

	<T extends ForestryBlockEntity> MailBlockType(FeatureTileType<T> teClass, String name, @Nullable IForestryTicker<T> serverTicker) {
		this.machineProperties = new MachineProperties.Builder<>(teClass, name).setServerTicker(serverTicker).create();
	}

	@Override
	public IMachineProperties<?> getMachineProperties() {
		return this.machineProperties;
	}

	@Override
	public String getSerializedName() {
		return getMachineProperties().getSerializedName();
	}
}

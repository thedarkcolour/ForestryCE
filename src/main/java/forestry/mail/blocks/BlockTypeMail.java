package forestry.mail.blocks;

import forestry.core.blocks.IBlockType;
import forestry.core.blocks.IMachineProperties;
import forestry.core.blocks.MachineProperties;
import forestry.core.tiles.IForestryTicker;
import forestry.core.tiles.TileForestry;
import forestry.mail.features.MailTiles;
import forestry.mail.tiles.TileStampCollector;
import forestry.mail.tiles.TileTrader;
import forestry.modules.features.FeatureTileType;

import javax.annotation.Nullable;

public enum BlockTypeMail implements IBlockType {
	MAILBOX(MailTiles.MAILBOX, "mailbox", null),
	TRADE_STATION(MailTiles.TRADER, "trade_station", TileTrader::serverTick),
	STAMP_COLLETOR(MailTiles.STAMP_COLLECTOR, "stamp_collector", TileStampCollector::serverTick);

	private final IMachineProperties<?> machineProperties;

	<T extends TileForestry> BlockTypeMail(FeatureTileType<T> teClass, String name, @Nullable IForestryTicker<T> serverTicker) {
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

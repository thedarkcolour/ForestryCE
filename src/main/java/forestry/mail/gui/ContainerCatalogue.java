package forestry.mail.gui;

import forestry.api.mail.*;
import forestry.core.gui.IGuiSelectable;
import forestry.core.utils.NetworkUtil;
import forestry.mail.carriers.trading.TradeStationRegistry;
import forestry.mail.features.MailMenuTypes;
import forestry.mail.network.packets.PacketLetterInfoResponseTrader;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.*;

public class ContainerCatalogue extends AbstractContainerMenu implements IGuiSelectable, ILetterInfoReceiver {
	private final Player player;
	private final List<ITradeStation> stations = new ArrayList<>();

	@Nullable
	private ITradeStationInfo currentTrade = null;

	private final DataSlot stationIndex = DataSlot.standalone();

	// for display on client
	private final DataSlot stationCount = DataSlot.standalone();

	private boolean needsSync = true;
	private final DataSlot currentFilter = DataSlot.standalone();

	private static final String[] FILTER_NAMES = new String[]{"all", "online", "offline"};
	private static final List<Set<IPostalState>> FILTERS = new ArrayList<>();

	static {
		EnumSet<EnumTradeStationState> all = EnumSet.allOf(EnumTradeStationState.class);
		EnumSet<EnumTradeStationState> online = EnumSet.of(EnumTradeStationState.OK);
		EnumSet<EnumTradeStationState> offline = EnumSet.copyOf(all);
		offline.removeAll(online);

		FILTERS.add(Collections.unmodifiableSet(all));
		FILTERS.add(Collections.unmodifiableSet(online));
		FILTERS.add(Collections.unmodifiableSet(offline));
	}

	public static ContainerCatalogue fromNetwork(int windowId, Inventory inv, FriendlyByteBuf data) {
		return new ContainerCatalogue(windowId, inv);
	}

	public ContainerCatalogue(int windowId, Inventory inv) {
		super(MailMenuTypes.CATALOGUE.menuType(), windowId);
		this.player = inv.player;

		addDataSlot(this.stationIndex);
		addDataSlot(this.stationCount);
		addDataSlot(this.currentFilter);

        this.stationIndex.set(0);
        this.stationCount.set(0);
        this.currentFilter.set(1);

		rebuildStationsList();
	}

	public int getPageCount() {
		return Math.max(this.stationCount.get(), 1);
	}

	public int getPageNumber() {
		return this.stationIndex.get() + 1;
	}

	public String getFilterIdent() {
		return FILTER_NAMES[this.currentFilter.get()];
	}

	private void rebuildStationsList() {
		if (this.player.level().isClientSide) {
			return;
		}

        this.stations.clear();

		Map<IMailAddress, ITradeStation> tradeStations = TradeStationRegistry.getOrCreate((ServerLevel) this.player.level()).getActiveTradeStations();

		for (ITradeStation station : tradeStations.values()) {
			ITradeStationInfo info = station.getTradeInfo();

			// Filter out any trade stations which do not actually offer anything.
			if (FILTERS.get(this.currentFilter.get()).contains(info.state())) {
                this.stations.add(station);
			}
		}

        this.stationIndex.set(0);
        this.stationCount.set(this.stations.size());
		updateTradeInfo();
	}

	public void nextPage() {
		if (!this.stations.isEmpty()) {
            this.stationIndex.set((this.stationIndex.get() + 1) % this.stations.size());
			updateTradeInfo();
		}
	}

	public void previousPage() {
		if (!this.stations.isEmpty()) {
            this.stationIndex.set((this.stationIndex.get() - 1 + this.stations.size()) % this.stations.size());
			updateTradeInfo();
		}
	}

	public void cycleFilter() {
        this.currentFilter.set((this.currentFilter.get() + 1) % FILTERS.size());
		rebuildStationsList();
	}

	/* Managing Trade info */
	private void updateTradeInfo() {
		// Updating is done by the server.
		if (this.player.level().isClientSide) {
			return;
		}

		if (!this.stations.isEmpty()) {
			ITradeStation station = this.stations.get(this.stationIndex.get());
			setTradeInfo(station.getTradeInfo());
		} else {
			setTradeInfo(null);
		}
        this.needsSync = true;
	}

	@Override
	public void handleLetterInfoUpdate(IPostalCarrier carrier, @Nullable IMailAddress address, @Nullable ITradeStationInfo tradeInfo) {
		setTradeInfo(tradeInfo);
	}

	@Nullable
	public ITradeStationInfo getTradeInfo() {
		return this.currentTrade;
	}

	private void setTradeInfo(@Nullable ITradeStationInfo info) {
        this.currentTrade = info;
	}

	@Override
	public void broadcastChanges() {
		super.broadcastChanges();

		if (this.needsSync) {
			NetworkUtil.sendToPlayer(new PacketLetterInfoResponseTrader(this.currentTrade), (ServerPlayer) this.player);
            this.needsSync = false;
		}
	}

	@Override
	public boolean stillValid(Player p_75145_1_) {
		return true;
	}

	@Override
	public void handleSelectionRequest(ServerPlayer player, int primary, int secondary) {
		switch (primary) {
			case 0 -> nextPage();
			case 1 -> previousPage();
			case 2 -> cycleFilter();
		}

		this.needsSync = true;
	}

	@Override
	public ItemStack quickMoveStack(Player player, int slot) {
		return ItemStack.EMPTY;
	}
}

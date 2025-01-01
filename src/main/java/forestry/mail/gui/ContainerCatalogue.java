/*******************************************************************************
 * Copyright (c) 2011-2014 SirSengir.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Various Contributors including, but not limited to:
 * SirSengir (original work), CovertJaguar, Player, Binnie, MysteriousAges
 ******************************************************************************/
package forestry.mail.gui;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import forestry.api.mail.*;
import forestry.mail.carriers.trading.TradeStationRegistry;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;

import forestry.core.gui.IGuiSelectable;
import forestry.core.utils.NetworkUtil;
import forestry.mail.features.MailMenuTypes;
import forestry.mail.network.packets.PacketLetterInfoResponseTrader;

import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.item.ItemStack;

public class ContainerCatalogue extends AbstractContainerMenu implements IGuiSelectable, ILetterInfoReceiver {

	private final Player player;
	private final List<ITradeStation> stations = new ArrayList<>();

	@Nullable
	private ITradeStationInfo currentTrade = null;

	private DataSlot stationIndex = DataSlot.standalone();

	// for display on client
	private DataSlot stationCount = DataSlot.standalone();

	private boolean needsSync = true;
	private DataSlot currentFilter = DataSlot.standalone();

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

		addDataSlot(stationIndex);
		addDataSlot(stationCount);
		addDataSlot(currentFilter);

		stationIndex.set(0);
		stationCount.set(0);
		currentFilter.set(1);

		rebuildStationsList();
	}

	public int getPageCount() {
		return Math.max(stationCount.get(), 1);
	}

	public int getPageNumber() {
		return stationIndex.get() + 1;
	}

	public String getFilterIdent() {
		return FILTER_NAMES[currentFilter.get()];
	}

	private void rebuildStationsList() {
		if (player.level.isClientSide) {
			return;
		}

		stations.clear();

		Map<IMailAddress, ITradeStation> tradeStations = TradeStationRegistry.getOrCreate((ServerLevel) player.level).getActiveTradeStations();

		for (ITradeStation station : tradeStations.values()) {
			ITradeStationInfo info = station.getTradeInfo();

			// Filter out any trade stations which do not actually offer anything.
			if (FILTERS.get(currentFilter.get()).contains(info.state())) {
				stations.add(station);
			}
		}

		stationIndex.set(0);
		stationCount.set(stations.size());
		updateTradeInfo();
	}

	public void nextPage() {
		if (!stations.isEmpty()) {
			stationIndex.set((stationIndex.get() + 1) % stations.size());
			updateTradeInfo();
		}
	}

	public void previousPage() {
		if (!stations.isEmpty()) {
			stationIndex.set((stationIndex.get() - 1 + stations.size()) % stations.size());
			updateTradeInfo();
		}
	}

	public void cycleFilter() {
		currentFilter.set((currentFilter.get() + 1) % FILTERS.size());
		rebuildStationsList();
	}

	/* Managing Trade info */
	private void updateTradeInfo() {
		// Updating is done by the server.
		if (player.level.isClientSide) {
			return;
		}

		if (!stations.isEmpty()) {
			ITradeStation station = stations.get(stationIndex.get());
			setTradeInfo(station.getTradeInfo());
		} else {
			setTradeInfo(null);
		}
		needsSync = true;
	}

	@Override
	public void handleLetterInfoUpdate(IPostalCarrier carrier, @Nullable IMailAddress address, @Nullable ITradeStationInfo tradeInfo) {
		setTradeInfo(tradeInfo);
	}

	@Nullable
	public ITradeStationInfo getTradeInfo() {
		return currentTrade;
	}

	private void setTradeInfo(@Nullable ITradeStationInfo info) {
		currentTrade = info;
	}

	@Override
	public void broadcastChanges() {
		super.broadcastChanges();

		if (needsSync) {
			NetworkUtil.sendToPlayer(new PacketLetterInfoResponseTrader(currentTrade), (ServerPlayer) player);
			needsSync = false;
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

		needsSync = true;
	}

	@Override
	public ItemStack quickMoveStack(Player player, int slot) {
		return ItemStack.EMPTY;
	}
}

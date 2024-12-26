package forestry.mail;

import com.mojang.authlib.GameProfile;
import forestry.api.mail.*;
import forestry.mail.carriers.PostalCarriers;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class TradeRegistry extends SavedData implements IWatchable.Watcher {
    private static final String SAVE_NAME = "forestry_trade_stations";

    public final Map<IMailAddress, ITradeStation> cachedTradeStations = new HashMap<>();

    /**
     * @param address the potential address of the Trader
     * @return true if the passed address can be an address for a trade station
     */
    public boolean isValidTradeAddress(IMailAddress address) {
        return address.getCarrier().equals(PostalCarriers.TRADER.get()) && address.getName().matches("^[a-zA-Z0-9]+$");
    }

    /**
     * @param address the potential address of the Trader
     * @return true if the trade address has not yet been used before.
     */
    public boolean isAvailableTradeAddress(IMailAddress address) {
        return getTradeStation(address) == null;
    }

    public void registerTradeStation(IMailAddress address, ITradeStation station) {
        cachedTradeStations.put(address, station);
        station.registerUpdateWatcher(this);
        setDirty();
    }

    @Nullable
    public ITradeStation getTradeStation(IMailAddress address) {
        if (cachedTradeStations.containsKey(address)) {
            return cachedTradeStations.get(address);
        }

        return null;
    }

    public ITradeStation getOrCreateTradeStation(GameProfile owner, IMailAddress address) {
        ITradeStation trade = getTradeStation(address);

        if (trade == null) {
            trade = new TradeStation(owner, address);
            registerTradeStation(address, trade);
            trade.setDirty();
        }

        return trade;
    }

    public void deleteTradeStation(IMailAddress address) {
        ITradeStation trade = getTradeStation(address);
        if (trade == null) {
            return;
        }
        trade.invalidate();
        trade.unregisterUpdateWatcher(this);
        cachedTradeStations.remove(address);
        setDirty();
    }

    public Map<IMailAddress, ITradeStation> getActiveTradeStations() {
        return this.cachedTradeStations;
    }

    @Override
    public void onWatchableUpdate() {
        setDirty();
    }

    private static TradeRegistry create() {
        return new TradeRegistry();
    }

    private static TradeRegistry load(CompoundTag compoundTag) {
        TradeRegistry registry = new TradeRegistry();
        ListTag tradeStations = compoundTag.getList("tradeStations", 10);
        for(int i = 0; i < tradeStations.size(); ++i) {
            CompoundTag stationTag = tradeStations.getCompound(i);

            IMailAddress address = new MailAddress(stationTag.getCompound("address"));
            ITradeStation station = new TradeStation(stationTag.getCompound("station"));
            registry.registerTradeStation(address, station);
        }
        return registry;
    }

    @Override
    public CompoundTag save(CompoundTag compoundTag) {
        ListTag tradeStations = new ListTag();
        for (Map.Entry<IMailAddress, ITradeStation> entry : cachedTradeStations.entrySet()) {
            CompoundTag entryTag = new CompoundTag();
            entryTag.put("address", entry.getKey().write(new CompoundTag()));
            entryTag.put("station", entry.getValue().write(new CompoundTag()));
            tradeStations.add(entryTag);
        }
        compoundTag.put("tradeStations", tradeStations);
        return compoundTag;
    }

    public static TradeRegistry getOrCreate(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TradeRegistry::load, TradeRegistry::create, SAVE_NAME);
    }
}

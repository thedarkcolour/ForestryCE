package forestry.mail;

import com.mojang.authlib.GameProfile;
import forestry.Forestry;
import forestry.api.mail.*;
import forestry.mail.carriers.PostalCarriers;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class TradeRegistry extends SavedData {
    private static final String SAVE_NAME = "forestry_trade_stations";

    private final ServerLevel level;

    public final Map<IMailAddress, ITradeStation> cachedTradeStations = new HashMap<>();

    public TradeRegistry(ServerLevel level) {
        this.level = level;
    }

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
    }

    @Nullable
    public TradeStation getTradeStation(IMailAddress address) {
        if (cachedTradeStations.containsKey(address)) {
            return (TradeStation) cachedTradeStations.get(address);
        }

        TradeStation trade = level.getDataStorage().get(TradeStation::new, TradeStation.SAVE_NAME + address);

        // Only existing and valid mail orders are returned
        if (trade != null && trade.isValid()) {
            registerTradeStation(address, trade);
            return trade;
        }

        return null;
    }

    public TradeStation getOrCreateTradeStation(GameProfile owner, IMailAddress address) {
        TradeStation trade = getTradeStation(address);

        if (trade == null) {
            trade = level.getDataStorage().computeIfAbsent(TradeStation::new, () -> new TradeStation(owner, address), TradeStation.SAVE_NAME + address);
            trade.setDirty();
            registerTradeStation(address, trade);
        }

        return trade;
    }

    public void deleteTradeStation(IMailAddress address) {
        TradeStation trade = getTradeStation(address);
        if (trade == null) {
            return;
        }
        // TODO: Clean this up or migrate to save in a single file, as deleting seems not possible for now
        // Need to be marked as invalid since WorldSavedData seems to do some caching of its own.
        trade.invalidate();
        cachedTradeStations.remove(address);
        //		File file = world.getSaveHandler().getMapFileFromName(trade.getName());	//TODO which file?
        boolean delete = false; //TODO fix file.delete();
        if (!delete) {
            Forestry.LOGGER.error("Failed to delete trade station file. {}", "FIXME!");//file);
        }
    }

    public Map<IMailAddress, ITradeStation> getActiveTradeStations() {
        return this.cachedTradeStations;
    }

    private static TradeRegistry create(ServerLevel level) {
        return new TradeRegistry(level);
    }

    private static TradeRegistry load(CompoundTag compoundTag, ServerLevel level) {
        return new TradeRegistry(level);
    }

    @Override
    public CompoundTag save(CompoundTag compoundTag) {
        return compoundTag;
    }

    public static TradeRegistry getOrCreate(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent((tag) -> TradeRegistry.load(tag, level), () -> TradeRegistry.create(level), SAVE_NAME);
    }
}

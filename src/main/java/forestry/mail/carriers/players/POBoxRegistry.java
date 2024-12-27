package forestry.mail.carriers.players;

import forestry.api.mail.IMailAddress;
import forestry.mail.IWatchable;
import forestry.mail.MailAddress;
import forestry.mail.carriers.PostalCarriers;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;

public class POBoxRegistry extends SavedData implements IWatchable.Watcher {
    private static final String SAVE_NAME = "forestry_poboxes";

    public final Map<IMailAddress, POBox> cachedPOBoxes = new HashMap<>();

    /**
     * @param address the potential address of the PO box
     * @return true if the passed address is valid for PO Boxes.
     */
    public boolean isValidPOBox(IMailAddress address) {
        return address.getCarrier().equals(PostalCarriers.PLAYER.get()) && address.getName().matches("^[a-zA-Z0-9]+$");
    }

    private void registerPOBOx(IMailAddress address, POBox box) {
        cachedPOBoxes.put(address, box);
        box.registerUpdateWatcher(this);
        setDirty();
    }

    public POBox getPOBox(IMailAddress address) {
        return cachedPOBoxes.get(address);
    }

    public POBox getOrCreatePOBox(IMailAddress address) {
        POBox pobox = getPOBox(address);

        if (pobox == null) {
            pobox = new POBox(address);
            registerPOBOx(address, pobox);
            pobox.setDirty();
        }

        return pobox;
    }

    @Override
    public void onWatchableUpdate() {
        setDirty();
    }

    private static POBoxRegistry create() {
        return new POBoxRegistry();
    }

    private static POBoxRegistry load(CompoundTag compoundTag) {
        POBoxRegistry registry = new POBoxRegistry();
        ListTag tradeStations = compoundTag.getList("poboxes", 10);
        for(int i = 0; i < tradeStations.size(); ++i) {
            CompoundTag stationTag = tradeStations.getCompound(i);

            IMailAddress address = new MailAddress(stationTag.getCompound("address"));
            POBox pobox = new POBox(stationTag.getCompound("pobox"));
            registry.registerPOBOx(address, pobox);
        }
        return registry;
    }

    @Override
    public CompoundTag save(CompoundTag compoundTag) {
        ListTag poboxes = new ListTag();
        for (Map.Entry<IMailAddress, POBox> entry : cachedPOBoxes.entrySet()) {
            CompoundTag entryTag = new CompoundTag();
            entryTag.put("address", entry.getKey().write(new CompoundTag()));
            entryTag.put("pobox", entry.getValue().write(new CompoundTag()));
            poboxes.add(entryTag);
        }
        compoundTag.put("poboxes", poboxes);
        return compoundTag;
    }

    public static POBoxRegistry getOrCreate(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(POBoxRegistry::load, POBoxRegistry::create, SAVE_NAME);
    }
}

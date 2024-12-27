package forestry.mail;

import forestry.api.mail.IMailAddress;
import forestry.mail.carriers.PostalCarriers;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;

public class POBoxRegistry extends SavedData {
    private static final String SAVE_NAME = "forestry_poboxes";

    public final Map<IMailAddress, POBox> cachedPOBoxes = new HashMap<>();

    private final ServerLevel level;

    public POBoxRegistry(ServerLevel level) {
        this.level = level;
    }

    /**
     * @param address the potential address of the PO box
     * @return true if the passed address is valid for PO Boxes.
     */
    public boolean isValidPOBox(IMailAddress address) {
        return address.getCarrier().equals(PostalCarriers.PLAYER.get()) && address.getName().matches("^[a-zA-Z0-9]+$");
    }

    public POBox getPOBox(IMailAddress address) {
        if (cachedPOBoxes.containsKey(address)) {
            return cachedPOBoxes.get(address);
        }

        return level.getDataStorage().get(POBox::new, POBox.SAVE_NAME + address);
    }

    public POBox getOrCreatePOBox(IMailAddress add) {
        POBox pobox = getPOBox(add);

        if (pobox == null) {
            pobox = level.getDataStorage().computeIfAbsent(POBox::new, () -> new POBox(add), POBox.SAVE_NAME + add);

            pobox.setDirty();
            cachedPOBoxes.put(add, pobox);
        }

        return pobox;
    }

    private static POBoxRegistry create(ServerLevel level) {
        return new POBoxRegistry(level);
    }

    private static POBoxRegistry load(CompoundTag compoundTag, ServerLevel level) {
        return new POBoxRegistry(level);
    }

    @Override
    public CompoundTag save(CompoundTag compoundTag) {
        return compoundTag;
    }

    public static POBoxRegistry getOrCreate(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(tag -> POBoxRegistry.load(tag, level), () -> POBoxRegistry.create(level), SAVE_NAME);
    }
}

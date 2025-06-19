package forestry.api.core;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

public interface INbtReadable {
	void read(CompoundTag nbt, HolderLookup.Provider registries);
}

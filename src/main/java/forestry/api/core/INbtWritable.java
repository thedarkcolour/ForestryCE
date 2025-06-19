package forestry.api.core;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

public interface INbtWritable {
	CompoundTag write(CompoundTag nbt, HolderLookup.Provider registries);
}

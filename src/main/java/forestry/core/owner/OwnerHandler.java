package forestry.core.owner;

import forestry.api.core.INbtReadable;
import forestry.api.core.INbtWritable;
import forestry.core.network.IStreamable;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.component.ResolvableProfile;

import javax.annotation.Nullable;

public class OwnerHandler implements IOwnerHandler, IStreamable, INbtWritable, INbtReadable {
	@Nullable
	private ResolvableProfile owner = null;

	@Override
	@Nullable
	public ResolvableProfile getOwner() {
		return this.owner;
	}

	@Override
	public void setOwner(ResolvableProfile owner) {
		this.owner = owner;
	}

	@Override
	public void writeData(RegistryFriendlyByteBuf buffer) {
		if (this.owner == null || this.owner.id().isEmpty()) {
			buffer.writeBoolean(false);
		} else {
			buffer.writeBoolean(true);
			ResolvableProfile.STREAM_CODEC.encode(buffer, this.owner);
		}
	}

	@Override
	public void readData(RegistryFriendlyByteBuf buffer) {
		if (buffer.readBoolean()) {
			ResolvableProfile owner = ResolvableProfile.STREAM_CODEC.decode(buffer);
			setOwner(owner);
		}
	}

	@Override
	public void read(CompoundTag data, HolderLookup.Provider registries) {
		if (data.contains("owner")) {
			ResolvableProfile.CODEC
				.parse(NbtOps.INSTANCE, data.get("profile"))
				.result()
				.ifPresent(this::setOwner);
		}
	}

	@Override
	public CompoundTag write(CompoundTag data, HolderLookup.Provider registries) {
		if (this.owner != null) {
			data.put("owner", ResolvableProfile.CODEC.encodeStart(NbtOps.INSTANCE, this.owner).getOrThrow());
		}
		return data;
	}
}

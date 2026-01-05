package forestry.core.owner;

import com.mojang.authlib.GameProfile;
import forestry.api.core.INbtReadable;
import forestry.api.core.INbtWritable;
import forestry.core.network.IStreamable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.FriendlyByteBuf;

import javax.annotation.Nullable;
import java.util.UUID;

public class OwnerHandler implements IOwnerHandler, IStreamable, INbtWritable, INbtReadable {
	@Nullable
	private GameProfile owner = null;

	@Override
	@Nullable
	public GameProfile getOwner() {
		return this.owner;
	}

	@Override
	public void setOwner(GameProfile owner) {
		this.owner = owner;
	}

	@Override
	public void writeData(FriendlyByteBuf data) {
		if (this.owner == null) {
			data.writeBoolean(false);
		} else {
			data.writeBoolean(true);
			data.writeLong(this.owner.getId().getMostSignificantBits());
			data.writeLong(this.owner.getId().getLeastSignificantBits());
			data.writeUtf(this.owner.getName());
		}
	}

	@Override
	public void readData(FriendlyByteBuf data) {
		if (data.readBoolean()) {
			GameProfile owner = new GameProfile(new UUID(data.readLong(), data.readLong()), data.readUtf());
			setOwner(owner);
		}
	}

	@Override
	public void read(CompoundTag data) {
		if (data.contains("owner")) {
			GameProfile owner = NbtUtils.readGameProfile(data.getCompound("owner"));
			if (owner != null) {
				setOwner(owner);
			}
		}
	}

	@Override
	public CompoundTag write(CompoundTag data) {
		if (this.owner != null) {
			CompoundTag nbt = new CompoundTag();
			NbtUtils.writeGameProfile(nbt, this.owner);
			data.put("owner", nbt);
		}
		return data;
	}
}

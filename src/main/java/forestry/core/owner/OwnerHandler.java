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
package forestry.core.owner;

import com.mojang.authlib.GameProfile;
import forestry.api.core.INbtReadable;
import forestry.api.core.INbtWritable;
import forestry.core.network.IStreamable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.RegistryFriendlyByteBuf;

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
	public void writeData(RegistryFriendlyByteBuf buffer) {
		if (this.owner == null) {
			buffer.writeBoolean(false);
		} else {
			buffer.writeBoolean(true);
			buffer.writeLong(this.owner.getId().getMostSignificantBits());
			buffer.writeLong(this.owner.getId().getLeastSignificantBits());
			buffer.writeUtf(this.owner.getName());
		}
	}

	@Override
	public void readData(RegistryFriendlyByteBuf buffer) {
		if (buffer.readBoolean()) {
			GameProfile owner = new GameProfile(new UUID(buffer.readLong(), buffer.readLong()), buffer.readUtf());
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

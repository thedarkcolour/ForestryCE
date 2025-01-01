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
package forestry.mail;

import com.google.common.base.Preconditions;

import java.util.Locale;
import java.util.UUID;

import forestry.api.mail.IPostalCarrier;
import forestry.mail.carriers.PostalCarriers;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.StringUtils;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;

import com.mojang.authlib.GameProfile;

import forestry.api.core.INbtWritable;
import forestry.api.mail.IMailAddress;
import forestry.core.utils.PlayerUtil;

public class MailAddress implements IMailAddress {

	private static final GameProfile invalidGameProfile = new GameProfile(new UUID(0, 0), "");
	public static final MailAddress INVALID = new MailAddress(invalidGameProfile);

	private final IPostalCarrier carrier;
	private final GameProfile gameProfile; // gameProfile is a fake GameProfile for traders, and real for players

	public MailAddress(GameProfile gameProfile) {

		this.carrier = PostalCarriers.PLAYER.get();
		this.gameProfile = gameProfile;
	}

	public MailAddress(String name) {
		Preconditions.checkNotNull(name, "name must not be null");
		Preconditions.checkArgument(StringUtils.isNotBlank(name), "name must not be blank");

		this.carrier = PostalCarriers.TRADER.get();
		this.gameProfile = new GameProfile(null, name);
	}

	public MailAddress(CompoundTag nbt) {
		IPostalCarrier carrier = null;
		GameProfile gameProfile = invalidGameProfile;
		if (nbt.contains("carrier")) {
			carrier = PostalCarriers.REGISTRY.get().getValue(ResourceLocation.tryParse(nbt.getString("carrier")));
		}

		if (carrier == null) {
			carrier = PostalCarriers.PLAYER.get();
			gameProfile = invalidGameProfile;
		} else if (nbt.contains("profile")) {
			CompoundTag profileTag = nbt.getCompound("profile");
			gameProfile = NbtUtils.readGameProfile(profileTag);
			if (gameProfile == null) {
				gameProfile = invalidGameProfile;
			}
		}

		this.carrier = carrier;
		this.gameProfile = gameProfile;
	}

	@Override
	public IPostalCarrier getCarrier() {
		return carrier;
	}

	@Override
	public String getName() {
		return gameProfile.getName();
	}

	@Override
	public boolean isValid() {
		return gameProfile.getName() != null && !PlayerUtil.isSameGameProfile(gameProfile, invalidGameProfile);
	}

	@Override
	public GameProfile getPlayerProfile() {
		if (!this.carrier.equals(PostalCarriers.PLAYER.get())) {
			return invalidGameProfile;
		}
		return gameProfile;
	}

	@Override
	public int hashCode() {
		return gameProfile.getName().hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof MailAddress address)) {
			return false;
		}

		return PlayerUtil.isSameGameProfile(address.gameProfile, gameProfile);
	}

	@Override
	public String toString() {
		String name = getName().toLowerCase(Locale.ENGLISH);
		if (getCarrier().equals(PostalCarriers.PLAYER.get())) {
			return carrier + "-" + name + '-' + gameProfile.getId();
		} else {
			return carrier + "-" + name;
		}
	}

	@Override
	public CompoundTag write(CompoundTag compoundNBT) {
		compoundNBT.putString("carrier", PostalCarriers.REGISTRY.get().getKey(carrier).toString());

		if (gameProfile != invalidGameProfile) {
			CompoundTag profileNbt = new CompoundTag();
			NbtUtils.writeGameProfile(profileNbt, gameProfile);
			compoundNBT.put("profile", profileNbt);
		}
		return compoundNBT;
	}


}

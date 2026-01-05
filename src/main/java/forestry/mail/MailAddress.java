package forestry.mail;

import com.google.common.base.Preconditions;
import com.mojang.authlib.GameProfile;
import forestry.api.mail.IMailAddress;
import forestry.api.mail.IPostalCarrier;
import forestry.core.utils.PlayerUtil;
import forestry.mail.carriers.PostalCarriers;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.StringUtils;

import java.util.Locale;
import java.util.UUID;

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
		return this.carrier;
	}

	@Override
	public String getName() {
		return this.gameProfile.getName();
	}

	@Override
	public boolean isValid() {
		return this.gameProfile.getName() != null && !PlayerUtil.isSameGameProfile(this.gameProfile, invalidGameProfile);
	}

	@Override
	public GameProfile getPlayerProfile() {
		if (!this.carrier.equals(PostalCarriers.PLAYER.get())) {
			return invalidGameProfile;
		}
		return this.gameProfile;
	}

	@Override
	public int hashCode() {
		return this.gameProfile.getName().hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof MailAddress address)) {
			return false;
		}

		return PlayerUtil.isSameGameProfile(address.gameProfile, this.gameProfile);
	}

	@Override
	public String toString() {
		String name = getName().toLowerCase(Locale.ENGLISH);
		if (getCarrier().equals(PostalCarriers.PLAYER.get())) {
			return this.carrier + "-" + name + '-' + this.gameProfile.getId();
		} else {
			return this.carrier + "-" + name;
		}
	}

	@Override
	public CompoundTag write(CompoundTag compoundNBT) {
		compoundNBT.putString("carrier", PostalCarriers.REGISTRY.get().getKey(this.carrier).toString());

		if (this.gameProfile != invalidGameProfile) {
			CompoundTag profileNbt = new CompoundTag();
			NbtUtils.writeGameProfile(profileNbt, this.gameProfile);
			compoundNBT.put("profile", profileNbt);
		}
		return compoundNBT;
	}


}

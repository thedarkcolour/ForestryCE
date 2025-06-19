package forestry.api.mail;

import com.mojang.authlib.GameProfile;
import forestry.api.core.INbtWritable;

public interface IMailAddress extends INbtWritable {
	IPostalCarrier getCarrier();

	String getName();

	boolean isValid();

	GameProfile getPlayerProfile();
}

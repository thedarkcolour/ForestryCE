package forestry.core.owner;

import com.mojang.authlib.GameProfile;
import net.minecraft.world.item.component.ResolvableProfile;

import javax.annotation.Nullable;

public interface IOwnerHandler {
	@Nullable
	GameProfile getOwner();

	void setOwner(ResolvableProfile owner);
}

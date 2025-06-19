package forestry.core.owner;

import com.mojang.authlib.GameProfile;
import net.minecraft.world.item.component.ResolvableProfile;

public enum FakeOwnerHandler implements IOwnerHandler {
	INSTANCE;

	@Override
	public GameProfile getOwner() {
		return null;
	}

	@Override
	public void setOwner(ResolvableProfile owner) {
	}
}

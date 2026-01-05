package forestry.core.owner;

import com.mojang.authlib.GameProfile;

public enum FakeOwnerHandler implements IOwnerHandler {
	INSTANCE;

	@Override
	public GameProfile getOwner() {
		return null;
	}

	@Override
	public void setOwner(GameProfile owner) {
	}
}

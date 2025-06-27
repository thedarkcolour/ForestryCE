package forestry.core.owner;

import net.minecraft.world.item.component.ResolvableProfile;

import javax.annotation.Nullable;

public interface IOwnerHandler {
	@Nullable
	ResolvableProfile getOwner();

	void setOwner(ResolvableProfile owner);
}

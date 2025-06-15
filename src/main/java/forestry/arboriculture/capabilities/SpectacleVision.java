package forestry.arboriculture.capabilities;

import forestry.api.core.ISpectacleVision;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public enum SpectacleVision implements ISpectacleVision {
	INSTANCE;

	@Override
	public boolean canSeePollination(Player player, ItemStack armor, boolean doSee) {
		return true;
	}
}

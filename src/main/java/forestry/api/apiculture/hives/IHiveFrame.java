package forestry.api.apiculture.hives;

import forestry.api.apiculture.IBeeHousing;
import forestry.api.apiculture.IBeeModifier;
import forestry.api.apiculture.bee.IBee;
import net.minecraft.world.item.ItemStack;

public interface IHiveFrame {
	/**
	 * Wears out a frame.
	 *
	 * @param housing IBeeHousing the frame is contained in.
	 * @param frame   ItemStack containing the actual frame.
	 * @param queen   Current queen in the caller.
	 * @param wear    Integer denoting the amount worn out. The wear modifier of the current beekeeping mode has already been taken into account.
	 * @return ItemStack containing the actual frame with adjusted damage, or Empty ItemStack if it has been broken.
	 */
	ItemStack frameUsed(IBeeHousing housing, ItemStack frame, IBee queen, int wear);

	/**
	 * @param frame ItemStack containing the actual frame.
	 * @return the {@link IBeeModifier} for this frame.
	 */
	IBeeModifier getBeeModifier(ItemStack frame);
}

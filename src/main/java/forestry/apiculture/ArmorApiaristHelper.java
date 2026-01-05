package forestry.apiculture;

import forestry.api.ForestryCapabilities;
import forestry.api.apiculture.IArmorApiaristHelper;
import forestry.api.apiculture.genetics.IBeeEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public class ArmorApiaristHelper implements IArmorApiaristHelper {
	@Override
	public boolean isArmorApiarist(ItemStack stack, LivingEntity entity, IBeeEffect cause, boolean doProtect) {
		if (stack.isEmpty()) {
			return false;
		}

		return stack.getCapability(ForestryCapabilities.ARMOR_APIARIST)
			.map(armorApiarist -> armorApiarist.protectEntity(entity, stack, cause, doProtect))
			.orElse(false);
	}

	@Override
	public int wearsItems(LivingEntity entity, @Nullable IBeeEffect cause, boolean doProtect) {
		int count = 0;

		for (ItemStack armorItem : entity.getAllSlots()) {
			if (isArmorApiarist(armorItem, entity, cause, doProtect)) {
				count++;
			}
		}

		return count;
	}
}

package forestry.api.apiculture;

import forestry.api.ForestryCapabilities;
import forestry.api.apiculture.bee.IBeeEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

/**
 * When implemented as a capability by armor items, protects the wearer from negative bee effects.
 *
 * @see ForestryCapabilities#BEE_PROTECTION
 */
public interface IBeeProtection {
	/**
	 * Called when the apiarist's armor acts as protection against an attack.
	 *
	 * @param entity  Entity being attacked
	 * @param armor   Armor item
	 * @param cause   Optional cause of attack, such as a bee effect identifier
	 * @param execute Whether or not to actually do the side effects of protection, use {@code false} if simulating
	 * @return Whether or not the armor should protect the player from that attack
	 */
	boolean doBeeProtection(LivingEntity entity, ItemStack armor, @Nullable IBeeEffect cause, boolean execute);

	/**
	 * Called when the apiarist's armor acts as protection against an attack.
	 *
	 * @param entity    Entity being attacked
	 * @param cause     Optional cause of attack, such as a bee effect identifier
	 * @param doProtect Whether or not to actually do the side effects of protection
	 * @return The number of valid Apiarist Armor pieces the player is wearing that are actually protecting.
	 * 4 means full protection.
	 */
	static int getBeeProtectionLevel(LivingEntity entity, @Nullable IBeeEffect cause, boolean doProtect) {
		int count = 0;

		for (ItemStack armorItem : entity.getArmorSlots()) {
			IBeeProtection capability = armorItem.getCapability(ForestryCapabilities.BEE_PROTECTION);
			if (capability != null && capability.doBeeProtection(entity, armorItem, cause, doProtect)) {
				count++;
			}
		}

		return count;
	}
}

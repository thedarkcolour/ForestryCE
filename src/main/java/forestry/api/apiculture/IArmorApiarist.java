package forestry.api.apiculture;

import forestry.api.ForestryCapabilities;
import forestry.api.apiculture.genetics.IBeeEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

/**
 * When implemented as a capability by armor items, protects the wearer from negative bee effects.
 *
 * @see ForestryCapabilities#BEE_PROTECTION
 */
public interface IArmorApiarist {
	/**
	 * Called when the apiarist's armor acts as protection against an attack.
	 *
	 * @param entity  Entity being attacked
	 * @param armor   Armor item
	 * @param cause   Optional cause of attack, such as a bee effect identifier
	 * @param execute Whether or not to actually do the side effects of protection, use {@code false} if simulating
	 * @return Whether or not the armor should protect the player from that attack
	 */
	boolean protectEntity(LivingEntity entity, ItemStack armor, @Nullable IBeeEffect cause, boolean execute);
}

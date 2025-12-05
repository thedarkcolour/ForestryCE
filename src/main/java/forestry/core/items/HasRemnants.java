package forestry.core.items;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;

import java.util.function.Consumer;
import java.util.function.Supplier;

public interface HasRemnants {
	class Pickaxe extends PickaxeItem implements HasRemnants {
		private final Supplier<ItemStack> remnants;

		public Pickaxe(Tier tier, int damageBonus, float speedModifier, Properties properties, Supplier<ItemStack> remnants) {
			super(tier, damageBonus, speedModifier, properties);
			this.remnants = remnants;
		}

		@Override
		public <T extends LivingEntity> int damageItem(ItemStack stack, int amount, T entity, Consumer<T> onBroken) {
			if (stack.getDamageValue() + amount >= stack.getMaxDamage()) {
				if (entity instanceof Player player) {
					// make sure it's really broken
					stack.shrink(1);
					player.getInventory().add(this.remnants.get());
				}
			}
			return super.damageItem(stack, amount, entity, onBroken);
		}
	}

	class Shovel extends ShovelItem implements HasRemnants {
		private final Supplier<ItemStack> remnants;

		public Shovel(Tier tier, float damageBonus, float speedModifier, Properties properties, Supplier<ItemStack> remnants) {
			super(tier, damageBonus, speedModifier, properties);
			this.remnants = remnants;
		}

		@Override
		public <T extends LivingEntity> int damageItem(ItemStack stack, int amount, T entity, Consumer<T> onBroken) {
			if (stack.getDamageValue() + amount >= stack.getMaxDamage()) {
				if (entity instanceof Player player) {
					// make sure it's really broken
					stack.shrink(1);
					player.getInventory().add(this.remnants.get());
				}
			}
			return super.damageItem(stack, amount, entity, onBroken);
		}
	}

	class Axe extends AxeItem implements HasRemnants {
		private final Supplier<ItemStack> remnants;

		public Axe(Tier tier, float damageBonus, float speedModifier, Properties properties, Supplier<ItemStack> remnants) {
			super(tier, damageBonus, speedModifier, properties);
			this.remnants = remnants;
		}

		@Override
		public <T extends LivingEntity> int damageItem(ItemStack stack, int amount, T entity, Consumer<T> onBroken) {
			if (stack.getDamageValue() + amount >= stack.getMaxDamage()) {
				if (entity instanceof Player player) {
					// make sure it's really broken
					stack.shrink(1);
					player.getInventory().add(this.remnants.get());
				}
			}
			return super.damageItem(stack, amount, entity, onBroken);
		}
	}

	class Sword extends SwordItem implements HasRemnants {
		private final Supplier<ItemStack> remnants;

		public Sword(Tier tier, int damageBonus, float speedModifier, Properties properties, Supplier<ItemStack> remnants) {
			super(tier, damageBonus, speedModifier, properties);
			this.remnants = remnants;
		}

		@Override
		public <T extends LivingEntity> int damageItem(ItemStack stack, int amount, T entity, Consumer<T> onBroken) {
			if (stack.getDamageValue() + amount >= stack.getMaxDamage()) {
				if (entity instanceof Player player) {
					// make sure it's really broken
					stack.shrink(1);
					player.getInventory().add(this.remnants.get());
				}
			}
			return super.damageItem(stack, amount, entity, onBroken);
		}
	}

	class Hoe extends HoeItem implements HasRemnants {
		private final Supplier<ItemStack> remnants;

		public Hoe(Tier tier, int damageBonus, float speedModifier, Properties properties, Supplier<ItemStack> remnants) {
			super(tier, damageBonus, speedModifier, properties);
			this.remnants = remnants;
		}

		@Override
		public <T extends LivingEntity> int damageItem(ItemStack stack, int amount, T entity, Consumer<T> onBroken) {
			if (stack.getDamageValue() + amount >= stack.getMaxDamage()) {
				if (entity instanceof Player player) {
					// make sure it's really broken
					stack.shrink(1);
					player.getInventory().add(this.remnants.get());
				}
			}
			return super.damageItem(stack, amount, entity, onBroken);
		}
	}
}

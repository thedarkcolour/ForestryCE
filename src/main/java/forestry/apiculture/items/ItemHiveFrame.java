package forestry.apiculture.items;

import forestry.api.apiculture.IBeeHousing;
import forestry.api.apiculture.IBeeModifier;
import forestry.api.apiculture.genetics.IBee;
import forestry.api.apiculture.hives.IHiveFrame;
import forestry.api.genetics.IGenome;
import forestry.core.items.ItemForestry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class ItemHiveFrame extends ItemForestry implements IHiveFrame {
	private final HiveFrameBeeModifier beeModifier;

	public ItemHiveFrame(int maxDamage, float geneticDecay) {
		super(new Item.Properties().durability(maxDamage));

		this.beeModifier = new HiveFrameBeeModifier(geneticDecay);
	}

	@Override
	public int getMaxStackSize(ItemStack stack) {
		return 64;
	}

	@Override
	public ItemStack frameUsed(IBeeHousing housing, ItemStack frame, IBee queen, int wear) {
		if (frame.hurt(wear, housing.getWorldObj().getRandom(), null)) {
			return ItemStack.EMPTY;
		} else {
			return frame;
		}
	}

	@Override
	public IBeeModifier getBeeModifier(ItemStack frame) {
		return this.beeModifier;
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag advanced) {
		super.appendHoverText(stack, world, tooltip, advanced);
        this.beeModifier.addInformation(tooltip);
		if (!stack.isDamaged()) {
			tooltip.add(Component.translatable("item.forestry.durability", stack.getMaxDamage()));
		}
	}

	private static class HiveFrameBeeModifier implements IBeeModifier {
		private static final float production = 2f;
		private final float geneticDecay;

		public HiveFrameBeeModifier(float geneticDecay) {
			this.geneticDecay = geneticDecay;
		}

		@Override
		public float modifyProductionSpeed(IGenome genome, float currentSpeed) {
			return currentSpeed < 10f ? currentSpeed * production : 1f;
		}

		@Override
		public float modifyGeneticDecay(IGenome genome, float currentDecay) {
			return this.geneticDecay;
		}

		public void addInformation(List<Component> tooltip) {
			tooltip.add(Component.translatable("item.forestry.bee.modifier.production", production));
			tooltip.add(Component.translatable("item.forestry.bee.modifier.genetic.decay", this.geneticDecay));
		}
	}
}

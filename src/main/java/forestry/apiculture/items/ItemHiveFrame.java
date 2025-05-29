package forestry.apiculture.items;

import forestry.api.apiculture.IBeeHousing;
import forestry.api.apiculture.IBeeModifier;
import forestry.api.apiculture.genetics.IBee;
import forestry.api.apiculture.hives.IHiveFrame;
import forestry.api.genetics.IGenome;
import forestry.core.items.ItemForestry;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.mutable.MutableBoolean;

import javax.annotation.Nullable;
import java.util.List;

public class ItemHiveFrame extends ItemForestry implements IHiveFrame, IBeeModifier {
	private static final float PRODUCTION_MODIFIER = 2f;

	private final float geneticDecay;

	public ItemHiveFrame(int maxDamage, float geneticDecay) {
		super(new Item.Properties().durability(maxDamage));

		this.geneticDecay = geneticDecay;
	}

	@Override
	public int getMaxStackSize(ItemStack stack) {
		return 64;
	}

	@Override
	public ItemStack frameUsed(IBeeHousing housing, ItemStack frame, IBee queen, int wear) {
		MutableBoolean broken = new MutableBoolean(false);
		frame.hurtAndBreak(wear, ((ServerLevel) housing.getLevel()), null, unused -> broken.setTrue());

		if (broken.isTrue()) {
			return ItemStack.EMPTY;
		} else {
			return frame;
		}
	}

	@Override
	public IBeeModifier getBeeModifier(ItemStack frame) {
		return this;
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag advanced) {
		super.appendHoverText(stack, world, tooltip, advanced);

		tooltip.add(Component.translatable("item.forestry.bee.modifier.production", PRODUCTION_MODIFIER));
		tooltip.add(Component.translatable("item.forestry.bee.modifier.genetic.decay", this.geneticDecay));

		if (!stack.isDamaged()) {
			tooltip.add(Component.translatable("item.forestry.durability", stack.getMaxDamage()));
		}
	}

	@Override
	public float modifyProductionSpeed(IGenome genome, float currentSpeed) {
		return currentSpeed < 10f ? currentSpeed * PRODUCTION_MODIFIER : 1f;
	}

	@Override
	public float modifyGeneticDecay(IGenome genome, float currentDecay) {
		return currentDecay * this.geneticDecay;
	}
}

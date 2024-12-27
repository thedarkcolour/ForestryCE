package forestry.apiculture.items;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import forestry.api.apiculture.IBeeHousing;
import forestry.api.apiculture.IBeeModifier;
import forestry.api.apiculture.genetics.IBee;
import forestry.api.apiculture.genetics.IBeeSpecies;
import forestry.api.apiculture.hives.IHiveFrame;
import forestry.api.genetics.IGenome;
import forestry.api.genetics.IMutation;
import forestry.core.items.ItemForestry;

import org.jetbrains.annotations.Nullable;

// 100% mutation chance. 100% production chance. 0% lifespan.
public class ItemCreativeHiveFrame extends ItemForestry implements IHiveFrame {
	public static final String NBT_FORCE_MUTATIONS = "force_mutations";

	public ItemCreativeHiveFrame() {
		super(new Item.Properties().rarity(Rarity.EPIC));
	}

	@Override
	public ItemStack frameUsed(IBeeHousing housing, ItemStack frame, IBee queen, int wear) {
		return frame;
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag advanced) {
		super.appendHoverText(stack, world, tooltip, advanced);

		tooltip.add(Component.translatable("item.forestry.bee.modifier.production", Modifier.PRODUCTION));
		tooltip.add(Component.translatable("item.forestry.bee.modifier.genetic.decay", Modifier.GENETIC_DECAY));

		if (hasForceMutations(stack)) {
			tooltip.add(Component.literal("Maximum mutation chances").withStyle(ChatFormatting.LIGHT_PURPLE));
		} else {
			tooltip.add(Component.literal("Base mutation chances").withStyle(ChatFormatting.GRAY));
		}
	}

	@Override
	public IBeeModifier getBeeModifier(ItemStack frame) {
		return hasForceMutations(frame) ? Modifier.FORCE_MUTATIONS : Modifier.BASE_MUTATIONS;
	}

	public static boolean hasForceMutations(ItemStack stack) {
		return stack.getTag() != null && stack.getTag().contains(NBT_FORCE_MUTATIONS);
	}

	private enum Modifier implements IBeeModifier {
		BASE_MUTATIONS,
		FORCE_MUTATIONS {
			@Override
			public float modifyMutationChance(IGenome genome, IGenome mate, IMutation<IBeeSpecies> mutation, float currentChance) {
				return MUTATION;
			}
		};

		private static final float PRODUCTION = 10000f;
		private static final float POLLINATION = 100f;
		private static final float MUTATION = 100f;
		private static final float GENETIC_DECAY = 0f;

		@Override
		public float modifyAging(IGenome genome, @Nullable IGenome mate, float currentAging) {
			return -1f;
		}

		@Override
		public float modifyProductionSpeed(IGenome genome, float currentSpeed) {
			return PRODUCTION;
		}

		@Override
		public float modifyPollination(IGenome genome, float currentPollination) {
			return POLLINATION;
		}

		@Override
		public float modifyGeneticDecay(IGenome genome, float currentDecay) {
			return GENETIC_DECAY;
		}

		@Override
		public boolean isSealed() {
			return true;
		}

		@Override
		public boolean isAlwaysActive(IGenome genome) {
			return true;
		}

		@Override
		public boolean isSunlightSimulated() {
			return true;
		}

		@Override
		public boolean isHellish() {
			return true;
		}
	}
}

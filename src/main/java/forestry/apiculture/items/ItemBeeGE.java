package forestry.apiculture.items;

import forestry.api.apiculture.bee.BeeLifeStage;
import forestry.api.apiculture.bee.IBee;
import forestry.api.apiculture.bee.IBeeSpecies;
import forestry.api.genetics.ISpeciesType;
import forestry.api.genetics.capability.IIndividualHandlerItem;
import forestry.core.genetics.ItemGE;
import forestry.core.items.definitions.IColoredItem;
import forestry.core.utils.SpeciesUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class ItemBeeGE extends ItemGE implements IColoredItem {
	public ItemBeeGE(BeeLifeStage type) {
		super(type != BeeLifeStage.DRONE ? new Item.Properties().stacksTo(1) : new Item.Properties(), type);
	}

	@Override
	protected ISpeciesType<?, ?> getType() {
		return SpeciesUtil.BEE_TYPE.get();
	}

	@Override
	protected IBeeSpecies getSpecies(ItemStack stack) {
		return IIndividualHandlerItem.getSpecies(stack, SpeciesUtil.BEE_TYPE.get());
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> list, TooltipFlag flag) {
		if (this.stage != BeeLifeStage.DRONE && IIndividualHandlerItem.getIndividual(stack) instanceof IBee bee) {
			if (bee.isPristine()) {
				list.add(Component.translatable("for.bees.stock.pristine").withStyle(ChatFormatting.YELLOW, ChatFormatting.ITALIC));
			} else {
				list.add(Component.translatable("for.bees.stock.ignoble").withStyle(ChatFormatting.YELLOW));
			}
		}

		super.appendHoverText(stack, level, list, flag);
	}

	@Override
	public int getColorFromItemStack(ItemStack stack, int tintIndex) {
		IBeeSpecies species = getSpecies(stack);

		return switch (tintIndex) {
			case 2 -> species.getStripes();
			case 1 -> species.getBody();
			case 0 -> species.getOutline();
			default -> 0xffffff;
		};
	}

	public final BeeLifeStage getStage() {
		return (BeeLifeStage) this.stage;
	}
}

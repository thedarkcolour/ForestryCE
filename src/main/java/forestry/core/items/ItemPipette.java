package forestry.core.items;

import forestry.api.core.IToolPipette;
import forestry.core.features.CoreDataComponents;
import forestry.core.items.definitions.IColoredItem;
import forestry.core.utils.RenderUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.SimpleFluidContent;

import java.util.List;

public class ItemPipette extends ItemForestry implements IToolPipette, IColoredItem {
	public ItemPipette() {
		super(new Properties().component(CoreDataComponents.FLUID_CONTENTS, SimpleFluidContent.EMPTY).stacksTo(1));
	}

	@Override
	public boolean canPipette(ItemStack stack) {
		SimpleFluidContent contained = stack.get(CoreDataComponents.FLUID_CONTENTS);
		return contained == null || contained.getAmount() < FluidType.BUCKET_VOLUME;
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> list, TooltipFlag flag) {
		super.appendHoverText(stack, ctx, list, flag);

		SimpleFluidContent contained = stack.get(CoreDataComponents.FLUID_CONTENTS);
		if (contained != null) {
			list.add(contained.copy().getHoverName().copy().append(" (" + contained.getAmount() + " mb)").withStyle(ChatFormatting.GRAY));
		}
	}

	@Override
	public int getColorFromItemStack(ItemStack stack, int tintIndex) {
		if (tintIndex == 1) {
			SimpleFluidContent contents = stack.get(CoreDataComponents.FLUID_CONTENTS);

			if (contents != null) {
				return RenderUtil.getFluidColor(contents.getFluid());
			}
		}
		return 0xffffff;
	}
}

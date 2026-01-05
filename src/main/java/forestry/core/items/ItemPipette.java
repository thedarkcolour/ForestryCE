package forestry.core.items;

import forestry.api.core.IToolPipette;
import forestry.core.fluids.PipetteContents;
import forestry.core.items.definitions.IColoredItem;
import forestry.core.utils.RenderUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStack;

import javax.annotation.Nullable;
import java.util.List;

public class ItemPipette extends ItemForestry implements IToolPipette, IColoredItem {
	public ItemPipette() {
		super(new Properties().stacksTo(1));
	}

	@Override
	public boolean canPipette(ItemStack itemstack) {
		PipetteContents contained = PipetteContents.create(itemstack);
		return contained == null || !contained.isFull();
	}

	@Override
	public void appendHoverText(ItemStack itemstack, @Nullable Level world, List<Component> list, TooltipFlag flag) {
		super.appendHoverText(itemstack, world, list, flag);

		PipetteContents contained = PipetteContents.create(itemstack);
		if (contained != null) {
			contained.addTooltip(list);
		}
	}

	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
		return new FluidHandlerItemStack(stack, FluidType.BUCKET_VOLUME);
	}

	@Override
	public int getColorFromItemStack(ItemStack stack, int tintIndex) {
		if (tintIndex == 1) {
			PipetteContents contents = PipetteContents.create(stack);

			if (contents != null) {
				return RenderUtil.getFluidColor(contents.getContents().getFluid());
			}
		}
		return 0xffffff;
	}
}

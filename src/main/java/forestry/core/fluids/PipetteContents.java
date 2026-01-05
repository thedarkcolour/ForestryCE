package forestry.core.fluids;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.FluidUtil;

import javax.annotation.Nullable;
import java.util.List;

public class PipetteContents {
	private final FluidStack contents;

	@Nullable
	public static PipetteContents create(ItemStack itemStack) {
		FluidStack contents = FluidUtil.getFluidContained(itemStack).orElse(FluidStack.EMPTY);
		if (contents.isEmpty()) {
			return null;
		}
		return new PipetteContents(contents);
	}

	public PipetteContents(FluidStack contents) {
		this.contents = contents;
	}

	public FluidStack getContents() {
		return this.contents;
	}

	public boolean isFull() {
		return this.contents.getAmount() >= FluidType.BUCKET_VOLUME;
	}

	public void addTooltip(List<Component> list) {
		list.add(this.contents.getDisplayName().copy().append(" (" + this.contents.getAmount() + " mb)").withStyle(ChatFormatting.GRAY));
	}
}

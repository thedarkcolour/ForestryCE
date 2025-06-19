package forestry.core.fluids;

import com.google.common.base.Preconditions;
import forestry.api.core.tooltips.ToolTip;
import forestry.core.utils.ModUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

public class FilteredTank extends StandardTank {
	private Supplier<Set<ResourceLocation>> filters = Set::of;

	public FilteredTank(int capacity) {
		super(capacity);
		setValidator(this::fluidMatchesFilter);
	}

	public FilteredTank(int capacity, boolean canFill, boolean canDrain) {
		super(capacity, canFill, canDrain);
		setValidator(this::fluidMatchesFilter);
	}

	public FilteredTank setFilter(Supplier<Set<ResourceLocation>> filters) {
		this.filters = Preconditions.checkNotNull(filters);
		return this;
	}

	public FilteredTank setFilters(Collection<Fluid> filters) {
		Set<ResourceLocation> set = new HashSet<>();
		this.filters = () -> set;
		for (Fluid fluid : filters) {
			set.add(ModUtil.getRegistryName(fluid));
		}
		return this;
	}

	private boolean fluidMatchesFilter(FluidStack resource) {
		return resource.getFluid() != Fluids.EMPTY && this.filters.get().contains(ModUtil.getRegistryName(resource.getFluid()));
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	protected void refreshTooltip() {
		if (hasFluid()) {
			super.refreshTooltip();
			return;
		}

		ToolTip toolTip = getToolTip();
		toolTip.clear();
		Set<ResourceLocation> filters = this.filters.get();

		if (Screen.hasShiftDown() || filters.size() < 5) {
			for (ResourceLocation filterName : filters) {
				Fluid fluidFilter = BuiltInRegistries.FLUID.get(filterName);
				FluidType attributes = fluidFilter.getFluidType();
				Rarity rarity = attributes.getRarity();
				FluidStack filterFluidStack = new FluidStack(fluidFilter, 1);
				toolTip.add(filterFluidStack.getHoverName().copy().withStyle(rarity.getStyleModifier()));
			}
		} else {
			Component tmiComponent = Component.literal("<")
				.append(Component.translatable("for.gui.tooltip.tmi"))
				.append(Component.literal(">"));
			toolTip.add(tmiComponent, ChatFormatting.ITALIC);
		}
		toolTip.add(Component.translatable("for.gui.tooltip.liquid.amount", getFluidAmount(), getCapacity()));
	}
}

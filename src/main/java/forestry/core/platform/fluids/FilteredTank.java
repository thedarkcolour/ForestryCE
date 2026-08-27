package forestry.core.platform.fluids;

import com.google.common.base.Preconditions;
import com.google.common.base.Suppliers;
import forestry.api.core.tooltips.ToolTip;
import forestry.core.platform.util.ModUtil;
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
import java.util.stream.Collectors;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.datamaps.DataMapType;

public class FilteredTank extends StandardTank {
	private Supplier<Set<ResourceLocation>> filters = Suppliers.ofInstance(Set.of());

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

	/**
	 * Accepts every fluid with an entry in the data map. The validator reads the entry directly, so only
	 * the tooltip pays to build the set of names
	 *
	 * @param dataMap The fluid data map that decides what this tank holds
	 */
	public FilteredTank setFilter(DataMapType<Fluid, ?> dataMap) {
		this.filters = () -> BuiltInRegistries.FLUID.getDataMap(dataMap).keySet().stream()
				.map(ResourceKey::location)
				.collect(Collectors.toSet());
		setValidator(stack -> !stack.isEmpty() && stack.getFluidHolder().getData(dataMap) != null);
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
				if (fluidFilter == Fluids.EMPTY) {
					continue;
				}
				FluidType attributes = fluidFilter.getFluidType();
				Rarity rarity = attributes.getRarity();
				if (rarity == null) {
					rarity = Rarity.COMMON;
				}
				FluidStack filterFluidStack = new FluidStack(fluidFilter, 1);
				toolTip.add(filterFluidStack.getHoverName(), rarity.color());
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

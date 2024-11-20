package forestry.farming.logic;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.ToIntFunction;

import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import forestry.api.farming.IFarmHousing;
import forestry.api.farming.IFarmLogic;
import forestry.api.farming.IFarmType;
import forestry.api.farming.IFarmable;
import forestry.api.farming.IWaterConsumption;
import forestry.api.farming.Soil;

public final class FarmType implements IFarmType {
	private final ResourceLocation id;
	private final ImmutableSet<Soil> soils;
	private final ItemStack icon;
	private final ImmutableList<IFarmable> farmables;
	private final ToIntFunction<IFarmHousing> fertilizerConsumption;
	private final IWaterConsumption waterConsumption;
	private final String translationKey;
	private final IFarmLogic manualLogic;
	private final IFarmLogic managedLogic;

	public FarmType(ResourceLocation id, ItemStack icon, BiFunction<IFarmType, Boolean, IFarmLogic> logicFactory, ImmutableList<IFarmable> farmables, ToIntFunction<IFarmHousing> fertilizerConsumption, IWaterConsumption waterConsumption, ImmutableSet<Soil> soils) {
		this.id = id;
		this.icon = icon;
		this.soils = soils;
		this.farmables = farmables;
		this.fertilizerConsumption = fertilizerConsumption;
		this.waterConsumption = waterConsumption;
		this.translationKey = Util.makeDescriptionId("farm", this.id);

		// Build these last so that the other fields are initialized in case they're used
		this.manualLogic = logicFactory.apply(this, true);
		this.managedLogic = logicFactory.apply(this, false);
	}

	@Override
	public List<IFarmable> getFarmables() {
		return this.farmables;
	}

	@Override
	public IFarmLogic getLogic(boolean manual) {
		return manual ? this.manualLogic : this.managedLogic;
	}

	@Override
	public boolean isAcceptedSoil(BlockState state) {
		for (Soil soil : this.soils) {
			BlockState soilState = soil.soilState();
			if (soilState.getBlock() == state.getBlock()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isAcceptedResource(ItemStack stack) {
		for (Soil soil : this.soils) {
			if (ItemStack.isSameItem(soil.resource(), stack)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public int getFertilizerConsumption(IFarmHousing housing) {
		return this.fertilizerConsumption.applyAsInt(housing);
	}

	@Override
	public int getWaterConsumption(IFarmHousing housing, float hydrationModifier) {
		return this.waterConsumption.get(housing, hydrationModifier);
	}

	@Override
	public Component getDisplayName(boolean manual) {
		String type = manual ? "farm.forestry.grammar.manual" : "farm.forestry.grammar.managed";
		return Component.translatable(type, Component.translatable(this.translationKey));
	}

	@Override
	public String getTranslationKey() {
		return this.translationKey;
	}

	@Override
	public ItemStack getIcon() {
		return this.icon;
	}

	@Override
	public boolean isAcceptedSeedling(ItemStack stack) {
		for (IFarmable farmable : this.farmables) {
			if (farmable.isGermling(stack)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isAcceptedWindfall(ItemStack stack) {
		for (IFarmable farmable : this.farmables) {
			if (farmable.isWindfall(stack)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Collection<Soil> getSoils() {
		return this.soils;
	}
}

package forestry.agriculture.farmlogic;

import com.google.common.collect.ImmutableMap;
import forestry.api.ForestryDataMaps;
import forestry.api.agriculture.IFarmType;
import forestry.api.agriculture.IFarmingManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public class FarmingManager implements IFarmingManager {
	private final ImmutableMap<ResourceLocation, IFarmType> farmTypes;

	public FarmingManager(ImmutableMap<ResourceLocation, IFarmType> farmTypes) {
		this.farmTypes = farmTypes;
	}

	@Override
	public int getFertilizeValue(ItemStack stack) {
		Integer value = stack.getItemHolder().getData(ForestryDataMaps.FARM_FERTILIZERS);
		return value != null ? value : 0;
	}

	@Nullable
	@Override
	public IFarmType getFarmType(ResourceLocation id) {
		return this.farmTypes.get(id);
	}
}

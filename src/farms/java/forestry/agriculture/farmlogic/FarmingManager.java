package forestry.agriculture.farmlogic;

import com.google.common.collect.ImmutableMap;
import forestry.api.agriculture.IFarmType;
import forestry.api.agriculture.IFarmingManager;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import forestry.api.ForestryDataMaps;

public class FarmingManager implements IFarmingManager {
	private final Object2IntOpenHashMap<Item> fertilizers;
	private final ImmutableMap<ResourceLocation, IFarmType> farmTypes;

	public FarmingManager(Object2IntOpenHashMap<Item> fertilizers, ImmutableMap<ResourceLocation, IFarmType> farmTypes) {
		this.farmTypes = farmTypes;
		this.fertilizers = fertilizers;
	}

	@Override
	public int getFertilizeValue(ItemStack stack) {
		Integer value = stack.getItemHolder().getData(ForestryDataMaps.FARM_FERTILIZERS);
		if (value != null) {
			return value;
		}
		// Falls back to the deprecated plugin registration, so a mod that has not moved to the data
		// map keeps working. A pack overrides either one by writing the data map entry itself
		return this.fertilizers.getInt(stack.getItem());
	}

	@Nullable
	@Override
	public IFarmType getFarmType(ResourceLocation id) {
		return this.farmTypes.get(id);
	}
}

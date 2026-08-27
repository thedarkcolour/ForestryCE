package forestry.agriculture.plugin;

import com.google.common.collect.ImmutableMap;
import forestry.api.agriculture.IFarmLogic;
import forestry.api.agriculture.IFarmType;
import forestry.api.plugin.IFarmTypeBuilder;
import forestry.api.plugin.IFarmingRegistration;
import forestry.apiimpl.plugin.ModifiableRegistrar;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.function.BiFunction;
import java.util.function.Consumer;

public class FarmingRegistration implements IFarmingRegistration {
	private final ModifiableRegistrar<ResourceLocation, IFarmTypeBuilder, FarmTypeBuilder> farmTypes = new ModifiableRegistrar<>(IFarmTypeBuilder.class);
	private final Object2IntOpenHashMap<Item> fertilizers = new Object2IntOpenHashMap<>();

	@Override
	public IFarmTypeBuilder createFarmType(ResourceLocation id, BiFunction<IFarmType, Boolean, IFarmLogic> logicFactory, ItemStack icon) {
		return this.farmTypes.create(id, new FarmTypeBuilder(id, logicFactory, icon));
	}

	@Override
	public void modifyFarmType(ResourceLocation id, Consumer<IFarmTypeBuilder> action) {
		this.farmTypes.modify(id, action);
	}

	@Override
	@Deprecated(forRemoval = true)
	@SuppressWarnings("removal")
	public void registerFertilizer(Item fertilizer, int amount) {
		this.fertilizers.put(fertilizer, amount);
	}

	public Object2IntOpenHashMap<Item> getFertilizers() {
		return this.fertilizers;
	}

	public ImmutableMap<ResourceLocation, IFarmType> buildFarmTypes() {
		return this.farmTypes.build(FarmTypeBuilder::build);
	}
}

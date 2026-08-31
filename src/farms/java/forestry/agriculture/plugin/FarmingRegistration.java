package forestry.agriculture.plugin;

import com.google.common.collect.ImmutableMap;
import forestry.api.agriculture.IFarmLogic;
import forestry.api.agriculture.IFarmType;
import forestry.api.plugin.IFarmTypeBuilder;
import forestry.api.plugin.IFarmingRegistration;
import forestry.apiimpl.plugin.ModifiableRegistrar;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.function.BiFunction;
import java.util.function.Consumer;

public class FarmingRegistration implements IFarmingRegistration {
	private final ModifiableRegistrar<ResourceLocation, IFarmTypeBuilder, FarmTypeBuilder> farmTypes = new ModifiableRegistrar<>(IFarmTypeBuilder.class);

	@Override
	public IFarmTypeBuilder createFarmType(ResourceLocation id, BiFunction<IFarmType, Boolean, IFarmLogic> logicFactory, ItemStack icon) {
		return this.farmTypes.create(id, new FarmTypeBuilder(id, logicFactory, icon));
	}

	@Override
	public void modifyFarmType(ResourceLocation id, Consumer<IFarmTypeBuilder> action) {
		this.farmTypes.modify(id, action);
	}

	public ImmutableMap<ResourceLocation, IFarmType> buildFarmTypes() {
		return this.farmTypes.build(FarmTypeBuilder::build);
	}
}

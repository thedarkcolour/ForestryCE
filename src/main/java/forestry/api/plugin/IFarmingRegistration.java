package forestry.api.plugin;

import forestry.api.agriculture.IFarmLogic;
import forestry.api.agriculture.IFarmType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * Register farm related data here.
 * Obtain an instance from {@link IForestryPlugin#registerFarming}.
 */
public interface IFarmingRegistration {
	/**
	 * Creates a new farm type.
	 * Calling {@link IFarmTypeBuilder#setFertilizerConsumption} and {@link IFarmTypeBuilder#setWaterConsumption} is required.
	 *
	 * @param id           The unique ID of the farm type. See {@link forestry.api.agriculture.ForestryFarmTypes} for defaults.
	 * @param logicFactory The factory for creating IFarmLogic instances. The boolean is whether the logic should be manual.
	 * @param icon         The item
	 * @return A builder to customize the properties of this farm type.
	 */
	IFarmTypeBuilder createFarmType(ResourceLocation id, BiFunction<IFarmType, Boolean, IFarmLogic> logicFactory, ItemStack icon);

	/**
	 * Modifies an existing farm type.
	 *
	 * @param id     The ID of the farm type to modify. See {@link forestry.api.agriculture.ForestryFarmTypes} for defaults.
	 * @param action The modifications to this farm type.
	 */
	void modifyFarmType(ResourceLocation id, Consumer<IFarmTypeBuilder> action);
}

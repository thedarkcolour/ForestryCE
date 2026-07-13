package forestry.api.fuels;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;

import java.util.Map;

// todo get rid of the ItemStack maps
public class FuelManager {
	/**
	 * Add new resources for the moistener here (i.e. wheat)
	 */
	public static Map<ItemStack, MoistenerFuel> moistenerResource;
	/**
	 * Add new substrates for the rainmaker here
	 */
	public static Map<ItemStack, RainSubstrate> rainSubstrate;
	/**
	 * Add new fuels for EngineBronze (= biogas engine) here
	 */
	public static Map<Fluid, EngineBronzeFuel> biogasEngineFuel;
	/**
	 * Add new fuels for EngineCopper (= peat-fired engine) here
	 */
	public static Map<ItemStack, EngineCopperFuel> peatEngineFuel;

}

package forestry.api.fuels;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;

import java.util.Map;

// todo get rid of the ItemStack maps
public class FuelManager {
	/**
	 * Add new fuels for the fermenter here (i.e. fertilizer).
	 */
	public static Map<ItemStack, FermenterFuel> fermenterFuel;
	/**
	 * Add new resources for the moistener here (i.e. wheat)
	 */
	public static Map<ItemStack, MoistenerFuel> moistenerResource;
	/**
	 * Add new fuels for EngineBronze (= biogas engine) here
	 */
	public static Map<Fluid, EngineBronzeFuel> biogasEngineFuel;
	/**
	 * Add new fuels for EngineCopper (= peat-fired engine) here
	 */
	public static Map<ItemStack, EngineCopperFuel> peatEngineFuel;

}

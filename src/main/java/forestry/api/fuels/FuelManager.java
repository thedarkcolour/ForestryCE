package forestry.api.fuels;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;

import java.util.Map;

@Deprecated(forRemoval = true)
public class FuelManager {
	public static Map<ItemStack, FermenterFuel> fermenterFuel;
	public static Map<ItemStack, MoistenerFuel> moistenerResource;
	public static Map<ItemStack, RainmakerFuel> rainSubstrate;
	/**
	 * Add new fuels for EngineBronze (= biogas engine) here
	 */
	public static Map<Fluid, BiogasEngineFuel> biogasEngineFuel;
	/**
	 * Add new fuels for EngineCopper (= peat-fired engine) here
	 */
	public static Map<ItemStack, PeatEngineFuel> peatEngineFuel;

}

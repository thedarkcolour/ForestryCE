package forestry.api.fuels;

import net.minecraft.world.item.ItemStack;

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
	 * Add new substrates for the rainmaker here
	 */
	public static Map<ItemStack, RainSubstrate> rainSubstrate;
}

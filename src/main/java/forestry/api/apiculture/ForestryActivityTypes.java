package forestry.api.apiculture;

import net.minecraft.resources.ResourceLocation;

import forestry.api.ForestryConstants;

/**
 * IDs for all activity types available in base Forestry.
 * Use {@link forestry.api.plugin.IApicultureRegistration#registerActivityType} to register your own.
 */
public class ForestryActivityTypes {
	/**
	 * Diurnal bees work during daytime, and sleep during nighttime.
	 * Active during ticks 0 to 12000.
	 */
	public static final ResourceLocation DIURNAL = ForestryConstants.forestry("activity_diurnal");
	/**
	 * Nocturnal bees work during nighttime, and sleep during daytime.
	 * Active during ticks 12000 to 24000.
	 */
	public static final ResourceLocation NOCTURNAL = ForestryConstants.forestry("activity_nocturnal");
	/**
	 * Crepuscular bees work during the sunrise and sunset.
	 * Active during ticks 0 to 1000 and 12000 to 13000.
	 */
	public static final ResourceLocation CREPUSCULAR = ForestryConstants.forestry("activity_crepuscular");
	/**
	 * Metaturnal bees work during both sunrise and sunset. They never sleep.
	 * Active during ticks 0 to 24000.
	 */
	public static final ResourceLocation METATURNAL = ForestryConstants.forestry("activity_metaturnal");
}

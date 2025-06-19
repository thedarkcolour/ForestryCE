package forestry.core.config;

import net.neoforged.neoforge.fluids.FluidType;

public class Constants {
	// System
	public static final int FLUID_PER_HONEY_DROP = 100;

	public static final int[] SLOTS_NONE = new int[0];

	public static final String TRANSLATION_KEY_ITEM = "item.forestry.";

	// Textures
	public static final String TEXTURE_PATH_GUI = "textures/gui";
	public static final String TEXTURE_PATH_BLOCK = "textures/block";
	public static final String TEXTURE_PATH_ITEM = "textures/item";

	// Food stuff
	public static final int FOOD_AMBROSIA_HEAL = 8;

	public static final int APIARY_MIN_LEVEL_LIGHT = 11;
	public static final int APIARY_BREEDING_TIME = 100;

	// Factory
	public static final int PROCESSOR_TANK_CAPACITY = 10 * FluidType.BUCKET_VOLUME;

	public static final int MACHINE_MAX_ENERGY = 40000;

	// Storage
	public static final int RAINTANK_TANK_CAPACITY = 30 * FluidType.BUCKET_VOLUME;
	public static final int RAINTANK_AMOUNT_PER_UPDATE = 10;
	public static final int RAINTANK_FILLING_TIME = 12;
	public static final int CARPENTER_CRATING_CYCLES = 5;
	public static final int CARPENTER_UNCRATING_CYCLES = 5;
	public static final int CARPENTER_CRATING_LIQUID_QUANTITY = 100;
}

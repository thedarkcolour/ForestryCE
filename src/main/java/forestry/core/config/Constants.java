package forestry.core.config;

import net.minecraftforge.fluids.FluidType;

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

	// Energy
	public static final int ENGINE_TANK_CAPACITY = 10 * FluidType.BUCKET_VOLUME;
	public static final int ENGINE_CYCLE_DURATION_WATER = 1000;
	public static final int ENGINE_CYCLE_DURATION_JUICE = 2500;
	public static final int ENGINE_CYCLE_DURATION_HONEY = 2500;
	public static final int ENGINE_CYCLE_DURATION_MILK = 10000;
	public static final int ENGINE_CYCLE_DURATION_SEED_OIL = 2500;
	public static final int ENGINE_CYCLE_DURATION_BIOMASS = 2500;
	public static final int ENGINE_CYCLE_DURATION_ETHANOL = 15000;
	public static final int ENGINE_FUEL_VALUE_WATER = 10;
	public static final int ENGINE_FUEL_VALUE_JUICE = 10;
	public static final int ENGINE_FUEL_VALUE_HONEY = 20;
	public static final int ENGINE_FUEL_VALUE_MILK = 10;
	public static final int ENGINE_FUEL_VALUE_SEED_OIL = 30;
	public static final int ENGINE_FUEL_VALUE_BIOMASS = 50;
	public static final int ENGINE_HEAT_VALUE_LAVA = 20;

	public static final float ENGINE_PISTON_SPEED_MAX = 0.08f;

	public static final int ENGINE_COPPER_CYCLE_DURATION_PEAT = 2500;
	public static final int ENGINE_COPPER_FUEL_VALUE_PEAT = 20;
	public static final int ENGINE_COPPER_CYCLE_DURATION_BITUMINOUS_PEAT = 3000;
	public static final int ENGINE_COPPER_FUEL_VALUE_BITUMINOUS_PEAT = 40;
	public static final int ENGINE_COPPER_HEAT_MAX = 10000;
	public static final int ENGINE_COPPER_ASH_FOR_ITEM = 7500;

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

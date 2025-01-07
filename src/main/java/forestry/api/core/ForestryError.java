/*******************************************************************************
 * Copyright 2011-2014 SirSengir
 *
 * This work (the API) is licensed under the "MIT" License, see LICENSE.txt for details.
 ******************************************************************************/
package forestry.api.core;

import net.minecraft.resources.ResourceLocation;

import forestry.api.ForestryConstants;

/**
 * All errors used by base Forestry for its blocks and items.
 */
public enum ForestryError implements IError {
	INVALID("invalid", "invalid"), // This error is a bug. Report to the GitHub repository of the mod that adds this block.

	// Bees
	TOO_HOT("too_hot"), // The bees are melting in the heat here and unable to work. Use the habitat locator to find a cooler climate.
	TOO_COLD("too_cold"), // The bees are huddled together to survive the freezing cold here. Use the  habitat locator to find a warmer climate.
	TOO_HUMID("too_humid"), // The damp climate here has made the bees' wings too damp to fly. Use the  habitat locator to find a dryer climate.
	TOO_ARID("too_arid"), // The dry climate here has made the bees parched and unable do work. Use the  habitat locator to find a wetter climate.
	IS_RAINING("is_raining"), // Only tolerant fliers can work in the rain.
	NOT_GLOOMY("not_gloomy"), // The bees can only work in darkness.
	NOT_BRIGHT("not_lucid"), // The bees have trouble navigating in the dark.
	NOT_DAY("not_day"), // The bees can only work during the daytime.
	NOT_NIGHT("not_night"), // The bees can only work during the night.
	NOT_TWILIGHT("not_twilight"), // The bees can only work during sunset or sunrise.
	NO_FLOWER("no_flower"), // Hive members are not finding the right flowers. Use the Beealyzer to discover their flower preference.
	NO_QUEEN("no_queen"), // Supply this hive with a queen or a princess and a drone.
	NO_DRONE("no_drone"), // Mating requires a drone present.
	NO_SKY("no_sky"), // The hive requires direct sunlight from above.
	SLEEPY("sleepy"),

	// Machines
	NO_RESOURCE("no_resource"), // More resources need to be supplied for operation.
	NO_RESOURCE_INVENTORY("no_resource_inventory", "no_resource"), // Resources need to be added to the machine's inventory to craft this recipe.
	NO_RESOURCE_LIQUID("no_resource_liquid", "no_liquid"), // More liquid resources need to be supplied for operation.
	NO_RECIPE("no_recipe"), // No matching recipe was found for the supplied resources.
	NO_SPACE_INVENTORY("no_space"), // Empty this machine's inventory.
	NO_SPACE_TANK("no_space_tank"), // Empty this machine's liquid tank.
	NO_POWER("no_power"), // This machine requires RF energy from an engine to function.
	NO_REDSTONE("no_redstone", "disabled"), // This machine requires a redstone signal to activate it.
	DISABLED_BY_REDSTONE("disabled_redstone", "disabled"), // This machine is being disabled by a redstone signal.
	NOT_DARK("not_dark", "not_gloomy"), // A lower light level is required for operation.

	// Rain Tank
	NOT_RAINING("not_raining"), // Operation is only possible when it is raining.
	NO_RAIN_BIOME("no_rain_biome", "not_raining"), // This location never receives rain.
	NO_SKY_RAIN_TANK("no_sky_rain_tank", "no_sky"), // Clear the area above this machine so it can gather rain.

	// Analyzer
	NO_HONEY("no_honey"), // This gadget requires honey drops or honeydew for operation.
	NO_SPECIMEN("no_specimen"), // Supply specimen to analyze.

	// Engines
	FORCED_COOLDOWN("forced_cooldown"), // Engine has overheated and is forced into cooldown.
	NO_FUEL("no_fuel"), // (Biogas & Peat-fired) Replenish this machine's fuel supplies.
	NO_HEAT("no_heat"), // (Biogas engine) Refill the heating tank.
	NO_ENERGY_NET("no_energy_net"), // Your world is barren of any electricity. (Install IndustrialCraft\u00b2.)

	// Trade Station
	NO_STAMPS("no_stamps"), // The trade station requires more stamps to pay postage.
	NO_PAPER("no_paper"), // The trade station requires more paper to send letters.
	NO_SUPPLIES("no_supplies", "no_resource"), // The trade station requires more supplies to send.
	NO_TRADE("no_trade", "no_resource"), // The trade station requires items to Send and Receive.

	// Trade Station naming
	NOT_ALPHANUMERIC("not_alpha_numeric"), // A Trade Station name must consist of letters and numbers only.
	NOT_UNIQUE("not_unique"), // Trade Station names must be unique and this name is already taken.

	// Letters
	NOT_POST_PAID("not_postpaid", "no_stamps"), // Apply more stamps to pay the postal service.
	NO_RECIPIENT("no_recipient"), // You need to address your letter to a recipient to send it.

	// Circuit Boards
	NO_CIRCUIT_BOARD("no_circuit_board"), // Insert a circuit board to solder the selected tubes onto it.
	NO_CIRCUIT_LAYOUT("no_circuit_layout"), // No layouts available due to the current game settings.
	CIRCUIT_MISMATCH("circuit_mismatch"), // Amount of tubes does not match size of circuit board.

	// Farms
	NO_FERTILIZER("no_fertilizer"), // Farms require fertilizer for function. Compost is insufficient.
	NO_FARMLAND("no_farmland"), // Smooth sandstone, bricks or stone bricks create a platform the farm will build on.
	NO_LIQUID_FARM("no_liquid"), // Depending on rainfall, temperature and humidity farms need to be supplied with varying amounts of water.
	;

	private final ResourceLocation id;
	private final ResourceLocation sprite;
	private final String descriptionKey;
	private final String helpKey;

	ForestryError(String id) {
		this(id, id);
	}

	ForestryError(String id, String iconName) {
		this.id = ForestryConstants.forestry(id);
		this.sprite = ForestryConstants.forestry("errors/" + iconName);
		String idDotted = this.id.getNamespace() + '.' + this.id.getPath();
		this.descriptionKey = "errors." + idDotted + ".desc";
		this.helpKey = "errors." + idDotted + ".help";

	}

	@Override
	public String getDescriptionTranslationKey() {
		return this.descriptionKey;
	}

	@Override
	public String getHelpTranslationKey() {
		return this.helpKey;
	}

	@Override
	public ResourceLocation getSprite() {
		return this.sprite;
	}

	@Override
	public ResourceLocation getId() {
		return this.id;
	}
}

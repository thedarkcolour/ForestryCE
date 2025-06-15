package forestry.api.apiculture.hives;

import com.google.common.collect.ImmutableList;
import forestry.api.IForestryApi;
import forestry.api.apiculture.IBeeHousing;
import forestry.api.apiculture.IBeeListener;
import forestry.api.apiculture.IBeeModifier;
import forestry.api.apiculture.IBeekeepingLogic;
import forestry.apiculture.VillageHive;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.List;

/**
 * Contains data and factory methods related to bee hives. Obtain from {@link IForestryApi#getHiveManager()}.
 */
public interface IHiveManager {
	/**
	 * @return List of all registered hives.
	 */
	List<IHive> getHives();

	/**
	 * @return List of common hives used when picking hives for Forestry village houses.
	 */
	ImmutableList<VillageHive> getCommonVillageHives();

	/**
	 * @return List of rare hives used when picking hives for Forestry village houses.
	 */
	ImmutableList<VillageHive> getRareVillageHives();

	/**
	 * @return A list of potential drops for the hive with the specified ID.
	 */
	List<IHiveDrop> getDrops(ResourceLocation id);

	/**
	 * Used to determine if an item is usable as fuel in an Alveary Swarmer for queen rearing.
	 * In default Forestry, only Royal Jelly is usable and returns {@code 0.01f}, or 1%.
	 *
	 * @return The chance
	 */
	float getSwarmingMaterialChance(Item swarmItem);

	/**
	 * Creates beekeepingLogic for a housing.
	 * Should be used when the housing is created, see IBeekeepingLogic
	 */
	IBeekeepingLogic createBeekeepingLogic(IBeeHousing housing);

	/**
	 * Combines multiple modifiers from an IBeeHousing into one.
	 * Stays up to date with changes to the housing's modifiers.
	 */
	IBeeModifier createBeeHousingModifier(IBeeHousing housing);

	/**
	 * Combines multiple listeners from an IBeeHousing into one.
	 * Stays up to date with changes to the housing's listeners.
	 */
	IBeeListener createBeeHousingListener(IBeeHousing housing);
}

package forestry.api.farming;

import forestry.api.core.IErrorLogicSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.Collection;

/**
 * The IFarmHousing describes a forestry farm handler.
 * <p>
 * It contains methods to interact with the farm itself.
 * Can be used in {@link IFarmLogic}'s to remove fluid or add products.
 */
public interface IFarmHousing extends IErrorLogicSource, IExtentCache {

	/**
	 * Position of the farm. Mostly used by internal logic.
	 * <p>
	 * If the farm is a multiblock, the position is based in the center.
	 *
	 * @return Position of the farm.
	 */
	BlockPos getCoords();

	/**
	 * @return The area of the farmland of the farm.
	 */
	Vec3i getArea();

	/**
	 * @return The offset used by the farm to create a bounding box for the harvest logic.
	 */
	Vec3i getOffset();

	/**
	 * @return true if any work was done, false otherwise.
	 */
	boolean doWork();

	/**
	 * Checks if the given liquid and amount is contained in the internal tank.
	 *
	 * @param liquid The liquid to be checked
	 * @return True if the tank contains the liquid, false otherwise
	 */
	boolean hasLiquid(FluidStack liquid);

	/**
	 * Removes the given liquid from the internal tank if possible.
	 *
	 * @param liquid The liquid to be removed
	 */
	void removeLiquid(FluidStack liquid);

	/**
	 * Callback for {@link IFarmLogic}s to plant a sapling, seed, germling, stem.
	 * Will remove the appropriate germling from the farm's inventory.
	 * It's up to the logic to only call this on a valid location.
	 *
	 * @return true if planting was successful, false otherwise.
	 */
	boolean plantGermling(IFarmable farmable, Level world, BlockPos pos, Direction direction);

	default boolean isValidPlatform(Level world, BlockPos pos) {
		return false;
	}

	/**
	 * If the farmland area is square.
	 *
	 * @return True if the farmland area is a square, false otherwise
	 */
	default boolean isSquare() {
		return false;
	}

	/**
	 * Checks if the farm can plant soil.
	 *
	 * @param manual If true the manual mode of the farm is enabled
	 * @return True if the farm is able to place soil, false otherwise
	 */
	default boolean canPlantSoil(boolean manual) {
		return !manual;
	}

	/* INTERACTION WITH HATCHES */

	/**
	 * @return The inventory instance of this farm.
	 */
	IFarmInventory getFarmInventory();

	/**
	 * Adds a product to an internal buffer of items, which will later be added to the inventory.
	 *
	 * @param stack The stack to be added to the inventory.
	 */
	void addPendingProduct(ItemStack stack);

	/* LOGIC */

	/**
	 * Sets the farm logic of one direction of the farm.
	 *
	 * @param direction The direction of the farm to be set
	 * @param logic     The farm logic that direction should be set to
	 */
	void setFarmLogic(Direction direction, IFarmLogic logic);

	/**
	 * Resets the farm logic off the given direction to the default logic (ARBOREAL).
	 *
	 * @param direction The direction to reset
	 */
	void resetFarmLogic(Direction direction);

	/**
	 * Receives the logic of the given direction.
	 *
	 * @param direction The direction of the logic to get.
	 * @return The logic that is located on this side of the farm.
	 */
	IFarmLogic getFarmLogic(Direction direction);

	/**
	 * Receives a collection with all logics of this farm.
	 * By default this ether contains 1 or 4 logics.
	 *
	 * @return A collection which contains all logics of this farm.
	 */
	Collection<IFarmLogic> getFarmLogics();

	/* GUI */

	/**
	 * The percentage of fertilizer stored my the farm multiplied by the given scale.
	 *
	 * @param scale Value used to scale the fertilizer percentage
	 * @return The percentage of fertilizer stored my the farm multiplied by the given scale.
	 */
	int getStoredFertilizerScaled(int scale);

	/**
	 * Receives the corner position of the given direction.
	 * Mainly used for internal logic to position the crops and germlings.
	 *
	 * @param direction The direction to receive
	 * @return The position of the direction corner.
	 */
	BlockPos getFarmCorner(Direction direction);
}

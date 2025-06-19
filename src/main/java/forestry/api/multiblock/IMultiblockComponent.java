package forestry.api.multiblock;

import forestry.core.inventory.IInventoryAdapter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.component.ResolvableProfile;

import javax.annotation.Nullable;

/**
 * Basic interface for a multiblock machine component.
 * Implemented by TileEntities.
 */
public interface IMultiblockComponent {
	/**
	 * Returns the location of this tile entity in the world.
	 *
	 * @return ChunkCoordinates set to the location of this tile entity in the world.
	 */
	BlockPos getBlockPos();

	/**
	 * @return the gameProfile of the player who owns this single component (not the entire multiblock)
	 */
	@Nullable
	ResolvableProfile getOwner();

	/**
	 * @return the multiblock logic for this part
	 */
	IMultiblockLogic getMultiblockLogic();

	/**
	 * Called when a machine is fully assembled from the disassembled state, meaning
	 * it was constructed by a player/entity action, not by chunks loading.
	 * Note that, for non-square machines, the min/max coordinates may not actually be part
	 * of the machine! They form an outer bounding box for the whole machine itself.
	 *
	 * @param multiblockController The controller to which this part is being assembled.
	 */
	void onMachineAssembled(IMultiblockController multiblockController, BlockPos minCoord, BlockPos maxCoord);

	/**
	 * Called when the machine is broken for game reasons, e.g. a player removed a block
	 * or an explosion occurred.
	 */
	void onMachineBroken();

	/**
	 * A component with a separate inventory, like the Alveary Swarmer or Alveary Sieve.
	 */
	interface HasInventory {
		/**
		 * Called when this part is destroyed to drop its contents.
		 */
		IInventoryAdapter getInternalInventory();
	}
}

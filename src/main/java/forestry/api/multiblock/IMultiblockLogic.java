package forestry.api.multiblock;

import forestry.api.core.INbtReadable;
import forestry.api.core.INbtWritable;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;

/**
 * Multiblock Logic implements the basic logic for IMultiblockComponent tile entities.
 * Instances must come from MultiblockManager.logicFactory, most of the implementation is hidden.
 * <p>
 * IMultiblockComponent tile entities must wire up the methods in the "Updating and Synchronization" section.
 * As a starting point, you can use MultiblockTileEntityBase.
 */
public interface IMultiblockLogic extends INbtWritable, INbtReadable {
	/**
	 * @return {@code true} if this block is connected to a multiblock controller. False otherwise.
	 */
	boolean isConnected();

	/**
	 * @return the multiblock controller for this logic
	 */
	IMultiblockController getController();

	/* Updating and Synchronization */

	/**
	 * call on Tile.validate()
	 **/
	void validate(Level level, IMultiblockComponent part);

	/**
	 * call on Tile.setRemoved()
	 **/
	void setRemoved(Level level, IMultiblockComponent part);

	/**
	 * call on Tile.onChunkUnload()
	 **/
	void onChunkUnload(Level level, IMultiblockComponent part);

	/**
	 * Writes data for client synchronization.
	 * Use this in Tile.getDescriptionPacket()
	 */
	void encodeUpdatePacket(CompoundTag nbt, HolderLookup.Provider registries);

	/**
	 * Reads data for client synchronization.
	 * Use this in Tile.onDataPacket()
	 */
	void decodeUpdatePacket(CompoundTag nbt, HolderLookup.Provider registries);

	/**
	 * Read the logic's data from file.
	 * Use this in Tile.read()
	 */
	@Override
	void read(CompoundTag nbt, HolderLookup.Provider registries);

	/**
	 * Write the logic's data to file.
	 * Use this in Tile.write()
	 */
	@Override
	CompoundTag write(CompoundTag nbt, HolderLookup.Provider registries);
}

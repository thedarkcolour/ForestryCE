package forestry.core.multiblock;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.LevelChunk;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import forestry.Forestry;
import forestry.api.multiblock.IMultiblockComponent;
import forestry.core.tiles.TileUtil;

/**
 * This class contains the base logic for "multiblock controllers". Conceptually, they are
 * meta-TileEntities. They govern the logic for an associated group of TileEntities.
 * <p>
 * Subordinate TileEntities implement the IMultiblockComponent class and, generally, should not have an update() loop.
 */
public abstract class MultiblockControllerBase implements IMultiblockControllerInternal {
	// Multiblock stuff - do not mess with
	protected final Level level;

	// Ticks
	private static final Random rand = new Random();
	private int tickCount = rand.nextInt(256);
	@Nullable
	private BlockPos destroyedCoord = null;

	// Disassembled -> Assembled; Assembled -> Disassembled OR Paused; Paused -> Assembled
	protected enum AssemblyState {
		DISASSEMBLED, ASSEMBLED, PAUSED
	}

	protected AssemblyState assemblyState;

	protected HashSet<IMultiblockComponent> connectedParts;

	/**
	 * This is a deterministically-picked coordinate that identifies this
	 * multiblock uniquely in its dimension.
	 * Currently, this is the coord with the lowest X, Y and Z coordinates, in that order of evaluation.
	 * i.e. If something has a lower X but higher Y/Z coordinates, it will still be the reference.
	 * If something has the same X but a lower Y coordinate, it will be the reference. Etc.
	 */
	@Nullable
	private BlockPos referenceCoord;

	/**
	 * Minimum bounding box coordinate. Blocks do not necessarily exist at this coord if your machine
	 * is not a cube/rectangular prism.
	 */
	@Nullable
	private BlockPos minimumCoord;

	/**
	 * Maximum bounding box coordinate. Blocks do not necessarily exist at this coord if your machine
	 * is not a cube/rectangular prism.
	 */
	@Nullable
	private BlockPos maximumCoord;

	/**
	 * Set to true whenever a part is removed from this controller.
	 */
	private boolean shouldCheckForDisconnections;

	/**
	 * Set whenever we validate the multiblock
	 */
	@Nullable
	private MultiblockValidationException lastValidationException;

	protected MultiblockControllerBase(Level level) {
		this.level = level;
		this.connectedParts = new HashSet<>();

		this.referenceCoord = null;
		this.assemblyState = AssemblyState.DISASSEMBLED;

		this.minimumCoord = null;
		this.maximumCoord = null;

		this.shouldCheckForDisconnections = true;
		this.lastValidationException = null;
	}

	@Override

	public Collection<IMultiblockComponent> getComponents() {
		return Collections.unmodifiableCollection(connectedParts);
	}

	/**
	 * Call when a block with cached save-delegate data is added to the multiblock.
	 * The part will be notified that the data has been used after this call completes.
	 *
	 * @param part The NBT tag containing this controller's data.
	 */
	protected abstract void onAttachedPartWithMultiblockData(IMultiblockComponent part, CompoundTag data);

	@Override
	public void attachBlock(IMultiblockComponent part) {
		BlockPos coord = part.getCoordinates();

		if (!connectedParts.add(part)) {
			Forestry.LOGGER.warn("[{}] Controller {} is double-adding part {} @ {}. This is unusual. " +
							"If you encounter odd behavior, please tear down the machine and rebuild it.",
					level.isClientSide() ? "CLIENT" : "SERVER", hashCode(), part.hashCode(), coord);
		}

		MultiblockLogic logic = (MultiblockLogic) part.getMultiblockLogic();

		logic.setController(this);
		this.onBlockAdded(part);

		if (logic.hasMultiblockSaveData()) {
			CompoundTag savedData = logic.getMultiblockSaveData();
			onAttachedPartWithMultiblockData(part, savedData);
			logic.onMultiblockDataAssimilated();
		}

		if (this.referenceCoord == null) {
			referenceCoord = coord;
			logic.becomeMultiblockSaveDelegate();
		} else if (coord.compareTo(referenceCoord) < 0) {
			TileUtil.actOnTile(level, referenceCoord, IMultiblockComponent.class, tile -> {
				MultiblockLogic teLogic = (MultiblockLogic) tile.getMultiblockLogic();
				teLogic.forfeitMultiblockSaveDelegate();
			});

			referenceCoord = coord;
			logic.becomeMultiblockSaveDelegate();
		} else {
			logic.forfeitMultiblockSaveDelegate();
		}

		if (minimumCoord != null) {
			if (coord.getX() < minimumCoord.getX()) {
				minimumCoord = new BlockPos(coord.getX(), minimumCoord.getY(), minimumCoord.getZ());
			}
			if (coord.getY() < minimumCoord.getY()) {
				minimumCoord = new BlockPos(minimumCoord.getX(), coord.getY(), minimumCoord.getZ());
			}
			if (coord.getZ() < minimumCoord.getZ()) {
				minimumCoord = new BlockPos(minimumCoord.getX(), minimumCoord.getY(), coord.getZ());
			}
		}

		if (maximumCoord != null) {
			if (coord.getX() > maximumCoord.getX()) {
				maximumCoord = new BlockPos(coord.getX(), maximumCoord.getY(), maximumCoord.getZ());
			}
			if (coord.getY() > maximumCoord.getY()) {
				maximumCoord = new BlockPos(maximumCoord.getX(), coord.getY(), maximumCoord.getZ());
			}
			if (coord.getZ() > maximumCoord.getZ()) {
				maximumCoord = new BlockPos(maximumCoord.getX(), maximumCoord.getY(), coord.getZ());
			}
		}

		MultiblockRegistry.addDirtyController(level, this);
	}

	/**
	 * Called when a new part is added to the machine. Good time to register things into lists.
	 *
	 * @param newPart The part being added.
	 */
	protected abstract void onBlockAdded(IMultiblockComponent newPart);

	/**
	 * Called when a part is removed from the machine. Good time to clean up lists.
	 *
	 * @param oldPart The part being removed.
	 */
	protected abstract void onBlockRemoved(IMultiblockComponent oldPart);

	/**
	 * Called when a machine is assembled from a disassembled state.
	 */
	protected void onMachineAssembled() {

	}

	/**
	 * Called when a machine is restored to the assembled state from a paused state.
	 */
	protected void onMachineRestored() {

	}

	/**
	 * Called when a machine is paused from an assembled state
	 * This generally only happens due to chunk-loads and other "system" events.
	 */
	protected void onMachinePaused() {

	}

	/**
	 * Called when a machine is disassembled from an assembled state.
	 * This happens due to user or in-game actions (e.g. explosions)
	 */
	protected void onMachineDisassembled() {

	}

	/**
	 * Callback whenever a part is removed (or will very shortly be removed) from a controller.
	 * Do housekeeping/callbacks, also nulls min/max coords.
	 *
	 * @param part The part being removed.
	 */
	private void onDetachBlock(IMultiblockComponent part) {
		// Strip out this part
		MultiblockLogic logic = (MultiblockLogic) part.getMultiblockLogic();
		logic.setController(null);
		this.onBlockRemoved(part);
		logic.forfeitMultiblockSaveDelegate();

		minimumCoord = maximumCoord = null;

		if (referenceCoord != null && referenceCoord.equals(part.getCoordinates())) {
			referenceCoord = null;
		}

		shouldCheckForDisconnections = true;
	}

	@Override
	public void detachBlock(IMultiblockComponent part, boolean chunkUnloading) {
		if (chunkUnloading && this.assemblyState == AssemblyState.ASSEMBLED) {
			this.assemblyState = AssemblyState.PAUSED;
			this.onMachinePaused();
		}

		BlockPos oldReference = this.referenceCoord;
		// Strip out this part
		onDetachBlock(part);
		if (!connectedParts.remove(part)) {
			BlockPos partCoords = part.getCoordinates();
			Forestry.LOGGER.warn("[{}] Double-removing part ({}) @ {}, {}, {}, this is unexpected and may cause problems. " +
							"If you encounter anomalies, please tear down the reactor and rebuild it.",
					level.isClientSide() ? "CLIENT" : "SERVER", part.hashCode(), partCoords.getX(), partCoords.getY(), partCoords.getZ());
		}

		if (connectedParts.isEmpty()) {
			// Destroy/unregister
			MultiblockRegistry.addDeadController(this.level, this);
			// Save last known reference position so drops can be spawned
			this.destroyedCoord = oldReference;
			return;
		}

		MultiblockRegistry.addDirtyController(this.level, this);

		// Find new save delegate if we need to.
		if (referenceCoord == null) {
			selectNewReferenceCoord();
		}
	}

	@Override
	public String getLastValidationError() {
		if (lastValidationException == null) {
			return null;
		}
		return lastValidationException.getMessage();
	}

	@Override
	public void reassemble() {
		MultiblockRegistry.addDirtyController(level, this);
	}

	/**
	 * Checks if a machine is whole. If not, throws an exception with the reason why.
	 */
	protected abstract void isMachineWhole() throws MultiblockValidationException;

	@Override
	public void checkIfMachineIsWhole() {
		AssemblyState oldState = this.assemblyState;
		boolean isWhole;
		lastValidationException = null;
		try {
			isMachineWhole();
			isWhole = true;
		} catch (MultiblockValidationException e) {
			lastValidationException = e;
			isWhole = false;
		}

		if (isWhole) {
			// This will alter assembly state
			assembleMachine(oldState);
		} else if (oldState == AssemblyState.ASSEMBLED) {
			// This will alter assembly state
			disassembleMachine();
		}
		// Else Paused, do nothing
	}

	/**
	 * Called when a machine becomes "whole" and should begin
	 * functioning as a game-logically finished machine.
	 * Calls onMachineAssembled on all attached parts.
	 */
	private void assembleMachine(AssemblyState oldState) {
		this.assemblyState = AssemblyState.ASSEMBLED;

		for (IMultiblockComponent part : connectedParts) {
			part.onMachineAssembled(this, getMinimumCoord(), getMaximumCoord());
		}

		if (oldState == AssemblyState.PAUSED) {
			onMachineRestored();
		} else {
			onMachineAssembled();
		}
	}

	/**
	 * Called when the machine needs to be disassembled.
	 * It is not longer "whole" and should not be functional, usually
	 * as a result of a block being removed.
	 * Calls onMachineBroken on all attached parts.
	 */
	private void disassembleMachine() {
		this.assemblyState = AssemblyState.DISASSEMBLED;

		for (IMultiblockComponent part : connectedParts) {
			part.onMachineBroken();
		}

		onMachineDisassembled();
	}

	@Override
	public void assimilate(IMultiblockControllerInternal other) {
		BlockPos otherReferenceCoord = other.getReferenceCoord();
		BlockPos referenceCoord = getReferenceCoord();
		if (otherReferenceCoord != null && referenceCoord != null && referenceCoord.compareTo(otherReferenceCoord) >= 0) {
			throw new IllegalArgumentException("The controller with the lowest minimum-coord value must consume the one with the higher coords");
		}

		Set<IMultiblockComponent> partsToAcquire = new HashSet<>(other.getComponents());

		// releases all blocks and references gently so they can be incorporated into another multiblock
		other._onAssimilated(this);

		for (IMultiblockComponent acquiredPart : partsToAcquire) {
			// By definition, none of these can be the minimum block.
			if (isInvalid(acquiredPart)) {
				continue;
			}

			connectedParts.add(acquiredPart);
			MultiblockLogic logic = (MultiblockLogic) acquiredPart.getMultiblockLogic();
			logic.setController(this);
			this.onBlockAdded(acquiredPart);
		}

		this.onAssimilate(other);
		other.onAssimilated(this);
	}

	/**
	 * Called when this machine is consumed by another controller.
	 * Essentially, forcibly tear down this object.
	 *
	 * @param otherController The controller consuming this controller.
	 */
	@Override
	public void _onAssimilated(IMultiblockControllerInternal otherController) {
		if (referenceCoord != null) {
			if (level.getChunkSource().hasChunk(referenceCoord.getX() >> 4, referenceCoord.getZ() >> 4)) {
				TileUtil.actOnTile(level, referenceCoord, IMultiblockComponent.class, part -> {
					MultiblockLogic logic = (MultiblockLogic) part.getMultiblockLogic();
					logic.forfeitMultiblockSaveDelegate();
				});
			}
			this.referenceCoord = null;
		}

		connectedParts.clear();
	}

	/**
	 * Callback. Called after this controller assimilates all the blocks
	 * from another controller.
	 * Use this to absorb that controller's game data.
	 *
	 * @param assimilated The controller whose uniqueness was added to our own.
	 */
	protected abstract void onAssimilate(IMultiblockControllerInternal assimilated);

	@Override
	public final void updateMultiblockEntity() {
		tickCount++;

		if (connectedParts.isEmpty()) {
			// This shouldn't happen, but just in case...
			MultiblockRegistry.addDeadController(this.level, this);
			return;
		}

		if (this.assemblyState != AssemblyState.ASSEMBLED) {
			// Not assembled - don't run game logic
			return;
		}

		if (level.isClientSide) {
			clientTick(tickCount);
		} else if (serverTick(tickCount)) {
			// If this returns true, the server has changed its internal data.
			// If our chunks are loaded (they should be), we must mark our chunks as dirty.
			if (minimumCoord != null && maximumCoord != null &&
					level.hasChunksAt(minimumCoord, maximumCoord)) {
				int minChunkX = minimumCoord.getX() >> 4;
				int minChunkZ = minimumCoord.getZ() >> 4;
				int maxChunkX = maximumCoord.getX() >> 4;
				int maxChunkZ = maximumCoord.getZ() >> 4;

				for (int x = minChunkX; x <= maxChunkX; x++) {
					for (int z = minChunkZ; z <= maxChunkZ; z++) {
						// Ensure that we save our data, even if the our save delegate is in has no TEs.
						LevelChunk chunkToSave = this.level.getChunkSource().getChunkNow(x, z);
						// unloaded chunks do not need to be saved again, NBT delegate is in loaded chunks
						if (chunkToSave != null) {
							chunkToSave.setUnsaved(true);
						}
					}
				}
			}
		}
		// Else: Server, but no need to save data.
	}

	/**
	 * The server-side update loop! Use this similarly to a TileEntity's update loop.
	 * You do not need to call your superclass' update() if you're directly
	 * derived from MultiblockControllerBase. This is a callback.
	 * Note that this will only be called when the machine is assembled.
	 *
	 * @return True if the multiblock should save data, i.e. its internal game state has changed. False otherwise.
	 */
	protected abstract boolean serverTick(int tickCount);

	protected int getTickCount() {
		return tickCount;
	}

	/**
	 * Client-side update loop. Generally, this shouldn't do anything, but if you want
	 * to do some interpolation or something, do it here.
	 */
	@OnlyIn(Dist.CLIENT)
	protected abstract void clientTick(int tickCount);

	protected final boolean updateOnInterval(int tickInterval) {
		return tickCount % tickInterval == 0;
	}

	// Validation helpers

	/**
	 * @param level the level of the block on the multiblock, starting at 0 for the bottom.
	 * @param world World object for the world in which this controller is located.
	 * @param pos   coordinate of the block being tested
	 * @throws MultiblockValidationException if the tested block is not allowed on the machine's side faces
	 */
	protected void isBlockGoodForExteriorLevel(int level, Level world, BlockPos pos) throws MultiblockValidationException {
		Block block = world.getBlockState(pos).getBlock();
		throw new MultiblockValidationException(Component.translatable("for.multiblock.error.invalid.interior", block).getString(), pos);
	}

	/**
	 * The interior is any block that does not touch blocks outside the machine.
	 *
	 * @param world World object for the world in which this controller is located.
	 * @param pos   coordinate of the block being tested
	 * @throws MultiblockValidationException if the tested block is not allowed in the machine's interior
	 */
	protected void isBlockGoodForInterior(Level world, BlockPos pos) throws MultiblockValidationException {
		Block block = world.getBlockState(pos).getBlock();
		throw new MultiblockValidationException(Component.translatable("for.multiblock.error.invalid.interior", block).getString(), pos);
	}

	@Override
	@Nullable
	public BlockPos getReferenceCoord() {
		if (referenceCoord == null) {
			return selectNewReferenceCoord();
		}
		return referenceCoord;
	}

	/**
	 * @return The number of blocks connected to this controller.
	 */
	public int getNumConnectedBlocks() {
		return connectedParts.size();
	}

	@Override
	public void recalculateMinMaxCoords() {
		minimumCoord = new BlockPos(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
		maximumCoord = new BlockPos(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);

		for (IMultiblockComponent part : connectedParts) {
			BlockPos partCoords = part.getCoordinates();
			int minX = minimumCoord.getX();
			int minY = minimumCoord.getY();
			int minZ = minimumCoord.getZ();
			int maxX = maximumCoord.getX();
			int maxY = maximumCoord.getY();
			int maxZ = maximumCoord.getZ();
			if (partCoords.getX() < minimumCoord.getX()) {
				minX = partCoords.getX();
			}
			if (partCoords.getX() > maximumCoord.getX()) {
				maxX = partCoords.getX();
			}
			if (partCoords.getY() < minimumCoord.getY()) {
				minY = partCoords.getY();
			}
			if (partCoords.getY() > maximumCoord.getY()) {
				maxY = partCoords.getY();
			}
			if (partCoords.getZ() < minimumCoord.getZ()) {
				minZ = partCoords.getZ();
			}
			if (partCoords.getZ() > maximumCoord.getZ()) {
				maxZ = partCoords.getZ();
			}
			minimumCoord = new BlockPos(minX, minY, minZ);
			maximumCoord = new BlockPos(maxX, maxY, maxZ);
		}
	}

	/**
	 * @return The minimum bounding-box coordinate containing this machine's blocks.
	 */
	protected BlockPos getMinimumCoord() {
		if (minimumCoord == null) {
			recalculateMinMaxCoords();
		}
		return new BlockPos(minimumCoord);
	}

	/**
	 * @return The maximum bounding-box coordinate containing this machine's blocks.
	 */
	protected BlockPos getMaximumCoord() {
		if (maximumCoord == null) {
			recalculateMinMaxCoords();
		}
		return new BlockPos(maximumCoord);
	}

	protected final BlockPos getCenterCoord() {
		BlockPos minCoord = getMinimumCoord();
		BlockPos maxCoord = getMaximumCoord();

		return new BlockPos(
			(minCoord.getX() + maxCoord.getX()) / 2,
			(minCoord.getY() + maxCoord.getY()) / 2,
			(minCoord.getZ() + maxCoord.getZ()) / 2
		);
	}

	protected final BlockPos getTopCenterCoord() {
		BlockPos minCoord = getMinimumCoord();
		BlockPos maxCoord = getMaximumCoord();

		return new BlockPos(
			(minCoord.getX() + maxCoord.getX()) / 2,
			maxCoord.getY(),
			(minCoord.getZ() + maxCoord.getZ()) / 2
		);
	}

	protected final boolean isCoordInMultiblock(int x, int y, int z) {
		if (minimumCoord == null || maximumCoord == null) {
			return false;
		}
		return x >= minimumCoord.getX() && x <= maximumCoord.getX() && y >= minimumCoord.getY() && y <= maximumCoord.getY() && z >= minimumCoord.getZ() && z <= maximumCoord.getZ();
	}

	@Override
	public boolean hasNoParts() {
		return connectedParts.isEmpty();
	}

	@Override
	public boolean shouldConsume(IMultiblockControllerInternal otherController) {
		if (!otherController.getClass().equals(getClass())) {
			throw new IllegalArgumentException("Attempting to merge two multiblocks with different master classes - this should never happen!");
		}

		if (otherController == this) {
			return false;
		} // Don't be silly, don't eat yourself.

		int res = _shouldConsume(otherController);
		if (res < 0) {
			return true;
		} else if (res > 0) {
			return false;
		} else {
			// Strip dead parts from both and retry
			Forestry.LOGGER.warn("[{}] Encountered two controllers with the same reference coordinate. Auditing connected parts and retrying.", level.isClientSide ? "CLIENT" : "SERVER");
			auditParts();
			otherController.auditParts();

			res = _shouldConsume(otherController);
			if (res < 0) {
				return true;
			} else if (res > 0) {
				return false;
			} else {
				Forestry.LOGGER.error("My Controller ({}): size ({}), parts: {}", hashCode(), connectedParts.size(), getPartsListString());
				Forestry.LOGGER.error("Other Controller ({}): size ({}), coords: {}", otherController.hashCode(), otherController.getComponents().size(), otherController.getPartsListString());
				throw new IllegalArgumentException("[" + (level.isClientSide ? "CLIENT" : "SERVER") + "] " +
						"Two controllers with the same reference coord that somehow both have valid parts - this should never happen!");
			}

		}
	}

	private int _shouldConsume(IMultiblockControllerInternal otherController) {
		BlockPos myCoord = getReferenceCoord();
		BlockPos theirCoord = otherController.getReferenceCoord();

		// Always consume other controllers if their reference coordinate is null - this means they're empty and can be assimilated on the cheap
		if (theirCoord == null || myCoord == null) {
			return -1;
		} else {
			return myCoord.compareTo(theirCoord);
		}
	}

	@Override
	public String getPartsListString() {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (IMultiblockComponent part : connectedParts) {
			if (!first) {
				sb.append(", ");
			}
			BlockPos partCoord = part.getCoordinates();
			sb.append(String.format("(%d: %d, %d, %d)", part.hashCode(), partCoord.getX(), partCoord.getY(), partCoord.getZ()));
			first = false;
		}

		return sb.toString();
	}

	@Override
	public void auditParts() {
		HashSet<IMultiblockComponent> deadParts = new HashSet<>();
		for (IMultiblockComponent part : connectedParts) {
			BlockPos partCoord = part.getCoordinates();
			if (isInvalid(part) || TileUtil.getTile(level, partCoord) != part) {
				onDetachBlock(part);
				deadParts.add(part);
			}
		}

		connectedParts.removeAll(deadParts);
		Forestry.LOGGER.warn("[{}] Controller found {} dead parts during an audit, {} parts remain attached", level.isClientSide ? "CLIENT" : "SERVER", deadParts.size(), connectedParts.size());
	}

	@Override

	public Set<IMultiblockComponent> checkForDisconnections() {
		if (!this.shouldCheckForDisconnections) {
			return Collections.emptySet();
		}

		if (this.hasNoParts()) {
			MultiblockRegistry.addDeadController(level, this);
			return Collections.emptySet();
		}

		ChunkSource chunkProvider = level.getChunkSource();

		// Invalidate our reference coord, we'll recalculate it shortly
		referenceCoord = null;

		// Reset visitations and find the minimum coordinate
		Set<IMultiblockComponent> deadParts = new HashSet<>();
		BlockPos c;
		IMultiblockComponent referencePart = null;

		int originalSize = connectedParts.size();

		for (IMultiblockComponent part : connectedParts) {
			// This happens during chunk unload.
			BlockPos partCoord = part.getCoordinates();
			if (chunkProvider.getChunkNow(partCoord.getX() >> 4, partCoord.getZ() >> 4) == null || isInvalid(part)) {
				deadParts.add(part);
				onDetachBlock(part);
				continue;
			}

			if (TileUtil.getTile(level, partCoord) != part) {
				deadParts.add(part);
				onDetachBlock(part);
				continue;
			}

			MultiblockLogic logic = (MultiblockLogic) part.getMultiblockLogic();

			logic.setUnvisited();
			logic.forfeitMultiblockSaveDelegate();

			c = part.getCoordinates();
			if (referenceCoord == null) {
				referenceCoord = c;
				referencePart = part;
			} else if (c.compareTo(referenceCoord) < 0) {
				referenceCoord = c;
				referencePart = part;
			}
		}

		connectedParts.removeAll(deadParts);
		deadParts.clear();

		if (referencePart == null || hasNoParts()) {
			// There are no valid parts remaining. The entire multiblock was unloaded during a chunk unload. Halt.
			shouldCheckForDisconnections = false;
			MultiblockRegistry.addDeadController(level, this);
			return Collections.emptySet();
		} else {
			MultiblockLogic logic = (MultiblockLogic) referencePart.getMultiblockLogic();
			logic.becomeMultiblockSaveDelegate();
		}

		// Now visit all connected parts, breadth-first, starting from reference coord's part
		IMultiblockComponent part;
		LinkedList<IMultiblockComponent> partsToCheck = new LinkedList<>();

		partsToCheck.add(referencePart);

		while (!partsToCheck.isEmpty()) {
			part = partsToCheck.removeFirst();
			MultiblockLogic partLogic = (MultiblockLogic) part.getMultiblockLogic();
			partLogic.setVisited();

			List<IMultiblockComponent> nearbyParts = MultiblockUtil.getNeighboringParts(level, part); // Chunk-safe on server, but not on client
			for (IMultiblockComponent nearbyPart : nearbyParts) {
				// Ignore different machines
				MultiblockLogic nearbyPartLogic = (MultiblockLogic) nearbyPart.getMultiblockLogic();
				if (nearbyPartLogic.getController() != this) {
					continue;
				}

				if (!nearbyPartLogic.isVisited()) {
					nearbyPartLogic.setVisited();
					partsToCheck.add(nearbyPart);
				}
			}
		}

		// Finally, remove all parts that remain disconnected.
		Set<IMultiblockComponent> removedParts = new HashSet<>();
		for (IMultiblockComponent orphanCandidate : connectedParts) {
			MultiblockLogic logic = (MultiblockLogic) orphanCandidate.getMultiblockLogic();
			if (!logic.isVisited()) {
				deadParts.add(orphanCandidate);
				onDetachBlock(orphanCandidate);
				removedParts.add(orphanCandidate);
			}
		}

		// Trim any blocks that were invalid, or were removed.
		connectedParts.removeAll(deadParts);

		// Cleanup. Not necessary, really.
		deadParts.clear();

		// Juuuust in case.
		if (referenceCoord == null) {
			selectNewReferenceCoord();
		}

		// We've run the checks from here on out.
		shouldCheckForDisconnections = false;

		return removedParts;
	}

	@Override

	public Set<IMultiblockComponent> detachAllBlocks() {
		ChunkSource chunkProvider = level.getChunkSource();
		for (IMultiblockComponent part : connectedParts) {
			BlockPos partCoord = part.getCoordinates();
			if (chunkProvider.getChunkNow(partCoord.getX() >> 4, partCoord.getZ() >> 4) != null) {
				onDetachBlock(part);
			}
		}

		Set<IMultiblockComponent> detachedParts = connectedParts;
		connectedParts = new HashSet<>();
		return detachedParts;
	}

	/**
	 * @return True if this multiblock machine is considered assembled and ready to go.
	 */
	@Override
	public boolean isAssembled() {
		return this.assemblyState == AssemblyState.ASSEMBLED;
	}

	@Nullable
	private BlockPos selectNewReferenceCoord() {
		ChunkSource chunkProvider = level.getChunkSource();
		IMultiblockComponent theChosenOne = null;
		referenceCoord = null;

		for (IMultiblockComponent part : connectedParts) {
			BlockPos partCoord = part.getCoordinates();
			if (isInvalid(part) || chunkProvider.getChunkNow(partCoord.getX() >> 4, partCoord.getZ() >> 4) == null) {
				// Chunk is unloading, skip this coord to prevent chunk thrashing
				continue;
			}

			if (referenceCoord == null || referenceCoord.compareTo(partCoord) > 0) {
				referenceCoord = part.getCoordinates();
				theChosenOne = part;
			}
		}

		if (theChosenOne != null) {
			MultiblockLogic<?> logic = (MultiblockLogic<?>) theChosenOne.getMultiblockLogic();
			logic.becomeMultiblockSaveDelegate();
		}

		return referenceCoord;
	}

	private static boolean isInvalid(IMultiblockComponent part) {
		return part instanceof BlockEntity && ((BlockEntity) part).isRemoved();
	}

	@Nullable
	public BlockPos getDestroyedCoord() {
		return this.destroyedCoord;
	}
}

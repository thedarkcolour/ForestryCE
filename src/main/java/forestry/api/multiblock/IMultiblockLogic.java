package forestry.api.multiblock;

/**
 * Multiblock Logic implements the basic accessor for IMultiblockComponent tile entities.
 *
 * <p>After the engine rewrite (plan Phase 2) this is a thin accessor: it no longer drives validation,
 * chunk events, persistence, or networking (those moved to the base member BlockEntity and the
 * event-driven triggers). It only resolves the owning block's controller via the new
 * {@code MultiblockIndex} (returning the machine's {@code Fake} controller when unassembled).
 */
public interface IMultiblockLogic {

	/**
	 * @return True if this block is connected to an assembled multiblock controller. False otherwise.
	 */
	boolean isConnected();

	/**
	 * @return the multiblock controller for this logic, or the machine's {@code Fake} controller when this
	 * block is not part of an assembled structure.
	 */
	IMultiblockController getController();
}

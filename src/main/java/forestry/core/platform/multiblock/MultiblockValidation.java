package forestry.core.platform.multiblock;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import forestry.api.core.multiblock.IMultiblockComponent;
import forestry.core.platform.multiblock.pattern.MultiblockPattern;
import forestry.core.platform.multiblock.pattern.PatternResult;
import forestry.core.platform.multiblock.pattern.Predicates;
import forestry.core.platform.multiblock.pattern.StructurePos;
import forestry.core.platform.tile.TileUtil;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

/**
 * The event-driven validation trigger (plan Task 2.5, spec 5.3). Replaces the deleted flood-fill, registry
 * and tick-loop. On a block place, break, load or neighbor change, it runs the stateless pattern query
 * (bounded candidate origins fed to {@link MultiblockPattern#validate}) and applies the resulting assemble
 * or deactivate transition against the {@link MultiblockIndex} and the member BlockEntities.
 *
 * <p>It is intentionally cheap and never runs on a tick loop. It runs only on the server thread. Clients
 * mirror the holder's description packet.
 */
public final class MultiblockValidation {
	private MultiblockValidation() {
	}

	/**
	 * Re-validates any structure the position could be a member of, and applies the transition. Called from a
	 * member BE's {@code onLoad} and {@code setRemoved}, and a block's {@code neighborChanged}.
	 */
	public static void validateAt(Level level, BlockPos pos) {
		MultiblockTileEntityForestry<?> member = TileUtil.getTile(level, pos, MultiblockTileEntityForestry.class);
		if (member == null) {
			return;
		}
		validateFor(level, pos, member);
	}

	/**
	 * Runs validation for the given member BE. Tries each candidate origin, and on the first
	 * {@link PatternResult.Match} assembles or updates the machine. If none match, deactivates any machine
	 * this block currently anchors or belongs to.
	 */
	public static void validateFor(Level level, BlockPos pos, MultiblockTileEntityForestry<?> member) {
		// the client receives chunks in a different order than the server loads them, so it mirrors the holder's
		// description packet instead of reaching its own verdict
		if (level.isClientSide) {
			return;
		}
		MultiblockPattern pattern = member.getPattern();
		LevelStructureView view = new LevelStructureView(level);
		StructurePos origin = new StructurePos(pos.getX(), pos.getY(), pos.getZ());

		@Nullable PatternResult.Match match = null;
		@Nullable PatternResult.Failure bestFailure = null;
		int bestRank = Integer.MIN_VALUE;
		LongSet unloadedChunks = new LongOpenHashSet();
		for (StructurePos candidate : pattern.candidateOrigins(origin)) {
			PatternResult result = pattern.validate(view, candidate);
			if (result instanceof PatternResult.Match m) {
				// Confirm this position is actually a member of the matched structure. Candidate origins are
				// generated permissively over all sizes and offsets, so a match might not contain pos
				if (containsPos(m, pos)) {
					match = m;
					break;
				}
			} else {
				// Track the most player-meaningful failure, not just the first, so the stored deactivation
				// message matches what the right-click hint would show. Never the internal NOT_MAXIMAL deferral.
				PatternResult.Failure failure = (PatternResult.Failure) result;
				if (failure.firstKey().equals(Predicates.KEY_NOT_LOADED)) {
					StructurePos cell = failure.first().pos();
					unloadedChunks.add(ChunkPos.asLong(cell.x() >> 4, cell.z() >> 4));
				}
				int rank = hintRank(failure.firstKey());
				if (rank > bestRank) {
					bestRank = rank;
					bestFailure = failure;
				}
			}
		}

		if (match != null) {
			DeferredValidation.forget(level, pos);
			assemble(level, member, match);
		} else if (!unloadedChunks.isEmpty()) {
			// any candidate cut off by an unloaded chunk might be the real structure, so no verdict or message yet
			DeferredValidation.defer(level, pos, unloadedChunks);
			deactivate(level, member, null);
		} else {
			DeferredValidation.forget(level, pos);
			deactivate(level, member, bestFailure);
		}
	}

	/**
	 * Computes the on-demand validation hint for an unassembled block (spec 11, plan Task 8.1). Runs the
	 * pattern validator over every candidate origin for the position and returns the most-informative
	 * failure's translation key, or {@code null} if the position actually forms a structure, in which case
	 * the caller should defer to the normal assembled path. Used by {@code BlockStructure.use} for the empty-hand right-click hint on a
	 * never-formed block, where no controller, and so no stored {@code lastValidationError}, exists yet.
	 *
	 * <p>Candidate origins are generated permissively over all sizes and offsets, so most candidates fail with
	 * a generic loaded-shell deferral, {@code invalid.interior} or {@code invalid.part}. This picks the failure
	 * whose key is the most player-meaningful, preferring a content or size error such as {@code needSlabs},
	 * {@code needGearbox} or {@code error.small} over a generic deferral. That mirrors the old engine's
	 * {@code isMachineWhole} message.
	 */
	@Nullable
	public static net.minecraft.network.chat.Component findValidationHint(Level level, BlockPos pos, MultiblockTileEntityForestry<?> member) {
		MultiblockPattern pattern = member.getPattern();
		LevelStructureView view = new LevelStructureView(level);
		StructurePos origin = new StructurePos(pos.getX(), pos.getY(), pos.getZ());

		@Nullable PatternResult.Failure best = null;
		int bestRank = Integer.MIN_VALUE;
		for (StructurePos candidate : pattern.candidateOrigins(origin)) {
			PatternResult result = pattern.validate(view, candidate);
			if (result instanceof PatternResult.Match m) {
				if (containsPos(m, pos)) {
					// pos actually forms a structure, no hint needed and the caller falls through to the
					// assembled path
					return null;
				}
			} else {
				PatternResult.Failure failure = (PatternResult.Failure) result;
				int rank = hintRank(failure.firstKey());
				if (rank > bestRank) {
					bestRank = rank;
					best = failure;
				}
			}
		}
		// If only internal "wrong candidate origin" deferrals were seen, with no real content or size error
		// from any candidate, there is no actionable hint. Suppress the message rather than show the
		// internal key.
		if (best == null || isInternal(best.firstKey())) {
			return null;
		}
		return buildMessage(level, best.first());
	}

	/**
	 * Builds the player-facing chat component for a failing cell, filling the lang key's format args (spec
	 * Task A.3) so no literal {@code %s} or {@code %d} leaks. Size keys carry their integer args on the cell.
	 * The content keys {@code invalid.interior} and {@code invalid.part} take the offending block's display
	 * name, resolved here from the world because the pattern layer is {@code net.minecraft}-free and cannot.
	 */
	public static net.minecraft.network.chat.Component buildMessage(Level level, PatternResult.FailingCell cell) {
		String key = cell.key();
		if (key.equals(forestry.core.platform.multiblock.pattern.Predicates.KEY_INVALID_INTERIOR)
				|| key.equals(forestry.core.platform.multiblock.pattern.Predicates.KEY_INVALID_PART)) {
			BlockPos cellPos = new BlockPos(cell.pos().x(), cell.pos().y(), cell.pos().z());
			net.minecraft.network.chat.Component blockName = level.getBlockState(cellPos).getBlock().getName();
			return net.minecraft.network.chat.Component.translatable(key, blockName);
		}
		int[] args = cell.args();
		if (args.length == 0) {
			return net.minecraft.network.chat.Component.translatable(key);
		}
		Object[] boxed = new Object[args.length];
		for (int i = 0; i < args.length; i++) {
			boxed[i] = args[i];
		}
		return net.minecraft.network.chat.Component.translatable(key, boxed);
	}

	/** Ranks a failure key by how player-meaningful it is. Higher is preferred for the chat hint (spec 11). */
	private static int hintRank(String key) {
		// The internal "wrong candidate origin" deferral, where a same-type block sits below or around a
		// non-min-corner candidate (spec 5.3), tells the player nothing. It is a discovery artefact of probing
		// many permissive origins, not an error. Rank it LOWEST so the meaningful failure from the candidate
		// rooted at the true min corner always wins. This is the Task A fix, since it used to leak as the
		// misleading invalid.part "%s". The not-loaded deferral is ranked the same, since it is no verdict at all.
		if (isInternal(key)) {
			return -1;
		}
		// A generic "this cell is or isn't a component" message is the next least useful, but still better
		// than the internal deferral. It at least points at a real bad cell when the structure is the
		// right size.
		if (key.equals(forestry.core.platform.multiblock.pattern.Predicates.KEY_INVALID_INTERIOR)) {
			return 0;
		}
		if (key.equals(forestry.core.platform.multiblock.pattern.Predicates.KEY_INVALID_PART)) {
			return 1;
		}
		// Everything else is a real content or size error: needSlabs, needSpace, needGearbox, needPlain*,
		// small and large
		return 2;
	}

	// never shown to the player, and a not-loaded cell's chunk must not be read to name its block
	private static boolean isInternal(String key) {
		return key.equals(Predicates.KEY_NOT_MAXIMAL) || key.equals(Predicates.KEY_NOT_LOADED);
	}

	private static boolean containsPos(PatternResult.Match match, BlockPos pos) {
		StructurePos sp = new StructurePos(pos.getX(), pos.getY(), pos.getZ());
		return match.members().contains(sp);
	}

	/**
	 * Assembles, or re-establishes, the machine described by the match. Canonicalizes the holder to the
	 * lowest member (spec 6.1), installs the structure, registers the controller, wires every member's
	 * {@code anchorPos}, and (re)fires the per-part assembled callbacks.
	 */
	private static void assemble(Level level, MultiblockTileEntityForestry<?> member, PatternResult.Match match) {
		List<BlockPos> members = toBlockPos(match.members());
		BlockPos holderPos = toBlockPos(match.holder()); // the canonical lowest member

		// Resolve the holder BE, which hosts the controller
		MultiblockTileEntityForestry<?> holder = TileUtil.getTile(level, holderPos, MultiblockTileEntityForestry.class);
		if (holder == null) {
			// Holder cell not yet loaded as a BE, defer until a later onLoad on the holder assembles
			return;
		}

		// Find an existing controller anywhere among the members. It may currently be hosted on a
		// non-canonical holder after a partial reload, so we can canonicalize without dropping state.
		MultiblockController controller = resolveExistingController(level, members);
		boolean firstFormation = controller == null;
		if (controller == null) {
			controller = holder.createController(level);
			// BUG 1 and the section 10 tie-break. Seed from the lowest-(x,y,z) member that carries a
			// NON-EMPTY stash, NOT just the holder's stash. The real payload may live on a non-lowest member.
			// After a holder (anchor) break, handleHolderBreak hands the live payload to the lowest *loaded
			// survivor*, a non-lowest member, as its stash. When the broken corner is re-added it becomes the
			// new lowest member and holder but its own stash is EMPTY, so reading only the holder's stash
			// would seed the controller empty and lose the inventory. Scanning all members for the lowest
			// non-empty carrier recovers the payload regardless of which member holds it, and also implements
			// the deferred section 10 multi-carrier migration tie-break, because a C1 or C3 world may leave a
			// legacy tag on 2 or more members. The lowest non-empty wins and the rest are discarded (cleared
			// below). Never overwrite populated state with an empty tag.
			seedFromLowestStash(level, members, controller);
		} else {
			// Canonicalize the payload holder to the lowest member. The spec 6.1 single-holder invariant is
			// that exactly one loaded member serializes the payload. When the live holder is not the lowest
			// member, move hosting to the lowest member and FULLY demote the old holder: deregister its index
			// entry, clear its stash, and re-point its anchor to the new holder. Clearing the old holder's
			// stash is load-bearing. Otherwise its saveAdditional non-holder branch would re-emit PAYLOAD_KEY
			// from the stale stash and a second member would serialize the payload, RE-INTRODUCING corruption.
			BlockPos oldHolder = controller.getHolderPos();
			if (oldHolder != null && !oldHolder.equals(holderPos)) {
				MultiblockIndex.deregister(level, oldHolder);
				MultiblockTileEntityForestry<?> oldHolderBe = TileUtil.getTile(level, oldHolder, MultiblockTileEntityForestry.class);
				if (oldHolderBe != null) {
					oldHolderBe.clearStash();
					oldHolderBe.setAnchorPos(holderPos);
				}
				MultiblockController.markChunkDirty(level, oldHolder);
				MultiblockController.syncHolder(level, oldHolder);
			}
		}

		// MINOR 4. Only do the heavy re-bucket and per-part onMachineAssembled re-fire on a genuine
		// transition, meaning first formation or a move from deactivated to assembled including reload, or
		// when the member set actually changed. A redundant re-validation on a stable assembled machine, on
		// every neighborChanged or onLoad, must not re-bucket, because that re-randomizes FarmController's
		// per-Active tick offsets and triggers NxN blockstate refreshes. It must not re-fire the assembled
		// visuals either.
		boolean wasAssembled = controller.isAssembled();
		boolean sameMembers = wasAssembled && members.equals(controller.getMembers());
		boolean holderUnchanged = holderPos.equals(controller.getHolderPos());

		if (wasAssembled && sameMembers && holderUnchanged) {
			// Stable, already-assembled machine with no structural change. Keep the index and error state
			// fresh and make sure the triggering member is anchored, since it may have just reloaded, but
			// skip the expensive re-bucket and the per-part onMachineAssembled re-fire (MINOR 4).
			controller.setLastValidationError(null);
			member.setAnchorPos(holderPos);
			MultiblockIndex.register(level, holderPos, controller);
			return;
		}

		controller.setStructure(members, match.min() == null ? holderPos : toBlockPos(match.min()), toBlockPos(match.max()), holderPos);
		controller.setHolderPos(holderPos);
		controller.setAssembled(true);
		controller.setLastValidationError(null);
		MultiblockIndex.register(level, holderPos, controller);

		// Wire every member's anchorPos to the holder so getController and the ticker resolve correctly
		for (BlockPos mpos : members) {
			MultiblockTileEntityForestry<?> mbe = TileUtil.getTile(level, mpos, MultiblockTileEntityForestry.class);
			if (mbe != null) {
				mbe.setAnchorPos(holderPos);
			}
		}

		// Single-holder invariant (spec 6.1). Clear the stash on EVERY member so exactly one loaded member, the
		// holder, writes PAYLOAD_KEY. The live controller now owns the canonical payload, seeded from the
		// lowest non-empty stash above. Any residual stash on a member must be dropped, whether it is the
		// holder's own, a hand-off survivor's (BUG 1), or a discarded section 10 multi-carrier tag. Otherwise
		// its saveAdditional non-holder branch would re-emit a stale PAYLOAD_KEY and a second member would
		// serialize the payload, RE-INTRODUCING corruption. Every member is loaded at assembly, so this is
		// always safe.
		for (BlockPos mpos : members) {
			MultiblockTileEntityForestry<?> mbe = TileUtil.getTile(level, mpos, MultiblockTileEntityForestry.class);
			if (mbe != null) {
				mbe.clearStash();
			}
		}

		// Owner vote-once (spec 3.1 E1). Only the very first formation votes, reloads keep the payload owner
		if (firstFormation && !controller.isOwnerResolved()) {
			controller.voteOwnerOnceIfNeeded();
		}

		controller.onAssembled();

		// Re-fire per-part assembled visuals on every transition into assembled (spec 7.3), reloads included
		BlockPos min = controller.getMinimumCoord();
		BlockPos max = controller.getMaximumCoord();
		for (IMultiblockComponent part : controller.getComponents()) {
			part.onMachineAssembled(controller, min, max);
		}

		MultiblockController.syncHolder(level, holderPos);
	}

	/**
	 * Deactivates the machine this block currently anchors or belongs to, after a structural change made it
	 * invalid. Records the failure key for the on-demand chat message (spec 11). The genuine break and
	 * re-anchor hand-off itself is handled in {@code MultiblockTileEntityForestry.setRemoved} (spec 6.4).
	 * This method only flips the assembled flag and fires the per-part broken callbacks.
	 */
	private static void deactivate(Level level, MultiblockTileEntityForestry<?> member, @Nullable PatternResult.Failure failure) {
		BlockPos anchorPos = member.getAnchorPos();
		MultiblockController controller = anchorPos == null ? null : MultiblockIndex.get(level, anchorPos);
		if (controller == null) {
			return;
		}
		if (controller.isAssembled()) {
			List<IMultiblockComponent> parts = controller.getComponents();
			controller.setAssembled(false);
			controller.onBroken();
			for (IMultiblockComponent part : parts) {
				part.onMachineBroken();
			}
		}
		if (failure != null && !isInternal(failure.firstKey())) {
			// Build the localized message with its format args filled (spec Task A.3). Skip the internal
			// "wrong candidate origin" deferral, which is never shown to the player.
			controller.setLastValidationError(buildMessage(level, failure.first()).getString());
		}
		// Deregister the now-unformed controller from the index so deactivated controllers don't accumulate
		// (MAJOR 1, a per-level leak). Before dropping the index entry, hand the live controller's payload
		// back to the holder BE as its stash so (a) a save before re-validation still persists it,
		// holder-gated through the saveAdditional stash branch, and (b) a later re-validation re-adopts it
		// through applyStashTo. The genuine break and re-anchor hand-off in MultiblockTileEntityForestry
		// deregisters separately.
		MultiblockTileEntityForestry<?> holder = TileUtil.getTile(level, anchorPos, MultiblockTileEntityForestry.class);
		if (holder != null) {
			holder.stashFrom(controller);
		}
		MultiblockIndex.deregister(level, anchorPos);
		MultiblockController.syncHolder(level, anchorPos);
	}

	/**
	 * Seeds a freshly-created controller from the lowest-(x,y,z) member that carries a NON-EMPTY stash
	 * (spec 6.4 re-anchor recovery, spec 10 migration tie-break). The members list is produced lowest-first by
	 * the pattern engine, but positions are compared explicitly so the result is independent of list order.
	 * The lowest non-empty carrier wins, and members with no stash, such as the re-added corner, or an empty
	 * one are skipped, so the recovered payload is never overwritten with an empty tag. If no member carries a
	 * stash, meaning a genuinely brand-new structure, the controller is left at its constructed empty defaults.
	 */
	private static void seedFromLowestStash(Level level, List<BlockPos> members, MultiblockController controller) {
		MultiblockTileEntityForestry<?> bestCarrier = null;
		BlockPos bestPos = null;
		for (BlockPos pos : members) {
			MultiblockTileEntityForestry<?> mbe = TileUtil.getTile(level, pos, MultiblockTileEntityForestry.class);
			if (mbe != null && mbe.hasStash() && (bestPos == null || pos.compareTo(bestPos) < 0)) {
				bestCarrier = mbe;
				bestPos = pos;
			}
		}
		if (bestCarrier != null) {
			bestCarrier.applyStashTo(controller);
		}
	}

	/** Finds the controller currently hosted by any loaded member, in steady state or after a partial reload. */
	@Nullable
	private static MultiblockController resolveExistingController(Level level, List<BlockPos> members) {
		for (BlockPos pos : members) {
			MultiblockController c = MultiblockIndex.get(level, pos);
			if (c != null) {
				return c;
			}
		}
		// Also consult each member BE's stored anchorPos in case the index entry is keyed elsewhere
		for (BlockPos pos : members) {
			MultiblockTileEntityForestry<?> mbe = TileUtil.getTile(level, pos, MultiblockTileEntityForestry.class);
			if (mbe != null) {
				BlockPos anchor = mbe.getAnchorPos();
				if (anchor != null) {
					MultiblockController c = MultiblockIndex.get(level, anchor);
					if (c != null) {
						return c;
					}
				}
			}
		}
		return null;
	}

	private static List<BlockPos> toBlockPos(List<StructurePos> positions) {
		List<BlockPos> result = new ArrayList<>(positions.size());
		for (StructurePos sp : positions) {
			result.add(toBlockPos(sp));
		}
		return result;
	}

	private static BlockPos toBlockPos(@Nullable StructurePos sp) {
		return sp == null ? BlockPos.ZERO : new BlockPos(sp.x(), sp.y(), sp.z());
	}

	/** Re-validates every neighbor of the position after a break (spec 5.3). */
	public static void validateNeighbors(Level level, BlockPos pos) {
		for (net.minecraft.core.Direction dir : net.minecraft.core.Direction.values()) {
			validateAt(level, pos.relative(dir));
		}
	}
}

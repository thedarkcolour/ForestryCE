package forestry.core.multiblock;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import forestry.api.multiblock.IMultiblockComponent;
import forestry.core.multiblock.pattern.MultiblockPattern;
import forestry.core.multiblock.pattern.PatternResult;
import forestry.core.multiblock.pattern.StructurePos;
import forestry.core.tiles.TileUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/**
 * The event-driven validation trigger (plan Task 2.5; spec §5.3). Replaces the deleted flood-fill /
 * registry / tick-loop: on a block place/break/load/neighbor-change, it runs the stateless pattern query
 * (bounded candidate origins → {@link MultiblockPattern#validate}) and applies the resulting
 * assemble/deactivate transition against the {@link MultiblockIndex} and the member BlockEntities.
 *
 * <p>It is intentionally cheap and never runs on a tick loop. All access is on the main thread (server or
 * client), matching the engine's threading constraint.
 */
public final class MultiblockValidation {
	private MultiblockValidation() {
	}

	/**
	 * Re-validate any structure that {@code pos} could be a member of, and apply the transition. Called
	 * from a member BE's {@code onLoad}/{@code setRemoved}, a block's {@code neighborChanged}, and the
	 * client {@code PacketAlvearyChange} handler.
	 */
	public static void validateAt(Level level, BlockPos pos) {
		MultiblockTileEntityForestry<?> member = TileUtil.getTile(level, pos, MultiblockTileEntityForestry.class);
		if (member == null) {
			return;
		}
		validateFor(level, pos, member);
	}

	/**
	 * Runs validation for the given member BE at {@code pos}: tries each candidate origin, and on the first
	 * {@link PatternResult.Match} assembles/updates the machine; if none match, deactivates any machine this
	 * block currently anchors or belongs to.
	 */
	public static void validateFor(Level level, BlockPos pos, MultiblockTileEntityForestry<?> member) {
		MultiblockPattern pattern = member.getPattern();
		LevelStructureView view = new LevelStructureView(level);
		StructurePos origin = new StructurePos(pos.getX(), pos.getY(), pos.getZ());

		@Nullable PatternResult.Match match = null;
		@Nullable PatternResult.Failure bestFailure = null;
		int bestRank = Integer.MIN_VALUE;
		for (StructurePos candidate : pattern.candidateOrigins(origin)) {
			PatternResult result = pattern.validate(view, candidate);
			if (result instanceof PatternResult.Match m) {
				// Confirm this position is actually a member of the matched structure (candidate origins are
				// generated permissively over all sizes/offsets, so a match might not contain pos).
				if (containsPos(m, pos)) {
					match = m;
					break;
				}
			} else {
				// Track the most player-meaningful failure (not just the first) so the stored deactivation
				// message matches what the right-click hint would show — never the internal NOT_MAXIMAL deferral.
				PatternResult.Failure failure = (PatternResult.Failure) result;
				int rank = hintRank(failure.firstKey());
				if (rank > bestRank) {
					bestRank = rank;
					bestFailure = failure;
				}
			}
		}

		if (match != null) {
			assemble(level, member, match);
		} else {
			deactivate(level, member, bestFailure);
		}
	}

	/**
	 * Computes the on-demand validation hint for an unassembled block (spec §11; plan Task 8.1): runs the
	 * pattern validator over every candidate origin for {@code pos} and returns the most-informative failure's
	 * translation key, or {@code null} if {@code pos} actually forms a structure (caller should defer to the
	 * normal assembled path). Used by {@code BlockStructure.use} for the empty-hand right-click hint on a
	 * never-formed block, where no controller (and thus no stored {@code lastValidationError}) exists yet.
	 *
	 * <p>Candidate origins are generated permissively over all sizes/offsets, so most candidates fail with a
	 * generic loaded-shell deferral ({@code invalid.interior}/{@code invalid.part}). We pick the failure whose
	 * key is the most player-meaningful (a content/size error like {@code needSlabs}/{@code needGearbox}/
	 * {@code error.small} over a generic deferral), mirroring the old engine's {@code isMachineWhole} message.
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
					// pos actually forms a structure; no hint needed (caller falls through to the assembled path).
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
		// If only internal "wrong candidate origin" deferrals were seen (no real content/size error from any
		// candidate), there is no actionable hint — suppress the message rather than show the internal key.
		if (best == null || best.firstKey().equals(forestry.core.multiblock.pattern.Predicates.KEY_NOT_MAXIMAL)) {
			return null;
		}
		return buildMessage(level, best.first());
	}

	/**
	 * Builds the player-facing chat component for a failing cell, filling the lang key's format args (spec
	 * Task A.3) so no literal {@code %s}/{@code %d} leaks. Size keys carry their integer args on the cell;
	 * the content keys {@code invalid.interior}/{@code invalid.part} take the offending block's display name,
	 * resolved here from the world (the pattern layer is {@code net.minecraft}-free and cannot).
	 */
	public static net.minecraft.network.chat.Component buildMessage(Level level, PatternResult.FailingCell cell) {
		String key = cell.key();
		if (key.equals(forestry.core.multiblock.pattern.Predicates.KEY_INVALID_INTERIOR)
				|| key.equals(forestry.core.multiblock.pattern.Predicates.KEY_INVALID_PART)) {
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

	/** Ranks a failure key by how player-meaningful it is (higher = preferred for the chat hint, spec §11). */
	private static int hintRank(String key) {
		// The internal "wrong candidate origin" deferral (a same-type block sits below/around a non-min-corner
		// candidate, spec §5.3) tells the player nothing — it is a discovery artefact of probing many permissive
		// origins, not an error. Rank it LOWEST so the meaningful failure from the candidate rooted at the true
		// min corner always wins (this is the Task A fix: it used to leak as the misleading invalid.part "%s").
		if (key.equals(forestry.core.multiblock.pattern.Predicates.KEY_NOT_MAXIMAL)) {
			return -1;
		}
		// A generic "this cell is/isn't a component" message is the next least useful, but still better than the
		// internal deferral — it at least points at a real bad cell when the structure is the right size.
		if (key.equals(forestry.core.multiblock.pattern.Predicates.KEY_INVALID_INTERIOR)) {
			return 0;
		}
		if (key.equals(forestry.core.multiblock.pattern.Predicates.KEY_INVALID_PART)) {
			return 1;
		}
		// Everything else is a real content/size error (needSlabs, needSpace, needGearbox, needPlain*, small/large).
		return 2;
	}

	private static boolean containsPos(PatternResult.Match match, BlockPos pos) {
		StructurePos sp = new StructurePos(pos.getX(), pos.getY(), pos.getZ());
		return match.members().contains(sp);
	}

	/**
	 * Assembles (or re-establishes) the machine described by {@code match}. Canonicalizes the holder to the
	 * lowest member (spec §6.1), installs the structure, registers the controller, wires every member's
	 * {@code anchorPos}, and (re)fires the per-part assembled callbacks.
	 */
	private static void assemble(Level level, MultiblockTileEntityForestry<?> member, PatternResult.Match match) {
		List<BlockPos> members = toBlockPos(match.members());
		BlockPos holderPos = toBlockPos(match.holder()); // = lowest member (canonical)

		// Resolve the holder BE — it hosts the controller.
		MultiblockTileEntityForestry<?> holder = TileUtil.getTile(level, holderPos, MultiblockTileEntityForestry.class);
		if (holder == null) {
			// Holder cell not yet loaded as a BE; defer (a later onLoad on the holder will assemble).
			return;
		}

		// Find an existing controller anywhere among the members (it may currently be hosted on a
		// non-canonical holder after a partial reload), so we can canonicalize without dropping state.
		MultiblockController controller = resolveExistingController(level, members);
		boolean firstFormation = controller == null;
		if (controller == null) {
			controller = holder.createController(level);
			// BUG 1 / §10 tie-break: seed from the lowest-(x,y,z) member that carries a NON-EMPTY stash — NOT
			// just the holder's stash. The real payload may live on a non-lowest member: after a holder
			// (anchor) break, handleHolderBreak hands the live payload to the lowest *loaded survivor* (a
			// non-lowest member) as its stash; when the broken corner is re-added it becomes the new lowest
			// member/holder but its own stash is EMPTY, so reading only the holder's stash would seed the
			// controller empty and lose the inventory. Scanning all members for the lowest non-empty carrier
			// recovers the payload regardless of which member holds it, and also implements the deferred §10
			// multi-carrier migration tie-break (a C1/C3 world may leave a legacy tag on ≥2 members): the
			// lowest non-empty wins and the rest are discarded (cleared below) — never overwrite populated
			// state with an empty tag.
			seedFromLowestStash(level, members, controller);
		} else {
			// Canonicalize the payload holder to the lowest member (spec §6.1 single-holder invariant:
			// exactly one loaded member serializes the payload). When the live holder is not the lowest
			// member, move hosting to the lowest member and FULLY demote the old holder: deregister its index
			// entry, clear its stash, and re-point its anchor to the new holder. Clearing the old holder's
			// stash is load-bearing — otherwise its saveAdditional non-holder branch would re-emit PAYLOAD_KEY
			// from the stale stash and a second member would serialize the payload (RE-INTRODUCES corruption).
			BlockPos oldHolder = controller.getHolderPos();
			if (oldHolder != null && !oldHolder.equals(holderPos)) {
				MultiblockIndex.deregister(level, oldHolder);
				MultiblockTileEntityForestry<?> oldHolderBe = TileUtil.getTile(level, oldHolder, MultiblockTileEntityForestry.class);
				if (oldHolderBe != null) {
					oldHolderBe.clearStash();
					oldHolderBe.setAnchorPos(holderPos);
				}
				MultiblockController.markChunkDirty(level, oldHolder);
			}
		}

		// MINOR 4: only do the heavy re-bucket + per-part onMachineAssembled re-fire on a genuine transition
		// (first formation, or deactivated→assembled including reload), or when the member set actually
		// changed. A redundant re-validation on a stable assembled machine (every neighborChanged/onLoad)
		// must not re-bucket — that re-randomizes FarmController's per-Active tick offsets and triggers N×N
		// blockstate refreshes — nor re-fire the assembled visuals.
		boolean wasAssembled = controller.isAssembled();
		boolean sameMembers = wasAssembled && members.equals(controller.getMembers());
		boolean holderUnchanged = holderPos.equals(controller.getHolderPos());

		if (wasAssembled && sameMembers && holderUnchanged) {
			// Stable, already-assembled machine: no structural change. Keep the index/error state fresh and make
			// sure the triggering member is anchored (it may have just reloaded), but skip the expensive
			// re-bucket and the per-part onMachineAssembled re-fire (MINOR 4).
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

		// Wire every member's anchorPos to the holder so getController()/the ticker resolve correctly.
		for (BlockPos mpos : members) {
			MultiblockTileEntityForestry<?> mbe = TileUtil.getTile(level, mpos, MultiblockTileEntityForestry.class);
			if (mbe != null) {
				mbe.setAnchorPos(holderPos);
			}
		}

		// Single-holder invariant (spec §6.1): clear the stash on EVERY member so exactly one loaded member (the
		// holder) writes PAYLOAD_KEY. The live controller now owns the canonical payload (seeded from the lowest
		// non-empty stash above), so any residual stash on a member — the holder's own, a hand-off survivor's
		// (BUG 1), or a discarded §10 multi-carrier tag — must be dropped: otherwise its saveAdditional non-holder
		// branch would re-emit a stale PAYLOAD_KEY and a second member would serialize the payload (RE-INTRODUCES
		// corruption). Every member is loaded at assembly, so this is always safe.
		for (BlockPos mpos : members) {
			MultiblockTileEntityForestry<?> mbe = TileUtil.getTile(level, mpos, MultiblockTileEntityForestry.class);
			if (mbe != null) {
				mbe.clearStash();
			}
		}

		// Owner vote-once (spec §3.1 E1): only the very first formation votes; reloads keep the payload owner.
		if (firstFormation && !controller.isOwnerResolved()) {
			controller.voteOwnerOnceIfNeeded();
		}

		controller.onAssembled();

		// Re-fire per-part assembled visuals on every transition into assembled (spec §7.3), incl. reloads.
		BlockPos min = controller.getMinimumCoord();
		BlockPos max = controller.getMaximumCoord();
		for (IMultiblockComponent part : controller.getComponents()) {
			part.onMachineAssembled(controller, min, max);
		}
	}

	/**
	 * Deactivates the machine this block currently anchors/belongs to (a structural change made it invalid).
	 * Records the failure key for the on-demand chat message (spec §11). The genuine break / re-anchor
	 * hand-off itself is handled in {@code MultiblockTileEntityForestry.setRemoved} (§6.4); here we only
	 * flip the assembled flag and fire the per-part broken callbacks.
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
		if (failure != null && !failure.firstKey().equals(forestry.core.multiblock.pattern.Predicates.KEY_NOT_MAXIMAL)) {
			// Build the localized message with its format args filled (spec Task A.3); skip the internal
			// "wrong candidate origin" deferral, which is never shown to the player.
			controller.setLastValidationError(buildMessage(level, failure.first()).getString());
		}
		// Deregister the now-unformed controller from the index so deactivated controllers don't accumulate
		// (MAJOR 1: per-level leak). Before dropping the index entry, hand the live controller's payload back to
		// the holder BE as its stash so (a) a save before re-validation still persists it (holder-gated, via the
		// saveAdditional stash branch) and (b) a later re-validation re-adopts it through applyStashTo. The
		// genuine break / re-anchor hand-off in MultiblockTileEntityForestry deregisters separately.
		MultiblockTileEntityForestry<?> holder = TileUtil.getTile(level, anchorPos, MultiblockTileEntityForestry.class);
		if (holder != null) {
			holder.stashFrom(controller);
		}
		MultiblockIndex.deregister(level, anchorPos);
	}

	/**
	 * Seeds a freshly-created controller from the lowest-(x,y,z) member that carries a NON-EMPTY stash (spec
	 * §6.4 re-anchor recovery / §10 migration tie-break). The members list is produced lowest-first by the
	 * pattern engine, but we compare positions explicitly so the result is independent of list order: the
	 * lowest non-empty carrier wins, and members with no stash (the re-added corner) or an empty one are
	 * skipped — so we never overwrite the recovered payload with an empty tag. If no member carries a stash
	 * (a genuinely brand-new structure), the controller is left at its constructed empty defaults.
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

	/** Finds the controller currently hosted by any loaded member (steady or post-partial-reload). */
	@Nullable
	private static MultiblockController resolveExistingController(Level level, List<BlockPos> members) {
		for (BlockPos pos : members) {
			MultiblockController c = MultiblockIndex.get(level, pos);
			if (c != null) {
				return c;
			}
		}
		// Also consult each member BE's stored anchorPos in case the index entry is keyed elsewhere.
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

	/** Re-validate every neighbor of {@code pos} after a break (spec §5.3). */
	public static void validateNeighbors(Level level, BlockPos pos) {
		for (net.minecraft.core.Direction dir : net.minecraft.core.Direction.values()) {
			validateAt(level, pos.relative(dir));
		}
	}
}

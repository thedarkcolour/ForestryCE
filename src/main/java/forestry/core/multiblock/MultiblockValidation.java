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
		@Nullable PatternResult.Failure firstFailure = null;
		for (StructurePos candidate : pattern.candidateOrigins(origin)) {
			PatternResult result = pattern.validate(view, candidate);
			if (result instanceof PatternResult.Match m) {
				// Confirm this position is actually a member of the matched structure (candidate origins are
				// generated permissively over all sizes/offsets, so a match might not contain pos).
				if (containsPos(m, pos)) {
					match = m;
					break;
				}
			} else if (firstFailure == null) {
				firstFailure = (PatternResult.Failure) result;
			}
		}

		if (match != null) {
			assemble(level, member, match);
		} else {
			deactivate(level, member, firstFailure);
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
	public static String findValidationHint(Level level, BlockPos pos, MultiblockTileEntityForestry<?> member) {
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
		return best == null ? null : best.firstKey();
	}

	/** Ranks a failure key by how player-meaningful it is (higher = preferred for the chat hint, spec §11). */
	private static int hintRank(String key) {
		// Generic loaded-shell deferrals are least useful; a "this cell is/ isn't a component" message tells the
		// player nothing actionable when they're staring at a half-built machine.
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
			// Seed it from the holder's stashed payload (steady-state save or legacy migration).
			holder.applyStashTo(controller);
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

		// Re-point the (new) holder so its stash no longer shadows the live controller. After this, exactly one
		// loaded member (the holder) writes PAYLOAD_KEY: the old holder above had its stash cleared and anchor
		// re-pointed, every other member is a non-holder with a null stash, and only the holder's isHolder()
		// branch in saveAdditional serializes the controller payload (spec §6.1).
		holder.clearStash();

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
		if (failure != null) {
			controller.setLastValidationError(net.minecraft.network.chat.Component.translatable(failure.firstKey()).getString());
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

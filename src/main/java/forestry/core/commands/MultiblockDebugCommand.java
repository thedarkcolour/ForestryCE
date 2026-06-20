package forestry.core.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

import forestry.core.commands.MultiblockDebugLogic.Fingerprint;
import forestry.core.commands.MultiblockDebugLogic.Order;
import forestry.core.multiblock.MultiblockController;
import forestry.core.multiblock.MultiblockIndex;
import forestry.core.multiblock.MultiblockTileEntityForestry;
import forestry.core.multiblock.MultiblockValidation;
import forestry.core.tiles.TileUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

/**
 * {@code /forestry multiblock debug <x> <y> <z> [inspect|cycle [rounds] [order]]} (spec Task B).
 *
 * <p>A server-side (op level 2), main-thread debug tool for deterministic, repeatable testing of a
 * multiblock's data integrity across a simulated chunk unload→reload — without real {@code /forceload}
 * tickets (which keep chunks loaded and pull in neighbour chunks, making chunk-border tests finicky).
 *
 * <ul>
 *   <li><b>inspect</b> (default): reports the assembled state, holder, member positions grouped by chunk
 *       (with loaded-ness), a content summary, and — the key integrity check — how many members emit the
 *       {@code PAYLOAD_KEY} tag in {@code saveAdditional} (the single-holder invariant, must be exactly 1).</li>
 *   <li><b>cycle [rounds] [order]</b>: deterministically simulates an unload→reload of the member block
 *       entities {@code rounds} times in the chosen {@link Order} (default {@link Order#ANCHOR_LAST}, the
 *       order the old engine corrupted), capturing each member's {@code saveAdditional} tag, resetting the
 *       in-memory engine state, then replaying {@code load}+{@code onLoad} to re-run validation/assembly
 *       exactly as a real chunk reload would, and diffing a content fingerprint before vs after.</li>
 * </ul>
 *
 * <p>It is intentionally clarity-over-polish, and never throws on a missing / non-multiblock target — it
 * prints a friendly error and returns 0.
 */
public final class MultiblockDebugCommand {
	private MultiblockDebugCommand() {
	}

	public static ArgumentBuilder<CommandSourceStack, ?> register() {
		// /forestry multiblock debug <pos> [inspect | cycle [rounds] [order] | realcycle [rounds] [order]]
		return Commands.literal("multiblock").requires(CommandHelpers.ADMIN)
				.then(Commands.literal("debug")
						.then(Commands.argument("pos", BlockPosArgument.blockPos())
								.executes(ctx -> inspect(ctx, BlockPosArgument.getLoadedBlockPos(ctx, "pos")))
								.then(Commands.literal("inspect")
										.executes(ctx -> inspect(ctx, BlockPosArgument.getLoadedBlockPos(ctx, "pos"))))
								.then(Commands.literal("cycle")
										.executes(ctx -> cycle(ctx, BlockPosArgument.getLoadedBlockPos(ctx, "pos"), 1, Order.ANCHOR_LAST))
										.then(roundsArg(MultiblockDebugCommand::cycle)))
								.then(Commands.literal("realcycle")
										.executes(ctx -> realcycle(ctx, BlockPosArgument.getLoadedBlockPos(ctx, "pos"), 1, Order.ANCHOR_LAST))
										.then(roundsArg(MultiblockDebugCommand::realcycle)))));
	}

	/** A cycle-style executor: {@code (ctx, pos, rounds, order) -> exit code}. Shared by {@code cycle}/{@code realcycle}. */
	@FunctionalInterface
	private interface CycleExec {
		int run(CommandContext<CommandSourceStack> ctx, BlockPos pos, int rounds, Order order);
	}

	/**
	 * Builds the {@code [rounds] [order]} argument subtree wired to {@code exec}: a {@code rounds} integer arg
	 * (default order {@link Order#ANCHOR_LAST}) with one tab-completable literal per {@link Order} token as a
	 * sibling. Used by both the {@code cycle} (in-place simulation) and {@code realcycle} (genuine BE reload) modes.
	 */
	private static RequiredArgumentBuilder<CommandSourceStack, Integer> roundsArg(CycleExec exec) {
		var rounds = Commands.argument("rounds", IntegerArgumentType.integer(1, 1000))
				.executes(ctx -> exec.run(ctx, BlockPosArgument.getLoadedBlockPos(ctx, "pos"), IntegerArgumentType.getInteger(ctx, "rounds"), Order.ANCHOR_LAST));
		for (String token : Order.tokens()) {
			Order order = Order.parse(token);
			rounds.then(Commands.literal(token)
					.executes(ctx -> exec.run(ctx, BlockPosArgument.getLoadedBlockPos(ctx, "pos"), IntegerArgumentType.getInteger(ctx, "rounds"), order)));
		}
		return rounds;
	}

	/* ===== Resolution ===== */

	/**
	 * Resolves the multiblock at {@code pos}: the member BE, then its controller via the member's anchorPos →
	 * {@link MultiblockIndex}; if that is null (never assembled), runs a validation pass and retries. Returns
	 * {@code null} (after messaging the sender) if there is no multiblock BE there.
	 */
	@Nullable
	private static Resolved resolve(CommandSourceStack source, ServerLevel level, BlockPos pos) {
		MultiblockTileEntityForestry<?> member = TileUtil.getTile(level, pos, MultiblockTileEntityForestry.class);
		if (member == null) {
			source.sendFailure(Component.literal("No Forestry multiblock block entity at " + str(pos) + "."));
			return null;
		}
		MultiblockController controller = member.getController();
		if (controller == null) {
			// Not currently assembled here: try to (re)form it so inspect has something to report.
			MultiblockValidation.validateFor(level, pos, member);
			controller = member.getController();
		}
		return new Resolved(member, controller);
	}

	private record Resolved(MultiblockTileEntityForestry<?> member, @Nullable MultiblockController controller) {
	}

	/* ===== inspect ===== */

	private static int inspect(CommandContext<CommandSourceStack> ctx, BlockPos pos) {
		CommandSourceStack source = ctx.getSource();
		ServerLevel level = source.getLevel();
		Resolved resolved = resolve(source, level, pos);
		if (resolved == null) {
			return 0;
		}
		MultiblockController controller = resolved.controller();

		send(source, Component.literal("=== Multiblock @ " + str(pos) + " ===").withStyle(ChatFormatting.AQUA));
		if (controller == null) {
			send(source, Component.literal("assembled: false (no controller — block is not part of a formed machine)")
					.withStyle(ChatFormatting.YELLOW));
			return 1;
		}

		List<BlockPos> members = controller.getMembers();
		BlockPos holder = controller.getHolderPos();
		send(source, line("assembled", String.valueOf(controller.isAssembled())));
		send(source, line("holderPos", holder == null ? "null" : str(holder)));
		send(source, line("members", String.valueOf(members.size())));

		// Members grouped by chunk, with loaded-ness.
		Map<String, List<BlockPos>> byChunk = new TreeMap<>();
		for (BlockPos m : members) {
			byChunk.computeIfAbsent("[" + (m.getX() >> 4) + ", " + (m.getZ() >> 4) + "]", k -> new ArrayList<>()).add(m);
		}
		send(source, Component.literal("members by chunk (" + byChunk.size() + " chunk(s)):").withStyle(ChatFormatting.GRAY));
		for (Map.Entry<String, List<BlockPos>> e : byChunk.entrySet()) {
			BlockPos any = e.getValue().get(0);
			boolean loaded = level.getChunkSource().hasChunk(any.getX() >> 4, any.getZ() >> 4);
			send(source, Component.literal("  chunk " + e.getKey() + " " + (loaded ? "[loaded]" : "[UNLOADED]")
					+ " x" + e.getValue().size()).withStyle(loaded ? ChatFormatting.GRAY : ChatFormatting.RED));
		}

		// Content summary.
		Fingerprint fp = fingerprint(level, controller, members);
		send(source, line("inventoryItems", String.valueOf(fp.inventoryItems())));
		send(source, line("payloadHash", Integer.toHexString(fp.payloadHash())));

		// The key integrity check: single-holder invariant.
		boolean ok = fp.singleHolderOk();
		send(source, Component.literal("payloadCarriers (members emitting " + MultiblockTileEntityForestry.PAYLOAD_KEY
						+ "): " + fp.payloadCarriers() + "  -> single-holder invariant: " + (ok ? "PASS" : "FAIL"))
				.withStyle(ok ? ChatFormatting.GREEN : ChatFormatting.RED));
		return 1;
	}

	/* ===== cycle ===== */

	private static int cycle(CommandContext<CommandSourceStack> ctx, BlockPos pos, int rounds, Order order) {
		CommandSourceStack source = ctx.getSource();
		ServerLevel level = source.getLevel();
		Resolved resolved = resolve(source, level, pos);
		if (resolved == null) {
			return 0;
		}
		MultiblockController controller = resolved.controller();
		if (controller == null || !controller.isAssembled() || controller.getHolderPos() == null) {
			source.sendFailure(Component.literal("Multiblock at " + str(pos) + " is not assembled; cannot run a reload cycle."));
			return 0;
		}

		send(source, Component.literal("=== Cycle @ " + str(pos) + "  rounds=" + rounds + "  order=" + order + " ===")
				.withStyle(ChatFormatting.AQUA));

		boolean allPass = true;
		// Track the live controller across rounds: each round resets + re-forms it (a NEW controller object),
		// so round N+1 must snapshot from the controller round N produced, not the stale deregistered one.
		MultiblockController live = controller;
		for (int round = 1; round <= rounds; round++) {
			// (1) snapshot the BEFORE fingerprint from the live engine state.
			List<BlockPos> members = new ArrayList<>(live.getMembers());
			BlockPos holder = live.getHolderPos();
			if (holder == null || members.isEmpty()) {
				// A previous round corrupted the structure into an unassembled / memberless state — stop and
				// report rather than NPE; the FAIL from that round is already shown.
				send(source, Component.literal("round " + round + ": ABORTED (structure no longer assembled after a prior round)")
						.withStyle(ChatFormatting.RED));
				allPass = false;
				break;
			}
			Fingerprint before = fingerprint(level, live, members);

			// (2) capture each member's saveAdditional tag (the bytes a real save would write).
			Map<BlockPos, CompoundTag> captured = new java.util.HashMap<>();
			for (BlockPos m : members) {
				MultiblockTileEntityForestry<?> be = TileUtil.getTile(level, m, MultiblockTileEntityForestry.class);
				if (be != null) {
					CompoundTag tag = new CompoundTag();
					be.saveAdditional(tag);
					captured.put(m, tag);
				}
			}

			// (3) reset the in-memory engine state to mimic a fresh load: drop every index entry that points
			// at a member or holder, and clear each member BE's anchorPos / stash / cached controller link.
			for (BlockPos m : members) {
				MultiblockIndex.deregister(level, m);
			}
			MultiblockIndex.deregister(level, holder);
			for (BlockPos m : members) {
				MultiblockTileEntityForestry<?> be = TileUtil.getTile(level, m, MultiblockTileEntityForestry.class);
				if (be != null) {
					be.setAnchorPos(null);
					be.clearStash();
				}
			}

			// (4) replay load(capturedTag) then onLoad() per member in the chosen order — this re-runs
			// validation/assembly exactly as a real chunk reload would (onLoad -> MultiblockValidation
			// .validateFor). Each member's engine state was reset in step (3), so load() re-reads its
			// anchorPos + stashed PAYLOAD_KEY from the captured tag and onLoad() re-forms the controller from
			// scratch. The ORDER decides which member's onLoad first triggers assembly and therefore which
			// member seeds the fresh controller from its stash and whose stash is cleared — i.e. it exercises
			// the order-sensitive single-holder seeding + canonicalization paths the old engine corrupted
			// (anchorLast, the default, replays the anchor/holder LAST). The member blocks remain physically
			// loaded (we deliberately avoid /forceload chunk tickets), so this reproduces the save->load->
			// validate + single-holder + canonicalization paths rather than a genuine partial chunk presence.
			List<BlockPos> replayOrder = MultiblockDebugLogic.orderMembers(members, holder, BlockPos::compareTo, order);
			for (BlockPos m : replayOrder) {
				MultiblockTileEntityForestry<?> be = TileUtil.getTile(level, m, MultiblockTileEntityForestry.class);
				CompoundTag tag = captured.get(m);
				if (be != null && tag != null) {
					be.load(tag);
					be.onLoad();
				}
			}

			// (5) recompute the fingerprint from the re-formed engine state and diff. The holder may have been
			// canonicalized/handed off during re-validation, so resolve the live controller from any member.
			MultiblockController after = resolveAnyController(level, members);
			Fingerprint afterFp = after == null
					? new Fingerprint(false, 0, "null", 0, -1, countPayloadCarriers(level, members))
					: fingerprint(level, after, after.getMembers());

			List<String> diff = MultiblockDebugLogic.diff(before, afterFp);
			boolean roundPass = diff.isEmpty() && afterFp.singleHolderOk();
			allPass &= roundPass;
			send(source, Component.literal("round " + round + ": " + (roundPass ? "PASS" : "FAIL"))
					.withStyle(roundPass ? ChatFormatting.GREEN : ChatFormatting.RED));
			if (!roundPass) {
				if (!afterFp.singleHolderOk()) {
					send(source, Component.literal("  single-holder invariant FAILED: payloadCarriers="
							+ afterFp.payloadCarriers() + " (expected 1)").withStyle(ChatFormatting.RED));
				}
				for (String change : diff) {
					send(source, Component.literal("  " + change).withStyle(ChatFormatting.RED));
				}
			}

			// Advance to the re-formed controller for the next round (if the structure survived).
			if (after != null) {
				live = after;
			}
		}

		send(source, Component.literal("=== Cycle result: " + (allPass ? "PASS" : "FAIL") + " ===")
				.withStyle(allPass ? ChatFormatting.GREEN : ChatFormatting.RED));
		return allPass ? 1 : 0;
	}

	/* ===== realcycle ===== */

	/**
	 * The genuine-reload counterpart of {@link #cycle}: instead of resetting in-memory state in place, it really
	 * tears down and re-creates each member block entity, firing the same lifecycle a chunk eviction does —
	 * {@code onChunkUnloaded} → {@code setRemoved} (the unload path) → a fresh {@link BlockEntity#loadStatic} from
	 * the member's real {@code saveWithId} NBT → {@code onLoad} (re-validation). It clears the in-memory
	 * {@link MultiblockIndex} for the structure first, so the reload takes the fresh-world-load path (firstFormation
	 * + seed-from-stash) — the exact save→load→validate→seed pipeline the old engine corrupted.
	 *
	 * <p>This is the stronger benchmark requested for cross-checking against the old multiblock system: it uses
	 * fresh BlockEntity objects and real teardown callbacks rather than the {@code cycle} in-place simulation, so a
	 * methodology that passes here is faithful to a real chunk reload. It does not evict the whole chunk (the blocks
	 * stay put — that would fight the chunk ticket system); it reloads the block entities, where all multiblock
	 * state lives. The anchor's ticker is restored via {@code addAndRegisterBlockEntity}, so the machine keeps
	 * running afterwards.
	 */
	private static int realcycle(CommandContext<CommandSourceStack> ctx, BlockPos pos, int rounds, Order order) {
		CommandSourceStack source = ctx.getSource();
		ServerLevel level = source.getLevel();
		Resolved resolved = resolve(source, level, pos);
		if (resolved == null) {
			return 0;
		}
		MultiblockController controller = resolved.controller();
		if (controller == null || !controller.isAssembled() || controller.getHolderPos() == null) {
			source.sendFailure(Component.literal("Multiblock at " + str(pos) + " is not assembled; cannot run a reload cycle."));
			return 0;
		}

		send(source, Component.literal("=== Real reload @ " + str(pos) + "  rounds=" + rounds + "  order=" + order + " ===")
				.withStyle(ChatFormatting.AQUA));
		send(source, Component.literal("(genuinely recreates each member BE: onChunkUnloaded -> setRemoved -> loadStatic -> onLoad)")
				.withStyle(ChatFormatting.DARK_GRAY));

		// This command mutates a live world by recreating block entities. Refuse if any member sits in an unloaded
		// chunk: we must never force-load + tear down a half-present structure (that would let validation auto-create
		// empty BEs in the just-loaded chunk and corrupt a REAL machine). All members must be loaded up front.
		for (BlockPos m : controller.getMembers()) {
			if (!level.getChunkSource().hasChunk(m.getX() >> 4, m.getZ() >> 4)) {
				source.sendFailure(Component.literal("Member at " + str(m) + " is in an unloaded chunk; load the whole multiblock first (see inspect)."));
				return 0;
			}
		}

		boolean allPass = true;
		MultiblockController live = controller;
		for (int round = 1; round <= rounds; round++) {
			List<BlockPos> members = new ArrayList<>(live.getMembers());
			BlockPos holder = live.getHolderPos();
			if (holder == null || members.isEmpty()) {
				send(source, Component.literal("round " + round + ": ABORTED (structure no longer assembled after a prior round)")
						.withStyle(ChatFormatting.RED));
				allPass = false;
				break;
			}
			Fingerprint before = fingerprint(level, live, members);

			// (1) Capture each member's real saved NBT (saveAdditional + id) and blockstate, and DRY-RUN the fresh
			// recreation BEFORE any teardown: build every replacement BlockEntity via loadStatic up front. If any
			// returns null (corrupt / incompatible tag), abort this round WITHOUT touching the live machine — so a
			// "non-destructive" debug command can never leave a real multiblock damaged behind a half-done teardown.
			Map<BlockPos, BlockEntity> fresh = new java.util.HashMap<>();
			boolean prepOk = true;
			for (BlockPos m : members) {
				MultiblockTileEntityForestry<?> be = TileUtil.getTile(level, m, MultiblockTileEntityForestry.class);
				if (be == null) {
					prepOk = false;
					send(source, Component.literal("round " + round + ": ABORTED (no block entity at member " + str(m) + ")").withStyle(ChatFormatting.RED));
					break;
				}
				CompoundTag tag = be.saveWithId();
				BlockState state = level.getBlockState(m);
				BlockEntity recreated = BlockEntity.loadStatic(m, state, tag);
				if (recreated == null) {
					prepOk = false;
					send(source, Component.literal("round " + round + ": ABORTED (loadStatic failed for member " + str(m) + " — machine left untouched)").withStyle(ChatFormatting.RED));
					break;
				}
				fresh.put(m, recreated);
			}
			if (!prepOk) {
				allPass = false;
				break;
			}

			MultiblockController after;
			try {
				// (2) genuine unload: fire onChunkUnloaded on every member (matching the real chunk order), then remove
				// each block entity (real setRemoved on the unload path — no re-anchor / drop).
				for (BlockPos m : members) {
					MultiblockTileEntityForestry<?> be = TileUtil.getTile(level, m, MultiblockTileEntityForestry.class);
					if (be != null) {
						be.onChunkUnloaded();
					}
				}
				for (BlockPos m : members) {
					level.removeBlockEntity(m);
				}

				// (3) drop the in-memory index for these positions so the reload re-forms from scratch (fresh-world-load
				// path: firstFormation + seed-from-stash), the most adversarial / corruption-prone pipeline.
				for (BlockPos m : members) {
					MultiblockIndex.deregister(level, m);
				}
				MultiblockIndex.deregister(level, holder);

				// (4a) place the pre-built fresh block entities WITHOUT firing onLoad, so every real member is present
				// before any validation runs (otherwise the first member's onLoad would sample the not-yet-recreated
				// positions and Level#getBlockEntity (IMMEDIATE) would auto-create EMPTY BEs for them, churning the
				// seed-from-stash path and corrupting the result inside the test harness itself).
				for (BlockPos m : members) {
					BlockEntity be = fresh.get(m);
					if (be != null) {
						level.getChunkAt(m).setBlockEntity(be);
					}
				}

				// (4b) fire onLoad in the chosen (adversarial) order via addAndRegisterBlockEntity, which also restores
				// each member's ticker. All real members are present, so validation samples only real BEs.
				List<BlockPos> replayOrder = MultiblockDebugLogic.orderMembers(members, holder, BlockPos::compareTo, order);
				for (BlockPos m : replayOrder) {
					BlockEntity be = fresh.get(m);
					if (be != null) {
						level.getChunkAt(m).addAndRegisterBlockEntity(be);
					}
				}
			} catch (RuntimeException ex) {
				// Defense-in-depth: a mid-teardown failure must not silently leave a real machine unassembled. Re-run
				// validation from the holder to recover whatever survived, surface the error, and stop.
				MultiblockValidation.validateAt(level, holder);
				source.sendFailure(Component.literal("round " + round + ": EXCEPTION during reload (" + ex + "); attempted recovery via re-validation."));
				allPass = false;
				break;
			}

			// (5) recompute the fingerprint from the re-formed engine state and diff.
			after = resolveAnyController(level, members);

			// Push a block update on the re-formed holder so any client viewing the structure re-syncs promptly after
			// the server-side BE swap (the fresh controller has a new inventory object; an open GUI rebinds/closes).
			if (after != null && after.getHolderPos() != null) {
				BlockPos h = after.getHolderPos();
				level.sendBlockUpdated(h, level.getBlockState(h), level.getBlockState(h), 3);
			}
			Fingerprint afterFp = after == null
					? new Fingerprint(false, 0, "null", 0, -1, countPayloadCarriers(level, members))
					: fingerprint(level, after, after.getMembers());

			List<String> diff = MultiblockDebugLogic.diff(before, afterFp);
			boolean roundPass = diff.isEmpty() && afterFp.singleHolderOk();
			allPass &= roundPass;
			send(source, Component.literal("round " + round + ": " + (roundPass ? "PASS" : "FAIL"))
					.withStyle(roundPass ? ChatFormatting.GREEN : ChatFormatting.RED));
			if (!roundPass) {
				if (!afterFp.singleHolderOk()) {
					send(source, Component.literal("  single-holder invariant FAILED: payloadCarriers="
							+ afterFp.payloadCarriers() + " (expected 1)").withStyle(ChatFormatting.RED));
				}
				for (String change : diff) {
					send(source, Component.literal("  " + change).withStyle(ChatFormatting.RED));
				}
			}

			if (after != null) {
				live = after;
			}
		}

		send(source, Component.literal("=== Real reload result: " + (allPass ? "PASS" : "FAIL") + " ===")
				.withStyle(allPass ? ChatFormatting.GREEN : ChatFormatting.RED));
		return allPass ? 1 : 0;
	}

	/* ===== Fingerprint ===== */

	private static Fingerprint fingerprint(ServerLevel level, MultiblockController controller, List<BlockPos> members) {
		BlockPos holder = controller.getHolderPos();
		CompoundTag payload = new CompoundTag();
		controller.writePayload(payload);
		int payloadHash = payload.toString().hashCode();

		int items = 0;
		try {
			int size = controller.getInternalInventory().getContainerSize();
			for (int i = 0; i < size; i++) {
				ItemStack stack = controller.getInternalInventory().getItem(i);
				items += stack.getCount();
			}
		} catch (RuntimeException ex) {
			items = -1;
		}

		return new Fingerprint(
				controller.isAssembled(),
				members.size(),
				holder == null ? "null" : str(holder),
				payloadHash,
				items,
				countPayloadCarriers(level, members)
		);
	}

	/** Counts how many loaded members emit {@code PAYLOAD_KEY} in their {@code saveAdditional} (must be 1). */
	private static int countPayloadCarriers(ServerLevel level, List<BlockPos> members) {
		int carriers = 0;
		for (BlockPos m : members) {
			MultiblockTileEntityForestry<?> be = TileUtil.getTile(level, m, MultiblockTileEntityForestry.class);
			if (be != null) {
				CompoundTag tag = new CompoundTag();
				be.saveAdditional(tag);
				if (tag.contains(MultiblockTileEntityForestry.PAYLOAD_KEY)) {
					carriers++;
				}
			}
		}
		return carriers;
	}

	@Nullable
	private static MultiblockController resolveAnyController(ServerLevel level, List<BlockPos> members) {
		for (BlockPos m : members) {
			MultiblockTileEntityForestry<?> be = TileUtil.getTile(level, m, MultiblockTileEntityForestry.class);
			if (be != null) {
				MultiblockController c = be.getController();
				if (c != null) {
					return c;
				}
			}
		}
		return null;
	}

	/* ===== Small helpers ===== */

	private static Component line(String key, String value) {
		return Component.literal(key + ": ").withStyle(ChatFormatting.GRAY)
				.append(Component.literal(value).withStyle(ChatFormatting.WHITE));
	}

	private static void send(CommandSourceStack source, Component message) {
		source.sendSuccess(() -> message, false);
	}

	private static String str(BlockPos pos) {
		return pos.getX() + "," + pos.getY() + "," + pos.getZ();
	}
}

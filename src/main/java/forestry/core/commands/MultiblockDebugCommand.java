package forestry.core.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
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
		// /forestry multiblock debug <pos> [inspect | cycle [rounds] [order]]
		// One tab-completable literal per Order token, all siblings under the rounds argument node.
		var rounds = Commands.argument("rounds", IntegerArgumentType.integer(1, 1000))
				.executes(ctx -> cycle(ctx, BlockPosArgument.getLoadedBlockPos(ctx, "pos"), IntegerArgumentType.getInteger(ctx, "rounds"), Order.ANCHOR_LAST));
		for (String token : Order.tokens()) {
			Order order = Order.parse(token);
			rounds.then(Commands.literal(token)
					.executes(ctx -> cycle(ctx, BlockPosArgument.getLoadedBlockPos(ctx, "pos"), IntegerArgumentType.getInteger(ctx, "rounds"), order)));
		}

		return Commands.literal("multiblock").requires(CommandHelpers.ADMIN)
				.then(Commands.literal("debug")
						.then(Commands.argument("pos", BlockPosArgument.blockPos())
								.executes(ctx -> inspect(ctx, BlockPosArgument.getLoadedBlockPos(ctx, "pos")))
								.then(Commands.literal("inspect")
										.executes(ctx -> inspect(ctx, BlockPosArgument.getLoadedBlockPos(ctx, "pos"))))
								.then(Commands.literal("cycle")
										.executes(ctx -> cycle(ctx, BlockPosArgument.getLoadedBlockPos(ctx, "pos"), 1, Order.ANCHOR_LAST))
										.then(rounds))));
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

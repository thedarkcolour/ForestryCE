package forestry.core.commands;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nullable;

/**
 * The {@code net.minecraft}-free core of the {@code /forestry multiblock debug} command (spec Task B): the
 * member replay {@link Order ordering} and the content {@link Fingerprint} + its diff. Kept off the
 * Minecraft classpath so the deterministic logic is unit-testable without a running server (the command
 * itself, {@code MultiblockDebugCommand}, supplies the live {@code BlockPos}/world bindings).
 */
public final class MultiblockDebugLogic {
	private MultiblockDebugLogic() {
	}

	/**
	 * The order in which member block entities are reloaded during a simulated unload→reload cycle. The old
	 * engine corrupted the payload when the anchor (lowest member) reloaded LAST (its non-holder neighbours
	 * had already adopted/serialized the payload), so {@link #ANCHOR_LAST} is the default — the most
	 * adversarial order. The others probe the surrounding orderings.
	 */
	public enum Order {
		/** The anchor (holder / lowest member) reloads first, then the rest ascending. */
		ANCHOR_FIRST,
		/** The anchor reloads last, the rest ascending before it (default — old corruption order). */
		ANCHOR_LAST,
		/** Strict descending member order (anchor, being lowest, ends up last). */
		REVERSE,
		/** The natural ascending member order as the engine produced it (anchor first by construction). */
		AS_IS;

		/** Parses the command token (case-insensitive, camelCase as the player types it), or {@code null}. */
		@Nullable
		public static Order parse(String token) {
			return switch (token.toLowerCase(Locale.ROOT)) {
				case "anchorfirst" -> ANCHOR_FIRST;
				case "anchorlast" -> ANCHOR_LAST;
				case "reverse" -> REVERSE;
				case "asis" -> AS_IS;
				default -> null;
			};
		}

		/** The camelCase tokens accepted by {@link #parse}, for suggestions / help text. */
		public static List<String> tokens() {
			return List.of("anchorFirst", "anchorLast", "reverse", "asIs");
		}
	}

	/**
	 * Produces the replay order of {@code members} for the given {@link Order}. {@code comparator} defines
	 * the canonical ascending member order (the engine sorts members lowest-first); {@code anchor} is the
	 * holder position. The input list is not mutated.
	 *
	 * @param members    the member positions (any order)
	 * @param anchor     the holder / anchor position (must be present in {@code members})
	 * @param comparator the canonical ascending order over positions
	 */
	public static <P> List<P> orderMembers(List<P> members, P anchor, Comparator<P> comparator, Order order) {
		List<P> sorted = new ArrayList<>(members);
		sorted.sort(comparator);
		return switch (order) {
			case AS_IS -> sorted;
			case REVERSE -> {
				List<P> reversed = new ArrayList<>(sorted);
				java.util.Collections.reverse(reversed);
				yield reversed;
			}
			case ANCHOR_FIRST -> {
				List<P> result = new ArrayList<>(sorted.size());
				result.add(anchor);
				for (P p : sorted) {
					if (!p.equals(anchor)) {
						result.add(p);
					}
				}
				yield result;
			}
			case ANCHOR_LAST -> {
				List<P> result = new ArrayList<>(sorted.size());
				for (P p : sorted) {
					if (!p.equals(anchor)) {
						result.add(p);
					}
				}
				result.add(anchor);
				yield result;
			}
		};
	}

	/**
	 * A deterministic content fingerprint of a multiblock, captured before and after a simulated reload and
	 * compared for corruption. All fields are derived from in-memory engine state with no Minecraft types:
	 *
	 * @param assembled      whether the controller reports assembled
	 * @param memberCount    number of members in the validated set
	 * @param holder         the holder position rendered as a stable string (e.g. {@code "10,64,-5"})
	 * @param payloadHash    a stable hash of the holder controller's {@code writePayload} NBT (the shared
	 *                       inventory + owner + machine state); 0 when unassembled
	 * @param inventoryItems total item count across the shared controller inventory; -1 when unavailable
	 * @param payloadCarriers how many loaded members emit the {@code PAYLOAD_KEY} tag in their
	 *                       {@code saveAdditional} output — the single-holder invariant (must be exactly 1)
	 */
	public record Fingerprint(
			boolean assembled,
			int memberCount,
			String holder,
			int payloadHash,
			int inventoryItems,
			int payloadCarriers
	) {
		/** True iff the single-holder invariant holds: exactly one loaded member serializes the payload. */
		public boolean singleHolderOk() {
			return this.payloadCarriers == 1;
		}
	}

	/**
	 * Compares a before/after fingerprint pair and returns a human-readable list of the fields that changed
	 * (empty when identical). Used by the {@code cycle} mode to report exactly what the simulated reload
	 * lost / duplicated / zeroed.
	 */
	public static List<String> diff(Fingerprint before, Fingerprint after) {
		List<String> changes = new ArrayList<>();
		if (before.assembled() != after.assembled()) {
			changes.add("assembled: " + before.assembled() + " -> " + after.assembled());
		}
		if (before.memberCount() != after.memberCount()) {
			changes.add("memberCount: " + before.memberCount() + " -> " + after.memberCount());
		}
		if (!before.holder().equals(after.holder())) {
			changes.add("holder: " + before.holder() + " -> " + after.holder());
		}
		if (before.payloadHash() != after.payloadHash()) {
			changes.add("payloadHash: " + Integer.toHexString(before.payloadHash())
					+ " -> " + Integer.toHexString(after.payloadHash()) + " (CONTENT CHANGED)");
		}
		if (before.inventoryItems() != after.inventoryItems()) {
			changes.add("inventoryItems: " + before.inventoryItems() + " -> " + after.inventoryItems());
		}
		if (before.payloadCarriers() != after.payloadCarriers()) {
			changes.add("payloadCarriers: " + before.payloadCarriers() + " -> " + after.payloadCarriers()
					+ " (single-holder invariant)");
		}
		return changes;
	}
}

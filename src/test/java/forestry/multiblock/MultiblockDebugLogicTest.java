package forestry.multiblock;

import java.util.Comparator;
import java.util.List;

import org.junit.jupiter.api.Test;

import forestry.core.commands.MultiblockDebugLogic;
import forestry.core.commands.MultiblockDebugLogic.Fingerprint;
import forestry.core.commands.MultiblockDebugLogic.Order;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the {@code net.minecraft}-free core of the {@code /forestry multiblock debug} command:
 * the {@link Order} parsing + member replay ordering, and the {@link Fingerprint} diff / single-holder check.
 */
class MultiblockDebugLogicTest {

	// --- Order parsing ---

	@Test
	void orderParseIsCaseInsensitiveAndCoversAllTokens() {
		assertEquals(Order.ANCHOR_FIRST, Order.parse("anchorFirst"));
		assertEquals(Order.ANCHOR_FIRST, Order.parse("ANCHORFIRST"));
		assertEquals(Order.ANCHOR_LAST, Order.parse("anchorLast"));
		assertEquals(Order.REVERSE, Order.parse("reverse"));
		assertEquals(Order.AS_IS, Order.parse("asIs"));
		assertNull(Order.parse("nonsense"));
	}

	@Test
	void everyAdvertisedTokenParses() {
		for (String token : Order.tokens()) {
			assertEquals(token.toLowerCase(java.util.Locale.ROOT),
					Order.parse(token).name().replace("_", "").toLowerCase(java.util.Locale.ROOT));
		}
	}

	// --- orderMembers (use Integer positions; the anchor is the lowest = 1) ---

	private static final List<Integer> MEMBERS = List.of(3, 1, 2, 4); // unsorted on purpose
	private static final int ANCHOR = 1; // lowest = canonical holder

	@Test
	void asIsIsAscending() {
		assertEquals(List.of(1, 2, 3, 4),
				MultiblockDebugLogic.orderMembers(MEMBERS, ANCHOR, Comparator.naturalOrder(), Order.AS_IS));
	}

	@Test
	void reverseIsDescending() {
		assertEquals(List.of(4, 3, 2, 1),
				MultiblockDebugLogic.orderMembers(MEMBERS, ANCHOR, Comparator.naturalOrder(), Order.REVERSE));
	}

	@Test
	void anchorFirstPutsAnchorAtHead() {
		List<Integer> result = MultiblockDebugLogic.orderMembers(MEMBERS, ANCHOR, Comparator.naturalOrder(), Order.ANCHOR_FIRST);
		assertEquals(ANCHOR, result.get(0));
		assertEquals(List.of(1, 2, 3, 4), result);
	}

	@Test
	void anchorLastPutsAnchorAtTail() {
		// the old corruption order: the anchor (lowest member) reloads LAST.
		List<Integer> result = MultiblockDebugLogic.orderMembers(MEMBERS, ANCHOR, Comparator.naturalOrder(), Order.ANCHOR_LAST);
		assertEquals(ANCHOR, result.get(result.size() - 1));
		assertEquals(List.of(2, 3, 4, 1), result);
	}

	@Test
	void orderingDoesNotMutateInput() {
		List<Integer> input = new java.util.ArrayList<>(MEMBERS);
		MultiblockDebugLogic.orderMembers(input, ANCHOR, Comparator.naturalOrder(), Order.REVERSE);
		assertEquals(List.of(3, 1, 2, 4), input);
	}

	@Test
	void anchorLastWithAnchorNotLowestStillMovesItToTail() {
		// even if the anchor is not the lowest, anchorLast must place it last and keep the rest ascending.
		List<Integer> result = MultiblockDebugLogic.orderMembers(MEMBERS, 3, Comparator.naturalOrder(), Order.ANCHOR_LAST);
		assertEquals(List.of(1, 2, 4, 3), result);
	}

	// --- Fingerprint diff + single-holder ---

	private static Fingerprint fp(boolean assembled, int members, String holder, int hash, int items, int carriers) {
		return new Fingerprint(assembled, members, holder, hash, items, carriers);
	}

	@Test
	void identicalFingerprintsHaveEmptyDiff() {
		Fingerprint a = fp(true, 27, "0,64,0", 0xABCD, 5, 1);
		assertTrue(MultiblockDebugLogic.diff(a, a).isEmpty());
	}

	@Test
	void diffReportsEveryChangedField() {
		Fingerprint before = fp(true, 27, "0,64,0", 0xABCD, 5, 1);
		Fingerprint after = fp(false, 26, "1,64,0", 0x1234, 0, 2);
		List<String> diff = MultiblockDebugLogic.diff(before, after);
		assertEquals(6, diff.size());
		assertTrue(diff.stream().anyMatch(s -> s.startsWith("assembled:")));
		assertTrue(diff.stream().anyMatch(s -> s.startsWith("memberCount:")));
		assertTrue(diff.stream().anyMatch(s -> s.startsWith("holder:")));
		assertTrue(diff.stream().anyMatch(s -> s.contains("CONTENT CHANGED")));
		assertTrue(diff.stream().anyMatch(s -> s.startsWith("inventoryItems:")));
		assertTrue(diff.stream().anyMatch(s -> s.contains("single-holder invariant")));
	}

	@Test
	void singleHolderOkOnlyWhenExactlyOneCarrier() {
		assertTrue(fp(true, 27, "0,64,0", 1, 1, 1).singleHolderOk());
		assertFalse(fp(true, 27, "0,64,0", 1, 1, 0).singleHolderOk()); // payload lost
		assertFalse(fp(true, 27, "0,64,0", 1, 1, 2).singleHolderOk()); // payload duplicated (old corruption)
	}

	@Test
	void payloadHashChangeOnlyDiff() {
		// the most important corruption signature: same geometry, different content.
		Fingerprint before = fp(true, 27, "0,64,0", 0xAAAA, 5, 1);
		Fingerprint after = fp(true, 27, "0,64,0", 0xBBBB, 5, 1);
		List<String> diff = MultiblockDebugLogic.diff(before, after);
		assertEquals(1, diff.size());
		assertTrue(diff.get(0).contains("CONTENT CHANGED"));
	}
}

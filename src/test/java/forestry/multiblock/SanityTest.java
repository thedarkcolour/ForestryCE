package forestry.multiblock;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Sanity check that the MC-free JUnit 5 source set runs without the Minecraft classpath.
 * Imports nothing from {@code net.minecraft}.
 */
class SanityTest {
	@Test
	void onePlusOneIsTwo() {
		assertEquals(2, 1 + 1);
	}
}

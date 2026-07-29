package forestry.gametest;

import forestry.api.ForestryConstants;
import forestry.core.multiblock.MultiblockController;
import forestry.core.multiblock.MultiblockIndex;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.LevelAccessor;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/** Regression coverage for concurrent integrated-client/server access to {@link MultiblockIndex}. */
@GameTestHolder(ForestryConstants.MOD_ID)
@PrefixGameTestTemplate(false)
public final class MultiblockIndexConcurrencyTest {
	private static final int WORKERS = 16;
	private static final int ITERATIONS = 1_000;

	private MultiblockIndexConcurrencyTest() {
	}

	/**
	 * The integrated server and client load distinct {@link LevelAccessor} instances on different threads.
	 * The outer level index must therefore support concurrent {@code computeIfAbsent} calls.
	 *
	 * <p>This test is deliberately deterministic: it first asserts the concurrency contract of the outer map,
	 * then exercises that map from several threads. The old plain {@code HashMap} implementation fails the
	 * first assertion; the {@code ConcurrentHashMap} implementation passes and completes the stress phase.</p>
	 */
	@GameTest(template = "empty", timeoutTicks = 200)
	public static void outerLevelIndexSupportsConcurrentClientAndServerLoading(GameTestHelper helper) throws Exception {
		verifyConcurrentOuterLevelIndex();
		helper.succeed();
	}

	/** Package-private so the regression contract can also be exercised by a lightweight build harness. */
	static void verifyConcurrentOuterLevelIndex() throws Exception {
		Map<LevelAccessor, Map<BlockPos, MultiblockController>> levels = outerLevelIndex();
		if (!(levels instanceof ConcurrentMap)) {
			throw new AssertionError(
				"MultiblockIndex outer level map must be concurrent because integrated client and server load in parallel");
		}

		List<LevelAccessor> testLevels = new ArrayList<>(WORKERS);
		for (int i = 0; i < WORKERS; i++) {
			testLevels.add(testLevel("multiblock-index-concurrency-" + i));
		}

		ExecutorService executor = Executors.newFixedThreadPool(WORKERS);
		CountDownLatch ready = new CountDownLatch(WORKERS);
		CountDownLatch start = new CountDownLatch(1);
		List<Future<?>> futures = new ArrayList<>(WORKERS);
		try {
			for (LevelAccessor level : testLevels) {
				futures.add(executor.submit(() -> {
					ready.countDown();
					if (!start.await(5, TimeUnit.SECONDS)) {
						throw new AssertionError("Timed out waiting for concurrent MultiblockIndex test start");
					}
					for (int iteration = 0; iteration < ITERATIONS; iteration++) {
						levels.computeIfAbsent(level, ignored -> new HashMap<>());
					}
					return null;
				}));
			}

			if (!ready.await(5, TimeUnit.SECONDS)) {
				throw new AssertionError("Worker threads did not become ready for the MultiblockIndex concurrency test");
			}
			start.countDown();
			for (Future<?> future : futures) {
				future.get(10, TimeUnit.SECONDS);
			}
		} finally {
			start.countDown();
			executor.shutdownNow();
			for (LevelAccessor level : testLevels) {
				levels.remove(level);
			}
		}

	}

	@SuppressWarnings("unchecked")
	private static Map<LevelAccessor, Map<BlockPos, MultiblockController>> outerLevelIndex() throws Exception {
		Field field = MultiblockIndex.class.getDeclaredField("LEVELS");
		field.setAccessible(true);
		return (Map<LevelAccessor, Map<BlockPos, MultiblockController>>) field.get(null);
	}

	private static LevelAccessor testLevel(String name) {
		return (LevelAccessor) Proxy.newProxyInstance(
			MultiblockIndexConcurrencyTest.class.getClassLoader(),
			new Class<?>[]{LevelAccessor.class},
			(proxy, method, args) -> switch (method.getName()) {
				case "hashCode" -> System.identityHashCode(proxy);
				case "equals" -> proxy == args[0];
				case "toString" -> name;
				default -> defaultValue(method.getReturnType());
			});
	}

	private static Object defaultValue(Class<?> type) {
		if (!type.isPrimitive()) return null;
		if (type == boolean.class) return false;
		if (type == char.class) return '\0';
		if (type == byte.class) return (byte) 0;
		if (type == short.class) return (short) 0;
		if (type == int.class) return 0;
		if (type == long.class) return 0L;
		if (type == float.class) return 0.0F;
		if (type == double.class) return 0.0D;
		throw new IllegalArgumentException("Unsupported primitive type: " + type);
	}
}

package forestry.gametest;

import forestry.api.ForestryConstants;
import forestry.api.core.multiblock.IMultiblockComponent;
import forestry.api.core.multiblock.IMultiblockController;
import forestry.api.core.multiblock.IMultiblockInventoryProbe;
import forestry.core.platform.multiblock.MultiblockIndex;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

// multiblock behavior across real chunk unloads and loads, built at isolated sites (see ChunkLifecycle)
@GameTestHolder(ForestryConstants.MOD_ID)
@PrefixGameTestTemplate(false)
public final class MultiblockChunkTest {
	// the real limit is the wall-clock deadline in ChunkLifecycle.Steps.await, ticks run far faster than 20 per second
	private static final int TIMEOUT = 2_000_000;
	private static final int ALVEARY_PRODUCT_SLOT = 2;

	private MultiblockChunkTest() {
	}

	@FunctionalInterface
	private interface Builder {
		List<BlockPos> build(GameTestHelper helper, BlockPos base);
	}

	private static final class Run {
		List<BlockPos> members = List.of();
		List<BlockEntity> blockEntities = List.of();
		@Nullable
		IMultiblockController controller;
		Map<Item, Integer> before = Map.of();
	}

	@GameTest(template = "empty", timeoutTicks = TIMEOUT)
	public static void alvearyReloadsFromSavedChunk(GameTestHelper helper) {
		reloadsFromSavedChunk(helper, MultiblockTestSupport::buildAlveary, MultiblockTestSupport.ALVEARY_INV_SIZE, ALVEARY_PRODUCT_SLOT);
	}

	@GameTest(template = "empty", timeoutTicks = TIMEOUT)
	public static void farmReloadsFromSavedChunk(GameTestHelper helper) {
		reloadsFromSavedChunk(helper, MultiblockTestSupport::buildFarm, MultiblockTestSupport.FARM_INV_SIZE, 0);
	}

	// the in-memory controller must not outlive its chunk, or a reload never reads what was saved
	private static void reloadsFromSavedChunk(GameTestHelper helper, Builder builder, int invSize, int slot) {
		ServerLevel level = helper.getLevel();
		ChunkPos site = ChunkLifecycle.isolatedSite(helper);
		BlockPos base = new BlockPos(site.getMinBlockX() + 4, groundY(helper), site.getMinBlockZ() + 4);
		Run run = new Run();

		ChunkLifecycle.steps(helper)
			.run(() -> ChunkLifecycle.hold(level, site, 1))
			.await(() -> ChunkLifecycle.isFull(level, site), "site did not load")
			.run(() -> run.members = builder.build(helper, ChunkLifecycle.relative(helper, base)))
			.await(() -> isAssembled(level, run.members.getFirst()), "structure did not assemble")
			.run(() -> {
				MultiblockTestSupport.insertItem(level, run.members, invSize, slot, new ItemStack(Items.HONEYCOMB, 7));
				run.before = tally(level, run.members.getFirst());
				run.controller = controllerIfLoaded(level, run.members.getFirst());
				run.blockEntities = ChunkLifecycle.blockEntities(level, run.members);
				ChunkLifecycle.release(level, site, 1);
			})
			.await(() -> ChunkLifecycle.isUnloaded(run.blockEntities), "site did not unload")
			.run(() -> {
				helper.assertTrue(MultiblockIndex.get(level, run.members.getFirst()) == null,
					"controller stayed indexed after every member unloaded");
				ChunkLifecycle.hold(level, site, 1);
			})
			.await(() -> isAssembled(level, run.members.getFirst()), "structure did not reassemble after reload")
			.run(() -> {
				helper.assertTrue(controllerIfLoaded(level, run.members.getFirst()) != run.controller,
					"reload reused the in-memory controller instead of reading the saved chunk");
				Map<Item, Integer> after = tally(level, run.members.getFirst());
				helper.assertTrue(after.equals(run.before), "inventory changed across unload: before=" + run.before + " after=" + after);
				ChunkLifecycle.release(level, site, 1);
			})
			.succeed();
	}

	// an idle alveary never dirties its chunk from serverTick, so only the change itself can get it saved
	@GameTest(template = "empty", timeoutTicks = TIMEOUT)
	public static void alvearyAutomationChangeSurvivesChunkUnload(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ChunkPos site = ChunkLifecycle.isolatedSite(helper);
		BlockPos base = new BlockPos(site.getMinBlockX() + 4, groundY(helper), site.getMinBlockZ() + 4);
		Run run = new Run();

		ChunkLifecycle.steps(helper)
			.run(() -> ChunkLifecycle.hold(level, site, 1))
			.await(() -> ChunkLifecycle.isFull(level, site), "site did not load")
			.run(() -> run.members = MultiblockTestSupport.buildAlveary(helper, ChunkLifecycle.relative(helper, base)))
			.await(() -> isAssembled(level, run.members.getFirst()), "alveary did not assemble")
			.run(() -> MultiblockTestSupport.insertItem(level, run.members, MultiblockTestSupport.ALVEARY_INV_SIZE, ALVEARY_PRODUCT_SLOT, new ItemStack(Items.HONEYCOMB, 7)))
			// a chunk is not ready to save while its neighbors are still generating
			.await(() -> {
				level.getChunkSource().save(false);
				return !level.getChunkAt(run.members.getFirst()).isUnsaved();
			}, "site chunk was never saved before the change")
			.run(() -> {
				IItemHandler handler = level.getCapability(Capabilities.ItemHandler.BLOCK, run.members.getLast(), null);
				helper.assertTrue(handler != null, "alveary exposes no item handler");
				helper.assertTrue(handler.extractItem(ALVEARY_PRODUCT_SLOT, 1, false).getCount() == 1, "extraction through the item handler failed");
				run.blockEntities = ChunkLifecycle.blockEntities(level, run.members);
				ChunkLifecycle.release(level, site, 1);
			})
			.await(() -> ChunkLifecycle.isUnloaded(run.blockEntities), "site did not unload")
			.run(() -> ChunkLifecycle.hold(level, site, 1))
			.await(() -> isAssembled(level, run.members.getFirst()), "alveary did not reassemble after reload")
			.run(() -> {
				int honeycomb = tally(level, run.members.getFirst()).getOrDefault(Items.HONEYCOMB, 0);
				helper.assertTrue(honeycomb == 6, "expected the extraction to be saved (6 honeycomb), found " + honeycomb);
				ChunkLifecycle.release(level, site, 1);
			})
			.succeed();
	}

	// every member sits in one chunk but the air ring and upper face reach into the next, which holds no member to
	// trigger validation when it loads
	@GameTest(template = "empty", timeoutTicks = TIMEOUT)
	public static void alvearyFormsWhenShellChunkLoadsLater(GameTestHelper helper) {
		formsAfterPartialLoad(helper, 13, true, true);
	}

	@GameTest(template = "empty", timeoutTicks = TIMEOUT)
	public static void alvearyAcrossChunksFormsWhenOtherChunkLoads(GameTestHelper helper) {
		formsAfterPartialLoad(helper, 14, false, true);
	}

	// the order where the chunk that saved the payload arrives last
	@GameTest(template = "empty", timeoutTicks = TIMEOUT)
	public static void alvearyAcrossChunksFormsWhenHolderChunkLoadsLast(GameTestHelper helper) {
		formsAfterPartialLoad(helper, 14, false, false);
	}

	// chunk a holds the min corner and so the holder, and chunk b is the next chunk in +x. Loading one chunk alone
	// leaves the other just short of fully loaded
	private static void formsAfterPartialLoad(GameTestHelper helper, int column, boolean oneChunk, boolean holderFirst) {
		ServerLevel level = helper.getLevel();
		ChunkPos a = ChunkLifecycle.isolatedSite(helper);
		ChunkPos b = new ChunkPos(a.x + 1, a.z);
		ChunkPos first = holderFirst ? a : b;
		ChunkPos second = holderFirst ? b : a;
		BlockPos base = new BlockPos(a.getMinBlockX() + column, groundY(helper), a.getMinBlockZ() + 4);
		Run run = new Run();

		ChunkLifecycle.steps(helper)
			.run(() -> ChunkLifecycle.hold(level, a, 1))
			.await(() -> ChunkLifecycle.isFull(level, a) && ChunkLifecycle.isFull(level, b), "site did not load")
			.run(() -> run.members = MultiblockTestSupport.buildAlveary(helper, ChunkLifecycle.relative(helper, base)))
			.await(() -> isAssembled(level, run.members.getFirst()), "alveary did not assemble")
			.run(() -> {
				boolean inA = run.members.stream().allMatch(pos -> new ChunkPos(pos).equals(a));
				helper.assertTrue(inA == oneChunk, oneChunk ? "alveary is not inside one chunk" : "alveary does not cross into the second chunk");
				MultiblockTestSupport.insertItem(level, run.members, MultiblockTestSupport.ALVEARY_INV_SIZE, ALVEARY_PRODUCT_SLOT, new ItemStack(Items.HONEYCOMB, 7));
				run.before = tally(level, run.members.getFirst());
				run.blockEntities = ChunkLifecycle.blockEntities(level, run.members);
				ChunkLifecycle.release(level, a, 1);
			})
			.await(() -> ChunkLifecycle.isUnloaded(run.blockEntities), "site did not unload")
			.run(() -> ChunkLifecycle.hold(level, first, 0))
			.await(() -> ChunkLifecycle.isFull(level, first), "first chunk did not load")
			// onLoad runs on the tick after the chunk becomes fully loaded
			.idle(5)
			.run(() -> {
				helper.assertFalse(ChunkLifecycle.isFull(level, second), "second chunk loaded too early, the partial case was not exercised");
				helper.assertFalse(isAssembled(level, run.members.getFirst()), "alveary assembled before its second chunk loaded");
				ChunkLifecycle.hold(level, second, 0);
			})
			.await(() -> isAssembled(level, run.members.getFirst()), "alveary never formed after its second chunk loaded")
			.run(() -> {
				Map<Item, Integer> after = tally(level, run.members.getFirst());
				helper.assertTrue(after.equals(run.before), "inventory changed across the partial load: before=" + run.before + " after=" + after);
				ChunkLifecycle.release(level, a, 0);
				ChunkLifecycle.release(level, b, 0);
			})
			.succeed();
	}

	private static int groundY(GameTestHelper helper) {
		return helper.absolutePos(new BlockPos(0, 1, 0)).getY();
	}

	@Nullable
	private static IMultiblockController controllerIfLoaded(ServerLevel level, BlockPos pos) {
		return ChunkLifecycle.blockEntityIfLoaded(level, pos) instanceof IMultiblockComponent component
			? component.getMultiblockLogic().getController()
			: null;
	}

	private static boolean isAssembled(ServerLevel level, BlockPos pos) {
		IMultiblockController controller = controllerIfLoaded(level, pos);
		return controller != null && controller.isAssembled();
	}

	private static Map<Item, Integer> tally(ServerLevel level, BlockPos pos) {
		IMultiblockController controller = controllerIfLoaded(level, pos);
		return controller instanceof IMultiblockInventoryProbe probe
			? MultiblockTestSupport.tally(probe.snapshotSharedInventory())
			: Map.of();
	}
}

package forestry.gametest;

import forestry.api.ForestryConstants;
import forestry.api.apiculture.genetics.BeeLifeStage;
import forestry.api.apiculture.genetics.IBee;
import forestry.api.core.genetics.capability.IIndividualHandlerItem;
import forestry.apiculture.bees.BeeHousingInventory;
import forestry.core.platform.multiblock.MultiblockController;
import forestry.core.platform.util.SpeciesUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.ArrayList;
import java.util.List;

// an idle alveary never reports a change from serverTick, so a shared inventory change only survives a save if the
// change itself marks the holder's chunk unsaved
@GameTestHolder(ForestryConstants.MOD_ID)
@PrefixGameTestTemplate(false)
public final class MultiblockPersistenceTest {
	private static final BlockPos BASE = new BlockPos(6, 1, 6);
	private static final int PRODUCT_SLOT = 2;

	private MultiblockPersistenceTest() {
	}

	@GameTest(template = "empty")
	public static void alvearyAutomationChangeMarksHolderUnsaved(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		List<BlockPos> members = MultiblockTestSupport.buildAlveary(helper, BASE);

		helper.runAfterDelay(5, () -> {
			BlockPos holder = holderOf(helper, members);
			MultiblockTestSupport.insertItem(level, members, MultiblockTestSupport.ALVEARY_INV_SIZE, PRODUCT_SLOT, new ItemStack(Items.HONEYCOMB, 7));
			LevelChunk holderChunk = level.getChunkAt(holder);
			holderChunk.setUnsaved(false);

			IItemHandler handler = level.getCapability(Capabilities.ItemHandler.BLOCK, members.getLast(), null);
			helper.assertTrue(handler != null, "alveary exposes no item handler");
			ItemStack extracted = handler.extractItem(PRODUCT_SLOT, 1, false);
			helper.assertTrue(extracted.getCount() == 1, "extraction through the item handler failed");

			helper.assertTrue(holderChunk.isUnsaved(), "extraction through the item handler did not mark the holder's chunk unsaved");
			helper.succeed();
		});
	}

	@GameTest(template = "empty")
	public static void alvearyChangeFromOtherChunkMarksHolderUnsaved(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		// put the min corner in the last column of a chunk so the rest of the alveary sits in the next chunk
		int column = Math.floorMod(helper.absolutePos(BlockPos.ZERO).getX(), 16);
		int x = Math.floorMod(15 - column, 16);
		if (x > 12) {
			x--;
		}
		List<BlockPos> members = MultiblockTestSupport.buildAlveary(helper, new BlockPos(x, 1, 6));

		helper.runAfterDelay(5, () -> {
			BlockPos holder = holderOf(helper, members);
			ChunkPos holderChunkPos = new ChunkPos(holder);
			BlockPos remote = members.stream()
				.filter(pos -> !new ChunkPos(pos).equals(holderChunkPos))
				.findFirst()
				.orElse(null);
			helper.assertTrue(remote != null, "alveary does not straddle a chunk border");

			LevelChunk holderChunk = level.getChunkAt(holder);
			holderChunk.setUnsaved(false);
			level.getChunkAt(remote).setUnsaved(false);

			// the same calls a menu slot makes when a player places an item
			Container container = (Container) level.getBlockEntity(remote);
			container.setItem(PRODUCT_SLOT, new ItemStack(Items.HONEYCOMB, 7));
			container.setChanged();

			helper.assertTrue(holderChunk.isUnsaved(), "a change through a member in another chunk did not mark the holder's chunk unsaved");
			helper.succeed();
		});
	}

	// a queen's death leaves the alveary idle, so serverTick never reports the dead queen or her offspring. Machines
	// in the grid mark their chunks every tick, so this check spans ticks on a chunk to itself
	@GameTest(template = "empty", timeoutTicks = 2_000_000)
	public static void alvearyQueenDeathMarksHolderUnsaved(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ChunkPos site = ChunkLifecycle.isolatedSite(helper);
		List<BlockPos> members = new ArrayList<>();
		LevelChunk[] holderChunk = new LevelChunk[1];
		boolean[] unsaved = new boolean[1];

		ChunkLifecycle.steps(helper)
			.run(() -> ChunkLifecycle.hold(level, site, 1))
			.await(() -> ChunkLifecycle.isTicking(level, site), "site did not start ticking")
			.run(() -> members.addAll(MultiblockTestSupport.buildAlveary(helper, ChunkLifecycle.siteBlock(helper, site, 4, 4))))
			.await(() -> MultiblockTestSupport.isAssembled(level, members.getFirst()), "alveary did not assemble")
			// placing a block marks its chunk once more on the following tick
			.idle(10)
			.run(() -> {
				holderChunk[0] = level.getChunkAt(holderOf(helper, members));
				IBee queen = SpeciesUtil.BEE_TYPE.get().getDefaultSpecies().createIndividual();
				queen.setMate(queen.getGenome());
				queen.setHealth(0);
				MultiblockTestSupport.insertItem(level, members, MultiblockTestSupport.ALVEARY_INV_SIZE, BeeHousingInventory.SLOT_QUEEN, queen.createStack(BeeLifeStage.QUEEN));
				holderChunk[0].setUnsaved(false);
			})
			// the queen dies on the next tick and her offspring move into the inventory on the tick after. The next
			// tick's eager save can write a dirty chunk and clear its flag, so check it after every tick
			.everyTick(5, () -> unsaved[0] |= holderChunk[0].isUnsaved())
			.run(() -> {
				Container container = (Container) level.getBlockEntity(members.getFirst());
				helper.assertTrue(container.getItem(BeeHousingInventory.SLOT_QUEEN).isEmpty(), "the dead queen was not removed");
				boolean hasPrincess = false;
				for (int slot = BeeHousingInventory.SLOT_PRODUCT_1; slot < BeeHousingInventory.SLOT_PRODUCT_1 + BeeHousingInventory.SLOT_PRODUCT_COUNT; slot++) {
					hasPrincess |= IIndividualHandlerItem.getLifeStage(container.getItem(slot)) == BeeLifeStage.PRINCESS;
				}
				helper.assertTrue(hasPrincess, "the dead queen left no princess");
				helper.assertTrue(unsaved[0], "the queen's death did not mark the holder's chunk unsaved");
				ChunkLifecycle.release(level, site, 1);
			})
			.succeed();
	}

	private static BlockPos holderOf(GameTestHelper helper, List<BlockPos> members) {
		helper.assertTrue(MultiblockTestSupport.isAssembled(helper.getLevel(), members.getFirst()), "structure failed to assemble");
		BlockPos holder = ((MultiblockController) MultiblockTestSupport.controllerAt(helper.getLevel(), members.getFirst())).getHolderPos();
		helper.assertTrue(holder != null, "assembled controller has no holder");
		return holder;
	}
}

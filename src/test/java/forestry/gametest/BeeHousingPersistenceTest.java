package forestry.gametest;

import forestry.api.ForestryConstants;
import forestry.api.apiculture.genetics.BeeLifeStage;
import forestry.api.apiculture.genetics.IBee;
import forestry.api.core.genetics.capability.IIndividualHandlerItem;
import forestry.apiculture.apiary.ApicultureBlockType;
import forestry.apiculture.bees.BeeHousingInventory;
import forestry.apiculture.features.ApicultureBlocks;
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

import java.util.function.Consumer;

// apiary and bee house changes only survive a save if something marks the housing's chunk unsaved
@GameTestHolder(ForestryConstants.MOD_ID)
@PrefixGameTestTemplate(false)
public final class BeeHousingPersistenceTest {
	// the real limit is the wall-clock deadline in ChunkLifecycle.Steps.await
	private static final int TIMEOUT = 2_000_000;
	private static final BlockPos HOUSING = new BlockPos(2, 1, 2);

	private BeeHousingPersistenceTest() {
	}

	// nothing else runs between clearing the flag and checking it, so this one can share the grid's chunks
	@GameTest(template = "empty")
	public static void apiaryAutomationChangeMarksChunkUnsaved(GameTestHelper helper) {
		helper.setBlock(HOUSING, ApicultureBlocks.BASE.get(ApicultureBlockType.APIARY).defaultState());
		container(helper, HOUSING).setItem(BeeHousingInventory.SLOT_PRODUCT_1, new ItemStack(Items.HONEYCOMB, 7));
		LevelChunk chunk = helper.getLevel().getChunkAt(helper.absolutePos(HOUSING));
		chunk.setUnsaved(false);

		IItemHandler handler = helper.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, helper.absolutePos(HOUSING), null);
		helper.assertTrue(handler != null, "apiary exposes no item handler");
		helper.assertTrue(handler.extractItem(BeeHousingInventory.SLOT_PRODUCT_1, 1, false).getCount() == 1, "extraction through the item handler failed");

		helper.assertTrue(chunk.isUnsaved(), "extraction through the item handler did not mark the apiary's chunk unsaved");
		helper.succeed();
	}

	@GameTest(template = "empty", timeoutTicks = TIMEOUT)
	public static void apiaryQueenDeathMarksChunkUnsaved(GameTestHelper helper) {
		queenDeathMarksChunkUnsaved(helper, ApicultureBlockType.APIARY);
	}

	@GameTest(template = "empty", timeoutTicks = TIMEOUT)
	public static void beeHouseQueenDeathMarksChunkUnsaved(GameTestHelper helper) {
		queenDeathMarksChunkUnsaved(helper, ApicultureBlockType.BEE_HOUSE);
	}

	// a dead queen and her offspring are written by the idle tick, so only the inventory writes can mark the chunk
	private static void queenDeathMarksChunkUnsaved(GameTestHelper helper, ApicultureBlockType type) {
		IBee queen = SpeciesUtil.BEE_TYPE.get().getDefaultSpecies().createIndividual();
		queen.setMate(queen.getGenome());
		queen.setHealth(0);

		// the queen dies on the next tick and her offspring move into the inventory on the tick after
		onIsolatedSite(helper, type,
			container -> container.setItem(BeeHousingInventory.SLOT_QUEEN, queen.createStack(BeeLifeStage.QUEEN)),
			(container, unsaved) -> {
				helper.assertTrue(container.getItem(BeeHousingInventory.SLOT_QUEEN).isEmpty(), "the dead queen was not removed");
				helper.assertTrue(hasProduct(container, BeeLifeStage.PRINCESS), "the dead queen left no princess");
				helper.assertTrue(unsaved, "the queen's death did not mark the " + type.getSerializedName() + "'s chunk unsaved");
			});
	}

	// breeding changes only the progress counter until it finishes, so the tick itself has to mark the chunk
	@GameTest(template = "empty", timeoutTicks = TIMEOUT)
	public static void apiaryBreedingMarksChunkUnsaved(GameTestHelper helper) {
		onIsolatedSite(helper, ApicultureBlockType.APIARY,
			container -> {
				container.setItem(BeeHousingInventory.SLOT_QUEEN, SpeciesUtil.BEE_TYPE.get().getDefaultSpecies().createStack(BeeLifeStage.PRINCESS));
				container.setItem(BeeHousingInventory.SLOT_DRONE, SpeciesUtil.BEE_TYPE.get().getDefaultSpecies().createStack(BeeLifeStage.DRONE));
			},
			(container, unsaved) -> {
				helper.assertTrue(IIndividualHandlerItem.getLifeStage(container.getItem(BeeHousingInventory.SLOT_QUEEN)) == BeeLifeStage.PRINCESS,
					"breeding finished early, the progress-only case was not exercised");
				helper.assertTrue(unsaved, "breeding did not mark the apiary's chunk unsaved");
			});
	}

	@FunctionalInterface
	private interface Check {
		void check(Container container, boolean unsaved);
	}

	// machines in the grid mark their chunks every tick, so a check spanning ticks needs a chunk to itself
	private static void onIsolatedSite(GameTestHelper helper, ApicultureBlockType type, Consumer<Container> setup, Check check) {
		ServerLevel level = helper.getLevel();
		ChunkPos site = ChunkLifecycle.isolatedSite(helper);
		BlockPos housing = ChunkLifecycle.siteBlock(helper, site, 8, 8);
		LevelChunk[] chunk = new LevelChunk[1];
		boolean[] unsaved = new boolean[1];

		ChunkLifecycle.steps(helper)
			.run(() -> ChunkLifecycle.hold(level, site, 1))
			.await(() -> ChunkLifecycle.isTicking(level, site), "site did not start ticking")
			.run(() -> helper.setBlock(housing, ApicultureBlocks.BASE.get(type).defaultState()))
			// placing a block marks its chunk once more on the following tick
			.idle(10)
			.run(() -> {
				setup.accept(container(helper, housing));
				chunk[0] = level.getChunkAt(helper.absolutePos(housing));
				chunk[0].setUnsaved(false);
			})
			// the next tick's eager save can write a dirty chunk and clear its flag, so check it after every tick
			.everyTick(5, () -> unsaved[0] |= chunk[0].isUnsaved())
			.run(() -> {
				check.check(container(helper, housing), unsaved[0]);
				ChunkLifecycle.release(level, site, 1);
			})
			.succeed();
	}

	private static Container container(GameTestHelper helper, BlockPos pos) {
		return (Container) helper.getBlockEntity(pos);
	}

	private static boolean hasProduct(Container container, BeeLifeStage stage) {
		for (int slot = BeeHousingInventory.SLOT_PRODUCT_1; slot < BeeHousingInventory.SLOT_PRODUCT_1 + BeeHousingInventory.SLOT_PRODUCT_COUNT; slot++) {
			if (IIndividualHandlerItem.getLifeStage(container.getItem(slot)) == stage) {
				return true;
			}
		}
		return false;
	}
}

package forestry.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import forestry.api.ForestryConstants;
import forestry.core.content.sorting.blocks.GeneticFilterBlock;
import forestry.core.content.sorting.features.SortingBlocks;
import forestry.core.content.sorting.features.SortingBlockEntities;
import forestry.core.content.sorting.tiles.GeneticFilterBlockEntity;

/**
 * Regression suite for the Genetic Filter never forwarding items.
 * <p>
 * {@code TileGeneticFilter.serverTick} holds the only code that moves a routed item into the adjacent inventory, but
 * {@code TileForestry.serverTick} is not called automatically. It needs a {@link net.minecraft.world.level.block.entity.BlockEntityTicker},
 * and {@link GeneticFilterBlock} never supplied one, so the filter accepted items and kept them forever. This was
 * broken from the 1.19 port, where Forge dropped automatic block entity ticking for the explicit ticker model.
 * <p>
 * The ticker must be an {@code IForestryTicker}. A bare lambda runs {@code serverTick} but skips the {@code TickHelper}
 * advance, which leaves {@code updateOnInterval} reading a frozen tick count that depends only on the block position.
 * {@link #geneticFilterForwardsItemsToAdjacentInventory} covers that: it asserts the item ARRIVES rather than only that
 * the source slot empties, because a failed transfer also empties the slot by dropping the item on the floor.
 */
@GameTestHolder(ForestryConstants.MOD_ID)
@PrefixGameTestTemplate(false)
public class GeneticFilterTickerTest {
	private static final BlockPos FILTER_POS = new BlockPos(8, 2, 8);
	private static final Direction TARGET = Direction.SOUTH;
	private static final BlockPos CHEST_POS = FILTER_POS.relative(TARGET);

	private static final Item TRANSFERRED_ITEM = Items.HONEYCOMB;
	private static final int TRANSFERRED_COUNT = 3;
	// TileGeneticFilter transfers on a 5 tick interval. Idle well past it rather than couple to the constant.
	private static final int IDLE_TICKS = 20;

	@GameTest(template = "empty")
	public static void geneticFilterSuppliesAServerTicker(GameTestHelper helper) {
		helper.setBlock(FILTER_POS, SortingBlocks.FILTER.defaultState());

		BlockState state = helper.getBlockState(FILTER_POS);
		GeneticFilterBlock block = (GeneticFilterBlock) state.getBlock();

		helper.assertTrue(block.getTicker(helper.getLevel(), state, SortingBlockEntities.GENETIC_FILTER.tileType()) != null,
			"BlockGeneticFilter supplied no ticker, so TileGeneticFilter.serverTick can never run");
		helper.assertTrue(block.getTicker(helper.getLevel(), state, BlockEntityType.CHEST) == null,
			"BlockGeneticFilter supplied a ticker for a foreign block entity type");
		helper.succeed();
	}

	@GameTest(template = "empty")
	public static void geneticFilterForwardsItemsToAdjacentInventory(GameTestHelper helper) {
		helper.setBlock(CHEST_POS, Blocks.CHEST);
		helper.setBlock(FILTER_POS, SortingBlocks.FILTER.defaultState());

		if (!(helper.getBlockEntity(FILTER_POS) instanceof GeneticFilterBlockEntity filter)) {
			helper.fail("Expected a TileGeneticFilter at " + FILTER_POS);
			return;
		}

		// write straight into the slot for the target side, which is where ItemHandlerFilter.insertItem leaves a
		// routed item. This keeps the test on the ticking path and off the filter rules.
		int slot = TARGET.get3DDataValue();
		filter.setItem(slot, new ItemStack(TRANSFERRED_ITEM, TRANSFERRED_COUNT));

		helper.startSequence()
			.thenIdle(IDLE_TICKS)
			.thenExecute(() -> {
				helper.assertTrue(countInChest(helper) == TRANSFERRED_COUNT,
					"expected " + TRANSFERRED_COUNT + " " + TRANSFERRED_ITEM + " in the adjacent chest, found "
						+ countInChest(helper) + ": the filter never forwarded them");
				helper.assertTrue(filter.getItem(slot).isEmpty(),
					"filter kept the items in its " + TARGET + " slot after forwarding them");
			})
			.thenSucceed();
	}

	private static int countInChest(GameTestHelper helper) {
		if (!(helper.getBlockEntity(CHEST_POS) instanceof Container chest)) {
			helper.fail("Expected a chest at " + CHEST_POS);
			return -1;
		}

		int found = 0;
		for (int i = 0; i < chest.getContainerSize(); i++) {
			ItemStack stack = chest.getItem(i);
			if (stack.is(TRANSFERRED_ITEM)) {
				found += stack.getCount();
			}
		}

		return found;
	}
}

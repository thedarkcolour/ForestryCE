package forestry.gametest;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

import forestry.api.multiblock.IMultiblockComponent;
import forestry.api.multiblock.IMultiblockController;
import forestry.api.multiblock.IMultiblockInventoryProbe;
import forestry.apiculture.blocks.BlockAlvearyType;
import forestry.apiculture.features.ApicultureBlocks;
import forestry.farming.blocks.EnumFarmBlockType;
import forestry.farming.blocks.EnumFarmMaterial;
import forestry.farming.features.FarmingBlocks;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Engine-agnostic mechanics shared by the multiblock inventory-conservation GameTests ({@link MultiblockGameTests}).
 *
 * <p>Everything here is built on vanilla / Forestry-public APIs so the SAME tests run against the current
 * ("Erogenous Beef") engine and the upcoming multiblock overhaul. The only engine-specific coupling is reading the
 * shared inventory, and that goes through the explicit {@link IMultiblockInventoryProbe} seam — never through engine
 * internals. Structures are built block-by-block (no shipped structure NBT beyond an empty arena), inventory is read
 * through the probe, drops are counted as real {@link ItemEntity}s, and a reload is a genuine block-entity teardown +
 * recreate whose reform is driven by the engine's own server tick (no registry calls).
 */
public final class MultiblockTestSupport {
	/** Slot count the alveary's shared bee inventory surfaces as a {@link Container} when assembled. */
	public static final int ALVEARY_INV_SIZE = 9;
	/** Slot count the farm's shared inventory surfaces as a {@link Container} when assembled. */
	public static final int FARM_INV_SIZE = 22;

	private MultiblockTestSupport() {
	}

	/* ===================== structure building (relative coords; returns absolute member positions) ===================== */

	/**
	 * Builds a minimal valid 3x3x3 alveary anchored at {@code base} (relative), capped with a 3x3 oak-slab layer.
	 * The surrounding GameTest arena is air, satisfying the "space around the entrances" rule.
	 *
	 * @return the absolute world positions of the 27 alveary block entities (sorted), for later teardown/inventory access.
	 */
	public static List<BlockPos> buildAlveary(GameTestHelper helper, BlockPos base) {
		BlockState plain = ApicultureBlocks.ALVEARY.get(BlockAlvearyType.PLAIN).defaultState();
		BlockState slab = Blocks.OAK_SLAB.defaultBlockState();
		List<BlockPos> members = new ArrayList<>();
		for (int x = 0; x < 3; x++) {
			for (int z = 0; z < 3; z++) {
				for (int y = 0; y < 3; y++) {
					BlockPos rel = base.offset(x, y, z);
					helper.setBlock(rel, plain);
					members.add(helper.absolutePos(rel));
				}
			}
		}
		for (int x = 0; x < 3; x++) {
			for (int z = 0; z < 3; z++) {
				helper.setBlock(base.offset(x, 3, z), slab); // slab cap at maxY + 1
			}
		}
		members.sort(BlockPos::compareTo);
		return members;
	}

	/**
	 * Builds a minimal valid 3x3x4 farm anchored at {@code base} (relative): all plain blocks with a single gearbox at
	 * the bottom centre (a level-0 exterior position, which the band/interior rules permit).
	 *
	 * @return the absolute world positions of the 36 farm block entities (sorted).
	 */
	public static List<BlockPos> buildFarm(GameTestHelper helper, BlockPos base) {
		BlockState plain = FarmingBlocks.FARM.get(EnumFarmBlockType.PLAIN, EnumFarmMaterial.STONE_BRICK).defaultState();
		BlockState gearbox = FarmingBlocks.FARM.get(EnumFarmBlockType.GEARBOX, EnumFarmMaterial.STONE_BRICK).defaultState();
		List<BlockPos> members = new ArrayList<>();
		for (int x = 0; x < 3; x++) {
			for (int z = 0; z < 3; z++) {
				for (int y = 0; y < 4; y++) {
					BlockPos rel = base.offset(x, y, z);
					// gearbox at bottom centre, plain everywhere else
					helper.setBlock(rel, (x == 1 && y == 0 && z == 1) ? gearbox : plain);
					members.add(helper.absolutePos(rel));
				}
			}
		}
		members.sort(BlockPos::compareTo);
		return members;
	}

	/* ===================== controller / inventory access (engine-agnostic) ===================== */

	/** Resolves the controller that owns the member at {@code memberAbs}, or null if that block is not a connected part. */
	public static IMultiblockController controllerAt(ServerLevel level, BlockPos memberAbs) {
		if (level.getBlockEntity(memberAbs) instanceof IMultiblockComponent component) {
			return component.getMultiblockLogic().getController();
		}
		return null;
	}

	public static boolean isAssembled(ServerLevel level, BlockPos memberAbs) {
		IMultiblockController controller = controllerAt(level, memberAbs);
		return controller != null && controller.isAssembled();
	}

	/**
	 * Reads the shared inventory through {@link IMultiblockInventoryProbe} — the one engine-agnostic seam. Returns an
	 * empty list if the member is unattached or its controller does not implement the probe.
	 */
	public static List<ItemStack> snapshot(ServerLevel level, BlockPos memberAbs) {
		IMultiblockController controller = controllerAt(level, memberAbs);
		if (controller instanceof IMultiblockInventoryProbe probe) {
			return probe.snapshotSharedInventory();
		}
		return List.of();
	}

	/** Item -> total count, for order-independent inventory comparison. */
	public static Map<Item, Integer> tally(List<ItemStack> stacks) {
		Map<Item, Integer> tally = new TreeMap<>((a, b) -> Integer.compare(Item.getId(a), Item.getId(b)));
		for (ItemStack stack : stacks) {
			tally.merge(stack.getItem(), stack.getCount(), Integer::sum);
		}
		return tally;
	}

	public static int total(Map<Item, Integer> tally) {
		int sum = 0;
		for (int count : tally.values()) {
			sum += count;
		}
		return sum;
	}

	/**
	 * Writes {@code stack} into one slot of the machine's shared inventory through the vanilla {@link Container} surface
	 * (every assembled member delegates to the single shared inventory), bypassing slot-acceptance rules — we only need
	 * something countable to track. Must be called AFTER assembly. Throws if no shared inventory of {@code invSize} is
	 * found among the members.
	 */
	public static void insertItem(ServerLevel level, List<BlockPos> membersAbs, int invSize, int slot, ItemStack stack) {
		for (BlockPos member : membersAbs) {
			if (level.getBlockEntity(member) instanceof Container container && container.getContainerSize() == invSize) {
				container.setItem(slot, stack.copy());
				container.setChanged();
				return;
			}
		}
		throw new IllegalStateException("no shared inventory of size " + invSize + " found among members (not assembled?)");
	}

	/* ===================== drops ===================== */

	/** A box around the whole structure, inflated to cover where dropped items scatter. */
	public static AABB dropBox(List<BlockPos> membersAbs) {
		int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
		int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
		for (BlockPos m : membersAbs) {
			minX = Math.min(minX, m.getX());
			minY = Math.min(minY, m.getY());
			minZ = Math.min(minZ, m.getZ());
			maxX = Math.max(maxX, m.getX());
			maxY = Math.max(maxY, m.getY());
			maxZ = Math.max(maxZ, m.getZ());
		}
		return new AABB(minX, minY, minZ, maxX + 1, maxY + 1, maxZ + 1).inflate(4.0);
	}

	public static Set<UUID> itemEntityIds(ServerLevel level, AABB box) {
		Set<UUID> ids = new HashSet<>();
		for (ItemEntity entity : level.getEntitiesOfClass(ItemEntity.class, box)) {
			ids.add(entity.getUUID());
		}
		return ids;
	}

	/** Total count of {@code item} dropped inside {@code box} since {@code before} was captured (new item entities only). */
	public static int newDropCount(ServerLevel level, AABB box, Set<UUID> before, Item item) {
		int count = 0;
		for (ItemEntity entity : level.getEntitiesOfClass(ItemEntity.class, box)) {
			if (!before.contains(entity.getUUID()) && entity.getItem().is(item)) {
				count += entity.getItem().getCount();
			}
		}
		return count;
	}

	/**
	 * Total count of {@code item} held across the shared inventories of every DISTINCT controller present among
	 * {@code members}. This measures conservation <em>wherever the inventory ends up</em> — whether the structures merged
	 * into one controller, stayed separate, or re-homed parts — so the merge tests stay correct on an engine that
	 * handles adjacency differently than the current one. Reads only through the {@link IMultiblockInventoryProbe} seam.
	 */
	public static int keptAcross(ServerLevel level, List<BlockPos> members, Item item) {
		Set<IMultiblockController> seen = java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<>());
		int count = 0;
		for (BlockPos member : members) {
			IMultiblockController controller = controllerAt(level, member);
			if (controller instanceof IMultiblockInventoryProbe probe && seen.add(controller)) {
				for (ItemStack stack : probe.snapshotSharedInventory()) {
					if (stack.is(item)) {
						count += stack.getCount();
					}
				}
			}
		}
		return count;
	}

	/* ===================== genuine block-entity reload (engine-agnostic teardown + recreate) ===================== */

	/**
	 * Tears every member block entity down for a genuine reload: pre-builds fresh block entities from each member's
	 * real saved NBT, then pauses ({@code onChunkUnloaded}) and removes the live block entities. Returns the fresh
	 * (not-yet-placed) block entities keyed by position, to be placed via {@link #placeAndRegister}. Makes NO
	 * registry/engine-internal calls, so it is faithful for either engine.
	 */
	public static Map<BlockPos, BlockEntity> teardown(ServerLevel level, List<BlockPos> membersAbs) {
		Map<BlockPos, BlockEntity> fresh = new LinkedHashMap<>();
		for (BlockPos member : membersAbs) {
			BlockEntity be = level.getBlockEntity(member);
			if (be == null) {
				continue;
			}
			BlockState state = level.getBlockState(member);
			BlockEntity recreated = BlockEntity.loadStatic(member, state, be.saveWithId());
			if (recreated != null) {
				fresh.put(member, recreated);
			}
		}
		for (BlockPos member : membersAbs) {
			BlockEntity be = level.getBlockEntity(member);
			if (be != null) {
				be.onChunkUnloaded();
			}
		}
		for (BlockPos member : membersAbs) {
			level.removeBlockEntity(member);
		}
		return fresh;
	}

	/**
	 * Places and registers the pre-built block entities at {@code positions} (a subset of a {@link #teardown} result).
	 * Sets all first so no member observes a hole, then registers each (fires {@code onLoad} -> the engine queues each
	 * as an orphan; the next server tick reforms/merges). Staging different subsets across ticks reproduces an
	 * adversarial partial-arrival order.
	 */
	public static void placeAndRegister(ServerLevel level, Map<BlockPos, BlockEntity> fresh, List<BlockPos> positions) {
		for (BlockPos pos : positions) {
			BlockEntity be = fresh.get(pos);
			if (be != null) {
				level.getChunkAt(pos).setBlockEntity(be);
			}
		}
		for (BlockPos pos : positions) {
			BlockEntity be = fresh.get(pos);
			if (be != null) {
				level.getChunkAt(pos).addAndRegisterBlockEntity(be);
			}
		}
	}

	/**
	 * Convenience: a single-tick full reload (teardown every member, then immediately recreate them all). The reform
	 * and any drop/merge are driven by the engine's own subsequent server tick. Wait a few ticks before measuring.
	 */
	public static void reloadInPlace(ServerLevel level, List<BlockPos> membersAbs) {
		Map<BlockPos, BlockEntity> fresh = teardown(level, membersAbs);
		placeAndRegister(level, fresh, new ArrayList<>(fresh.keySet()));
	}
}

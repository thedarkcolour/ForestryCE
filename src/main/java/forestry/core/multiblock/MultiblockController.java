package forestry.core.multiblock;

import com.mojang.authlib.GameProfile;
import forestry.api.IForestryApi;
import forestry.api.core.IErrorLogic;
import forestry.api.core.IErrorLogicSource;
import forestry.api.core.ILocationProvider;
import forestry.api.multiblock.IMultiblockComponent;
import forestry.api.multiblock.IMultiblockController;
import forestry.core.inventory.FakeInventoryAdapter;
import forestry.core.inventory.IInventoryAdapter;
import forestry.core.owner.IOwnedTile;
import forestry.core.owner.IOwnerHandler;
import forestry.core.owner.OwnerHandler;
import forestry.core.tiles.TileUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The new declarative-engine superclass for {@code AlvearyController} and {@code FarmController}
 * (spec §6, §7; plan Task 2.1). It replaces the deleted "Erogenous Beef" base chain
 * {@code RectangularMultiblockControllerBase → MultiblockControllerForestry → MultiblockControllerBase}
 * with the surface those two controllers actually consume, minus all the flood-fill / merge / pause
 * machinery (no {@code assimilate}, {@code checkForDisconnections}, {@code attachBlock}, {@code PAUSED}, …).
 *
 * <p><b>State model (spec §6.1).</b> Instead of incrementally attaching parts, the controller's member
 * set and bounding box are supplied wholesale by the stateless pattern validator (the world-side trigger
 * code in Task 2.5 calls {@link #setStructure}). Three deterministic positions, kept separate:
 * <ul>
 *   <li><b>reference coord</b> = lowest-{@code (x,y,z)} member, read straight off the validated member
 *       set; {@link #getReferenceCoord()} never returns {@code null}/{@code ZERO} once a structure is set.</li>
 *   <li><b>center / top-center</b> = bounding-box-derived from {@code min}/{@code max}.</li>
 *   <li><b>payload holder</b> = the member that hosts/ticks this controller and serializes its payload
 *       (= the lowest member in steady state); tracked here as {@link #getHolderPos()}.</li>
 * </ul>
 *
 * <p><b>Hosting/ticking (spec §7.1).</b> This object is hosted by, and ticked from, the holder
 * BlockEntity's {@code BlockEntityTicker} (wired in Task 2.4). The abstract {@link #serverTick(int)} /
 * {@link #clientTick(int)} bodies hold the per-machine game logic (relocated from the old controllers'
 * tick methods in Tasks 2.2/2.3); {@link #writePayload(CompoundTag)} / {@link #readPayload(CompoundTag)}
 * round-trip the shared payload through the holder's NBT; {@link #onAssembled()} / {@link #onBroken()}
 * fire on assembled↔deactivated transitions (per-part visual callbacks are re-fired by the trigger code).
 *
 * <p><b>Owner (spec §3.1 E1).</b> The owner handler is carried here (ported from
 * {@code MultiblockControllerForestry}); the concrete controllers vote the majority owner once at first
 * formation and persist it in the payload — they do <em>not</em> re-vote on reload. This base only
 * supplies storage + accessors and the inventory plumbing; it deliberately does <em>not</em> re-implement
 * the old per-tick majority vote.
 */
public abstract class MultiblockController implements IMultiblockController, WorldlyContainer, IOwnedTile, IErrorLogicSource, ILocationProvider {
	protected final Level level;

	private final OwnerHandler ownerHandler;
	private final IErrorLogic errorLogic;

	/**
	 * The last validation error key (spec §11), set by the trigger code on a failed validation so
	 * {@link #getLastValidationError()} can surface it in chat. {@code null} when assembled or never tried.
	 */
	@Nullable
	private String lastValidationError;

	/**
	 * E1 owner-vote-once latch (spec §3.1): true once the majority owner has been resolved at first
	 * formation (or adopted from a legacy payload), so reloads never re-vote.
	 */
	private boolean ownerResolved = false;

	/**
	 * The validated member set (lowest-first), the bounding box, and the holder position. These are set
	 * wholesale by {@link #setStructure} on every (re)assembly and cleared on deactivation — never built
	 * incrementally and never cached across reloads (spec §6.1).
	 */
	private List<BlockPos> members = Collections.emptyList();
	@Nullable
	private BlockPos min;
	@Nullable
	private BlockPos max;
	@Nullable
	private BlockPos holderPos;

	/** Cached assembled flag, flipped by events (spec §7.3); no PAUSED state. */
	private boolean assembled = false;

	protected MultiblockController(Level level) {
		this.level = level;
		this.ownerHandler = new OwnerHandler();
		this.errorLogic = IForestryApi.INSTANCE.getErrorManager().createErrorLogic();
	}

	/* ===== Structure / geometry (spec §6.1) ===== */

	/**
	 * Installs the validated structure. {@code members} must be sorted lowest-first (the pattern engine
	 * already produces them in ascending {@code (x,y,z)} order); {@code min}/{@code max} are the bounding
	 * box recomputed from the member set; {@code holderPos} is the payload holder (the lowest member in
	 * steady state, or its hand-off survivor §6.4). Buckets are rebuilt by the concrete controller in its
	 * override of {@link #bucketComponents()} (called here after the geometry is in place).
	 */
	public void setStructure(List<BlockPos> members, BlockPos min, BlockPos max, BlockPos holderPos) {
		this.members = List.copyOf(members);
		this.min = min;
		this.max = max;
		this.holderPos = holderPos;
		bucketComponents();
	}

	/**
	 * Updates only the payload holder (a §6.4 re-anchor hand-off / §6.1 canonicalization), without
	 * re-bucketing. The concrete shared-inventory back-reference is re-pointed by the caller.
	 */
	public void setHolderPos(BlockPos holderPos) {
		this.holderPos = holderPos;
	}

	public List<BlockPos> getMembers() {
		return this.members;
	}

	/** The payload holder / anchor position (spec §6.1 #3); {@code null} before a structure is set. */
	@Nullable
	public BlockPos getHolderPos() {
		return this.holderPos;
	}

	/**
	 * The bounding-box minimum corner (spec §6.1 #2), recomputed from the member set on every (re)assembly.
	 * {@code null} only before the first structure is installed.
	 */
	@Nullable
	public BlockPos getMinimumCoord() {
		return this.min;
	}

	@Nullable
	public BlockPos getMaximumCoord() {
		return this.max;
	}

	/**
	 * The reference coord = lowest-{@code (x,y,z)} member (spec §6.1 #1). Always reads the validated member
	 * set; never falls back to a cached null/ZERO (the old {@code getReferenceCoord()} returning {@code ZERO}
	 * before resolution is exactly the path being removed). Returns {@code null} only when no structure is
	 * installed (an unassembled controller), which callers gate on {@link #isAssembled()}.
	 */
	@Nullable
	public BlockPos getReferenceCoord() {
		return this.members.isEmpty() ? null : this.members.get(0);
	}

	/** Bounding-box center (spec §6.1 #2); used for alveary climate origin / FX / farm targeting. */
	public BlockPos getCenterCoord() {
		BlockPos lo = this.min;
		BlockPos hi = this.max;
		if (lo == null || hi == null) {
			return BlockPos.ZERO;
		}
		return new BlockPos(
				(lo.getX() + hi.getX()) / 2,
				(lo.getY() + hi.getY()) / 2,
				(lo.getZ() + hi.getZ()) / 2
		);
	}

	/** Bounding-box top-center (spec §6.1 #2); used for alveary sky/light sampling. */
	public BlockPos getTopCenterCoord() {
		BlockPos lo = this.min;
		BlockPos hi = this.max;
		if (lo == null || hi == null) {
			return BlockPos.ZERO;
		}
		return new BlockPos(
				(lo.getX() + hi.getX()) / 2,
				hi.getY(),
				(lo.getZ() + hi.getZ()) / 2
		);
	}

	/** Whether the position is within this machine's bounding box (mirrors the old {@code isCoordInMultiblock}). */
	protected final boolean isCoordInMultiblock(int x, int y, int z) {
		BlockPos lo = this.min;
		BlockPos hi = this.max;
		if (lo == null || hi == null) {
			return false;
		}
		return x >= lo.getX() && x <= hi.getX()
				&& y >= lo.getY() && y <= hi.getY()
				&& z >= lo.getZ() && z <= hi.getZ();
	}

	/* ===== Assembled flag (spec §7.3) ===== */

	@Override
	public boolean isAssembled() {
		return this.assembled;
	}

	public void setAssembled(boolean assembled) {
		this.assembled = assembled;
	}

	/* ===== Public IMultiblockController surface (spec §11) ===== */

	/**
	 * Force a re-validation of this machine (spec §11). Used by the public API; the event-driven triggers
	 * (Task 2.5) normally call the validator directly, but the public {@code reassemble()} contract is kept
	 * by re-running validation from the current holder position.
	 */
	@Override
	public void reassemble() {
		BlockPos holder = this.holderPos;
		if (holder != null) {
			MultiblockValidation.validateAt(this.level, holder);
		}
	}

	@Override
	@Nullable
	public String getLastValidationError() {
		return this.lastValidationError;
	}

	public void setLastValidationError(@Nullable String lastValidationError) {
		this.lastValidationError = lastValidationError;
	}

	/**
	 * Resolves the live member BlockEntities from the validated member set (spec §5.2). Unloaded / missing
	 * members are skipped, so this is the loaded subset.
	 */
	@Override
	public List<IMultiblockComponent> getComponents() {
		List<IMultiblockComponent> components = new ArrayList<>(this.members.size());
		for (BlockPos pos : this.members) {
			IMultiblockComponent component = TileUtil.getTile(this.level, pos, IMultiblockComponent.class);
			if (component != null) {
				components.add(component);
			}
		}
		return components;
	}

	/* ===== E1 owner-vote-once (spec §3.1) ===== */

	/**
	 * Resolves the machine owner by majority vote over the member parts, but <b>only once</b> — at first
	 * formation (spec §3.1 E1). Subsequent (re)assemblies and reloads do not re-vote, because the owner is
	 * round-tripped in the payload and {@link #ownerResolved} stays set. On a legacy-migration / payload
	 * read, {@link #markOwnerResolved()} latches this without a vote so the adopted owner is authoritative.
	 */
	protected final void voteOwnerOnceIfNeeded() {
		if (this.ownerResolved || this.level.isClientSide) {
			return;
		}

		com.google.common.collect.Multiset<GameProfile> owners = com.google.common.collect.HashMultiset.create();
		for (IMultiblockComponent part : getComponents()) {
			GameProfile owner = part.getOwner();
			if (owner != null) {
				owners.add(owner);
			}
		}

		GameProfile owner = null;
		int max = 0;
		for (com.google.common.collect.Multiset.Entry<GameProfile> entry : owners.entrySet()) {
			int count = entry.getCount();
			if (count > max) {
				max = count;
				owner = entry.getElement();
			}
		}

		if (owner != null) {
			this.ownerHandler.setOwner(owner);
		}
		this.ownerResolved = true;
	}

	/** Latches the owner-vote (spec §3.1 E1) without voting (the owner came from the payload / legacy tag). */
	protected final void markOwnerResolved() {
		this.ownerResolved = true;
	}

	protected final boolean isOwnerResolved() {
		return this.ownerResolved;
	}

	/* ===== Owner / error logic / world (ported from MultiblockControllerForestry) ===== */

	@Override
	public IOwnerHandler getOwnerHandler() {
		return this.ownerHandler;
	}

	@Override
	public IErrorLogic getErrorLogic() {
		return this.errorLogic;
	}

	@Override
	public Level getWorldObj() {
		return this.level;
	}

	/* ===== Abstract per-machine surface (spec §7.1) ===== */

	/**
	 * Rebuilds the component buckets (bee modifiers/listeners/climatisers, farm active components, …) from
	 * the current member set. Called by {@link #setStructure} after the geometry is installed. The old
	 * engine did this incrementally in {@code onBlockAdded}; here the concrete controller iterates
	 * {@link #getMembers()} and resolves each member's BE.
	 */
	protected abstract void bucketComponents();

	/**
	 * Server-side machine logic, run once per tick by the holder BE's ticker while assembled (spec §7.1).
	 *
	 * @return true if the controller's state changed and the holder's chunk should be marked dirty.
	 */
	public abstract boolean serverTick(int tickCount);

	/** Client-side machine logic (alveary FX), run by the holder BE's client ticker while assembled. */
	public abstract void clientTick(int tickCount);

	/** Serializes the shared payload into the holder BE's NBT (spec §6.1). */
	public abstract CompoundTag writePayload(CompoundTag data);

	/** Reads the shared payload from the holder BE's NBT. */
	public abstract void readPayload(CompoundTag data);

	/** Serializes the client-sync subset of the payload into the holder's description packet (spec §9). */
	public abstract void writeDescriptionPayload(CompoundTag data);

	/** Reads the client-sync subset of the payload from the holder's description packet (spec §9). */
	public abstract void readDescriptionPayload(CompoundTag data);

	/** Fired on the disassembled→assembled transition (spec §7.3). */
	public abstract void onAssembled();

	/** Fired on the assembled→disassembled (or deactivate) transition (spec §7.3). */
	public abstract void onBroken();

	/** An unlocalized string identifying this machine type (e.g. {@code "for.multiblock.alveary.type"}). */
	public abstract String getUnlocalizedType();

	/* ===== Inventory plumbing (ported from MultiblockControllerForestry) ===== */

	/**
	 * The shared controller inventory. Concrete controllers return their real inventory when assembled and
	 * {@link FakeInventoryAdapter#INSTANCE} otherwise (so caps/GUIs resolve to a no-op before assembly).
	 */
	public IInventoryAdapter getInternalInventory() {
		return FakeInventoryAdapter.INSTANCE;
	}

	@Override
	public void setChanged() {
		getInternalInventory().setChanged();
	}

	@Override
	public final int getContainerSize() {
		return getInternalInventory().getContainerSize();
	}

	@Override
	public final ItemStack getItem(int slotIndex) {
		return getInternalInventory().getItem(slotIndex);
	}

	@Override
	public final ItemStack removeItem(int slotIndex, int amount) {
		return getInternalInventory().removeItem(slotIndex, amount);
	}

	@Override
	public ItemStack removeItemNoUpdate(int slotIndex) {
		return getInternalInventory().removeItemNoUpdate(slotIndex);
	}

	@Override
	public final void setItem(int slotIndex, ItemStack itemstack) {
		getInternalInventory().setItem(slotIndex, itemstack);
	}

	@Override
	public final int getMaxStackSize() {
		return getInternalInventory().getMaxStackSize();
	}

	@Override
	public final void startOpen(Player player) {
		getInternalInventory().startOpen(player);
	}

	@Override
	public final void stopOpen(Player player) {
		getInternalInventory().stopOpen(player);
	}

	@Override
	public final boolean stillValid(Player player) {
		return getInternalInventory().stillValid(player);
	}

	@Override
	public final boolean canPlaceItem(int slotIndex, ItemStack itemStack) {
		return getInternalInventory().canPlaceItem(slotIndex, itemStack);
	}

	@Override
	public int[] getSlotsForFace(Direction side) {
		return getInternalInventory().getSlotsForFace(side);
	}

	@Override
	public final boolean canPlaceItemThroughFace(int slotIndex, ItemStack itemStack, Direction side) {
		return getInternalInventory().canPlaceItemThroughFace(slotIndex, itemStack, side);
	}

	@Override
	public final boolean canTakeItemThroughFace(int slotIndex, ItemStack itemStack, Direction side) {
		return getInternalInventory().canTakeItemThroughFace(slotIndex, itemStack, side);
	}

	@Override
	public void clearContent() {
		getInternalInventory().clearContent();
	}

	@Override
	public boolean isEmpty() {
		return getInternalInventory().isEmpty();
	}

	/* ===== Convenience ===== */

	/** Mirrors the old {@code MultiblockControllerBase.updateOnInterval}. */
	protected final boolean updateOnInterval(int tickInterval, int tickCount) {
		return tickCount % tickInterval == 0;
	}

	/** The machine owner (the once-voted owner stored in the payload; spec §3.1 E1). */
	@Nullable
	public GameProfile getOwner() {
		return this.ownerHandler.getOwner();
	}

	/* ===== Payload owner round-trip (spec §3.1 E1, §6.1) ===== */

	/** Writes the machine owner into the payload tag (the controller payload, not the per-part owner). */
	protected final void writeOwner(CompoundTag data) {
		this.ownerHandler.write(data);
	}

	/** Reads the machine owner from the payload tag and latches the vote-once flag (spec §3.1 E1). */
	protected final void readOwner(CompoundTag data) {
		this.ownerHandler.read(data);
		// The owner is authoritative once it has been persisted in the payload; never re-vote on reload.
		markOwnerResolved();
	}

	/**
	 * Force-marks the chunk containing {@code pos} as unsaved so a stale holder copy is dropped on next save
	 * (spec §6.1 canonicalization / §6.4 re-anchor). Idempotent; no-op if the chunk is not loaded.
	 */
	public static void markChunkDirty(Level level, BlockPos pos) {
		net.minecraft.world.level.chunk.LevelChunk chunk = level.getChunkSource().getChunkNow(pos.getX() >> 4, pos.getZ() >> 4);
		if (chunk != null) {
			chunk.setUnsaved(true);
		}
	}
}

package forestry.core.multiblock;

import com.mojang.authlib.GameProfile;
import forestry.api.core.ILocationProvider;
import forestry.api.core.ISpectacleBlock;
import forestry.api.multiblock.IMultiblockComponent;
import forestry.api.multiblock.IMultiblockController;
import forestry.api.multiblock.IMultiblockLogic;
import forestry.api.multiblock.MultiblockTileEntityBase;
import forestry.core.config.Constants;
import forestry.core.inventory.FakeInventoryAdapter;
import forestry.core.inventory.IInventoryAdapter;
import forestry.core.multiblock.pattern.MultiblockPattern;
import forestry.core.tiles.IFilterSlotDelegate;
import forestry.core.tiles.TileUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.List;

public abstract class MultiblockTileEntityForestry<T extends IMultiblockLogic> extends MultiblockTileEntityBase<T> implements WorldlyContainer, IFilterSlotDelegate, ILocationProvider, MenuProvider, ISpectacleBlock {
	/** NBT key for the round-tripped controller payload (legacy: same key, so migration is a rename). */
	private static final String PAYLOAD_KEY = "multiblockData";
	/** NBT key for this member's stored anchor position. */
	private static final String ANCHOR_KEY = "anchorPos";

	@Nullable
	private GameProfile owner;

	public MultiblockTileEntityForestry(BlockEntityType<?> tileEntityType, BlockPos pos, BlockState state, T multiblockLogic) {
		super(tileEntityType, pos, state, multiblockLogic);
		if (multiblockLogic instanceof MultiblockLogicBase base) {
			base.setTile(this);
		}
	}

	/* ===== New-engine hosting hooks (spec §6.1, §7.1) ===== */

	/** Creates a fresh controller for this machine family (hosted by the holder). */
	public abstract MultiblockController createController(Level level);

	/** The declarative pattern for this machine family (spec §5.1). */
	public abstract MultiblockPattern getPattern();

	/**
	 * Resolves the live controller hosted at this member's anchor (spec §6.1). Returns {@code null} when
	 * unassembled / the anchor is missing; the typed {@code TileAlveary}/{@code TileFarm} accessors fall
	 * back to their {@code Fake} controller in that case.
	 */
	@Nullable
	public MultiblockController getController() {
		BlockPos anchor = getAnchorPos();
		if (anchor == null || this.level == null) {
			return null;
		}
		return MultiblockIndex.get(this.level, anchor);
	}

	/** True if this member is currently the payload holder (spec §6.1). */
	protected boolean isHolder() {
		BlockPos anchor = getAnchorPos();
		return anchor != null && anchor.equals(getBlockPos());
	}

	/** Seeds a freshly-created controller from this holder's stashed payload (spec §6.4 / §10). */
	public void applyStashTo(MultiblockController controller) {
		CompoundTag stash = getStash();
		if (stash != null) {
			controller.readPayload(stash);
		}
	}

	/* ===== GUI ===== */

	/**
	 * Called by a structure block when it is right clicked by a player.
	 */
	public void openGui(ServerPlayer player, BlockPos pos) {
		NetworkHooks.openScreen(player, this, pos);
	}

	/* ===== Persistence (spec §6.1 holder-gated, §6.4 stash) ===== */

	@Override
	public void load(CompoundTag data) {
		super.load(data);

		if (data.contains("owner")) {
			CompoundTag ownerNbt = data.getCompound("owner");
			this.owner = NbtUtils.readGameProfile(ownerNbt);
		}

		if (data.contains(ANCHOR_KEY)) {
			setAnchorPos(NbtUtils.readBlockPos(data.getCompound(ANCHOR_KEY)));
		}

		// The shared payload (controller state) is stashed until load-time validation adopts it. Any member
		// type may carry it after a re-anchor hand-off (spec §6.4), so always stash it if present.
		if (data.contains(PAYLOAD_KEY)) {
			setStash(data.getCompound(PAYLOAD_KEY).copy());
		}

		// Per-block own inventory (sieve / swarmer / hygroregulator) round-trips here. The shared controller
		// inventory is NOT read here — it travels in the payload (holder-gated).
		if (this instanceof IMultiblockComponent.HasInventory) {
			getInternalInventory().read(data);
		}
	}

	@Override
	public void saveAdditional(CompoundTag data) {
		super.saveAdditional(data);

		if (this.owner != null) {
			CompoundTag nbt = new CompoundTag();
			NbtUtils.writeGameProfile(nbt, this.owner);
			data.put("owner", nbt);
		}

		BlockPos anchor = getAnchorPos();
		if (anchor != null) {
			data.put(ANCHOR_KEY, NbtUtils.writeBlockPos(anchor));
		}

		// Single-holder invariant (spec §6.1): only the holder serializes the shared payload. If a live
		// controller is hosted here, write it fresh; otherwise round-trip the stash (e.g. unassembled, or a
		// hand-off survivor that hasn't re-validated yet).
		if (isHolder()) {
			MultiblockController controller = getController();
			if (controller != null) {
				CompoundTag payload = new CompoundTag();
				controller.writePayload(payload);
				data.put(PAYLOAD_KEY, payload);
			} else {
				CompoundTag stash = getStash();
				if (stash != null) {
					data.put(PAYLOAD_KEY, stash);
				}
			}
		} else {
			// Non-holder members that still carry a stash (pre-adoption) keep round-tripping it so it is not
			// lost across a save before validation; a holder write above always wins for the canonical copy.
			CompoundTag stash = getStash();
			if (stash != null) {
				data.put(PAYLOAD_KEY, stash);
			}
		}

		// Own inventory (sieve / swarmer / hygroregulator).
		if (this instanceof IMultiblockComponent.HasInventory) {
			getInternalInventory().write(data);
		}
	}

	/* ===== Lifecycle triggers (spec §5.3, §6.4, §7.4) ===== */

	@Override
	public void onLoad() {
		super.onLoad();
		if (this.level != null) {
			MultiblockValidation.validateFor(this.level, getBlockPos(), this);
		}
	}

	@Override
	public void onChunkUnloaded() {
		super.onChunkUnloaded();
		setUnloading(true);
		// Chunk unload is a temporary pause, NOT a break (spec §6.4, §7.4): flip the anchor's assembled flag
		// (stops ticking) but do NOT fire per-part onMachineBroken — those mutate blockstates (alveary
		// entrance textures / farm BAND) and must not run during a chunk unload (mirrors the old PAUSED path,
		// which fired no per-part callbacks). The state stays untouched in the anchor's NBT; reload re-fires
		// the assembled callbacks via load-time validation.
		if (this.level != null) {
			BlockPos anchor = getAnchorPos();
			if (anchor != null) {
				MultiblockController controller = MultiblockIndex.get(this.level, anchor);
				if (controller != null && controller.isAssembled()) {
					controller.setAssembled(false);
					controller.onBroken();
				}
			}
		}
	}

	@Override
	public void setRemoved() {
		boolean genuineBreak = !isUnloading();
		Level level = this.level;
		BlockPos pos = getBlockPos();
		BlockPos anchor = getAnchorPos();

		super.setRemoved();

		if (level == null || level.isClientSide) {
			return;
		}

		if (!genuineBreak) {
			// Temporary chunk unload: deactivation already happened in onChunkUnloaded. No re-anchor / drops.
			return;
		}

		// Genuine break (spec §6.4).
		MultiblockController controller = anchor == null ? null : MultiblockIndex.get(level, anchor);
		if (controller != null && pos.equals(anchor)) {
			handleHolderBreak(level, pos, controller);
		} else if (controller != null) {
			// Non-holder break: deactivate; payload stays on its holder; re-validate neighbors below.
			if (controller.isAssembled()) {
				List<IMultiblockComponent> parts = controller.getComponents();
				controller.setAssembled(false);
				controller.onBroken();
				for (IMultiblockComponent part : parts) {
					part.onMachineBroken();
				}
			}
		}

		// Re-validate neighbors so a still-valid sub/adjacent structure re-forms (spec §5.3).
		MultiblockValidation.validateNeighbors(level, pos);
	}

	/**
	 * The §6.4 re-anchor hand-off: this holder is being genuinely broken. Resolve the lowest-(x,y,z) loaded
	 * surviving member, hand it the payload, re-point the index, and force-mark this chunk dirty. Force-load
	 * the nearest survivor chunk if none is loaded. Only if no survivor exists at all do we full-dismantle.
	 */
	private void handleHolderBreak(Level level, BlockPos brokenHolder, MultiblockController controller) {
		MultiblockController.markChunkDirty(level, brokenHolder);

		// Candidate survivors = all current members minus the broken holder.
		List<BlockPos> members = controller.getMembers();
		BlockPos survivor = lowestLoadedSurvivor(level, members, brokenHolder);

		if (survivor == null) {
			survivor = lowestSurvivorForceLoaded(level, members, brokenHolder);
		}

		if (survivor == null) {
			// Truly the last member (no survivor on disk): full-dismantle drop (spec §6.3.2 / §6.4).
			MultiblockIndex.deregister(level, brokenHolder);
			controller.setAssembled(false);
			controller.onDestroyed(brokenHolder);
			return;
		}

		// Hand the payload to the survivor synchronously and re-point the index.
		MultiblockTileEntityForestry<?> survivorBe = TileUtil.getTile(level, survivor, MultiblockTileEntityForestry.class);
		MultiblockIndex.deregister(level, brokenHolder);

		if (survivorBe != null) {
			// Hand the live controller's serialized payload to the survivor as its stash (so a save before the
			// next validation persists it on the survivor), re-point the holder, and re-key the index. The live
			// controller already holds the in-memory state, so no re-read is needed.
			CompoundTag payload = new CompoundTag();
			controller.writePayload(payload);
			survivorBe.setStash(payload);
			survivorBe.setAnchorPos(survivor);
			controller.setHolderPos(survivor);
			MultiblockIndex.register(level, survivor, controller);
			MultiblockController.markChunkDirty(level, survivor);
		}

		// The structure is no longer whole; deactivate. A neighbor re-validation (caller) re-forms it if the
		// remaining members still satisfy the pattern.
		if (controller.isAssembled()) {
			List<IMultiblockComponent> parts = controller.getComponents();
			controller.setAssembled(false);
			controller.onBroken();
			for (IMultiblockComponent part : parts) {
				part.onMachineBroken();
			}
		}
	}

	@Nullable
	private static BlockPos lowestLoadedSurvivor(Level level, List<BlockPos> members, BlockPos broken) {
		BlockPos best = null;
		for (BlockPos pos : members) {
			if (pos.equals(broken)) {
				continue;
			}
			if (!level.getChunkSource().hasChunk(pos.getX() >> 4, pos.getZ() >> 4)) {
				continue;
			}
			// Skip a member that has itself been removed (multi-break in one operation).
			MultiblockTileEntityForestry<?> be = TileUtil.getTile(level, pos, MultiblockTileEntityForestry.class);
			if (be == null || be.isRemoved()) {
				continue;
			}
			if (best == null || pos.compareTo(best) < 0) {
				best = pos;
			}
		}
		return best;
	}

	@Nullable
	private static BlockPos lowestSurvivorForceLoaded(Level level, List<BlockPos> members, BlockPos broken) {
		BlockPos best = null;
		for (BlockPos pos : members) {
			if (pos.equals(broken)) {
				continue;
			}
			if (best == null || pos.compareTo(best) < 0) {
				best = pos;
			}
		}
		if (best == null) {
			return null;
		}
		// Force-load the survivor's chunk to perform the hand-off (spec §6.4 step 3).
		level.getChunk(best.getX() >> 4, best.getZ() >> 4, ChunkStatus.FULL, true);
		MultiblockTileEntityForestry<?> be = TileUtil.getTile(level, best, MultiblockTileEntityForestry.class);
		return (be != null && !be.isRemoved()) ? best : null;
	}

	/* ===== Network sync (holder carries the controller payload, spec §9) ===== */

	@Override
	protected void encodeDescriptionPacket(CompoundTag packetData) {
		super.encodeDescriptionPacket(packetData);
		BlockPos anchor = getAnchorPos();
		if (anchor != null) {
			packetData.put(ANCHOR_KEY, NbtUtils.writeBlockPos(anchor));
		}
		if (isHolder()) {
			MultiblockController controller = getController();
			if (controller != null) {
				CompoundTag payload = new CompoundTag();
				controller.writeDescriptionPayload(payload);
				packetData.put(PAYLOAD_KEY, payload);
			}
		}
	}

	@Override
	protected void decodeDescriptionPacket(CompoundTag packetData) {
		super.decodeDescriptionPacket(packetData);
		if (packetData.contains(ANCHOR_KEY)) {
			setAnchorPos(NbtUtils.readBlockPos(packetData.getCompound(ANCHOR_KEY)));
		}
		if (packetData.contains(PAYLOAD_KEY)) {
			MultiblockController controller = getController();
			if (controller != null) {
				controller.readDescriptionPayload(packetData.getCompound(PAYLOAD_KEY));
			} else {
				setStash(packetData.getCompound(PAYLOAD_KEY).copy());
			}
		}
	}

	/* ===== INVENTORY ===== */
	public IInventoryAdapter getInternalInventory() {
		return FakeInventoryAdapter.INSTANCE;
	}

	public boolean allowsAutomation() {
		return false;
	}

	@Override
	public boolean isEmpty() {
		return getInternalInventory().isEmpty();
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
		if (allowsAutomation()) {
			return getInternalInventory().getSlotsForFace(side);
		} else {
			return Constants.SLOTS_NONE;
		}
	}

	@Override
	public final boolean canPlaceItemThroughFace(int slotIndex, ItemStack itemStack, Direction side) {
		return allowsAutomation() && getInternalInventory().canPlaceItemThroughFace(slotIndex, itemStack, side);
	}

	@Override
	public final boolean canTakeItemThroughFace(int slotIndex, ItemStack itemStack, Direction side) {
		return allowsAutomation() && getInternalInventory().canTakeItemThroughFace(slotIndex, itemStack, side);
	}

	@Override
	public final boolean canSlotAccept(int slotIndex, ItemStack stack) {
		return getInternalInventory().canSlotAccept(slotIndex, stack);
	}

	@Override
	public final boolean isLocked(int slotIndex) {
		return getInternalInventory().isLocked(slotIndex);
	}

	/* ILocatable */
	@Override
	public final @Nullable Level getWorldObj() {
		return this.level;
	}

	/* IMultiblockComponent */

	@Override
	@Nullable
	public final GameProfile getOwner() {
		return this.owner;
	}

	public final void setOwner(GameProfile owner) {
		this.owner = owner;
	}

	@Override
	public void clearContent() {
		getInternalInventory().clearContent();
	}

	@Override
	public boolean isHighlighted(Player player) {
		if (!player.isCreative()) {
			return false;
		}
		MultiblockController controller = getController();
		return controller != null && controller.isAssembled() && getBlockPos().equals(controller.getReferenceCoord());
	}
}

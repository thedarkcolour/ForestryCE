package forestry.core.platform.multiblock;

import com.mojang.authlib.GameProfile;
import forestry.api.core.ILocationProvider;
import forestry.api.core.ISpectacleBlock;
import forestry.api.core.multiblock.IMultiblockComponent;
import forestry.api.core.multiblock.IMultiblockLogic;
import forestry.api.core.multiblock.MultiblockTileEntityBase;
import forestry.core.platform.config.Constants;
import forestry.core.platform.inventory.FakeInventoryAdapter;
import forestry.api.core.IInventoryAdapter;
import forestry.core.platform.multiblock.pattern.MultiblockPattern;
import forestry.api.core.IFilterSlotDelegate;
import forestry.core.platform.tile.TileUtil;
import forestry.core.platform.util.NBTUtilForestry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
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
import net.minecraft.world.level.chunk.status.ChunkStatus;

import javax.annotation.Nullable;
import java.util.List;

public abstract class MultiblockTileEntityForestry<T extends IMultiblockLogic> extends MultiblockTileEntityBase<T> implements WorldlyContainer, IFilterSlotDelegate, ILocationProvider, MenuProvider, ISpectacleBlock {
	/**
	 * NBT key for the round-tripped controller payload. The legacy engine used the same key, so migration is a
	 * rename. Public so the {@code /forestry multiblock debug} command can assert the single-holder invariant,
	 * that exactly one member emits this key in its {@code saveAdditional} (spec 6.1), without duplicating the
	 * magic string.
	 */
	public static final String PAYLOAD_KEY = "multiblockData";
	/** NBT key for this member's stored anchor position. */
	private static final String ANCHOR_KEY = "anchorPos";
	/** Description-packet-only key. The holder advertises that the structure is assembled (BUG 2, spec 9). */
	private static final String ASSEMBLED_KEY = "mbAssembled";
	/** Description-packet-only key. The holder advertises the member positions so the client reconstructs (BUG 2). */
	private static final String MEMBERS_KEY = "mbMembers";

	@Nullable
	private GameProfile owner;

	public MultiblockTileEntityForestry(BlockEntityType<?> tileEntityType, BlockPos pos, BlockState state, T multiblockLogic) {
		super(tileEntityType, pos, state, multiblockLogic);
		if (multiblockLogic instanceof MultiblockLogicBase base) {
			base.setTile(this);
		}
	}

	/* ===== New-engine hosting hooks (spec 6.1, 7.1) ===== */

	/** Creates a fresh controller for this machine family, hosted by the holder. */
	public abstract MultiblockController createController(Level level);

	/** The declarative pattern for this machine family (spec 5.1). */
	public abstract MultiblockPattern getPattern();

	/**
	 * Used by {@link LevelStructureView#typeIdFor} to resolve a cell's role without naming any part class.
	 * A subclass that does not override this inherits its parent's role, so an addon part extending a role
	 * part is that role, as it was before the pattern engine rewrite.
	 *
	 * @return The pattern type id for this part's role in its machine
	 */
	public abstract String patternTypeId();

	/**
	 * Resolves the live controller hosted at this member's anchor (spec 6.1). Returns {@code null} when
	 * unassembled or the anchor is missing. The typed {@code TileAlveary} and {@code TileFarm} accessors fall
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

	/** True if this member is currently the payload holder (spec 6.1). */
	protected boolean isHolder() {
		BlockPos anchor = getAnchorPos();
		return anchor != null && anchor.equals(getBlockPos());
	}

	/** Seeds a freshly-created controller from this member's stashed payload (spec 6.4 and 10). */
	public void applyStashTo(MultiblockController controller) {
		CompoundTag stash = getStash();
		if (stash != null) {
			controller.readPayload(stash);
		}
	}

	/**
	 * Determines whether this member currently carries a non-empty stashed payload (spec 6.4 and 10). Used by
	 * {@code MultiblockValidation.assemble} to find the lowest member whose stash holds the real payload after
	 * a re-anchor hand-off survivor (BUG 1) or a legacy multi-carrier migration (the section 10 tie-break),
	 * since the carrier may be a non-lowest member and the re-added lowest corner's stash may be empty.
	 */
	public boolean hasStash() {
		CompoundTag stash = getStash();
		return stash != null && !stash.isEmpty();
	}

	/**
	 * Serializes the controller's live payload into this member's stash (spec 6.4). Used on a deactivate, where
	 * the controller is deregistered from the index but the holder BE remains loaded, so the payload survives a
	 * save before re-validation and can be re-adopted through {@link #applyStashTo}.
	 */
	public void stashFrom(MultiblockController controller) {
		CompoundTag payload = new CompoundTag();
		controller.writePayload(payload);
		setStash(payload);
	}

	/* ===== GUI ===== */

	/**
	 * Called when a structure block is right clicked by a player.
	 */
	public void openGui(ServerPlayer player, BlockPos pos) {
		player.openMenu(this, pos);
	}

	/* ===== Persistence (spec 6.1 holder-gated, 6.4 stash) ===== */

	@Override
	public void loadAdditional(CompoundTag data, HolderLookup.Provider registries) {
		super.loadAdditional(data, registries);

		if (data.contains("owner")) {
			CompoundTag ownerNbt = data.getCompound("owner");
			this.owner = NBTUtilForestry.readGameProfile(ownerNbt);
		}

		if (data.contains(ANCHOR_KEY)) {
			NbtUtils.readBlockPos(data, ANCHOR_KEY).ifPresent(this::setAnchorPos);
		}

		// The shared payload, meaning controller state, is stashed until load-time validation adopts it. Any
		// member type may carry it after a re-anchor hand-off (spec 6.4), so always stash it if present.
		if (data.contains(PAYLOAD_KEY)) {
			setStash(data.getCompound(PAYLOAD_KEY).copy());
		}

		// Per-block own inventory (sieve, swarmer, hygroregulator) round-trips here. The shared controller
		// inventory is NOT read here, it travels in the holder-gated payload.
		if (this instanceof IMultiblockComponent.HasInventory) {
			getInternalInventory().read(data, registries);
		}
	}

	@Override
	public void saveAdditional(CompoundTag data, HolderLookup.Provider registries) {
		super.saveAdditional(data, registries);

		if (this.owner != null) {
			CompoundTag nbt = new CompoundTag();
			NBTUtilForestry.writeGameProfile(nbt, this.owner);
			data.put("owner", nbt);
		}

		BlockPos anchor = getAnchorPos();
		if (anchor != null) {
			data.put(ANCHOR_KEY, NbtUtils.writeBlockPos(anchor));
		}

		// Single-holder invariant (spec 6.1), only the holder serializes the shared payload. If a live
		// controller is hosted here, write it fresh, otherwise round-trip the stash (ex. unassembled, or a
		// hand-off survivor that hasn't re-validated yet)
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
			// Non-holder members that still carry a pre-adoption stash keep round-tripping it so it is not
			// lost across a save before validation. A holder write above always wins for the canonical copy.
			CompoundTag stash = getStash();
			if (stash != null) {
				data.put(PAYLOAD_KEY, stash);
			}
		}

		// Own inventory (sieve, swarmer, hygroregulator)
		if (this instanceof IMultiblockComponent.HasInventory) {
			getInternalInventory().write(data, registries);
		}
	}

	@Override
	public void setChanged() {
		super.setChanged();
		// menus and hatches write the shared inventory through this member, which may be in a different chunk
		// than the holder that saves it
		MultiblockController controller = getController();
		if (controller != null) {
			controller.setChanged();
		}
	}

	/* ===== Lifecycle triggers (spec 5.3, 6.4, 7.4) ===== */

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
		// Chunk unload is a temporary pause, NOT a break (spec 6.4, 7.4). Flip the anchor's assembled flag,
		// which stops ticking, but do NOT fire per-part onMachineBroken. Those mutate blockstates (alveary
		// entrance textures, farm BAND) and must not run during a chunk unload. This mirrors the old PAUSED
		// path, which fired no per-part callbacks. Likewise do NOT call controller.onBroken(), because for the
		// farm that clears the computed targets, which the old PAUSED path left intact (MINOR 6 parity). The
		// state stays untouched in the anchor's NBT, and reload re-fires the assembled callbacks through
		// load-time validation.
		if (this.level != null && !this.level.isClientSide) {
			BlockPos anchor = getAnchorPos();
			if (anchor != null) {
				MultiblockController controller = MultiblockIndex.get(this.level, anchor);
				if (controller != null && controller.isAssembled()) {
					controller.setAssembled(false);
				}
				// the chunk was saved just before unloading, so a reload must read that copy instead of reviving
				// this controller, which would also never be released
				if (controller != null && anchor.equals(getBlockPos())) {
					MultiblockIndex.deregister(this.level, anchor);
				}
			}
			DeferredValidation.forget(this.level, getBlockPos());
		}
	}

	@Override
	public void setRemoved() {
		boolean genuineBreak = !isUnloading();
		Level level = this.level;
		BlockPos pos = getBlockPos();
		BlockPos anchor = getAnchorPos();

		super.setRemoved();

		if (level == null) {
			return;
		}
		if (level.isClientSide) {
			// the server resends the new holder's packet if the machine survives
			dropClientController();
			return;
		}

		if (!genuineBreak) {
			// Temporary chunk unload, deactivation already happened in onChunkUnloaded. No re-anchor, no drops
			return;
		}
		DeferredValidation.forget(level, pos);

		// Genuine break (spec 6.4). Three cases by where the shared payload currently lives:
		//   A. a LIVE controller is hosted at this broken holder          -> re-anchor controller state, or drop
		//   B. a LIVE controller is hosted at another, non-holder member  -> deactivate it, payload stays put
		//   C. NO live controller, but this BE carries the dormant stash  -> re-anchor the stash, or drop it
		// Case C is the load-bearing fix for the silent-wipe bug. The first break of any part deactivates the
		// controller, which deregisters it from the index and hands the payload to the holder BE's stash. From then
		// on there is no live controller, so before this fix a later break of the stash carrier would fall through
		// every branch and the only copy of the inventory would die with the block, with no re-anchor and no drop.
		// Now the carrier hands its stash to a surviving sibling, or drops the contents into the world when it is
		// the last member. That is parity with the GregTech-style "controller destroyed -> drop" (spec 6.3.2, 6.4).
		MultiblockController controller = anchor == null ? null : MultiblockIndex.get(level, anchor);
		if (controller != null && pos.equals(anchor)) {
			handleHolderBreak(level, pos, controller);
		} else if (controller != null) {
			// Non-holder break. Deactivate, the payload stays on its holder, and neighbors re-validate below
			if (controller.isAssembled()) {
				List<IMultiblockComponent> parts = controller.getComponents();
				controller.setAssembled(false);
				controller.onBroken();
				for (IMultiblockComponent part : parts) {
					part.onMachineBroken();
				}
				MultiblockController.syncHolder(level, anchor);
			}
		} else if (hasStash()) {
			// Dormant payload carrier broken with no live controller (Case C)
			handleStashCarrierBreak(level, pos);
		}

		// Re-validate neighbors so a still-valid sub-structure or adjacent structure re-forms (spec 5.3)
		MultiblockValidation.validateNeighbors(level, pos);
	}

	/**
	 * Performs the spec 6.4 re-anchor hand-off when this holder is genuinely broken. Resolves the
	 * lowest-(x,y,z) loaded surviving member, hands it the payload, re-points the index, and force-marks this
	 * chunk dirty. Force-loads the nearest survivor chunk if none is loaded. Only if no survivor exists at all
	 * does the machine full-dismantle.
	 */
	private void handleHolderBreak(Level level, BlockPos brokenHolder, MultiblockController controller) {
		MultiblockController.markChunkDirty(level, brokenHolder);

		// Candidate survivors are all current members minus the broken holder
		List<BlockPos> members = controller.getMembers();
		BlockPos survivor = lowestLoadedSurvivor(level, members, brokenHolder);

		if (survivor == null) {
			survivor = lowestSurvivorForceLoaded(level, members, brokenHolder);
		}

		if (survivor == null) {
			// Truly the last member, with no survivor on disk. Full-dismantle drop (spec 6.3.2, 6.4)
			MultiblockIndex.deregister(level, brokenHolder);
			controller.setAssembled(false);
			controller.onDestroyed(brokenHolder);
			return;
		}

		// Hand the payload to the survivor synchronously and re-point the index
		MultiblockTileEntityForestry<?> survivorBe = TileUtil.getTile(level, survivor, MultiblockTileEntityForestry.class);
		MultiblockIndex.deregister(level, brokenHolder);

		if (survivorBe != null) {
			// Hand the live controller's serialized payload to the survivor as its stash (so a save before the
			// next validation persists it on the survivor), re-point the holder, and re-key the index. The live
			// controller already holds the in-memory state, so no re-read is needed.
			//
			// Single-holder invariant (spec 6.1), exactly one loaded member serializes the payload. After this
			// hand-off the survivor is the sole holder. The broken holder must NOT retain a stash, so clear it.
			// Then even if a save were to run on it before removal completes, its non-holder saveAdditional
			// branch emits no PAYLOAD_KEY and only the survivor writes the payload.
			clearStash();
			CompoundTag payload = new CompoundTag();
			controller.writePayload(payload);
			survivorBe.setStash(payload);
			survivorBe.setAnchorPos(survivor);
			controller.setHolderPos(survivor);
			MultiblockIndex.register(level, survivor, controller);
			MultiblockController.markChunkDirty(level, survivor);

			// Keep the about-to-be-dormant structure anchor-consistent by re-pointing every OTHER surviving
			// member to the new holder too. Without this, only the survivor knows the new anchor and the
			// remaining members still point at the now-gone holder. A subsequent break of the survivor (Case C)
			// could then no longer find its siblings by shared anchorPos and would drop the inventory
			// prematurely while members remain.
			for (BlockPos mpos : members) {
				if (mpos.equals(brokenHolder) || mpos.equals(survivor)) {
					continue;
				}
				MultiblockTileEntityForestry<?> mbe = TileUtil.getTile(level, mpos, MultiblockTileEntityForestry.class);
				if (mbe != null && !mbe.isRemoved()) {
					mbe.setAnchorPos(survivor);
					// Persist the re-pointed anchor explicitly rather than relying on the per-part
					// onMachineBroken -> setChanged below, mirroring the Case C hand-off loop, so a save
					// before re-validation keeps the dormant structure anchor-consistent.
					MultiblockController.markChunkDirty(level, mpos);
				}
			}
		}

		// The structure is no longer whole, so deactivate. The caller's neighbor re-validation re-forms it if
		// the remaining members still satisfy the pattern.
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
			// Skip a member that has itself been removed, from a multi-break in one operation
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
		// Force-load the survivor's chunk to perform the hand-off (spec 6.4 step 3)
		level.getChunk(best.getX() >> 4, best.getZ() >> 4, ChunkStatus.FULL, true);
		MultiblockTileEntityForestry<?> be = TileUtil.getTile(level, best, MultiblockTileEntityForestry.class);
		return (be != null && !be.isRemoved()) ? best : null;
	}

	/**
	 * Performs the spec 6.4 re-anchor hand-off for a <b>dormant</b> structure (Case C). This BE carries the
	 * shared payload as a stash, because the structure was deactivated and the index entry dropped, and it is
	 * now being genuinely broken. There is no live controller and therefore no member list, so the surviving
	 * siblings are recovered by scanning the local neighbourhood for loaded same-family members that share this
	 * carrier's {@code anchorPos}. Members of two different machines never share an anchor, so the scan can
	 * never bleed into a neighbouring machine. The lowest survivor becomes the new dormant carrier. If none
	 * survives, the inventory is dropped into the world so nothing is ever silently lost.
	 */
	private void handleStashCarrierBreak(Level level, BlockPos brokenPos) {
		CompoundTag stash = getStash();
		if (stash == null || stash.isEmpty()) {
			return;
		}
		BlockPos anchor = getAnchorPos();
		BlockPos targetAnchor = anchor != null ? anchor : brokenPos;

		List<BlockPos> siblings = collectSharedAnchorSiblings(level, brokenPos, targetAnchor);
		if (siblings.isEmpty()) {
			// No LOADED sibling found. Before treating this as the terminal member and dropping the inventory,
			// mirror handleHolderBreak's force-load fallback. A still-standing sibling could be in a
			// neighbouring chunk that is currently unloaded, when the structure straddles a chunk border.
			// Force-load the at most 2x2 chunks the radius-4 scan can reach, then try once more, so the machine
			// re-anchors instead of dropping while real members survive on disk. In the common terminal case,
			// truly the last block, the re-scan is still empty and the contents drop as before.
			forceLoadNeighbourChunks(level, brokenPos);
			siblings = collectSharedAnchorSiblings(level, brokenPos, targetAnchor);
		}
		BlockPos survivor = null;
		for (BlockPos s : siblings) {
			if (survivor == null || s.compareTo(survivor) < 0) {
				survivor = s;
			}
		}

		MultiblockTileEntityForestry<?> survivorBe = survivor == null ? null
				: TileUtil.getTile(level, survivor, MultiblockTileEntityForestry.class);
		if (survivorBe == null) {
			// Terminal case, this was the last loaded member. Drop the stashed inventory into the world, which
			// is the fix for "destroy every block -> nothing drops", then clear the stash so the dying BE
			// writes nothing.
			dropStashContents(level, brokenPos, stash);
			clearStash();
			return;
		}

		// Hand the stash to the lowest survivor and re-point every sibling, and the survivor, at it, so the
		// chain stays anchor-consistent for the next break.
		survivorBe.setStash(stash.copy());
		clearStash();
		for (BlockPos s : siblings) {
			MultiblockTileEntityForestry<?> sbe = TileUtil.getTile(level, s, MultiblockTileEntityForestry.class);
			if (sbe != null) {
				sbe.setAnchorPos(survivor);
				MultiblockController.markChunkDirty(level, s);
			}
		}
	}

	/**
	 * Scans the bounded neighbourhood around the broken position for loaded, not-removed, same-family
	 * {@link MultiblockTileEntityForestry} members whose {@code anchorPos} equals the target anchor. Those are
	 * the surviving siblings of this carrier's dormant structure, and this BE itself is excluded. The radius
	 * covers the largest possible machine, a 5-wide farm where two members can be 4 blocks apart. The
	 * shared-anchor guard makes the scan structure-exact even if a different machine of the same family sits
	 * adjacent.
	 */
	private List<BlockPos> collectSharedAnchorSiblings(Level level, BlockPos brokenPos, BlockPos targetAnchor) {
		MultiblockPattern family = getPattern();
		List<BlockPos> result = new java.util.ArrayList<>();
		int r = 4;
		for (int dx = -r; dx <= r; dx++) {
			for (int dy = -r; dy <= r; dy++) {
				for (int dz = -r; dz <= r; dz++) {
					if (dx == 0 && dy == 0 && dz == 0) {
						continue;
					}
					BlockPos p = brokenPos.offset(dx, dy, dz);
					if (!level.getChunkSource().hasChunk(p.getX() >> 4, p.getZ() >> 4)) {
						continue;
					}
					MultiblockTileEntityForestry<?> be = TileUtil.getTile(level, p, MultiblockTileEntityForestry.class);
					if (be == null || be == this || be.isRemoved() || be.getPattern() != family) {
						continue;
					}
					BlockPos a = be.getAnchorPos();
					if (a != null && a.equals(targetAnchor)) {
						result.add(p.immutable());
					}
				}
			}
		}
		return result;
	}

	/**
	 * Force-loads the at most 2x2 chunks reachable by the radius-4 sibling scan around the center that are not
	 * already loaded. Used by {@link #handleStashCarrierBreak} as the spec 6.4 force-load fallback so a dormant
	 * structure that straddles a chunk border re-anchors to a survivor across the border instead of dropping its
	 * inventory while real members still exist on disk. A plus or minus 4 block span crosses at most one chunk
	 * boundary per axis, so this loads no more than four chunks, and only when the cheap loaded-only scan found
	 * nothing.
	 */
	private static void forceLoadNeighbourChunks(Level level, BlockPos center) {
		int r = 4;
		int minCx = (center.getX() - r) >> 4;
		int maxCx = (center.getX() + r) >> 4;
		int minCz = (center.getZ() - r) >> 4;
		int maxCz = (center.getZ() + r) >> 4;
		for (int cx = minCx; cx <= maxCx; cx++) {
			for (int cz = minCz; cz <= maxCz; cz++) {
				if (!level.getChunkSource().hasChunk(cx, cz)) {
					level.getChunk(cx, cz, ChunkStatus.FULL, true);
				}
			}
		}
	}

	/**
	 * Drops the shared inventory carried by a dormant stash into the world. Reuses the controller's
	 * {@link MultiblockController#onDestroyed} drop logic by reading the stash into a throwaway controller,
	 * which yields the same items a live controller would drop (ex. the alveary's bee inventory, or the farm's
	 * inventory and sockets). Fluids are intentionally not dropped, matching the live-controller path.
	 */
	private void dropStashContents(Level level, BlockPos pos, CompoundTag stash) {
		MultiblockController throwaway = createController(level);
		throwaway.readPayload(stash);
		throwaway.onDestroyed(pos);
	}

	/* ===== Network sync (holder carries the controller payload, spec 9) ===== */

	@Override
	protected void encodeDescriptionPacket(CompoundTag packetData) {
		super.encodeDescriptionPacket(packetData);
		BlockPos anchor = getAnchorPos();
		if (anchor != null) {
			packetData.put(ANCHOR_KEY, NbtUtils.writeBlockPos(anchor));
		}
		if (isHolder()) {
			MultiblockController controller = getController();
			if (controller != null && controller.isAssembled()) {
				// BUG 2. The holder carries the FULL assembled state to the client so the client can reconstruct
				// a real controller without relying on its own validation. The client chunk source's hasChunk,
				// used by LevelStructureView.isLoaded, is unreliable, so the client's maximality and loaded-shell
				// checks defer and the client controller would otherwise never form. That makes getController()
				// resolve to the Fake controller, which crashes the GUI, and breaks the spectacle highlight.
				// Sending assembled=true, the member set and the payload lets decodeDescriptionPacket trust the
				// server's authoritative state (spec 9).
				packetData.putBoolean(ASSEMBLED_KEY, true);
				CompoundTag members = new CompoundTag();
				List<BlockPos> memberPositions = controller.getMembers();
				members.putInt("count", memberPositions.size());
				for (int i = 0; i < memberPositions.size(); i++) {
					members.put(Integer.toString(i), NbtUtils.writeBlockPos(memberPositions.get(i)));
				}
				packetData.put(MEMBERS_KEY, members);

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
			NbtUtils.readBlockPos(packetData, ANCHOR_KEY).ifPresent(this::setAnchorPos);
		}

		// BUG 2. When the holder's packet carries the full assembled state, reconstruct and register a real
		// client-side controller and adopt the synced payload. This is the authoritative client path. It does
		// NOT depend on the client running its own unreliable validation, so the GUI and highlight resolve a
		// real assembled controller after a world reload (spec 9).
		if (this.level != null && this.level.isClientSide) {
			if (packetData.getBoolean(ASSEMBLED_KEY) && packetData.contains(MEMBERS_KEY)) {
				reconstructClientController(packetData);
			} else {
				dropClientController();
			}
		}
	}

	// only a holder's packet ever registered a client controller at this position
	private void dropClientController() {
		BlockPos pos = getBlockPos();
		MultiblockController controller = MultiblockIndex.get(this.level, pos);
		if (controller != null) {
			controller.setAssembled(false);
			controller.onBroken();
			MultiblockIndex.deregister(this.level, pos);
		}
	}

	/**
	 * Reconstructs the assembled controller on the client from the holder's synced state (BUG 2, spec 9). Gets
	 * or creates the controller at this holder, installs the synced member set and bounding box, marks it
	 * assembled, reads the description payload, registers it in the client {@link MultiblockIndex}, and wires
	 * every loaded member's {@code anchorPos} to this holder so each member's {@code getController()} resolves
	 * the real controller instead of the Fake. Trusting the server's synced state avoids the client's
	 * unreliable {@code hasChunk}-based validation.
	 */
	private void reconstructClientController(CompoundTag packetData) {
		BlockPos holderPos = getBlockPos();

		// Decode the synced member positions and derive the client controller's min and max bounding box
		CompoundTag membersTag = packetData.getCompound(MEMBERS_KEY);
		int count = membersTag.getInt("count");
		List<BlockPos> members = new java.util.ArrayList<>(count);
		BlockPos min = null;
		BlockPos max = null;
		for (int i = 0; i < count; i++) {
			BlockPos pos = NbtUtils.readBlockPos(membersTag, Integer.toString(i)).orElse(null);
			if (pos == null) {
				continue;
			}
			members.add(pos);
			if (min == null) {
				min = pos;
				max = pos;
			} else {
				min = new BlockPos(Math.min(min.getX(), pos.getX()), Math.min(min.getY(), pos.getY()), Math.min(min.getZ(), pos.getZ()));
				max = new BlockPos(Math.max(max.getX(), pos.getX()), Math.max(max.getY(), pos.getY()), Math.max(max.getZ(), pos.getZ()));
			}
		}
		if (members.isEmpty()) {
			return;
		}

		// Get or create the client controller hosted at this holder
		MultiblockController controller = MultiblockIndex.get(this.level, holderPos);
		if (controller == null) {
			controller = createController(this.level);
		}

		controller.setStructure(members, min, max, holderPos);
		controller.setHolderPos(holderPos);
		controller.setAssembled(true);
		if (packetData.contains(PAYLOAD_KEY)) {
			controller.readDescriptionPayload(packetData.getCompound(PAYLOAD_KEY));
		}
		// Fire the assembled transition so the controller's derived client state is real (ex. the alveary's
		// climate provider, while the farm is a no-op). Per-part onMachineAssembled visuals, meaning entrance
		// textures and BAND, are blockstates set on the server and synced by vanilla block updates, so they are
		// intentionally NOT re-fired here.
		controller.onAssembled();
		MultiblockIndex.register(this.level, holderPos, controller);
		clearStash();

		// Wire every loaded member's anchor to this holder so getController() resolves on each member. The GUI
		// is opened against the clicked member BE, and the highlight runs per-member. Members in unloaded
		// client chunks are skipped. They receive their own anchorPos in their own description packet when
		// they load.
		for (BlockPos mpos : members) {
			MultiblockTileEntityForestry<?> mbe = TileUtil.getTile(this.level, mpos, MultiblockTileEntityForestry.class);
			if (mbe != null) {
				mbe.setAnchorPos(holderPos);
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
		if (controller != null && controller.isAssembled()) {
			// Assembled, so highlight only the anchor (reference coord), in the rainbow colour
			return getBlockPos().equals(controller.getReferenceCoord());
		}
		// Unformed, so highlight every multiblock part in the flashing-white colour, see usesFlashingHighlight.
		// A player building a structure can then see exactly which blocks the engine recognises as parts and
		// where the would-be anchor, the lowest one, sits.
		return true;
	}

	@Override
	public boolean usesFlashingHighlight(Player player) {
		// Flash white while unformed, steady rainbow once assembled, both handled by the renderer
		MultiblockController controller = getController();
		return controller == null || !controller.isAssembled();
	}
}

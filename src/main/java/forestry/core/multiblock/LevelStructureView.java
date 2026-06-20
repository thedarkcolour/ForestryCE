package forestry.core.multiblock;

import forestry.apiculture.features.ApicultureTiles;
import forestry.apiculture.multiblock.AlvearyPattern;
import forestry.core.multiblock.pattern.StructurePos;
import forestry.core.multiblock.pattern.StructureView;
import forestry.farming.features.FarmingTiles;
import forestry.farming.multiblock.FarmPattern;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * The Phase-2 world adapter: a {@link StructureView} over a Forge {@link Level} (plan Task 2.1; spec
 * §5.2). It performs <b>chunk-safe</b> reads — {@link #isLoaded} checks the chunk source rather than
 * forcing a load — so the pattern engine's maximality + loaded-shell rule (spec §5.2, audit D1) can defer
 * on partially-loaded structures instead of assembling a shrunken sub-prism.
 *
 * <p>{@link #sample} reads the in-world {@link BlockEntity}, the blockstate's
 * {@code is(BlockTags.WOODEN_SLABS)} (the alveary slab cap), and {@code isSolidRender} (the alveary
 * entrance air ring), and resolves the cell's {@code componentTypeId} from the BE's
 * {@link BlockEntityType}.
 *
 * <p><b>Type-id translation (Phase-1 hand-off note).</b> In-world BE-type registration ids do <em>not</em>
 * match the pattern's type-id strings: {@code TileAlvearyPlain} registers as {@code "alveary"} (not
 * {@code "alveary_plain"}), and every farm member registers without the {@code "farm_"} prefix
 * ({@code "plain"}, {@code "gearbox"}, …). {@link #typeIdFor} therefore maps each member
 * {@link BlockEntityType} to the pattern type-id via an explicit table built from the feature holders
 * ({@code ApicultureTiles}/{@code FarmingTiles}). A BE whose type is not a known multiblock member maps
 * to {@code null} (the cell is reported as not-a-component → {@code invalid.interior}).
 */
public final class LevelStructureView implements StructureView {
	private final Level level;

	public LevelStructureView(Level level) {
		this.level = level;
	}

	/* ===== StructurePos <-> BlockPos ===== */

	private static BlockPos toBlockPos(StructurePos pos) {
		return new BlockPos(pos.x(), pos.y(), pos.z());
	}

	@Override
	public boolean isLoaded(StructurePos pos) {
		// Chunk-safe: do NOT force-load. hasChunk(chunkX, chunkZ) mirrors the old engine's chunk checks.
		return this.level.getChunkSource().hasChunk(pos.x() >> 4, pos.z() >> 4);
	}

	@Override
	public CellSample sample(StructurePos pos) {
		BlockPos blockPos = toBlockPos(pos);

		// Component: resolve via the BlockEntity's type. getBlockEntity is safe here because validation
		// only samples cells it has already confirmed loaded (loaded-shell rule).
		BlockEntity be = this.level.getBlockEntity(blockPos);
		String componentTypeId = be == null ? null : typeIdFor(be.getType());
		boolean isComponent = componentTypeId != null;

		BlockState state = this.level.getBlockState(blockPos);
		boolean isWoodenSlab = state.is(BlockTags.WOODEN_SLABS);
		boolean isSolidRender = state.isSolidRender(this.level, blockPos);

		return new CellSample(isComponent, be, isWoodenSlab, isSolidRender, componentTypeId);
	}

	/* ===== BE type -> pattern type-id mapping ===== */

	/**
	 * Lazily-built map from member {@link BlockEntityType} to pattern type-id string. Built on first use
	 * (after registration is complete — {@code FeatureTileType.tileType()} resolves the registry object),
	 * keyed by type identity.
	 */
	@Nullable
	private static volatile Map<BlockEntityType<?>, String> typeIds;

	@Nullable
	private static String typeIdFor(BlockEntityType<?> type) {
		Map<BlockEntityType<?>, String> map = typeIds;
		if (map == null) {
			map = buildTypeIds();
			typeIds = map;
		}
		return map.get(type);
	}

	private static synchronized Map<BlockEntityType<?>, String> buildTypeIds() {
		// double-checked: another thread may have built it while we waited on the lock
		Map<BlockEntityType<?>, String> existing = typeIds;
		if (existing != null) {
			return existing;
		}

		Map<BlockEntityType<?>, String> map = new IdentityHashMap<>();

		// --- Alveary members. In-world "alveary" -> pattern "alveary_plain"; the rest already match. ---
		map.put(ApicultureTiles.ALVEARY_PLAIN.tileType(), AlvearyPattern.PLAIN);
		map.put(ApicultureTiles.ALVEARY_SIEVE.tileType(), AlvearyPattern.PREFIX + "sieve");
		map.put(ApicultureTiles.ALVEARY_SWARMER.tileType(), AlvearyPattern.PREFIX + "swarmer");
		map.put(ApicultureTiles.ALVEARY_HYGROREGULATOR.tileType(), AlvearyPattern.PREFIX + "hygroregulator");
		map.put(ApicultureTiles.ALVEARY_STABILISER.tileType(), AlvearyPattern.PREFIX + "stabiliser");
		map.put(ApicultureTiles.ALVEARY_FAN.tileType(), AlvearyPattern.PREFIX + "fan");
		map.put(ApicultureTiles.ALVEARY_HEATER.tileType(), AlvearyPattern.PREFIX + "heater");

		// --- Farm members. Every in-world id lacks the "farm_" prefix and must be translated. ---
		map.put(FarmingTiles.PLAIN.tileType(), FarmPattern.PLAIN);
		map.put(FarmingTiles.GEARBOX.tileType(), FarmPattern.GEARBOX);
		map.put(FarmingTiles.HATCH.tileType(), FarmPattern.PREFIX + "hatch");
		map.put(FarmingTiles.VALVE.tileType(), FarmPattern.PREFIX + "valve");
		map.put(FarmingTiles.CONTROL.tileType(), FarmPattern.PREFIX + "control");

		return map;
	}
}

package forestry.apiculture.genetics.effects;

import forestry.api.IForestryApi;
import forestry.api.apiculture.IBeeHousing;
import forestry.api.apiculture.IBeeModifier;
import forestry.api.apiculture.IBeekeepingLogic;
import forestry.api.apiculture.bee.BeeLifeStage;
import forestry.api.apiculture.bee.IBeeEffect;
import forestry.api.genetics.IEffectData;
import forestry.api.genetics.IGenome;
import forestry.api.genetics.IIndividual;
import forestry.api.genetics.alleles.BeeChromosomes;
import forestry.api.genetics.capability.IIndividualHandlerItem;
import forestry.apiculture.genetics.Bee;
import forestry.core.tiles.TileUtil;
import forestry.core.utils.VecUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

// An effect applied to other bee hives that shouldn't stack (ex. the Chronophage and Rejuvenation effects)
public abstract class NonStackingBeeEffect implements IBeeEffect {
	private final HashMap<ResourceKey<Level>, HashSet<BlockPos>> trackedOwners;
	private final boolean dominant;

	public NonStackingBeeEffect(boolean dominant) {
		this.dominant = dominant;
		this.trackedOwners = new HashMap<>();

		NeoForge.EVENT_BUS.addListener(this::performGlobalEffect);
	}

	@Override
	public IEffectData doEffect(IGenome genome, IEffectData storedData, IBeeHousing housing) {
		// Don't spam adding to the set
		if ((housing.getLevel().getGameTime() & 64L) == 0) {
			this.trackedOwners.computeIfAbsent(housing.getLevel().dimension(), key -> new HashSet<>()).add(housing.getBlockPos());
		}
		return IBeeEffect.super.doEffect(genome, storedData, housing);
	}

	@Override
	public boolean isDominant() {
		return this.dominant;
	}

	private void performGlobalEffect(LevelTickEvent.Pre event) {
		Level level = event.getLevel();

		if (level.isClientSide || level.getGameTime() % IBeekeepingLogic.DEFAULT_WORK_THROTTLE != 0) {
			return;
		}

		HashSet<BlockPos> owners = this.trackedOwners.computeIfAbsent(level.dimension(), key -> new HashSet<>());
		HashSet<BlockPos> affectedHives = new HashSet<>();

		for (Iterator<BlockPos> iterator = owners.iterator(); iterator.hasNext(); ) {
			BlockPos pos = iterator.next();
			IBeeHousing housing = TileUtil.getTile(level, pos, IBeeHousing.class);

			// Don't want to call canWork twice in one tick
			if (housing != null && !housing.getErrorLogic().hasErrors()) {
				IIndividualHandlerItem handler = IIndividualHandlerItem.get(housing.getBeeInventory().getQueen());

				if (handler != null && handler.getStage() == BeeLifeStage.QUEEN) {
					IIndividual queen = handler.getIndividual();
					IGenome genome = queen.getGenome();

					if (genome.getActiveValue(BeeChromosomes.EFFECT) == this || genome.getInactiveValue(BeeChromosomes.EFFECT) == this) {
						IBeeModifier modifier = IForestryApi.INSTANCE.getHiveManager().createBeeHousingModifier(housing);
						Vec3i territory = Bee.getAdjustedTerritory(genome, modifier);

						affectNearbyTiles(affectedHives, level, pos, territory);

						// Skips the iterator.remove() at the end of the loop
						continue;
					}
				}
			}

			iterator.remove();
		}
	}

	private void affectNearbyTiles(HashSet<BlockPos> affectedHives, Level level, BlockPos pos, Vec3i territory) {
		BlockPos topLeft = pos.offset(VecUtil.center(territory));
		BlockPos bottomRight = topLeft.offset(territory);

		int topLeftX = topLeft.getX();
		int topLeftZ = topLeft.getZ();

		int bottomRightX = bottomRight.getX();
		int bottomRightZ = bottomRight.getZ();

		int territoryX = territory.getX();
		int territoryY = territory.getY();
		int territoryZ = territory.getZ();

		for (int x = SectionPos.blockToSectionCoord(topLeftX); x <= SectionPos.blockToSectionCoord(bottomRightX); x++) {
			for (int z = SectionPos.blockToSectionCoord(topLeftZ); z <= SectionPos.blockToSectionCoord(bottomRightZ); z++) {
				if (level.hasChunk(x, z)) {
					for (Map.Entry<BlockPos, BlockEntity> entry : level.getChunk(x, z).getBlockEntities().entrySet()) {
						BlockPos targetPos = entry.getKey();

						if (entry.getValue() instanceof IBeeHousing housing) {
							if (targetPos.equals(pos)) {
								continue;
							}
							// don't do math if already affected
							if (affectedHives.contains(targetPos)) {
								continue;
							}

							int targetX = targetPos.getX();

							if (targetX >= topLeftX && targetX < topLeftX + territoryX) {
								int targetY = targetPos.getY();

								if (targetY >= topLeft.getY() && targetY < topLeft.getY() + territoryY) {
									int targetZ = targetPos.getZ();

									if (targetZ >= topLeftZ && targetZ < topLeftZ + territoryZ) {
										if (affectedHives.add(targetPos)) {
											doEffectForHive(level, housing);
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Performs the effect on another beehive. Multiple hives that perform the same effect cannot affect the same
	 * hive twice in a single effect tick, hence the name "non-stacking" bee effect..
	 *
	 * @param level   The level where the housing is located
	 * @param housing The housing to perform the effect on. It is recommended to check
	 *                {@code IBeeHousing.getErrorLogic().hasErrors()} before performing the effect.
	 */
	protected abstract void doEffectForHive(Level level, IBeeHousing housing);
}

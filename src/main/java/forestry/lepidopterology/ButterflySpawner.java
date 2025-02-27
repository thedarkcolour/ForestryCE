/*******************************************************************************
 * Copyright (c) 2011-2014 SirSengir.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Various Contributors including, but not limited to:
 * SirSengir (original work), CovertJaguar, Player, Binnie, MysteriousAges
 ******************************************************************************/
package forestry.lepidopterology;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import forestry.Forestry;
import forestry.api.arboriculture.ILeafTickHandler;
import forestry.api.arboriculture.genetics.ITree;
import forestry.api.genetics.alleles.TreeChromosomes;
import forestry.api.lepidopterology.genetics.IButterfly;
import forestry.api.lepidopterology.genetics.IButterflySpecies;
import forestry.core.config.ForestryConfig;
import forestry.core.utils.SpeciesUtil;
import forestry.lepidopterology.entities.EntityButterfly;

public class ButterflySpawner implements ILeafTickHandler {
	static boolean attemptButterflySpawn(Level world, IButterfly butterfly, BlockPos pos) {
		Mob entityLiving = SpeciesUtil.BUTTERFLY_TYPE.get().spawnButterflyInWorld(world, butterfly.copy(), pos.getX(), pos.getY() + 0.1f, pos.getZ());
		Forestry.LOGGER.trace("Spawned a butterfly '{}' at {}/{}/{}.", butterfly.getDisplayName(), pos.getX(), pos.getY(), pos.getZ());
		return entityLiving != null;
	}

	@Override
	public boolean onRandomLeafTick(ITree tree, Level world, RandomSource rand, BlockPos pos, boolean isDestroyed) {
		if (!world.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING) || ForestryConfig.SERVER.disableButterflySpawning.get()) {
			return false;
		}

		if (rand.nextFloat() >= tree.getGenome().getActiveValue(TreeChromosomes.SAPPINESS) * tree.getGenome().getActiveValue(TreeChromosomes.YIELD)) {
			return false;
		}

		IButterfly spawn = SpeciesUtil.BUTTERFLY_TYPE.get().createRandomIndividual(rand);
		IButterflySpecies activeSpecies = spawn.getSpecies();
		float rarity = ModuleLepidopterology.spawnRarities.getOrDefault(activeSpecies.id().getPath(), activeSpecies.getRarity());

		if (rand.nextFloat() >= rarity * 0.5f) {
			return false;
		}

		if (EntityButterfly.isMaxButterflyCluster(Vec3.atCenterOf(pos), world)) {
			return false;
		}

		if (!spawn.canSpawn(world, pos.getX(), pos.getY(), pos.getZ())) {
			return false;
		}

		if (world.isEmptyBlock(pos.north())) {
			attemptButterflySpawn(world, spawn, pos.north());
		} else if (world.isEmptyBlock(pos.south())) {
			attemptButterflySpawn(world, spawn, pos.south());
		} else if (world.isEmptyBlock(pos.west())) {
			attemptButterflySpawn(world, spawn, pos.west());
		} else if (world.isEmptyBlock(pos.east())) {
			attemptButterflySpawn(world, spawn, pos.east());
		}

		return false;
	}

}

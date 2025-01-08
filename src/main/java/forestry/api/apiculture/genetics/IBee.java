/*******************************************************************************
 * Copyright 2011-2014 SirSengir
 *
 * This work (the API) is licensed under the "MIT" License, see LICENSE.txt for details.
 ******************************************************************************/
package forestry.api.apiculture.genetics;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import forestry.api.apiculture.IBeeHousing;
import forestry.api.core.IError;
import forestry.api.genetics.IEffectData;
import forestry.api.genetics.IGenome;
import forestry.api.genetics.IIndividualLiving;
import forestry.api.genetics.pollen.IPollen;

/**
 * Other implementations than Forestry's default one are not supported.
 */
public interface IBee extends IIndividualLiving {
	/**
	 * @return true if the individual is originally of pristine/natural origin.
	 */
	boolean isPristine();

	/**
	 * @return generation this individual is removed from the original individual.
	 */
	int getGeneration();

	/**
	 * Set the natural flag on this bee.
	 */
	void setPristine(boolean flag);

	IEffectData[] doEffect(IEffectData[] storedData, IBeeHousing housing);

	@OnlyIn(Dist.CLIENT)
	IEffectData[] doFX(IEffectData[] storedData, IBeeHousing housing);

	/**
	 * Determines whether the queen can work.
	 *
	 * @param housing the {@link IBeeHousing} the bee currently resides in.
	 * @return an empty set if the queen can work, a set of error states if the queen can not work
	 */
	Set<IError> getCanWork(IBeeHousing housing);

	List<Holder.Reference<Biome>> getSuitableBiomes(Registry<Biome> registry);

	/**
	 * @return A list of item stacks used for displaying this bee's products in the Analyzer GUI.
	 */
	List<ItemStack> getProduceList();

	/**
	 * @return A list of item stacks used for displaying this bee's specialty products in the Analyzer GUI.
	 */
	List<ItemStack> getSpecialtyList();

	List<ItemStack> produceStacks(IBeeHousing housing);

	@Nullable
	IBee spawnPrincess(IBeeHousing housing);

	List<IBee> spawnDrones(IBeeHousing housing);

	/**
	 * Plants a random flower from the given list near the housing
	 *
	 * @param housing          the location the bee is living
	 * @param potentialFlowers the flowers to choose from
	 * @return the position of the planted flower, or null of none were planted
	 */
	@Nullable
	BlockPos plantFlowerRandom(IBeeHousing housing, List<BlockState> potentialFlowers);

	@Nullable
	IPollen<?> retrievePollen(IBeeHousing housing);

	boolean pollinateRandom(IBeeHousing housing, IPollen<?> pollen);

	Iterator<BlockPos.MutableBlockPos> getAreaIterator(IBeeHousing housing);

	@Override
	IBee copy();

	@Override
	IBee copyWithGenome(IGenome newGenome);

	@Override
	IBeeSpeciesType getType();

	@Override
	IBeeSpecies getSpecies();

	@Override
	IBeeSpecies getInactiveSpecies();
}

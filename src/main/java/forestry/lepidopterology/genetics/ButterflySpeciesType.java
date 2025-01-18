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
package forestry.lepidopterology.genetics;

import com.google.common.collect.ImmutableMap;

import javax.annotation.Nullable;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;

import forestry.api.core.IProduct;
import forestry.api.genetics.ForestrySpeciesTypes;
import forestry.api.genetics.IAlyzerPlugin;
import forestry.api.genetics.IBreedingTracker;
import forestry.api.genetics.IIndividual;
import forestry.api.genetics.IMutationManager;
import forestry.api.genetics.alleles.ButterflyChromosomes;
import forestry.api.genetics.alleles.IKaryotype;
import forestry.api.genetics.capability.IIndividualHandlerItem;
import forestry.api.lepidopterology.IButterflyNursery;
import forestry.api.lepidopterology.ILepidopteristTracker;
import forestry.api.lepidopterology.genetics.ButterflyLifeStage;
import forestry.api.lepidopterology.genetics.IButterfly;
import forestry.api.lepidopterology.genetics.IButterflySpecies;
import forestry.api.lepidopterology.genetics.IButterflySpeciesType;
import forestry.api.plugin.IForestryPlugin;
import forestry.api.plugin.ISpeciesTypeBuilder;
import forestry.apiimpl.plugin.LepidopterologyRegistration;
import forestry.core.genetics.SpeciesType;
import forestry.core.genetics.root.BreedingTrackerManager;
import forestry.core.tiles.TileUtil;
import forestry.core.utils.BlockUtil;
import forestry.core.utils.EntityUtil;
import forestry.core.utils.SpeciesUtil;
import forestry.core.utils.TreeUtil;
import forestry.lepidopterology.ButterflySpawner;
import forestry.lepidopterology.ModuleLepidopterology;
import forestry.lepidopterology.blocks.BlockCocoon;
import forestry.lepidopterology.entities.EntityButterfly;
import forestry.lepidopterology.features.LepidopterologyBlocks;
import forestry.lepidopterology.features.LepidopterologyEntities;
import forestry.lepidopterology.tiles.TileCocoon;

public class ButterflySpeciesType extends SpeciesType<IButterflySpecies, IButterfly> implements IButterflySpeciesType {
	public ButterflySpeciesType(IKaryotype karyotype, ISpeciesTypeBuilder builder) {
		super(ForestrySpeciesTypes.BUTTERFLY, karyotype, builder);
	}

	@Override
	public Pair<ImmutableMap<ResourceLocation, IButterflySpecies>, IMutationManager<IButterflySpecies>> handleSpeciesRegistration(List<IForestryPlugin> plugins) {
		LepidopterologyRegistration registration = new LepidopterologyRegistration(this);

		for (IForestryPlugin plugin : plugins) {
			plugin.registerLepidopterology(registration);
		}

		ButterflyChromosomes.EFFECT.populate(registration.getEffects());
		ButterflyChromosomes.COCOON.populate(registration.getCocoons());

		return registration.buildAll();
	}

	@Override
	public void onSpeciesRegistered(ImmutableMap<ResourceLocation, IButterflySpecies> allSpecies, IMutationManager<IButterflySpecies> mutations) {
		super.onSpeciesRegistered(allSpecies, mutations);

		if (ModuleLepidopterology.spawnButterflysFromLeaves) {
			SpeciesUtil.TREE_TYPE.get().registerLeafTickHandler(new ButterflySpawner());
		}
	}

	@Override
	public boolean isMember(IIndividual individual) {
		return individual instanceof IButterfly;
	}

	@Override
	public EntityButterfly spawnButterflyInWorld(Level level, IButterfly butterfly, double x, double y, double z) {
		return EntityUtil.spawnEntity(level, EntityButterfly.create(LepidopterologyEntities.BUTTERFLY.entityType(), level, butterfly, BlockPos.containing(x, y, z)), x, y, z);
	}

	@Nullable
	@Override
	public BlockPos plantCocoon(LevelAccessor level, BlockPos coordinates, @Nullable IButterfly caterpillar, int age, boolean createNursery) {
		if (caterpillar == null) {
			return null;
		}

		BlockPos pos = getValidCocoonPos(level, coordinates, createNursery);
		if (pos == null) {
			return null;
		}
		// todo replace with cocoon allele
		BlockState state = LepidopterologyBlocks.COCOON.defaultState().setValue(BlockCocoon.AGE, age);
		boolean placed = level.setBlock(pos, state, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
		if (!placed) {
			return null;
		}

		Block block = level.getBlockState(pos).getBlock();
		if (!LepidopterologyBlocks.COCOON.blockEqual(block)) {
			return null;
		}

		TileCocoon cocoon = TileUtil.getTile(level, pos, TileCocoon.class);
		if (cocoon == null) {
			level.setBlock(pos, Blocks.AIR.defaultBlockState(), 18);
			return null;
		}

		cocoon.setCaterpillar(caterpillar);

		return pos;
	}

	@Nullable
	private static BlockPos getValidCocoonPos(LevelAccessor world, BlockPos pos, boolean createNursery) {
		if (isPositionValid(world, pos.below(), createNursery)) {
			return pos.below();
		}
		for (int tries = 0; tries < 3; tries++) {
			for (int y = 1; y < world.getRandom().nextInt(5); y++) {
				BlockPos coordinate = pos.offset(world.getRandom().nextInt(6) - 3, -y, world.getRandom().nextInt(6) - 3);
				if (isPositionValid(world, coordinate, createNursery)) {
					return coordinate;
				}
			}
		}

		return null;
	}

	public static boolean isPositionValid(LevelAccessor world, BlockPos pos, boolean createNursery) {
		BlockState blockState = world.getBlockState(pos);
		if (BlockUtil.canReplace(blockState, world, pos)) {
			BlockPos nurseryPos = pos.above();
			IButterflyNursery nursery = TreeUtil.getNursery(world, nurseryPos);
			if (nursery != null) {
				return true;
			} else if (createNursery && TreeUtil.canCreateNursery(world, nurseryPos)) {
				nursery = TreeUtil.getOrCreateNursery(world, nurseryPos, false);
				return nursery != null && nursery.getCaterpillar() == null;
			}
		}
		return false;
	}

	@Override
	public boolean isMated(ItemStack stack) {
		return IIndividualHandlerItem.filter(stack, individual -> individual instanceof IButterfly butterfly && butterfly.getMate() != null);
	}

	@Override
	public ILepidopteristTracker getBreedingTracker(LevelAccessor level, @Nullable GameProfile profile) {
		return BreedingTrackerManager.INSTANCE.getTracker(this, level, profile);
	}

	@Override
	public String getBreedingTrackerFile(@Nullable GameProfile profile) {
		return "LepidopteristTracker." + (profile == null ? "common" : profile.getId());
	}

	@Override
	public IBreedingTracker createBreedingTracker() {
		return new LepidopteristTracker();
	}

	@Override
	public void initializeBreedingTracker(IBreedingTracker tracker, @Nullable Level world, @Nullable GameProfile profile) {
		if (tracker instanceof LepidopteristTracker butterflyTracker) {
			butterflyTracker.setLevel(world);
			butterflyTracker.setUsername(profile);
		}
	}

	@Override
	public IAlyzerPlugin getAlyzerPlugin() {
		return ButterflyAlyzerPlugin.INSTANCE;
	}

	@Override
	public float getResearchSuitability(IButterflySpecies species, ItemStack stack) {
		for (IProduct product : species.getButterflyLoot()) {
			if (stack.is(product.item())) {
				return 1.0f;
			}
		}
		for (IProduct product : species.getCaterpillarProducts()) {
			if (stack.is(product.item())) {
				return 1.0f;
			}
		}
		return super.getResearchSuitability(species, stack);
	}

	@Override
	public List<ItemStack> getResearchBounty(IButterflySpecies species, Level level, GameProfile researcher, IButterfly individual, int bountyLevel) {
		List<ItemStack> bounty = super.getResearchBounty(species, level, researcher, individual, bountyLevel);
		bounty.add(individual.createStack(ButterflyLifeStage.SERUM));
		return bounty;
	}

	@Override
	public Codec<? extends IButterfly> getIndividualCodec() {
		return Butterfly.CODEC;
	}
}

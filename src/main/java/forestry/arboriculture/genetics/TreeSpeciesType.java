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
package forestry.arboriculture.genetics;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;

import forestry.api.arboriculture.IArboristTracker;
import forestry.api.arboriculture.ILeafTickHandler;
import forestry.api.arboriculture.ITreeSpecies;
import forestry.api.arboriculture.genetics.IFruit;
import forestry.api.arboriculture.genetics.ITree;
import forestry.api.arboriculture.genetics.ITreeSpeciesType;
import forestry.api.core.IProduct;
import forestry.api.genetics.ForestrySpeciesTypes;
import forestry.api.genetics.IAlyzerPlugin;
import forestry.api.genetics.IBreedingTracker;
import forestry.api.genetics.IBreedingTrackerHandler;
import forestry.api.genetics.IGenome;
import forestry.api.genetics.IIndividual;
import forestry.api.genetics.IMutationManager;
import forestry.api.genetics.alleles.IKaryotype;
import forestry.api.genetics.alleles.TreeChromosomes;
import forestry.api.plugin.IForestryPlugin;
import forestry.api.plugin.ISpeciesTypeBuilder;
import forestry.apiimpl.plugin.ArboricultureRegistration;
import forestry.arboriculture.PodFruit;
import forestry.arboriculture.blocks.BlockFruitPod;
import forestry.arboriculture.blocks.ForestryLeafType;
import forestry.arboriculture.features.ArboricultureBlocks;
import forestry.arboriculture.tiles.TileFruitPod;
import forestry.arboriculture.tiles.TileSapling;
import forestry.arboriculture.tiles.TileTreeContainer;
import forestry.core.ClientsideCode;
import forestry.core.genetics.SpeciesType;
import forestry.core.genetics.root.BreedingTrackerManager;
import forestry.core.tiles.TileUtil;
import forestry.core.utils.BlockUtil;

public class TreeSpeciesType extends SpeciesType<ITreeSpecies, ITree> implements ITreeSpeciesType, IBreedingTrackerHandler {
	// todo make both of these reloadable
	private final LinkedList<ILeafTickHandler> leafTickHandlers = new LinkedList<>();
	private final IdentityHashMap<BlockState, ITree> vanillaIndividuals = new IdentityHashMap<>();
	private final IdentityHashMap<Item, ITree> vanillaItems = new IdentityHashMap<>();

	public TreeSpeciesType(IKaryotype karyotype, ISpeciesTypeBuilder builder) {
		super(ForestrySpeciesTypes.TREE, karyotype, builder);
	}

	@Override
	public void onSpeciesRegistered(ImmutableMap<ResourceLocation, ITreeSpecies> allSpecies, IMutationManager<ITreeSpecies> mutations) {
		super.onSpeciesRegistered(allSpecies, mutations);

		this.vanillaIndividuals.clear();
		this.vanillaItems.clear();

		for (ITreeSpecies entry : allSpecies.values()) {
			ITree defaultIndividual = entry.createIndividual();

			for (BlockState state : entry.getVanillaLeafStates()) {
				this.vanillaIndividuals.put(state, defaultIndividual);
			}
			for (Item item : entry.getVanillaSaplingItems()) {
				this.vanillaItems.put(item, defaultIndividual);
			}
		}
		for (ForestryLeafType type : ForestryLeafType.allValues()) {
			ITreeSpecies species = allSpecies.get(type.getSpeciesId());

			if (species != null) {
				type.setSpecies(species);
			} else {
				throw new IllegalStateException("Invalid ForestryLeafType " + type.getSerializedName() + ": no tree species found with ID: " + type.getSpeciesId());
			}
		}
	}

	@Override
	public Pair<ImmutableMap<ResourceLocation, ITreeSpecies>, IMutationManager<ITreeSpecies>> handleSpeciesRegistration(List<IForestryPlugin> plugins) {
		ArboricultureRegistration registration = new ArboricultureRegistration(this);

		for (IForestryPlugin plugin : plugins) {
			plugin.registerArboriculture(registration);
		}

		// populate tree registry chromosomes
		TreeChromosomes.EFFECT.populate(registration.getEffects());
		TreeChromosomes.FRUIT.populate(registration.getFruits());

		return registration.buildAll();
	}

	@Override
	public ITree getTree(IGenome genome) {
		return new Tree(genome);
	}

	@Override
	public boolean isMember(IIndividual individual) {
		return individual instanceof ITree;
	}

	@Nullable
	@Override
	public ITree getTree(Level level, BlockPos pos) {
		if (level.getBlockEntity(pos) instanceof TileTreeContainer container) {
			return container.getTree();
		}
		return null;
	}

	@Nullable
	@Override
	public ITree getTree(BlockEntity tileEntity) {
		return tileEntity instanceof TileTreeContainer container ? container.getTree() : null;
	}

	@Override
	public boolean plantSapling(Level level, ITree tree, GameProfile owner, BlockPos pos) {
		BlockState state = ArboricultureBlocks.SAPLING_GE.defaultState();
		boolean placed = level.setBlockAndUpdate(pos, state);
		if (!placed) {
			return false;
		}

		BlockState blockState = level.getBlockState(pos);
		Block block = blockState.getBlock();
		if (!ArboricultureBlocks.SAPLING_GE.blockEqual(block)) {
			return false;
		}

		TileSapling sapling = TileUtil.getTile(level, pos, TileSapling.class);
		if (sapling == null) {
			level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
			return false;
		}

		sapling.setTree(tree.copy());
		sapling.getOwnerHandler().setOwner(owner);

		BlockUtil.sendPlaceSound(level, pos, blockState);

		return true;
	}

	@Override
	public boolean setFruitBlock(LevelAccessor level, IGenome genome, IFruit fruit, float yield, BlockPos pos) {
		Direction facing = BlockUtil.getValidPodFacing(level, pos, fruit.getLogTag());

		// todo make this not hardcoded to forestry pods
		if (facing != null && fruit instanceof PodFruit podFruit && ArboricultureBlocks.PODS.has(podFruit.getType())) {
			BlockFruitPod fruitPod = ArboricultureBlocks.PODS.get(podFruit.getType()).block();
			BlockState state = fruitPod.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, facing);
			boolean placed = level.setBlock(pos, state, 18);

			if (placed) {
				Block block = level.getBlockState(pos).getBlock();

				if (fruitPod == block) {
					TileFruitPod pod = TileUtil.getTile(level, pos, TileFruitPod.class);

					if (pod != null) {
						pod.setProperties(genome, fruit, yield);
						if (level.isClientSide()) {
							ClientsideCode.markForUpdate(pos);
						}
						return true;
					} else {
						level.setBlock(pos, Blocks.AIR.defaultBlockState(), 18);
						return false;
					}
				}
			}
		}
		return false;
	}

	@Override
	public IArboristTracker getBreedingTracker(LevelAccessor level, @Nullable GameProfile profile) {
		return BreedingTrackerManager.INSTANCE.getTracker(this, level, profile);
	}

	@Override
	public String getBreedingTrackerFile(@Nullable GameProfile profile) {
		return "ArboristTracker." + (profile == null ? "common" : profile.getId());
	}

	@Override
	public IBreedingTracker createBreedingTracker() {
		return new ArboristTracker();
	}

	@Override
	public void initializeBreedingTracker(IBreedingTracker tracker, @Nullable Level world, @Nullable GameProfile profile) {
		if (tracker instanceof ArboristTracker arboristTracker) {
			arboristTracker.setLevel(world);
			arboristTracker.setUsername(profile);
		}
	}

	@Override
	public void registerLeafTickHandler(ILeafTickHandler handler) {
		this.leafTickHandlers.add(handler);
	}

	@Override
	public Collection<ILeafTickHandler> getLeafTickHandlers() {
		return this.leafTickHandlers;
	}

	@Override
	public IAlyzerPlugin getAlyzerPlugin() {
		return TreeAlyzerPlugin.INSTANCE;
	}

	@Nullable
	@Override
	public ITree getVanillaIndividual(BlockState state) {
		return this.vanillaIndividuals.get(state);
	}

	@Nullable
	@Override
	public ITree getVanillaIndividual(Item item) {
		return this.vanillaItems.get(item);
	}

	@Override
	public Codec<? extends ITree> getIndividualCodec() {
		return Tree.CODEC;
	}

	@Override
	public float getResearchSuitability(ITreeSpecies species, ItemStack stack) {
		if (stack.isEmpty()) {
			return 0f;
		}
		IFruit fruit = species.getDefaultGenome().getActiveValue(TreeChromosomes.FRUIT);
		for (IProduct product : Iterables.concat(fruit.getProducts(), fruit.getSpecialty())) {
			if (stack.is(product.item())) {
				return 1f;
			}
		}
		return super.getResearchSuitability(species, stack);
	}
}

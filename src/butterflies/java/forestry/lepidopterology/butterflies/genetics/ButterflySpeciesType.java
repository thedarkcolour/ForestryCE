package forestry.lepidopterology.butterflies.genetics;

import com.google.common.collect.ImmutableMap;
import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Codec;
import forestry.api.core.IProduct;
import forestry.api.core.genetics.*;
import forestry.api.core.genetics.alleles.IKaryotype;
import forestry.api.core.genetics.capability.IIndividualHandlerItem;
import forestry.api.lepidopterology.IButterflyCocoon;
import forestry.api.lepidopterology.IButterflyEffect;
import forestry.api.lepidopterology.IButterflyNursery;
import forestry.api.lepidopterology.ILepidopteristTracker;
import forestry.api.lepidopterology.genetics.ButterflyLifeStage;
import forestry.api.lepidopterology.genetics.IButterfly;
import forestry.api.lepidopterology.genetics.IButterflySpecies;
import forestry.api.lepidopterology.genetics.IButterflySpeciesType;
import forestry.api.plugin.IForestryPlugin;
import forestry.api.plugin.ILepidopterologyRegistration;
import forestry.api.plugin.ISpeciesTypeBuilder;
import forestry.lepidopterology.plugin.LepidopterologyRegistration;
import forestry.core.engine.genetics.SpeciesType;
import forestry.core.engine.genetics.root.BreedingTrackerManager;
import forestry.core.platform.tile.TileUtil;
import forestry.core.platform.util.BlockUtil;
import forestry.core.platform.util.EntityUtil;
import forestry.core.platform.util.SpeciesUtil;
import forestry.arboriculture.trees.TreeUtil;
import forestry.lepidopterology.butterflies.ButterflySpawner;
import forestry.lepidopterology.ModuleLepidopterology;
import forestry.lepidopterology.cocoons.BlockCocoon;
import forestry.lepidopterology.entities.EntityButterfly;
import forestry.lepidopterology.features.LepidopterologyBlocks;
import forestry.lepidopterology.features.LepidopterologyEntities;
import forestry.lepidopterology.cocoons.TileCocoon;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class ButterflySpeciesType extends SpeciesType<IButterflySpecies, IButterfly> implements IButterflySpeciesType {
	// Reference-value registries backing the cocoon and butterfly_effect chromosomes.
	@Nullable
	private ImmutableMap<ResourceLocation, IButterflyCocoon> cocoons;
	@Nullable
	private ImmutableMap<ResourceLocation, IButterflyEffect> butterflyEffects;

	public ButterflySpeciesType(IKaryotype karyotype, ISpeciesTypeBuilder builder) {
		super(ForestrySpeciesTypes.BUTTERFLY, karyotype, builder);
	}

	@Override
	public IButterflyCocoon getCocoon(ResourceLocation id) {
		return getMapValue(this.cocoons, id, "cocoon");
	}

	@Nullable
	@Override
	public IButterflyCocoon getCocoonSafe(ResourceLocation id) {
		return getMapValueSafe(this.cocoons, id);
	}

	@Override
	public IButterflyEffect getButterflyEffect(ResourceLocation id) {
		return getMapValue(this.butterflyEffects, id, "butterfly effect");
	}

	@Nullable
	@Override
	public IButterflyEffect getButterflyEffectSafe(ResourceLocation id) {
		return getMapValueSafe(this.butterflyEffects, id);
	}

	@Override
	public ImmutableMap<ResourceLocation, IButterflySpecies> handleSpeciesRegistration(List<IForestryPlugin> plugins) {
		LepidopterologyRegistration registration = new LepidopterologyRegistration(this);

		for (IForestryPlugin plugin : plugins) {
			plugin.registerLepidopterology(registration);
		}

		// store the reference-value registries backing the cocoon and butterfly_effect chromosomes
		this.butterflyEffects = registration.getEffects();
		this.cocoons = registration.getCocoons();

		// Butterfly species now come solely from the butterfly_species datapack loader (see
		// LepidopterologyReloadHandler#rebuildButterflySpecies), populated by the real AddReloadListenerEvent/reload cycle
		// once setup completes - not from the code-side builders collected above.
		return ImmutableMap.of();
	}

	@Override
	public void onSpeciesRegistered(ImmutableMap<ResourceLocation, IButterflySpecies> allSpecies) {
		super.onSpeciesRegistered(allSpecies);

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

	@Override
	public DataProvider createSpeciesProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries, Consumer<ILepidopterologyRegistration> species) {
		return new ButterflySpeciesProvider(output, registries, this, species);
	}
}

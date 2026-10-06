package forestry.arboriculture.trees.genetics;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Codec;
import forestry.Forestry;
import forestry.api.IForestryApi;
import forestry.api.arboriculture.IArboristTracker;
import forestry.api.arboriculture.ILeafTickHandler;
import forestry.api.arboriculture.ITreeSpecies;
import forestry.api.arboriculture.genetics.IFruit;
import forestry.api.arboriculture.genetics.ITree;
import forestry.api.arboriculture.genetics.ITreeEffect;
import forestry.api.arboriculture.genetics.ITreeSpeciesType;
import forestry.api.core.IProduct;
import forestry.api.core.genetics.*;
import forestry.api.core.genetics.alleles.IKaryotype;
import forestry.api.core.genetics.alleles.TreeChromosomes;
import forestry.api.plugin.IForestryPlugin;
import forestry.api.plugin.ISpeciesTypeBuilder;
import forestry.apiimpl.ForestryApiImpl;
import forestry.arboriculture.plugin.ArboricultureRegistration;
import forestry.arboriculture.fruit.PodFruit;
import forestry.arboriculture.fruit.FruitPodBlock;
import forestry.arboriculture.leaves.ForestryLeafType;
import forestry.arboriculture.features.ArboricultureBlocks;
import forestry.arboriculture.fruit.FruitPodBlockEntity;
import forestry.arboriculture.sapling.SaplingBlockEntity;
import forestry.arboriculture.trees.TileTreeContainer;
import forestry.core.platform.client.ClientsideCode;
import forestry.core.engine.genetics.SpeciesType;
import forestry.core.engine.genetics.root.BreedingTrackerManager;
import forestry.core.platform.tile.TileUtil;
import forestry.core.platform.util.BlockUtil;
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

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;

public class TreeSpeciesType extends SpeciesType<ITreeSpecies, ITree> implements ITreeSpeciesType, IBreedingTrackerHandler {
	// todo make both of these reloadable
	private final LinkedList<ILeafTickHandler> leafTickHandlers = new LinkedList<>();
	private final IdentityHashMap<BlockState, ITree> vanillaIndividuals = new IdentityHashMap<>();
	private final IdentityHashMap<Item, ITree> vanillaItems = new IdentityHashMap<>();

	// Reference-value registries backing the fruits and tree_effect chromosomes.
	@Nullable
	private ImmutableMap<ResourceLocation, IFruit> fruits;
	@Nullable
	private ImmutableMap<ResourceLocation, ITreeEffect> treeEffects;

	// Code-side per-species block/worldgen bindings, keyed by species id. Captured from the DefaultTreeSpecies builders
	// at plugin registration (below); merged into runtime TreeSpecies by TreeSpeciesProjector. Never null after
	// handleSpeciesRegistration. Volatile: written on the registration thread, read from the reload/sync projection path.
	private volatile ImmutableMap<ResourceLocation, TreeBlockBindings> bindings = ImmutableMap.of();

	public TreeSpeciesType(IKaryotype karyotype, ISpeciesTypeBuilder builder) {
		super(ForestrySpeciesTypes.TREE, karyotype, builder);
	}

	@Override
	public IFruit getFruit(ResourceLocation id) {
		return getMapValue(this.fruits, id, "fruit");
	}

	@Nullable
	public TreeBlockBindings getBindings(ResourceLocation id) {
		return this.bindings.get(id);
	}

	@Nullable
	@Override
	public IFruit getFruitSafe(ResourceLocation id) {
		return getMapValueSafe(this.fruits, id);
	}

	@Override
	public ITreeEffect getTreeEffect(ResourceLocation id) {
		return getMapValue(this.treeEffects, id, "tree effect");
	}

	@Override
	public void setSpecies(ImmutableMap<ResourceLocation, ITreeSpecies> allSpecies) {
		super.setSpecies(allSpecies);
		rebuildVanillaMembership(allSpecies);
		rebuildLeafTypes(allSpecies);
	}

	private void rebuildVanillaMembership(ImmutableMap<ResourceLocation, ITreeSpecies> allSpecies) {
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
	}

	private void rebuildLeafTypes(ImmutableMap<ResourceLocation, ITreeSpecies> allSpecies) {
		// Fail-soft fallback for a leaf type whose species a datapack removed: use the default species so
		// ForestryLeafType#getIndividual() stays non-null whenever any species are loaded (its block-color/name/item
		// consumers deref it unguarded). Resolved via the map directly (not getDefaultSpecies(), which throws when the
		// default id itself is absent) so it is null only in the empty-at-setup window, where nothing renders yet.
		ITreeSpecies fallback = allSpecies.get(getKaryotype().getDefaultSpecies());
		for (ForestryLeafType type : ForestryLeafType.allValues()) {
			ITreeSpecies species = allSpecies.get(type.getSpeciesId());
			if (species == null) {
				species = fallback;
			}
			if (species != null) {
				type.setSpecies(species);
			} else {
				// Empty at setup before the datapack loads (no species at all, not even the default). Warn instead of
				// throwing and leave the back-ref for the datapack load to populate; nothing renders in this window.
				Forestry.LOGGER.warn("ForestryLeafType {} has no tree species with id {} and no default to fall back to (not yet loaded)", type.getSerializedName(), type.getSpeciesId());
			}
		}
	}

	@Override
	public ImmutableMap<ResourceLocation, ITreeSpecies> handleSpeciesRegistration(List<IForestryPlugin> plugins) {
		ArboricultureRegistration registration = new ArboricultureRegistration(this);

		for (IForestryPlugin plugin : plugins) {
			plugin.registerArboriculture(registration);
		}

		// store the reference-value registries backing the fruits and tree_effect chromosomes
		this.treeEffects = registration.getEffects();
		this.fruits = registration.getFruits();

		// capture the code-side block/worldgen bindings from the registered builders
		ImmutableMap.Builder<ResourceLocation, TreeBlockBindings> bindings = ImmutableMap.builder();
		registration.forEachSpeciesBuilder((id, builder) -> bindings.put(id, new TreeBlockBindings(
			builder.getGenerator(),
			builder.getVanillaLeafStates(),
			builder.getVanillaSaplingItems(),
			builder.getDecorativeLeaves()
		)));
		this.bindings = bindings.build();

		// initialize tree manager
		((ForestryApiImpl) IForestryApi.INSTANCE).setTreeManager(registration.buildTreeManager());

		// Tree species are no longer built at setup; they come exclusively from the tree_species datapack loader. The
		// companion reference registries (fruits/effects), the TreeManager, and the block/worldgen bindings above are
		// still captured here so projection can resolve them.
		return ImmutableMap.of();
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

	//TODO: Make it so this doesnt place saplings on top of blocks when you click on the side
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

		SaplingBlockEntity sapling = TileUtil.getTile(level, pos, SaplingBlockEntity.class);
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
			FruitPodBlock fruitPod = ArboricultureBlocks.PODS.get(podFruit.getType()).block();
			BlockState state = fruitPod.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, facing);
			boolean placed = level.setBlock(pos, state, 18);

			if (placed) {
				Block block = level.getBlockState(pos).getBlock();

				if (fruitPod == block) {
					FruitPodBlockEntity pod = TileUtil.getTile(level, pos, FruitPodBlockEntity.class);

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
		IFruit fruit = species.getDefaultGenome().resolveActive(TreeChromosomes.FRUIT);
		for (IProduct product : Iterables.concat(fruit.getProducts(), fruit.getSpecialty())) {
			if (stack.is(product.item())) {
				return 1f;
			}
		}
		return super.getResearchSuitability(species, stack);
	}
}

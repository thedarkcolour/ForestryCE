package forestry.apiimpl.plugin;

import com.google.common.base.Preconditions;
import forestry.api.arboriculture.ITreeGenData;
import forestry.api.arboriculture.ITreeGenerator;
import forestry.api.arboriculture.ITreeSpecies;
import forestry.api.arboriculture.IWoodType;
import forestry.api.arboriculture.genetics.ITreeSpeciesType;
import forestry.api.plugin.ITreeSpeciesBuilder;
import forestry.arboriculture.TreeSpecies;
import forestry.arboriculture.worldgen.DefaultTreeGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.function.Function;

public class TreeSpeciesBuilder extends SpeciesBuilder<ITreeSpeciesType, ITreeSpecies, ITreeSpeciesBuilder> implements ITreeSpeciesBuilder {
	@Nullable
	private IWoodType woodType = null;
	@Nullable
	private ITreeGenerator generator = null;
	private float rarity = 0.0f;
	private final HashSet<BlockState> vanillaStates = new HashSet<>();
	private final HashSet<Item> vanillaItems = new HashSet<>();
	private ItemStack decorativeLeaves = ItemStack.EMPTY;

	public TreeSpeciesBuilder(ResourceLocation id, String genus, String species, MutationsRegistration mutations) {
		super(id, genus, species, mutations);
	}

	@Override
	public ISpeciesFactory<ITreeSpeciesType, ITreeSpecies, ITreeSpeciesBuilder> createSpeciesFactory() {
		return TreeSpecies::new;
	}

	@Override
	public ITreeSpeciesBuilder setWoodType(IWoodType woodType) {
		this.woodType = woodType;
		return this;
	}

	@Override
	public ITreeSpeciesBuilder setTreeFeature(Function<ITreeSpecies, Feature<NoneFeatureConfiguration>> factory) {
		Preconditions.checkState(this.woodType != null, "Must call setWoodType before setTreeFeature");

		return setGenerator(new DefaultTreeGenerator(factory, this.woodType));
	}

	@Override
	public ITreeSpeciesBuilder setGenerator(ITreeGenerator generator) {
		this.generator = generator;
		return this;
	}

	@Override
	public ITreeSpeciesBuilder addVanillaStates(Collection<BlockState> states) {
		this.vanillaStates.addAll(states);
		return this;
	}

	@Override
	public ITreeSpeciesBuilder addVanillaSapling(Item sapling) {
		this.vanillaItems.add(sapling);
		return this;
	}

	@Override
	public ITreeSpeciesBuilder setRarity(float rarity) {
		Preconditions.checkArgument(0f <= rarity && rarity <= 1f, "Tree species rarity must be between 0 and 1.");
		this.rarity = rarity;
		return this;
	}

	@Override
	public ITreeSpeciesBuilder setDecorativeLeaves(ItemStack stack) {
		this.decorativeLeaves = stack;
		return this;
	}

	@Nullable
	@Override
	public ITreeGenerator getGenerator() {
		return this.generator;
	}

	@Override
	public List<BlockState> getVanillaLeafStates() {
		return List.copyOf(this.vanillaStates);
	}

	@Override
	public List<Item> getVanillaSaplingItems() {
		return List.copyOf(this.vanillaItems);
	}

	@Override
	public ItemStack getDecorativeLeaves() {
		return this.decorativeLeaves;
	}

	@Override
	public float getRarity() {
		return this.rarity;
	}
}

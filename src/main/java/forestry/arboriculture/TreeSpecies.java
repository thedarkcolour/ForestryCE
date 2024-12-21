package forestry.arboriculture;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;

import forestry.api.arboriculture.ITreeGenerator;
import forestry.api.arboriculture.ITreeSpecies;
import forestry.api.arboriculture.genetics.ITree;
import forestry.api.arboriculture.genetics.ITreeSpeciesType;
import forestry.api.arboriculture.genetics.TreeLifeStage;
import forestry.api.core.HumidityType;
import forestry.api.core.IProduct;
import forestry.api.core.TemperatureType;
import forestry.api.genetics.IGenome;
import forestry.api.genetics.ILifeStage;
import forestry.api.genetics.alleles.ForestryAlleles;
import forestry.api.genetics.alleles.TreeChromosomes;
import forestry.api.plugin.ITreeSpeciesBuilder;
import forestry.arboriculture.blocks.BlockDefaultLeavesFruit;
import forestry.arboriculture.features.ArboricultureBlocks;
import forestry.arboriculture.genetics.Tree;
import forestry.arboriculture.genetics.TreeGrowthHelper;
import forestry.arboriculture.tiles.TileLeaves;
import forestry.core.genetics.Species;

import org.jetbrains.annotations.Nullable;

public class TreeSpecies extends Species<ITreeSpeciesType, ITree> implements ITreeSpecies {
	private final TemperatureType temperature;
	private final HumidityType humidity;
	private final ITreeGenerator generator;
	private final List<BlockState> vanillaLeafStates;
	private final List<Item> vanillaSaplingItems;
	private final ItemStack decorativeLeaves;
	private final float rarity;

	public TreeSpecies(ResourceLocation id, ITreeSpeciesType speciesType, IGenome defaultGenome, ITreeSpeciesBuilder builder) {
		super(id, speciesType, defaultGenome, builder);

		this.temperature = builder.getTemperature();
		this.humidity = builder.getHumidity();
		// todo how to handle this being null?
		this.generator = builder.getGenerator();
		this.vanillaLeafStates = builder.getVanillaLeafStates();
		this.vanillaSaplingItems = builder.getVanillaSaplingItems();
		this.decorativeLeaves = builder.getDecorativeLeaves();
		this.rarity = builder.getRarity();
	}

	@Override
	public ITreeGenerator getGenerator() {
		return this.generator;
	}

	@Override
	public ItemStack getDecorativeLeaves() {
		return this.decorativeLeaves;
	}

	@Override
	public TemperatureType getTemperature() {
		return this.temperature;
	}

	@Override
	public HumidityType getHumidity() {
		return this.humidity;
	}

	@Override
	public List<BlockState> getVanillaLeafStates() {
		return this.vanillaLeafStates;
	}

	@Override
	public List<Item> getVanillaSaplingItems() {
		return this.vanillaSaplingItems;
	}

	@Override
	public int getGermlingColor(ILifeStage stage, int renderPass) {
		return stage == TreeLifeStage.POLLEN ? getEscritoireColor() : 0xffffff;
	}

	@Override
	public ITree createIndividual(IGenome genome) {
		return new Tree(genome);
	}

	@Override
	public int getEscritoireColor() {
		return this.escritoireColor;
	}

	@Override
	public float getRarity() {
		return this.rarity;
	}

	@Override
	public boolean isFruitLeaf(LevelAccessor level, BlockPos pos) {
		return level.getBlockState(pos).getBlock() instanceof BlockDefaultLeavesFruit || (level.getBlockEntity(pos) instanceof TileLeaves leaves && leaves.hasFruit());
	}

	@Override
	public float getHeightModifier(IGenome genome) {
		return genome.getActiveValue(TreeChromosomes.HEIGHT);
	}

	@Override
	public void addTooltip(ITree individual, List<Component> tooltip) {
		// No info 4 u!
		if (!individual.isAnalyzed()) {
			addUnknownGenomeTooltip(tooltip);
			return;
		}

		IGenome genome = individual.getGenome();

		// You analyzed it? Juicy tooltip coming up!
		addHybridTooltip(tooltip, genome, TreeChromosomes.SPECIES, "for.trees.hybrid");

		Component saplingsAndMaturation = Component.literal("S: ").append(genome.getActiveName(TreeChromosomes.SAPLINGS)).append(", ").withStyle(ChatFormatting.YELLOW)
				.append(Component.literal("M: ").append(genome.getActiveName(TreeChromosomes.MATURATION)).withStyle(ChatFormatting.RED));
		Component heightAndGirth = Component.literal("H: ").append(genome.getActiveName(TreeChromosomes.HEIGHT)).append(", ").withStyle(ChatFormatting.LIGHT_PURPLE)
				.append(Component.literal("G: ").append(genome.getActiveName(TreeChromosomes.GIRTH)).withStyle(ChatFormatting.AQUA));
		Component yieldAndSappiness = Component.literal("Y: ").append(genome.getActiveName(TreeChromosomes.YIELD)).append(", ").withStyle(ChatFormatting.WHITE)
				.append(Component.literal("S: ").append(genome.getActiveName(TreeChromosomes.SAPPINESS)).withStyle(ChatFormatting.GOLD));
		tooltip.add(saplingsAndMaturation);
		tooltip.add(heightAndGirth);
		tooltip.add(yieldAndSappiness);

		if (genome.getActiveValue(TreeChromosomes.FIREPROOF)) {
			tooltip.add(Component.translatable("for.gui.fireresist").withStyle(ChatFormatting.RED));
		}

		MutableComponent fruitAndEffect = null;
		if (genome.getActiveAllele(TreeChromosomes.FRUIT) != ForestryAlleles.FRUIT_NONE) {
			fruitAndEffect = Component.literal("F: ").append(genome.getActiveName(TreeChromosomes.FRUIT)).withStyle(ChatFormatting.GREEN);
		}
		if (genome.getActiveAllele(TreeChromosomes.EFFECT) != ForestryAlleles.TREE_EFFECT_NONE) {
			MutableComponent effect = Component.literal("E: ").append(genome.getActiveName(TreeChromosomes.EFFECT)).withStyle(ChatFormatting.DARK_AQUA);

			if (fruitAndEffect != null) {
				fruitAndEffect.append(Component.literal(", ")).append(effect);
			} else {
				fruitAndEffect = effect;
			}
		}
		if (fruitAndEffect != null) {
			tooltip.add(fruitAndEffect);
		}
	}

	@Nullable
	@Override
	public BlockPos getGrowthPos(IGenome genome, LevelAccessor level, BlockPos pos, int expectedGirth, int expectedHeight) {
		return TreeGrowthHelper.getGrowthPos(level, genome, pos, expectedGirth, expectedHeight);
	}

	@Override
	public int getGirth(IGenome genome) {
		return genome.getActiveValue(TreeChromosomes.GIRTH);
	}

	@Override
	public boolean setLeaves(IGenome genome, LevelAccessor level, BlockPos pos, RandomSource random, boolean convertBlockEntity) {
		if (convertBlockEntity) {
			BlockState state = LeavesBlock.updateDistance(ArboricultureBlocks.LEAVES.defaultState(), level, pos);
			boolean wasFruit = isFruitLeaf(level, pos);
			boolean placed = level.setBlock(pos, state, 19);

			if (placed) {
				if (level.getBlockEntity(pos) instanceof TileLeaves leaves) {
					Tree tree = new Tree(genome);
					leaves.setTree(tree);

					if (wasFruit) {
						leaves.setFruit(tree, true);
						// default fruits are fully ripe
						leaves.addRipeness(1);
						leaves.setChanged();
					}
				} else {
					level.setBlock(pos, Blocks.AIR.defaultBlockState(), 19);
				}
			}

			return false;
		} else {
			return getGenerator().setLeaves(genome, level, pos, random);
		}
	}

	@Override
	public boolean setLogBlock(IGenome genome, LevelAccessor level, BlockPos pos, Direction facing) {
		return getGenerator().setLogBlock(genome, level, pos, facing);
	}

	@Override
	public boolean allowsFruitBlocks(IGenome genome) {
		return genome.getActiveValue(TreeChromosomes.FRUIT).requiresFruitBlocks();
	}

	@Override
	public boolean trySpawnFruitBlock(LevelAccessor level, RandomSource rand, BlockPos pos) {
		return this.defaultGenome.getActiveValue(TreeChromosomes.FRUIT).trySpawnFruitBlock(this.defaultGenome, level, rand, pos);
	}

	@Override
	public List<IProduct> getProducts() {
		return this.defaultGenome.getActiveValue(TreeChromosomes.FRUIT).getProducts();
	}

	@Override
	public List<IProduct> getSpecialties() {
		return this.defaultGenome.getActiveValue(TreeChromosomes.FRUIT).getSpecialty();
	}
}

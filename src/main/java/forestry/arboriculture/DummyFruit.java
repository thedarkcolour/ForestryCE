package forestry.arboriculture;

import forestry.api.arboriculture.genetics.IFruit;
import forestry.api.arboriculture.genetics.ITreeSpeciesType;
import forestry.api.core.IProduct;
import forestry.api.genetics.IGenome;
import forestry.api.genetics.alleles.TreeChromosomes;
import forestry.core.utils.SpeciesUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;

import javax.annotation.Nullable;
import java.util.List;

public class DummyFruit implements IFruit {
	private final boolean dominant;

	public DummyFruit(boolean dominant) {
		this.dominant = dominant;
	}

	@Override
	public boolean isDominant() {
		return this.dominant;
	}

	@Override
	public List<ItemStack> getFruits(IGenome genome, Level level, int ripeningTime) {
		return List.of();
	}

	@Override
	public boolean requiresFruitBlocks() {
		return false;
	}

	@Override
	public boolean trySpawnFruitBlock(IGenome genome, LevelAccessor world, RandomSource rand, BlockPos pos) {
		return false;
	}

	@Override
	public int getColour(IGenome genome, BlockGetter world, BlockPos pos, int ripeningTime) {
		return 0xffffff;
	}

	@Override
	public int getDecorativeColor() {
		return 0xffffff;
	}

	@Override
	public boolean isFruitLeaf() {
		return false;
	}

	@Override
	public float getFruitChance(IGenome genome, LevelAccessor level) {
		ITreeSpeciesType treeRoot = SpeciesUtil.TREE_TYPE.get();
		if (treeRoot == null) {
			return 0f;
		}
		//float yieldModifier = treeRoot.getTreekeepingMode(level).getYieldModifier(genome, 1.0F);
		return genome.getActiveValue(TreeChromosomes.YIELD) * 2.5F;// * yieldModifier;
	}

	@Override
	public int getRipeningPeriod() {
		return 0;
	}

	@Override
	public List<IProduct> getProducts() {
		return List.of();
	}

	@Override
	public List<IProduct> getSpecialty() {
		return List.of();
	}

	@Nullable
	@Override
	public ResourceLocation getSprite(IGenome genome, BlockGetter world, BlockPos pos, int ripeningTime) {
		return getDecorativeSprite();
	}

}

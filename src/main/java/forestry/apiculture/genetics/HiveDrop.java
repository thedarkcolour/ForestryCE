package forestry.apiculture.genetics;

import forestry.api.apiculture.genetics.IBee;
import forestry.api.apiculture.genetics.IBeeSpecies;
import forestry.api.apiculture.hives.IHiveDrop;
import forestry.api.genetics.alleles.IAllele;
import forestry.api.genetics.alleles.IChromosome;
import forestry.core.utils.SpeciesUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HiveDrop implements IHiveDrop {
	private final ResourceLocation speciesId;
	private final double chance;
	private final List<ItemStack> bonus;
	private final double ignobleChance;
	private final Map<IChromosome<?>, IAllele> alleles;

	@Nullable
	private IBeeSpecies species;

	public HiveDrop(double chance, ResourceLocation speciesId, List<ItemStack> bonus, float ignobleChance, Map<IChromosome<?>, IAllele> alleles) {
		this.speciesId = speciesId;
		this.chance = chance;
		this.bonus = bonus;
		this.ignobleChance = ignobleChance;
		this.alleles = alleles;
	}

	@Override
	public IBee createIndividual(BlockGetter level, BlockPos pos) {
		if (this.species == null) {
			this.species = SpeciesUtil.getBeeSpecies(this.speciesId);
		}
		return this.species.createIndividual(this.alleles);
	}

	@Override
	public List<ItemStack> getExtraItems(BlockGetter level, BlockPos pos, int fortune) {
		ArrayList<ItemStack> result = new ArrayList<>();
		for (ItemStack stack : this.bonus) {
			result.add(stack.copy());
		}

		return result;
	}

	@Override
	public double getChance(BlockGetter level, BlockPos pos, int fortune) {
		return this.chance;
	}

	@Override
	public double getIgnobleChance(BlockGetter level, BlockPos pos, int fortune) {
		return this.ignobleChance;
	}
}

package forestry.apiimpl.plugin;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import forestry.api.apiculture.hives.IHive;
import forestry.api.apiculture.hives.IHiveDefinition;
import forestry.api.apiculture.hives.IHiveDrop;
import forestry.api.genetics.alleles.IAllele;
import forestry.api.genetics.alleles.IChromosome;
import forestry.api.plugin.IHiveBuilder;
import forestry.apiculture.genetics.HiveDrop;
import forestry.apiculture.hives.Hive;

public class HiveBuilder implements IHiveBuilder {
	private final IHiveDefinition definition;
	private final ArrayList<IHiveDrop> drops = new ArrayList<>();
	private float generationChance;

	public HiveBuilder(IHiveDefinition definition) {
		this.definition = definition;
		this.generationChance = definition.getGenChance();
	}

	@Override
	public IHiveBuilder addDrop(double chance, ResourceLocation speciesId, Supplier<List<ItemStack>> extraItems, float ignobleChance, Map<IChromosome<?>, IAllele> alleles) {
		this.drops.add(new HiveDrop(chance, speciesId, extraItems.get(), ignobleChance, alleles));
		return this;
	}

	@Override
	public IHiveBuilder addCustomDrop(IHiveDrop drop) {
		this.drops.add(drop);
		return this;
	}

	@Override
	public void setGenerationChance(float generationChance) {
		this.generationChance = generationChance;
	}

	public IHive build() {
		return new Hive(this.definition, this.generationChance, ImmutableList.copyOf(drops));
	}
}

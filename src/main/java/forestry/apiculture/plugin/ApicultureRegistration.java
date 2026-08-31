package forestry.apiculture.plugin;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import forestry.api.apiculture.IActivityType;
import forestry.api.apiculture.IBeeJubilance;
import forestry.api.apiculture.genetics.IBeeEffect;
import forestry.api.apiculture.genetics.IBeeSpecies;
import forestry.api.apiculture.hives.IHiveDefinition;
import forestry.api.core.genetics.ISpeciesType;
import forestry.api.core.genetics.alleles.Allele;
import forestry.api.core.genetics.alleles.IChromosome;
import forestry.api.plugin.IApicultureRegistration;
import forestry.api.plugin.IBeeSpeciesBuilder;
import forestry.api.plugin.IHiveBuilder;
import forestry.api.apiculture.hives.VillageHive;
import forestry.apiculture.hives.HiveManager;
import forestry.apiimpl.plugin.ModifiableRegistrar;
import forestry.apiimpl.plugin.Registrar;
import forestry.apiimpl.plugin.SpeciesRegistration;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.Map;
import java.util.function.Consumer;

public class ApicultureRegistration extends SpeciesRegistration<IBeeSpeciesBuilder, IBeeSpecies, BeeSpeciesBuilder> implements IApicultureRegistration {
	private final ModifiableRegistrar<ResourceLocation, IHiveBuilder, HiveBuilder> hives = new ModifiableRegistrar<>(IHiveBuilder.class);
	private final Registrar<ResourceLocation, IBeeEffect, IBeeEffect> beeEffects = new Registrar<>(IBeeEffect.class);
	private final Registrar<ResourceLocation, IBeeJubilance, IBeeJubilance> jubilances = new Registrar<>(IBeeJubilance.class);
	private final Registrar<ResourceLocation, IActivityType, IActivityType> activityTypes = new Registrar<>(IActivityType.class);
	private final ArrayList<VillageHive> commonVillageHives = new ArrayList<>();
	private final ArrayList<VillageHive> rareVillageHives = new ArrayList<>();
	private final Object2FloatOpenHashMap<Item> swarmerMaterials = new Object2FloatOpenHashMap<>();

	public ApicultureRegistration(ISpeciesType<IBeeSpecies, ?> type) {
		super(type);
	}

	@Override
	protected BeeSpeciesBuilder createSpeciesBuilder(ResourceLocation id, String genus, String species) {
		return new BeeSpeciesBuilder(id, genus, species);
	}

	@Override
	public IBeeSpeciesBuilder registerSpecies(ResourceLocation id, String genus, String species, boolean dominant, TextColor outline) {
		return register(id, genus, species)
			.setDominant(dominant)
			.setOutline(outline);
	}

	@Override
	public void addVillageBee(ResourceLocation speciesId, boolean rare, Map<IChromosome<?>, Allele<?>> alleles) {
		(rare ? this.rareVillageHives : this.commonVillageHives).add(new VillageHive(speciesId, alleles));
	}

	@Override
	public void registerBeeEffect(ResourceLocation id, IBeeEffect effect) {
		this.beeEffects.create(id, effect);
	}

	public ImmutableMap<ResourceLocation, IBeeEffect> getBeeEffects() {
		return this.beeEffects.build();
	}

	@Override
	public void registerBeeJubilance(ResourceLocation id, IBeeJubilance jubilance) {
		this.jubilances.create(id, jubilance);
	}

	public ImmutableMap<ResourceLocation, IBeeJubilance> getJubilances() {
		return this.jubilances.build();
	}

	@Override
	public void registerActivityType(ResourceLocation id, IActivityType type) {
		this.activityTypes.create(id, type);
	}

	public ImmutableMap<ResourceLocation, IActivityType> getActivityTypes() {
		return this.activityTypes.build();
	}

	@Override
	public IHiveBuilder registerHive(ResourceLocation id, IHiveDefinition definition) {
		return this.hives.create(id, new HiveBuilder(definition));
	}

	@Override
	public void modifyHive(ResourceLocation id, Consumer<IHiveBuilder> builder) {
		this.hives.modify(id, builder);
	}

	public HiveManager buildHiveManager() {
		return new HiveManager(this.hives.build(HiveBuilder::build), ImmutableList.copyOf(this.commonVillageHives), ImmutableList.copyOf(this.rareVillageHives));
	}
}

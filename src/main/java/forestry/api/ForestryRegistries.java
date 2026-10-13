package forestry.api;

import com.mojang.serialization.MapCodec;

import forestry.api.apiculture.FlowerTypeType;
import forestry.api.apiculture.IBeeJubilance;
import forestry.api.apiculture.IFlowerType;
import forestry.api.apiculture.genetics.IBeeEffect;
import forestry.api.core.FluidProductType;
import forestry.api.core.IFluidProduct;
import forestry.api.core.IProduct;
import forestry.api.core.ProductType;
import forestry.api.core.circuits.ICircuit;
import forestry.api.core.genetics.IMutationCondition;
import forestry.api.core.genetics.ISpeciesType;
import forestry.api.core.genetics.MutationConditionType;
import forestry.api.mail.IPostalCarrier;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.RegistryBuilder;

public class ForestryRegistries {
	public static final Registry<ICircuit> CIRCUIT = new RegistryBuilder<>(Keys.CIRCUIT_TYPE)
		.sync(true)
		.create();

	public static final Registry<IPostalCarrier> POSTAL_CARRIER = new RegistryBuilder<>(Keys.POSTAL_CARRIER)
		.sync(true)
		.create();

	public static final Registry<ISpeciesType<?, ?>> SPECIES_TYPE = new RegistryBuilder<>(Keys.SPECIES_TYPE)
		.sync(true)
		.create();

	public static final Registry<MapCodec<? extends IBeeEffect>> BEE_EFFECT_TYPE = new RegistryBuilder<>(Keys.BEE_EFFECT_TYPE)
		.create();

	public static final Registry<MapCodec<? extends IBeeJubilance>> BEE_JUBILANCE_TYPE = new RegistryBuilder<>(Keys.BEE_JUBILANCE_TYPE)
		.create();

	public static final Registry<MutationConditionType<?>> MUTATION_CONDITION_TYPE = new RegistryBuilder<>(Keys.MUTATION_CONDITION_TYPE)
		.sync(true)
		.create();

	public static final Registry<ProductType<?>> PRODUCT_TYPE = new RegistryBuilder<>(Keys.PRODUCT_TYPE)
		.sync(true)
		.create();

	public static final Registry<FluidProductType<?>> FLUID_PRODUCT_TYPE = new RegistryBuilder<>(Keys.FLUID_PRODUCT_TYPE)
		.sync(true)
		.create();

	public static final Registry<FlowerTypeType<?>> FLOWER_TYPE_SERIALIZER = new RegistryBuilder<>(Keys.FLOWER_TYPE_SERIALIZER)
		.sync(true)
		.create();

	public static class Keys {
		public static final ResourceKey<Registry<ICircuit>> CIRCUIT_TYPE = ResourceKey.createRegistryKey(ForestryConstants.forestry("circuit"));
		public static final ResourceKey<Registry<IPostalCarrier>> POSTAL_CARRIER = ResourceKey.createRegistryKey(ForestryConstants.forestry("postal_carrier"));
		public static final ResourceKey<Registry<ISpeciesType<?, ?>>> SPECIES_TYPE = ResourceKey.createRegistryKey(ForestryConstants.forestry("species_type"));
		public static final ResourceKey<Registry<MapCodec<? extends IBeeEffect>>> BEE_EFFECT_TYPE = ResourceKey.createRegistryKey(ForestryConstants.forestry("bee_effect_type"));
		public static final ResourceKey<Registry<MapCodec<? extends IBeeJubilance>>> BEE_JUBILANCE_TYPE = ResourceKey.createRegistryKey(ForestryConstants.forestry("bee_jubilance_type"));
		public static final ResourceKey<Registry<MutationConditionType<?>>> MUTATION_CONDITION_TYPE = ResourceKey.createRegistryKey(ForestryConstants.forestry("mutation_condition_type"));
		public static final ResourceKey<Registry<ProductType<?>>> PRODUCT_TYPE = ResourceKey.createRegistryKey(ForestryConstants.forestry("product_type"));
		public static final ResourceKey<Registry<FluidProductType<?>>> FLUID_PRODUCT_TYPE = ResourceKey.createRegistryKey(ForestryConstants.forestry("fluid_product_type"));
		public static final ResourceKey<Registry<FlowerTypeType<?>>> FLOWER_TYPE_SERIALIZER = ResourceKey.createRegistryKey(ForestryConstants.forestry("flower_type_serializer"));
	}
}

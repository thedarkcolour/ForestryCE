package forestry.core.fluids;

import forestry.api.ForestryConstants;
import forestry.api.modules.ForestryModuleIds;
import forestry.core.ForestryColors;
import forestry.core.items.definitions.DrinkProperties;
import forestry.core.utils.ModUtil;
import forestry.modules.features.*;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.UnaryOperator;

@FeatureProvider
public enum ForestryFluids {
	BIO_ETHANOL(properties -> properties
		.particleColor(ForestryColors.color(255, 111, 0))
		.density(790)
		.viscosity(1000)
		.flammability(300)
		.spreadsFire()),
	BIOMASS(properties -> properties
		.particleColor(ForestryColors.color(100, 132, 41))
		.density(400)
		.viscosity(6560)
		.flammability(100)),
	GLASS(properties -> properties
		.particleColor(ForestryColors.color(164, 164, 164))
		.density(2400)
		.viscosity(10000)
		.flammability(0)
		.spreadsFire()
		.temperature(1400)),
	HONEY(properties -> properties
		.particleColor(ForestryColors.color(255, 196, 35))
		.density(1420)
		.viscosity(75600)
		.drinkProperties(2, 0.2f, 64)
	),
	ICE(properties -> properties
		.particleColor(ForestryColors.color(175, 242, 255))
		.density(520)
		.viscosity(1000)
		.temperature(265)),
	JUICE(properties -> properties
		.particleColor(ForestryColors.color(168, 201, 114))
		.drinkProperties(2, 0.2f, 32)
	),
	SEED_OIL(properties -> properties
		.particleColor(ForestryColors.color(255, 255, 168))
		.density(885)
		.viscosity(5000)
		.spreadsFire()
		.flammability(2)),
	SHORT_MEAD(properties -> properties
		.particleColor(ForestryColors.color(239, 154, 56))
		.density(1000)
		.viscosity(1200)
		.spreadsFire()
		.flammability(4)
		.drinkProperties(1, 0.2f, 32)
	);

	private static final Map<ResourceLocation, ForestryFluids> BY_ID = new HashMap<>();

	static {
		for (ForestryFluids fluidDefinition : ForestryFluids.values()) {
			BY_ID.put(ForestryConstants.forestry(fluidDefinition.feature.getName()), fluidDefinition);
		}
	}

	private final ResourceLocation tag;
	private final FeatureFluid feature;
	private final FeatureItem<BucketItem> bucket;

	ForestryFluids(UnaryOperator<FeatureFluid.Builder> properties) {
		IFeatureRegistry registry = ModFeatureRegistry.get(ForestryModuleIds.CORE);
		this.feature = properties.apply(registry
				.fluid(name().toLowerCase(Locale.ENGLISH)))
			.bucket(this::getBucket)
			.create();
		this.bucket = registry
			.item(() -> new BucketItem(getFluid(), new Item.Properties()
					.craftRemainder(Items.BUCKET)
					.stacksTo(1)),
				"bucket_" + name().toLowerCase(Locale.ENGLISH)
			);
		this.tag = ForestryConstants.forestry(this.feature.getName());
	}

	public int getTemperature() {
		return 295;
	}

	public final ResourceLocation getTag() {
		return this.tag;
	}

	public FeatureFluid getFeature() {
		return this.feature;
	}

	public BucketItem getBucket() {
		return this.bucket.item();
	}

	public final Fluid getFluid() {
		return this.feature.fluid();
	}

	public final Fluid getFlowing() {
		return this.feature.flowing();
	}

	public final FluidStack getFluid(int mb) {
		Fluid fluid = getFluid();
		return new FluidStack(fluid, mb);
	}

	public final SizedFluidIngredient ingredient(int mb) {
		return SizedFluidIngredient.of(getFluid(), mb);
	}

	public final int getParticleColor() {
		return this.feature.properties().particleColor;
	}

	public final boolean is(Fluid fluid) {
		return getFluid() == fluid;
	}

	public final boolean is(FluidStack fluidStack) {
		return getFluid() == fluidStack.getFluid();
	}

	public static boolean areEqual(@Nullable Fluid fluid, FluidStack fluidStack) {
		return fluid == fluidStack.getFluid();
	}

	@Nullable
	public static ForestryFluids getFluidDefinition(Fluid fluid) {
		return BY_ID.get(ModUtil.getRegistryName(fluid));
	}

	/**
	 * Get the properties for an ItemFluidContainerForestry before it gets registered.
	 */
	@Nullable
	public DrinkProperties getDrinkProperties() {
		return this.feature.properties().properties;
	}

	@SuppressWarnings("deprecation")
	public Holder<Fluid> holder() {
		return getFluid().builtInRegistryHolder();
	}
}

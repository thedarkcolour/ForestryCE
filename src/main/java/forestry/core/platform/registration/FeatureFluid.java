package forestry.core.platform.registration;

import forestry.core.platform.client.ForestryColors;
import forestry.core.platform.fluids.ForestryFluidBlock;
import forestry.core.platform.item.DrinkProperties;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class FeatureFluid extends ModFeature implements IFluidFeature {
	private final IBlockFeature<ForestryFluidBlock, BlockItem> block;
	private final FluidProperties properties;
	private final BaseFlowingFluid.Properties internal;

	private final DeferredHolder<Fluid, ? extends FlowingFluid> fluidObject;
	private final DeferredHolder<Fluid, ? extends FlowingFluid> flowingFluidObject;

	public FeatureFluid(Builder builder) {
		super(builder.moduleId, builder.identifier);
		this.block = builder.registry.block((properties) -> new ForestryFluidBlock(properties, this), "fluid_" + builder.identifier);
		this.properties = new FluidProperties(builder);
		DeferredHolder<FluidType, FluidType> attributes = builder.registry.getRegistry(NeoForgeRegistries.Keys.FLUID_TYPES).register(this.name, () -> new ForestryFluidType(this.properties, FluidType.Properties.create()
			.density(this.properties.density)
			.viscosity(this.properties.viscosity)
			.temperature(this.properties.temperature)));
		DeferredRegister<Fluid> fluidRegistry = builder.registry.getRegistry(Registries.FLUID);
		this.internal = new BaseFlowingFluid.Properties(attributes, this::fluid, this::flowing).block(this.block::block).bucket(properties().bucket);
		this.fluidObject = fluidRegistry.register(this.name, () -> new BaseFlowingFluid.Source(this.internal));
		this.flowingFluidObject = fluidRegistry.register(this.name + "_flowing", () -> new BaseFlowingFluid.Flowing(this.internal));
	}

	@Override
	public ResourceKey<? extends Registry<?>> getRegistry() {
		return Registries.FLUID;
	}

	@Override
	public IBlockFeature<ForestryFluidBlock, BlockItem> fluidBlock() {
		return this.block;
	}

	@Override
	public FlowingFluid fluid() {
		return this.fluidObject.get();
	}

	@Override
	public FlowingFluid flowing() {
		return this.flowingFluidObject.get();
	}

	@Override
	public FluidProperties properties() {
		return this.properties;
	}

	public static class Builder {
		final FeatureRegistry registry;
		private final ResourceLocation moduleId;
		final String identifier;

		int density = 1000;
		int viscosity = 1000;
		int temperature = 295;
		int particleColor = ForestryColors.WHITE;
		int flammability = 0;
		int luminosity = 0;
		boolean spreadsFire = false;
		@Nullable
		DrinkProperties properties = null;
		Supplier<Item> bucket = () -> Items.AIR;

		public Builder(FeatureRegistry registry, ResourceLocation moduleId, String identifier) {
			this.registry = registry;
			this.moduleId = moduleId;
			this.identifier = identifier;
		}

		public Builder spreadsFire() {
			this.spreadsFire = true;
			return this;
		}

		public Builder luminosity(int luminosity) {
			this.luminosity = Mth.clamp(luminosity, 0, 15);
			return this;
		}

		public Builder flammability(int flammability) {
			this.flammability = flammability;
			return this;
		}

		public Builder density(int density) {
			this.density = density;
			return this;
		}

		public Builder viscosity(int viscosity) {
			this.viscosity = viscosity;
			return this;
		}

		// Temperatures are in Kelvin
		public Builder temperature(int temperature) {
			this.temperature = temperature;
			return this;
		}

		public Builder particleColor(int color) {
			this.particleColor = color;
			return this;
		}

		public Builder bucket(Supplier<Item> bucket) {
			this.bucket = bucket;
			return this;
		}

		public Builder drinkProperties(int healAmount, float saturationModifier, int maxItemUseDuration) {
			this.properties = new DrinkProperties(healAmount, saturationModifier, maxItemUseDuration);
			return this;
		}

		public FeatureFluid create() {
			return this.registry.register(new FeatureFluid(this));
		}
	}

	public static class ForestryFluidType extends FluidType {
		private final int color;
		private final ResourceLocation stillTexture;
		private final ResourceLocation flowingTexture;

		public ForestryFluidType(FluidProperties forestryProps, Properties properties) {
			super(properties);
			this.color = forestryProps.particleColor;
			this.stillTexture = forestryProps.resources[0];
			this.flowingTexture = forestryProps.resources[1];
		}

		public int getColor() {
			return this.color;
		}

		public ResourceLocation getStillTexture() {
			return this.stillTexture;
		}

		public ResourceLocation getFlowingTexture() {
			return FluidProperties.resourceExists(this.flowingTexture) ? this.flowingTexture : this.stillTexture;
		}
	}
}

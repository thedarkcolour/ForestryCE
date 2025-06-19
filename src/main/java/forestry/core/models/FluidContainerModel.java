package forestry.core.models;

import com.google.common.collect.Maps;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import forestry.core.utils.ModUtil;
import forestry.storage.client.FilledCrateModel;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.model.CompositeModel;
import net.neoforged.neoforge.client.model.DynamicFluidContainerModel;
import net.neoforged.neoforge.client.model.QuadTransformers;
import net.neoforged.neoforge.client.model.geometry.*;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;
import java.util.function.Function;

// fixes issue in the DynamicFluidContainerModel where fluids have edges
public class FluidContainerModel implements IUnbakedGeometry<FluidContainerModel> {
	public static final ItemColor DYNAMIC_COLOR = new DynamicFluidContainerModel.Colors();

	private final Fluid fluid;
	private final boolean coverIsMask;
	private final boolean applyFluidLuminosity;

	public FluidContainerModel(Fluid fluid, boolean coverIsMask, boolean applyFluidLuminosity) {
		this.fluid = fluid;
		this.coverIsMask = coverIsMask;
		this.applyFluidLuminosity = applyFluidLuminosity;
	}

	public FluidContainerModel withFluid(Fluid newFluid) {
		return new FluidContainerModel(newFluid, this.coverIsMask, this.applyFluidLuminosity);
	}

	// Note: The fluid mask is ignored, the fluid element is always from (4, 2) to (12, 14).
	@Override
	public BakedModel bake(IGeometryBakingContext context, ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides overrides) {
		Material baseLocation = context.hasMaterial("base") ? context.getMaterial("base") : null;
		Material fluidMaskLocation = context.hasMaterial("fluid") ? context.getMaterial("fluid") : null;
		Material coverLocation = context.hasMaterial("cover") ? context.getMaterial("cover") : null;
		TextureAtlasSprite baseSprite = baseLocation != null ? spriteGetter.apply(baseLocation) : null;
		TextureAtlasSprite fluidSprite = this.fluid != Fluids.EMPTY ? spriteGetter.apply(ClientHooks.getBlockMaterial(IClientFluidTypeExtensions.of(this.fluid).getStillTexture())) : null;
		TextureAtlasSprite coverSprite = (coverLocation != null && (!this.coverIsMask || baseLocation != null)) ? spriteGetter.apply(coverLocation) : null;

		TextureAtlasSprite particleSprite = fluidSprite;
		if (particleSprite == null && baseSprite != null) {
			particleSprite = baseSprite;
		} else if (!this.coverIsMask) {
			particleSprite = coverSprite;
		}

		// todo is this modelLocation correct?
		var itemContext = StandaloneGeometryBakingContext.builder(context).withGui3d(false).withUseBlockLight(false).build(ResourceLocation.fromNamespaceAndPath("neoforge", "dynamic_fluid_container"));
		var modelBuilder = CompositeModel.Baked.builder(itemContext, particleSprite, new ContainedFluidOverrideHandler(baker, itemContext, this), context.getTransforms());
		var normalRenderTypes = DynamicFluidContainerModel.getLayerRenderTypes(false);

		if (baseLocation != null && baseSprite != null) {
			var baseElement = UnbakedGeometryHelper.createUnbakedItemElements(0, baseSprite);
			var quads = UnbakedGeometryHelper.bakeElements(baseElement, $ -> baseSprite, modelState);
			modelBuilder.addQuads(normalRenderTypes, quads);
		}

		// Fluid layer
		if (fluidMaskLocation != null && fluidSprite != null) {
			// no edges
			var fluidElement = Collections.singletonList(FilledCrateModel.make2dElement(1, 4, 2, 12, 14, -0.002f));
			var quads = UnbakedGeometryHelper.bakeElements(fluidElement, $ -> fluidSprite, modelState);

			var emissive = this.applyFluidLuminosity && this.fluid.getFluidType().getLightLevel() > 0;
			var renderTypes = DynamicFluidContainerModel.getLayerRenderTypes(emissive);
			if (emissive) {
				QuadTransformers.settingMaxEmissivity().processInPlace(quads);
			}

			modelBuilder.addQuads(renderTypes, quads);
		}

		if (coverSprite != null) {
			var sprite = this.coverIsMask ? baseSprite : coverSprite;
			if (sprite != null) {
				// no edges
				var coverElement = Collections.singletonList(FilledCrateModel.make2dElement(2, 0, 0, 16, 16, 0.002f)); // Use cover as mask
				var quads = UnbakedGeometryHelper.bakeElements(coverElement, $ -> sprite, modelState); // Bake with selected texture
				modelBuilder.addQuads(normalRenderTypes, quads);
			}
		}

		modelBuilder.setParticle(particleSprite);

		return modelBuilder.build();
	}

	public enum Loader implements IGeometryLoader<FluidContainerModel> {
		INSTANCE;

		@Override
		public FluidContainerModel read(JsonObject jsonObject, JsonDeserializationContext deserializationContext) {
			boolean coverIsMask = GsonHelper.getAsBoolean(jsonObject, "cover_is_mask", true);
			boolean applyFluidLuminosity = GsonHelper.getAsBoolean(jsonObject, "apply_fluid_luminosity", true);

			// create new model with correct liquid
			return new FluidContainerModel(Fluids.EMPTY, coverIsMask, applyFluidLuminosity);
		}
	}

	private static final class ContainedFluidOverrideHandler extends ItemOverrides {
		private final Map<String, BakedModel> cache = Maps.newHashMap();
		private final ModelBaker bakery;
		private final IGeometryBakingContext owner;
		private final FluidContainerModel parent;

		private ContainedFluidOverrideHandler(ModelBaker bakery, IGeometryBakingContext owner, FluidContainerModel parent) {
			this.bakery = bakery;
			this.owner = owner;
			this.parent = parent;
		}

		@Override
		public BakedModel resolve(BakedModel originalModel, ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed) {
			return FluidUtil.getFluidContained(stack)
				.map(fluidStack -> {
					Fluid fluid = fluidStack.getFluid();
					String name = ModUtil.getRegistryName(fluid).toString();

					if (!this.cache.containsKey(name)) {
						FluidContainerModel unbaked = this.parent.withFluid(fluid);
						BakedModel bakedModel = unbaked.bake(this.owner, this.bakery, Material::sprite, BlockModelRotation.X0_Y0, this, ResourceLocation.fromNamespaceAndPath(NeoForgeVersion.MOD_ID, "bucket_override"));
						this.cache.put(name, bakedModel);
						return bakedModel;
					}

					return this.cache.get(name);
				})
				// not a fluid item apparently
				.orElse(originalModel); // empty bucket
		}
	}
}

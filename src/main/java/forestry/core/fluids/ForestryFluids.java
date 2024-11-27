/*******************************************************************************
 * Copyright (c) 2011-2014 SirSengir.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Various Contributors including, but not limited to:
 * SirSengir (original work), CovertJaguar, Player, Binnie, MysteriousAges
 ******************************************************************************/
package forestry.core.fluids;

import javax.annotation.Nullable;
import java.awt.Color;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.UnaryOperator;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import net.minecraftforge.fluids.FluidStack;

import forestry.api.ForestryConstants;
import forestry.api.modules.ForestryModuleIds;
import forestry.core.items.definitions.DrinkProperties;
import forestry.core.utils.ModUtil;
import forestry.modules.features.FeatureFluid;
import forestry.modules.features.FeatureItem;
import forestry.modules.features.FeatureProvider;
import forestry.modules.features.IFeatureRegistry;
import forestry.modules.features.ModFeatureRegistry;

@FeatureProvider
public enum ForestryFluids {
	BIO_ETHANOL(properties -> properties
			.particleColor(new Color(255, 111, 0))
			.density(790)
			.viscosity(1000)
			.flammability(300)
			.spreadsFire()),
	BIOMASS(properties -> properties
			.particleColor(new Color(100, 132, 41))
			.density(400)
			.viscosity(6560)
			.flammability(100)),
	GLASS(properties -> properties
			.particleColor(new Color(164, 164, 164))
			.density(2400)
			.viscosity(10000)
			.flammability(0)
			.spreadsFire()
			.temperature(1400)),
	HONEY(properties -> properties
			.particleColor(new Color(255, 196, 35))
			.density(1420)
			.viscosity(75600)
			.drinkProperties(2, 0.2f, 64)
	),
	ICE(properties -> properties
			.particleColor(new Color(175, 242, 255))
			.density(520)
			.viscosity(1000)
			.temperature(265)),
	JUICE(properties -> properties
			.particleColor(new Color(168, 201, 114))
			.drinkProperties(2, 0.2f, 32)
	),
	SEED_OIL(properties -> properties
			.particleColor(new Color(255, 255, 168))
			.density(885)
			.viscosity(5000)
			.spreadsFire()
			.flammability(2)),
	SHORT_MEAD(properties -> properties
			.particleColor(new Color(239, 154, 56))
			.density(1000)
			.viscosity(1200)
			.spreadsFire()
			.flammability(4)
			.drinkProperties(1, 0.2f, 32)
	);

	private static final Map<ResourceLocation, ForestryFluids> tagToFluid = new HashMap<>();

	static {
		for (ForestryFluids fluidDefinition : ForestryFluids.values()) {
			tagToFluid.put(ForestryConstants.forestry(fluidDefinition.feature.getName()), fluidDefinition);
		}
	}

	private final ResourceLocation tag;
	private final FeatureFluid feature;
	private final FeatureItem<BucketItem> bucket;

	ForestryFluids(UnaryOperator<FeatureFluid.Builder> properties) {
		IFeatureRegistry registry = ModFeatureRegistry.get(ForestryModuleIds.FLUIDS);
		this.feature = properties.apply(registry
						.fluid(name().toLowerCase(Locale.ENGLISH)))
				.bucket(this::getBucket)
				.create();
		this.bucket = registry
				.item(() -> new BucketItem(this::getFluid, new Item.Properties()
								.craftRemainder(Items.BUCKET)
								.stacksTo(1)),
						"bucket_" + name().toLowerCase(Locale.ENGLISH)
				);
		this.tag = ForestryConstants.forestry(feature.getName());
	}

	public int getTemperature() {
		return 295;
	}

	public final ResourceLocation getTag() {
		return tag;
	}

	public FeatureFluid getFeature() {
		return feature;
	}

	public BucketItem getBucket() {
		return bucket.item();
	}

	public final Fluid getFluid() {
		return feature.fluid();
	}

	public final Fluid getFlowing() {
		return feature.flowing();
	}

	public final FluidStack getFluid(int mb) {
		Fluid fluid = getFluid();
		if (fluid == Fluids.EMPTY) {
			return FluidStack.EMPTY;
		}
		return new FluidStack(fluid, mb);
	}

	public final Color getParticleColor() {
		return feature.properties().particleColor;
	}

	public final boolean is(Fluid fluid) {
		return getFluid() == fluid;
	}

	public final boolean is(FluidStack fluidStack) {
		return getFluid() == fluidStack.getFluid();
	}

	public static boolean areEqual(Fluid fluid, FluidStack fluidStack) {
		return fluid == fluidStack.getFluid();
	}

	@Nullable
	public static ForestryFluids getFluidDefinition(Fluid fluid) {
		return tagToFluid.get(ModUtil.getRegistryName(fluid));
	}

	@Nullable
	public static ForestryFluids getFluidDefinition(FluidStack stack) {
		if (!stack.isEmpty()) {
			return getFluidDefinition(stack.getFluid());
		}

		return null;
	}

	/**
	 * Get the properties for an ItemFluidContainerForestry before it gets registered.
	 */
	@Nullable
	public DrinkProperties getDrinkProperties() {
		return feature.properties().properties;
	}
}

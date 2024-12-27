package forestry.modules.features;

import javax.annotation.Nullable;
import java.awt.Color;
import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.Item;
import net.minecraft.resources.ResourceLocation;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.data.loading.DatagenModLoader;

import net.minecraftforge.fml.loading.FMLEnvironment;

import forestry.api.ForestryConstants;
import forestry.core.items.definitions.DrinkProperties;

public class FluidProperties {
	public final int density;
	public final int viscosity;
	public final int temperature;
	public final Color particleColor;
	public final int flammability;
	public final boolean spreadsFire;
	@Nullable
	public final DrinkProperties properties;
	public final ResourceLocation[] resources = new ResourceLocation[2];
	public final Supplier<Item> bucket;

	public FluidProperties(FeatureFluid.Builder builder) {
		this.density = builder.density;
		this.viscosity = builder.viscosity;
		this.temperature = builder.temperature;
		this.particleColor = builder.particleColor;
		this.flammability = builder.flammability;
		this.spreadsFire = builder.spreadsFire;
		this.properties = builder.properties;
		this.resources[0] = builder.registry.getModuleId().withPath("block/liquid/" + builder.identifier + "_still");
		this.resources[1] = builder.registry.getModuleId().withPath("block/liquid/" + builder.identifier + "_flow");
		if (!resourceExists(resources[1])) {
			this.resources[1] = resources[0];
		}
		this.bucket = builder.bucket;
	}

	public boolean resourceExists(ResourceLocation location) {
		if (FMLEnvironment.dist == Dist.DEDICATED_SERVER || DatagenModLoader.isRunningDataGen()) {
			return true;
		}
        return Minecraft.getInstance().getResourceManager().getResource(location).isPresent();
	}
}

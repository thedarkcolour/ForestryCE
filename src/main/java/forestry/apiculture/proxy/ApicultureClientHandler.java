package forestry.apiculture.proxy;

import forestry.api.client.IClientModuleHandler;
import forestry.apiculture.features.ApicultureBlocks;
import forestry.apiculture.features.ApicultureMenuTypes;
import forestry.apiculture.gui.*;
import forestry.apiculture.models.ModelBee;
import forestry.apiculture.particles.*;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class ApicultureClientHandler implements IClientModuleHandler {
	@Override
	public void registerEvents(IEventBus modBus) {
		modBus.addListener(ApicultureClientHandler::setupClient);
		modBus.addListener(ApicultureClientHandler::registerParticleFactory);
		modBus.addListener(ApicultureClientHandler::handleSprites);
		modBus.addListener(ApicultureClientHandler::registerModelLoaders);
	}

	private static void setupClient(FMLClientSetupEvent event) {
		event.enqueueWork(() -> {
			ApicultureBlocks.BEE_COMB.getBlocks().forEach((block) -> ItemBlockRenderTypes.setRenderLayer(block, RenderType.cutout()));

			MenuScreens.register(ApicultureMenuTypes.ALVEARY.menuType(), GuiAlveary::new);
			MenuScreens.register(ApicultureMenuTypes.ALVEARY_HYGROREGULATOR.menuType(), GuiAlvearyHygroregulator::new);
			MenuScreens.register(ApicultureMenuTypes.ALVEARY_SIEVE.menuType(), GuiAlvearySieve::new);
			MenuScreens.register(ApicultureMenuTypes.ALVEARY_SWARMER.menuType(), GuiAlvearySwarmer::new);
			MenuScreens.register(ApicultureMenuTypes.BEE_HOUSING.menuType(), GuiBeeHousing<ContainerBeeHousing>::new);
		});
	}

	private static void registerParticleFactory(RegisterParticleProvidersEvent event) {
		event.registerSprite(ApicultureParticles.BEE_EXPLORER_PARTICLE.get(), (data, level, x, y, z, xSpeed, ySpeed, zSpeed) -> new BeeExploreParticle(level, x, y, z, data.destination, data.color));
		event.registerSprite(ApicultureParticles.BEE_ROUND_TRIP_PARTICLE.get(), (data, level, x, y, z, xSpeed, ySpeed, zSpeed) -> new BeeRoundTripParticle(level, x, y, z, data.destination, data.color));
		event.registerSprite(ApicultureParticles.BEE_TARGET_ENTITY_PARTICLE.get(), (data, level, x, y, z, xSpeed, ySpeed, zSpeed) -> {
			Entity entity = level.getEntity(data.entity);
			return entity == null ? null : new BeeTargetEntityParticle(level, x, y, z, entity, data.color);
		});
	}

	private static void handleSprites(TextureStitchEvent.Post event) {
		TextureAtlas map = event.getAtlas();
		if (map.location().equals(TextureAtlas.LOCATION_PARTICLES)) {
			for (int i = 0; i < ParticleSnow.SPRITES.length; i++) {
				ParticleSnow.SPRITES[i] = map.getSprite(new ResourceLocation("forestry:snow." + (i + 1)));
			}
		}
	}

	private static void registerModelLoaders(ModelEvent.RegisterGeometryLoaders event) {
		event.register("bee_ge", new ModelBee.Loader());
	}
}

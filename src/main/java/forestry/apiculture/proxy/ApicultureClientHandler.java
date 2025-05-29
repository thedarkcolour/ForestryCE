package forestry.apiculture.proxy;

import forestry.api.ForestryConstants;
import forestry.api.client.IClientModuleHandler;
import forestry.apiculture.features.ApicultureBlocks;
import forestry.apiculture.features.ApicultureMenuTypes;
import forestry.apiculture.gui.*;
import forestry.apiculture.models.ModelBee;
import forestry.apiculture.particles.*;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.event.TextureAtlasStitchedEvent;

public class ApicultureClientHandler implements IClientModuleHandler {
	@Override
	public void registerEvents(IEventBus modBus) {
		modBus.addListener(ApicultureClientHandler::setupClient);
		modBus.addListener(ApicultureClientHandler::registerMenus);
		modBus.addListener(ApicultureClientHandler::registerParticleFactory);
		modBus.addListener(ApicultureClientHandler::handleSprites);
		modBus.addListener(ApicultureClientHandler::registerModelLoaders);
	}

	private static void setupClient(FMLClientSetupEvent event) {
		// todo use JSON render_type field
		event.enqueueWork(() -> ApicultureBlocks.BEE_COMB.getBlocks().forEach((block) -> ItemBlockRenderTypes.setRenderLayer(block, RenderType.cutout())));
	}

	private static void registerMenus(RegisterMenuScreensEvent event) {
		event.register(ApicultureMenuTypes.ALVEARY.menuType(), GuiAlveary::new);
		event.register(ApicultureMenuTypes.ALVEARY_HYGROREGULATOR.menuType(), GuiAlvearyHygroregulator::new);
		event.register(ApicultureMenuTypes.ALVEARY_SIEVE.menuType(), GuiAlvearySieve::new);
		event.register(ApicultureMenuTypes.ALVEARY_SWARMER.menuType(), GuiAlvearySwarmer::new);
		event.register(ApicultureMenuTypes.BEE_HOUSING.menuType(), GuiBeeHousing::new);
	}

	private static void registerParticleFactory(RegisterParticleProvidersEvent event) {
		event.registerSprite(ApicultureParticles.BEE_EXPLORER_PARTICLE.value(), (data, level, x, y, z, xSpeed, ySpeed, zSpeed) -> new BeeExploreParticle(level, x, y, z, data.destination(), data.color()));
		event.registerSprite(ApicultureParticles.BEE_ROUND_TRIP_PARTICLE.value(), (data, level, x, y, z, xSpeed, ySpeed, zSpeed) -> new BeeRoundTripParticle(level, x, y, z, data.destination(), data.color()));
		event.registerSprite(ApicultureParticles.BEE_TARGET_ENTITY_PARTICLE.value(), (data, level, x, y, z, xSpeed, ySpeed, zSpeed) -> {
			Entity entity = level.getEntity(data.entity());
			return entity == null ? null : new BeeTargetEntityParticle(level, x, y, z, entity, data.color());
		});
	}

	private static void handleSprites(TextureAtlasStitchedEvent event) {
		TextureAtlas map = event.getAtlas();
		if (map.location().equals(TextureAtlas.LOCATION_PARTICLES)) {
			for (int i = 0; i < ParticleSnow.SPRITES.length; i++) {
				ParticleSnow.SPRITES[i] = map.getSprite(ForestryConstants.forestry("snow." + (i + 1)));
			}
		}
	}

	private static void registerModelLoaders(ModelEvent.RegisterGeometryLoaders event) {
		event.register(ForestryConstants.forestry("bee_ge"), new ModelBee.Loader());
	}
}

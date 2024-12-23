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
package forestry.apiculture.proxy;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;

import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.IEventBus;

import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import forestry.api.ForestryConstants;
import forestry.api.client.IClientModuleHandler;
import forestry.apiculture.features.ApicultureBlocks;
import forestry.apiculture.features.ApicultureMenuTypes;
import forestry.apiculture.gui.ContainerBeeHousing;
import forestry.apiculture.gui.GuiAlveary;
import forestry.apiculture.gui.GuiAlvearyHygroregulator;
import forestry.apiculture.gui.GuiAlvearySieve;
import forestry.apiculture.gui.GuiAlvearySwarmer;
import forestry.apiculture.gui.GuiBeeHousing;
import forestry.apiculture.models.ModelBee;
import forestry.apiculture.particles.ApicultureParticles;
import forestry.apiculture.particles.BeeExploreParticle;
import forestry.apiculture.particles.BeeRoundTripParticle;
import forestry.apiculture.particles.BeeTargetEntityParticle;
import forestry.apiculture.particles.ParticleSnow;

public class ApicultureClientHandler implements IClientModuleHandler {
	@Override
	public void registerEvents(IEventBus modBus) {
		modBus.addListener(ApicultureClientHandler::setupClient);
		modBus.addListener(ApicultureClientHandler::registerParticleFactory);
		modBus.addListener(ApicultureClientHandler::hackFixForSnowingEffect);
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
		event.register(ApicultureParticles.BEE_EXPLORER_PARTICLE.get(), BeeExploreParticle.Factory::new);
		event.register(ApicultureParticles.BEE_ROUND_TRIP_PARTICLE.get(), BeeRoundTripParticle.Factory::new);
		event.register(ApicultureParticles.BEE_TARGET_ENTITY_PARTICLE.get(), BeeTargetEntityParticle.Factory::new);
	}

	private static void hackFixForSnowingEffect(TextureStitchEvent.Pre event) {
		if (event.getAtlas().location().equals(TextureAtlas.LOCATION_PARTICLES)) {
			for (int i = 0; i < ParticleSnow.sprites.length; i++) {
				event.addSprite(ForestryConstants.forestry("particle/snow." + (i + 1)));
			}
		}
	}

	private static void handleSprites(TextureStitchEvent.Post event) {
		TextureAtlas map = event.getAtlas();
		if (map.location().equals(TextureAtlas.LOCATION_PARTICLES)) {
			for (int i = 0; i < ParticleSnow.sprites.length; i++) {
				ParticleSnow.sprites[i] = map.getSprite(ForestryConstants.forestry("particle/snow." + (i + 1)));
			}
		}
	}

	private static void registerModelLoaders(ModelEvent.RegisterGeometryLoaders event) {
		event.register("bee_ge", new ModelBee.Loader());
	}
}

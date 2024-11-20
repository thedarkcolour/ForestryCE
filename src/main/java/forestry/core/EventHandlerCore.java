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
package forestry.core;

import java.util.List;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.ProcessorLists;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;

import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import net.minecraftforge.fml.common.Mod;

import forestry.api.ForestryConstants;
import forestry.api.IForestryApi;
import forestry.api.genetics.IBreedingTracker;
import forestry.api.genetics.ISpeciesType;
import forestry.apiculture.ApiaristAI;
import forestry.apiculture.features.ApicultureEffects;
import forestry.apiculture.villagers.ApicultureVillagers;
import forestry.core.worldgen.ApiaristPoolElement;
import forestry.core.worldgen.VillagerJigsaw;

@Mod.EventBusSubscriber(modid = ForestryConstants.MOD_ID)
public class EventHandlerCore {
	@SubscribeEvent
	public static void handlePlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
		Player player = event.getEntity();
		syncBreedingTrackers(player);
	}

	@SubscribeEvent
	public static void handlePlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
		Player player = event.getEntity();
		syncBreedingTrackers(player);
	}

	private static void syncBreedingTrackers(Player player) {
		for (ISpeciesType<?, ?> type : IForestryApi.INSTANCE.getGeneticManager().getSpeciesTypes()) {
			IBreedingTracker breedingTracker = type.getBreedingTracker(player.getCommandSenderWorld(), player.getGameProfile());
			breedingTracker.syncToPlayer(player);
		}
	}

	@SubscribeEvent
	public static void onEntityJoinWorld(EntityJoinLevelEvent event) {
		Entity entity = event.getEntity();
		if (entity instanceof Villager villager) {
			if (villager.getVillagerData().getProfession().equals(ApicultureVillagers.PROF_BEEKEEPER.get())) {
				villager.goalSelector.addGoal(6, new ApiaristAI(villager, 0.6));
			}
		}
	}

	@SubscribeEvent
	public static void doHakunaDamageReduction(LivingAttackEvent event) {
		if (event.getEntity().hasEffect(ApicultureEffects.HAKUNA_MATATA.get())) {
			event.setCanceled(true);
			if (event.getAmount() > 5) {
				event.getEntity().removeEffect(ApicultureEffects.HAKUNA_MATATA.get());
				event.getEntity().addEffect(new MobEffectInstance(ApicultureEffects.MATATA.get(), (int) (300 * event.getAmount())));
				event.getEntity().playSound(SoundEvents.WITHER_BREAK_BLOCK);
				if (event.getSource().getEntity() instanceof LivingEntity attacker) {
					//no to no worries when attacking
					if (attacker.hasEffect(ApicultureEffects.HAKUNA_MATATA.get())) {
						attacker.removeEffect(ApicultureEffects.HAKUNA_MATATA.get());
						attacker.addEffect(new MobEffectInstance(ApicultureEffects.MATATA.get(), (int) (300 * event.getAmount())));
						attacker.playSound(SoundEvents.WITHER_BREAK_BLOCK);
					}
				}
			}
		}
	}

	@SubscribeEvent
	public static void serverAboutToStart(ServerAboutToStartEvent event) {
		Registry<StructureTemplatePool> pools = event.getServer().registryAccess().registry(Registries.TEMPLATE_POOL).orElseThrow();
		Registry<StructureProcessorList> processors = event.getServer().registryAccess().registry(Registries.PROCESSOR_LIST).orElseThrow();

		VillagerJigsaw.init(pools, processors);
	}
}

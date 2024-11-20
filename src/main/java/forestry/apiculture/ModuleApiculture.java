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
package forestry.apiculture;

import java.util.function.Consumer;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.IEventBus;

import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import forestry.api.apiculture.BeeManager;
import forestry.api.apiculture.IArmorApiarist;
import forestry.api.client.IClientModuleHandler;
import forestry.api.modules.ForestryModule;
import forestry.api.modules.ForestryModuleIds;
import forestry.api.modules.IPacketRegistry;
import forestry.apiculture.commands.CommandBee;
import forestry.apiculture.features.ApicultureItems;
import forestry.apiculture.items.EnumPollenCluster;
import forestry.apiculture.network.packets.PacketAlvearyChange;
import forestry.apiculture.network.packets.PacketBeeLogicActive;
import forestry.apiculture.network.packets.PacketHabitatBiomePointer;
import forestry.apiculture.proxy.ApicultureClientHandler;
import forestry.apiculture.villagers.ApicultureVillagers;
import forestry.core.network.PacketIdClient;
import forestry.modules.BlankForestryModule;

@ForestryModule
public class ModuleApiculture extends BlankForestryModule {
	public static int ticksPerBeeWorkCycle = 550;
	public static boolean hivesDamageOnPeaceful = false;
	public static boolean hivesDamageUnderwater = true;
	public static boolean hivesDamageOnlyPlayers = false;
	public static boolean hiveDamageOnAttack = true;
	public static boolean doSelfPollination = false;
	public static int maxFlowersSpawnedPerHive = 20;

	@Override
	public ResourceLocation getId() {
		return ForestryModuleIds.APICULTURE;
	}

	@Override
	public void registerEvents(IEventBus modBus) {
		modBus.addListener(ModuleApiculture::registerCapabilities);
		modBus.addListener(ModuleApiculture::onCommonSetup);

		MinecraftForge.EVENT_BUS.addListener(ApicultureVillagers::villagerTrades);
	}

	private static void onCommonSetup(FMLCommonSetupEvent event) {
		// BREWING RECIPES
		BrewingRecipeRegistry.addRecipe(
				Ingredient.of(PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.AWKWARD)),
				Ingredient.of(ApicultureItems.POLLEN_CLUSTER.stack(EnumPollenCluster.NORMAL, 1)),
				PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.HEALING));
		BrewingRecipeRegistry.addRecipe(
				Ingredient.of(PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.AWKWARD)),
				Ingredient.of(ApicultureItems.POLLEN_CLUSTER.stack(EnumPollenCluster.CRYSTALLINE, 1)),
				PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.REGENERATION));
	}

	private static void registerCapabilities(RegisterCapabilitiesEvent event) {
		event.register(IArmorApiarist.class);
	}

	@Override
	public void addToRootCommand(LiteralArgumentBuilder<CommandSourceStack> command) {
		command.then(CommandBee.register());
	}

	@Override
	public void setupApi() {
		BeeManager.armorApiaristHelper = new ArmorApiaristHelper();
	}

/*

	// todo replace with tags "acceptable flowers," "plantable flowers," where plantable is subset of acceptable
	private void initFlowerRegistry() {
		FlowerRegistry flowerRegistry = (FlowerRegistry) FlowerManager.flowerRegistry;

		flowerRegistry.registerAcceptableFlowerRule(new EndFlowerAcceptableRule(), FlowerManager.FlowerTypeEnd);

		// Register acceptable plants
		flowerRegistry.registerAcceptableFlower(Blocks.DRAGON_EGG, FlowerManager.FlowerTypeEnd);
		flowerRegistry.registerAcceptableFlower(Blocks.CHORUS_PLANT, FlowerManager.FlowerTypeEnd);
		flowerRegistry.registerAcceptableFlower(Blocks.CHORUS_FLOWER, FlowerManager.FlowerTypeEnd);
		flowerRegistry.registerAcceptableFlower(Blocks.VINE, FlowerManager.FlowerTypeJungle);
		flowerRegistry.registerAcceptableFlower(Blocks.FERN, FlowerManager.FlowerTypeJungle);
		flowerRegistry.registerAcceptableFlower(Blocks.WHEAT, FlowerManager.FlowerTypeWheat);
		flowerRegistry.registerAcceptableFlower(Blocks.PUMPKIN_STEM, FlowerManager.FlowerTypeGourd);
		flowerRegistry.registerAcceptableFlower(Blocks.MELON_STEM, FlowerManager.FlowerTypeGourd);
		flowerRegistry.registerAcceptableFlower(Blocks.NETHER_WART, FlowerManager.FlowerTypeNether);
		flowerRegistry.registerAcceptableFlower(Blocks.CACTUS, FlowerManager.FlowerTypeCacti);

		Block[] standardFlowers = new Block[]{
				Blocks.DANDELION,
				Blocks.POPPY,
				Blocks.BLUE_ORCHID,
				Blocks.ALLIUM,
				Blocks.AZURE_BLUET,
				Blocks.RED_TULIP,
				Blocks.ORANGE_TULIP,
				Blocks.WHITE_TULIP,
				Blocks.PINK_TULIP,
				Blocks.OXEYE_DAISY,
				Blocks.CORNFLOWER,
				Blocks.WITHER_ROSE,
				Blocks.LILY_OF_THE_VALLEY,
		};
		Block[] pottedStandardFlowers = new Block[]{
				Blocks.POTTED_POPPY,
				Blocks.POTTED_BLUE_ORCHID,
				Blocks.POTTED_ALLIUM,
				Blocks.POTTED_AZURE_BLUET,
				Blocks.POTTED_RED_TULIP,
				Blocks.POTTED_ORANGE_TULIP,
				Blocks.POTTED_WHITE_TULIP,
				Blocks.POTTED_PINK_TULIP,
				Blocks.POTTED_OXEYE_DAISY,
				Blocks.POTTED_CORNFLOWER,
				Blocks.POTTED_LILY_OF_THE_VALLEY,
				Blocks.POTTED_WITHER_ROSE,
		};


		//Flower Pots
		for (Block standardFlower : pottedStandardFlowers) {
			flowerRegistry.registerAcceptableFlower(standardFlower, standardTypes);
		}

		flowerRegistry.registerAcceptableFlower(Blocks.POTTED_RED_MUSHROOM, FlowerManager.FlowerTypeMushrooms);
		flowerRegistry.registerAcceptableFlower(Blocks.POTTED_BROWN_MUSHROOM, FlowerManager.FlowerTypeMushrooms);

		flowerRegistry.registerAcceptableFlower(Blocks.POTTED_CACTUS, FlowerManager.FlowerTypeCacti);
	}
*/

	@Override
	public void registerPackets(IPacketRegistry registry) {
		registry.clientbound(PacketIdClient.BEE_LOGIC_ACTIVE, PacketBeeLogicActive.class, PacketBeeLogicActive::decode, PacketBeeLogicActive::handle);
		registry.clientbound(PacketIdClient.HABITAT_BIOME_POINTER, PacketHabitatBiomePointer.class, PacketHabitatBiomePointer::decode, PacketHabitatBiomePointer::handle);
		registry.clientbound(PacketIdClient.ALVERAY_CONTROLLER_CHANGE, PacketAlvearyChange.class, PacketAlvearyChange::decode, PacketAlvearyChange::handle);
	}

	// todo config
	public static double getSecondPrincessChance() {
		return (float) 0;
	}

	@Override
	public void registerClientHandler(Consumer<IClientModuleHandler> registrar) {
		registrar.accept(new ApicultureClientHandler());
	}
}

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
package forestry.factory;

import java.util.function.Consumer;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import net.minecraftforge.eventbus.api.IEventBus;

import forestry.api.client.IClientModuleHandler;
import forestry.api.fuels.FermenterFuel;
import forestry.api.fuels.FuelManager;
import forestry.api.fuels.MoistenerFuel;
import forestry.api.fuels.RainSubstrate;
import forestry.api.modules.ForestryModule;
import forestry.api.modules.ForestryModuleIds;
import forestry.api.modules.IPacketRegistry;
import forestry.core.config.Preference;
import forestry.core.features.CoreItems;
import forestry.core.network.PacketIdClient;
import forestry.core.network.PacketIdServer;
import forestry.core.utils.datastructures.ItemStackMap;
import forestry.factory.client.FactoryClientHandler;
import forestry.factory.network.packets.PacketRecipeTransferRequest;
import forestry.factory.network.packets.PacketRecipeTransferUpdate;
import forestry.modules.BlankForestryModule;

@ForestryModule
public class ModuleFactory extends BlankForestryModule {
	@Override
	public ResourceLocation getId() {
		return ForestryModuleIds.FACTORY;
	}

	@Override
	public void registerEvents(IEventBus modBus) {
	}

	@Override
	public void registerClientHandler(Consumer<IClientModuleHandler> registrar) {
		registrar.accept(new FactoryClientHandler());
	}

	@Override
	public void setupApi() {
		FuelManager.fermenterFuel = new ItemStackMap<>();
		FuelManager.moistenerResource = new ItemStackMap<>();
		FuelManager.rainSubstrate = new ItemStackMap<>();

		// Set fuels and resources for the fermenter
		ItemStack fertilizerCompound = CoreItems.FERTILIZER_COMPOUND.stack();
		FuelManager.fermenterFuel.put(fertilizerCompound, new FermenterFuel(fertilizerCompound,
				Preference.FERMENTED_CYCLE_FERTILIZER, Preference.FERMENTATION_DURATION_FERTILIZER));

		int cyclesCompost = Preference.FERMENTATION_DURATION_COMPOST;
		int valueCompost = Preference.FERMENTED_CYCLE_COMPOST;
		ItemStack fertilizerBio = CoreItems.COMPOST.stack();
		ItemStack mulch = CoreItems.MULCH.stack();
		FuelManager.fermenterFuel.put(fertilizerBio, new FermenterFuel(fertilizerBio, valueCompost, cyclesCompost));
		FuelManager.fermenterFuel.put(mulch, new FermenterFuel(mulch, valueCompost, cyclesCompost));

		// Add moistener resources
		ItemStack wheat = new ItemStack(Items.WHEAT);
		ItemStack mouldyWheat = CoreItems.MOULDY_WHEAT.stack();
		ItemStack decayingWheat = CoreItems.DECAYING_WHEAT.stack();
		FuelManager.moistenerResource.put(wheat, new MoistenerFuel(wheat, mouldyWheat, 0, 300));
		FuelManager.moistenerResource.put(mouldyWheat, new MoistenerFuel(mouldyWheat, decayingWheat, 1, 600));
		FuelManager.moistenerResource.put(decayingWheat, new MoistenerFuel(decayingWheat, mulch, 2, 900));

		// Set rain substrates
		ItemStack iodineCharge = CoreItems.IODINE_CHARGE.stack();
		ItemStack dissipationCharge = CoreItems.DISSIPATION_CHARGE.stack();
		FuelManager.rainSubstrate.put(iodineCharge, new RainSubstrate(iodineCharge, 10000, 0.01f));
		FuelManager.rainSubstrate.put(dissipationCharge, new RainSubstrate(dissipationCharge, 0.075f));
	}

	@Override
	public void registerPackets(IPacketRegistry registry) {
		registry.serverbound(PacketIdServer.RECIPE_TRANSFER_REQUEST, PacketRecipeTransferRequest.class, PacketRecipeTransferRequest::decode, PacketRecipeTransferRequest::handle);
		registry.clientbound(PacketIdClient.RECIPE_TRANSFER_UPDATE, PacketRecipeTransferUpdate.class, PacketRecipeTransferUpdate::decode, PacketRecipeTransferUpdate::handle);
	}
}

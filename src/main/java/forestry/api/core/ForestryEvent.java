/*******************************************************************************
 * Copyright 2011-2014 SirSengir
 *
 * This work (the API) is licensed under the "MIT" License, see LICENSE.txt for details.
 ******************************************************************************/
package forestry.api.core;

import net.minecraft.world.entity.player.Player;

import com.mojang.authlib.GameProfile;

import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

import forestry.api.apiculture.IBeeHousing;
import forestry.api.apiculture.genetics.IBee;
import forestry.api.genetics.IBreedingTracker;
import forestry.api.genetics.IMutation;
import forestry.api.genetics.ISpecies;
import forestry.api.genetics.ISpeciesType;

import org.jetbrains.annotations.ApiStatus;

// todo move to forestry.api.event
public abstract class ForestryEvent extends Event {
	/**
	 * Supertype for events concerning breeding and mutation.
	 */
	@ApiStatus.Internal
	private static abstract class BreedingEvent extends ForestryEvent {
		public final ISpeciesType<?, ?> root;
		public final IBreedingTracker tracker;
		public final GameProfile username;

		private BreedingEvent(ISpeciesType<?, ?> root, GameProfile username, IBreedingTracker tracker) {
			this.root = root;
			this.username = username;
			this.tracker = tracker;
		}
	}

	/**
	 * Fired before a queen is created as a result of breeding a princess and a drone.
	 * @since 2.4.4
	 */
	@Cancelable
	public static class BeeMatingEvent extends ForestryEvent {
		private final IBeeHousing housing;
		private IBee princess;
		private final IBee drone;

		public BeeMatingEvent(IBeeHousing housing, IBee princess, IBee drone) {
			this.housing = housing;
			this.princess = princess;
			this.drone = drone;
		}

		public IBeeHousing getHousing() {
			return housing;
		}

		public IBee getPrincess() {
			return princess;
		}

		/**
		 * Used to override the princess individual, which will become the queen individual.
		 * @param princess The new princess individual to replace the queen with.
		 */
		public void setPrincess(IBee princess) {
			this.princess = princess;
		}

		public IBee getDrone() {
			return drone;
		}
	}

	public static class SpeciesDiscovered extends BreedingEvent {
		public final ISpecies<?> species;

		public SpeciesDiscovered(ISpeciesType<?, ?> root, GameProfile username, ISpecies<?> species, IBreedingTracker tracker) {
			super(root, username, tracker);
			this.species = species;
		}
	}

	public static class MutationDiscovered extends BreedingEvent {
		public final IMutation<?> allele;

		public MutationDiscovered(ISpeciesType<?, ?> root, GameProfile username, IMutation<?> allele, IBreedingTracker tracker) {
			super(root, username, tracker);
			this.allele = allele;
		}
	}

	public static class SyncedBreedingTracker extends ForestryEvent {
		public final IBreedingTracker tracker;
		public final Player player;

		public SyncedBreedingTracker(IBreedingTracker tracker, Player player) {
			this.tracker = tracker;
			this.player = player;
		}
	}
}

package forestry.api.event;

import com.mojang.authlib.GameProfile;
import forestry.api.genetics.IBreedingTracker;
import forestry.api.genetics.IMutation;
import forestry.api.genetics.ISpecies;
import forestry.api.genetics.ISpeciesType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.component.ResolvableProfile;
import net.neoforged.bus.api.Event;
import org.jetbrains.annotations.ApiStatus;

/**
 * Supertype for events concerning breeding and mutation.
 */
@ApiStatus.Internal
public abstract class BreedingTrackerEvent extends Event {
	public final ISpeciesType<?, ?> root;
	public final IBreedingTracker tracker;
	public final ResolvableProfile username;

	private BreedingTrackerEvent(ISpeciesType<?, ?> root, ResolvableProfile username, IBreedingTracker tracker) {
		this.root = root;
		this.username = username;
		this.tracker = tracker;
	}

	/**
	 * Fired when a player first discovers a mutation, such as:
	 * <ul>
	 *     <li>when successfully triggering the mutation through breeding</li>
	 *     <li>when learning the mutation from Research Notes in the Escritoire</li>
	 * </ul>
	 */
	public static class MutationDiscovered extends BreedingTrackerEvent {
		public final IMutation<?> allele;

		public MutationDiscovered(ISpeciesType<?, ?> root, ResolvableProfile username, IMutation<?> allele, IBreedingTracker tracker) {
			super(root, username, tracker);
			this.allele = allele;
		}
	}

	/**
	 * Fired when a player first discovers a species, such as:
	 * <ul>
	 *     <li>when successfully breeding the species</li>
	 *     <li>when picking up the item form of the species after using a Scoop or Grafter</li>
	 *     <li>when learning a mutation involving the species from Research Notes in the Escritoire</li>
	 * </ul>
	 */
	public static class SpeciesDiscovered extends BreedingTrackerEvent {
		public final ISpecies<?> species;

		public SpeciesDiscovered(ISpeciesType<?, ?> root, ResolvableProfile username, ISpecies<?> species, IBreedingTracker tracker) {
			super(root, username, tracker);
			this.species = species;
		}
	}

	/**
	 * Fired when a player's breeding tracker is synced to the client.
	 */
	public static class Synced extends BreedingTrackerEvent {
		public final Player player;

		public Synced(ISpeciesType<?, ?> type, IBreedingTracker tracker, Player player) {
			super(type, new ResolvableProfile(player.getGameProfile()), tracker);

            this.player = player;
        }
	}
}

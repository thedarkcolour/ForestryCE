package forestry.api.event;

import com.mojang.authlib.GameProfile;
import forestry.api.genetics.IBreedingTracker;
import forestry.api.genetics.IMutation;
import forestry.api.genetics.ISpecies;
import forestry.api.genetics.ISpeciesType;
import net.neoforged.bus.api.Event;
import org.jetbrains.annotations.ApiStatus;

/**
 * Supertype for events concerning breeding and mutation.
 */
@ApiStatus.Internal
public abstract class BreedingEvent extends Event {
	public final ISpeciesType<?, ?> root;
	public final IBreedingTracker tracker;
	public final GameProfile username;

	private BreedingEvent(ISpeciesType<?, ?> root, GameProfile username, IBreedingTracker tracker) {
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
	public static class MutationDiscovered extends BreedingEvent {
		public final IMutation<?> allele;

		public MutationDiscovered(ISpeciesType<?, ?> root, GameProfile username, IMutation<?> allele, IBreedingTracker tracker) {
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
	public static class SpeciesDiscoveredEvent extends BreedingEvent {
		public final ISpecies<?> species;

		public SpeciesDiscoveredEvent(ISpeciesType<?, ?> root, GameProfile username, ISpecies<?> species, IBreedingTracker tracker) {
			super(root, username, tracker);
			this.species = species;
		}
	}
}

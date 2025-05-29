package forestry.api.plugin;

import net.minecraft.resources.ResourceLocation;

/**
 * Used to register possible mutations resulting in a species.
 */
public interface IMutationsRegistration {
	/**
	 * Registers a mutation that results in this species. The species of the parents must be different.
	 * The ordering of the parents does not matter for mutations.
	 *
	 * @param firstParent  One of the species involved in this mutation.
	 * @param secondParent The other species involved in this mutation. Must not be the same as {@code firstParent}.
	 * @param chance       The chance (between 0 and 1 inclusive) of this mutation occurring.
	 * @throws IllegalArgumentException If both parent species are the same species, or if chance is not [0,1].
	 * @throws IllegalStateException    If there is already a mutation builder for the given parents.
	 *                                  Use {@link #get} to modify the existing one.
	 */
	IMutationBuilder add(ResourceLocation firstParent, ResourceLocation secondParent, float chance);

	/**
	 * Retrieves an already existing mutation so that it can be further customized.
	 *
	 * @return A mutation builder for an already registered mutation between these two parents.
	 * @throws IllegalArgumentException If both parent species are the same species.
	 * @throws IllegalStateException    If there is no existing mutation between those two parents.
	 */
	IMutationBuilder get(ResourceLocation firstParent, ResourceLocation secondParent);
}

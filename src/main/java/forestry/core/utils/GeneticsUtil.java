package forestry.core.utils;

import forestry.api.ForestryCapabilities;
import forestry.api.core.IArmorNaturalist;
import forestry.api.genetics.*;
import forestry.api.genetics.capability.IIndividualHandlerItem;
import forestry.api.lepidopterology.IButterflyNursery;
import forestry.api.lepidopterology.genetics.IButterfly;
import forestry.compat.curios.CuriosCompat;
import forestry.core.genetics.ItemGE;
import forestry.core.tiles.TileUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.*;

public class GeneticsUtil {
	public static boolean hasNaturalistEye(Player player) {
		ItemStack armorItemStack = player.getItemBySlot(EquipmentSlot.HEAD);
		if (armorItemStack.isEmpty()) {
			if (CuriosCompat.IS_LOADED) {
				return CuriosCompat.hasNaturalistEye(player);
			} else {
				return false;
			}
		}

		return hasNaturalistEye(player, armorItemStack);
	}

	public static boolean hasNaturalistEye(Player player, ItemStack armorItemStack) {
		final IArmorNaturalist armorNaturalist = armorItemStack.getCapability(ForestryCapabilities.SPECTACLE_VISION);

		return armorNaturalist != null && armorNaturalist.canSeePollination(player, armorItemStack, true);
	}

	public static boolean canNurse(IButterfly butterfly, Level world, final BlockPos pos) {
		IButterflyNursery tile = TileUtil.getTile(world, pos, IButterflyNursery.class);
		return tile != null && tile.canNurse(butterfly);
	}

	public static ItemStack convertToGeneticEquivalent(ItemStack foreign) {
		IIndividualHandlerItem individual = IIndividualHandlerItem.get(foreign);
		if (individual != null && !individual.isGeneticForm()) {
			ItemStack equivalent = individual.getIndividual().getSpecies().createStack(individual.getStage());
			equivalent.setCount(foreign.getCount());
			return equivalent;
		}
		return foreign;
	}

	public static int getResearchComplexity(ISpecies<?> species) {
		return 1 + getGeneticAdvancement(species, new HashSet<>());
	}

	@SuppressWarnings("unchecked")
	private static int getGeneticAdvancement(ISpecies<?> species, Set<ISpecies<?>> exclude) {
		int highest = 0;
		exclude.add(species);

		ISpeciesType<?, ?> type = species.getType();
		for (IMutation<?> mutation : ((IMutationManager<ISpecies<?>>) type.getMutations()).getMutationsInto(species)) {
			highest = getHighestAdvancement(mutation.getFirstParent(), highest, exclude);
			highest = getHighestAdvancement(mutation.getSecondParent(), highest, exclude);
		}

		return 1 + highest;
	}

	private static int getHighestAdvancement(ISpecies<?> mutationSpecies, int highest, Set<ISpecies<?>> exclude) {
		if (exclude.contains(mutationSpecies)) {
			return highest;
		}

		int otherAdvance = getGeneticAdvancement(mutationSpecies, exclude);
		return Math.max(otherAdvance, highest);
	}

	/**
	 * Creates a translation key for an OBJECT of a TYPE (ex. a SPECIES of a SPECIES TYPE, or an ALLELE of a CHROMOSOME)
	 * The format is as follows:
	 * <ul>
	 *     <li>If {@code typeId} and {@code objectId} share the same namespace, the format is: <br> {@code TYPE.NAMESPACE.TYPEPATH.OBJECTPATH}</li>
	 *     <li>If {@code typeId} and {@code objectId} have different namespaces, the format is: <br> {@code TYPE.TYPENAMESPACE.TYPEPATH.OBJECTNAMESPACE.OBJECTPATH}</li>
	 * </ul>
	 * For example, the Austere bee species from Forestry has the translation key: <br> {@code species.forestry.bee.austere} <br>
	 * and the Creeper bee species from Binnie's Extra Bees has the translation key: <br> {@code species.forestry.bee.extrabees.creeper}
	 *
	 * @param type     The first part of the translation key that describes the type of object this is. Can be empty.
	 * @param typeId   The ID of the type. An example would be the ID of the species type.
	 * @param objectId The ID of the object. An example would be the ID of the species.
	 * @return The translation key.
	 */
	public static String createTranslationKey(String type, ResourceLocation typeId, ResourceLocation objectId) {
		String typeNamespace = typeId.getNamespace();
		StringBuilder translationKey = new StringBuilder(type);
		if (!type.isEmpty()) {
			translationKey.append('.');
		}
		translationKey.append(typeNamespace);
		translationKey.append('.');
		translationKey.append(typeId.getPath());
		translationKey.append('.');

		String speciesNamespace = objectId.getNamespace();
		if (speciesNamespace.equals(typeNamespace)) {
			// for species from the same mod as species type, use the following format:
			// species.forestry.bee.austere
			translationKey.append(objectId.getPath());
		} else {
			// if species type is from another mod, use this format instead:
			// species.forestry.bee.extrabees.creeper
			translationKey.append(speciesNamespace);
			translationKey.append('.');
			translationKey.append(objectId.getPath());
		}

		return translationKey.toString();
	}

	public static IdentityHashMap<ISpecies<?>, ItemStack> getIconStacks(ILifeStage stage, ISpeciesType<?, ?> type) {
		IdentityHashMap<ISpecies<?>, ItemStack> map = new IdentityHashMap<>();
		getIconStacks(map, stage, type);
		return map;
	}

	public static void getIconStacks(Map<ISpecies<?>, ItemStack> map, ILifeStage stage, ISpeciesType<?, ?> type) {
		ArrayList<ItemStack> itemList = new ArrayList<>(type.getAllSpecies().size());
		ItemGE.addCreativeItems(stage, itemList, false, type);

		for (ItemStack stack : itemList) {
			IIndividualHandlerItem.ifPresent(stack, individual -> {
				ISpecies<?> species = individual.getSpecies();
				map.put(species, stack);
			});
		}
	}
}

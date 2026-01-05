package forestry.core.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import net.minecraftforge.common.util.LazyOptional;

import forestry.api.ForestryCapabilities;
import forestry.api.apiculture.genetics.IBeeSpecies;
import forestry.api.arboriculture.ITreeSpecies;
import forestry.api.core.IArmorNaturalist;
import forestry.api.genetics.ILifeStage;
import forestry.api.genetics.IMutation;
import forestry.api.genetics.IMutationManager;
import forestry.api.genetics.ISpecies;
import forestry.api.genetics.ISpeciesType;
import forestry.api.genetics.capability.IIndividualHandlerItem;
import forestry.api.lepidopterology.IButterflyNursery;
import forestry.api.lepidopterology.genetics.IButterfly;
import forestry.api.lepidopterology.genetics.IButterflySpecies;
import forestry.arboriculture.capabilities.ArmorNaturalist;
import forestry.compat.curios.CuriosCompat;
import forestry.core.genetics.ItemGE;
import forestry.core.tiles.TileUtil;

public class GeneticsUtil {
	private static String getKeyPrefix(ISpecies<?> allele) {
		if (allele instanceof IBeeSpecies) {
			return "for.bees";
		} else if (allele instanceof ITreeSpecies) {
			return "for.trees";
		} else if (allele instanceof IButterflySpecies) {
			return "for.butterflies";
		}
		throw new IllegalStateException();
	}

	public static Component getAlyzerName(ILifeStage type, ISpecies<?> allele) {
		String customKey = getKeyPrefix(allele) +
			".custom.alyzer." +
			type.getSerializedName() +
			'.' +
			allele.getTranslationKey();
		return Translator.tryTranslate(customKey, allele::getDisplayName);
	}

	public static Component getItemName(ILifeStage type, ISpecies<?> species) {
		String prefix = getKeyPrefix(species);
		String customKey = prefix +
			".custom." +
			type.getSerializedName() +
			'.' +
			species.getTranslationKey();
		return Translator.tryTranslate(customKey, () -> {
			Component speciesName = species.getDisplayName();
			Component typeName = Component.translatable(prefix + ".grammar." + type.getSerializedName() + ".type");
			return Component.translatable(prefix + ".grammar." + type.getSerializedName(), speciesName, typeName);
		});
	}

	public static boolean hasNaturalistEye(Player player) {
		return hasNaturalistEye(player, player.getItemBySlot(EquipmentSlot.HEAD)) || (CuriosCompat.IS_LOADED && CuriosCompat.hasNaturalistEye(player));
	}

	public static boolean hasNaturalistEye(Player player, ItemStack armorItemStack) {
		if (armorItemStack.isEmpty()) {
			return false;
		}
		final IArmorNaturalist armorNaturalist;
		LazyOptional<IArmorNaturalist> armorCap = armorItemStack.getCapability(ForestryCapabilities.ARMOR_NATURALIST);
		if (armorCap.isPresent()) {
			armorNaturalist = armorCap.orElse(ArmorNaturalist.INSTANCE);
		} else {
			return false;
		}

		return armorNaturalist.canSeePollination(player, armorItemStack, true);
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

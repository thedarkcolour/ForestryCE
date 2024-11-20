package forestry.core.gui;

import javax.annotation.Nullable;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import forestry.api.genetics.IBreedingTracker;
import forestry.api.genetics.capability.IIndividualHandlerItem;
import forestry.core.features.CoreItems;
import forestry.core.gui.slots.SlotAnalyzer;
import forestry.core.gui.slots.SlotLockable;
import forestry.core.inventory.ItemInventoryAlyzer;
import forestry.core.utils.GeneticsUtil;

public class ContainerAnalyzerProviderHelper {
	private static final int SLOT_ENERGY = 0;

	private final Player player;
	private final ContainerForestry container;
	@Nullable
	private final ItemInventoryAlyzer alyzerInventory;

	public ContainerAnalyzerProviderHelper(ContainerForestry container, Inventory playerInventory) {
		this.player = playerInventory.player;
		this.container = container;

		ItemInventoryAlyzer alyzerInventory = null;
		for (int i = 0; i < playerInventory.getContainerSize(); i++) {
			ItemStack stack = playerInventory.getItem(i);
			if (stack.isEmpty() || !CoreItems.PORTABLE_ALYZER.itemEqual(stack)) {
				continue;
			}
			alyzerInventory = new ItemInventoryAlyzer(playerInventory.player, stack);
			Slot slot = container.getSlot(i < 9 ? i + 27 : i - 9);
			if (slot instanceof SlotLockable lockable) {
				lockable.lock();
			}
			break;
		}
		this.alyzerInventory = alyzerInventory;

		if (alyzerInventory != null) {
			container.addSlot(new SlotAnalyzer(alyzerInventory, ItemInventoryAlyzer.SLOT_ENERGY, -110, 20));
		}
	}

	@Nullable
	public Slot getAnalyzerSlot() {
		if (alyzerInventory == null) {
			return null;
		}
		for (Slot slot : container.slots) {
			if (slot instanceof SlotAnalyzer) {
				return slot;
			}
		}
		return null;
	}

	public void analyzeSpecimen(int selectedSlot) {
		if (selectedSlot < 0 || alyzerInventory == null) {
			return;
		}
		Slot specimenSlot = container.getForestrySlot(selectedSlot);
		ItemStack specimen = specimenSlot.getItem();
		if (specimen.isEmpty()) {
			return;
		}

		// TODO... how do I convert to a genetic specimen?
		ItemStack convertedSpecimen = GeneticsUtil.convertToGeneticEquivalent(specimen);
		if (!ItemStack.matches(specimen, convertedSpecimen)) {
			specimenSlot.set(convertedSpecimen);
			specimen = convertedSpecimen;
		}

		final ItemStack finalSpecimen = specimen;
		IIndividualHandlerItem.ifPresent(specimen, individual -> {
			if (!individual.isAnalyzed()) {
				ItemStack energyStack = alyzerInventory.getItem(SLOT_ENERGY);
				if (!ItemInventoryAlyzer.isAlyzingFuel(energyStack)) {
					return;
				}

				if (individual.analyze()) {
					IBreedingTracker breedingTracker = individual.getType().getBreedingTracker(player.level(), player.getGameProfile());
					breedingTracker.registerSpecies(individual.getSpecies());
					// todo should inactive species count?
					//breedingTracker.registerSpecies(individual.getGenome().getSecondarySpecies());

					individual.saveToStack(finalSpecimen);

					// Decrease energy
					alyzerInventory.removeItem(SLOT_ENERGY, 1);
				}
			}
		});
	}
}

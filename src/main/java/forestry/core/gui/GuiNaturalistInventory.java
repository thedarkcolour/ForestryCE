package forestry.core.gui;

import com.google.common.collect.ImmutableList;
import forestry.api.apiculture.IApiaristTracker;
import forestry.api.genetics.*;
import forestry.api.genetics.alleles.IRegistryChromosome;
import forestry.api.genetics.capability.IIndividualHandlerItem;
import forestry.core.config.Constants;
import forestry.core.gui.buttons.GuiBetterButton;
import forestry.core.gui.buttons.StandardButtonTextureSets;
import forestry.core.network.packets.PacketGuiSelectRequest;
import forestry.core.render.ColourProperties;
import forestry.core.utils.NetworkUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class GuiNaturalistInventory<C extends AbstractContainerMenu & INaturalistMenu> extends GuiForestry<C> {
	private final ISpeciesType<?, ?> speciesType;
	private final IBreedingTracker breedingTracker;
	private final HashMap<ResourceLocation, ItemStack> iconStacks = new HashMap<>();
	private final int pageCurrent, pageMax;
	private final CycleTimer timer = new CycleTimer(0);

	public GuiNaturalistInventory(C menu, Inventory playerInv, Component name) {
		super(Constants.TEXTURE_PATH_GUI + "/apiaristinventory.png", menu, playerInv, name);

		this.speciesType = menu.getSpeciesType();

		this.pageCurrent = menu.getCurrentPage();
		this.pageMax = ContainerNaturalistInventory.MAX_PAGE;

        this.imageWidth = 196;
        this.imageHeight = 202;

		// todo have one place where icon stacks are stored
		for (ISpecies species : this.speciesType.getAllSpecies()) {
            this.iconStacks.put(species.id(), species.createStack(species.createIndividual(), this.speciesType.getDefaultStage()));
		}

        this.breedingTracker = this.speciesType.getBreedingTracker(playerInv.player.level(), playerInv.player.getGameProfile());
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTicks, int j, int i) {
		super.renderBg(graphics, partialTicks, j, i);
        this.timer.onDraw();
		Component header = Component.translatable("for.gui.page").append(" " + (this.pageCurrent + 1) + "/" + this.pageMax);
		graphics.drawString(this.font, header, this.leftPos + 95 + this.textLayout.getCenteredOffset(header, 98), this.topPos + 10, ColourProperties.INSTANCE.get("gui.title"), false);

		IIndividual individual = getHoveredIndividual();
		if (individual == null) {
			displayBreedingStatistics(graphics, 10);
		}

		if (individual != null) {
            this.textLayout.startPage(graphics);

			IGenome genome = individual.getGenome();
			IRegistryChromosome<? extends ISpecies<?>> speciesChromosome = individual.getType().getKaryotype().getSpeciesChromosome();
			// var allows generics to compile :)
			var speciesPair = genome.getAllelePair(speciesChromosome);
			boolean pureBred = speciesPair.isSameAlleles();

			ISpecies<?> active = speciesPair.active().value();
			displaySpeciesInformation(graphics, true, active, this.iconStacks.get(active.id()), 10, pureBred ? 25 : 10);
			if (!pureBred) {
				ISpecies<?> inactive = speciesPair.inactive().value();
				displaySpeciesInformation(graphics, individual.isAnalyzed(), inactive, this.iconStacks.get(inactive.id()), 10, 10);
			}

            this.textLayout.endPage(graphics);
		}
	}

	@Override
	public void init() {
		super.init();

		addRenderableWidget(new GuiBetterButton(this.leftPos + 99, this.topPos + 7, StandardButtonTextureSets.LEFT_BUTTON_SMALL, b -> {
			if (this.pageCurrent > 0) {
				flipPage(this.pageCurrent - 1);
			}
		}));
		addRenderableWidget(new GuiBetterButton(this.leftPos + 180, this.topPos + 7, StandardButtonTextureSets.RIGHT_BUTTON_SMALL, b -> {
			if (this.pageCurrent < this.pageMax - 1) {
				flipPage(this.pageCurrent + 1);
			}
		}));
	}

	private void flipPage(int page) {
        this.menu.onFlipPage();
		NetworkUtil.sendToServer(new PacketGuiSelectRequest(page, 0));
	}

	@Nullable
	private IIndividual getHoveredIndividual() {
		Slot slot = this.hoveredSlot;
		if (slot == null) {
			return null;
		}

		if (!slot.hasItem()) {
			return null;
		}

		if (!slot.getItem().hasTag()) {
			return null;
		}

		if (!this.speciesType.isMember(slot.getItem())) {
			return null;
		}

		return IIndividualHandlerItem.getIndividual(slot.getItem());
	}

	private void displayBreedingStatistics(GuiGraphics graphics, int x) {
        this.textLayout.startPage(graphics);

        this.textLayout.drawLine(graphics, Component.translatable("for.gui.speciescount").append(": ").append(this.breedingTracker.getSpeciesBred() + "/" + this.speciesType.getSpeciesCount()), x);
        this.textLayout.newLine();
        this.textLayout.newLine();

		if (this.breedingTracker instanceof IApiaristTracker tracker) {
            this.textLayout.drawLine(graphics, Component.translatable("for.gui.queens").append(": ").append(Integer.toString(tracker.getQueenCount())), x);
            this.textLayout.newLine();

            this.textLayout.drawLine(graphics, Component.translatable("for.gui.princesses").append(": ").append(Integer.toString(tracker.getPrincessCount())), x);
            this.textLayout.newLine();

            this.textLayout.drawLine(graphics, Component.translatable("for.gui.drones").append(": ").append(Integer.toString(tracker.getDroneCount())), x);
            this.textLayout.newLine();
		}

        this.textLayout.endPage(graphics);
	}

	private void displaySpeciesInformation(GuiGraphics graphics, boolean analyzed, ISpecies<?> species, ItemStack iconStack, int x, int maxMutationCount) {
		if (!analyzed) {
            this.textLayout.drawLine(graphics, Component.translatable("for.gui.unknown"), x);
			return;
		}

        this.textLayout.drawLine(graphics, species.getDisplayName(), x);
		GuiUtil.drawItemStack(graphics, this, iconStack, this.leftPos + x + 67, this.topPos + this.textLayout.getLineY() - 4);

        this.textLayout.newLine();

		// Viable Combinations
		int columnWidth = 16;
		int column = 10;

		@SuppressWarnings("rawtypes")
		IMutationManager manager = this.speciesType.getMutations();
		List<List<? extends IMutation<?>>> mutations = splitMutations(manager.getMutationsFrom(species), maxMutationCount);
		for (IMutation<?> combination : this.timer.getCycledItem(mutations, Collections::emptyList)) {
			if (combination.isSecret()) {
				continue;
			}

			if (this.breedingTracker.isDiscovered(combination)) {
				drawMutationIcon(graphics, combination, species, column);
			} else {
				drawUnknownIcon(graphics, combination, column);
			}

			column += columnWidth;
			if (column > 75) {
				column = 10;
                this.textLayout.newLine(18);
			}
		}

        this.textLayout.newLine();
        this.textLayout.newLine();
	}

	private void drawMutationIcon(GuiGraphics graphics, IMutation<?> combination, ISpecies<?> species, int x) {
		GuiUtil.drawItemStack(graphics, this, this.iconStacks.get(combination.getPartner(species).id()), this.leftPos + x, this.topPos + this.textLayout.getLineY());
	}

	private void drawUnknownIcon(GuiGraphics graphics, IMutation<?> mutation, int x) {
		float chance = mutation.getChance();

		int line;
		int column;
		if (chance >= 20) {
			line = 16;
			column = 228;
		} else if (chance >= 15) {
			line = 16;
			column = 212;
		} else if (chance >= 12) {
			line = 16;
			column = 196;
		} else if (chance >= 10) {
			line = 0;
			column = 228;
		} else if (chance >= 5) {
			line = 0;
			column = 212;
		} else {
			line = 0;
			column = 196;
		}

		graphics.blit(this.textureFile, this.leftPos + x, this.topPos + this.textLayout.getLineY(), column, line, 16, 16);
	}

	private static List<List<? extends IMutation<?>>> splitMutations(List<? extends IMutation<?>> mutations, int maxMutationCount) {
		int size = mutations.size();
		if (size <= maxMutationCount) {
			return Collections.singletonList(mutations);
		}
		ImmutableList.Builder<List<? extends IMutation<?>>> subGroups = new ImmutableList.Builder<>();
		List<IMutation<?>> subList = new LinkedList<>();
		subGroups.add(subList);
		int count = 0;
		for (IMutation<?> mutation : mutations) {
			if (mutation.isSecret()) {
				continue;
			}
			if (count % maxMutationCount == 0 && count != 0) {
				subList = new LinkedList<>();
				subGroups.add(subList);
			}
			subList.add(mutation);
			count++;
		}
		return subGroups.build();
	}

	@Override
	protected void addLedgers() {
		addHintLedger("naturalist.chest");
	}
}

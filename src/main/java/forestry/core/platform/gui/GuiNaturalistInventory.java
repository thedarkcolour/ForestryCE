package forestry.core.platform.gui;

import com.google.common.collect.ImmutableList;
import forestry.api.apiculture.IApiaristTracker;
import forestry.api.core.genetics.*;
import forestry.api.core.genetics.alleles.IChromosome;
import forestry.api.core.genetics.capability.IIndividualHandlerItem;
import forestry.core.features.CoreItems;
import forestry.core.platform.config.Constants;
import forestry.core.platform.gui.ledgers.Ledger;
import forestry.core.platform.network.packets.PacketGuiSelectRequest;
import forestry.core.platform.util.NBTUtilForestry;
import forestry.core.platform.util.NetworkUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
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
	private static final ResourceLocation SCROLLER_SPRITE = ResourceLocation.withDefaultNamespace("container/creative_inventory/scroller");
	private static final int INVENTORY_X = 6;
	private static final int INVENTORY_Y = 6;
	private static final int INVENTORY_WIDTH = ContainerNaturalistInventory.COLUMNS * 18;
	private static final int SCROLL_X = 153;
	private static final int SCROLL_Y = 6;
	private static final int SCROLL_WIDTH = 14;
	private static final int SCROLL_HEIGHT = 90;
	private static final int SCROLLER_HEIGHT = 15;
	private static final int SCROLLER_WIDTH = 12;
	private static final int SCROLL_U = 176;
	private static final int SCROLL_V = 0;

	private final ISpeciesType<?, ?> speciesType;
	private final IBreedingTracker breedingTracker;
	private final ItemStack ledgerIcon;
	private final HashMap<ResourceLocation, ItemStack> iconStacks = new HashMap<>();
	private final CycleTimer timer = new CycleTimer(0);
	private boolean scrolling;

	public GuiNaturalistInventory(C menu, Inventory playerInv, Component name) {
		super(Constants.TEXTURE_PATH_GUI + "/naturalistinventory.png", menu, playerInv, name);

		this.speciesType = menu.getSpeciesType();
		this.imageWidth = 176;
		this.imageHeight = 189;

		// todo have one place where icon stacks are stored
		for (ISpecies species : this.speciesType.getAllSpecies()) {
			this.iconStacks.put(species.id(), species.createStack(species.createIndividual(), this.speciesType.getDefaultStage()));
		}

		this.breedingTracker = this.speciesType.getBreedingTracker(playerInv.player.level(), playerInv.player.getGameProfile());
		this.ledgerIcon = CoreItems.PORTABLE_ANALYZER.stack();
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTicks, int j, int i) {
		super.renderBg(graphics, partialTicks, j, i);
		this.timer.onDraw();
		drawScrollBar(graphics);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
		if (mouseButton == 0 && isOverScrollBar(mouseX, mouseY)) {
			this.scrolling = true;
			scrollTo(mouseY);
			return true;
		}
		return super.mouseClicked(mouseX, mouseY, mouseButton);
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int mouseButton, double dragX, double dragY) {
		if (this.scrolling && mouseButton == 0) {
			scrollTo(mouseY);
			return true;
		}
		return super.mouseDragged(mouseX, mouseY, mouseButton, dragX, dragY);
	}

	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int mouseButton) {
		this.scrolling = false;
		return super.mouseReleased(mouseX, mouseY, mouseButton);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		if (scrollY != 0 && isOverScrollableArea(mouseX, mouseY)) {
			setScrollRow(this.menu.getScrollRow() - (int) Math.signum(scrollY));
			return true;
		}
		return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
	}

	private void drawScrollBar(GuiGraphics graphics) {
		graphics.blit(
			this.textureFile,
			this.leftPos + SCROLL_X + 1,
			this.topPos + SCROLL_Y,
			SCROLL_WIDTH,
			SCROLL_HEIGHT,
			SCROLL_U,
			SCROLL_V,
			SCROLL_WIDTH,
			SCROLL_HEIGHT,
			256,
			256
		);

		int range = SCROLL_HEIGHT - SCROLLER_HEIGHT - 2;
		int offset = 1 + Math.round(
			(float) this.menu.getScrollRow()
				/ ContainerNaturalistInventory.MAX_SCROLL
				* range
		);

		graphics.blitSprite(
			SCROLLER_SPRITE,
			this.leftPos + SCROLL_X + 2,
			this.topPos + SCROLL_Y + offset,
			SCROLLER_WIDTH,
			SCROLLER_HEIGHT
		);
	}

	private boolean isOverScrollableArea(double mouseX, double mouseY) {
		return isHovering(INVENTORY_X, INVENTORY_Y, INVENTORY_WIDTH, SCROLL_HEIGHT, mouseX, mouseY) || isOverScrollBar(mouseX, mouseY);
	}

	private boolean isOverScrollBar(double mouseX, double mouseY) {
		return isHovering(SCROLL_X, SCROLL_Y, SCROLL_WIDTH, SCROLL_HEIGHT, mouseX, mouseY);
	}

	private void scrollTo(double mouseY) {
		double range = SCROLL_HEIGHT - SCROLLER_HEIGHT - 2;
		double position = mouseY - this.topPos - SCROLL_Y - 1 - SCROLLER_HEIGHT / 2.0;
		int row = (int) Math.round(
			Mth.clamp(position / range, 0.0, 1.0)
				* ContainerNaturalistInventory.MAX_SCROLL
		);
		setScrollRow(row);
	}

	private void setScrollRow(int row) {
		row = Mth.clamp(row, 0, ContainerNaturalistInventory.MAX_SCROLL);
		if (this.menu.getScrollRow() == row) {
			return;
		}

		this.menu.setScrollRow(row);
		NetworkUtil.sendToServer(new PacketGuiSelectRequest(row, 0));
	}

	@Nullable
	private IIndividual getHoveredIndividual() {
		Slot slot = this.hoveredSlot;
		if (slot == null || !slot.hasItem() || NBTUtilForestry.getItemStackTag(slot.getItem()) == null || !this.speciesType.isMember(slot.getItem())) {
			return null;
		}

		return IIndividualHandlerItem.getIndividual(slot.getItem());
	}

	private void drawUnknownIcon(GuiGraphics graphics, IMutation<?> mutation, int x, int y) {
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

		graphics.blit(this.textureFile, x, y, column, line, 16, 16);
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
		this.ledgerManager.add(new NaturalistInfoLedger());
		addHintLedger("naturalist.chest");
	}

	private class NaturalistInfoLedger extends Ledger {
		private NaturalistInfoLedger() {
			super(GuiNaturalistInventory.this.ledgerManager, "naturalist", false, 136);
			this.maxHeight = 126;
		}

		@Override
		public void draw(GuiGraphics graphics, int y, int x) {
			drawBackground(graphics, y, x);
			GuiUtil.drawItemStack(graphics, GuiNaturalistInventory.this, GuiNaturalistInventory.this.ledgerIcon, x + 4, y + 4);

			if (!isFullyOpened()) {
				return;
			}
			drawHeader(graphics, Component.translatable("for.gui.naturalist.statistics", GuiNaturalistInventory.this.speciesType.getDisplayName()), x + 22, y + 8);
			graphics.fill(x + 4, y + 22, x + getWidth() - 4, y + getHeight() - 4, 0xff000000);

			IIndividual individual = GuiNaturalistInventory.this.getHoveredIndividual();
			if (individual == null) {
				drawStatistics(graphics, x, y);
			} else {
				drawIndividual(graphics, individual, x, y);
			}
		}

		private void drawStatistics(GuiGraphics graphics, int x, int y) {
			int textY = y + 28;

			drawSplitText(
				graphics,
				Component.translatable("for.gui.speciescount")
					.append(": ")
					.append(GuiNaturalistInventory.this.breedingTracker.getSpeciesBred()
						+ "/" + GuiNaturalistInventory.this.speciesType.getSpeciesCount()),
				x + 10,
				textY,
				this.maxTextWidth
			);

			if (GuiNaturalistInventory.this.breedingTracker instanceof IApiaristTracker tracker) {
				textY += 18;

				drawHeader(graphics, Component.translatable("for.gui.breeding").append(": "), x + 10, textY);

				textY += 14;
				drawSplitText(graphics, Component.translatable("for.gui.queens").append(": ").append(Integer.toString(tracker.getQueenCount())), x + 12, textY, this.maxTextWidth);

				textY += 14;
				drawSplitText(graphics, Component.translatable("for.gui.princesses").append(": ").append(Integer.toString(tracker.getPrincessCount())), x + 12, textY, this.maxTextWidth);

				textY += 14;
				drawSplitText(graphics, Component.translatable("for.gui.drones").append(": ").append(Integer.toString(tracker.getDroneCount())), x + 12, textY, this.maxTextWidth);
			}
		}

		private void drawIndividual(GuiGraphics graphics, IIndividual individual, int x, int y) {
			IGenome genome = individual.getGenome();
			IChromosome<ResourceLocation> chromosome = individual.getType().getKaryotype().getSpeciesChromosome();
			boolean pureBred = genome.getAllelePair(chromosome).isSameAlleles();

			int nextY = drawSpecies(graphics, true, genome.getActiveSpecies(), x, y + 32, pureBred ? 25 : 10);
			if (!pureBred) {
				drawSpecies(graphics, individual.isAnalyzed(), genome.getInactiveSpecies(), x, nextY, 10);
			}
		}

		private int drawSpecies(GuiGraphics graphics, boolean analyzed, ISpecies<?> species, int x, int y, int maxMutationCount) {
			if (!analyzed) {
				drawHeader(graphics, Component.translatable("for.gui.unknown"), x + 22, y);
				return y + 22;
			}

			ItemStack icon = GuiNaturalistInventory.this.iconStacks.get(species.id());
			GuiUtil.drawItemStack(graphics, GuiNaturalistInventory.this, icon, x + 8, y - 4);
			drawHeader(graphics, species.getDisplayName(), x + 28, y);

			@SuppressWarnings("rawtypes")
			IMutationManager manager = GuiNaturalistInventory.this.speciesType.getMutations();
			List<List<? extends IMutation<?>>> groups = splitMutations(manager.getMutationsFrom(species), maxMutationCount);
			List<? extends IMutation<?>> mutations = GuiNaturalistInventory.this.timer.getCycledItem(groups, Collections::emptyList);

			int visible = 0;
			for (IMutation<?> mutation : mutations) {
				if (!mutation.isSecret()) {
					visible++;
				}
			}

			int mutationY = y + 16;
			int index = 0;
			for (IMutation<?> mutation : mutations) {
				if (mutation.isSecret()) {
					continue;
				}

				int itemX = x + 10 + (index % 5) * 18;
				int itemY = mutationY + (index / 5) * 18;
				if (GuiNaturalistInventory.this.breedingTracker.isDiscovered(mutation)) {
					GuiUtil.drawItemStack(graphics, GuiNaturalistInventory.this, GuiNaturalistInventory.this.iconStacks.get(mutation.getPartner(species).id()), itemX, itemY);
				} else {
					GuiNaturalistInventory.this.drawUnknownIcon(graphics, mutation, itemX, itemY);
				}
				index++;
			}

			return mutationY + Math.max(1, (visible + 4) / 5) * 18 + 4;
		}

		@Override
		public Component getTooltip() {
			return Component.translatable(
				"for.gui.naturalist.statistics",
				GuiNaturalistInventory.this.speciesType.getDisplayName()
			);
		}
		@Override
		public boolean shouldDrawTooltip() {
			return !isOpen() && getWidth() == minWidth;
		}
	}
}

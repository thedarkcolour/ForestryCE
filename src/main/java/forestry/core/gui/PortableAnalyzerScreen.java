package forestry.core.gui;

import forestry.api.client.IForestryClientApi;
import forestry.api.client.genetics.IAnalyzerGraphics;
import forestry.api.client.genetics.IAnalyzerPlugin;
import forestry.api.genetics.IIndividual;
import forestry.api.genetics.ILifeStage;
import forestry.api.genetics.ISpecies;
import forestry.api.genetics.ISpeciesType;
import forestry.api.genetics.capability.IIndividualHandlerItem;
import forestry.apiimpl.client.genetics.AnalyzerScreenGraphics;
import forestry.core.ForestryColors;
import forestry.core.config.Constants;
import forestry.core.gui.widgets.WidgetManager;
import forestry.core.inventory.ItemInventoryAlyzer;
import forestry.core.render.ColourProperties;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.List;

// Portable analyzer
public class PortableAnalyzerScreen extends GuiForestry<ContainerAlyzer> {
	private final ItemInventoryAlyzer itemInventory;

	public PortableAnalyzerScreen(ContainerAlyzer container, Inventory playerInv, Component name) {
		super(Constants.TEXTURE_PATH_GUI + "/portablealyzer.png", container, playerInv, name);

		this.itemInventory = container.inventory;
		this.imageWidth = 247;
		this.imageHeight = 238;
	}

	public static int getColorCoding(boolean dominant) {
		return dominant ? ForestryColors.DOMINANT_RED : ForestryColors.RECESSIVE_BLUE;
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
		super.renderBg(graphics, partialTicks, mouseX, mouseY);
		this.widgetManager.clear();

		int specimenSlot = getSpecimenSlot();
		if (specimenSlot < ItemInventoryAlyzer.SLOT_ANALYZE_1) {
			drawAnalyticsOverview(graphics);
			return;
		}

		ItemStack stackInSlot = this.itemInventory.getItem(specimenSlot);

		IIndividualHandlerItem.ifPresent(stackInSlot, (individual, stage) -> {
			drawIndividualInfo(graphics, mouseX, mouseY, specimenSlot, individual, stage, stackInSlot);
		});
	}

	private <S extends ISpecies<I>, I extends IIndividual> void drawIndividualInfo(GuiGraphics graphics, int mouseX, int mouseY, int slot, I individual, ILifeStage stage, ItemStack stack) {
		ISpeciesType<S, I> type = individual.getType().cast();
		IAnalyzerPlugin<S, I> plugin = IForestryClientApi.INSTANCE.getGeneticManager().getAnalyzerPlugin(type);

		// prefer new style plugin, but fallback to old style
		if (plugin != null) {
			IAnalyzerGraphics<S, I> analyzerGraphics = new AnalyzerScreenGraphics<>(graphics, this, mouseX, mouseY, individual);

			switch (slot) {
				case ItemInventoryAlyzer.SLOT_ANALYZE_1 -> plugin.drawPage1(analyzerGraphics, individual, stage, stack);
				case ItemInventoryAlyzer.SLOT_ANALYZE_2 -> plugin.drawPage2(analyzerGraphics, individual, stage, stack);
				case ItemInventoryAlyzer.SLOT_ANALYZE_3 -> plugin.drawPage3(analyzerGraphics, individual, stage, stack);
				case ItemInventoryAlyzer.SLOT_ANALYZE_4 -> plugin.drawPage4(analyzerGraphics, individual, stage, stack);
				case ItemInventoryAlyzer.SLOT_ANALYZE_5 -> plugin.drawPage5(analyzerGraphics, individual, stage, stack);
				default -> drawAnalyticsOverview(graphics);
			}
		} else {
			// draw "unsupported" screen
		}
	}

	private int getSpecimenSlot() {
		for (int k = ItemInventoryAlyzer.SLOT_SPECIMEN; k <= ItemInventoryAlyzer.SLOT_ANALYZE_5; k++) {
			ItemStack stackInSlot = this.itemInventory.getItem(k);

			if (!stackInSlot.isEmpty() && IIndividualHandlerItem.filter(stackInSlot, IIndividual::isAnalyzed)) {
				return k;
			}
		}
		return -1;
	}

	public void drawAnalyticsOverview(GuiGraphics graphics) {
		this.textLayout.startPage(graphics);

		this.textLayout.newLine();
		Component title = Component.translatable("for.gui.portablealyzer");
		this.textLayout.drawCenteredLine(graphics, title, 8, 208, ColourProperties.INSTANCE.get("gui.screen"));
		this.textLayout.newLine();

		graphics.drawWordWrap(this.font, Component.translatable("for.gui.portablealyzer.help"), this.leftPos + 0 + 16, this.topPos + 42, 200, ColourProperties.INSTANCE.get("gui.screen"));
		this.textLayout.newLine();
		this.textLayout.newLine();
		this.textLayout.newLine();
		this.textLayout.newLine();

		this.textLayout.drawLine(graphics, Component.translatable("for.gui.alyzer.overview").append(":"), 0 + 16);
		this.textLayout.newLine();
		this.textLayout.drawLine(graphics, Component.literal("I  : ").append(Component.translatable("for.gui.general")), 0 + 16);
		this.textLayout.newLine();
		this.textLayout.drawLine(graphics, Component.literal("II : ").append(Component.translatable("for.gui.environment")), 0 + 16);
		this.textLayout.newLine();
		this.textLayout.drawLine(graphics, Component.literal("III: ").append(Component.translatable("for.gui.produce")), 0 + 16);
		this.textLayout.newLine();
		this.textLayout.drawLine(graphics, Component.literal("IV : ").append(Component.translatable("for.gui.evolution")), 0 + 16);

		this.textLayout.endPage(graphics);
	}

	public WidgetManager getWidgetManager() {
		return this.widgetManager;
	}

	@Override
	protected void addLedgers() {
		addErrorLedger(this.itemInventory);
		addHintLedger(getHints());
	}

	public List<String> getHints() {
		IIndividual individual = IIndividualHandlerItem.getIndividual(this.itemInventory.getSpecimen());
		if (individual == null) {
			return List.of();
		}

		IAnalyzerPlugin<?, ?> plugin = IForestryClientApi.INSTANCE.getGeneticManager().getAnalyzerPlugin(individual.getType());
		if (plugin == null) {
			return List.of();
		}

		return plugin.getHints();
	}
}

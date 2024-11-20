package forestry.energy.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

import forestry.api.ForestryConstants;
import forestry.api.client.IForestryClientApi;
import forestry.core.gui.GuiForestryTitled;
import forestry.core.gui.GuiUtil;
import forestry.core.gui.ledgers.Ledger;
import forestry.core.gui.ledgers.LedgerManager;
import forestry.energy.tiles.EngineBlockEntity;

public class EngineScreen<M extends AbstractContainerMenu, E extends EngineBlockEntity> extends GuiForestryTitled<M> {
	protected final E engine;

	public EngineScreen(String texture, M menu, Inventory inv, Component title, E engine) {
		super(texture, menu, inv, title);
		this.engine = engine;
	}

	protected static class EngineLedger<E extends EngineBlockEntity> extends Ledger {
		private final E engine;

		public EngineLedger(LedgerManager manager, E engine) {
			super(manager, "power");

			this.engine = engine;
			this.maxHeight = 94;
		}

		@Override
		public void draw(GuiGraphics graphics, int y, int x) {
			drawBackground(graphics, y, x);

			drawSprite(graphics, IForestryClientApi.INSTANCE.getTextureManager().getSprite(ForestryConstants.forestry("misc/energy")), x + 3, y + 4);

			if (isFullyOpened()) {
				drawHeader(graphics, Component.translatable("for.gui.energy"), x + 22, y + 8);

				drawSubheader(graphics, Component.translatable("for.gui.currentOutput").append(":"), x + 22, y + 20);
				drawText(graphics, GuiUtil.formatRate(engine.getCurrentOutput()), x + 22, y + 32);

				drawSubheader(graphics, Component.translatable("for.gui.stored").append(":"), x + 22, y + 44);
				drawText(graphics, GuiUtil.formatEnergyValue(engine.getEnergyManager().getEnergyStored()), x + 22, y + 56);

				drawSubheader(graphics, Component.translatable("for.gui.heat").append(":"), x + 22, y + 68);
				drawText(graphics, (double) engine.getHeat() / (double) 10 + 20.0 + " C", x + 22, y + 80);
			}
		}

		@Override
		public Component getTooltip() {
			return Component.translatable(GuiUtil.formatRate(engine.getCurrentOutput()));
		}
	}

	@Override
	protected void addLedgers() {
		addErrorLedger(engine);
		addHintLedger(engine.getHintKey());
		ledgerManager.add(new EngineLedger<>(ledgerManager, engine));
	}
}

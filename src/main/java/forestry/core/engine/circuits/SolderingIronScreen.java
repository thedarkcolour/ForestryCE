package forestry.core.engine.circuits;

import forestry.api.IForestryApi;
import forestry.api.core.circuits.ForestryCircuitSocketTypes;
import forestry.api.core.circuits.ICircuit;
import forestry.api.core.circuits.ICircuitLayout;
import forestry.api.agriculture.HorizontalDirection;
import forestry.core.platform.config.Constants;
import forestry.core.platform.gui.ForestryScreen;
import forestry.core.platform.inventory.SolderingIronInventory;
import forestry.core.platform.render.ColourProperties;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.Locale;

public class SolderingIronScreen extends ForestryScreen<SolderingIronMenu> {
	private final SolderingIronInventory itemInventory;

	public SolderingIronScreen(SolderingIronMenu container, Inventory inv, Component title) {
		super(Constants.TEXTURE_PATH_GUI + "/solder.png", container, inv, title);

		this.itemInventory = container.getItemInventory();
		this.imageWidth = 176;
		this.imageHeight = 205;
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
		super.renderBg(graphics, partialTicks, mouseX, mouseY);

		ICircuitLayout layout = this.menu.getLayout();
		Component title = layout.getName();
		graphics.drawString(this.font, title, this.leftPos + 8 + this.textLayout.getCenteredOffset(title, 138), this.topPos + 16, ColourProperties.INSTANCE.get("gui.screen"), false);

		for (int i = 0; i < 4; i++) {
			Component description;
			ItemStack tube = this.itemInventory.getItem(i + 2);
			ICircuit circuit = IForestryApi.INSTANCE.getCircuitManager().getCircuit(layout, tube);
			if (circuit == null) {
				description = Component.literal("(").append(Component.translatable("for.gui.noeffect")).append(")");
			} else {
				description = circuit.getDisplayName();
			}

			int row = i * 20;
			graphics.drawString(this.font, description, this.leftPos + 32, this.topPos + 36 + row, ColourProperties.INSTANCE.get("gui.screen"), false);

			if (tube.isEmpty()) {
				if (ForestryCircuitSocketTypes.FARM == layout.getSocketType()) {
					Direction farmDirection = HorizontalDirection.VALUES.get(i);
					String farmDirectionString = farmDirection.toString().toLowerCase(Locale.ENGLISH);
					Component localizedDirection = Component.translatable("for.gui.solder." + farmDirectionString);
					graphics.drawString(this.font, localizedDirection, this.leftPos + 17, this.topPos + 36 + row, ColourProperties.INSTANCE.get("gui.screen"), false);
				}
			}
		}
	}

	@Override
	public void init() {
		super.init();

		addRenderableWidget(Button.builder(Component.literal("<"), b -> SolderingIronMenu.regressSelection(0))
			.pos(this.leftPos + 12, this.topPos + 10)
			.size(12, 18)
			.build());
		addRenderableWidget(Button.builder(Component.literal(">"), b -> SolderingIronMenu.advanceSelection(0))
			.pos(this.leftPos + 130, this.topPos + 10)
			.size(12, 18)
			.build());
	}

	@Override
	protected void addLedgers() {
		addErrorLedger(this.itemInventory);
		addHintLedger("soldering.iron");
	}
}

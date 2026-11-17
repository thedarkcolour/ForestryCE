package forestry.mail.gui;

import forestry.api.ForestryRegistries;
import forestry.api.mail.IMailAddress;
import forestry.api.mail.IPostalCarrier;
import forestry.core.platform.config.Constants;
import forestry.core.platform.config.SessionVars;
import forestry.core.platform.gui.ForestryScreen;
import forestry.core.platform.gui.GuiTextBox;
import forestry.core.platform.gui.widgets.ItemStackWidget;
import forestry.core.platform.gui.widgets.Widget;
import forestry.core.platform.render.ColourProperties;
import forestry.core.platform.util.NetworkUtil;
import forestry.mail.carriers.PostalCarriers;
import forestry.mail.inventory.LetterInventory;
import forestry.mail.network.packets.PacketLetterInfoRequest;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Locale;

public class LetterScreen extends ForestryScreen<LetterMenu> {
	private final LetterInventory itemInventory;
	private final boolean isProcessedLetter;
	private boolean checkedSessionVars;

	private EditBox address;
	private GuiTextBox text;

	private boolean addressFocus;
	private boolean textFocus;
	private String lastSyncedRecipient = "";
	private IPostalCarrier lastSyncedCarrier;
	private String lastSyncedText = "";

	private final ArrayList<Widget> tradeInfoWidgets;

	public LetterScreen(LetterMenu container, Inventory inv, Component title) {
		super(Constants.TEXTURE_PATH_GUI + "/letter.png", container, inv, title);
		this.minecraft = Minecraft.getInstance(); //not 100% why this is needed, maybe side issues

		this.itemInventory = container.getItemInventory();
		this.imageWidth = 194;
		this.imageHeight = 227;

		this.isProcessedLetter = container.getLetter().isProcessed();
		this.widgetManager.add(new AddresseeSlot(this.widgetManager, 16, 12, container));
		this.tradeInfoWidgets = new ArrayList<>();
	}

	@Override
	public void init() {
		super.init();

        this.address = new EditBox(this.minecraft.font, this.leftPos + 46, this.topPos + 13, 93, 13, Component.translatable("for.gui.mail.to"));
        this.address.setEditable(!this.isProcessedLetter);
		IMailAddress recipient = this.menu.getRecipient();
		if (recipient != null) {
            this.address.setValue(recipient.getName());
		}
        this.lastSyncedRecipient = this.address.getValue();
        this.lastSyncedCarrier = this.menu.getCarrier();
        this.address.setResponder(recipientName -> setRecipient(recipientName, this.menu.getCarrier()));

        this.text = new GuiTextBox(this.minecraft.font, this.leftPos + 17, this.topPos + 31, 122, 57);
        this.text.setMaxLength(128);
        this.text.setEditable(!this.isProcessedLetter);
		if (!this.menu.getText().isEmpty()) {
            this.text.setValue(this.menu.getText());
		}
        this.lastSyncedText = this.text.getValue();
        this.text.setResponder(this::setText);

		addWidget(this.address);
		addWidget(this.text);
	}

	@Override
	public boolean keyPressed(int key, int scanCode, int modifiers) {
		if (this.isProcessedLetter) {
			return super.keyPressed(key, scanCode, modifiers);
		}

		// Set focus or enter text into address
		if (this.address.isFocused()) {
			if (key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_ESCAPE) {
				this.address.setFocused(false);
			} else if (key == GLFW.GLFW_KEY_TAB) {
				// Name autocomplete
				String currentValue = this.address.getValue().toLowerCase(Locale.ENGLISH);
				Minecraft.getInstance().getConnection().getOnlinePlayers().stream()
					.map(info -> info.getProfile().getName())
					.filter(name -> name.toLowerCase(Locale.ENGLISH).contains(currentValue))
					.findFirst()
					.ifPresent(name -> this.address.setValue(name));
			} else {
				this.address.keyPressed(key, scanCode, modifiers);
			}
			return true;
		}

		if (this.text.isFocused()) {
			if (key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_ESCAPE) {
				if (hasShiftDown() && key != GLFW.GLFW_KEY_ESCAPE) {
                    this.text.setValue(this.text.getValue() + "\n");
				} else {
					this.text.setFocused(false);
				}
			} else if (key == GLFW.GLFW_KEY_DOWN) {
                this.text.advanceLine();
			} else if (key == GLFW.GLFW_KEY_UP) {
                this.text.regressLine();
			} else if (this.text.moreLinesAllowed() || key == GLFW.GLFW_KEY_DELETE || key == GLFW.GLFW_KEY_BACKSLASH) {
				this.text.keyPressed(key, scanCode, modifiers);
			}
			return true;
		}

		return super.keyPressed(key, scanCode, modifiers);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
		if (super.mouseClicked(mouseX, mouseY, mouseButton)) {
			return true;
		}
		this.address.mouseClicked(mouseX, mouseY, mouseButton);
		this.text.mouseClicked(mouseX, mouseY, mouseButton);
		return true;
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseY, int mouseX) {

		if (!this.isProcessedLetter && !this.checkedSessionVars) {
            this.checkedSessionVars = true;
			setFromSessionVars();
			String recipient = this.address.getValue();
			IPostalCarrier carrier = this.menu.getCarrier();
			setRecipient(recipient, carrier);
		}

		// Check for focus changes
		if (this.addressFocus != this.address.isFocused()) {
			String recipient = this.address.getValue();
			IPostalCarrier carrier = this.menu.getCarrier();
			if (StringUtils.isNotBlank(recipient)) {
				setRecipient(recipient, carrier);
			}
		}
        this.addressFocus = this.address.isFocused();
		if (this.lastSyncedCarrier != this.menu.getCarrier() && StringUtils.isNotBlank(this.address.getValue())) {
			setRecipient(this.address.getValue(), this.menu.getCarrier());
		}
		if (this.textFocus != this.text.isFocused()) {
			setText();
		}
        this.textFocus = this.text.isFocused();

		super.renderBg(graphics, partialTicks, mouseY, mouseX);

		if (this.isProcessedLetter) {
			graphics.drawString(this.font, this.address.getValue(), this.leftPos + 49, this.topPos + 16, ColourProperties.INSTANCE.get("gui.mail.lettertext"));
			graphics.drawWordWrap(this.font, Component.literal(this.text.getValue()), this.leftPos + 20, this.topPos + 34, 119, ColourProperties.INSTANCE.get("gui.mail.lettertext"));
		} else {
			clearTradeInfoWidgets();
            this.address.render(graphics, mouseX, mouseY, partialTicks);    //TODO correct?
			if (this.menu.getCarrier().equals(PostalCarriers.TRADER.value())) {
				drawTradePreview(graphics, 18, 32);
			} else {
                this.text.render(graphics, mouseX, mouseY, partialTicks);
			}
		}
	}

	private void drawTradePreview(GuiGraphics graphics, int x, int y) {

		Component infoString = null;
		if (this.menu.getTradeInfo() == null) {
			infoString = Component.translatable("for.gui.mail.no.trader");
		} else if (this.menu.getTradeInfo().tradegood().isEmpty()) {
			infoString = Component.translatable("for.gui.mail.nothing.to.trade");
		} else if (!this.menu.getTradeInfo().state().isOk()) {
			infoString = this.menu.getTradeInfo().state().getDescription();
		}

		if (infoString != null) {
			graphics.drawWordWrap(this.font, infoString, this.leftPos + x, this.topPos + y, 119, ColourProperties.INSTANCE.get("gui.mail.lettertext"));
			return;
		}

		graphics.drawString(this.font, Component.translatable("for.gui.mail.pleasesend"), this.leftPos + x, this.topPos + y, ColourProperties.INSTANCE.get("gui.mail.lettertext"));

		addTradeInfoWidget(new ItemStackWidget(this.widgetManager, x, y + 10, this.menu.getTradeInfo().tradegood()));

		graphics.drawString(this.font, Component.translatable("for.gui.mail.foreveryattached"), this.leftPos + x, this.topPos + y + 28, ColourProperties.INSTANCE.get("gui.mail.lettertext"));

		for (int i = 0; i < this.menu.getTradeInfo().required().size(); i++) {
			addTradeInfoWidget(new ItemStackWidget(this.widgetManager, x + i * 18, y + 38, this.menu.getTradeInfo().required().get(i)));
		}
	}

	private void addTradeInfoWidget(Widget widget) {
        this.tradeInfoWidgets.add(widget);
        this.widgetManager.add(widget);
	}

	private void clearTradeInfoWidgets() {
		for (Widget widget : this.tradeInfoWidgets) {
            this.widgetManager.remove(widget);
		}
        this.tradeInfoWidgets.clear();
	}

	@Override
	public void onClose() {
		String recipientName = this.address.getValue();
		IPostalCarrier carrier = this.menu.getCarrier();
		setRecipient(recipientName, carrier);
		setText();
		super.onClose();
	}

	private void setFromSessionVars() {
		if (SessionVars.getStringVar("mail.letter.recipient") == null) {
			return;
		}

		String recipient = SessionVars.getStringVar("mail.letter.recipient");
		String typeName = SessionVars.getStringVar("mail.letter.carrier");
		ResourceLocation carrierId = ResourceLocation.tryParse(typeName);
		IPostalCarrier carrier = ForestryRegistries.POSTAL_CARRIER.get(carrierId);

		if (StringUtils.isNotBlank(recipient) && carrier != null) {
            this.menu.setCarrier(carrier);
            this.address.setValue(recipient);
		}

		SessionVars.clearStringVar("mail.letter.recipient");
		SessionVars.clearStringVar("mail.letter.carrier");
	}

	private void setRecipient(String recipientName, IPostalCarrier carrier) {
		if (this.isProcessedLetter || StringUtils.isBlank(recipientName)) {
			return;
		}
		if (recipientName.equals(this.lastSyncedRecipient) && carrier == this.lastSyncedCarrier) {
			return;
		}

        this.lastSyncedRecipient = recipientName;
        this.lastSyncedCarrier = carrier;
		PacketLetterInfoRequest packet = new PacketLetterInfoRequest(recipientName, carrier);
		NetworkUtil.sendToServer(packet);
	}

	@OnlyIn(Dist.CLIENT)
	private void setText() {
		setText(this.text.getValue());
	}

	@OnlyIn(Dist.CLIENT)
	private void setText(String text) {
		if (this.isProcessedLetter || this.lastSyncedText.equals(text)) {
			return;
		}

        this.lastSyncedText = text;
        this.menu.setText(text);
	}

	@Override
	protected void addLedgers() {
		addErrorLedger(this.itemInventory);
		addHintLedger("letter");
	}
}

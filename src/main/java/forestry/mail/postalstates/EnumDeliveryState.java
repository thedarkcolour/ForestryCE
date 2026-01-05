package forestry.mail.postalstates;

import forestry.api.mail.IPostalState;
import net.minecraft.network.chat.Component;

public enum EnumDeliveryState implements IPostalState {
	OK("for.chat.mail.ok"),
	NO_MAILBOX("for.chat.mail.no.mailbox"),
	NOT_MAILABLE("for.chat.mail.not.mailable"),
	ALREADY_MAILED("for.chat.mail.already.mailed"),
	NOT_POSTPAID("for.chat.mail.not.postpaid"),
	MAILBOX_FULL("for.chat.mail.mailbox.full");

	private final String unlocalizedDescription;

	EnumDeliveryState(String unlocalizedDescription) {
		this.unlocalizedDescription = unlocalizedDescription;
	}

	@Override
	public boolean isOk() {
		return this == OK;
	}

	@Override
	public Component getDescription() {
		return Component.translatable(this.unlocalizedDescription);
	}
}

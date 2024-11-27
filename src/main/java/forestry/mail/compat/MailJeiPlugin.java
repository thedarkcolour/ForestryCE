package forestry.mail.compat;

import net.minecraft.resources.ResourceLocation;

import forestry.api.modules.ForestryModuleIds;
import forestry.core.utils.JeiUtil;
import forestry.mail.blocks.BlockTypeMail;
import forestry.mail.features.MailBlocks;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeRegistration;

@JeiPlugin
public class MailJeiPlugin implements IModPlugin {
	@Override
	public ResourceLocation getPluginUid() {
		return ForestryModuleIds.MAIL;
	}

	@Override
	public void registerRecipes(IRecipeRegistration registration) {
		JeiUtil.addDescription(registration, MailBlocks.BASE.get(BlockTypeMail.MAILBOX).block(), MailBlocks.BASE.get(BlockTypeMail.STAMP_COLLETOR).block(), MailBlocks.BASE.get(BlockTypeMail.TRADE_STATION).block());
	}
}

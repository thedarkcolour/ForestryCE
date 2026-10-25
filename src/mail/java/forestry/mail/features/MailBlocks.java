package forestry.mail.features;

import forestry.api.modules.ForestryModuleIds;
import forestry.core.platform.item.ForestryBlockItem;
import forestry.mail.blocks.MailBlock;
import forestry.mail.blocks.MailBlockType;
import forestry.core.platform.registration.FeatureBlockGroup;
import forestry.core.platform.registration.FeatureProvider;
import forestry.core.platform.registration.IFeatureRegistry;
import forestry.core.platform.registration.ModFeatureRegistry;

import java.util.List;

@FeatureProvider
public class MailBlocks {
	private static final IFeatureRegistry REGISTRY = ModFeatureRegistry.get(ForestryModuleIds.MAIL);

	public static final FeatureBlockGroup<MailBlock, MailBlockType> BASE = REGISTRY.blockGroup(MailBlock::new, List.of(MailBlockType.values())).item(ForestryBlockItem::new).create();
}

package forestry.modules.features;

import net.minecraft.world.inventory.AbstractContainerMenu;

import forestry.api.core.IMenuTypeProvider;

public interface IMenuTypeFeature<C extends AbstractContainerMenu> extends IMenuTypeProvider<C>, IModFeature {
}

package forestry.core.items;

import java.util.function.Consumer;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.level.block.Block;

import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import forestry.core.client.CoreClientHandler;

public class ItemBlockTesr<B extends Block> extends ItemBlockForestry<B> {
	public ItemBlockTesr(B block, Properties builder) {
		super(block, builder);
	}

	public ItemBlockTesr(B block) {
		super(block, new Properties());
	}

	@Override
	public void initializeClient(Consumer<IClientItemExtensions> consumer) {
		consumer.accept(new IClientItemExtensions() {
			@Override
			public BlockEntityWithoutLevelRenderer getCustomRenderer() {
				return CoreClientHandler.bewlr;
			}
		});
	}
}

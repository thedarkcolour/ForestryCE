package forestry.core.platform.item;

import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SpecialRendererBlockItem<B extends Block> extends ForestryBlockItem<B> {
	// Tracks every instance so that CoreClientHandler can register a single
	// IClientItemExtensions (returning the BEWLR) for them at RegisterClientExtensionsEvent
	// time. Replaces the per-instance Item#initializeClient override removed in 1.21.
	private static final List<SpecialRendererBlockItem<?>> INSTANCES = new ArrayList<>();

	public SpecialRendererBlockItem(B block, Properties builder) {
		super(block, builder);
		INSTANCES.add(this);
	}

	public SpecialRendererBlockItem(B block) {
		this(block, new Properties());
	}

	public static List<SpecialRendererBlockItem<?>> getInstances() {
		return Collections.unmodifiableList(INSTANCES);
	}
}

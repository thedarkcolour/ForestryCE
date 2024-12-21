package forestry.api.client.plugin;

import java.awt.Color;

import net.minecraft.resources.ResourceLocation;

import forestry.api.client.arboriculture.ILeafSprite;
import forestry.api.client.arboriculture.ILeafTint;

import it.unimi.dsi.fastutil.ints.Int2IntFunction;

/**
 * Contains methods for creating instances of client-only objects using Forestry's built-in implementations.
 */
public interface IClientHelper {
	/**
	 * @return A leaf tint that does not tint the texture. Used for vanilla Cherry Blossom trees in 1.20.
	 */
	ILeafTint createNoneTint();

	/**
	 * @return A leaf tint that always uses the same color.
	 */
	ILeafTint createFixedTint(Color color);

	/**
	 * @return A tint that changes based on the biome. Used for vanilla Oak trees.
	 */
	ILeafTint createBiomeTint();

	/**
	 * Creates a tint that changes based on the biome.
	 *
	 * @param mapper A transformation function that returns a new tint given the default biome tint.
	 * @return A biome-dependent tint.
	 */
	ILeafTint createBiomeTint(Int2IntFunction mapper);

	/**
	 * Creates a leaf sprite given a namespace and name. The sprites used are located at:
	 * <ul>
	 *     <li>Normal: &lt;NAMESPACE&gt;/textures/blocks/leaves/&lt;PATH&gt;.png</li>
	 *     <li>Fast graphics: &lt;NAMESPACE&gt;/textures/blocks/leaves/&lt;PATH&gt;_fast.png</li>
	 *     <li>Pollinated: &lt;NAMESPACE&gt;/textures/blocks/leaves/&lt;PATH&gt;_pollinated.png</li>
	 *     <li>Pollinated with fast graphics: &lt;NAMESPACE&gt;/textures/blocks/leaves/&lt;PATH&gt;_pollinated_fast.png</li>
	 * </ul>
	 *
	 * @param id The name and namespace used to generate texture paths for normal, pollinated, fast normal, and fast
	 *           pollinated textures.
	 * @return A new leaf sprite.
	 */
	ILeafSprite createLeafSprite(ResourceLocation id);
}

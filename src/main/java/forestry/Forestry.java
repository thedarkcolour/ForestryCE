package forestry;

import forestry.api.ForestryConstants;
import forestry.api.IForestryApi;
import forestry.apiimpl.plugin.PluginManager;
import forestry.core.EventHandlerCore;
import forestry.core.config.ForestryConfig;
import forestry.modules.ForestryModuleManager;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.NeoForgeMod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Forestry Minecraft Mod
 *
 * @author SirSengir
 */
@Mod(ForestryConstants.MOD_ID)
public class Forestry {
	public static final boolean DEBUG = ModList.get().isLoaded("modkit");
	public static final Logger LOGGER = LogManager.getLogger(ForestryConstants.MOD_ID);

	public Forestry(ModContainer container) {
		// IForestryModule - registration of game objects
		ForestryModuleManager moduleManager = (ForestryModuleManager) IForestryApi.INSTANCE.getModuleManager();
		moduleManager.init();
		NeoForge.EVENT_BUS.register(EventHandlerCore.class);

		// IForestryPlugin - registration of Forestry data
		PluginManager.loadPlugins();
		PluginManager.registerErrors();

		ForestryConfig.register(container);

		NeoForgeMod.enableMilkFluid();
	}
}

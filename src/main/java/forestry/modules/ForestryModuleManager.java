package forestry.modules;

import forestry.Forestry;
import forestry.api.modules.ForestryModule;
import forestry.api.modules.IForestryModule;
import forestry.api.modules.IModuleManager;
import forestry.core.utils.ModUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.objectweb.asm.Type;

import java.util.*;

public class ForestryModuleManager implements IModuleManager {
	private final LinkedHashMap<ResourceLocation, IForestryModule> loadedModules = new LinkedHashMap<>();
	private final LinkedHashMap<String, List<IForestryModule>> loadedModulesByMod = new LinkedHashMap<>();

	@Override
	public Collection<IForestryModule> getLoadedModules() {
		return Collections.unmodifiableCollection(this.loadedModules.values());
	}

	@Override
	public boolean isModuleLoaded(ResourceLocation id) {
		return this.loadedModules.containsKey(id);
	}

	@Override
	public List<IForestryModule> getModulesForMod(String modId) {
		return Collections.unmodifiableList(this.loadedModulesByMod.get(modId));
	}

	private void loadModules() {
		LinkedHashMap<String, List<IForestryModule>> discoveredModules = discoverModules();
		HashSet<ResourceLocation> discoveredIds = new HashSet<>();
		LinkedList<IForestryModule> modulesToLoad = new LinkedList<>();

		for (List<IForestryModule> modModules : discoveredModules.values()) {
			for (IForestryModule module : modModules) {
				discoveredIds.add(module.getId());
				modulesToLoad.add(module);
			}
		}

		// check dependencies and skip loading of modules whose dependencies are missing
		Iterator<IForestryModule> iterator;
		boolean changed;
		do {
			changed = false;
			iterator = modulesToLoad.iterator();

			while (iterator.hasNext()) {
				IForestryModule module = iterator.next();
				List<ResourceLocation> dependencies = module.getModuleDependencies();
				List<String> modDependencies = module.getModDependencies();

				if (discoveredIds.containsAll(dependencies)) {
					for (String modId : modDependencies) {
						if (!ModList.get().isLoaded(modId)) {
							Forestry.LOGGER.warn("Module {} is missing mod dependencies: {}", module.getId(), modDependencies);
						}
					}
					// if all dependency mods are loaded, skip removal code
					continue;
				} else {
					Forestry.LOGGER.warn("Module {} is missing dependencies: {}", module.getId(), dependencies);
				}

				// remove from loaded modules
				iterator.remove();
				changed = true;
				discoveredIds.remove(module.getId());
			}
		} while (changed);

		// sort modules in LOAD order
		do {
			changed = false;
			iterator = modulesToLoad.iterator();
			while (iterator.hasNext()) {
				IForestryModule module = iterator.next();

				if (this.loadedModules.keySet().containsAll(module.getModuleDependencies())) {
					iterator.remove();
					this.loadedModules.put(module.getId(), module);
					this.loadedModulesByMod.computeIfAbsent(module.getId().getNamespace(), modId -> new ArrayList<>()).add(module);
					changed = true;
					break;
				}
			}
		} while (changed);
	}

	// Called during mod construction by Forestry
	public void init() {
		loadModules();

		for (Map.Entry<ResourceLocation, IForestryModule> entry : this.loadedModules.entrySet()) {
			IEventBus modBus = ModuleUtil.getModBus(entry.getKey().getNamespace());
			IForestryModule module = entry.getValue();

			module.registerEvents(modBus);

			if (FMLEnvironment.dist == Dist.CLIENT) {
				module.registerClientHandler(handler -> handler.registerEvents(modBus));
			}
		}

		this.loadedModulesByMod.forEach((modid, modules) -> {
			Forestry.LOGGER.debug("Handling Forestry module loading for mod '{}' with {} modules: {}", modid, modules.size(), Arrays.toString(modules.toArray()));
		});
	}

	// Returns a map of mod IDs to modules, with core modules first in each mod list
	private static LinkedHashMap<String, List<IForestryModule>> discoverModules() {
		LinkedHashMap<String, List<IForestryModule>> modules = new LinkedHashMap<>();

		ModuleUtil.forEachAnnotated(Type.getType(ForestryModule.class), klass -> {
			IForestryModule module;
			try {
				module = klass.asSubclass(IForestryModule.class).getConstructor().newInstance();
			} catch (ReflectiveOperationException e) {
				throw new RuntimeException("Failed to instantiate module class " + klass.getName(), e);
			} catch (ClassCastException e) {
				throw new RuntimeException("Cannot load class" + klass.getName() + " as a @ForestryModule, it does not implement IForestryModule", e);
			}
			String modId = module.getId().getNamespace();
			// Namespace of the id must be a modid
			if (!ModUtil.isModLoaded(modId)) {
				throw new RuntimeException("Module " + module.getClass() + " returned '" + module.getId() + "' for its ID namespace, but no mod with ID '" + modId + "' is loaded");
			}
			List<IForestryModule> modModules = modules.computeIfAbsent(modId, k -> new ArrayList<>());
			// Core modules load first
			if (module.isCore()) {
				modModules.add(0, module);
			} else {
				modModules.add(module);
			}
		});

		return modules;
	}

	public void setupApi() {
		for (IForestryModule module : getLoadedModules()) {
			try {
				module.setupApi();
			} catch (Throwable t) {
				// this exception normally gets swallowed, so log it and rethrow
				Forestry.LOGGER.fatal("Module {} threw an error in its IForestryModule.setupApi method", module.getId(), t);
				throw new RuntimeException(t);
			}
		}
	}
}

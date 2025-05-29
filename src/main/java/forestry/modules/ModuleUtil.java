package forestry.modules;

import forestry.Forestry;
import forestry.modules.features.FeatureProvider;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.neoforgespi.language.ModFileScanData;
import org.objectweb.asm.Type;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Set;
import java.util.function.Consumer;

public class ModuleUtil {
	private static final HashMap<Class<?>, Field> MOD_BUS_FIELDS = new HashMap<>();
	private static final HashMap<String, IEventBus> MOD_BUSES = new HashMap<>();

	public static void loadFeatureProviders() {
		forEachAnnotated(Type.getType(FeatureProvider.class), klass -> Forestry.LOGGER.debug("Loaded feature provider {}", klass));
	}

	static void forEachAnnotated(Type annotationType, Consumer<Class<?>> action) {
		for (ModFileScanData scanData : ModList.get().getAllScanData()) {
			Set<ModFileScanData.AnnotationData> annotationData = scanData.getAnnotations();

			for (ModFileScanData.AnnotationData data : annotationData) {
				if (!data.annotationType().equals(annotationType)) {
					continue;
				}

				String className = data.memberName();
				try {
					Class<?> klass = Class.forName(className);
					action.accept(klass);
				} catch (ReflectiveOperationException e) {
					throw new RuntimeException("Failed to load annotated member " + className, e);
				}
			}
		}
	}

	// Since Forge/Neo doesn't offer a unified way to get the mod bus, we have to do this ugly hack.
	// Kotlin for Forge, JavaFML, and ScalableCatsForce all name their fields eventBus.
	// GroovyModLoader calls its field modBus.
	public static IEventBus getModBus(String modid) {
		return MOD_BUSES.computeIfAbsent(modid, key -> {
			ModContainer modContainer = ModList.get().getModContainerById(modid).orElseThrow();

			// look up which field in the mod container class contains the mod bus
			Field modBusField = MOD_BUS_FIELDS.computeIfAbsent(modContainer.getClass(), type -> {
				// iterate class hierarchy in case of anonymous or subclass weirdness of JVM languages
				for (Class<?> klass = type; klass != ModContainer.class; klass = klass.getSuperclass()) {
					// getDeclaredFields is required to read private members
					for (Field field : klass.getDeclaredFields()) {
						String name = field.getName();

						if (name.equals("eventBus") || name.equals("modBus")) {
							if (IEventBus.class.isAssignableFrom(field.getType())) {
								field.setAccessible(true);
								return field;
							}
						}
					}
				}
				throw new RuntimeException("Failed to find eventBus or modBus field in mod container: " + type);
			});

			try {
				return (IEventBus) modBusField.get(modContainer);
			} catch (IllegalAccessException | ClassCastException e) {
				throw new RuntimeException("Failed to obtain mod-specific event bus for '" + modid + "'", e);
			}
		});
	}
}

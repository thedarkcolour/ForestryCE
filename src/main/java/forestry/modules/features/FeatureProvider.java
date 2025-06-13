package forestry.modules.features;

import forestry.modules.ModuleUtil;
import net.neoforged.bus.api.IEventBus;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotated classes are loaded during mod construction after by {@link ModuleUtil#loadFeatureProviders}.
 * At this point, {@link forestry.api.modules.IForestryModule#registerEvents(IEventBus)} has already been called.
 * Note that annotated classes are NOT INSTANTIATED, only classloaded.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface FeatureProvider {
}

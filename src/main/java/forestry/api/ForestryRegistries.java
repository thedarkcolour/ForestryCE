package forestry.api;

import forestry.api.circuits.ICircuit;
import forestry.api.mail.IPostalCarrier;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.RegistryBuilder;

public class ForestryRegistries {
	public static final Registry<ICircuit> CIRCUIT = new RegistryBuilder<>(Keys.CIRCUIT_TYPE)
		.sync(true)
		.create();

	public static final Registry<IPostalCarrier> POSTAL_CARRIER = new RegistryBuilder<>(Keys.POSTAL_CARRIER)
		.sync(true)
		.create();

	public static class Keys {
		public static final ResourceKey<Registry<ICircuit>> CIRCUIT_TYPE = ResourceKey.createRegistryKey(ForestryConstants.forestry("circuit"));
		public static final ResourceKey<Registry<IPostalCarrier>> POSTAL_CARRIER = ResourceKey.createRegistryKey(ForestryConstants.forestry("postal_carrier"));
	}
}

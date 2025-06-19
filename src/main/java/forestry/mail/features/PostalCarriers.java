package forestry.mail.features;

import forestry.api.ForestryRegistries;
import forestry.api.mail.IPostalCarrier;
import forestry.api.modules.ForestryModuleIds;
import forestry.mail.carriers.players.CarrierPlayer;
import forestry.mail.carriers.trading.CarrierTrader;
import forestry.modules.features.FeatureProvider;
import forestry.modules.features.ModFeatureRegistry;
import net.minecraft.core.Holder;
import net.neoforged.neoforge.registries.DeferredRegister;

@FeatureProvider
public class PostalCarriers {
	private static final DeferredRegister<IPostalCarrier> POSTAL_CARRIERS = ModFeatureRegistry.get(ForestryModuleIds.MAIL).getRegistry(ForestryRegistries.POSTAL_CARRIER.key());

	public static final Holder<IPostalCarrier> PLAYER = POSTAL_CARRIERS.register("player", CarrierPlayer::new);
	public static final Holder<IPostalCarrier> TRADER = POSTAL_CARRIERS.register("trader", CarrierTrader::new);
}

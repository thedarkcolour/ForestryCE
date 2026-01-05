package forestry.lepidopterology;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import forestry.api.client.IClientModuleHandler;
import forestry.api.modules.ForestryModule;
import forestry.api.modules.ForestryModuleIds;
import forestry.lepidopterology.commands.CommandButterfly;
import forestry.lepidopterology.entities.EntityButterfly;
import forestry.lepidopterology.features.LepidopterologyEntities;
import forestry.lepidopterology.proxy.LepidopterologyClientHandler;
import forestry.modules.BlankForestryModule;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.eventbus.api.IEventBus;

import java.util.List;
import java.util.function.Consumer;

@ForestryModule
public class ModuleLepidopterology extends BlankForestryModule {
	public static int maxDistance = 64;
	private static final boolean allowPollination = true;
	public static final Object2FloatOpenHashMap<String> spawnRarities = new Object2FloatOpenHashMap<>();
	public static boolean spawnButterflysFromLeaves = true;
	private static final boolean generateCocoons = false;
	private static final float generateCocoonsAmount = 1.0f;
	private static final float serumChance = 0.55f;
	private static final float secondSerumChance = 0;

	@Override
	public void registerEvents(IEventBus modBus) {
		MinecraftForge.EVENT_BUS.addListener(ModuleLepidopterology::onEntityTravelToDimension);
		modBus.addListener(ModuleLepidopterology::onAttributeCreate);
	}

	public static void onEntityTravelToDimension(EntityTravelToDimensionEvent event) {
		if (event.getEntity() instanceof EntityButterfly) {
			event.setCanceled(true);
		}
	}

	public static void onAttributeCreate(EntityAttributeCreationEvent event) {
		event.put(LepidopterologyEntities.BUTTERFLY.entityType(), LepidopterologyEntities.BUTTERFLY.createAttributes().build());
	}

	@Override
	public ResourceLocation getId() {
		return ForestryModuleIds.LEPIDOPTEROLOGY;
	}

	@Override
	public List<ResourceLocation> getModuleDependencies() {
		return List.of(ForestryModuleIds.CORE, ForestryModuleIds.ARBORICULTURE);
	}

	@Override
	public void addToRootCommand(LiteralArgumentBuilder<CommandSourceStack> command) {
		command.then(CommandButterfly.register());
	}

	public static boolean isPollinationAllowed() {
		return allowPollination;
	}

	public static boolean isSpawnButterflysFromLeaves() {
		return spawnButterflysFromLeaves;
	}

	public static boolean isGenerateCocoons() {
		return generateCocoons;
	}

	public static float getGenerateCocoonsAmount() {
		return generateCocoonsAmount;
	}

	public static float getSerumChance() {
		return serumChance;
	}

	public static float getSecondSerumChance() {
		return secondSerumChance;
	}

	@Override
	public void registerClientHandler(Consumer<IClientModuleHandler> registrar) {
		registrar.accept(new LepidopterologyClientHandler());
	}
}

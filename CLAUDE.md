# Forestry: Community Edition - AI Assistant Guide

This document provides context for AI assistants (like Claude Code) working on the Forestry mod codebase.

## Project Overview

Forestry: Community Edition is a Minecraft mod for Minecraft 1.20.1 (Forge), known for its bees, trees, and automatic farms. It's a community fork that continues development after the original maintainers went inactive.

**Key Technologies:**
- Minecraft 1.20.1
- Forge (NeoForged Gradle toolchain)
- Java 17
- Gradle build system

**Main Features:**
- Apiculture (Beekeeping with complex genetics)
- Arboriculture (Tree breeding and cultivation)
- Lepidopterology (Butterfly breeding)
- Automated farming (Multiblock farms)
- Processing machines (Centrifuge, Squeezer, Carpenter, etc.)
- Genetics system (Mendelian inheritance for organisms)

## Architecture Overview

### Module System

Forestry uses a sophisticated **module-based architecture** where functionality is split into independent modules with dependency management.

**Key Components:**
- `IForestryModule` - Interface all modules implement
- `BlankForestryModule` - Base class (automatically depends on CORE)
- `ForestryModuleManager` - Discovers, loads, and initializes modules
- `@ForestryModule` - Annotation for automatic discovery

**Module Lifecycle:**
1. Discovery via ASM scanning for `@ForestryModule` classes
2. Dependency resolution (checks module + mod dependencies)
3. Initialization via `registerEvents()` on mod event bus
4. API setup during common setup

**Core Modules** (in `src/main/java/forestry/`):

| Module | Location | Purpose |
|--------|----------|---------|
| Core | `core/ModuleCore.java` | Foundation module, always loads first |
| Apiculture | `apiculture/ModuleApiculture.java` | Bees, hives, beekeeping |
| Arboriculture | `arboriculture/ModuleArboriculture.java` | Trees, saplings, wood |
| Lepidopterology | `lepidopterology/ModuleLepidopterology.java` | Butterflies |
| Farming | `farming/ModuleFarming.java` | Multiblock farms |
| Factory | `factory/ModuleFactory.java` | Processing machines |
| Energy | `energy/ModuleEnergy.java` | Engines and power generation |
| Cultivation | `cultivation/ModuleCultivation.java` | Single-block planters |
| Storage | `storage/ModuleStorage.java` | Backpacks and crates |
| Mail | `mail/ModuleMail.java` | Stamp collection system |
| Charcoal | `arboriculture/ModuleCharcoal.java` | Charcoal pile multiblock |
| Sorting | `sorting/ModuleSorting.java` | Item sorting |
| Worktable | `worktable/ModuleWorktable.java` | Project table (crafting variant) |

### Feature Registration System

**Pattern:** Classes annotated with `@FeatureProvider` auto-register game content using static fields.

**Example** (`core/features/CoreItems.java`):
```java
@FeatureProvider
public class CoreItems {
    private static final IFeatureRegistry REGISTRY = ModFeatureRegistry.get(ForestryModuleIds.CORE);

    public static final FeatureItem<ItemWrench> WRENCH = REGISTRY.item(ItemWrench::new, "wrench");
    public static final FeatureItemGroup<ItemElectronTube, EnumElectronTube> ELECTRON_TUBES =
        REGISTRY.itemGroup(ItemElectronTube::new, "electron_tube", EnumElectronTube.values());
}
```

**Feature Types:**
- `FeatureBlock<B, I>` - Blocks with optional BlockItems
- `FeatureItem<I>` - Items
- `FeatureBlockGroup<B, S>` / `FeatureItemGroup<I, S>` - Variants
- `FeatureItemTable<I, R, C>` - 2D tables of items
- `FeatureTileType<T>` - BlockEntityTypes
- `FeatureMenuType<C>` - Container menus
- `FeatureEntityType<E>` - Entity types
- `FeatureFluid` - Fluids

### Genetics System

The genetics system enables breeding of bees, trees, and butterflies with Mendelian inheritance.

**Core Abstractions:**

```
ISpeciesType (BEE, TREE, BUTTERFLY)
    └── ISpecies (individual species like "Forest Bee")
        └── IIndividual (organism instance)
            └── IGenome (contains chromosomes)
                └── IChromosome (single trait with active/inactive alleles)
```

**Key Interfaces:**
- `ISpeciesType` - Organism type (bees/trees/butterflies)
- `ISpecies` - Species definition with unique traits
- `IIndividual` - Living organism with genetics
- `IGenome` - Contains all chromosomes (active/inactive alleles)
- `IKaryotype` - Defines chromosome structure for a species type
- `IChromosome` - Single genetic trait (species, lifespan, fertility, etc.)
- `IMutation` - Cross-breeding rules between species

**Implementation** (`core/genetics/`):
- Species registry managed by `IGeneticManager`
- All species registered via plugin system
- Alleles stored in `IAlleleManager`
- Mutations managed by `MutationManager` per species type

**Taxonomy:**
- Full Linnaean classification support via `ITaxon`
- Genus, family, order, etc. defined in plugins

### Plugin System

External mods (and Forestry's own modules) register content via the plugin API.

**Interface:** `IForestryPlugin` (`api/plugin/IForestryPlugin.java`)

**Registration Methods:**
```java
void registerGenetics(IGeneticRegistration) - Species types, karyotypes
void registerApiculture(IApicultureRegistration) - Bee species, hives
void registerArboriculture(IArboricultureRegistration) - Tree species
void registerLepidopterology(ILepidopterologyRegistration) - Butterflies
void registerCircuits(ICircuitRegistration) - Circuit boards
void registerFarming(IFarmingRegistration) - Farm types
void registerErrors(IErrorRegistration) - Error states
void registerClient(Consumer<IClientRegistration>) - Client rendering
```

**Discovery:** Java ServiceLoader pattern
- Plugins must provide `META-INF/services/forestry.api.plugin.IForestryPlugin`
- Default plugin: `plugin/DefaultForestryPlugin.java` (registers all vanilla species)

**Plugin Manager:** `apiimpl/plugin/PluginManager.java`
- Loads plugins in order
- Builds immutable registries after registration

### Central API

**Access Point:** `IForestryApi` (singleton via ServiceLoader)

**Managers:**
- `IModuleManager` - Module system
- `IGeneticManager` - Species registry
- `IAlleleManager` - Allele registry
- `IHiveManager` - Bee hive mechanics
- `ITreeManager` - Tree mechanics
- `IFarmingManager` - Farm types
- `ICircuitManager` - Circuit boards
- `IErrorManager` - Error states
- `IClimateManager` - Climate system
- `IFilterManager` - Genetic filters

**Implementation:** `apiimpl/ForestryApiImpl.java`

## Directory Structure

```
src/main/java/forestry/
├── api/                        # Public API (for other mods)
│   ├── genetics/              # Genetics API
│   ├── apiculture/            # Bee API
│   ├── arboriculture/         # Tree API
│   ├── modules/               # Module system API
│   └── plugin/                # Plugin API
├── apiimpl/                   # API implementations (internal)
│   └── plugin/                # Plugin manager
├── core/                      # Core module
│   ├── features/              # Core items/blocks registration
│   ├── genetics/              # Genetics implementation
│   ├── data/                  # Data generation providers
│   ├── tiles/                 # Core tile entities
│   ├── multiblock/            # Multiblock framework
│   ├── network/               # Networking packets
│   └── gui/                   # GUI framework
├── modules/                   # Module framework
│   └── features/              # Feature registration system
├── apiculture/               # Bee module
│   ├── genetics/             # Bee genetics
│   ├── multiblock/           # Alveary multiblock
│   └── tiles/                # Bee tile entities
├── arboriculture/            # Tree module
│   ├── genetics/             # Tree genetics
│   ├── worldgen/             # Tree generation
│   └── charcoal/             # Charcoal pile
├── lepidopterology/          # Butterfly module
├── farming/                  # Farm module (multiblock farms)
├── factory/                  # Machine module
├── energy/                   # Engine module
├── cultivation/              # Planter blocks
├── storage/                  # Backpacks and crates
├── mail/                     # Stamp system
├── sorting/                  # Item sorting
├── worktable/                # Project table
├── plugin/                   # Default plugin (vanilla content)
└── compat/                   # Mod compatibility (JEI, KubeJS)
```

## Data Generation

**Entry Point:** `core/data/Data.java` - Listens for `GatherDataEvent`

**Providers:**
- `ForestryRecipeProvider` - All recipes (crafting + custom machines)
- `ForestryBlockTagsProvider` - Block tags
- `ForestryItemTagsProvider` - Item tags
- `ForestryBackpackTagProvider` - Backpack contents
- `ForestryBlockStateProvider` - Block states and models
- `ForestryItemModelProvider` - Item models
- `ForestryBlockLootTables` - Loot tables
- `ForestryAdvancementProvider` - Advancements
- `ForestryLootModifierProvider` - Global loot modifiers

**Recipe Builders** (`core/data/builder/`):
Custom builders for Forestry machines:
- `CarpenterRecipeBuilder` - Carpenter (crafting with liquids)
- `CentrifugeRecipeBuilder` - Centrifuge (multiple outputs + chances)
- `FabricatorRecipeBuilder` - Thermionic fabricator
- `FermenterRecipeBuilder` - Fermenter
- `SqueezerRecipeBuilder` - Squeezer
- `MoistenerRecipeBuilder` - Moistener
- `StillRecipeBuilder` - Still

**Run Data Generation:**
```bash
gradlew runData
```
Output: `src/generated/resources/`

## Common Patterns

### 1. Adding a New Item

**Location:** Module's `features/*Features.java` class

**Steps:**
1. Add static field in `@FeatureProvider` class
2. Use registry method: `REGISTRY.item(Constructor, "id")`
3. Run data generation for models/recipes

**Example:**
```java
@FeatureProvider
public class ApicultureItems {
    private static final IFeatureRegistry REGISTRY = ModFeatureRegistry.get(ForestryModuleIds.APICULTURE);

    public static final FeatureItem<ItemHoneyComb> HONEYCOMB =
        REGISTRY.item(ItemHoneyComb::new, "honeycomb");
}
```

### 2. Adding a New Species

**Location:** Plugin class (usually `plugin/DefaultForestryPlugin.java`)

**Steps:**
1. Register in appropriate plugin method (`registerApiculture`, etc.)
2. Use species builder
3. Define chromosomes/alleles
4. Add client rendering in `registerClient()`

**Example:**
```java
@Override
public void registerApiculture(IApicultureRegistration registration) {
    registration.registerSpecies(
        builder -> builder
            .id("forestry:forest")
            .temperature(EnumTemperature.NORMAL)
            .humidity(EnumHumidity.NORMAL)
            .classification(BeeClassification.MUNDANE)
            .primaryColor(0x19d0ec)
            .secondaryColor(0xffdc16)
    );
}
```

### 3. Adding a New Module

**Steps:**
1. Create `Module*.java` extending `BlankForestryModule`
2. Annotate with `@ForestryModule`
3. Implement `getId()` returning unique `ResourceLocation`
4. Override `registerEvents()` for Forge event registration
5. Create feature provider classes for content

**Example:**
```java
@ForestryModule
public class ModuleNewFeature extends BlankForestryModule {
    @Override
    public ResourceLocation getId() {
        return new ResourceLocation(Constants.MOD_ID, "new_feature");
    }

    @Override
    public void registerEvents(IEventBus modBus) {
        // Register to Forge event bus
    }
}
```

### 4. Adding Recipes

**Location:** `core/data/recipe/ForestryRecipeProvider.java`

**For Custom Machines:**
```java
CarpenterRecipeBuilder.carpenter(Items.DIAMOND)
    .pattern("###", "# #", "###")
    .key('#', Items.COAL)
    .fluidInput(new FluidStack(Fluids.WATER, 1000))
    .save(consumer, "diamond_from_coal");
```

**For Vanilla Crafting:**
```java
ShapedRecipeBuilder.shaped(RecipeCategory.MISC, result)
    .pattern("###")
    .define('#', ingredient)
    .unlockedBy("has_ingredient", has(ingredient))
    .save(consumer);
```

### 5. Tile Entity Pattern

**Base Classes:**
- `TileBase` - Minimal base (extends BlockEntity)
- `TileForestry` - Standard features (inventory, errors, owner, streaming)
- `TilePowered` - Adds energy handling (extends TileForestry)

**Example:**
```java
public class TileMyMachine extends TilePowered implements IStreamable {
    public TileMyMachine(BlockPos pos, BlockState state) {
        super(FeatureType.MY_MACHINE.tileType(), pos, state, 1000); // 1000 RF capacity
    }

    @Override
    public void writeData(StreamBuffer buffer) {
        // Sync data to client
    }

    @Override
    public void readData(StreamBuffer buffer) {
        // Read data from server
    }
}
```

### 6. Multiblock Pattern

**Base Classes:**
- `MultiblockControllerBase` - Controller logic
- `MultiblockTileEntityForestry` - Component tiles
- `RectangularMultiblockControllerBase` - For rectangular structures

**Example Usage:**
- Alveary (beekeeping multiblock)
- Farms (automated farming)
- Charcoal pile

## Build System

**Key Gradle Tasks:**
- `gradlew build` - Build JAR (output: `build/libs/`)
- `gradlew runClient` - Launch client
- `gradlew runServer` - Launch server
- `gradlew runData` - Generate data (recipes, models, etc.)

**Dependencies:**
- JEI (optional) - Recipe viewing
- Patchouli (required) - In-game documentation
- KubeJS (optional) - Script integration
- Curios (optional) - Accessories
- Architectury (optional) - Cross-loader API

**Version Properties:** `gradle.properties`
```properties
minecraftVersion=1.20.1
forgeVersion=47.3.0
forestryVersion=2.7.0
```

## Integration Points

### JEI Integration (`core/recipes/jei/`)
- Custom recipe categories for Forestry machines
- `ForestryJeiPlugin` - Main JEI plugin
- Category classes for each machine type

### KubeJS Integration (`compat/kubejs/`)
- `ForestryKubeJSPlugin` - Main plugin
- Event bindings for genetics registration
- Recipe type bindings

### Patchouli (Required)
- In-game documentation book
- Located in `src/main/resources/data/forestry/patchouli_books/`

## Important Utilities

**Location:** `core/utils/`

- `SpeciesUtil` - Species lookups and helpers
- `ModUtil` - Module and mod detection
- `NBTUtilForestry` - NBT serialization
- `NetworkUtil` - Network packet helpers
- `InventoryUtil` - Inventory manipulation
- `ItemStackUtil` - ItemStack utilities

## Contribution Workflow

**Branch:** Currently on `1.20.1`, main branch is `1.20.1`

**Before Committing:**
1. Run `gradlew runData` to regenerate data files (it will freeze indefinitely after reaching the log message `[minecraft/HashCache]: Caching: total files: ...`, so you'll have to terminate after that message appears)
2. Ensure no compilation errors
3. Test changes in-game

**Git Config:**
```bash
git config --local blame.ignoreRevsFile .git-blame-ignore-revs
```

## Key Concepts for AI Assistants

### When Reading Code

1. **Module Boundaries:** Each module is relatively independent. Changes in one module rarely affect others (except Core).

2. **Feature References:** Items/blocks are accessed via static fields in `*Features.java` classes, not registry lookups.

3. **Genetics Flow:** Species → Individual → Genome → Chromosomes → Alleles

4. **Data Generation:** Most JSON files in `src/generated/resources/` are generated, not hand-written.

### When Making Changes

1. **Prefer Editing:** Always prefer editing existing feature provider classes over creating new ones.

2. **Data Gen:** After adding items/blocks, run `gradlew runData` to generate JSON files.

3. **Module Dependencies:** Check module dependencies before adding cross-module references.

4. **API Stability:** The `api/` package is public. Changes here affect other mods.

5. **Immutability:** Most registries are immutable after registration phase. No runtime registration.

### Common Tasks

**Find where an item is registered:**
- Search for the item's registry name in `*Features.java` files

**Find where a species is registered:**
- Check `plugin/DefaultForestryPlugin.java` or external plugin classes

**Add a new recipe:**
- Edit `core/data/recipe/ForestryRecipeProvider.java`, then run `gradlew runData`

**Add a new machine:**
1. Create tile entity extending `TilePowered`
2. Create block class
3. Register in module's features class
4. Add recipes in data provider
5. Add JEI integration if needed

**Debug genetics:**
- Check `IGeneticManager` for species registry
- Use `IIndividual.analyze()` to get genetic info
- Mutations managed by `MutationManager`

## File Locations Reference

| Task | Location |
|------|----------|
| Add item/block | `<module>/features/*Features.java` |
| Add recipe | `core/data/recipe/ForestryRecipeProvider.java` |
| Add species | `plugin/DefaultForestryPlugin.java` |
| Add machine logic | `<module>/tiles/Tile*.java` |
| Add GUI | `<module>/gui/` or `<module>/screen/` |
| Add JEI category | `core/recipes/jei/` |
| Add localization | `src/main/resources/assets/forestry/lang/en_us.json` |
| Add textures | `src/main/resources/assets/forestry/textures/` |
| Add data | Run `gradlew runData` to generate |

## Additional Resources

- **Wiki:** http://forestry.sengir.net/
- **Discord:** https://discord.gg/49XNRJk
- **Issues:** https://github.com/ForestryMC/ForestryMC/issues
- **CurseForge:** https://www.curseforge.com/minecraft/mc-mods/forestry-community-edition

## Notes

- This is a fork maintained after original developers went inactive
- Focus on bug fixes and maintaining compatibility
- Gameplay changes should be discussed on Discord first
- The codebase is large (hundreds of hours of work) - take time to understand patterns before making changes

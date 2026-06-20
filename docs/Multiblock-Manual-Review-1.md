my findings:
- PROBLEM: when right clicking an Alveary that doesn't have all 27 of its Alveary blocks, i get the misleading message "A multiblock part is incompatible with this type (%s)." my guess is that this is referring to AIR being in the structure check, but i believe there used to be a more accurate error message along the lines of "the Alveary must be 3x3x3".
- GOOD: right clicking an Alveary with all 27 of its Alveary blocks, but missing slabs, correctly displays "The Alveary must have wooden slabs on top."
- GOOD: adding all blocks, then the slabs correctly changes the appearance of the Alveary blocks to resemble its completed state.
- GOOD: Alveary GUI opens and shows the correct climate information and IBeekeepingLogic error states (missing a Queen, since it's a fresh Alveary)
- GOOD: adding a Queen with correct climate conditions starts ticking the bee and displaying particles.
- GOOD: removing a block (Alveary or slab) immediately disassembles the structure
- GOOD: removing a non-anchor (lowest XYZ corner) block and adding it back persists the multiblock's inventory
- PROBLEM: inventory IS LOST if you destroy the block at the anchor position (it doesn't re anchor)
- GOOD: the Spectacles reveal the anchor block with the NBT data (proven by /data get block), and none of the other blocks have the data.
- GOOD: multifarm forms properly and plants saplings + builds farmland when provided power, fertilizer, dirt, and saplings
- GOOD: extending a 3x4x3 multifarm to a 4x4x4 multifarm reassembles the multiblock and persists the inventory
- PROBLEM: exiting world, then rejoining no longer highlights the anchor block when wearing spectacles, despite its NBT data being intact and properly referenced in the anchorPos of other blocks
- PROBLEM: exiting world, then rejoining, then trying to click to open the GUI causes a crash: 
```
Caused by: java.lang.IllegalStateException
	at forestry.farming.multiblock.FakeFarmController.getFarmLogic(FakeFarmController.java:106) ~[main/:?] {re:classloading}
	at forestry.farming.gui.FarmLogicSlot.getLogic(FarmLogicSlot.java:29) ~[main/:?] {re:classloading}
	at forestry.farming.gui.FarmLogicSlot.getProperties(FarmLogicSlot.java:33) ~[main/:?] {re:classloading}
	at forestry.farming.gui.FarmLogicSlot.getStackIndex(FarmLogicSlot.java:37) ~[main/:?] {re:classloading}
	at forestry.farming.gui.FarmLogicSlot.draw(FarmLogicSlot.java:42) ~[main/:?] {re:classloading}
	at forestry.core.gui.widgets.WidgetManager.drawWidgets(WidgetManager.java:50) ~[main/:?] {re:classloading}
	at forestry.core.gui.GuiForestry.drawWidgets(GuiForestry.java:217) ~[main/:?] {re:classloading}
	at forestry.core.gui.GuiForestry.renderBg(GuiForestry.java:205) ~[main/:?] {re:classloading}
	at forestry.core.gui.GuiForestryTitled.renderBg(GuiForestryTitled.java:22) ~[main/:?] {re:classloading}
	at forestry.farming.gui.GuiFarm.renderBg(GuiFarm.java:48) ~[main/:?] {re:classloading}
	at net.minecraft.client.gui.screens.inventory.AbstractContainerScreen.render(AbstractContainerScreen.java:125) ~[forge-1.20.1-47.4.0.jar:?] {re:mixin,pl:accesstransformer:B,pl:runtimedistcleaner:A,re:classloading,pl:accesstransformer:B,pl:runtimedistcleaner:A}
	at forestry.core.gui.GuiForestry.render(GuiForestry.java:87) ~[main/:?] {re:classloading}
	at net.minecraft.client.gui.screens.Screen.renderWithTooltip(Screen.java:109) ~[forge-1.20.1-47.4.0.jar:?] {re:mixin,pl:accesstransformer:B,pl:runtimedistcleaner:A,re:computing_frames,pl:accesstransformer:B,pl:runtimedistcleaner:A,re:classloading,pl:accesstransformer:B,pl:mixin:APP:patchouli_xplat.mixins.json:client.AccessorScreen,pl:mixin:APP:kubejs-common.mixins.json:ScreenMixin,pl:mixin:A,pl:runtimedistcleaner:A}
	at net.minecraftforge.client.ForgeHooksClient.drawScreenInternal(ForgeHooksClient.java:428) ~[forge-1.20.1-47.4.0.jar:?] {re:classloading}
	at net.minecraftforge.client.ForgeHooksClient.drawScreen(ForgeHooksClient.java:421) ~[forge-1.20.1-47.4.0.jar:?] {re:classloading}
	at net.minecraft.client.renderer.GameRenderer.render(GameRenderer.java:971) ~[forge-1.20.1-47.4.0.jar:?] {re:mixin,pl:accesstransformer:B,pl:runtimedistcleaner:A,re:classloading,pl:accesstransformer:B,pl:runtimedistcleaner:A}
	... 24 more
Disconnected from the target VM, address: 'localhost:46339', transport: 'socket'
```
- GOOD: despite the GUI crashing after world reload, it is still possible to insert dirt into the Farm with a Hatch block, and it correctly appears in the anchor block's NBT after being inserted. this makes me think that while the server has a working multiblock, the client does not.

i have some questions about 2.5 (forceload actually affects a 3x3 area - the center is chunkloaded in one mode, and the surrounding 8 chunks are loaded in another mode according to the minecraft.wiki ) before i proceed with it. also, could you potentially create a command that would automate this loading/unloading debugging given the chunk position of the anchor position of a multiblock, assuming it has other parts in other chunks? testing with this command has always been finnicky and unreliable, but if i can run something like /forestry multiblock debug X Y Z that would make testing a million times easier

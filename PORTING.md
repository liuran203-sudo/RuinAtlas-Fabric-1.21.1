# 墟舆录 Ruin Atlas · Fabric 移植说明

NeoForge 版（`D:\Workspaces\RuinAtlas-NeoForge-1.21.1`）→ Fabric 版（本工程）。
要求：同比例移植，类名 / 包结构 / 字段顺序 / 注释与 NeoForge 版保持一致，便于两库同步维护。

## 依赖口径

只用 **fabric-api**（Fabric 官方配套库），不引入任何第三方库或额外 API。
`build.gradle` / `gradle.properties` 保持模板原样未改动（`splitEnvironmentSourceSets()`、`configureDataGeneration`、`modImplementation fabric-api`）。

## 目录布局

| 位置 | 内容 |
| --- | --- |
| `src/main/java/com/liuran/ruinatlas` | 主类、Config、block / item / technique / init / network / event / worldgen / datagen |
| `src/main/java/com/liuran/ruinatlas/platform` | 与 NeoForge 同名的注册桥接层（见下） |
| `src/client/java/com/liuran/ruinatlas` | 客户端入口 `RuinAtlasClient` |
| `src/client/java/com/liuran/ruinatlas/client` | RaClientSetup / RaClientEvents / RaSpiritHud / RaKeyMappings / RaTechniqueScreen / SpiritAuraClient / 两个粒子 / 粒子注册 |
| `src/main/resources` | 手绘模型、贴图、粒子定义、dimension / worldgen 数据、`fabric.mod.json`、`ruinatlas.mixins.json` |
| `src/main/generated` | 数据生成产物（lang / blockstates / models / loot_table / recipe / tags），Loom 默认并入资源 |

## 注册层桥接（platform 包）

Fabric 没有 mod 事件总线的延迟注册，所以用同名 API 顶上，调用点写法不变：

- `platform/DeferredRegister`：`createBlocks` / `createItems` / `create(ResourceKey, String)`；
  字段初始化时即直接 `Registry.register`，`register()` 只作为与 NeoForge 一致的装配入口（空实现，靠调用触发类装载）。
  嵌套 `Blocks` / `Items` 提供 `registerBlock` / `registerItem` / `registerSimpleItem`。
- `platform/DeferredHolder` / `DeferredBlock` / `DeferredItem`：`getId()` / `get()` / `isBound()`。
- `platform/RaSimpleParticleType`：原版 `SimpleParticleType` 构造器是 protected（NeoForge 会放开可见性，Fabric 不会），
  用同包子类开放，序列化与网络行为与原版一致。

## 各层对应关系

| NeoForge | Fabric |
| --- | --- |
| `@Mod` 类构造器 | `RuinAtlas implements ModInitializer#onInitialize` |
| `@Mod(dist=CLIENT)` + `@EventBusSubscriber` | `RuinAtlasClient implements ClientModInitializer`（客户端入口） |
| `ModConfigSpec` | `Config`（`ruinatlas-common.properties`，键名/默认值/校验一致） |
| `AttachmentType`（NeoForge 注册表） | Fabric data attachment（`RaAttachments`，公开 API 一致，含持久化与同步） |
| `PayloadRegistrar` / `PacketDistributor` | `PayloadTypeRegistry` + `ServerPlayNetworking` / `ClientPlayNetworking`（`RaNetwork`、两个 payload 的 `handle(...)`） |
| `PlayerTickEvent.Post` | `ServerTickEvents.END_SERVER_TICK` 遍历在线玩家 |
| `LivingIncomingDamageEvent#setCanceled` | `ServerLivingEntityEvents.ALLOW_DAMAGE` 返回 false |
| `RegisterKeyMappingsEvent` | `KeyBindingHelper.registerKeyBinding` |
| `RegisterGuiLayersEvent` | `HudRenderCallback.EVENT` |
| `RegisterParticleProvidersEvent` | `ParticleFactoryRegistry.getInstance().register` |
| `GatherDataEvent` + `DataGenerator#addProvider` | `DataGeneratorEntrypoint` + `Pack#addProvider`（入口 `datagen/RaDatagen`） |
| `BlockTagsProvider` / `ItemTagsProvider` | `FabricTagProvider.BlockTagProvider` / `.ItemTagProvider` |
| `LanguageProvider` | `FabricLanguageProvider` + 本工程 `RaLanguageProvider` 桥接三个 `add` 重载（lang 正文逐字未改） |
| `BlockStateProvider` / `ItemModelProvider` | 自写 `RaBlockModel` / `RaItemModel`（`DataProvider` 直接输出与 NeoForge 同形的 JSON） |

## 与 NeoForge 版仅有差异的地方（均为平台能力差异，已在源码注释中标明）

1. **创造栏排序**：Fabric 的创造栏 API 没有 `withTabsBefore`，`RUIN_ATLAS_TAB` 排在原版各项之后。
2. **方块战利品表的校验范围**：原版 `BlockLootSubProvider#generate(BiConsumer)` 会遍历整个方块注册表要求每块都有表
   （NeoForge 用 `getKnownBlocks()` 收窄）。Fabric 侧在 `RaBlockLootTable` 里重写该方法，只输出本模组的表。
3. **配方 JSON 的 `result.count`**：原版 `ItemStack` 编解码在数量为 1 时省略 `count`，NeoForge 版会写出 `"count": 1`。
   语义等价（默认即 1），仅文本表现形式不同。

## 构建与验证

```powershell
# 需要 Java 25+ 才能加载 Loom 1.19 快照插件（编译目标仍是 Java 21）
$env:JAVA_HOME='D:\Program Files\Java\jdk-26'
$env:GRADLE_USER_HOME='D:\wushan\.gradle'
.\gradlew.bat build          # 产物：build/libs/ruinatlas-1.0.0.jar
.\gradlew.bat runDatagen     # 输出到 src/main/generated
```

已完成验证：

- `compileJava` / `compileClientJava` 通过；
- `runDatagen` 全 Provider 跑通，产物与 NeoForge `src/generated/resources` 逐文件比对：
  385 个文件同名同数，**除 `.cache` 内部哈希与上述 `result.count` 一处外内容完全一致**；
- `runServer` 启动到 `Done (13.981s)`，模组初始化、数据包加载（1319 配方 / 1428 进度）、维度准备均无报错；
- `build` 产出 jar（450 KB，含 75 个 class、80 个 blockstate、116 个物品模型、3 个 lang、84 张战利品表、30 个配方、维度与世界生成数据）。

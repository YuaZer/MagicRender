import * as THREE from "/vendor/three.module.js";
import { OrbitControls } from "/vendor/OrbitControls.js";

const translations = {
  en: {
    appSubtitle: "Effect Editor",
    runtimeTitle: "Runtime",
    reloadConfig: "Reload Config",
    validate: "Validate",
    preview: "Preview",
    stop: "Stop",
    export: "Export",
    overwrite: "Overwrite",
    previewPanel: "Preview",
    resetCamera: "Reset camera",
    expandPreview: "Expand preview",
    collapsePreview: "Collapse preview",
    statusPanel: "Status",
    jsonPanel: "Config JSON",
    ready: "Ready.",
    runtimeUrl: "URL",
    runtimeEffects: "Effects",
    runtimePort: "Port",
    source: "Source",
    target: "Target",
    basic: "Basic",
    trail: "Trail",
    motion: "Motion",
    circle: "Magic Circle",
    beam: "Beam",
    advanced: "Advanced",
    previewPage: "Preview",
    basicHint: "Edit effect metadata and shared visibility.",
    trailHint: "Ribbon trail shape, color and sampling.",
    motionHint: "Orbit, helix and custom formula motion layer.",
    circleHint: "Arcane circle attached to the preview source.",
    beamHint: "Beam from source to target point or entity.",
    advancedHint: "Layered glow, point clouds, flowing ribbon bundles, circle layers and radial burst rays.",
    previewHint: "Editor-only preview target and distance settings.",
    effectId: "Effect Id",
    enabled: "Enabled",
    group: "Group",
    durationTicks: "Duration Ticks",
    importance: "Importance",
    drawDistance: "Draw Distance",
    hideWhenShadersConflict: "Hide When Shaders Conflict",
    trailEnabled: "Trail Enabled",
    style: "Style",
    widthStart: "Width Start",
    widthEnd: "Width End",
    colorStart: "Color Start",
    colorEnd: "Color End",
    lifetimeTicks: "Lifetime Ticks",
    maxPoints: "Max Points",
    minSampleDistance: "Min Sample Distance",
    maxSegmentLength: "Max Segment Length",
    texture: "Texture",
    blendMode: "Blend Mode",
    mode: "Mode",
    radius: "Radius",
    angularSpeed: "Angular Speed",
    verticalAmplitude: "Vertical Amplitude",
    verticalSpeed: "Vertical Speed",
    phase: "Phase",
    formulaX: "Formula X",
    formulaY: "Formula Y",
    formulaZ: "Formula Z",
    maxInsertedPoints: "Max Inserted Points Per Tick",
    sampleEveryTick: "Sample Every Tick",
    renderMode: "Render Mode",
    circleEnabled: "Circle Enabled",
    thickness: "Thickness",
    segments: "Segments",
    facing: "Facing",
    rotationSpeed: "Rotation Speed",
    innerRadiusScale: "Inner Radius Scale",
    glyphs: "Glyphs",
    beamEnabled: "Beam Enabled",
    width: "Width",
    noise: "Noise",
    targetMode: "Target Mode",
    fixedDistance: "Fixed Distance",
    sourceHeight: "Source Height Offset",
    targetHeight: "Target Height Offset",
    fallbackDistance: "Fallback To Fixed Distance"
    ,advancedEnabled: "Advanced Enabled",
    bloomLayers: "Bloom Layers",
    bloomScaleStep: "Bloom Scale Step",
    bloomAlphaFalloff: "Bloom Alpha Falloff",
    glowEnabled: "True Glow Enabled",
    glowIntensity: "Glow Intensity",
    glowRadius: "Glow Radius",
    glowIterations: "Glow Iterations",
    glowDownsample: "Glow Downsample",
    glowThreshold: "Glow Threshold",
    coreEnabled: "Core Glow Enabled",
    coreRadius: "Core Radius",
    corePulseAmplitude: "Core Pulse",
    emitterCount: "Emitter Count",
    emitterShape: "Emitter Shape",
    emitterSizeStart: "Particle Size Start",
    emitterSizeEnd: "Particle Size End",
    emitterHeight: "Emitter Height",
    emitterSwirl: "Emitter Swirl",
    bundleCount: "Ribbon Count",
    bundleLength: "Ribbon Length",
    bundleAmplitude: "Ribbon Amplitude",
    bundleFrequency: "Ribbon Frequency",
    bundleTwist: "Ribbon Twist",
    bundleFlow: "Ribbon Flow",
    circleLayerRadius: "Layer Radius",
    circleLayerGlyphs: "Layer Glyphs",
    burstRays: "Burst Rays",
    burstLength: "Burst Length",
    effectGroupTitle: "Effect Group",
    effectTypeTitle: "Effect Type",
    addChildEffect: "Add child effect",
    groupKey: "Group Key",
    groupDescription: "Description",
    exportChildrenWithGroup: "Export child effects with group",
    exportGroup: "Export Group",
    overwriteGroup: "Overwrite Group",
    importChild: "Import Child",
    groupNodePrefix: "Group",
    exportChild: "Export",
    deleteChild: "Delete",
    groupNodeHint: "This node is the exported effect group. Select a child effect below to edit its config.",
    noImportEffectSelected: "No runtime effect config selected for import."
  },
  zh: {
    appSubtitle: "特效编辑器",
    runtimeTitle: "运行状态",
    reloadConfig: "重载配置",
    validate: "校验",
    preview: "预览",
    stop: "停止",
    export: "导出",
    overwrite: "覆盖",
    previewPanel: "预览",
    resetCamera: "重置视角",
    expandPreview: "放大预览",
    collapsePreview: "退出放大",
    statusPanel: "状态",
    jsonPanel: "配置 JSON",
    ready: "就绪。",
    runtimeUrl: "地址",
    runtimeEffects: "特效数",
    runtimePort: "端口",
    source: "源点",
    target: "目标",
    basic: "基础",
    trail: "轨迹",
    motion: "运动",
    circle: "法阵",
    beam: "光束",
    advanced: "高级",
    previewPage: "预览",
    basicHint: "编辑特效元数据和通用可见性。",
    trailHint: "设置拖尾轨迹的形状、颜色和采样。",
    motionHint: "设置环绕、螺旋和自定义公式运动。",
    circleHint: "附着在预览源点上的法阵。",
    beamHint: "从源点指向目标点或实体的光束。",
    advancedHint: "分层泛光、点云粒子、渐变流光束、多层法阵和放射光刺。",
    previewHint: "编辑器专用的预览目标和距离设置。",
    effectId: "特效 ID",
    enabled: "启用",
    group: "分组",
    durationTicks: "持续 Tick",
    importance: "重要性",
    drawDistance: "可见距离",
    hideWhenShadersConflict: "光影冲突时隐藏",
    trailEnabled: "启用轨迹",
    style: "样式",
    widthStart: "起始宽度",
    widthEnd: "结束宽度",
    colorStart: "起始颜色",
    colorEnd: "结束颜色",
    lifetimeTicks: "寿命 Tick",
    maxPoints: "最大点数",
    minSampleDistance: "最小采样距离",
    maxSegmentLength: "最大分段长度",
    texture: "贴图",
    blendMode: "混合模式",
    mode: "模式",
    radius: "半径",
    angularSpeed: "角速度",
    verticalAmplitude: "垂直振幅",
    verticalSpeed: "垂直速度",
    phase: "相位",
    formulaX: "公式 X",
    formulaY: "公式 Y",
    formulaZ: "公式 Z",
    maxInsertedPoints: "每 Tick 最大插点",
    sampleEveryTick: "每 Tick 采样",
    renderMode: "渲染模式",
    circleEnabled: "启用法阵",
    thickness: "厚度",
    segments: "分段",
    facing: "朝向",
    rotationSpeed: "旋转速度",
    innerRadiusScale: "内环比例",
    glyphs: "符文数量",
    beamEnabled: "启用光束",
    width: "宽度",
    noise: "扰动",
    targetMode: "目标模式",
    fixedDistance: "固定距离",
    sourceHeight: "源点高度偏移",
    targetHeight: "目标高度偏移",
    fallbackDistance: "无目标时回退固定距离",
    advancedEnabled: "启用高级效果",
    bloomLayers: "泛光层数",
    bloomScaleStep: "泛光放大倍率",
    bloomAlphaFalloff: "泛光透明衰减",
    glowEnabled: "启用真实辉光",
    glowIntensity: "辉光强度",
    glowRadius: "辉光半径",
    glowIterations: "辉光迭代",
    glowDownsample: "辉光降采样",
    glowThreshold: "辉光阈值",
    coreEnabled: "启用核心光团",
    coreRadius: "核心半径",
    corePulseAmplitude: "核心脉冲",
    emitterCount: "粒子数量",
    emitterShape: "发射器形状",
    emitterSizeStart: "粒子起始大小",
    emitterSizeEnd: "粒子结束大小",
    emitterHeight: "发射高度",
    emitterSwirl: "旋转速度",
    bundleCount: "流光数量",
    bundleLength: "流光长度",
    bundleAmplitude: "流光振幅",
    bundleFrequency: "流光频率",
    bundleTwist: "流光扭转",
    bundleFlow: "流动速度",
    circleLayerRadius: "法阵层半径",
    circleLayerGlyphs: "法阵层符文数",
    burstRays: "光刺数量",
    burstLength: "光刺长度",
    effectGroupTitle: "\u7279\u6548\u7ec4",
    effectTypeTitle: "\u7279\u6548\u7c7b\u578b",
    addChildEffect: "\u6dfb\u52a0\u5b50\u7279\u6548",
    groupKey: "\u7ec4 Key",
    groupDescription: "\u63cf\u8ff0",
    exportChildrenWithGroup: "\u5bfc\u51fa\u7ec4\u65f6\u540c\u65f6\u5bfc\u51fa\u5b50\u7279\u6548",
    exportGroup: "\u5bfc\u51fa\u7279\u6548\u7ec4",
    overwriteGroup: "\u8986\u76d6\u7279\u6548\u7ec4",
    importChild: "\u5bfc\u5165\u5b50\u7279\u6548",
    groupNodePrefix: "\u7279\u6548\u7ec4",
    exportChild: "\u5bfc\u51fa",
    deleteChild: "\u5220\u9664",
    groupNodeHint: "\u8fd9\u662f\u5bfc\u51fa\u7528\u7684\u7279\u6548\u7ec4\u8282\u70b9\u3002\u9009\u62e9\u4e0b\u65b9\u5b50\u7279\u6548\u540e\u7f16\u8f91\u5176\u914d\u7f6e\u3002",
    noImportEffectSelected: "\u6ca1\u6709\u9009\u62e9\u53ef\u5bfc\u5165\u7684\u8fd0\u884c\u65f6\u7279\u6548\u914d\u7f6e\u3002"
  }
};

let language = localStorage.getItem("magicrender.editor.language") || "en";
const t = (key) => translations[language]?.[key] ?? translations.en[key] ?? key;

const formulaHelp = {
  en: "Formula mode uses JavaScript-style math expressions, not arbitrary scripts. Available variables: tick, time, radius, angularSpeed, verticalAmplitude, verticalSpeed, phase, angle, angleDegrees, verticalAngle. Available functions/constants: Math.sin, Math.cos, Math.tan, Math.sqrt, Math.abs, Math.min, Math.max, Math.pow, Math.PI, Math.E. Examples: X = Math.cos(angle) * radius; Y = Math.sin(verticalAngle) * verticalAmplitude; Z = Math.sin(angle) * radius.",
  zh: "公式模式使用 JavaScript 风格的数学表达式，不执行任意脚本。可用变量：tick、time、radius、angularSpeed、verticalAmplitude、verticalSpeed、phase、angle、angleDegrees、verticalAngle。可用函数/常量：Math.sin、Math.cos、Math.tan、Math.sqrt、Math.abs、Math.min、Math.max、Math.pow、Math.PI、Math.E。示例：X = Math.cos(angle) * radius；Y = Math.sin(verticalAngle) * verticalAmplitude；Z = Math.sin(angle) * radius。"
};

const helpText = {
  en: {
    "id": "Unique effect id. Use namespace:path, lowercase only. Example: magicrender:fire_ring. This becomes the exported file name and command id.",
    "enabled": "Turns the whole effect on or off. Disabled effects stay in JSON but will not spawn.",
    "group": "Effect group used by limits and organization. Blank exports as default. Use the same group for effects that should share limits.",
    "durationTicks": "Effect lifetime in game ticks. 20 ticks = 1 second. Larger values keep preview/effects alive longer; smaller values end sooner.",
    "importance": "Scheduling priority. low is cheaper/background, normal is default, high is for important effects that should survive stricter limits.",
    "visibility.drawDistance": "Maximum visible distance in blocks. Larger values can be seen farther away but cost more rendering work.",
    "visibility.hideWhenShadersConflict": "If true, hides this effect when shader compatibility says it may render incorrectly.",
    "components.trail.enabled": "Enables the ribbon trail component. The trail samples motion over time and draws a strip through the sampled points.",
    "components.trail.style": "Style label for the trail. Currently used as config metadata; keep descriptive names such as ribbon, flame, water.",
    "components.trail.width.start": "Width at newly spawned trail points. Larger values make the head thicker; small values make a thin line. Example: 0.2 to 0.6.",
    "components.trail.width.end": "Width at the oldest trail points. 0 fades to a sharp tail; values near start keep a constant-width ribbon.",
    "components.trail.color.start": "Color at new trail points. Format #RRGGBB or #RRGGBBAA. Example: #FFFF6600 for orange.",
    "components.trail.color.end": "Color at old trail points. Use a different color for gradients, or alpha 00 to fade out.",
    "components.trail.lifetimeTicks": "How long each trail point remains. Larger values create longer trails and more work; smaller values create short streaks.",
    "components.trail.maxPoints": "Maximum stored trail points. Higher values make smoother/longer trails but cost more CPU/GPU. 32-96 is usually reasonable.",
    "components.trail.minSampleDistance": "Minimum movement before adding points when Sample Every Tick is off. Larger values reduce point count but make corners rougher.",
    "components.trail.maxSegmentLength": "Maximum distance between generated trail points. Smaller values fill gaps better; larger values are cheaper but can look segmented.",
    "components.trail.texture": "Texture resource id used by the trail. Must point to an existing texture. Examples: minecraft:textures/particle/flame.png, minecraft:textures/particle/drip_fall.png, minecraft:textures/particle/splash_0.png.",
    "components.trail.blendMode": "additive makes bright glowing trails; alpha keeps normal transparency and is better for smoke/water-like effects.",
    "components.trail.motion.mode": "follow attaches directly to the source; orbit circles around it; helix adds vertical sine motion; formula uses custom X/Y/Z expressions.",
    "components.trail.motion.radius": "Orbit/formula radius in blocks. 0 disables orbit offset. Larger values widen circles or formulas using radius.",
    "components.trail.motion.angularSpeed": "Degrees per tick around the source. Positive and negative values rotate opposite directions. 20 means one full circle in 18 ticks.",
    "components.trail.motion.verticalAmplitude": "Maximum vertical offset for helix/formula. Larger values make taller waves; 0 keeps it flat.",
    "components.trail.motion.verticalSpeed": "Degrees per tick for vertical wave motion. Larger absolute values make the up/down wave faster.",
    "components.trail.motion.phase": "Initial angle offset in degrees. Use it to rotate the starting position without changing speed.",
    "components.trail.motion.formula.x": `${formulaHelp.en}\nThis field controls X offset from the source. Example: Math.cos(angle) * radius`,
    "components.trail.motion.formula.y": `${formulaHelp.en}\nThis field controls vertical Y offset. Example: Math.sin(verticalAngle) * verticalAmplitude`,
    "components.trail.motion.formula.z": `${formulaHelp.en}\nThis field controls Z offset from the source. Example: Math.sin(angle) * radius`,
    "components.trail.maxInsertedPointsPerTick": "Maximum extra points inserted when motion jumps too far in one tick. Higher values smooth fast movement; lower values are cheaper.",
    "components.trail.sampleEveryTick": "If true, samples every tick for smooth trails. If false, minSampleDistance controls when points are added.",
    "components.trail.renderMode": "face_camera makes the ribbon face the camera; world_up keeps it upright in world space.",
    "components.magicCircle.enabled": "Enables the magic circle component at the source position.",
    "components.magicCircle.style": "Style label for the circle. Use names like arcane, rune, seal for organization.",
    "components.magicCircle.radius": "Circle radius in blocks. Larger values cover more area; very large circles cost more to render.",
    "components.magicCircle.color": "Circle color. Format #RRGGBB or #RRGGBBAA.",
    "components.magicCircle.thickness": "Line thickness of the circle. Larger values look bolder; small values look sharper.",
    "components.magicCircle.segments": "Number of circle segments. Higher values are smoother but cost more. 64-128 is typical.",
    "components.magicCircle.facing": "face_camera turns toward the camera; horizontal lies flat in the world.",
    "components.magicCircle.rotationSpeed": "Rotation speed in degrees per tick. Positive and negative values rotate opposite directions.",
    "components.magicCircle.innerRadiusScale": "Inner circle radius as a fraction of outer radius. Smaller values make a wider ring gap; larger values bring rings together.",
    "components.magicCircle.glyphs": "Number of decorative glyph slots. 0 disables glyph placement; higher values make denser symbols.",
    "components.magicCircle.blendMode": "additive gives glow; alpha gives normal transparency.",
    "components.beam.enabled": "Enables the beam component from source to target.",
    "components.beam.style": "Style label for the beam. Use names like mana, lightning, laser.",
    "components.beam.width": "Beam width in blocks. Larger values make a thick beam; smaller values make a thin ray.",
    "components.beam.color.start": "Beam color at the source. Format #RRGGBB or #RRGGBBAA.",
    "components.beam.color.end": "Beam color at the target. Use different start/end colors for gradients.",
    "components.beam.segments": "Beam subdivision count. More segments allow smoother noise curves; 1 is a straight beam.",
    "components.beam.noise": "Random-looking wave amount. 0 is straight; larger values make lightning-like bends.",
    "components.beam.texture": "Texture resource id for the beam. Example: minecraft:textures/misc/white.png or a mod texture.",
    "components.beam.blendMode": "additive makes bright energy beams; alpha is softer and less glowing.",
    "preview.targetMode": "fixed_distance uses a point in front of you; crosshair_entity targets the entity under the crosshair; look_point targets the looked-at point.",
    "preview.fixedDistance": "Distance in blocks for fixed preview target. Larger values preview longer beams/trails.",
    "preview.sourceHeightOffset": "Vertical offset from source entity feet. 0.55 is around body height; larger values move source upward.",
    "preview.targetHeightOffset": "Vertical offset from target entity feet. Use it to aim at body/head height.",
    "preview.fallbackToFixedDistance": "If true, preview falls back to fixed distance when no target entity/point is found.",
    "components.advanced.enabled": "Master switch for all advanced layers. Turn this off to keep the advanced settings in JSON while hiding bloom, core glow, emitters, ribbons, circle layers and bursts.",
    "components.advanced.bloom.layers": "Legacy glow approximation layer count. This duplicates translucent quads around sprites; it is not real post-process bloom. Keep it low when True Glow is enabled.",
    "components.advanced.bloom.scaleStep": "Legacy approximation scale between duplicated glow shells. True Glow uses the Glow Radius field instead.",
    "components.advanced.bloom.alphaFalloff": "Legacy approximation opacity falloff. True Glow uses post-process blur and intensity instead.",
    "components.advanced.glow.enabled": "Enables true screen-space glow for advanced ribbons, circles, particles and core sprites. This renders bright geometry into an offscreen buffer, blurs it, then adds it back to the scene.",
    "components.advanced.glow.intensity": "Final additive strength of the blurred glow. Higher values create stronger light spill; too high can wash out colors.",
    "components.advanced.glow.radius": "Blur radius multiplier. Larger values spread light farther from ribbons and glyphs; smaller values keep a tight neon edge.",
    "components.advanced.glow.iterations": "Number of blur passes. More iterations make smoother, wider glow but cost more GPU time. 3-6 is a practical range.",
    "components.advanced.glow.downsample": "Resolution divisor for the glow buffer. 2 is sharp, 4 is cheaper and softer, 1 is highest quality but most expensive.",
    "components.advanced.glow.threshold": "Brightness cutoff before blur in the game renderer. 0 glows all advanced additive geometry; higher values keep only the brightest parts. The web preview approximates this setting.",
    "components.advanced.core.enabled": "Enables the central glow sprite at the source. Use it when the effect needs a visible energy core instead of only trails or particles.",
    "components.advanced.core.color": "Color of the central glow. Use alpha in #RRGGBBAA to control how strongly the core covers nearby elements.",
    "components.advanced.core.radius": "Base radius of the central glow in blocks. Larger values create a bigger orb; smaller values make a compact highlight.",
    "components.advanced.core.pulseAmplitude": "How much the core radius pulses over time. 0 keeps a steady size; higher values make stronger breathing or heartbeat motion.",
    "components.advanced.particleEmitters.0.shape": "Emitter volume shape. sphere spreads particles in all directions, column stacks them vertically, ring keeps them around a circle, box fills a rectangular volume.",
    "components.advanced.particleEmitters.0.count": "Particle count for the first emitter. Higher values make denser point clouds but cost more to render.",
    "components.advanced.particleEmitters.0.color.start": "Particle color at the start of its life. Use this with end color to create color shifts over time.",
    "components.advanced.particleEmitters.0.color.end": "Particle color near the end of its life. Lower alpha makes particles fade out instead of disappearing sharply.",
    "components.advanced.particleEmitters.0.size.start": "Particle size when spawned. Larger start size makes the cloud feel heavier and brighter.",
    "components.advanced.particleEmitters.0.size.end": "Particle size near the end of life. Smaller end size creates shrinking sparks; similar values keep stable dots.",
    "components.advanced.particleEmitters.0.radius": "Horizontal spread radius for sphere, ring and column emitters. Larger values widen the particle field.",
    "components.advanced.particleEmitters.0.height": "Vertical spread height for column and box-like emitters. Larger values create taller pillars or volumes.",
    "components.advanced.particleEmitters.0.swirlSpeed": "Angular swirl speed for particles. Positive and negative values rotate in opposite directions; 0 disables swirl motion.",
    "components.advanced.ribbonBundles.0.count": "Number of flowing ribbons in the first bundle. More ribbons create a fuller energy weave but increase geometry cost.",
    "components.advanced.ribbonBundles.0.length": "Length of each ribbon in samples/space. Larger values leave longer flowing strands; smaller values make compact arcs.",
    "components.advanced.ribbonBundles.0.amplitude": "Wave height of the ribbon bundle. Higher values make wider sine-wave motion; 0 keeps ribbons close to their path.",
    "components.advanced.ribbonBundles.0.frequency": "Wave frequency along each ribbon. Higher values create more ripples; lower values create broad smooth curves.",
    "components.advanced.ribbonBundles.0.twist": "Amount of twist around the bundle path. Higher values make the ribbons braid around each other more strongly.",
    "components.advanced.ribbonBundles.0.flowSpeed": "Animation speed of the ribbon texture/phase. Higher values make the bundle stream faster; 0 freezes the flow.",
    "components.advanced.circleLayers.0.radius": "Radius of the first advanced circle layer. Larger values place the layer farther from the source and make the seal cover more area.",
    "components.advanced.circleLayers.0.glyphs": "Number of glyph marks on the advanced circle layer. 0 removes marks; higher values make the ring denser and more ornate.",
    "components.advanced.radialBursts.0.rays": "Number of radial burst rays. Higher values create a fuller starburst; lower values make distinct spikes.",
    "components.advanced.radialBursts.0.length": "Length of each burst ray in blocks. Larger values make sharper, farther-reaching spikes; smaller values keep the burst close to the core."
  },
  zh: {
    "id": "特效唯一 ID。格式必须是 namespace:path，并使用小写。示例：magicrender:fire_ring。它会成为导出文件名和命令使用的 ID。",
    "enabled": "启用或禁用整个特效。禁用后配置仍会保留，但不会生成特效。",
    "group": "特效分组，用于限制和组织。留空会按 default 导出。同类特效建议放到同一分组。",
    "durationTicks": "特效持续时间，单位是游戏 Tick。20 Tick = 1 秒。数值越大存在越久，越小结束越快。",
    "importance": "调度重要性。low 更适合背景效果，normal 是默认值，high 用于更重要、应优先保留的效果。",
    "visibility.drawDistance": "最大可见距离，单位方块。数值越大越远可见，但渲染压力也更高。",
    "visibility.hideWhenShadersConflict": "启用后，如果检测到光影兼容问题，会隐藏该特效，避免错误渲染。",
    "components.trail.enabled": "启用拖尾轨迹组件。轨迹会随时间采样运动点，并把这些点连成带状条带。",
    "components.trail.style": "轨迹样式标签。当前主要作为配置元数据，可填写 ribbon、flame、water 等便于识别的名称。",
    "components.trail.width.start": "新生成轨迹点的宽度。数值越大头部越粗，越小越细。常用范围：0.2 到 0.6。",
    "components.trail.width.end": "最旧轨迹点的宽度。填 0 会收成尖尾；接近起始宽度则形成等宽条带。",
    "components.trail.color.start": "新轨迹点颜色。格式 #RRGGBB 或 #RRGGBBAA。示例：#FFFF6600 表示橙色。",
    "components.trail.color.end": "旧轨迹点颜色。与起始色不同可形成渐变；Alpha 用 00 可逐渐透明。",
    "components.trail.lifetimeTicks": "每个轨迹点保留多久。数值越大轨迹越长、开销越高；越小则拖尾更短。",
    "components.trail.maxPoints": "最多保存多少轨迹点。更高会更平滑/更长，但 CPU/GPU 开销更高。通常 32-96 较合理。",
    "components.trail.minSampleDistance": "关闭每 Tick 采样时，移动多远才新增轨迹点。越大点数越少但转角更粗糙。",
    "components.trail.maxSegmentLength": "轨迹点之间允许的最大距离。越小越能补齐高速移动的空隙；越大更省性能但可能分段明显。",
    "components.trail.texture": "轨迹使用的纹理资源路径，必须真实存在。示例：minecraft:textures/particle/flame.png、minecraft:textures/particle/drip_fall.png、minecraft:textures/particle/splash_0.png。",
    "components.trail.blendMode": "additive 会更亮、更发光；alpha 是普通透明，更适合烟、水滴一类较柔和效果。",
    "components.trail.motion.mode": "follow 直接跟随源点；orbit 围绕源点旋转；helix 在旋转基础上加入上下波动；formula 使用自定义 X/Y/Z 公式。",
    "components.trail.motion.radius": "环绕/公式半径，单位方块。0 表示无环绕偏移；越大圆周或使用 radius 的公式越宽。",
    "components.trail.motion.angularSpeed": "绕源点旋转的角速度，单位度/Tick。正负方向相反。20 表示约 18 Tick 转一圈。",
    "components.trail.motion.verticalAmplitude": "螺旋/公式的最大垂直偏移。越大上下波动越高；0 表示保持平面。",
    "components.trail.motion.verticalSpeed": "垂直波动速度，单位度/Tick。绝对值越大，上下起伏越快。",
    "components.trail.motion.phase": "初始角度偏移，单位度。用于改变起始位置，不改变速度。",
    "components.trail.motion.formula.x": `${formulaHelp.zh}\n该字段控制相对源点的 X 偏移。示例：Math.cos(angle) * radius`,
    "components.trail.motion.formula.y": `${formulaHelp.zh}\n该字段控制垂直 Y 偏移。示例：Math.sin(verticalAngle) * verticalAmplitude`,
    "components.trail.motion.formula.z": `${formulaHelp.zh}\n该字段控制相对源点的 Z 偏移。示例：Math.sin(angle) * radius`,
    "components.trail.maxInsertedPointsPerTick": "一 Tick 内移动过远时最多补插多少轨迹点。数值越大高速运动越平滑；越小越省性能。",
    "components.trail.sampleEveryTick": "启用后每 Tick 都采样，轨迹更平滑。关闭后由最小采样距离决定何时新增点。",
    "components.trail.renderMode": "face_camera 会让条带朝向摄像机；world_up 会让条带按世界向上方向保持竖直。",
    "components.magicCircle.enabled": "启用源点位置的法阵组件。",
    "components.magicCircle.style": "法阵样式标签。可用 arcane、rune、seal 等名称便于整理。",
    "components.magicCircle.radius": "法阵半径，单位方块。越大覆盖范围越大；过大也会增加渲染开销。",
    "components.magicCircle.color": "法阵颜色。格式 #RRGGBB 或 #RRGGBBAA。",
    "components.magicCircle.thickness": "法阵线条厚度。越大越粗重，越小越精细。",
    "components.magicCircle.segments": "圆环分段数。越高越圆滑但开销越高。常用 64-128。",
    "components.magicCircle.facing": "face_camera 朝向摄像机；horizontal 平铺在世界水平面。",
    "components.magicCircle.rotationSpeed": "旋转速度，单位度/Tick。正负值旋转方向相反。",
    "components.magicCircle.innerRadiusScale": "内环半径占外环半径的比例。越小内外环距离越大；越大越接近外环。",
    "components.magicCircle.glyphs": "符文槽数量。0 表示不放符文；越大符号越密。",
    "components.magicCircle.blendMode": "additive 更发光；alpha 是普通透明。",
    "components.beam.enabled": "启用从源点到目标的光束组件。",
    "components.beam.style": "光束样式标签。可用 mana、lightning、laser 等名称。",
    "components.beam.width": "光束宽度，单位方块。越大越粗，越小越像细射线。",
    "components.beam.color.start": "光束源点颜色。格式 #RRGGBB 或 #RRGGBBAA。",
    "components.beam.color.end": "光束目标端颜色。与起始色不同可形成渐变。",
    "components.beam.segments": "光束分段数。分段越多，扰动曲线越平滑；1 表示直线光束。",
    "components.beam.noise": "光束扰动幅度。0 是直线；越大越像闪电弯折。",
    "components.beam.texture": "光束纹理资源路径。示例：minecraft:textures/misc/white.png 或模组自定义纹理。",
    "components.beam.blendMode": "additive 适合明亮能量束；alpha 更柔和、不那么发光。",
    "preview.targetMode": "fixed_distance 使用玩家前方固定点；crosshair_entity 使用准星实体；look_point 使用视线命中的点。",
    "preview.fixedDistance": "固定预览目标距离，单位方块。越大越适合预览长光束/长轨迹。",
    "preview.sourceHeightOffset": "源点相对实体脚底的高度偏移。0.55 接近身体高度；越大源点越高。",
    "preview.targetHeightOffset": "目标点相对目标实体脚底的高度偏移。可用于瞄准身体或头部高度。",
    "preview.fallbackToFixedDistance": "启用后，如果找不到实体/命中点，会回退到固定距离预览。",
    "components.advanced.enabled": "高级效果的总开关。关闭后会保留 JSON 设置，但不显示泛光、核心光团、粒子、流光束、法阵层和放射光刺。",
    "components.advanced.bloom.layers": "旧式辉光近似的复制层数。它只是围绕精灵叠加半透明面片，不是真正的后处理 Bloom。启用真实辉光后建议保持较低。",
    "components.advanced.bloom.scaleStep": "旧式近似中每层面片的放大倍率。真实辉光请使用“辉光半径”。",
    "components.advanced.bloom.alphaFalloff": "旧式近似中外层面片的透明衰减。真实辉光会使用后处理模糊和强度控制。",
    "components.advanced.glow.enabled": "启用高级丝带、法阵、粒子和核心精灵的真实屏幕空间辉光。发光几何会先渲染到离屏缓冲，再模糊并叠加回画面。",
    "components.advanced.glow.intensity": "模糊辉光最终叠加到画面的强度。数值越高光溢出越明显；过高会洗掉颜色层次。",
    "components.advanced.glow.radius": "模糊半径倍率。数值越大，光从丝带和符文向外扩散越远；数值越小则保持紧致霓虹边缘。",
    "components.advanced.glow.iterations": "模糊迭代次数。次数越多辉光越平滑、越宽，但 GPU 开销越高。常用范围是 3-6。",
    "components.advanced.glow.downsample": "辉光缓冲的降采样倍率。2 较清晰，4 更省性能且更柔，1 质量最高但开销最大。",
    "components.advanced.glow.threshold": "游戏渲染器中模糊前的亮度阈值。0 表示所有高级加法几何都参与辉光；数值越高越只保留最亮部分。Web 预览只做近似显示。",
    "components.advanced.core.enabled": "启用源点中心的光团。当效果需要明显能量核心，而不只是轨迹或粒子时使用。",
    "components.advanced.core.color": "核心光团颜色。可以用 #RRGGBBAA 里的 alpha 控制它对周围元素的覆盖强度。",
    "components.advanced.core.radius": "核心光团的基础半径，单位为方块。数值越大光球越大；数值越小则是更紧凑的高光。",
    "components.advanced.core.pulseAmplitude": "核心半径随时间脉冲的幅度。0 表示大小恒定；数值越高，呼吸或心跳感越强。",
    "components.advanced.particleEmitters.0.shape": "粒子发射器体积形状。sphere 向四周扩散，column 竖直堆叠，ring 围绕圆环，box 填充长方体区域。",
    "components.advanced.particleEmitters.0.count": "第一个发射器的粒子数量。数值越高，点云越密，但渲染开销也越高。",
    "components.advanced.particleEmitters.0.color.start": "粒子生命开始时的颜色。和结束颜色搭配可以做出随时间变色的效果。",
    "components.advanced.particleEmitters.0.color.end": "粒子生命结束附近的颜色。降低 alpha 可以让粒子淡出，而不是突然消失。",
    "components.advanced.particleEmitters.0.size.start": "粒子生成时的大小。起始尺寸越大，点云看起来越厚重、越亮。",
    "components.advanced.particleEmitters.0.size.end": "粒子生命结束附近的大小。较小的结束尺寸会形成收缩火花；接近起始值则保持稳定光点。",
    "components.advanced.particleEmitters.0.radius": "粒子发射器的水平扩散半径。对 sphere、ring 和 column 形状影响明显，数值越大粒子范围越宽。",
    "components.advanced.particleEmitters.0.height": "粒子发射器的垂直扩散高度。对 column 和类 box 效果最明显，数值越大柱体或体积越高。",
    "components.advanced.particleEmitters.0.swirlSpeed": "粒子的环绕旋转速度。正负值旋转方向相反；0 表示不做旋流运动。",
    "components.advanced.ribbonBundles.0.count": "第一组流光束的条带数量。数量越多，能量编织感越饱满，但几何开销也更高。",
    "components.advanced.ribbonBundles.0.length": "每条流光的长度。数值越大，拖出的流动线条越长；数值越小则更紧凑。",
    "components.advanced.ribbonBundles.0.amplitude": "流光束的波动幅度。数值越高，正弦摆动越宽；0 会让流光更贴近原本路径。",
    "components.advanced.ribbonBundles.0.frequency": "流光沿路径的波动频率。数值越高细密波纹越多；数值越低则是大弧度的平滑曲线。",
    "components.advanced.ribbonBundles.0.twist": "流光围绕束路径扭转的强度。数值越高，条带互相缠绕的编织感越强。",
    "components.advanced.ribbonBundles.0.flowSpeed": "流光纹理或相位的动画速度。数值越高，能量流动越快；0 会冻结流动。",
    "components.advanced.circleLayers.0.radius": "第一个高级法阵层的半径。数值越大，层离源点越远，封印覆盖面积也越大。",
    "components.advanced.circleLayers.0.glyphs": "高级法阵层上的符文数量。0 表示不放符文；数值越高，圆环越密集、越华丽。",
    "components.advanced.radialBursts.0.rays": "放射爆发光刺的数量。数值越高，星芒越饱满；数值越低，单条尖刺更明显。",
    "components.advanced.radialBursts.0.length": "每条放射光刺的长度，单位为方块。数值越大，尖刺越锐利且伸展越远；数值越小则更贴近核心。"
  }
};

const helpFor = (key) => helpText[language]?.[key] ?? helpText.en[key] ?? "";

const pages = [
  { id: "basic", title: "basic", hint: "basicHint" },
  { id: "trail", title: "trail", hint: "trailHint" },
  { id: "motion", title: "motion", hint: "motionHint" },
  { id: "circle", title: "circle", hint: "circleHint" },
  { id: "beam", title: "beam", hint: "beamHint" },
  { id: "advanced", title: "advanced", hint: "advancedHint" },
  { id: "preview", title: "previewPage", hint: "previewHint" }
];

let state = null;
let project = null;
let page = "basic";
let preview3d = null;
let defaultDraft = null;
let runtimeEffects = [];

const $ = (id) => document.getElementById(id);
const layoutStorageKey = "magicrender.editor.layout";
const pathGet = (obj, path) => path.split(".").reduce((a, b) => a?.[b], obj);
const pathSet = (obj, path, value) => {
  const parts = path.split(".");
  let cursor = obj;
  for (const part of parts.slice(0, -1)) cursor = cursor[part] ??= {};
  cursor[parts.at(-1)] = value;
};

const fields = {
  basic: [
    ["id", "effectId", "text"],
    ["enabled", "enabled", "checkbox"],
    ["group", "group", "text"],
    ["durationTicks", "durationTicks", "number"],
    ["importance", "importance", "select", ["low", "normal", "high"]],
    ["visibility.drawDistance", "drawDistance", "number"],
    ["visibility.hideWhenShadersConflict", "hideWhenShadersConflict", "checkbox"]
  ],
  trail: [
    ["components.trail.enabled", "trailEnabled", "checkbox"],
    ["components.trail.style", "style", "text"],
    ["components.trail.width.start", "widthStart", "number"],
    ["components.trail.width.end", "widthEnd", "number"],
    ["components.trail.color.start", "colorStart", "color"],
    ["components.trail.color.end", "colorEnd", "color"],
    ["components.trail.lifetimeTicks", "lifetimeTicks", "number"],
    ["components.trail.maxPoints", "maxPoints", "number"],
    ["components.trail.minSampleDistance", "minSampleDistance", "number"],
    ["components.trail.maxSegmentLength", "maxSegmentLength", "number"],
    ["components.trail.texture", "texture", "text"],
    ["components.trail.blendMode", "blendMode", "select", ["additive", "alpha"]]
  ],
  motion: [
    ["components.trail.motion.mode", "mode", "select", ["follow", "orbit", "helix", "formula"]],
    ["components.trail.motion.radius", "radius", "number"],
    ["components.trail.motion.angularSpeed", "angularSpeed", "number"],
    ["components.trail.motion.verticalAmplitude", "verticalAmplitude", "number"],
    ["components.trail.motion.verticalSpeed", "verticalSpeed", "number"],
    ["components.trail.motion.phase", "phase", "number"],
    ["components.trail.motion.formula.x", "formulaX", "textarea"],
    ["components.trail.motion.formula.y", "formulaY", "textarea"],
    ["components.trail.motion.formula.z", "formulaZ", "textarea"],
    ["components.trail.maxInsertedPointsPerTick", "maxInsertedPoints", "number"],
    ["components.trail.sampleEveryTick", "sampleEveryTick", "checkbox"],
    ["components.trail.renderMode", "renderMode", "select", ["face_camera", "world_up"]]
  ],
  circle: [
    ["components.magicCircle.enabled", "circleEnabled", "checkbox"],
    ["components.magicCircle.style", "style", "text"],
    ["components.magicCircle.radius", "radius", "number"],
    ["components.magicCircle.color", "colorStart", "color"],
    ["components.magicCircle.thickness", "thickness", "number"],
    ["components.magicCircle.segments", "segments", "number"],
    ["components.magicCircle.facing", "facing", "select", ["face_camera", "horizontal"]],
    ["components.magicCircle.rotationSpeed", "rotationSpeed", "number"],
    ["components.magicCircle.innerRadiusScale", "innerRadiusScale", "number"],
    ["components.magicCircle.glyphs", "glyphs", "number"],
    ["components.magicCircle.blendMode", "blendMode", "select", ["additive", "alpha"]]
  ],
  beam: [
    ["components.beam.enabled", "beamEnabled", "checkbox"],
    ["components.beam.style", "style", "text"],
    ["components.beam.width", "width", "number"],
    ["components.beam.color.start", "colorStart", "color"],
    ["components.beam.color.end", "colorEnd", "color"],
    ["components.beam.segments", "segments", "number"],
    ["components.beam.noise", "noise", "number"],
    ["components.beam.texture", "texture", "text"],
    ["components.beam.blendMode", "blendMode", "select", ["additive", "alpha"]]
  ],
  advanced: [
    ["components.advanced.enabled", "advancedEnabled", "checkbox"],
    ["components.advanced.bloom.layers", "bloomLayers", "number"],
    ["components.advanced.bloom.scaleStep", "bloomScaleStep", "number"],
    ["components.advanced.bloom.alphaFalloff", "bloomAlphaFalloff", "number"],
    ["components.advanced.glow.enabled", "glowEnabled", "checkbox"],
    ["components.advanced.glow.intensity", "glowIntensity", "number"],
    ["components.advanced.glow.radius", "glowRadius", "number"],
    ["components.advanced.glow.iterations", "glowIterations", "number"],
    ["components.advanced.glow.downsample", "glowDownsample", "number"],
    ["components.advanced.glow.threshold", "glowThreshold", "number"],
    ["components.advanced.core.enabled", "coreEnabled", "checkbox"],
    ["components.advanced.core.color", "colorStart", "color"],
    ["components.advanced.core.radius", "coreRadius", "number"],
    ["components.advanced.core.pulseAmplitude", "corePulseAmplitude", "number"],
    ["components.advanced.particleEmitters.0.shape", "emitterShape", "select", ["sphere", "column", "ring", "box"]],
    ["components.advanced.particleEmitters.0.count", "emitterCount", "number"],
    ["components.advanced.particleEmitters.0.color.start", "colorStart", "color"],
    ["components.advanced.particleEmitters.0.color.end", "colorEnd", "color"],
    ["components.advanced.particleEmitters.0.size.start", "emitterSizeStart", "number"],
    ["components.advanced.particleEmitters.0.size.end", "emitterSizeEnd", "number"],
    ["components.advanced.particleEmitters.0.radius", "radius", "number"],
    ["components.advanced.particleEmitters.0.height", "emitterHeight", "number"],
    ["components.advanced.particleEmitters.0.swirlSpeed", "emitterSwirl", "number"],
    ["components.advanced.ribbonBundles.0.count", "bundleCount", "number"],
    ["components.advanced.ribbonBundles.0.length", "bundleLength", "number"],
    ["components.advanced.ribbonBundles.0.amplitude", "bundleAmplitude", "number"],
    ["components.advanced.ribbonBundles.0.frequency", "bundleFrequency", "number"],
    ["components.advanced.ribbonBundles.0.twist", "bundleTwist", "number"],
    ["components.advanced.ribbonBundles.0.flowSpeed", "bundleFlow", "number"],
    ["components.advanced.circleLayers.0.radius", "circleLayerRadius", "number"],
    ["components.advanced.circleLayers.0.glyphs", "circleLayerGlyphs", "number"],
    ["components.advanced.radialBursts.0.rays", "burstRays", "number"],
    ["components.advanced.radialBursts.0.length", "burstLength", "number"]
  ],
  preview: [
    ["preview.targetMode", "targetMode", "select", ["fixed_distance", "crosshair_entity", "look_point"]],
    ["preview.fixedDistance", "fixedDistance", "number"],
    ["preview.sourceHeightOffset", "sourceHeight", "number"],
    ["preview.targetHeightOffset", "targetHeight", "number"],
    ["preview.fallbackToFixedDistance", "fallbackDistance", "checkbox"]
  ]
};

function applyLanguage() {
  document.documentElement.lang = language === "zh" ? "zh-CN" : "en";
  document.querySelectorAll("[data-i18n]").forEach((node) => {
    node.textContent = t(node.dataset.i18n);
  });
  document.querySelectorAll("[data-i18n-title]").forEach((node) => {
    node.title = t(node.dataset.i18nTitle);
    node.setAttribute("aria-label", t(node.dataset.i18nTitle));
  });
  const zhOption = document.querySelector("#languageSelect option[value='zh']");
  if (zhOption) zhOption.textContent = "\u4e2d\u6587";
  $("languageSelect").value = language;
  if ($("status").textContent === "" || $("status").textContent === translations.en.ready || $("status").textContent === translations.zh.ready) {
    setStatus(t("ready"));
  }
  const expand = $("expandPreviewBtn");
  if (expand) {
    expand.textContent = document.body.classList.contains("preview-expanded") ? "⤡" : "⤢";
    expand.title = document.body.classList.contains("preview-expanded") ? t("collapsePreview") : t("expandPreview");
    expand.setAttribute("aria-label", expand.title);
  }
}

async function api(path, body) {
  const options = body === undefined ? {} : {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body)
  };
  const response = await fetch(path, options);
  if (!response.ok) throw new Error(await response.text());
  return response.json();
}

function cloneJson(value) {
  return JSON.parse(JSON.stringify(value));
}

function createProject(effect) {
  const draft = cloneJson(effect);
  const groupKey = validGroupKey(draft.group) ? draft.group : "magicrender:editor_group";
  draft.group = groupKey;
  return {
    group: { key: groupKey, description: "" },
    effects: [draft],
    selectedIndex: 0
  };
}

function validGroupKey(value) {
  return /^[a-z0-9_.-]+:[a-z0-9_./-]+$/.test(String(value || ""));
}

function selectedEffect() {
  if (!project?.effects?.length) return null;
  project.selectedIndex = Math.max(0, Math.min(project.selectedIndex || 0, project.effects.length - 1));
  return project.effects[project.selectedIndex];
}

function syncStateFromProject() {
  state = selectedEffect();
  if (state && project?.group?.key) state.group = project.group.key;
}

function projectPayload() {
  syncStateFromProject();
  return {
    group: {
      key: project.group.key,
      description: project.group.description || ""
    },
    effects: project.effects,
    selectedIndex: project.selectedIndex || 0,
    includeEffects: $("includeEffectsOnGroupExport")?.checked !== false
  };
}

function nextChildId() {
  const prefix = String(project?.group?.key || "magicrender:editor_group").replace(":", ":").replace(/[^a-z0-9_:./-]/g, "_");
  const base = prefix.includes(":") ? prefix : "magicrender:editor_group";
  let index = project.effects.length + 1;
  const existing = new Set(project.effects.map((effect) => effect.id));
  while (existing.has(`${base}_child_${index}`)) index += 1;
  return `${base}_child_${index}`;
}

function addChildEffect(source = null) {
  const child = cloneJson(source || defaultDraft || state);
  child.id = source?.id ? uniqueImportedId(source.id) : nextChildId();
  child.group = project.group.key;
  project.effects.push(child);
  project.selectedIndex = project.effects.length - 1;
  render();
}

function uniqueImportedId(id) {
  const existing = new Set(project.effects.map((effect) => effect.id));
  if (!existing.has(id)) return id;
  let index = 2;
  while (existing.has(`${id}_${index}`)) index += 1;
  return `${id}_${index}`;
}

function updateProjectGroupFromInputs() {
  const nextKey = $("groupKeyInput").value.trim();
  project.group.key = nextKey || "magicrender:editor_group";
  project.group.description = $("groupDescriptionInput").value;
  for (const effect of project.effects) effect.group = project.group.key;
  syncStateFromProject();
  updateJson();
  renderProjectTree();
}

function renderProjectTree() {
  if (!project) return;
  $("groupKeyInput").value = project.group.key || "";
  $("groupDescriptionInput").value = project.group.description || "";
  const tree = $("projectTree");
  tree.innerHTML = "";

  const root = document.createElement("div");
  root.className = "project-node";
  const rootButton = document.createElement("button");
  rootButton.type = "button";
  rootButton.className = "node-main secondary";
  rootButton.textContent = `${t("groupNodePrefix")}: ${project.group.key || "(empty)"}`;
  rootButton.onclick = () => setStatus(t("groupNodeHint"));
  root.appendChild(rootButton);
  root.appendChild(document.createElement("span"));
  root.appendChild(document.createElement("span"));
  tree.appendChild(root);

  project.effects.forEach((effect, index) => {
    const row = document.createElement("div");
    row.className = "project-node";
    const select = document.createElement("button");
    select.type = "button";
    select.className = `node-main child secondary${index === project.selectedIndex ? " active" : ""}`;
    select.textContent = effect.id || `child_${index + 1}`;
    select.title = effect.id || "";
    select.onclick = () => {
      project.selectedIndex = index;
      render();
    };
    const exportOne = document.createElement("button");
    exportOne.type = "button";
    exportOne.className = "secondary";
    exportOne.textContent = t("exportChild");
    exportOne.onclick = async () => {
      project.selectedIndex = index;
      syncStateFromProject();
      const result = await api("/api/export", state);
      setStatus(result.message, result.ok);
    };
    const remove = document.createElement("button");
    remove.type = "button";
    remove.className = "danger";
    remove.textContent = t("deleteChild");
    remove.disabled = project.effects.length <= 1;
    remove.onclick = () => {
      project.effects.splice(index, 1);
      project.selectedIndex = Math.max(0, Math.min(project.selectedIndex, project.effects.length - 1));
      render();
    };
    row.appendChild(select);
    row.appendChild(exportOne);
    row.appendChild(remove);
    tree.appendChild(row);
  });
}

function renderImportOptions() {
  const select = $("importEffectSelect");
  if (!select) return;
  select.innerHTML = "";
  for (const effect of runtimeEffects) {
    const option = document.createElement("option");
    option.value = effect.id;
    option.textContent = effect.id;
    select.appendChild(option);
  }
}

function clamp(value, min, max) {
  return Math.max(min, Math.min(max, value));
}

function readLayout() {
  try {
    return JSON.parse(localStorage.getItem(layoutStorageKey) || "{}");
  } catch {
    return {};
  }
}

function writeLayout(next) {
  localStorage.setItem(layoutStorageKey, JSON.stringify({ ...readLayout(), ...next }));
}

function applyStoredLayout() {
  const layout = readLayout();
  if (Number.isFinite(layout.groupbar)) {
    document.documentElement.style.setProperty("--groupbar-width", `${clamp(layout.groupbar, 300, 560)}px`);
  }
  if (Number.isFinite(layout.typebar)) {
    document.documentElement.style.setProperty("--typebar-width", `${clamp(layout.typebar, 210, 380)}px`);
  }
  if (Number.isFinite(layout.form)) {
    document.documentElement.style.setProperty("--form-width", `${clamp(layout.form, 500, 880)}px`);
  }
}

function setupResizers() {
  document.querySelectorAll("[data-resize]").forEach((handle) => {
    handle.addEventListener("pointerdown", (event) => {
      event.preventDefault();
      handle.setPointerCapture?.(event.pointerId);
      handle.classList.add("dragging");
      const resizeTarget = handle.dataset.resize;
      const onMove = (moveEvent) => {
        if (resizeTarget === "groupbar") resizeGroupbar(moveEvent.clientX);
        else if (resizeTarget === "typebar") resizeTypebar(moveEvent.clientX);
        else if (resizeTarget === "form") resizeForm(moveEvent.clientX);
        resizePreview3d();
        renderPreview3d();
      };
      const onEnd = () => {
        handle.classList.remove("dragging");
        window.removeEventListener("pointermove", onMove);
        window.removeEventListener("pointerup", onEnd);
        window.removeEventListener("pointercancel", onEnd);
      };
      window.addEventListener("pointermove", onMove);
      window.addEventListener("pointerup", onEnd);
      window.addEventListener("pointercancel", onEnd);
    });
  });
}

function resizeGroupbar(clientX) {
  const shell = document.querySelector(".shell").getBoundingClientRect();
  const width = clamp(clientX - shell.left, 300, Math.min(560, shell.width - 760));
  document.documentElement.style.setProperty("--groupbar-width", `${width}px`);
  writeLayout({ groupbar: width });
}

function resizeTypebar(clientX) {
  const shell = document.querySelector(".shell").getBoundingClientRect();
  const groupWidth = document.querySelector(".groupbar").getBoundingClientRect().width;
  const width = clamp(clientX - shell.left - groupWidth - 6, 210, Math.min(380, shell.width - groupWidth - 620));
  document.documentElement.style.setProperty("--typebar-width", `${width}px`);
  writeLayout({ typebar: width });
}

function resizeForm(clientX) {
  const content = document.querySelector(".content").getBoundingClientRect();
  const width = clamp(clientX - content.left, 500, Math.max(500, content.width - 430));
  document.documentElement.style.setProperty("--form-width", `${width}px`);
  writeLayout({ form: width });
}

function renderTabs() {
  $("tabs").innerHTML = "";
  for (const item of pages) {
    const button = document.createElement("button");
    button.textContent = t(item.title);
    button.className = item.id === page ? "active" : "";
    button.onclick = () => {
      page = item.id;
      render();
    };
    $("tabs").appendChild(button);
  }
}

function renderForm() {
  ensureAdvancedDefaults();
  const meta = pages.find((p) => p.id === page);
  $("pageTitle").textContent = t(meta.title);
  $("pageHint").textContent = t(meta.hint);
  const form = $("form");
  form.innerHTML = "";
  for (const [key, label, type, options] of fields[page]) {
    const wrap = document.createElement("div");
    wrap.className = "field";
    const header = document.createElement("div");
    header.className = "field-head";
    const text = document.createElement("label");
    text.textContent = t(label);
    header.appendChild(text);
    const help = helpFor(key);
    if (help) {
      const helpButton = document.createElement("button");
      helpButton.type = "button";
      helpButton.className = "help-button";
      helpButton.textContent = "?";
      helpButton.setAttribute("aria-label", `${t(label)} help`);
      helpButton.dataset.help = help;
      header.appendChild(helpButton);
    }
    wrap.appendChild(header);
    let input;
    if (type === "select") {
      input = document.createElement("select");
      for (const option of options) {
        const item = document.createElement("option");
        item.value = option;
        item.textContent = option;
        input.appendChild(item);
      }
    } else if (type === "textarea") {
      input = document.createElement("textarea");
    } else {
      input = document.createElement("input");
      input.type = type === "color" ? "text" : type;
      if (type === "number") input.step = "any";
    }
    const current = pathGet(state, key);
    if (type === "checkbox") input.checked = Boolean(current);
    else input.value = current ?? "";
    input.oninput = () => {
      const value = type === "checkbox" ? input.checked : type === "number" ? Number(input.value) : input.value;
      pathSet(state, key, value);
      if (key === "group" && project) {
        project.group.key = value || "magicrender:editor_group";
        for (const effect of project.effects) effect.group = project.group.key;
      }
      renderProjectTree();
      updateJson();
      drawPreview();
    };
    wrap.appendChild(input);
    form.appendChild(wrap);
  }
}

function ensureAdvancedDefaults() {
  state.components ??= {};
  state.components.advanced ??= {};
  const advanced = state.components.advanced;
  advanced.enabled ??= false;
  advanced.bloom ??= { enabled: true, layers: 3, scaleStep: 1.8, alphaFalloff: 0.45 };
  advanced.glow ??= { enabled: true, intensity: 1.35, radius: 1.0, iterations: 4, downsample: 2, threshold: 0.0 };
  advanced.core ??= { enabled: false, color: "#FFFFFFFF", radius: 0.6, pulseAmplitude: 0.18, pulseSpeed: 0.12, texture: "minecraft:textures/particle/flash.png", blendMode: "additive" };
  advanced.particleEmitters ??= [];
  advanced.ribbonBundles ??= [];
  advanced.circleLayers ??= [];
  advanced.radialBursts ??= [];
  advanced.particleEmitters[0] ??= { enabled: true, shape: "sphere", count: 96, color: { start: "#FFFFFFFF", end: "#FFFFFFFF" }, size: { start: 0.08, end: 0.02 }, radius: 1.2, height: 2.0, speed: 0.02, swirlSpeed: 0, noise: 0.2, texture: "minecraft:textures/particle/flash.png", blendMode: "additive" };
  advanced.ribbonBundles[0] ??= { enabled: true, count: 8, width: { start: 0.12, end: 0.02 }, color: { start: "#FFFFFFFF", end: "#FFFFFFFF" }, length: 8, samples: 96, phaseStep: 24, amplitude: 0.8, frequency: 1.4, twist: 0.45, flowSpeed: 0.08, texture: "minecraft:textures/particle/flame.png", blendMode: "additive" };
  advanced.circleLayers[0] ??= { enabled: true, radius: 2, thickness: 0.04, color: "#FFFFFFFF", segments: 128, rotationSpeed: 1, glyphs: 12, glyphMode: "ticks", facing: "face_camera", blendMode: "additive" };
  advanced.radialBursts[0] ??= { enabled: true, rays: 16, length: 2.8, width: { start: 0.08, end: 0 }, color: { start: "#FFFFFFFF", end: "#FFFFFF00" }, rotationSpeed: 0, randomJitter: 0.15, texture: "minecraft:textures/particle/flash.png", blendMode: "additive" };
}

function updateJson() {
  $("jsonView").value = project ? JSON.stringify(projectPayload(), null, 2) : JSON.stringify(state, null, 2);
}

function setStatus(message, ok = true) {
  $("status").textContent = message;
  $("status").style.color = ok ? "var(--muted)" : "var(--danger)";
}

function renderMessages(result) {
  const box = $("messages");
  box.innerHTML = "";
  for (const error of result.errors ?? []) addMessage(error, true);
  for (const warning of result.warnings ?? []) addMessage(warning, false);
}

function addMessage(text, error) {
  const item = document.createElement("div");
  item.className = error ? "message error" : "message";
  item.textContent = text;
  $("messages").appendChild(item);
}

async function refreshRuntime() {
  const status = await api("/api/status");
  $("runtime").innerHTML = `<dt>${t("runtimeUrl")}</dt><dd>${status.url}</dd><dt>${t("runtimeEffects")}</dt><dd>${status.effects}</dd><dt>${t("runtimePort")}</dt><dd>${status.port}</dd>`;
  try {
    const effects = await api("/api/effects");
    runtimeEffects = (effects.effects ?? []).filter((effect) => effect.config);
    renderImportOptions();
  } catch (error) {
    runtimeEffects = [];
    renderImportOptions();
  }
}

function drawPreview() {
  if (!state) return;
  initPreview3d();
  rebuildPreview3d();
  renderPreview3d();
}

function initPreview3d() {
  if (preview3d) return;
  const canvas = $("previewCanvas");
  const scene = new THREE.Scene();
  scene.background = new THREE.Color(0x0b0f14);
  const camera = new THREE.PerspectiveCamera(48, 1, 0.05, 300);
  camera.position.set(6, 4.2, 7);
  const renderer = new THREE.WebGLRenderer({ canvas, antialias: true, alpha: false });
  renderer.setPixelRatio(Math.min(window.devicePixelRatio || 1, 2));
  renderer.autoClear = false;
  const controls = new OrbitControls(camera, canvas);
  controls.enableDamping = true;
  controls.dampingFactor = 0.08;
  controls.target.set(1.8, 0.9, 0);
  controls.maxDistance = 80;
  controls.minDistance = 1.2;

  const root = new THREE.Group();
  scene.add(root);
  scene.add(new THREE.GridHelper(16, 16, 0x304050, 0x1d2832));
  scene.add(new THREE.AxesHelper(1.8));
  const light = new THREE.DirectionalLight(0xffffff, 1.8);
  light.position.set(5, 7, 4);
  scene.add(light);
  scene.add(new THREE.AmbientLight(0x7f95aa, 1.2));

  const clock = new THREE.Clock();
  preview3d = { canvas, scene, camera, renderer, controls, root, animated: [], lastWidth: 0, lastHeight: 0, clock, time: 0, tick: 0, glowScene: new THREE.Scene(), glowTarget: null, trailHistory: [] };
  window.addEventListener("resize", () => {
    resizePreview3d();
    renderPreview3d();
  });
  const animate = () => {
    if (!preview3d) return;
    requestAnimationFrame(animate);
    const delta = Math.min(0.05, preview3d.clock.getDelta());
    preview3d.time += delta;
    preview3d.tick = preview3d.time * 20;
    updatePreviewDynamics(preview3d);
    for (const item of preview3d.animated) item.rotation.y += item.userData.spin ?? 0.01;
    preview3d.controls.update();
    renderPreview3d();
  };
  animate();
}

function rebuildPreview3d() {
  const view = preview3d;
  clearGroup(view.root);
  clearGroup(view.glowScene);
  view.animated = [];
  view.trailHistory = [];
  view.time = 0;
  view.tick = 0;
  view.clock.getDelta();
  const source = new THREE.Vector3(0, state.preview.sourceHeightOffset || 0, 0);
  const target = new THREE.Vector3(Math.max(1, state.preview.fixedDistance || 4), state.preview.targetHeightOffset || 0.8, 0);
  addPoint(view.root, source, 0x67d8ff);
  addPoint(view.root, target, 0xffd166);
  if (state.components.beam.enabled) addBeam(view.root, source, target);
  if (state.components.trail.enabled) addTrail(view, source);
  if (state.components.magicCircle.enabled) addMagicCircle(view, source);
  if (state.components.advanced?.enabled) addAdvanced(view, source, target);
}

function addPoint(root, position, color) {
  const mesh = new THREE.Mesh(
    new THREE.SphereGeometry(0.1, 16, 12),
    new THREE.MeshStandardMaterial({ color, emissive: color, emissiveIntensity: 0.35 })
  );
  mesh.position.copy(position);
  root.add(mesh);
}

function addBeam(root, source, target) {
  const beam = state.components.beam;
  const segments = Math.max(1, Math.round(beam.segments || 1));
  const points = [];
  const direction = new THREE.Vector3().subVectors(target, source);
  for (let i = 0; i <= segments; i++) {
    const progress = i / segments;
    const point = new THREE.Vector3().copy(source).addScaledVector(direction, progress);
    point.y += Math.sin(progress * Math.PI * Math.max(2, segments)) * (beam.noise || 0);
    points.push(point);
  }
  root.add(new THREE.Line(
    new THREE.BufferGeometry().setFromPoints(points),
    new THREE.LineBasicMaterial({ color: colorNumber(beam.color?.start || "#67d8ff"), transparent: true, opacity: alphaValue(beam.color?.start || "#67d8ff") })
  ));
  addTubeLikeMarkers(root, points, Math.max(0.015, (beam.width || 0.08) * 0.18), colorNumber(beam.color?.end || beam.color?.start || "#67d8ff"));
}

function addTrail(view, source) {
  const trail = state.components.trail;
  const mesh = new THREE.Mesh(new THREE.BufferGeometry(), ribbonMaterial(trail.color?.start || "#ffffff", trail.blendMode));
  mesh.userData.dynamicTrail = true;
  mesh.userData.source = source.clone();
  mesh.userData.trail = trail;
  view.root.add(mesh);
  addGlowClone(view, mesh);
}

function addMagicCircle(view, source) {
  const circle = state.components.magicCircle;
  const radius = Math.max(0.05, circle.radius || 1);
  const color = colorNumber(circle.color || "#67d8ff");
  const mesh = new THREE.Mesh(
    new THREE.TorusGeometry(radius, Math.max(0.01, circle.thickness || 0.03), 8, Math.max(16, Math.round(circle.segments || 64))),
    new THREE.MeshStandardMaterial({ color, emissive: color, emissiveIntensity: 0.45, transparent: true, opacity: alphaValue(circle.color || "#67d8ff") })
  );
  mesh.position.copy(source);
  if (circle.facing === "horizontal") mesh.rotation.x = Math.PI / 2;
  mesh.userData.spin = (circle.rotationSpeed || 0) * Math.PI / 180 / 8;
  view.root.add(mesh);
  view.animated.push(mesh);

  const glyphs = Math.max(0, Math.round(circle.glyphs || 0));
  for (let i = 0; i < glyphs; i++) {
    const angle = (i / glyphs) * Math.PI * 2;
    const marker = new THREE.Mesh(
      new THREE.BoxGeometry(0.08, 0.018, 0.18),
      new THREE.MeshStandardMaterial({ color, emissive: color, emissiveIntensity: 0.3 })
    );
    marker.position.set(source.x + Math.cos(angle) * radius, source.y, source.z + Math.sin(angle) * radius);
    marker.rotation.y = -angle;
    view.root.add(marker);
  }
}

function addAdvanced(view, source, target) {
  ensureAdvancedDefaults();
  const advanced = state.components.advanced;
  if (advanced.core?.enabled) addCoreGlow(view.root, source, advanced.core, advanced.bloom);
  for (const emitter of advanced.particleEmitters ?? []) {
    if (emitter.enabled !== false) addParticleEmitter(view.root, source, emitter, advanced.bloom);
  }
  for (const bundle of advanced.ribbonBundles ?? []) {
    if (bundle.enabled !== false) addRibbonBundle(view.root, source, target, bundle);
  }
  for (const layer of advanced.circleLayers ?? []) {
    if (layer.enabled !== false) addCircleLayer(view, source, layer);
  }
  for (const burst of advanced.radialBursts ?? []) {
    if (burst.enabled !== false) addRadialBurst(view.root, source, burst);
  }
}

function addCoreGlow(root, source, core, bloom) {
  const color = colorNumber(core.color);
  const material = new THREE.SpriteMaterial({ color, transparent: true, opacity: alphaValue(core.color), depthWrite: false, blending: THREE.AdditiveBlending });
  const sprite = new THREE.Sprite(material);
  const radius = Math.max(0.02, core.radius || 0.6);
  sprite.position.copy(source);
  sprite.scale.setScalar(radius * 2);
  root.add(sprite);
  addGlowClone(preview3d, sprite);
  addSpriteBloom(root, source, color, radius, bloom, alphaValue(core.color));
}

function addSpriteBloom(root, position, color, radius, bloom, opacity = 0.35) {
  if (!bloom?.enabled) return;
  let size = radius * 2;
  let alpha = opacity * (bloom.alphaFalloff ?? 0.45);
  const layers = Math.max(0, Math.min(8, Math.round(bloom.layers ?? 3)));
  for (let i = 0; i < layers; i++) {
    size *= bloom.scaleStep ?? 1.8;
    const sprite = new THREE.Sprite(new THREE.SpriteMaterial({ color, transparent: true, opacity: alpha, depthWrite: false, blending: THREE.AdditiveBlending }));
    sprite.position.copy(position);
    sprite.scale.setScalar(size);
    root.add(sprite);
    alpha *= bloom.alphaFalloff ?? 0.45;
  }
}

function addParticleEmitter(root, source, emitter, bloom) {
  const count = Math.max(0, Math.min(600, Math.round(emitter.count || 0)));
  const geometry = new THREE.BufferGeometry();
  const positions = new Float32Array(count * 3);
  const colors = new Float32Array(count * 3);
  const colorStart = new THREE.Color(colorNumber(emitter.color?.start || emitter.colorStart || "#ffffff"));
  const colorEnd = new THREE.Color(colorNumber(emitter.color?.end || emitter.colorEnd || "#ffffff"));
  for (let i = 0; i < count; i++) {
    const seed = randomSeed(i + 1);
    const p = particlePreviewPosition(source, emitter, seed, i);
    positions[i * 3] = p.x;
    positions[i * 3 + 1] = p.y;
    positions[i * 3 + 2] = p.z;
    const c = colorStart.clone().lerp(colorEnd, i / Math.max(1, count - 1));
    colors[i * 3] = c.r;
    colors[i * 3 + 1] = c.g;
    colors[i * 3 + 2] = c.b;
  }
  geometry.setAttribute("position", new THREE.BufferAttribute(positions, 3));
  geometry.setAttribute("color", new THREE.BufferAttribute(colors, 3));
  const points = new THREE.Points(geometry, new THREE.PointsMaterial({ size: emitter.size?.start || emitter.sizeStart || 0.08, vertexColors: true, transparent: true, opacity: 0.9, depthWrite: false, blending: THREE.AdditiveBlending }));
  root.add(points);
  addGlowClone(preview3d, points);
  if (bloom?.enabled && count > 0) addSpriteBloom(root, source, colorNumber(emitter.color?.start || emitter.colorStart || "#ffffff"), Math.max(0.15, (emitter.radius || 1.2) * 0.35), bloom, 0.18);
}

function particlePreviewPosition(source, emitter, seed, index) {
  const shape = String(emitter.shape || "sphere").toLowerCase();
  const radius = emitter.radius || 1.2;
  const height = emitter.height || 2;
  const angle = seed * Math.PI * 2;
  const second = randomSeed(index * 17 + 3) * Math.PI * 2;
  if (shape === "column" || shape === "pillar") return source.clone().add(new THREE.Vector3(Math.cos(angle) * radius, (randomSeed(index + 9) - 0.5) * height, Math.sin(angle) * radius));
  if (shape === "ring" || shape === "disc") return source.clone().add(new THREE.Vector3(Math.cos(angle) * radius, 0, Math.sin(angle) * radius));
  if (shape === "box" || shape === "cube") return source.clone().add(new THREE.Vector3((seed - 0.5) * radius * 2, (randomSeed(index + 5) - 0.5) * height, (randomSeed(index + 7) - 0.5) * radius * 2));
  const r = radius * (0.3 + randomSeed(index + 11) * 0.7);
  return source.clone().add(new THREE.Vector3(Math.cos(angle) * Math.sin(second) * r, Math.cos(second) * height * 0.5, Math.sin(angle) * Math.sin(second) * r));
}

function addRibbonBundle(root, source, target, bundle) {
  const count = Math.max(0, Math.min(48, Math.round(bundle.count || 0)));
  for (let line = 0; line < count; line++) {
    const points = ribbonBundlePoints(source, target, bundle, line, preview3d.tick);
    const mesh = buildRibbonMesh(
      points,
      bundle.width?.start || bundle.widthStart || 0.1,
      bundle.width?.end || bundle.widthEnd || 0.02,
      bundle.color?.start || bundle.colorStart || "#ffffff",
      bundle.color?.end || bundle.colorEnd || "#ffffff",
      preview3d.camera.position
    );
    mesh.material = ribbonMaterial(bundle.color?.start || bundle.colorStart || "#ffffff", bundle.blendMode);
    mesh.material.vertexColors = true;
    mesh.userData.ribbonBundle = { source: source.clone(), target: target.clone(), bundle, line };
    root.add(mesh);
    addGlowClone(preview3d, mesh);
  }
}

function ribbonBundlePoints(source, target, bundle, line, tick) {
  const direction = target.clone().sub(source).normalize();
  const side = new THREE.Vector3().crossVectors(direction, new THREE.Vector3(0, 1, 0)).normalize();
  if (!Number.isFinite(side.x)) side.set(0, 0, 1);
  const up = new THREE.Vector3().crossVectors(side, direction).normalize();
  const points = [];
  const samples = Math.max(8, Math.min(180, Math.round(bundle.samples || 96)));
  const phase = (line * (bundle.phaseStep || 24) + tick * (bundle.flowSpeed || 0) * 8) * Math.PI / 180;
  for (let i = 0; i < samples; i++) {
    const t = i / (samples - 1);
    const wave = Math.sin(t * Math.PI * 2 * (bundle.frequency || 1.4) + phase) * (bundle.amplitude || 0.8);
    const twist = Math.cos(t * Math.PI * 2 * (bundle.frequency || 1.4) + phase) * (bundle.amplitude || 0.8) * (bundle.twist || 0.45);
    points.push(source.clone().add(direction.clone().multiplyScalar((bundle.length || 8) * t)).add(side.clone().multiplyScalar(wave)).add(up.clone().multiplyScalar(twist)));
  }
  return points;
}

function addCircleLayer(view, source, layer) {
  const color = colorNumber(layer.color || "#ffffff");
  const mesh = new THREE.Mesh(
    new THREE.TorusGeometry(Math.max(0.05, layer.radius || 1), Math.max(0.006, layer.thickness || 0.03), 8, Math.max(16, Math.round(layer.segments || 96))),
    new THREE.MeshStandardMaterial({ color, emissive: color, emissiveIntensity: 0.5, transparent: true, opacity: alphaValue(layer.color || "#ffffff"), blending: THREE.AdditiveBlending })
  );
  mesh.position.copy(source);
  if (layer.facing === "horizontal") mesh.rotation.x = Math.PI / 2;
  mesh.userData.spin = (layer.rotationSpeed || 0) * Math.PI / 180 / 8;
  view.root.add(mesh);
  addGlowClone(view, mesh);
  view.animated.push(mesh);
  const glyphs = Math.max(0, Math.min(96, Math.round(layer.glyphs || 0)));
  for (let i = 0; i < glyphs; i++) {
    const angle = i / glyphs * Math.PI * 2;
    const marker = new THREE.Mesh(new THREE.BoxGeometry(0.07, 0.015, 0.22), new THREE.MeshStandardMaterial({ color, emissive: color, emissiveIntensity: 0.45, transparent: true, blending: THREE.AdditiveBlending }));
    marker.position.set(source.x + Math.cos(angle) * layer.radius, source.y, source.z + Math.sin(angle) * layer.radius);
    marker.rotation.y = -angle;
    view.root.add(marker);
    addGlowClone(view, marker);
  }
}

function addRadialBurst(root, source, burst) {
  const rays = Math.max(0, Math.min(128, Math.round(burst.rays || 0)));
  for (let i = 0; i < rays; i++) {
    const jitter = (randomSeed(i + 31) - 0.5) * (burst.randomJitter || 0.15);
    const angle = i / Math.max(1, rays) * Math.PI * 2 + jitter;
    const end = source.clone().add(new THREE.Vector3(Math.cos(angle) * (burst.length || 2.8), Math.sin(angle * 1.7) * (burst.length || 2.8) * 0.15, Math.sin(angle) * (burst.length || 2.8)));
    const line = new THREE.Line(new THREE.BufferGeometry().setFromPoints([source, end]), new THREE.LineBasicMaterial({ color: colorNumber(burst.color?.start || burst.colorStart || "#ffffff"), transparent: true, opacity: 0.85, blending: THREE.AdditiveBlending }));
    root.add(line);
    addGlowClone(preview3d, line);
  }
}

function ribbonMaterial(color, blendMode = "additive") {
  return new THREE.MeshBasicMaterial({
    color: colorNumber(color || "#ffffff"),
    transparent: true,
    opacity: alphaValue(color || "#ffffff"),
    side: THREE.DoubleSide,
    depthWrite: false,
    blending: blendMode === "alpha" ? THREE.NormalBlending : THREE.AdditiveBlending
  });
}

function addGlowClone(view, mesh) {
  if (!state.components.advanced?.glow?.enabled) return;
  const sourceMaterial = Array.isArray(mesh.material) ? mesh.material[0] : mesh.material;
  const glowMaterial = new THREE.MeshBasicMaterial({
    color: sourceMaterial?.color?.clone?.() ?? new THREE.Color(0xffffff),
    transparent: true,
    opacity: Math.min(1, (sourceMaterial?.opacity ?? 1) * (state.components.advanced.glow.intensity ?? 1.35)),
    side: THREE.DoubleSide,
    depthWrite: false,
    blending: THREE.AdditiveBlending
  });
  let clone;
  if (mesh.isPoints) {
    glowMaterial.size = sourceMaterial?.size;
    clone = new THREE.Points(mesh.geometry, sourceMaterial.clone());
    clone.material.opacity = Math.min(1, (sourceMaterial.opacity ?? 1) * (state.components.advanced.glow.intensity ?? 1.35));
    clone.material.blending = THREE.AdditiveBlending;
    clone.material.depthWrite = false;
  } else if (mesh.isLine) {
    clone = new THREE.Line(mesh.geometry, sourceMaterial.clone());
    clone.material.opacity = Math.min(1, (sourceMaterial.opacity ?? 1) * (state.components.advanced.glow.intensity ?? 1.35));
    clone.material.blending = THREE.AdditiveBlending;
    clone.material.depthWrite = false;
  } else if (mesh.isSprite) {
    clone = new THREE.Sprite(sourceMaterial.clone());
    clone.material.opacity = Math.min(1, (sourceMaterial.opacity ?? 1) * (state.components.advanced.glow.intensity ?? 1.35));
    clone.material.blending = THREE.AdditiveBlending;
    clone.material.depthWrite = false;
  } else {
    clone = new THREE.Mesh(mesh.geometry, glowMaterial);
  }
  clone.userData.sourceMesh = mesh;
  mesh.userData.glowClone = clone;
  view.glowScene.add(clone);
}

function buildRibbonMesh(points, widthStart, widthEnd, colorStart, colorEnd, cameraPosition) {
  const geometry = new THREE.BufferGeometry();
  if (points.length < 2) return new THREE.Mesh(geometry, ribbonMaterial(colorStart));
  const positions = [];
  const colors = [];
  const uvs = [];
  let distance = 0;
  const colorA = new THREE.Color(colorNumber(colorStart));
  const colorB = new THREE.Color(colorNumber(colorEnd));
  const sideVectors = points.map((_, index) => ribbonSide(points, index, cameraPosition));
  for (let i = 0; i < points.length - 1; i++) {
    const t0 = i / Math.max(1, points.length - 1);
    const t1 = (i + 1) / Math.max(1, points.length - 1);
    const p0 = points[i];
    const p1 = points[i + 1];
    const nextDistance = distance + p0.distanceTo(p1);
    const half0 = lerp(widthStart, widthEnd, t0) * 0.5;
    const half1 = lerp(widthStart, widthEnd, t1) * 0.5;
    const left0 = p0.clone().addScaledVector(sideVectors[i], -half0);
    const right0 = p0.clone().addScaledVector(sideVectors[i], half0);
    const left1 = p1.clone().addScaledVector(sideVectors[i + 1], -half1);
    const right1 = p1.clone().addScaledVector(sideVectors[i + 1], half1);
    pushRibbonVertex(positions, colors, uvs, left0, colorA, colorB, t0, 0, distance);
    pushRibbonVertex(positions, colors, uvs, right0, colorA, colorB, t0, 1, distance);
    pushRibbonVertex(positions, colors, uvs, right1, colorA, colorB, t1, 1, nextDistance);
    pushRibbonVertex(positions, colors, uvs, left0, colorA, colorB, t0, 0, distance);
    pushRibbonVertex(positions, colors, uvs, right1, colorA, colorB, t1, 1, nextDistance);
    pushRibbonVertex(positions, colors, uvs, left1, colorA, colorB, t1, 0, nextDistance);
    distance = nextDistance;
  }
  geometry.setAttribute("position", new THREE.Float32BufferAttribute(positions, 3));
  geometry.setAttribute("color", new THREE.Float32BufferAttribute(colors, 3));
  geometry.setAttribute("uv", new THREE.Float32BufferAttribute(uvs, 2));
  geometry.computeBoundingSphere();
  const mesh = new THREE.Mesh(geometry, ribbonMaterial(colorStart));
  mesh.material.vertexColors = true;
  return mesh;
}

function pushRibbonVertex(positions, colors, uvs, point, colorA, colorB, t, u, v) {
  const color = colorA.clone().lerp(colorB, t);
  positions.push(point.x, point.y, point.z);
  colors.push(color.r, color.g, color.b);
  uvs.push(u, v);
}

function ribbonSide(points, index, cameraPosition) {
  const prev = points[Math.max(0, index - 1)];
  const next = points[Math.min(points.length - 1, index + 1)];
  const direction = next.clone().sub(prev).normalize();
  const reference = cameraPosition.clone().sub(points[index]).normalize();
  const side = direction.clone().cross(reference);
  if (side.lengthSq() < 1e-6) return new THREE.Vector3(1, 0, 0);
  return side.normalize();
}

function updatePreviewDynamics(view) {
  const source = new THREE.Vector3(0, state.preview.sourceHeightOffset || 0, 0);
  updateTrailHistory(view, source);
  view.root.traverse((node) => {
    if (node.userData.dynamicTrail) updateTrailMesh(node, view.camera.position);
    if (node.userData.ribbonBundle) updateRibbonBundleMesh(node, view.tick, view.camera.position);
    if (node.userData.glowClone) syncGlowClone(node);
  });
  view.glowScene.traverse((node) => {
    if (node.userData.sourceMesh) {
      node.geometry = node.userData.sourceMesh.geometry;
      node.position.copy(node.userData.sourceMesh.position);
      node.rotation.copy(node.userData.sourceMesh.rotation);
      node.scale.copy(node.userData.sourceMesh.scale);
    }
  });
}

function updateTrailHistory(view, source) {
  if (!state.components.trail.enabled) return;
  const trail = state.components.trail;
  const current = source.clone().add(vectorFromOffset(motionOffset(trail.motion, view.tick)));
  const last = view.trailHistory.at(-1);
  if (!last || trail.sampleEveryTick || last.position.distanceTo(current) >= (trail.minSampleDistance || 0.04)) {
    view.trailHistory.push({ position: current, age: 0 });
  }
  for (const point of view.trailHistory) point.age += 1;
  const lifetime = Math.max(1, trail.lifetimeTicks || 36);
  view.trailHistory = view.trailHistory.filter((point) => point.age <= lifetime).slice(-(trail.maxPoints || 64));
}

function updateTrailMesh(mesh, cameraPosition) {
  const trail = mesh.userData.trail;
  const points = preview3d.trailHistory.map((point) => point.position);
  const updated = buildRibbonMesh(points, trail.width?.start || 0.1, trail.width?.end || 0.0, trail.color?.start || "#ffffff", trail.color?.end || "#ffffff", cameraPosition);
  mesh.geometry.dispose();
  mesh.geometry = updated.geometry;
  mesh.material.vertexColors = true;
}

function updateRibbonBundleMesh(mesh, tick, cameraPosition) {
  const { source, target, bundle, line } = mesh.userData.ribbonBundle;
  const points = ribbonBundlePoints(source, target, bundle, line, tick);
  const updated = buildRibbonMesh(points, bundle.width?.start || bundle.widthStart || 0.1, bundle.width?.end || bundle.widthEnd || 0.02, bundle.color?.start || bundle.colorStart || "#ffffff", bundle.color?.end || bundle.colorEnd || "#ffffff", cameraPosition);
  mesh.geometry.dispose();
  mesh.geometry = updated.geometry;
  mesh.material.vertexColors = true;
}

function syncGlowClone(mesh) {
  const clone = mesh.userData.glowClone;
  clone.geometry = mesh.geometry;
  clone.position.copy(mesh.position);
  clone.rotation.copy(mesh.rotation);
  clone.scale.copy(mesh.scale);
}

function vectorFromOffset(offset) {
  return new THREE.Vector3(offset.x || 0, offset.y || 0, offset.z || 0);
}

function lerp(a, b, t) {
  return a + (b - a) * t;
}

function randomSeed(value) {
  const x = Math.sin(value * 12.9898) * 43758.5453;
  return x - Math.floor(x);
}

function buildTrailPoints(source) {
  const samples = Math.max(16, Math.min(160, Number(state.components.trail.maxPoints || 72)));
  const points = [];
  for (let i = 0; i < samples; i++) {
    const offset = motionOffset(state.components.trail.motion, i);
    points.push(new THREE.Vector3(source.x + offset.x, source.y + offset.y, source.z + offset.z));
  }
  return points;
}

function motionOffset(motion, tick) {
  const time = tick / 20;
  const radius = Number(motion.radius || 0);
  const angularSpeed = Number(motion.angularSpeed || 0);
  const verticalAmplitude = Number(motion.verticalAmplitude || 0);
  const verticalSpeed = Number(motion.verticalSpeed || 0);
  const phase = Number(motion.phase || 0);
  const angleDegrees = phase + tick * angularSpeed;
  const verticalAngleDegrees = phase + tick * verticalSpeed;
  const angle = angleDegrees * Math.PI / 180;
  const verticalAngle = verticalAngleDegrees * Math.PI / 180;
  if (motion.mode === "orbit") return { x: Math.cos(angle) * radius, y: 0, z: Math.sin(angle) * radius };
  if (motion.mode === "helix" || motion.mode === "spiral") return { x: Math.cos(angle) * radius, y: Math.sin(verticalAngle) * verticalAmplitude, z: Math.sin(angle) * radius };
  if (motion.mode === "formula") {
    const vars = { tick, time, radius, angularSpeed, verticalAmplitude, verticalSpeed, phase, angle, angleDegrees, verticalAngle };
    return {
      x: evaluateFormula(motion.formula?.x, vars, Math.cos(angle) * radius),
      y: evaluateFormula(motion.formula?.y, vars, Math.sin(verticalAngle) * verticalAmplitude),
      z: evaluateFormula(motion.formula?.z, vars, Math.sin(angle) * radius)
    };
  }
  return { x: tick * 0.035, y: Math.sin(tick * 0.16) * Math.max(0.1, radius * 0.2), z: 0 };
}

function evaluateFormula(expression, vars, fallback) {
  if (!expression || !String(expression).trim()) return fallback;
  try {
    const names = Object.keys(vars);
    const values = Object.values(vars);
    const fn = new Function("Math", ...names, `"use strict"; return (${expression});`);
    const value = Number(fn(Math, ...values));
    return Number.isFinite(value) ? value : fallback;
  } catch {
    return fallback;
  }
}

function addTubeLikeMarkers(root, points, radius, color) {
  const step = Math.max(1, Math.floor(points.length / 28));
  const material = new THREE.MeshStandardMaterial({ color, emissive: color, emissiveIntensity: 0.2, transparent: true, opacity: 0.75 });
  for (let i = 0; i < points.length; i += step) {
    const marker = new THREE.Mesh(new THREE.SphereGeometry(radius, 10, 8), material);
    marker.position.copy(points[i]);
    root.add(marker);
  }
}

function resizePreview3d() {
  const view = preview3d;
  if (!view) return;
  const rect = view.canvas.getBoundingClientRect();
  const width = Math.max(1, Math.floor(rect.width));
  const height = Math.max(1, Math.floor(rect.height));
  if (width === view.lastWidth && height === view.lastHeight) return;
  view.lastWidth = width;
  view.lastHeight = height;
  view.renderer.setSize(width, height, false);
  view.camera.aspect = width / height;
  view.camera.updateProjectionMatrix();
}

function renderPreview3d() {
  if (!preview3d) return;
  resizePreview3d();
  const glow = state?.components?.advanced?.glow;
  if (!glow?.enabled) {
    preview3d.renderer.setRenderTarget(null);
    preview3d.renderer.setClearColor(0x0b0f14, 1);
    preview3d.renderer.clear(true, true, true);
    preview3d.renderer.render(preview3d.scene, preview3d.camera);
    return;
  }
  ensurePreviewGlowTarget(preview3d, glow);
  preview3d.renderer.setRenderTarget(preview3d.glowTarget);
  preview3d.renderer.setClearColor(0x000000, 1);
  preview3d.renderer.clear(true, true, true);
  preview3d.renderer.render(preview3d.glowScene, preview3d.camera);
  preview3d.renderer.setRenderTarget(null);
  preview3d.renderer.setClearColor(0x0b0f14, 1);
  preview3d.renderer.clear(true, true, true);
  preview3d.renderer.render(preview3d.scene, preview3d.camera);
  compositePreviewGlow(preview3d, glow);
}

function ensurePreviewGlowTarget(view, glow) {
  const divisor = Math.max(1, Math.min(8, Math.round(glow.downsample || 2)));
  const width = Math.max(1, Math.floor(view.lastWidth / divisor));
  const height = Math.max(1, Math.floor(view.lastHeight / divisor));
  if (view.glowTarget && view.glowTarget.width === width && view.glowTarget.height === height) return;
  view.glowTarget?.dispose();
  view.glowTarget = new THREE.WebGLRenderTarget(width, height, { depthBuffer: false, stencilBuffer: false });
}

function compositePreviewGlow(view, glow) {
  if (!view.glowQuadScene) {
    view.glowQuadScene = new THREE.Scene();
    view.glowQuadCamera = new THREE.OrthographicCamera(-1, 1, 1, -1, 0, 1);
    view.glowQuadMaterial = new THREE.MeshBasicMaterial({ map: view.glowTarget.texture, transparent: true, depthWrite: false, blending: THREE.AdditiveBlending });
    view.glowQuad = new THREE.Mesh(new THREE.PlaneGeometry(2, 2), view.glowQuadMaterial);
    view.glowQuadScene.add(view.glowQuad);
  }
  view.glowQuadMaterial.map = view.glowTarget.texture;
  const iterations = Math.max(1, Math.min(12, Math.round(glow.iterations || 4)));
  const radius = Math.max(0.25, glow.radius || 1);
  const baseOpacity = Math.min(1, Math.max(0.02, (glow.intensity || 1.35) / iterations));
  for (let pass = 0; pass <= iterations; pass++) {
    const offset = pass === 0 ? 0 : radius * pass / iterations * 0.004;
    const opacity = pass === 0 ? baseOpacity : baseOpacity * (1 - pass / (iterations + 1));
    drawGlowQuad(view, 0, 0, opacity);
    if (pass > 0) {
      drawGlowQuad(view, offset, 0, opacity);
      drawGlowQuad(view, -offset, 0, opacity);
      drawGlowQuad(view, 0, offset, opacity);
      drawGlowQuad(view, 0, -offset, opacity);
      drawGlowQuad(view, offset * 0.707, offset * 0.707, opacity * 0.7);
      drawGlowQuad(view, -offset * 0.707, offset * 0.707, opacity * 0.7);
      drawGlowQuad(view, offset * 0.707, -offset * 0.707, opacity * 0.7);
      drawGlowQuad(view, -offset * 0.707, -offset * 0.707, opacity * 0.7);
    }
  }
}

function drawGlowQuad(view, x, y, opacity) {
  view.glowQuad.position.set(x, y, 0);
  view.glowQuadMaterial.opacity = opacity;
  view.renderer.setRenderTarget(null);
  view.renderer.autoClear = false;
  view.renderer.render(view.glowQuadScene, view.glowQuadCamera);
}

function resetPreviewCamera() {
  initPreview3d();
  preview3d.camera.position.set(6, 4.2, 7);
  preview3d.controls.target.set(1.8, 0.9, 0);
  preview3d.controls.update();
  renderPreview3d();
}

function togglePreviewExpanded() {
  document.body.classList.toggle("preview-expanded");
  applyLanguage();
  setTimeout(() => {
    resizePreview3d();
    renderPreview3d();
  }, 0);
}

function clearGroup(group) {
  while (group.children.length) {
    const child = group.children.pop();
    child.traverse?.((node) => {
      node.geometry?.dispose?.();
      if (Array.isArray(node.material)) node.material.forEach((material) => material.dispose?.());
      else node.material?.dispose?.();
    });
  }
}

function colorNumber(value) {
  const hex = String(value || "#ffffff").trim().replace("#", "");
  return Number.parseInt(hex.slice(0, 6).padEnd(6, "f"), 16) || 0xffffff;
}

function alphaValue(value) {
  const clean = String(value || "").replace("#", "");
  if (clean.length < 8) return 0.9;
  return Math.max(0.05, Math.min(1, Number.parseInt(clean.slice(6, 8), 16) / 255));
}

function render() {
  syncStateFromProject();
  renderProjectTree();
  renderTabs();
  renderForm();
  updateJson();
  drawPreview();
}

async function init() {
  applyStoredLayout();
  setupResizers();
  defaultDraft = await api("/api/draft/default");
  project = createProject(defaultDraft);
  syncStateFromProject();
  applyLanguage();
  await refreshRuntime();
  render();
  $("groupKeyInput").oninput = updateProjectGroupFromInputs;
  $("groupDescriptionInput").oninput = updateProjectGroupFromInputs;
  $("includeEffectsOnGroupExport").onchange = updateJson;
  $("addChildBtn").onclick = () => addChildEffect();
  $("importChildBtn").onclick = () => {
    const id = $("importEffectSelect").value;
    const match = runtimeEffects.find((effect) => effect.id === id);
    if (!match?.config) {
      setStatus(t("noImportEffectSelected"), false);
      return;
    }
    addChildEffect(match.config);
  };
  $("exportGroupBtn").onclick = async () => {
    const result = await api("/api/export/group", projectPayload());
    setStatus(result.message, result.ok);
  };
  $("overwriteGroupBtn").onclick = async () => {
    const result = await api("/api/export/group/overwrite", projectPayload());
    setStatus(result.message, result.ok);
  };
  $("languageSelect").onchange = () => {
    language = $("languageSelect").value;
    localStorage.setItem("magicrender.editor.language", language);
    applyLanguage();
    refreshRuntime();
    render();
  };
  $("validateBtn").onclick = async () => {
    const result = await api("/api/validate", state);
    renderMessages(result);
    setStatus(result.summary, result.ok);
  };
  $("previewBtn").onclick = async () => {
    const result = await api("/api/preview", state);
    setStatus(result.message, result.ok);
  };
  $("stopBtn").onclick = async () => setStatus((await api("/api/preview/stop", state)).message);
  $("exportBtn").onclick = async () => {
    const result = await api("/api/export", state);
    setStatus(result.message, result.ok);
  };
  $("overwriteBtn").onclick = async () => {
    const result = await api("/api/export/overwrite", state);
    setStatus(result.message, result.ok);
  };
  $("reloadBtn").onclick = async () => {
    const result = await api("/api/reload", state);
    setStatus(result.message, result.ok);
    await refreshRuntime();
  };
  $("resetCameraBtn").onclick = resetPreviewCamera;
  $("expandPreviewBtn").onclick = togglePreviewExpanded;
}

init().catch((error) => setStatus(error.message, false));

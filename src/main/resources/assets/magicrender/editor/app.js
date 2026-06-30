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
    previewPage: "Preview",
    basicHint: "Edit effect metadata and shared visibility.",
    trailHint: "Ribbon trail shape, color and sampling.",
    motionHint: "Orbit, helix and custom formula motion layer.",
    circleHint: "Arcane circle attached to the preview source.",
    beamHint: "Beam from source to target point or entity.",
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
    previewPage: "预览",
    basicHint: "编辑特效元数据和通用可见性。",
    trailHint: "设置拖尾轨迹的形状、颜色和采样。",
    motionHint: "设置环绕、螺旋和自定义公式运动。",
    circleHint: "附着在预览源点上的法阵。",
    beamHint: "从源点指向目标点或实体的光束。",
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
    fallbackDistance: "无目标时回退固定距离"
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
    "preview.fallbackToFixedDistance": "If true, preview falls back to fixed distance when no target entity/point is found."
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
    "preview.fallbackToFixedDistance": "启用后，如果找不到实体/命中点，会回退到固定距离预览。"
  }
};

const helpFor = (key) => helpText[language]?.[key] ?? helpText.en[key] ?? "";

const pages = [
  { id: "basic", title: "basic", hint: "basicHint" },
  { id: "trail", title: "trail", hint: "trailHint" },
  { id: "motion", title: "motion", hint: "motionHint" },
  { id: "circle", title: "circle", hint: "circleHint" },
  { id: "beam", title: "beam", hint: "beamHint" },
  { id: "preview", title: "previewPage", hint: "previewHint" }
];

let state = null;
let page = "basic";
let preview3d = null;

const $ = (id) => document.getElementById(id);
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
    body: JSON.stringify(state)
  };
  const response = await fetch(path, options);
  if (!response.ok) throw new Error(await response.text());
  return response.json();
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
      updateJson();
      drawPreview();
    };
    wrap.appendChild(input);
    form.appendChild(wrap);
  }
}

function updateJson() {
  $("jsonView").value = JSON.stringify(state, null, 2);
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

  preview3d = { canvas, scene, camera, renderer, controls, root, animated: [], lastWidth: 0, lastHeight: 0 };
  window.addEventListener("resize", () => {
    resizePreview3d();
    renderPreview3d();
  });
  const animate = () => {
    if (!preview3d) return;
    requestAnimationFrame(animate);
    for (const item of preview3d.animated) item.rotation.y += item.userData.spin ?? 0.01;
    preview3d.controls.update();
    renderPreview3d();
  };
  animate();
}

function rebuildPreview3d() {
  const view = preview3d;
  clearGroup(view.root);
  view.animated = [];
  const source = new THREE.Vector3(0, state.preview.sourceHeightOffset || 0, 0);
  const target = new THREE.Vector3(Math.max(1, state.preview.fixedDistance || 4), state.preview.targetHeightOffset || 0.8, 0);
  addPoint(view.root, source, 0x67d8ff);
  addPoint(view.root, target, 0xffd166);
  if (state.components.beam.enabled) addBeam(view.root, source, target);
  if (state.components.trail.enabled) addTrail(view.root, source);
  if (state.components.magicCircle.enabled) addMagicCircle(view, source);
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

function addTrail(root, source) {
  const trail = state.components.trail;
  const points = buildTrailPoints(source);
  root.add(new THREE.Line(
    new THREE.BufferGeometry().setFromPoints(points),
    new THREE.LineBasicMaterial({ color: colorNumber(trail.color?.start || "#ffffff"), transparent: true, opacity: alphaValue(trail.color?.start || "#ffffff") })
  ));
  addTubeLikeMarkers(root, points, Math.max(0.01, (trail.width?.start || 0.08) * 0.12), colorNumber(trail.color?.end || trail.color?.start || "#ffffff"));
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
  preview3d.renderer.render(preview3d.scene, preview3d.camera);
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
  renderTabs();
  renderForm();
  updateJson();
  drawPreview();
}

async function init() {
  state = await api("/api/draft/default");
  applyLanguage();
  await refreshRuntime();
  render();
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

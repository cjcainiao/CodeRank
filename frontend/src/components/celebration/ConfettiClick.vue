<!-- 主动触发时，两侧花筒喷出沿抛物线飞行的彩纸 -->
<template>
  <Teleport v-if="bursts.length" to="body">
    <div ref="overlay" class="confetti-click" aria-hidden="true">
      <div
        v-for="burst in bursts"
        :key="burst.id"
        class="confetti-click__burst"
        :data-burst-id="burst.id"
      >
        <div class="confetti-click__cannon confetti-click__cannon--left">
          <span class="confetti-click__cannon-art">
            <span class="confetti-click__cannon-body"></span>
            <span class="confetti-click__cannon-band"></span>
            <span class="confetti-click__cannon-rim"></span>
          </span>
          <span class="confetti-click__origin"></span>
        </div>
        <div class="confetti-click__cannon confetti-click__cannon--right">
          <span class="confetti-click__cannon-art">
            <span class="confetti-click__cannon-body"></span>
            <span class="confetti-click__cannon-band"></span>
            <span class="confetti-click__cannon-rim"></span>
          </span>
          <span class="confetti-click__origin"></span>
        </div>
        <span
          v-for="piece in burst.pieces"
          :key="piece.id"
          class="confetti-click__piece"
          :style="piece.style"
        ></span>
      </div>
    </div>
  </Teleport>
</template>

<script setup lang="ts">
import { nextTick, onBeforeUnmount, onMounted, ref, shallowRef, watch } from 'vue'

const DEFAULT_COLORS = ['#f06c9b', '#ffad5c', '#f4d35e', '#54cfa9', '#58b8ee', '#aa86e8']
const GRAVITY_PX_PER_S2 = 960
const MAX_ACTIVE_BURSTS = 2
const TRAJECTORY_STEPS = 20

type Side = 'left' | 'right'

interface ConfettiPiece {
  id: number
  side: Side
  style: Record<string, string>
}

interface ConfettiBurst {
  id: number
  pieces: ConfettiPiece[]
}

const props = withDefaults(
  defineProps<{
    enabled?: boolean
    colors?: string[]
    pieceCount?: number
  }>(),
  {
    enabled: true,
    colors: () => [],
    pieceCount: 40,
  },
)

const overlay = ref<HTMLElement>()
const bursts = shallowRef<ConfettiBurst[]>([])
const removalTimers = new Map<number, ReturnType<typeof setTimeout>>()
const animations = new Map<number, Animation[]>()
let nextBurstId = 0
let motionQuery: MediaQueryList | undefined
let mounted = false

function removeBurst(id: number) {
  const timer = removalTimers.get(id)
  if (timer) clearTimeout(timer)
  removalTimers.delete(id)
  for (const animation of animations.get(id) ?? []) animation.cancel()
  animations.delete(id)
  bursts.value = bursts.value.filter((burst) => burst.id !== id)
}

function clearBursts() {
  for (const timer of removalTimers.values()) clearTimeout(timer)
  for (const group of animations.values()) {
    for (const animation of group) animation.cancel()
  }
  removalTimers.clear()
  animations.clear()
  bursts.value = []
}

function buildTrajectory(
  origin: DOMRect,
  side: Side,
  viewportWidth: number,
  viewportHeight: number,
) {
  const peakHeight = Math.min(viewportHeight * 0.62, 540) * (0.65 + Math.random() * 0.45)
  const initialYVelocity = -Math.sqrt(2 * GRAVITY_PX_PER_S2 * peakHeight)
  const apexTime = -initialYVelocity / GRAVITY_PX_PER_S2
  const horizontalReach = viewportWidth * (0.16 + Math.random() * 0.23)
  const initialXVelocity = (side === 'left' ? 1 : -1) * (horizontalReach / apexTime)
  const startX = origin.left + origin.width / 2
  const startY = origin.top + origin.height / 2
  const landingDistance = Math.max(0, viewportHeight + 32 - startY)
  const flightTime =
    (-initialYVelocity +
      Math.sqrt(initialYVelocity ** 2 + 2 * GRAVITY_PX_PER_S2 * landingDistance)) /
    GRAVITY_PX_PER_S2
  const startRotation = Math.random() * 360
  const spin = (Math.random() < 0.5 ? -1 : 1) * (420 + Math.random() * 480)
  const flutterPhase = Math.random() * Math.PI * 2

  const frames = Array.from({ length: TRAJECTORY_STEPS + 1 }, (_, index) => {
    const progress = index / TRAJECTORY_STEPS
    const seconds = flightTime * progress
    // 水平匀速、竖直匀加速：x = x₀ + vₓt，y = y₀ + vᵧt + ½gt²。
    const x = startX + initialXVelocity * seconds
    const y = startY + initialYVelocity * seconds + 0.5 * GRAVITY_PX_PER_S2 * seconds ** 2
    const opacity = Math.min(1, progress / 0.04, (1 - progress) / 0.2)
    const flutter = Math.sin(seconds * 13 + flutterPhase) * 70

    return {
      offset: progress,
      transform: `translate3d(${x.toFixed(1)}px, ${y.toFixed(1)}px, 0) translate(-50%, -50%) rotate(${(startRotation + spin * seconds).toFixed(1)}deg) rotateY(${flutter.toFixed(1)}deg)`,
      opacity: Math.max(0, opacity),
    }
  })

  return { frames, durationMs: flightTime * 1000 }
}

async function celebrate() {
  if (!mounted || !props.enabled || motionQuery?.matches) return

  const palette = props.colors.length ? props.colors : DEFAULT_COLORS
  const count = Number.isFinite(props.pieceCount)
    ? Math.min(48, Math.max(1, Math.round(props.pieceCount)))
    : 40
  const id = ++nextBurstId
  const pieces: ConfettiPiece[] = []

  for (const side of ['left', 'right'] as const) {
    const colorOffset = Math.floor(Math.random() * palette.length)
    for (let index = 0; index < count; index++) {
      pieces.push({
        id: pieces.length,
        side,
        style: {
          '--confetti-color': palette[(index + colorOffset) % palette.length]!,
          '--confetti-width': `${(4 + Math.random() * 4).toFixed(1)}px`,
          '--confetti-height': `${(8 + Math.random() * 8).toFixed(1)}px`,
        },
      })
    }
  }

  if (bursts.value.length >= MAX_ACTIVE_BURSTS) removeBurst(bursts.value[0]!.id)
  bursts.value = [...bursts.value, { id, pieces }]
  await nextTick()

  const root = overlay.value?.querySelector<HTMLElement>(`[data-burst-id="${id}"]`)
  if (!root || !mounted || !props.enabled || motionQuery?.matches) {
    removeBurst(id)
    return
  }
  const leftOrigin = root.querySelector<HTMLElement>(
    '.confetti-click__cannon--left .confetti-click__origin',
  )
  const rightOrigin = root.querySelector<HTMLElement>(
    '.confetti-click__cannon--right .confetti-click__origin',
  )
  if (!leftOrigin || !rightOrigin) {
    removeBurst(id)
    return
  }

  const origins = {
    left: leftOrigin.getBoundingClientRect(),
    right: rightOrigin.getBoundingClientRect(),
  }
  const viewportWidth = window.innerWidth
  const viewportHeight = window.innerHeight
  const running: Animation[] = []
  let lastFinish = 0

  root.querySelectorAll<HTMLElement>('.confetti-click__piece').forEach((element, index) => {
    const piece = pieces[index]!
    const { frames, durationMs } = buildTrajectory(
      origins[piece.side],
      piece.side,
      viewportWidth,
      viewportHeight,
    )
    const delay = 190 + Math.random() * 70
    const animation = element.animate(frames, {
      duration: durationMs,
      delay,
      easing: 'linear',
      fill: 'both',
    })
    running.push(animation)
    lastFinish = Math.max(lastFinish, durationMs + delay)
  })

  animations.set(id, running)
  removalTimers.set(
    id,
    setTimeout(() => removeBurst(id), lastFinish + 80),
  )
}

function handleMotionChange() {
  if (motionQuery?.matches) clearBursts()
}

defineExpose({ celebrate })

watch(
  () => props.enabled,
  (enabled) => {
    if (!enabled) clearBursts()
  },
)

onMounted(() => {
  mounted = true
  motionQuery = window.matchMedia('(prefers-reduced-motion: reduce)')
  motionQuery.addEventListener('change', handleMotionChange)
})

onBeforeUnmount(() => {
  mounted = false
  motionQuery?.removeEventListener('change', handleMotionChange)
  clearBursts()
})
</script>

<style scoped>
.confetti-click {
  position: fixed;
  /* 高于 Element Plus 弹窗与遮罩：其基础为 2000 且随弹窗叠加递增，
     取 9999 保证礼花始终浮在最上层，否则会被评测结果弹窗盖住 */
  z-index: 9999;
  inset: 0;
  overflow: hidden;
  pointer-events: none;
  user-select: none;
  contain: layout style paint;
}

.confetti-click__burst {
  position: absolute;
  inset: 0;
}

.confetti-click__cannon {
  position: absolute;
  bottom: calc(10px + env(safe-area-inset-bottom, 0px));
  width: 54px;
  height: 90px;
  transform: rotate(var(--cannon-angle));
  transform-origin: 50% 100%;
}

.confetti-click__cannon--left {
  left: calc(14px + env(safe-area-inset-left, 0px));
  --cannon-angle: 35deg;
}

.confetti-click__cannon--right {
  right: calc(14px + env(safe-area-inset-right, 0px));
  --cannon-angle: -35deg;
}

.confetti-click__cannon-art {
  position: relative;
  display: block;
  width: 100%;
  height: 100%;
  animation: confetti-cannon-pop 1500ms ease-out both;
}

.confetti-click__cannon-body {
  position: absolute;
  inset: 0;
  clip-path: polygon(0 0, 100% 0, 64% 100%, 36% 100%);
  background:
    linear-gradient(90deg, rgb(255 255 255 / 60%), transparent 30% 68%, rgb(67 38 78 / 25%)),
    repeating-linear-gradient(
      145deg,
      #f07fa7 0 13px,
      #ffd279 13px 26px,
      #6acbd0 26px 39px,
      #ae9be3 39px 52px
    );
}

.confetti-click__cannon-body::after {
  position: absolute;
  inset: 0;
  background:
    radial-gradient(circle at 31% 21%, #fff5d2 0 2px, transparent 2.5px),
    radial-gradient(circle at 65% 43%, #fff5d2 0 1.5px, transparent 2px),
    radial-gradient(circle at 43% 68%, #fff5d2 0 1.5px, transparent 2px);
  content: '';
}

.confetti-click__cannon-band {
  position: absolute;
  right: 17px;
  bottom: 0;
  left: 17px;
  height: 12px;
  border: 1px solid #bc855a;
  border-radius: 2px 2px 5px 5px;
  background: linear-gradient(90deg, #fff4ce, #e7ad65 65%, #bc784b);
  box-shadow: 0 2px 4px rgb(77 39 54 / 20%);
}

.confetti-click__cannon-rim {
  position: absolute;
  top: -5px;
  left: -1px;
  width: 56px;
  height: 14px;
  border: 3px solid #ffe3a5;
  border-radius: 50%;
  background: radial-gradient(ellipse at center, #713b61 0 46%, #ad6280 72%, #e49a9b);
  box-shadow:
    inset 0 1px 3px rgb(41 21 48 / 40%),
    0 2px 3px rgb(89 49 69 / 22%);
}

.confetti-click__origin {
  position: absolute;
  top: 2px;
  left: 50%;
  width: 2px;
  height: 2px;
  transform: translate(-50%, -50%);
}

.confetti-click__piece {
  position: absolute;
  top: 0;
  left: 0;
  width: var(--confetti-width);
  height: var(--confetti-height);
  border-radius: 1px;
  background: var(--confetti-color);
  opacity: 0;
  backface-visibility: visible;
  will-change: transform, opacity;
}

@keyframes confetti-cannon-pop {
  0% {
    transform: translateY(52px) scale(0.75);
    opacity: 0;
  }
  13% {
    transform: translateY(0) scale(1);
    opacity: 1;
  }
  22% {
    transform: translateY(9px) scale(0.96);
  }
  35%,
  72% {
    transform: translateY(0) scale(1);
    opacity: 1;
  }
  100% {
    transform: translateY(22px) scale(0.9);
    opacity: 0;
  }
}

@media (prefers-reduced-motion: reduce) {
  .confetti-click {
    display: none;
  }
}
</style>

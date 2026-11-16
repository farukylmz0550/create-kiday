package com.farukylmz.createkiday.entity;

import com.farukylmz.createkiday.bracelet.BraceletFlightService;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** Server-authoritative hunting flight based on Create: Luvi's Hunter boomerang. */
public final class HunterDiskEntity extends Entity {
    public static final int FALLBACK_FLIGHT_TICKS = 60;
    public static final int MAX_OUTBOUND_TICKS = 160;
    public static final int MAX_TOTAL_FLIGHT_TICKS = 220;
    public static final int MAX_STAGNANT_TICKS = 10;
    public static final int MAX_HUNTER_HITS = 5;
    public static final double HUNTER_LAUNCH_DISTANCE = 4.0;
    public static final double HUNTER_RANGE = 16.0;
    public static final double MAX_TURN_RADIANS = Math.toRadians(12.0);
    private static final double MAX_MOVEMENT_PER_TICK = 1.35;

    private static final EntityDataAccessor<Optional<UUID>> OWNER_ID = SynchedEntityData.defineId(
            HunterDiskEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Optional<UUID>> BRACELET_ID = SynchedEntityData.defineId(
            HunterDiskEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Optional<UUID>> FLIGHT_ID = SynchedEntityData.defineId(
            HunterDiskEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Boolean> RETURNING = SynchedEntityData.defineId(
            HunterDiskEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> HITS = SynchedEntityData.defineId(
            HunterDiskEntity.class, EntityDataSerializers.INT);

    private Vec3 origin = Vec3.ZERO;
    private Vec3 forward = new Vec3(0, 0, 1);
    private Vec3 right = new Vec3(1, 0, 0);
    private Vec3 localUp = new Vec3(0, 1, 0);
    private Vec3 steering = Vec3.ZERO;
    private UUID navigationTargetId;
    private Vec3 navigationTarget;
    private final Set<UUID> hunterHitSet = new HashSet<>();
    private final Set<UUID> outgoingHits = new HashSet<>();
    private final Set<UUID> returningHits = new HashSet<>();
    private boolean hunterFallback;
    private int flightTick;
    private int totalFlightTicks;
    private int stagnantTicks;
    private int clientLerpSteps;
    private double clientLerpX;
    private double clientLerpY;
    private double clientLerpZ;
    private float clientLerpYRot;
    private float clientLerpXRot;

    public HunterDiskEntity(EntityType<? extends HunterDiskEntity> type, Level level) {
        super(type, level);
        noPhysics = true;
        setNoGravity(true);
    }

    public void launch(ServerPlayer owner, UUID braceletId, UUID flightId) {
        entityData.set(OWNER_ID, Optional.of(owner.getUUID()));
        entityData.set(BRACELET_ID, Optional.of(braceletId));
        entityData.set(FLIGHT_ID, Optional.of(flightId));
        origin = owner.getEyePosition().add(0, -0.25, 0);
        FlightBasis basis = createBasis(owner.getLookAngle(), owner.getYRot());
        forward = basis.forward();
        right = basis.right();
        localUp = basis.up();
        setPos(origin.x, origin.y, origin.z);
        setDeltaMovement(forward.scale(1.25));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(OWNER_ID, Optional.empty());
        builder.define(BRACELET_ID, Optional.empty());
        builder.define(FLIGHT_ID, Optional.empty());
        builder.define(RETURNING, false);
        builder.define(HITS, 0);
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide()) {
            tickClientInterpolation();
            return;
        }

        ServerPlayer owner = owner();
        if (owner == null || !owner.isAlive() || owner.level() != level()) {
            finishFlight(null);
            return;
        }
        totalFlightTicks++;
        if (totalFlightTicks > MAX_TOTAL_FLIGHT_TICKS) {
            finishFlight(owner);
            return;
        }
        if (totalFlightTicks > MAX_OUTBOUND_TICKS && !isReturningPhase()) forceReturn();

        ServerLevel serverLevel = (ServerLevel) level();
        Vec3 previous = position();
        Vec3 desired;
        if (!entityData.get(RETURNING) && !hunterFallback) desired = hunterDesired(serverLevel, owner);
        else if (entityData.get(RETURNING)) desired = directReturnDesired(owner);
        else desired = fallbackEllipseDesired(owner);
        navigationTarget = desired;

        Vec3 routed = steerAroundObstacle(previous, desired);
        if (routed == null) {
            recordStagnation(owner);
            return;
        }
        // The targetless tour already follows a continuous ellipse. Following
        // its server sample directly prevents turn limiting from cutting the
        // curve inward and making the disk appear to reverse too early.
        LivingEntity trackedTarget = navigationTargetId == null ? null : findLiving(serverLevel, navigationTargetId);
        double turnLimit = trackedTarget != null && isSteepDescent(position(), trackedTarget.getBoundingBox().getCenter())
                ? Math.toRadians(22.0) : MAX_TURN_RADIANS;
        Vec3 moved = hunterFallback && !entityData.get(RETURNING)
                ? routed
                : smoothMovement(previous, routed, getDeltaMovement(), turnLimit);
        // A turn-limited point can cut inside an obstacle even though the routed
        // point was safe. In that rare case prefer the safe avoidance sample.
        if (!pathClear(previous, moved)) moved = routed;

        Vec3 movement = moved.subtract(previous);
        setDeltaMovement(movement);
        setPos(moved.x, moved.y, moved.z);
        hasImpulse = true;
        if (movement.lengthSqr() > 1.0E-6) {
            setYRot((float) (Mth.atan2(movement.z, movement.x) * Mth.RAD_TO_DEG) - 90.0F);
        }
        scanHits(serverLevel, owner, previous, moved);
        flightTick++;

        if (movement.lengthSqr() < 1.0E-6) recordStagnation(owner);
        else stagnantTicks = 0;
        Vec3 ownerDelta = owner.getEyePosition().add(0, -0.25, 0).subtract(moved);
        // The second half of the targetless ellipse is a return-facing hit
        // phase, but it is still part of the tour. Only deliver after the full
        // ellipse has switched the disk into its real owner-return state.
        if (!isRemoved() && entityData.get(RETURNING) && ownerDelta.lengthSqr() < 1.8) {
            finishFlight(owner);
        }
    }

    /**
     * Movement is sampled by the server every tick. Consume each sample in one
     * client tick; spreading it over two or three ticks accumulates visual lag,
     * making the disk appear to stutter and disappear before reaching its owner.
     * Normal render partial-tick interpolation still smooths the rendered frames.
     */
    @Override
    public void lerpTo(double x, double y, double z, float yRot, float xRot, int steps) {
        if (!level().isClientSide()) {
            super.lerpTo(x, y, z, yRot, xRot, steps);
            return;
        }
        clientLerpX = x;
        clientLerpY = y;
        clientLerpZ = z;
        clientLerpYRot = yRot;
        clientLerpXRot = xRot;
        clientLerpSteps = clientInterpolationSteps(steps);
    }

    @Override
    public double lerpTargetX() {
        return clientLerpSteps > 0 ? clientLerpX : getX();
    }

    @Override
    public double lerpTargetY() {
        return clientLerpSteps > 0 ? clientLerpY : getY();
    }

    @Override
    public double lerpTargetZ() {
        return clientLerpSteps > 0 ? clientLerpZ : getZ();
    }

    @Override
    public float lerpTargetYRot() {
        return clientLerpSteps > 0 ? clientLerpYRot : getYRot();
    }

    @Override
    public float lerpTargetXRot() {
        return clientLerpSteps > 0 ? clientLerpXRot : getXRot();
    }

    private void tickClientInterpolation() {
        if (clientLerpSteps <= 0) return;
        Vec3 next = interpolationStep(position(), new Vec3(clientLerpX, clientLerpY, clientLerpZ), clientLerpSteps);
        setPos(next.x, next.y, next.z);
        setRot(Mth.rotLerp(1.0F / clientLerpSteps, getYRot(), clientLerpYRot),
                Mth.lerp(1.0F / clientLerpSteps, getXRot(), clientLerpXRot));
        clientLerpSteps--;
    }

    public static Vec3 interpolationStep(Vec3 current, Vec3 target, int remainingSteps) {
        if (remainingSteps <= 0) return current;
        return current.add(target.subtract(current).scale(1.0 / remainingSteps));
    }

    public static int clientInterpolationSteps(int ignoredRequestedSteps) {
        return 1;
    }

    /** Caps heading change without changing the routed step's intended distance. */
    public static Vec3 smoothMovement(Vec3 from, Vec3 routed, Vec3 currentVelocity, double maxTurnRadians) {
        Vec3 desiredDelta = routed.subtract(from);
        if (desiredDelta.lengthSqr() < 1.0E-8) return routed;
        double distance = Math.min(desiredDelta.length(), MAX_MOVEMENT_PER_TICK);
        Vec3 direction = turnToward(currentVelocity, desiredDelta, maxTurnRadians);
        return from.add(direction.scale(distance));
    }

    public static Vec3 turnToward(Vec3 currentVelocity, Vec3 desiredDelta, double maxTurnRadians) {
        Vec3 desiredDirection = desiredDelta.normalize();
        if (currentVelocity.lengthSqr() < 1.0E-8) return desiredDirection;
        Vec3 currentDirection = currentVelocity.normalize();
        double dot = Mth.clamp(currentDirection.dot(desiredDirection), -1.0, 1.0);
        double angle = Math.acos(dot);
        if (angle <= maxTurnRadians || angle < 1.0E-6) return desiredDirection;
        double blend = maxTurnRadians / angle;
        Vec3 turned = currentDirection.scale(1.0 - blend).add(desiredDirection.scale(blend));
        return turned.lengthSqr() < 1.0E-8 ? desiredDirection : turned.normalize();
    }

    private Vec3 hunterDesired(ServerLevel level, ServerPlayer owner) {
        LivingEntity target = navigationTargetId == null ? null : findLiving(level, navigationTargetId);
        if (!isValidHunterTarget(level, target)) {
            navigationTargetId = null;
            target = selectHunterTarget(level);
            navigationTargetId = target == null ? null : target.getUUID();
        }
        if (target == null) {
            beginHunterFallback();
            return fallbackEllipseDesired(owner);
        }

        Vec3 targetPos = target.getBoundingBox().getCenter();
        double travelled = position().distanceTo(origin);
        boolean nearbyTarget = targetPos.distanceToSqr(origin)
                <= HUNTER_LAUNCH_DISTANCE * HUNTER_LAUNCH_DISTANCE;
        boolean steepDescent = isSteepDescent(position(), targetPos);
        if (!nearbyTarget && !steepDescent && travelled < HUNTER_LAUNCH_DISTANCE) {
            double step = Math.min(1.25, HUNTER_LAUNCH_DISTANCE - travelled);
            Vec3 launchPoint = position().add(forward.scale(step));
            if (pathClear(position(), launchPoint)) return launchPoint;
        }
        return hunterApproachStep(position(), targetPos, right);
    }

    /** Banks sideways instead of diving vertically when a target is almost directly below. */
    public static Vec3 hunterApproachStep(Vec3 current, Vec3 target, Vec3 bankRight) {
        Vec3 delta = target.subtract(current);
        if (delta.length() <= 1.35) return target;
        Vec3 approach = delta;
        if (isSteepDescent(current, target)) {
            // Lead to a side waypoint that moves back toward the target as the
            // disk descends. A fixed offset makes a turn-limited disk orbit the
            // target instead of completing the approach.
            double bankDistance = Mth.clamp((-delta.y - 1.0) * 0.32, 0.0, 2.0);
            Vec3 horizontalBank = new Vec3(bankRight.x, 0, bankRight.z);
            if (horizontalBank.lengthSqr() < 1.0E-6) horizontalBank = new Vec3(1, 0, 0);
            approach = target.add(horizontalBank.normalize().scale(bankDistance)).subtract(current);
        }
        // Slow progressively as the target gets closer so the 12-degree turn
        // limiter converges instead of producing a constant-radius orbit.
        double speed = Mth.clamp(delta.length() * 0.12, 0.3, 1.15);
        return current.add(approach.normalize().scale(speed));
    }

    private static boolean isSteepDescent(Vec3 current, Vec3 target) {
        Vec3 delta = target.subtract(current);
        double horizontal = Math.sqrt(delta.x * delta.x + delta.z * delta.z);
        return delta.y < -1.0 && horizontal < Math.max(1.25, -delta.y * 0.45);
    }

    private LivingEntity selectHunterTarget(ServerLevel level) {
        if (entityData.get(HITS) >= MAX_HUNTER_HITS) return null;
        return level.getEntitiesOfClass(LivingEntity.class, new AABB(origin, origin).inflate(HUNTER_RANGE),
                        living -> living instanceof Enemy && isValidHunterTarget(level, living))
                .stream()
                .min(Comparator.comparingDouble(living -> living.position().distanceToSqr(position())))
                .orElse(null);
    }

    private boolean isValidHunterTarget(ServerLevel level, LivingEntity target) {
        return target != null && target.isAlive() && !target.isRemoved() && target.level() == level
                && level.isLoaded(target.blockPosition())
                && target.position().distanceToSqr(origin) <= HUNTER_RANGE * HUNTER_RANGE
                && !hunterHitSet.contains(target.getUUID());
    }

    private Vec3 fallbackEllipseDesired(ServerPlayer owner) {
        double progress = Math.min(1.0, (flightTick + 1.0) / FALLBACK_FLIGHT_TICKS);
        Vec3 target = ellipsePoint(origin, forward, right, owner.getEyePosition().add(0, -0.25, 0), progress);
        if (progress >= 1.0) forceReturn();
        return target;
    }

    public static FlightBasis createBasis(Vec3 look, float yawDegrees) {
        Vec3 basisForward = look.normalize();
        if (Math.abs(basisForward.y) > 0.85) {
            double yaw = Math.toRadians(yawDegrees);
            Vec3 horizontalForward = new Vec3(-Mth.sin((float) yaw), 0, Mth.cos((float) yaw));
            double vertical = Mth.clamp(basisForward.y, -0.7, 0.7);
            basisForward = horizontalForward.scale(Math.sqrt(1.0 - vertical * vertical))
                    .add(0, vertical, 0).normalize();
        }
        Vec3 basisRight = basisForward.cross(new Vec3(0, 1, 0));
        if (basisRight.lengthSqr() < 1.0E-6) {
            double yaw = Math.toRadians(yawDegrees);
            basisRight = new Vec3(Math.cos(yaw), 0, Math.sin(yaw));
        }
        basisRight = basisRight.normalize();
        return new FlightBasis(basisForward, basisRight, basisRight.cross(basisForward).normalize());
    }

    public static Vec3 ellipsePoint(Vec3 origin, Vec3 forward, Vec3 right, Vec3 ownerTarget, double progress) {
        double clamped = Mth.clamp(progress, 0.0, 1.0);
        double angle = Math.PI * 2.0 * clamped;
        Vec3 point = origin.add(forward.scale(10.0 * (1.0 - Math.cos(angle))))
                .add(right.scale(2.0 * Math.sin(angle)));
        if (clamped > 0.5) {
            double alpha = (clamped - 0.5) * 2.0;
            alpha = alpha * alpha * (3.0 - 2.0 * alpha);
            point = point.add(ownerTarget.subtract(origin).scale(alpha));
        }
        return point;
    }

    public record FlightBasis(Vec3 forward, Vec3 right, Vec3 up) {
    }

    private Vec3 directReturnDesired(ServerPlayer owner) {
        Vec3 target = owner.getEyePosition().add(0, -0.25, 0);
        Vec3 delta = target.subtract(position());
        return delta.length() <= 1.25 ? target : position().add(delta.normalize().scale(1.25));
    }

    private void beginHunterFallback() {
        navigationTargetId = null;
        hunterFallback = true;
        origin = position();
        flightTick = 0;
        steering = Vec3.ZERO;
        Vec3 firstArcPoint = ellipsePoint(origin, forward, right, origin, 1.0 / FALLBACK_FLIGHT_TICKS);
        Vec3 firstArcStep = firstArcPoint.subtract(origin);
        if (firstArcStep.lengthSqr() > 1.0E-8) setDeltaMovement(firstArcStep.normalize().scale(1.25));
    }

    private void forceReturn() {
        navigationTargetId = null;
        entityData.set(RETURNING, true);
        stagnantTicks = 0;
        steering = Vec3.ZERO;
    }

    private void recordStagnation(ServerPlayer owner) {
        stagnantTicks++;
        if (stagnantTicks < MAX_STAGNANT_TICKS) return;
        if (entityData.get(RETURNING)) finishFlight(owner);
        else forceReturn();
    }

    private Vec3 steerAroundObstacle(Vec3 from, Vec3 desired) {
        // Drop any previous avoidance bias as soon as the direct homing path is
        // open. Keeping a stale sideways offset after clearing an obstacle can
        // make the disk orbit a nearby target instead of closing the last few
        // blocks to hit it.
        if (pathClear(from, desired)) {
            steering = Vec3.ZERO;
            return desired;
        }

        steering = steering.scale(0.65);

        FlightBasis movementBasis = movementBasis(from, desired);
        List<Vec3> directions = avoidanceDirections(movementBasis);
        Vec3 best = null;
        double bestScore = Double.MAX_VALUE;
        for (double ring : new double[]{0.75, 1.5}) {
            for (Vec3 direction : directions) {
                Vec3 offset = direction.scale(ring);
                Vec3 candidate = desired.add(offset);
                if (!pathClear(from, candidate)) continue;
                double score = candidate.distanceToSqr(desired)
                        + Math.max(0, from.distanceTo(desired) - candidate.distanceTo(desired));
                if (score < bestScore) {
                    bestScore = score;
                    best = candidate;
                    steering = steering.scale(0.45).add(offset.scale(0.55));
                }
            }
            if (best != null) break;
        }
        if (best != null) return best;

        List<Vec3> escapeDirections = escapeDirections(movementBasis);
        for (double step : new double[]{0.2, 0.45, 0.8, 1.15}) {
            for (Vec3 direction : escapeDirections) {
                Vec3 candidate = from.add(direction.scale(step));
                if (!pathClear(from, candidate)) continue;
                double score = candidate.distanceToSqr(desired);
                if (score < bestScore) {
                    bestScore = score;
                    best = candidate;
                }
            }
            if (best != null) break;
        }
        if (best != null) steering = steering.scale(0.25).add(best.subtract(from).scale(0.75));
        return best;
    }

    private static List<Vec3> escapeDirections(FlightBasis basis) {
        Vec3 backward = basis.forward().scale(-1);
        Vec3 basisRight = basis.right();
        Vec3 up = basis.up();
        return List.of(basisRight, basisRight.scale(-1), up, up.scale(-1),
                basisRight.add(up).normalize(), basisRight.subtract(up).normalize(),
                up.subtract(basisRight).normalize(), basisRight.add(up).scale(-1).normalize(),
                backward, backward.add(basisRight).normalize(), backward.subtract(basisRight).normalize(),
                backward.add(up).normalize(), backward.subtract(up).normalize());
    }

    private FlightBasis movementBasis(Vec3 from, Vec3 target) {
        Vec3 heading = target.subtract(from);
        if (heading.lengthSqr() < 1.0E-6) return new FlightBasis(forward, right, localUp);
        heading = heading.normalize();
        Vec3 movementRight = heading.cross(new Vec3(0, 1, 0));
        if (movementRight.lengthSqr() < 1.0E-6) movementRight = right;
        movementRight = movementRight.normalize();
        return new FlightBasis(heading, movementRight, movementRight.cross(heading).normalize());
    }

    public static List<Vec3> avoidanceDirections(FlightBasis basis) {
        Vec3 basisRight = basis.right();
        Vec3 up = basis.up();
        return List.of(basisRight, basisRight.scale(-1), up, up.scale(-1),
                basisRight.add(up).normalize(), basisRight.subtract(up).normalize(),
                up.subtract(basisRight).normalize(), basisRight.add(up).scale(-1).normalize());
    }

    private boolean pathClear(Vec3 from, Vec3 to) {
        HitResult hit = level().clip(new ClipContext(from, to, ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE, this));
        if (hit.getType() != HitResult.Type.MISS) return false;
        Vec3 delta = to.subtract(from);
        for (int i = 1; i <= 4; i++) {
            Vec3 sample = from.add(delta.scale(i / 4.0));
            AABB box = getBoundingBox().move(sample.subtract(position())).inflate(0.04);
            if (!level().noBlockCollision(this, box)) return false;
        }
        return true;
    }

    private void scanHits(ServerLevel level, ServerPlayer owner, Vec3 from, Vec3 to) {
        AABB sweep = new AABB(from, to).inflate(1.15);
        Set<UUID> phaseHits = isReturningPhase() ? returningHits : outgoingHits;
        for (LivingEntity living : level.getEntitiesOfClass(LivingEntity.class, sweep,
                living -> living.isAlive() && living != owner)) {
            // Use a swept, forgiving contact volume. The disk is fast enough
            // to pass between entity samples, and a narrow ray made valid
            // visual contacts intermittently fail to deal damage.
            AABB contactBox = living.getBoundingBox().inflate(1.0);
            if (contactBox.clip(from, to).isEmpty() && !contactBox.contains(from)) continue;
            if (!hunterFallback) {
                if (!(living instanceof Enemy) || hunterHitSet.contains(living.getUUID())) continue;
                if (navigationTargetId != null && !navigationTargetId.equals(living.getUUID())) continue;
                living.invulnerableTime = 0;
                if (living.hurt(level.damageSources().thrown(this, owner), 4.0F)) {
                    hunterHitSet.add(living.getUUID());
                    entityData.set(HITS, entityData.get(HITS) + 1);
                    navigationTargetId = null;
                    if (entityData.get(HITS) >= MAX_HUNTER_HITS) {
                        forceReturn();
                        break;
                    }
                }
            } else if (phaseHits.add(living.getUUID())) {
                living.hurt(level.damageSources().thrown(this, owner), 5.0F);
            }
        }
    }

    private boolean isReturningPhase() {
        return entityData.get(RETURNING) || hunterFallback && flightTick >= FALLBACK_FLIGHT_TICKS / 2;
    }

    private static LivingEntity findLiving(ServerLevel level, UUID id) {
        Entity entity = level.getEntity(id);
        return entity instanceof LivingEntity living ? living : null;
    }

    private ServerPlayer owner() {
        if (!(level() instanceof ServerLevel serverLevel)) return null;
        return entityData.get(OWNER_ID).map(serverLevel::getEntity)
                .filter(ServerPlayer.class::isInstance).map(ServerPlayer.class::cast).orElse(null);
    }

    private void finishFlight(ServerPlayer owner) {
        UUID ownerId = entityData.get(OWNER_ID).orElse(null);
        UUID braceletId = entityData.get(BRACELET_ID).orElse(null);
        UUID flightId = entityData.get(FLIGHT_ID).orElse(null);
        if (owner != null && braceletId != null && flightId != null) {
            if (!BraceletFlightService.acceptReturn(owner, braceletId, flightId)) clearFlightLock();
        } else if (ownerId != null && braceletId != null && flightId != null) {
            BraceletFlightService.FLIGHTS.complete(ownerId, braceletId, flightId);
        }
        discard();
    }

    public UUID navigationTargetId() {
        return navigationTargetId;
    }

    public Vec3 navigationTarget() {
        return navigationTarget;
    }

    public int hunterHits() {
        return entityData.get(HITS);
    }

    public boolean isHunterFallback() {
        return hunterFallback;
    }

    public boolean isReturningToOwner() {
        return entityData.get(RETURNING);
    }

    public int flightTick() {
        return flightTick;
    }

    public int stagnantTicks() {
        return stagnantTicks;
    }

    private void clearFlightLock() {
        UUID ownerId = entityData.get(OWNER_ID).orElse(null);
        UUID braceletId = entityData.get(BRACELET_ID).orElse(null);
        UUID flightId = entityData.get(FLIGHT_ID).orElse(null);
        if (ownerId != null && braceletId != null && flightId != null) {
            BraceletFlightService.FLIGHTS.complete(ownerId, braceletId, flightId);
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.hasUUID("Owner")) entityData.set(OWNER_ID, Optional.of(tag.getUUID("Owner")));
        if (tag.hasUUID("Bracelet")) entityData.set(BRACELET_ID, Optional.of(tag.getUUID("Bracelet")));
        if (tag.hasUUID("Flight")) entityData.set(FLIGHT_ID, Optional.of(tag.getUUID("Flight")));
        entityData.set(RETURNING, tag.getBoolean("Returning"));
        entityData.set(HITS, tag.getInt("Hits"));
        origin = readVec(tag, "Origin");
        forward = readVec(tag, "Forward");
        right = readVec(tag, "Right");
        localUp = readVec(tag, "Up");
        steering = readVec(tag, "Steering");
        hunterFallback = tag.getBoolean("HunterFallback");
        flightTick = tag.getInt("FlightTick");
        totalFlightTicks = tag.getInt("TotalFlightTicks");
        stagnantTicks = tag.getInt("StagnantTicks");
        navigationTargetId = tag.hasUUID("HunterTarget") ? tag.getUUID("HunterTarget") : null;
        readUuidSet(tag, "HunterHitSet", hunterHitSet);
        readUuidSet(tag, "OutgoingHits", outgoingHits);
        readUuidSet(tag, "ReturningHits", returningHits);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        entityData.get(OWNER_ID).ifPresent(id -> tag.putUUID("Owner", id));
        entityData.get(BRACELET_ID).ifPresent(id -> tag.putUUID("Bracelet", id));
        entityData.get(FLIGHT_ID).ifPresent(id -> tag.putUUID("Flight", id));
        tag.putBoolean("Returning", entityData.get(RETURNING));
        tag.putInt("Hits", entityData.get(HITS));
        writeVec(tag, "Origin", origin);
        writeVec(tag, "Forward", forward);
        writeVec(tag, "Right", right);
        writeVec(tag, "Up", localUp);
        writeVec(tag, "Steering", steering);
        tag.putBoolean("HunterFallback", hunterFallback);
        tag.putInt("FlightTick", flightTick);
        tag.putInt("TotalFlightTicks", totalFlightTicks);
        tag.putInt("StagnantTicks", stagnantTicks);
        if (navigationTargetId != null) tag.putUUID("HunterTarget", navigationTargetId);
        writeUuidSet(tag, "HunterHitSet", hunterHitSet);
        writeUuidSet(tag, "OutgoingHits", outgoingHits);
        writeUuidSet(tag, "ReturningHits", returningHits);
    }

    private static void writeVec(CompoundTag tag, String name, Vec3 value) {
        CompoundTag vector = new CompoundTag();
        vector.putDouble("X", value.x);
        vector.putDouble("Y", value.y);
        vector.putDouble("Z", value.z);
        tag.put(name, vector);
    }

    private static Vec3 readVec(CompoundTag tag, String name) {
        CompoundTag vector = tag.getCompound(name);
        return new Vec3(vector.getDouble("X"), vector.getDouble("Y"), vector.getDouble("Z"));
    }

    private static void writeUuidSet(CompoundTag tag, String name, Set<UUID> values) {
        ListTag list = new ListTag();
        for (UUID value : values) list.add(NbtUtils.createUUID(value));
        tag.put(name, list);
    }

    private static void readUuidSet(CompoundTag tag, String name, Set<UUID> target) {
        ListTag list = tag.getList(name, Tag.TAG_INT_ARRAY);
        for (int i = 0; i < list.size(); i++) target.add(NbtUtils.loadUUID(list.get(i)));
    }
}

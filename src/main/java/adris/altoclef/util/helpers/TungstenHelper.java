package adris.altoclef.util.helpers;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Tungsten pathfinder integration via reflection.
 * Fallback when Baritone fails (NaN, stuck, no path).
 * All methods are safe to call even if Tungsten is not installed — they no-op.
 */
public class TungstenHelper {

    private static Boolean loaded = null;
    private static boolean active = false;
    private static int failCount = 0;
    private static long lastStartTime = 0;

    private static final int MAX_FAIL_COUNT = 3;
    private static final long COOLDOWN_MS = 2000;

    // Cached reflection handles
    private static boolean reflectionReady = false;
    private static Object pathfinderInstance;    // TungstenModDataContainer.PATHFINDER
    private static Object executorInstance;      // TungstenModDataContainer.EXECUTOR
    private static AtomicBoolean pathfinderActive;
    private static AtomicBoolean pathfinderStop;
    private static Field executorStopField;
    private static Method findMethod;            // PathFinder.find(WorldView, Vec3d, PlayerEntity)
    private static Method executorIsRunning;
    private static Field searchTimeoutField;
    private static Field minPathSizeField;
    private static Field minDistPathField;

    public static boolean isTungstenLoaded() {
        if (loaded == null) {
            loaded = FabricLoader.getInstance().isModLoaded("tungsten");
            if (loaded) initReflection();
        }
        return loaded && reflectionReady;
    }

    private static void initReflection() {
        try {
            Class<?> containerClass = Class.forName("kaptainwutax.tungsten.TungstenModDataContainer");
            pathfinderInstance = containerClass.getField("PATHFINDER").get(null);
            executorInstance = containerClass.getField("EXECUTOR").get(null);

            Class<?> pfClass = pathfinderInstance.getClass();
            pathfinderActive = (AtomicBoolean) pfClass.getField("active").get(pathfinderInstance);
            pathfinderStop = (AtomicBoolean) pfClass.getField("stop").get(pathfinderInstance);

            searchTimeoutField = pfClass.getField("searchTimeoutMs");
            minPathSizeField = pfClass.getField("minPathSizeForTimeout");
            minDistPathField = pfClass.getField("minDistPath");

            findMethod = pfClass.getMethod("find",
                    Class.forName("net.minecraft.world.WorldView"),
                    Vec3d.class,
                    Class.forName("net.minecraft.entity.player.PlayerEntity"));

            Class<?> execClass = executorInstance.getClass();
            executorIsRunning = execClass.getMethod("isRunning");
            executorStopField = execClass.getField("stop");

            reflectionReady = true;
            Debug.logInternal("[TungstenHelper] Reflection init OK");
        } catch (Exception e) {
            reflectionReady = false;
            Debug.logWarning("[TungstenHelper] Reflection init failed: " + e.getMessage());
        }
    }

    /**
     * Try Tungsten pathfinding to a position. Returns true if Tungsten was started.
     */
    public static boolean tryPathTo(Vec3d target) {
        if (!isTungstenLoaded()) return false;
        if (failCount >= MAX_FAIL_COUNT) return false;
        if (System.currentTimeMillis() - lastStartTime < COOLDOWN_MS) return false;

        try {
            var player = AltoClef.getInstance().getPlayer();
            var world = AltoClef.getInstance().getWorld();
            if (player == null || world == null) return false;

            // Don't restart if already working
            if (pathfinderActive.get() || (boolean) executorIsRunning.invoke(executorInstance))
                return true;

            // Configure for responsive fallback
            searchTimeoutField.set(pathfinderInstance, 1500L);
            minPathSizeField.set(pathfinderInstance, 2);
            minDistPathField.set(pathfinderInstance, 0.3);

            findMethod.invoke(pathfinderInstance, world, target, player);
            lastStartTime = System.currentTimeMillis();
            active = true;

            Debug.logInternal("[TungstenHelper] Started fallback pathfinding to " + target);
            return true;
        } catch (Exception e) {
            Debug.logWarning("[TungstenHelper] Failed to start: " + e.getMessage());
            failCount++;
            return false;
        }
    }

    /** Try Tungsten pathfinding to an entity. */
    public static boolean tryPathToEntity(Entity entity) {
        if (entity == null || entity.isRemoved()) return false;
        return tryPathTo(entity.getPos());
    }

    /** Stop Tungsten pathfinding if it's running. */
    public static void stop() {
        if (!isTungstenLoaded() || !active) return;
        try {
            pathfinderStop.set(true);
            executorStopField.set(executorInstance, true);
            active = false;
            Debug.logInternal("[TungstenHelper] Stopped fallback pathfinding");
        } catch (Exception e) {
            Debug.logWarning("[TungstenHelper] Failed to stop: " + e.getMessage());
        }
    }

    /** Is Tungsten currently pathfinding or executing? */
    public static boolean isActive() {
        if (!isTungstenLoaded() || !active) return false;
        try {
            return pathfinderActive.get()
                    || (boolean) executorIsRunning.invoke(executorInstance);
        } catch (Exception e) {
            return false;
        }
    }

    /** Reset fail counter — call when task restarts or target changes. */
    public static void reset() {
        failCount = 0;
        active = false;
        lastStartTime = 0;
    }
}

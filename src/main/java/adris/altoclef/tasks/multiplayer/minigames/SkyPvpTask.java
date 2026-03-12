package adris.altoclef.tasks.multiplayer.minigames;

import adris.altoclef.AltoClef;
import adris.altoclef.tasksystem.Task;

/**
 * Sky PvP task stub — not yet implemented.
 */
public class SkyPvpTask extends Task {

    @Override
    protected void onStart() {
    }

    @Override
    protected Task onTick() {
        return null;
    }

    @Override
    protected void onStop(Task interruptTask) {
    }

    @Override
    protected boolean isEqual(Task other) {
        return other instanceof SkyPvpTask;
    }

    @Override
    protected String toDebugString() {
        return "SkyPvp (stub)";
    }
}

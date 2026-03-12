package adris.altoclef.util.helpers;

import adris.altoclef.AltoClef;
import adris.altoclef.util.time.TimerGame;
import baritone.api.utils.input.Input;

public abstract class KillAuraHelper {
    static long _timeExpires = 0;
    static double _rvalue = -1;
    public static double initialYaw = -1f;

    public static double GetNextRandomY() {
        if (System.currentTimeMillis() >= _timeExpires) {
            _timeExpires = System.currentTimeMillis() + 55;
            _rvalue = -1.0 + Math.random() * 1.0;
        }
        return _rvalue;
    }

    public static long TimerStartTime = -1;
    public static long TimerStartExpires = 0;
    public static long TimerGoing = 0;
    static long _lastRequestTime = -1;
    public static float YawSpeed = 1;
    public static float PitchSpeed = 1;
    private static final TimerGame _inPvpAction = new TimerGame(1);
    private static final TimerGame _CooldownFor18 = new TimerGame(0.07);
    private static final TimerGame _rotatedMoveTimer = new TimerGame(1);
    private static Input _rotatedMove = Input.MOVE_RIGHT;

    public static boolean ElapsedPvpCD() {
        return _CooldownFor18.elapsed();
    }

    public static void ResetPvpCD() {
        _CooldownFor18.setInterval(0.02 + (Math.random() / 30));
        _CooldownFor18.reset();
    }

    public static void TimerStop() {
        if (TimerStartTime != -1) {
            TimerStartTime = -1;
            initialYaw = -1;
            _lastRequestTime = -1;
            _inPvpAction.reset();
        }
    }

    public static boolean IsInBattle() {
        return _inPvpAction.elapsed();
    }

    public static boolean TimerStart(float initialYaww) {
        IsInBattle();
        if (TimerStartTime == -1) {
            YawSpeed = 0;
            PitchSpeed = 0;
            initialYaw = initialYaww;
            TimerStartTime = System.currentTimeMillis();
            TimerStartExpires = TimerStartTime + 100;
            _lastRequestTime = TimerStartTime;
            TimerGoing = 0;
            return true;
        } else {
            TimerGoing = (System.currentTimeMillis() - TimerStartTime);
            if (System.currentTimeMillis() > _lastRequestTime + 100) {
                TimerStop();
            }
            _lastRequestTime = System.currentTimeMillis();
            return false;
        }
    }

    public static long JumpTimerStarted = -1;

    public static Input getRotatedMove() {
        if (_rotatedMoveTimer.elapsed()) {
            _rotatedMoveTimer.reset();
            _rotatedMove = Math.random() > 0.5 ? Input.MOVE_LEFT : Input.MOVE_RIGHT;
        }
        return _rotatedMove;
    }

    public static void GoJump(AltoClef mod, boolean rotated, boolean jump) {
        if (JumpTimerStarted == -1) {
            JumpTimerStarted = System.currentTimeMillis();
        }
        if (System.currentTimeMillis() > JumpTimerStarted + 900) {
            boolean doJump = jump && mod.getPlayer().isOnGround();
            boolean highSpeed = mod.getPlayer().getVelocity().horizontalLengthSquared() > 0.02;
            new Thread(() -> {
                Input rotatedInp = getRotatedMove();
                mod.getInputControls().hold(Input.SPRINT);
                mod.getInputControls().hold(Input.MOVE_FORWARD);
                if (highSpeed) mod.getInputControls().hold(Input.JUMP);
                if (rotated) mod.getInputControls().hold(rotatedInp);
                sleepSec(0.3);
                if (doJump) mod.getInputControls().hold(Input.JUMP);
                sleepSec(0.5);
                if (rotated) mod.getInputControls().release(rotatedInp);
                mod.getInputControls().release(Input.MOVE_FORWARD);
                mod.getInputControls().release(Input.SPRINT);
                mod.getInputControls().release(Input.JUMP);
            }).start();
            JumpTimerStarted = -1;
        }
    }

    public static void GoJump(AltoClef mod, boolean rotated) {
        GoJump(mod, rotated, false);
    }

    private static void sleepSec(double seconds) {
        try {
            Thread.sleep((int) (1000 * seconds));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

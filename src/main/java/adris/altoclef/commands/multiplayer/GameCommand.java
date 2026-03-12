package adris.altoclef.commands.multiplayer;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.commandsystem.ArgParser;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.commandsystem.args.StringArg;
import adris.altoclef.commandsystem.exception.CommandException;
import adris.altoclef.tasks.speedrun.beatgame.BeatMinecraftTask;
import adris.altoclef.util.agent.Pipeline;

public class GameCommand extends Command {
    public GameCommand() {
        super("game", "Run the main game or minigame pipeline (task chain)", new StringArg("pipeline", ""));
    }

    @Override
    protected void call(AltoClef mod, ArgParser parser) throws CommandException {
        String pipelineStr = parser.get(String.class);
        if (pipelineStr == null || pipelineStr.isBlank()) {
            Debug.logMessage("Pipeline set to None");
            AltoClef.setPipeline(Pipeline.None);
            finish();
            return;
        }
        pipelineStr = pipelineStr.toLowerCase();

        switch (pipelineStr) {
            case "none", "no":
                Debug.logMessage("Pipeline set to None");
                AltoClef.setPipeline(Pipeline.None);
                finish();
                break;
            // SkyWars, BedWars, BattleRoyale tasks not yet ported — set pipeline flag only
            case "swt", "skywarsteam", "sky_wars_team":
                AltoClef.setPipeline(Pipeline.SkyWars);
                Debug.logMessage("Pipeline set to SkyWars (task not yet available)");
                finish();
                break;
            case "sw", "skywars", "sky_wars":
                AltoClef.setPipeline(Pipeline.SkyWars);
                Debug.logMessage("Pipeline set to SkyWars (task not yet available)");
                finish();
                break;
            case "mm", "murder", "murdermystery", "mystery":
                AltoClef.setPipeline(Pipeline.MurderMystery);
                Debug.logMessage("Pipeline set to MurderMystery (task not yet available)");
                finish();
                break;
            case "bw", "bed", "bedwars":
                AltoClef.setPipeline(Pipeline.BedWars);
                Debug.logMessage("Pipeline set to BedWars (task not yet available)");
                finish();
                break;
            case "megabattle", "mega", "evil", "yandere":
                AltoClef.setPipeline(Pipeline.BattleRoyale);
                Debug.logMessage("Pipeline set to BattleRoyale (task not yet available)");
                finish();
                break;
            default:
                // Speedrun / beat Minecraft
                Debug.logMessage("Pipeline set to SpeedRun");
                AltoClef.setPipeline(Pipeline.SpeedRun);
                mod.runUserTask(new BeatMinecraftTask(mod), this::finish);
                break;
        }
    }
}

package adris.altoclef.util.agent;

/**
 * Represents a current pipeline that is higher level than a task chain.
 */
public enum Pipeline {
    SpeedRun("Minecraft speedrunning mode - focuses on beating the game by killing the Ender Dragon."),
    SkyWars("SkyWars game mode - strategy for surviving and winning in SkyWars minigame."),
    MurderMystery("Murder Mystery mode - playing the Murder Mystery minigame."),
    BedWars("BedWars game mode - strategy for surviving and winning in Bed Wars minigame."),
    BattleRoyale("Battle royale mode - aggressive combat pipeline."),
    None("");

    private final String description;

    Pipeline(String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }
}

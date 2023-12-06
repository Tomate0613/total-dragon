package tomate.totaldragon;

// TODO Replace this with an actual config (placeholder for now)
public class DragonConfig {
    public enum PhantomBehaviour {
        NO_PHANTOMS_IN_FIGHT,
        VANILLA_PHANTOMS,
        IMPROVED_PHANTOMS_IN_FIGHT,
        IMPROVED_PHANTOMS_ALWAYS
    };

    public static final boolean logDragonPhasesToInGameChat = false;
    public static final boolean playersFallIntoOverworld = true;
    public static final boolean chainReactionEndCrystals = true;
    public static final boolean endCrystalSpawns = true;
    public static final boolean spawnEndermitesInFight = true;
    public static final PhantomBehaviour phantomBehaviour = PhantomBehaviour.IMPROVED_PHANTOMS_IN_FIGHT;
}

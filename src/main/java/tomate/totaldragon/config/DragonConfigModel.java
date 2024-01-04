package tomate.totaldragon.config;

import io.wispforest.owo.config.annotation.Config;
import io.wispforest.owo.config.annotation.Modmenu;

@Modmenu(modId = "total-dragon")
@Config(name = "dragon-config", wrapperName = "DragonConfig")
public class DragonConfigModel {
    public enum PhantomBehaviour {
        NO_PHANTOMS_IN_FIGHT,
        VANILLA_PHANTOMS,
        IMPROVED_PHANTOMS_IN_FIGHT,
        IMPROVED_PHANTOMS_ALWAYS
    };

    public boolean playersFallIntoOverworld = true;
    public boolean chainReactionEndCrystals = true;
    public boolean endCrystalSpawns = true;
    public boolean spawnEndermitesInFight = true;
    public PhantomBehaviour phantomBehaviour = PhantomBehaviour.IMPROVED_PHANTOMS_IN_FIGHT;
    public boolean logDragonPhasesToInGameChat = false;
}

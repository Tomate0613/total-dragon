package tomate.totaldragon.config;

import io.wispforest.owo.config.annotation.Config;
import io.wispforest.owo.config.annotation.Modmenu;
import io.wispforest.owo.config.annotation.Nest;
import io.wispforest.owo.config.annotation.RangeConstraint;

@Modmenu(modId = "total-dragon")
@Config(name = "dragon-config", wrapperName = "DragonConfig")
public class DragonConfigModel {
    public enum PhantomBehaviour {
        NO_PHANTOMS_IN_FIGHT,
        VANILLA_PHANTOMS,
        IMPROVED_PHANTOMS_IN_FIGHT,
        IMPROVED_PHANTOMS_ALWAYS
    }

    public boolean playersFallIntoOverworld = true;
    public boolean chainReactionEndCrystals = true;
    public boolean endCrystalSpawns = true;
    public boolean spawnEndermitesInFight = true;
    public PhantomBehaviour phantomBehaviour = PhantomBehaviour.IMPROVED_PHANTOMS_IN_FIGHT;
    public boolean logDragonPhasesToInGameChat = false;

    @Nest
    public FireballConfig fireball = new FireballConfig();

    public static class FireballConfig {
        @RangeConstraint(min = 1, max = 100)
        public int minRadius = 10;

        @RangeConstraint(min = 1, max = 100)
        public int maxRadius = 14;

        @RangeConstraint(min = 10, max = 1000)
        public int duration = 600;
        @RangeConstraint(min = 10, max = 1000)
        public float radiusPerTick = 0.001f;
        @RangeConstraint(min = -10, max = 10)
        public float radiusOnUse = 1;
        @RangeConstraint(min = 1, max = 100)
        public int waitTime = 20;
        @RangeConstraint(min = 10, max = 1000)
        public int reapplicationDelay = 10;

        @RangeConstraint(min = 1, max = 1000)
        public int effectDuration = 1;
        @RangeConstraint(min = 0, max = 255)
        public int effectAmplifier = 2;
    }
}

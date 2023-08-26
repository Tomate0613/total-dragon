package tomate.totaldragon;

import com.google.common.collect.ImmutableList;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.SpikeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.SpikeConfiguration;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;

// This is probably the worst way to maintain the fight state, so I will most likely change it in the future
public class FightState {
    public static final int HEALTH_PER_PLAYER = 200;
    // This needs to be > 0, as the dragon would die if nobody was online otherwise
    public static final int EXTRA_HEALTH = 10;
    public static final int CRYSTALS_PER_PLAYER = 10;
    public static final int CRYSTALS_RESPAWN_PER_DEATH = 5;

    @Nullable
    public static ServerPlayer targetPlayer;
    public static float knockbackCooldown = 0;
    public static EnderDragon dragon;

    public static int crystalsDestroyed = 0;
    public static final LinkedList<BlockPos> crystalPositions = new LinkedList<>();

    public static boolean isNextSpikeEndCrystalVulnerable = false;

    public static Vec3 vulnerableEndCrystalPos;

    public static void onPlayerJoin() {
        if(dragon == null)
            return;

        recalculateMaxDragonHealth();
        dragon.setHealth(dragon.getHealth() + HEALTH_PER_PLAYER);
    }

    public static void onPlayerLeave() {
        if(dragon == null)
            return;

        recalculateMaxDragonHealth();
    }

    public static void onPlayerDied() {
        crystalsDestroyed -= CRYSTALS_RESPAWN_PER_DEATH;

        if(crystalsDestroyed < 0)
            crystalsDestroyed = 0;
    }

    public static void onEnderDragonSpawned(EnderDragon dragon) {
        FightState.dragon = dragon;

        var server = dragon.getServer();

        if(server == null)
            throw new RuntimeException("The dragon was spawned but there is no server");

        var max = recalculateMaxDragonHealth();
        dragon.setHealth((float)max);
    }

    public static int getCrystalsToDestroy() {
        var dragonFight = dragon.getDragonFight();

        if(dragonFight == null)
            throw new RuntimeException("No dragon fight");

        var server = dragon.getServer();
        var playerCount = server == null ? 1 : server.getPlayerCount();

        TotalDragon.LOGGER.debug("PlayerCount: " + playerCount);
        TotalDragon.LOGGER.debug("CrystalsPerPlayer: " + crystalPositions + " => " + playerCount * CRYSTALS_PER_PLAYER);
        TotalDragon.LOGGER.debug("CrystalsDestroyed: " + crystalsDestroyed);
        TotalDragon.LOGGER.debug("CrystalsAlive: " + dragonFight.getCrystalsAlive());

        return (playerCount * CRYSTALS_PER_PLAYER) - crystalsDestroyed - dragonFight.getCrystalsAlive();
    }

    public static int getPlayerCount() {
        var server = dragon.getServer();
        return server == null ? 1 : server.getPlayerCount();
    }

    public static double recalculateMaxDragonHealth() {
        var playersOnline = getPlayerCount();
        var maxHealth = HEALTH_PER_PLAYER * playersOnline + EXTRA_HEALTH;

        var maxHealthAttribute = dragon.getAttributes().getInstance(Attributes.MAX_HEALTH);

        assert maxHealthAttribute != null;
        maxHealthAttribute.setBaseValue(HEALTH_PER_PLAYER * playersOnline);

        if(dragon.getHealth() > maxHealth)
            dragon.setHealth((float) maxHealth);


        return maxHealth;
    }

    public static void resetData() {
        TotalDragon.LOGGER.info("Resetting data");
        crystalsDestroyed = 0;
        crystalPositions.clear();
    }

    public static void respawnSpike(BlockPos pos) {
        if(dragon.level() instanceof ServerLevel serverLevel) {
            var endSpikes = SpikeFeature.getSpikesForLevel(serverLevel);

            AtomicBoolean foundEndspike = new AtomicBoolean(false);
            endSpikes.forEach(endSpike -> {
                var pos2 = new Vec2(pos.getX(), pos.getZ());
                var endSpikePos2 = new Vec2(endSpike.getCenterX(), endSpike.getCenterZ());

                if(pos2.distanceToSqr(endSpikePos2) > 3)
                    return;

                if(foundEndspike.get())
                    return;

                foundEndspike.set(true);


                isNextSpikeEndCrystalVulnerable = true;
                serverLevel.explode(null, (float)endSpike.getCenterX() + 0.5f, endSpike.getHeight(), (float)endSpike.getCenterZ() + 0.5f, 5.0f, Level.ExplosionInteraction.BLOCK);
                SpikeConfiguration spikeConfiguration = new SpikeConfiguration(true, ImmutableList.of(endSpike), new BlockPos(0, 128, 0));
                Feature.END_SPIKE.place(spikeConfiguration, serverLevel, serverLevel.getChunkSource().getGenerator(), RandomSource.create(), new BlockPos(endSpike.getCenterX(), 45, endSpike.getCenterZ()));
            });

            if(!foundEndspike.get()) {
                throw new RuntimeException("Could not find endspike");
            }
        }
    }
}

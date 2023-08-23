package tomate.totaldragon.phases;

import net.minecraft.world.entity.boss.enderdragon.phases.DragonPhaseInstance;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;

import java.util.Arrays;

public class CustomEnderDragonPhase {
    public static final EnderDragonPhase<DragonSittingKickingPhase> SITTING_KICKING = create(DragonSittingKickingPhase.class);

    // TODO This code is absolutely awful
    private static <T extends DragonPhaseInstance> EnderDragonPhase<T> create(Class<T> instanceClass) {
        var name = instanceClass.getName();
        EnderDragonPhase<T> enderDragonPhase = new EnderDragonPhase<T>(EnderDragonPhase.phases.length, instanceClass, name);
        EnderDragonPhase.phases = Arrays.copyOf(EnderDragonPhase.phases, EnderDragonPhase.phases.length + 1);
        EnderDragonPhase.phases[enderDragonPhase.getId()] = enderDragonPhase;
        return enderDragonPhase;
    }

    public static void load() {
        SITTING_KICKING.getId();
    }
}

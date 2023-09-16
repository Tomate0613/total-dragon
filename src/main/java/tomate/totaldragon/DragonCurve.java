package tomate.totaldragon;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.phys.Vec3;

public class DragonCurve {
    EnderDragon dragon;
    Vec3 startingPoint;
    Vec3 middlePoint;
    Vec3 endingPoint;
    Vec3 direction = Vec3.ZERO;

    float curveProgress;


    public DragonCurve(EnderDragon dragon) {
        this.dragon = dragon;
    }

    public void generateNext() {
        var startingDirection = direction.scale(-1);
        startingPoint = dragon.position();

        if(dragon.position().add(startingDirection).y < 70) {
            startingDirection = new Vec3(startingDirection.x, 0, startingDirection.y);
        }

        middlePoint = startingDirection.scale(1 * startingPoint.distanceTo(endingPoint)).add(startingPoint);
        curveProgress = 0;
    }

    public void nextStep(Vec3 endingPoint) {
        if(curveProgress > 1 || this.endingPoint == null) {
            this.endingPoint = endingPoint;
            generateNext();
        }

        var q1 = startingPoint.lerp(middlePoint, curveProgress);
        var q2 = middlePoint.lerp(this.endingPoint, curveProgress);

        var pos = q1.lerp(q2, curveProgress);
        var distance = pos.distanceTo(dragon.position());

        dragon.setDeltaMovement(pos.subtract(dragon.position()));
        dragon.setPos(pos);

        direction = q2.vectorTo(q1).normalize();
        dragon.setYRot((float) Math.toDegrees(Mth.atan2(direction.z, direction.x)) - 90);
        dragon.setXRot((float) Math.toDegrees(Mth.atan2(direction.y, Math.sqrt(direction.x * direction.x + direction.z * direction.z))));

        if(distance == 0)
            curveProgress += 0.01f;
        else
            curveProgress += (float) (1 / distance * 0.01);

        // drawCurve(); // for debugging
    }

    void drawCurve() {
        var curveProgress = 0f;
        var pos = startingPoint;

        while(curveProgress < 1) {

            var q1 = startingPoint.lerp(middlePoint, curveProgress);
            var q2 = middlePoint.lerp(this.endingPoint, curveProgress);

            var pos1 = q1.lerp(q2, curveProgress);
            var distance = pos1.distanceTo(pos);

            TotalDragon.spawnParticleLine(pos, pos1, 3, FightState.targetPlayer, dragon.level());

            pos = pos1;


            if (distance == 0)
                curveProgress += 0.01f;
            else
                curveProgress += (float) (1 / distance * 0.01);
        }
        TotalDragon.spawnParticleLine(startingPoint, endingPoint, 1, FightState.targetPlayer, dragon.level());
    }
}

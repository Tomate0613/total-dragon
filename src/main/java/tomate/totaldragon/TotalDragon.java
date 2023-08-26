package tomate.totaldragon;

import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tomate.totaldragon.effects.DragonBreath;
import tomate.totaldragon.phases.CustomEnderDragonPhase;

import java.util.Objects;

import static tomate.totaldragon.FightState.dragon;
import static tomate.totaldragon.FightState.targetPlayer;

public class TotalDragon implements ModInitializer {
    private static final String TAG_ENCH_ID = "id";
    private static final String TAG_ENCH_LEVEL = "lvl";
    private static final String TAG_ENCH = "Enchantments";
    public static final SimpleParticleType SHOCKWAVE = FabricParticleTypes.simple();
    public static final MobEffect DRAGON_BREATH = new DragonBreath();
    public static final Logger LOGGER = LoggerFactory.getLogger("total-dragon");

    @Override
    public void onInitialize() {
        Registry.register(BuiltInRegistries.MOB_EFFECT, "dragon_breath", DRAGON_BREATH);

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(Commands.literal("dragonphase").requires(commandSourceStack -> commandSourceStack.hasPermission(2))
                    .then(Commands.literal("charging")
                    .executes(context -> {
                        setDragonPhase(context, EnderDragonPhase.CHARGING_PLAYER);
                        return 1;
                    })).then(Commands.literal("dying").executes(context -> {
                        setDragonPhase(context, EnderDragonPhase.DYING);
                        return 1;
                    })).then(Commands.literal("holding_pattern").executes(context -> {
                        setDragonPhase(context, EnderDragonPhase.HOLDING_PATTERN);
                        return 1;
                    })).then(Commands.literal("hovering").executes(context -> {
                        setDragonPhase(context, EnderDragonPhase.HOVERING);
                        return 1;
                    })).then(Commands.literal("landing").executes(context -> {
                        setDragonPhase(context, EnderDragonPhase.LANDING);
                        return 1;
                    })).then(Commands.literal("landing-approach").executes(context -> {
                        setDragonPhase(context, EnderDragonPhase.LANDING_APPROACH);
                        return 1;
                    })).then(Commands.literal("sitting-attacking").executes(context -> {
                        setDragonPhase(context, EnderDragonPhase.SITTING_ATTACKING);
                        return 1;
                    })).then(Commands.literal("sitting-flaming").executes(context -> {
                        setDragonPhase(context, EnderDragonPhase.SITTING_FLAMING);
                        return 1;
                    })).then(Commands.literal("sitting-scanning").executes(context -> {
                        setDragonPhase(context, EnderDragonPhase.SITTING_SCANNING);
                        return 1;
                    })).then(Commands.literal("strafe-player").executes(context -> {
                        setDragonPhase(context, EnderDragonPhase.STRAFE_PLAYER);
                        return 1;
                    })).then(Commands.literal("sitting-kicking").executes(context -> {
                        setDragonPhase(context, CustomEnderDragonPhase.SITTING_KICKING);
                        return 1;
                    })).then(Commands.literal("takeoff").executes(context -> {
                        setDragonPhase(context, EnderDragonPhase.TAKEOFF);
                        return 1;
                    }))
            );

            dispatcher.register(Commands.literal("particlepath").then(Commands.argument("pos1", Vec3Argument.vec3()).then(Commands.argument("pos2", Vec3Argument.vec3()).executes(context -> {
                var pos1 = Vec3Argument.getVec3(context, "pos1");
                var pos2 = Vec3Argument.getVec3(context, "pos2");


                spawnParticleLine(pos1, pos2, 10, context.getSource().getPlayer(), context.getSource().getLevel());
                return 1;
            }))));

            dispatcher.register(Commands.literal("end").requires(commandSourceStack -> commandSourceStack.hasPermission(2)).executes(context -> {
                if(context.getSource().getPlayer() == null)
                    return 0;

                context.getSource().getPlayer().changeDimension(context.getSource().getServer().getLevel(Level.END));

                return 1;
            }));

            dispatcher.register(Commands.literal("kit").requires(commandSourceStack -> commandSourceStack.hasPermission(2)).executes(this::kit));

            dispatcher.register(Commands.literal("ready").requires(commandSourceStack -> commandSourceStack.hasPermission(2)).executes(context -> {
                if(context.getSource().getPlayer() == null)
                    return 0;

                context.getSource().getPlayer().changeDimension(context.getSource().getServer().getLevel(Level.END));
                return kit(context);
            }));

        });

        Registry.register(BuiltInRegistries.PARTICLE_TYPE, new ResourceLocation("totaldragon", "shockwave"), SHOCKWAVE);
    }

    public int kit(CommandContext<CommandSourceStack> context) {
        if(context.getSource().getPlayer() == null)
            return 0;

        context.getSource().getPlayer().getInventory().clearContent();

        // Armor
        ItemStack helmet = new ItemStack(Items.NETHERITE_HELMET);
        helmet.enchant(Enchantments.ALL_DAMAGE_PROTECTION, 4);
        enchant(helmet, "combatroll:multi_roll", 4);

        ItemStack chestplate = new ItemStack(Items.NETHERITE_CHESTPLATE);
        chestplate.enchant(Enchantments.ALL_DAMAGE_PROTECTION, 4);
        enchant(chestplate, "combatroll:acrobat", 10);


        ItemStack leggings = new ItemStack(Items.NETHERITE_LEGGINGS);
        leggings.enchant(Enchantments.ALL_DAMAGE_PROTECTION, 4);

        ItemStack boots = new ItemStack(Items.NETHERITE_BOOTS);
        boots.enchant(Enchantments.ALL_DAMAGE_PROTECTION, 4);
        enchant(boots, "combatroll:longfooted", 5);


        context.getSource().getPlayer().getInventory().armor.set(EquipmentSlot.HEAD.getIndex(), helmet);
        context.getSource().getPlayer().getInventory().armor.set(EquipmentSlot.CHEST.getIndex(), chestplate);
        context.getSource().getPlayer().getInventory().armor.set(EquipmentSlot.LEGS.getIndex(), leggings);
        context.getSource().getPlayer().getInventory().armor.set(EquipmentSlot.FEET.getIndex(), boots);

        // Items
        ItemStack food = new ItemStack(Items.GOLDEN_CARROT);
        food.setCount(64);

        ItemStack bow = new ItemStack(Items.BOW);
        bow.enchant(Enchantments.POWER_ARROWS, 5);

        ItemStack arrows = new ItemStack(Items.ARROW);
        arrows.setCount(64);

        ItemStack sword = new ItemStack(Items.NETHERITE_SWORD);
        sword.enchant(Enchantments.SHARPNESS, 5);

        ItemStack dirt = new ItemStack(Items.DIRT);
        dirt.setCount(64);

        ItemStack waterbucket = new ItemStack(Items.WATER_BUCKET);

        ItemStack enderpearls = new ItemStack(Items.ENDER_PEARL);
        enderpearls.setCount(16);

        context.getSource().getPlayer().getInventory().setItem(0, sword);
        context.getSource().getPlayer().getInventory().setItem(1, bow);
        context.getSource().getPlayer().getInventory().setItem(2, dirt);
        context.getSource().getPlayer().getInventory().setItem(3, waterbucket);
        context.getSource().getPlayer().getInventory().setItem(4, enderpearls);
        context.getSource().getPlayer().getInventory().setItem(5, enderpearls.copy());
        context.getSource().getPlayer().getInventory().setItem(6, enderpearls.copy());
        context.getSource().getPlayer().getInventory().setItem(8, food);

        context.getSource().getPlayer().getInventory().setItem(9, arrows);
        context.getSource().getPlayer().getInventory().setItem(10, arrows.copy());
        context.getSource().getPlayer().getInventory().setItem(11, arrows.copy());
        context.getSource().getPlayer().getInventory().setItem(12, arrows.copy());

        context.getSource().getPlayer().getInventory().setItem(13, dirt.copy());
        context.getSource().getPlayer().getInventory().setItem(14, dirt.copy());
        context.getSource().getPlayer().getInventory().setItem(15, dirt.copy());



        context.getSource().sendSuccess(() -> Component.literal("Done"), true);

        return 1;
    }

    public static void spawnParticleLine(Vec3 start, Vec3 end, int particleCount, ServerPlayer serverPlayer, Level level) {
        if(end == null || serverPlayer == null)
            return;

        double distance = end.distanceTo(start);


        double incrementX = (end.x - start.x) / (particleCount * distance);
        double incrementY = (end.y - start.y) / (particleCount * distance);
        double incrementZ = (end.z - start.z) / (particleCount * distance);

        for (int i = 0; i < particleCount * distance; i++) {
            double x = start.x + incrementX * i;
            double y = start.y + incrementY * i;
            double z = start.z + incrementZ * i;

            if(level instanceof ServerLevel serverLevel)
                serverLevel.sendParticles(serverPlayer, ParticleTypes.DRAGON_BREATH, true, x, y, z, i, 0, 0, 0, 1); // .01 for debugging
            else
                level.addParticle(ParticleTypes.DRAGON_BREATH, x, y, z, 0, 0, 0);

        }
    }

    public void setDragonPhase(CommandContext<CommandSourceStack> context, EnderDragonPhase<?> phase) {
        try {
            dragon.getPhaseManager().setPhase(phase);

            if(phase == EnderDragonPhase.STRAFE_PLAYER) {
                dragon.getPhaseManager().getPhase(EnderDragonPhase.STRAFE_PLAYER).setTarget(context.getSource().getPlayer());
            }

            if(phase == EnderDragonPhase.CHARGING_PLAYER) {
                targetPlayer = context.getSource().getPlayer();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void enchant(ItemStack stack, String enchantment, int i) {
        stack.getOrCreateTag();
        if (!Objects.requireNonNull(stack.getTag()).contains(TAG_ENCH, 9)) {
            stack.getTag().put(TAG_ENCH, new ListTag());
        }
        ListTag listTag = stack.getTag().getList(TAG_ENCH, 10);

        CompoundTag compoundTag = new CompoundTag();
        compoundTag.putString(TAG_ENCH_ID, enchantment);
        compoundTag.putShort(TAG_ENCH_LEVEL, (short)i);

        listTag.add(compoundTag);
    }
}

package io.redspace.irons_artifice.modifier.modifiers;

import io.redspace.irons_artifice.api.AmmoEvent;
import io.redspace.irons_artifice.api.ComposeShotEvent;
import io.redspace.irons_artifice.api.GunShootEvent;
import io.redspace.irons_artifice.client.particle.ColorTransitionParticleOption;
import io.redspace.irons_artifice.data.ShotComponentMap;
import io.redspace.irons_artifice.data.ShotComponents;
import io.redspace.irons_artifice.gun.ShotProfile;
import io.redspace.irons_artifice.item.MagazineContents;
import io.redspace.irons_artifice.modifier.GunModifier;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import java.util.function.Consumer;

@EventBusSubscriber
public final class LeechModifier implements GunModifier {
    public static final float HEALTH_COST = 2.0f;
    private static final int TRAIL_FROM = 0xff6b6b;
    private static final int TRAIL_TO = 0x7a0000;
    private static final int MUZZLE_TINT = 0xc41e3a;

    @Override
    public void apply(ShotComponentMap components) {
        components.set(ShotComponents.LEECH, true);
    }

    @Override
    public void getDescriptionText(Consumer<Component> builder) {
        builder.accept(Component.translatable("irons_artifice.modifier.leech", (int) (HEALTH_COST / 2)).withStyle(ChatFormatting.RED));
    }

    @SubscribeEvent
    public static void substituteHealthForAmmo(AmmoEvent.Amount event) {
        ShotProfile profile = event.getShotProfile();
        if (!profile.get(ShotComponents.LEECH)) {
            return;
        }
        if (loadedAmmo(profile) >= event.getAmmoToConsume()) {
            return;
        }
        LivingEntity shooter = event.getEntity();
        if (!canPayWithHealth(shooter)) {
            return;
        }
        event.setAmmoToConsume(0);
    }

    @SubscribeEvent
    public static void payHealthAfterBloodShot(GunShootEvent.Post event) {
        ShotProfile profile = event.getShotProfile();
        if (!profile.get(ShotComponents.LEECH) || loadedAmmo(profile) > 0) {
            return;
        }
        LivingEntity shooter = event.getEntity();
        if (hasInfiniteMaterials(shooter)) {
            return;
        }
        shooter.setHealth(Math.max(0.1f, shooter.getHealth() - HEALTH_COST));
        shooter.hurtMarked = true;
    }

    @SubscribeEvent
    public static void tintBloodShots(ComposeShotEvent event) {
        ShotProfile profile = event.getShotProfile();
        if (!profile.get(ShotComponents.LEECH) || loadedAmmo(profile) > 0) {
            return;
        }
        ShotComponentMap components = profile.components();
        components.getOrCreate(ShotComponents.PARTICLE_TRAIL).add(ColorTransitionParticleOption.bulletTrail(TRAIL_FROM, TRAIL_TO));
        components.getOrCreate(ShotComponents.MUZZLE_FLASH).addTint(MUZZLE_TINT);
    }

    private static int loadedAmmo(ShotProfile profile) {
        MagazineContents magazine = profile.magazineContents();
        return magazine == null ? 0 : magazine.count();
    }

    private static boolean canPayWithHealth(LivingEntity shooter) {
        if (hasInfiniteMaterials(shooter)) {
            return true;
        }
        return shooter.getHealth() > HEALTH_COST;
    }

    private static boolean hasInfiniteMaterials(LivingEntity shooter) {
        return shooter instanceof Player player && player.hasInfiniteMaterials();
    }
}

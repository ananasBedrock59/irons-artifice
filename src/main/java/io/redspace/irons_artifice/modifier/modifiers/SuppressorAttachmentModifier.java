package io.redspace.irons_artifice.modifier.modifiers;

import io.redspace.irons_artifice.client.sounds.GunShotSoundSettings;
import io.redspace.irons_artifice.data.GunShotSoundStack;
import io.redspace.irons_artifice.data.ShotComponentMap;
import io.redspace.irons_artifice.data.ShotComponents;
import io.redspace.irons_artifice.modifier.GunModifier;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

public class SuppressorAttachmentModifier implements GunModifier {

    @Override
    public void apply(ShotComponentMap components) {
        GunShotSoundStack gunShotSoundStack = components.getOrDefault(ShotComponents.GUNSHOT_SOUND);
        GunShotSoundSettings baseSettings = gunShotSoundStack.getBaseSound();
        GunShotSoundSettings echoSettings = gunShotSoundStack.getEchoSound();
        gunShotSoundStack.setBaseSound(new GunShotSoundSettings(
                gunShotSoundStack.getEchoSound().soundEvent(),
                baseSettings.minPitch(), baseSettings.maxPitch(), baseSettings.start(), 0, baseSettings.end()
        ));
        components.getOrCreate(ShotComponents.GUNSHOT_SOUND).setEchoSound(new GunShotSoundSettings(echoSettings.soundEvent(), 0, 0, 0, 0, 0));
        components.getOrCreate(ShotComponents.MUZZLE_FLASH).types().clear();

    }

    @Override
    public void getDescriptionText(Consumer<Component> builder) {
        builder.accept(Component.translatable("irons_artifice.modifier.bayonet").withStyle(ChatFormatting.AQUA));
    }
}

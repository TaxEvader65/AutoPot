package com.taxevader65.autopot;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PotionItem;
import net.minecraft.item.SplashPotionItem;
import net.minecraft.potion.PotionUtil;

import net.minecraft.util.Hand;

import org.lwjgl.glfw.GLFW;

public class AutoPotClient implements ClientModInitializer {

    private static KeyBinding healKey;
    private static KeyBinding regenKey;
    private static KeyBinding strengthKey;
    private static KeyBinding speedKey;

    @Override
    public void onInitializeClient() {

        String category = "category.autopot";

        healKey = KeyBindingHelper.registerKeyBinding(
                new KeyBinding("key.autopot.healing",
                        InputUtil.Type.KEYSYM,
                        GLFW.GLFW_KEY_R,
                        category));

        regenKey = KeyBindingHelper.registerKeyBinding(
                new KeyBinding("key.autopot.regen",
                        InputUtil.Type.KEYSYM,
                        GLFW.GLFW_KEY_V,
                        category));

        strengthKey = KeyBindingHelper.registerKeyBinding(
                new KeyBinding("key.autopot.strength",
                        InputUtil.Type.KEYSYM,
                        GLFW.GLFW_KEY_B,
                        category));

        speedKey = KeyBindingHelper.registerKeyBinding(
                new KeyBinding("key.autopot.speed",
                        InputUtil.Type.KEYSYM,
                        GLFW.GLFW_KEY_N,
                        category));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {

            while (healKey.wasPressed())
                usePotion(client, StatusEffects.INSTANT_HEALTH);

            while (regenKey.wasPressed())
                usePotion(client, StatusEffects.REGENERATION);

            while (strengthKey.wasPressed())
                usePotion(client, StatusEffects.STRENGTH);

            while (speedKey.wasPressed())
                usePotion(client, StatusEffects.SPEED);
        });
    }

    private void usePotion(MinecraftClient client, StatusEffect effect) {

        if (client.player == null || client.interactionManager == null)
            return;

        PlayerInventory inv = client.player.getInventory();

        int potionSlot = -1;

        for (int i = 0; i < inv.size(); i++) {

            ItemStack stack = inv.getStack(i);

            if (!(stack.getItem() instanceof PotionItem
                    || stack.getItem() instanceof SplashPotionItem))
                continue;

            var effects = PotionUtil.getPotionEffects(stack);

            boolean matches = effects.stream()
                    .anyMatch(e -> e.getEffectType().value() == effect);

            if (matches) {
                potionSlot = i;
                break;
            }
        }

        if (potionSlot == -1)
            return;

        int previousSlot = inv.selectedSlot;

        float yaw = client.player.getYaw();
        float pitch = client.player.getPitch();

        inv.selectedSlot = potionSlot % 9;

        client.interactionManager.interactItem(
                client.player,
                Hand.MAIN_HAND
        );

        client.player.setYaw(yaw);
        client.player.setPitch(pitch);

        inv.selectedSlot = previousSlot;
    }
}

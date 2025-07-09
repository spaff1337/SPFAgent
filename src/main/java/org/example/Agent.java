package org.example;

import java.lang.instrument.Instrumentation;

public class Agent {
    public static void premain(String agentArgs, Instrumentation instrumentation) {
        System.out.println("[SPFAgent] Premain started");

        // TODO: Patch valid repair item check inside net.minecraft.world.item.ItemStack
        // TODO: Patch is fuel item check

        instrumentation.addTransformer(new EnchantmentsPatch());
    }
}
package org.example;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.LoaderClassPath;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

public class EnchantmentsPatch implements ClassFileTransformer {
    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        if (!"net/minecraft/world/item/enchantment/Enchantment".equals(className)) {
            return null;
        }

        try {
            ClassPool classPool = ClassPool.getDefault();
            classPool.appendClassPath(new LoaderClassPath(loader));

            CtClass ctClass = classPool.get("net.minecraft.world.item.enchantment.Enchantment");

            // CtMethod isPrimaryItemMethod = ctClass.getDeclaredMethod("isPrimaryItem");
            CtMethod isSupportedItemMethod = ctClass.getDeclaredMethod("isSupportedItem");
            CtMethod canEnchantMethod = ctClass.getDeclaredMethod("canEnchant");

            System.out.println("[SPFAgent] Transforming methods...");

            String isSupported =
                    "{" +
                        "org.bukkit.plugin.Plugin spfPlugin = org.bukkit.Bukkit.getPluginManager().getPlugin(\"SPFAdditions\");" +
                        "ClassLoader pluginClassLoader = spfPlugin.getClass().getClassLoader();" +

                        "Class spfItemClass = Class.forName(\"me.spaff.spfadditions.item.SPFItem\", false, pluginClassLoader);" +
                        "Class toolClass = Class.forName(\"me.spaff.spfadditions.item.component.tool.SPFTool\", false, pluginClassLoader);" +

                        "org.bukkit.inventory.ItemStack bukkitItem = org.bukkit.craftbukkit.inventory.CraftItemStack.asBukkitCopy($1);" +

                        "java.lang.reflect.Method isSpfItemMethod = spfItemClass.getMethod(\"isSPFItem\", new Class[] { org.bukkit.inventory.ItemStack.class });" +
                        "Object isSpfItem = isSpfItemMethod.invoke(null, new Object[] { bukkitItem });" +

                        "if (((Boolean) isSpfItem).booleanValue()) {" +
                            "java.lang.reflect.Method getAsSPFItemMethod = spfItemClass.getMethod(\"getAsSPFItem\", new Class[] { org.bukkit.inventory.ItemStack.class });" +
                            "Object spfItem = getAsSPFItemMethod.invoke(null, new Object[] { bukkitItem });" +

                            "java.lang.reflect.Method getComponentMethod = spfItemClass.getMethod(\"getComponent\", new Class[] { Class.class });" +
                            "Object optional = getComponentMethod.invoke(spfItem, new Object[] { toolClass });" +

                            "Class optionalClass = optional.getClass();" +
                            "java.lang.reflect.Method isPresentMethod = optionalClass.getMethod(\"isPresent\", new Class[0]);" +
                            "Boolean present = (Boolean) isPresentMethod.invoke(optional, new Object[0]);" +

                            "if (present.booleanValue()) {" +
                                "java.lang.reflect.Method getMethod = optionalClass.getMethod(\"get\", new Class[0]);" +
                                "Object tool = getMethod.invoke(optional, new Object[0]);" +

                                "java.lang.reflect.Method getSupportedMethod = tool.getClass().getMethod(\"getSupportedEnchantments\", new Class[0]);" +
                                "java.util.Collection supported = (java.util.Collection) getSupportedMethod.invoke(tool, new Object[0]);" +

                                "org.bukkit.enchantments.Enchantment enchantment = org.bukkit.craftbukkit.enchantments.CraftEnchantment.minecraftToBukkit(this);" +
                                "if (supported.contains(enchantment)) return true;" +
                            "}" +
                        "}" +
                    "}";

            // isSupportedItem
            isSupportedItemMethod.insertBefore(isSupported);

            // canEnchant
            canEnchantMethod.insertBefore(isSupported);

            return ctClass.toBytecode();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return null;

        // return ClassFileTransformer.super.transform(loader, className, classBeingRedefined, protectionDomain, classfileBuffer);
    }
}
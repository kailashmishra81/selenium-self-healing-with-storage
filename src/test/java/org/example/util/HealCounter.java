package org.example.util;

public class HealCounter {
    private static int aiHealCount = 0;
    private static int storageReuseCount = 0;

    public static synchronized void incrementAIHeal() {
        aiHealCount++;
    }

    public static synchronized void incrementStorageReuse() {
        storageReuseCount++;
    }


    public static void printStats() {
        System.out.println("   Locators healed through AI Calls      : " + aiHealCount);
        System.out.println("   Locators from Storage Reused     : " + storageReuseCount);
    }
}

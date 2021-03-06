package com.winthier.skills.bukkit;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.inventory.ItemStack;

class BukkitSkillBrew extends BukkitSkill implements Listener
{
    @lombok.Getter final BukkitSkillType skillType = BukkitSkillType.BREW;
    final double RADIUS = 40.0;

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBrew(BrewEvent event)
    {
        final Player player = getNearestPlayer(event.getContents().getHolder().getLocation(), RADIUS);
        if (player == null) return;
        if (!allowPlayer(player)) return;
        // Count potions for percentage
        int count = 0;
        ItemStack[] contents = event.getContents().getContents();
        for (int i = 0; i < Math.min(3, contents.length); ++i) {
            if (contents[i] != null) count += 1;
        }
        giveReward(player, rewardForItem(event.getContents().getIngredient()), (double)count / 3.0);
    }
}

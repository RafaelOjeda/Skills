package com.winthier.skills.bukkit;

import com.winthier.exploits.bukkit.BukkitExploits;
import lombok.Getter;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

class BukkitSkillHunt extends BukkitSkill implements Listener
{
    @Getter final BukkitSkillType skillType = BukkitSkillType.HUNT;
    long killDistanceInterval = 300;
    double minKillDistance = 16;

    @Override
    public void configure()
    {
        super.configure();
        killDistanceInterval = getConfig().getLong("KillDistanceInterval", 300);
        minKillDistance = getConfig().getDouble("MinKillDistance", 16);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event)
    {
        if (!(event.getDamager() instanceof Arrow)) return;
        Arrow arrow = (Arrow)event.getDamager();
        if (!(arrow.getShooter() instanceof Player)) return;
        Player player = (Player)arrow.getShooter();
        if (!allowPlayer(player)) return;
        LivingEntity entity = (LivingEntity)event.getEntity();
        if (BukkitExploits.getInstance().recentKillDistance(player, entity.getLocation(), killDistanceInterval) < minKillDistance) return;
        double percentage = BukkitExploits.getInstance().getEntityDamageByPlayerRemainderPercentage(entity, Math.min(entity.getHealth(), event.getFinalDamage()));
        giveReward(player, rewardForEntity(entity), percentage);
    }
}

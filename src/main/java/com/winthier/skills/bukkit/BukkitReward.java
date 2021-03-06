package com.winthier.skills.bukkit;

import com.winthier.skills.Reward;
import com.winthier.skills.sql.SQLReward;
import com.winthier.skills.util.Strings;
import java.util.Arrays;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.potion.PotionEffectType;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
class BukkitReward implements Reward
{
    @Value
    static class Key {
        @NonNull final BukkitSkillType skill;
        @NonNull final SQLReward.Target target;
        final Integer type;
        final Integer data;
        final String name;
        static Integer parseInt(String arg) {
            if (arg.equals("-")) return null;
            return Integer.parseInt(arg);
        }
        @SuppressWarnings("deprecation") static Integer parseType(SQLReward.Target target, String arg) {
            if (arg.equals("-")) return null;
            try {
                switch (target) {
                case BLOCK:
                case ITEM:
                    return Material.valueOf(arg.toUpperCase()).getId();
                case ENCHANTMENT:
                    return Enchantment.getByName(arg.toUpperCase()).getId();
                case POTION_EFFECT:
                    return PotionEffectType.getByName(arg.toUpperCase()).getId();
                }
            } catch (IllegalArgumentException iae) {}
            return Integer.parseInt(arg);
        }
        static String parseName(String arg) {
            if (arg.equals("-")) return null;
            return arg;
        }
        static Key parse(String[] tokens)
        {
            if (tokens.length != 5) throw new IllegalArgumentException("5 items required");
            String skillTypeArg = tokens[0];
            String targetArg = tokens[1];
            String typeArg = tokens[2];
            String dataArg = tokens[3];
            String nameArg = tokens[4];
            BukkitSkill skill = BukkitSkills.getInstance().skillByName(skillTypeArg);
            if (skill == null) throw new IllegalArgumentException("Skill not found: " + skillTypeArg);
            SQLReward.Target target = SQLReward.Target.valueOf(targetArg.toUpperCase());
            Integer type = parseType(target, typeArg);
            Integer data = parseInt(dataArg);
            String name = parseName(nameArg);
            return new Key(skill.getSkillType(), target, type, data, name);
        }
        static Key of(Reward reward) {
            if (reward instanceof SQLReward) {
                SQLReward sqlReward = (SQLReward)reward;
                return new Key(BukkitSkills.getInstance().skillByName(sqlReward.getSkill().getValue()).getSkillType(),
                               SQLReward.Target.valueOf(sqlReward.getTarget().getValue()),
                               sqlReward.getType(),
                               sqlReward.getData(),
                               sqlReward.getName() == null ? null : sqlReward.getName().getValue());
            }
            return null;
        }
        SQLReward findSQLReward() {
            return SQLReward.find(BukkitSkills.getInstance().skillByType(skill), target, type, data, name);
        }
        SQLReward makeSQLReward() {
            return SQLReward.of(BukkitSkills.getInstance().skillByType(skill), target, type, data, name);
        }
        BukkitReward find() {
            SQLReward sqlReward = findSQLReward();
            BukkitReward result = new BukkitReward(this);
            if (sqlReward != null) {
                result.skillPoints = sqlReward.getSkillPoints();
                result.money = sqlReward.getSkillPoints();
                result.exp = sqlReward.getExp();
            }
            return result;
        }
        @SuppressWarnings("deprecation")
        String typeAsPrettyString() {
            if (type == null) return "-";
            try {
                switch (target) {
                case BLOCK:
                case ITEM:
                    return Material.getMaterial(type).name();
                case ENCHANTMENT:
                    return Enchantment.getById(type).getName();
                case POTION_EFFECT:
                    return PotionEffectType.getById(type).getName();
                }
            } catch (IllegalArgumentException iae) {}
            return "?";
        }
        String typeAsString() {
            if (type == null) return "-";
            return "" + type;
        }
        String dataAsString() {
            if (data == null) return "-";
            return "" + data;
        }
        String nameAsString() {
            if (name == null) return "-";
            return name;
        }
        @Override public String toString() {
            return String.format("%s %s (%s) %s:%s {%s}", Strings.camelCase(skill.name()), Strings.camelCase(target.name()), typeAsPrettyString(), typeAsString(), dataAsString(), nameAsString());
        }
    }
    final Key key;
    float skillPoints = 0;
    float money = 0;
    float exp = 0;

    static BukkitReward parse(String[] tokens)
    {
        if (tokens.length != 8 && tokens.length != 9) throw new IllegalArgumentException("8 or 9 items required");
        String skillPointsArg = tokens[5];
        String moneyArg = tokens[6];
        String expArg = tokens[7];
        Key key = Key.parse(Arrays.copyOfRange(tokens, 0, 5));
        float skillPoints = Float.parseFloat(skillPointsArg);
        float money = Float.parseFloat(moneyArg);
        float exp = Float.parseFloat(expArg);
        if (tokens.length >= 9) {
            float factor = Float.parseFloat(tokens[8]);
            skillPoints *= factor;
            money *= factor;
            exp *= factor;
        }
        return new BukkitReward(key, skillPoints, money, exp);
    }

    static BukkitReward of(Reward reward)
    {
        Key key = Key.of(reward);
        BukkitReward result = new BukkitReward(key);
        result.skillPoints = reward.getSkillPoints();
        result.money = reward.getMoney();
        result.exp = reward.getExp();
        return result;
    }

    void store()
    {
        SQLReward sqlReward = key.makeSQLReward();
        sqlReward.setSkillPoints(skillPoints);
        sqlReward.setMoney(money);
        sqlReward.setExp(exp);
        sqlReward.save();
    }
    @Override
    public String toString()
    {
        return String.format("%s %.2f %.2f %.2f", key, skillPoints, money, exp);
    }
}

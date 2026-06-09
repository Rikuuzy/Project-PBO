import java.util.Random;

public class StatusSkill extends Skill {
    private StatusEffect.Type effectType;
    private int duration;
    private boolean targetsSelf;

    public StatusSkill(String name, String desc, int cost, int damage, String type, StatusEffect.Type effectType, int duration, boolean targetsSelf) {
        super(name, desc, cost, damage, type);
        this.effectType = effectType;
        this.duration = duration;
        this.targetsSelf = targetsSelf;
    }

    @Override
    public void use(Character caster, Character target, BattleSystem system) {
        Character mainTarget = targetsSelf ? caster : target;
        Random rand = new Random();

        // 1. Jika target memiliki status EVADE, hindari serangan ofensif dari lawan
        if (!targetsSelf && mainTarget.hasStatusEffect(StatusEffect.Type.EVADE)) {
            system.log("💨 " + mainTarget.getName() + " menghindari skill " + getName() + " dari " + caster.getName() + "!", "log-info");
            mainTarget.removeStatusEffect(StatusEffect.Type.EVADE);
            return;
        }

        // 2. Kalkulasi logika khusus untuk skill tertentu
        if (getType().equals("execute")) {
            // Death Mark: jika HP musuh < 20%, langsung K.O.
            double hpPercent = (double) target.getHp() / target.getFullHp();
            if (hpPercent < 0.20) {
                int dmg = target.getHp();
                target.setHp(0);
                system.recordDamageDealt(caster, dmg);
                system.log("💀 " + caster.getName() + " menggunakan " + getName() + " — Mengeksekusi instan " + target.getName() + "!", "log-crit");
            } else {
                // Serangan biasa jika HP musuh masih di atas 20%
                int dmg = (caster.getAttack() + 40) - (int)(target.getDefence() * 0.4);
                dmg += rand.nextInt(7) - 3;
                if (dmg < 1) dmg = 1;
                target.setHp(target.getHp() - dmg);
                system.recordDamageDealt(caster, dmg);
                system.log("🔮 " + caster.getName() + " menggunakan " + getName() + " — menghasilkan " + dmg + " damage ke " + target.getName() + " (HP target belum di bawah 20%)", "log-skill");
            }
            return;
        }

        if (getType().equals("berserk")) {
            // Berserk: mengorbankan 10 HP caster (HP tidak boleh kurang dari 1)
            int hpCost = 10;
            int newHp = Math.max(1, caster.getHp() - hpCost);
            caster.setHp(newHp);
            caster.addStatusEffect(StatusEffect.Type.BERSERK, duration);
            system.log("💢 " + caster.getName() + " menggunakan " + getName() + " — mengorbankan " + hpCost + " HP dan mengaktifkan Berserk!", "log-skill");
            return;
        }

        // 3. Skill status biasa yang juga menghasilkan damage (contoh: Shield Bash, Fireball, Poison Blade)
        int finalDmg = 0;
        if (getDamage() > 0) {
            int baseAtk = caster.getAttack();
            if (caster.hasStatusEffect(StatusEffect.Type.BUFF)) {
                baseAtk += (int) (caster.getAttack() * 0.3);
            }
            if (caster.hasStatusEffect(StatusEffect.Type.BERSERK)) {
                baseAtk += (int) (caster.getAttack() * 0.5);
            }
            finalDmg = (baseAtk + getDamage()) - (int)(mainTarget.getDefence() * 0.4);
            finalDmg += rand.nextInt(7) - 3;
            if (finalDmg < 1) finalDmg = 1;
            mainTarget.setHp(mainTarget.getHp() - finalDmg);
            system.recordDamageDealt(caster, finalDmg);
        }

        // Tambahkan efek status ke target
        mainTarget.addStatusEffect(effectType, duration);

        String logMsg = "";
        String style = "log-skill";
        if (effectType == StatusEffect.Type.STUN) {
            logMsg = "⚡ " + caster.getName() + " menggunakan " + getName() + (finalDmg > 0 ? " — mengenai " + finalDmg + " damage dan" : " —") + " memberikan efek Stun pada " + mainTarget.getName() + " selama " + duration + " ronde!";
        } else if (effectType == StatusEffect.Type.BURN) {
            logMsg = "🔥 " + caster.getName() + " menggunakan " + getName() + (finalDmg > 0 ? " — mengenai " + finalDmg + " damage dan" : " —") + " membakar " + mainTarget.getName() + " selama " + duration + " ronde!";
        } else if (effectType == StatusEffect.Type.POISON) {
            logMsg = "☠ " + caster.getName() + " menggunakan " + getName() + (finalDmg > 0 ? " — mengenai " + finalDmg + " damage dan" : " —") + " meracuni " + mainTarget.getName() + " selama " + duration + " ronde!";
        } else if (effectType == StatusEffect.Type.EVADE) {
            logMsg = "💨 " + caster.getName() + " menggunakan " + getName() + " — bersiap menghindari serangan musuh berikutnya!";
            style = "log-info";
        }

        system.log(logMsg, style);
    }
}

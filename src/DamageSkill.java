import java.util.Random;

public class DamageSkill extends Skill {
    private boolean isCritChanceIncreased;

    public DamageSkill(String name, String desc, int cost, int damage, String type, boolean isCritChanceIncreased) {
        super(name, desc, cost, damage, type);
        this.isCritChanceIncreased = isCritChanceIncreased;
    }

    @Override
    public void use(Character caster, Character target, BattleSystem system) {
        Random rand = new Random();
        double critChance = isCritChanceIncreased ? 0.80 : 0.15;
        boolean isCrit = rand.nextDouble() < critChance;
        
        int baseAtk = caster.getAttack();
        // Efek Buff: jika caster memiliki Buff ATK, +30% attack. Jika Berserk, +50% attack.
        if (caster.hasStatusEffect(StatusEffect.Type.BUFF)) {
            baseAtk += (int) (caster.getAttack() * 0.3);
        }
        if (caster.hasStatusEffect(StatusEffect.Type.BERSERK)) {
            baseAtk += (int) (caster.getAttack() * 0.5);
        }

        // Formula Damage: (baseAtk + skillDmg)*multiplier - targetDef*0.4 + random(-3 to 3)
        double multiplier = isCrit ? 1.8 : 1.0;
        int rawDmg = (int) ((getDamage()) * multiplier); // Skill damage acts as the skill's base power
        int finalDmg = (baseAtk + rawDmg) - (int)(target.getDefence() * 0.4);
        finalDmg += rand.nextInt(7) - 3; // nilai acak antara -3 sampai 3
        if (finalDmg < 1) finalDmg = 1;

        // Jika target memiliki status EVADE, hindari serangan
        if (target.hasStatusEffect(StatusEffect.Type.EVADE)) {
            system.log("💨 " + target.getName() + " menghindari serangan skill dari " + caster.getName() + "!", "log-info");
            target.removeStatusEffect(StatusEffect.Type.EVADE);
            return;
        }

        // Kurangi HP target
        target.setHp(target.getHp() - finalDmg);
        system.recordDamageDealt(caster, finalDmg);

        if (isCrit) {
            system.log("⚡ KRITIKAL! " + caster.getName() + " menggunakan " + getName() + " — mengenai " + target.getName() + " sebesar " + finalDmg + " damage!", "log-crit");
        } else {
            system.log("🔮 " + caster.getName() + " menggunakan " + getName() + " ke " + target.getName() + " — menghasilkan " + finalDmg + " damage!", "log-skill");
        }
    }
}

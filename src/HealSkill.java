public class HealSkill extends Skill {
    public HealSkill(String name, String desc, int cost, int healAmount) {
        super(name, desc, cost, healAmount, "heal");
    }

    @Override
    public void use(Character caster, Character target, BattleSystem system) {
        // Efek penyembuhan biasanya diaplikasikan ke caster itu sendiri
        int healVal = Math.abs(getDamage());
        int currentHp = caster.getHp();
        int maxHp = caster.getFullHp();
        int finalHeal = Math.min(healVal, maxHp - currentHp);
        
        caster.setHp(currentHp + finalHeal);
        
        system.log("✨ " + caster.getName() + " menggunakan " + getName() + " — memulihkan " + finalHeal + " HP!", "log-heal");
    }
}

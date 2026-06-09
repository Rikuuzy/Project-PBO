public class BuffSkill extends Skill {
    private StatusEffect.Type buffType;
    private int duration;

    public BuffSkill(String name, String desc, int cost, String type, StatusEffect.Type buffType, int duration) {
        super(name, desc, cost, 0, type);
        this.buffType = buffType;
        this.duration = duration;
    }

    @Override
    public void use(Character caster, Character target, BattleSystem system) {
        // Buff diaplikasikan kepada diri sendiri (caster)
        caster.addStatusEffect(buffType, duration);
        system.log("💪 " + caster.getName() + " menggunakan " + getName() + " — mendapatkan efek " + buffType.toString() + "!", "log-skill");
    }
}

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Enemy extends Character {
    private String icon;
    private String difficulty; // "Mudah", "Sedang", "Sulit"
    private List<Skill> skills;

    public Enemy(String name, int hp, int mp, int atk, int def, int spd, String icon, String difficulty) {
        super(name, hp, mp, atk, def, spd);
        this.icon = icon;
        this.difficulty = difficulty;
        this.skills = new ArrayList<>();
    }

    public String getIcon() { 
        return icon; 
    }
    
    public String getDifficulty() { 
        return difficulty; 
    }
    
    public List<Skill> getSkills() { 
        return skills; 
    }

    public void addSkill(Skill skill) {
        skills.add(skill);
    }

    @Override
    public void attack(Character target) {
        BattleSystem system = BattleSystem.getInstance();
        Random rand = new Random();
        
        int baseAtk = getAttack();
        // Efek buff musuh jika ada (opsional, tapi untuk konsistensi jika musuh mendapat buff)
        if (hasStatusEffect(StatusEffect.Type.BUFF)) {
            baseAtk += (int) (getAttack() * 0.3);
        }
        
        // Hitung bonus pertahanan target (Hero) jika sedang dalam posisi bertahan
        double defBonus = target.isDefending() ? target.getDefence() * 1.5 : target.getDefence();
        int finalDmg = baseAtk - (int)(defBonus * 0.4);
        finalDmg += rand.nextInt(7) - 3; // Acak antara -3 sampai 3
        if (finalDmg < 1) finalDmg = 1;

        // Cek status EVADE pada target
        if (target.hasStatusEffect(StatusEffect.Type.EVADE)) {
            system.log("💨 " + target.getName() + " menghindari serangan dari " + getName() + "!", "log-info");
            target.removeStatusEffect(StatusEffect.Type.EVADE);
            return;
        }

        // Terapkan damage
        target.setHp(target.getHp() - finalDmg);
        system.recordDamageDealt(this, finalDmg);
        system.log("🧟 " + getName() + " menyerang " + target.getName() + " — menghasilkan " + finalDmg + " damage", "log-enemy");
    }

    @Override
    public void takeTurn(BattleSystem battle) {
        BattleSystem system = BattleSystem.getInstance();
        Random rand = new Random();
        Hero hero = system.getHero();

        // 1. Cek apakah musuh ter-stun
        if (hasStatusEffect(StatusEffect.Type.STUN)) {
            system.log("⚡ " + getName() + " terstun dan tidak dapat bergerak ronde ini!", "log-info");
            removeStatusEffect(StatusEffect.Type.STUN);
            system.endEnemyTurn();
            return;
        }

        // 2. Aplikasikan efek damage-over-time (Racun / Terbakar)
        applyDamageOverTimeEffects(system);
        if (!isAlive()) {
            system.checkBattleOver();
            return;
        }

        // 3. Logika AI: Menggunakan skill jika MP cukup (peluang 35%)
        boolean useSkill = getMp() >= 8 && rand.nextDouble() < 0.35 && !skills.isEmpty();

        if (useSkill) {
            // Pilih salah satu skill musuh secara acak
            Skill selectedSkill = skills.get(rand.nextInt(skills.size()));
            if (getMp() >= selectedSkill.getCost()) {
                setMp(getMp() - selectedSkill.getCost());
                selectedSkill.use(this, hero, system);
                system.endEnemyTurn();
                return;
            }
        }

        // 4. Serangan biasa jika tidak pakai skill atau MP kurang
        attack(hero);
        system.endEnemyTurn();
    }

    private void applyDamageOverTimeEffects(BattleSystem system) {
        if (hasStatusEffect(StatusEffect.Type.POISON)) {
            int poisonDmg = 8;
            setHp(getHp() - poisonDmg);
            system.log("☠ " + getName() + " terkena damage racun berkala sebesar " + poisonDmg + "!", "log-enemy");
            decrementStatusDuration(StatusEffect.Type.POISON);
        }
        if (hasStatusEffect(StatusEffect.Type.BURN)) {
            int burnDmg = 12;
            setHp(getHp() - burnDmg);
            system.log("🔥 " + getName() + " terkena damage terbakar berkala sebesar " + burnDmg + "!", "log-enemy");
            decrementStatusDuration(StatusEffect.Type.BURN);
        }
    }

    private void decrementStatusDuration(StatusEffect.Type type) {
        for (StatusEffect effect : getStatusEffects()) {
            if (effect.getType() == type) {
                effect.decrementDuration();
            }
        }
        getStatusEffects().removeIf(effect -> effect.getType() == type && effect.getDuration() <= 0);
    }
}

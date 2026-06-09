import java.util.ArrayList;
import java.util.List;

public class Job {
    private String name;
    private int baseHp;
    private int baseMp;
    private int baseAtk;
    private int baseDef;
    private int baseSpd;
    private List<Skill> skills;

    public Job(String name, int baseHp, int baseMp, int baseAtk, int baseDef, int baseSpd) {
        this.name = name;
        this.baseHp = baseHp;
        this.baseMp = baseMp;
        this.baseAtk = baseAtk;
        this.baseDef = baseDef;
        this.baseSpd = baseSpd;
        this.skills = new ArrayList<>();
    }

    public String getName() { 
        return name; 
    }
    
    public int getBaseHp() { 
        return baseHp; 
    }
    
    public int getBaseMp() { 
        return baseMp; 
    }
    
    public int getBaseAtk() { 
        return baseAtk; 
    }
    
    public int getBaseDef() { 
        return baseDef; 
    }
    
    public int getBaseSpd() { 
        return baseSpd; 
    }
    
    public List<Skill> getSkills() { 
        return skills; 
    }

    public void addSkill(Skill skill) {
        skills.add(skill);
    }

    // Factory Methods untuk membuat instansi Job tertentu
    public static Job createWarrior() {
        Job warrior = new Job("Warrior", 120, 40, 22, 14, 8);
        warrior.addSkill(new DamageSkill("Slash Badai", "Serangan fisik beruntun (Damage: 28)", 10, 28, "atk", false));
        warrior.addSkill(new BuffSkill("Battle Cry", "Tingkatkan serangan (ATK +30%) selama 2 ronde", 8, "buff", StatusEffect.Type.BUFF, 2));
        warrior.addSkill(new StatusSkill("Shield Bash", "Serang musuh (Damage: 20) dan berikan efek Stun selama 1 ronde", 12, 20, "stun", StatusEffect.Type.STUN, 1, false));
        warrior.addSkill(new StatusSkill("Berserk", "Korbankan 10 HP, tingkatkan serangan (ATK +50%) selama 3 ronde", 15, 0, "berserk", StatusEffect.Type.BERSERK, 3, true));
        return warrior;
    }

    public static Job createMage() {
        Job mage = new Job("Mage", 80, 100, 30, 6, 12);
        mage.addSkill(new StatusSkill("Fireball", "Damage api tinggi (Damage: 40) + Terbakar selama 2 ronde", 15, 40, "burn", StatusEffect.Type.BURN, 2, false));
        mage.addSkill(new DamageSkill("Blizzard", "Damage badai salju es (Damage: 35) ke musuh", 20, 35, "atk", false));
        mage.addSkill(new DamageSkill("Arcane Bolt", "Damage sihir murni yang sangat kuat (Damage: 50)", 10, 50, "atk", false));
        mage.addSkill(new HealSkill("Heal", "Gunakan sihir suci untuk memulihkan HP sebesar 30 poin", 12, 30));
        return mage;
    }

    public static Job createRogue() {
        Job rogue = new Job("Rogue", 95, 60, 26, 9, 16);
        rogue.addSkill(new DamageSkill("Backstab", "Serang titik lemah (Peluang Kritikal 80%, Damage: 45)", 8, 45, "crit", true));
        rogue.addSkill(new StatusSkill("Poison Blade", "Serang musuh (Damage: 15) + Racun selama 3 ronde", 10, 15, "poison", StatusEffect.Type.POISON, 3, false));
        rogue.addSkill(new StatusSkill("Shadow Step", "Sembunyi di bayangan untuk menghindari 1 serangan berikutnya", 12, 0, "evade", StatusEffect.Type.EVADE, 1, true));
        rogue.addSkill(new StatusSkill("Death Mark", "Eksekusi instan musuh (Damage: K.O.) jika HP target < 20%", 20, 999, "execute", StatusEffect.Type.STUN, 0, false));
        return rogue;
    }
}

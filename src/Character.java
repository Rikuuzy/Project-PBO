import java.util.ArrayList;
import java.util.List;

public abstract class Character {
    private String name;
    private int full_hp; /* Kapasitas maksimal health point */
    private int hp; /* Kapasitas health point saat ini */
    private int full_mp; /* kapasitas maksimal magic point / mana point*/
    private int mp; /* Kapasitas magic point saat ini */
    private int attack; /* Kekuatan serangan fisik */
    private int defence; /* Kemampuan defence dia */
    private int speed; /* Kecepatan untuk giliran bertarung */
    private boolean defending; /* Apakah sedang dalam posisi bertahan */
    private List<StatusEffect> statusEffects; /* Daftar efek status aktif */

    public Character(String name, int full_hp, int full_mp, int attack, int defence, int speed) {
        this.name = name;
        this.full_hp = full_hp;
        this.hp = full_hp;
        this.full_mp = full_mp;
        this.mp = full_mp;
        this.attack = attack;
        this.defence = defence;
        this.speed = speed;
        this.defending = false;
        this.statusEffects = new ArrayList<>();
    }

    public Character(){
        this.name = "";
        this.full_hp = 0;
        this.hp = 0;
        this.full_mp = 0;
        this.mp = 0;
        this.attack = 0;
        this.defence = 0;
        this.speed = 0;
        this.defending = false;
        this.statusEffects = new ArrayList<>();
    }

    /* GETTER */

    public String getName(){ 
        return name; 
    }

    public int getHp(){ 
        return hp; 
    }

    public int getFullHp(){ 
        return full_hp; 
    }
    
    public int getMp() { 
        return mp;
    }

    public int getFullMp(){
        return full_mp;
    }

    public int getAttack() {
        return attack;
    }

    public int getDefence(){
        return defence;
    }

    public int getSpeed() {
        return speed;
    }

    public boolean isDefending() {
        return defending;
    }

    public List<StatusEffect> getStatusEffects() {
        return statusEffects;
    }

    /* Setter */

    public void setHp(int hp) {
        if (hp < 0) {
            this.hp = 0;
        }
        else if (hp > full_hp) {
            this.hp = full_hp;
        }
        else {
            this.hp = hp;
        }
    }

    public void setMp(int mp) {
        if (mp < 0) {
            this.mp = 0;
        }
        else if (mp > full_mp) {
            this.mp = full_mp;
        }
        else {
            this.mp = mp;
        }
    }

    public void setDefending(boolean defending) {
        this.defending = defending;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    /* Status Effect Helpers */

    public void addStatusEffect(StatusEffect.Type type, int duration) {
        for (StatusEffect effect : statusEffects) {
            if (effect.getType() == type) {
                // Perbarui durasi jika durasi baru lebih lama
                if (duration > effect.getDuration()) {
                    effect.setDuration(duration);
                }
                return;
            }
        }
        statusEffects.add(new StatusEffect(type, duration));
    }

    public boolean hasStatusEffect(StatusEffect.Type type) {
        for (StatusEffect effect : statusEffects) {
            if (effect.getType() == type) {
                return true;
            }
        }
        return false;
    }

    public void removeStatusEffect(StatusEffect.Type type) {
        statusEffects.removeIf(effect -> effect.getType() == type);
    }

    public boolean isAlive(){
        return hp > 0;
    }

    public abstract void attack(Character target);
    public abstract void takeTurn(BattleSystem battle);
}
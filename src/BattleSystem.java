import java.util.ArrayList;
import java.util.List;

public class BattleSystem {
    private static BattleSystem instance;

    private Hero hero;
    private Enemy enemy;
    private int turnCount = 0;
    private int heroDamageDealt = 0;
    private int enemyDamageDealt = 0;
    private boolean isBattleActive = false;
    private boolean isHeroTurn = true;

    // Interface untuk berkomunikasi dengan GameGUI tanpa keterikatan erat (loose coupling)
    public interface BattleListener {
        void onLogAdded(String message, String styleClass);
        void onBattleUpdated();
        void onBattleOver(boolean heroWon, int turns, int heroDmg, int enemyDmg, int remainingHp, int maxHp);
    }

    private List<BattleListener> listeners = new ArrayList<>();

    public BattleSystem(Hero hero, Enemy enemy, double difficultyMultiplier) {
        this.hero = hero;
        this.enemy = enemy;
        instance = this;
    }

    public static BattleSystem getInstance() {
        return instance;
    }

    public void addListener(BattleListener listener) {
        listeners.add(listener);
    }

    public Hero getHero() { 
        return hero; 
    }
    
    public Enemy getEnemy() { 
        return enemy; 
    }
    
    public int getTurnCount() { 
        return turnCount; 
    }
    
    public boolean isBattleActive() { 
        return isBattleActive; 
    }
    
    public boolean isHeroTurn() { 
        return isHeroTurn; 
    }

    public void startBattle() {
        isBattleActive = true;
        turnCount = 1;
        heroDamageDealt = 0;
        enemyDamageDealt = 0;
        isHeroTurn = true;

        log("⚔ Battle dimulai! " + hero.getName() + " vs " + enemy.getName(), "log-info");
        log("Giliran ke-1 dimulai. Pilih aksimu!", "log-info");
        updateUpdates();
    }

    public void log(String message, String styleClass) {
        for (BattleListener l : listeners) {
            l.onLogAdded(message, styleClass);
        }
    }

    public void updateUpdates() {
        for (BattleListener l : listeners) {
            l.onBattleUpdated();
        }
    }

    public void recordDamageDealt(Character caster, int damage) {
        if (caster instanceof Hero) {
            heroDamageDealt += damage;
        } else if (caster instanceof Enemy) {
            enemyDamageDealt += damage;
        }
    }

    public void executeHeroAttack() {
        if (!isBattleActive || !isHeroTurn) return;
        
        hero.setDefending(false);
        hero.attack(enemy);
        
        decrementHeroStatusEffects();
        updateUpdates();

        if (checkBattleOver()) return;

        isHeroTurn = false;
        // Menjalankan giliran musuh dengan delay kecil di thread terpisah agar GUI tidak hang
        new Thread(() -> {
            try {
                Thread.sleep(900);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            executeEnemyTurn();
        }).start();
    }

    public void executeHeroSkill(Skill skill) {
        if (!isBattleActive || !isHeroTurn) return;
        if (hero.getMp() < skill.getCost()) {
            log("❌ MP tidak cukup untuk menggunakan skill ini!", "log-info");
            return;
        }

        hero.setMp(hero.getMp() - skill.getCost());
        hero.setDefending(false);

        skill.use(hero, enemy, this);
        
        decrementHeroStatusEffects();
        updateUpdates();

        if (checkBattleOver()) return;

        isHeroTurn = false;
        new Thread(() -> {
            try {
                Thread.sleep(900);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            executeEnemyTurn();
        }).start();
    }

    public void executeHeroItem(Item item) {
        if (!isBattleActive || !isHeroTurn) return;
        if (item.getQuantity() <= 0) return;

        hero.setDefending(false);
        item.use(hero, enemy, this);
        
        decrementHeroStatusEffects();
        updateUpdates();

        if (checkBattleOver()) return;

        isHeroTurn = false;
        new Thread(() -> {
            try {
                Thread.sleep(900);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            executeEnemyTurn();
        }).start();
    }

    public void executeHeroDefend() {
        if (!isBattleActive || !isHeroTurn) return;

        hero.setDefending(true);
        log("🛡 " + hero.getName() + " mengambil posisi bertahan — DEF meningkat ronde ini!", "log-info");
        
        decrementHeroStatusEffects();
        updateUpdates();

        isHeroTurn = false;
        new Thread(() -> {
            try {
                Thread.sleep(900);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            executeEnemyTurn();
        }).start();
    }

    private void decrementHeroStatusEffects() {
        for (StatusEffect effect : hero.getStatusEffects()) {
            effect.decrementDuration();
        }
        hero.getStatusEffects().removeIf(effect -> effect.getDuration() <= 0);
    }

    private void executeEnemyTurn() {
        if (!isBattleActive) return;
        
        // Panggil takeTurn secara polimorfis pada musuh
        enemy.takeTurn(this);
    }

    public void startHeroTurn() {
        isHeroTurn = true;
        turnCount++;
        
        // Cek efek racun/terbakar di awal giliran hero
        applyHeroDamageOverTimeEffects();
        updateUpdates();
        
        if (checkBattleOver()) return;
        
        log("— Giliranmu (Ronde " + turnCount + ") —", "log-info");
        updateUpdates();
    }

    private void applyHeroDamageOverTimeEffects() {
        if (hero.hasStatusEffect(StatusEffect.Type.POISON)) {
            int poisonDmg = 8;
            hero.setHp(hero.getHp() - poisonDmg);
            log("☠ " + hero.getName() + " terkena damage racun berkala sebesar " + poisonDmg + "!", "log-enemy");
            
            for (StatusEffect effect : hero.getStatusEffects()) {
                if (effect.getType() == StatusEffect.Type.POISON) {
                    effect.decrementDuration();
                }
            }
            hero.getStatusEffects().removeIf(e -> e.getType() == StatusEffect.Type.POISON && e.getDuration() <= 0);
        }
        if (hero.hasStatusEffect(StatusEffect.Type.BURN)) {
            int burnDmg = 12;
            hero.setHp(hero.getHp() - burnDmg);
            log("🔥 " + hero.getName() + " terkena damage terbakar berkala sebesar " + burnDmg + "!", "log-enemy");
            
            for (StatusEffect effect : hero.getStatusEffects()) {
                if (effect.getType() == StatusEffect.Type.BURN) {
                    effect.decrementDuration();
                }
            }
            hero.getStatusEffects().removeIf(e -> e.getType() == StatusEffect.Type.BURN && e.getDuration() <= 0);
        }
    }

    public void endEnemyTurn() {
        if (!isBattleActive) return;
        
        if (checkBattleOver()) return;
        
        // Masuk giliran hero kembali
        startHeroTurn();
    }

    public boolean checkBattleOver() {
        if (!hero.isAlive()) {
            isBattleActive = false;
            log("💀 " + hero.getName() + " tumbang dalam pertempuran...", "log-enemy");
            for (BattleListener l : listeners) {
                l.onBattleOver(false, turnCount, heroDamageDealt, enemyDamageDealt, hero.getHp(), hero.getFullHp());
            }
            return true;
        } else if (!enemy.isAlive()) {
            isBattleActive = false;
            log("🏆 " + enemy.getName() + " berhasil dikalahkan!", "log-info");
            for (BattleListener l : listeners) {
                l.onBattleOver(true, turnCount, heroDamageDealt, enemyDamageDealt, hero.getHp(), hero.getFullHp());
            }
            return true;
        }
        return false;
    }
}

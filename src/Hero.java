import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Hero extends Character {
    private Job job;
    private String icon;
    private List<Item> inventory;

    public Hero(String name, Job job, String icon) {
        // Stats awal diambil dari Job yang dipilih
        super(name, job.getBaseHp(), job.getBaseMp(), job.getBaseAtk(), job.getBaseDef(), job.getBaseSpd());
        this.job = job;
        this.icon = icon;
        this.inventory = new ArrayList<>();

        // Inisialisasi inventaris default sesuai prototipe
        inventory.add(new Item("hpot", "Health Potion", "🧪", "Pulihkan 40 HP", 2, "heal", 40));
        inventory.add(new Item("mpot", "Mana Elixir", "💧", "Pulihkan 30 MP", 1, "mana", 30));
        inventory.add(new Item("bomb", "Fire Bomb", "💣", "Bakar musuh 35 damage", 1, "bomb", 35));
    }

    public Job getJob() {
        return job;
    }

    public String getIcon() {
        return icon;
    }

    public List<Item> getInventory() {
        return inventory;
    }

    @Override
    public void attack(Character target) {
        BattleSystem system = BattleSystem.getInstance();
        Random rand = new Random();
        boolean isCrit = rand.nextDouble() < 0.15; // 15% peluang kritikal biasa

        int baseAtk = getAttack();
        // Cek status efek buff/berserk
        if (hasStatusEffect(StatusEffect.Type.BUFF)) {
            baseAtk += (int) (getAttack() * 0.3);
        }
        if (hasStatusEffect(StatusEffect.Type.BERSERK)) {
            baseAtk += (int) (getAttack() * 0.5);
        }

        double multiplier = isCrit ? 1.8 : 1.0;
        int rawDmg = (int) (baseAtk * multiplier);
        int finalDmg = rawDmg - (int) (target.getDefence() * 0.4);
        finalDmg += rand.nextInt(7) - 3; // Variasi acak -3 sampai +3
        if (finalDmg < 1)
            finalDmg = 1;

        // Cek jika target memiliki status EVADE
        if (target.hasStatusEffect(StatusEffect.Type.EVADE)) {
            system.log("💨 " + target.getName() + " menghindari serangan dari " + getName() + "!", "log-info");
            target.removeStatusEffect(StatusEffect.Type.EVADE);
            return;
        }

        // Terapkan damage
        target.setHp(target.getHp() - finalDmg);
        system.recordDamageDealt(this, finalDmg);

        if (isCrit) {
            system.log("⚡ KRITIKAL! " + getName() + " menyerang " + target.getName() + " — menghasilkan " + finalDmg
                    + " damage!", "log-crit");
        } else {
            system.log("⚔️ " + getName() + " menyerang " + target.getName() + " — menghasilkan " + finalDmg + " damage",
                    "log-attack");
        }
    }

    @Override
    public void takeTurn(BattleSystem battle) {
        // Mengaktifkan panel tombol aksi di GUI untuk giliran Hero (pemain)
        battle.startHeroTurn();
    }
}

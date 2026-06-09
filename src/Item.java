import java.util.Random;

public class Item {
    private String id;
    private String name;
    private String icon;
    private String description;
    private int quantity;
    private String type; // "heal", "mana", "bomb"
    private int value;

    public Item(String id, String name, String icon, String description, int quantity, String type, int value) {
        this.id = id;
        this.name = name;
        this.icon = icon;
        this.description = description;
        this.quantity = quantity;
        this.type = type;
        this.value = value;
    }

    public String getId() { 
        return id; 
    }
    
    public String getName() { 
        return name; 
    }
    
    public String getIcon() { 
        return icon; 
    }
    
    public String getDescription() { 
        return description; 
    }
    
    public int getQuantity() { 
        return quantity; 
    }
    
    public void setQuantity(int quantity) { 
        this.quantity = quantity; 
    }
    
    public String getType() { 
        return type; 
    }
    
    public int getValue() { 
        return value; 
    }

    public void use(Character user, Character enemy, BattleSystem system) {
        if (quantity <= 0) return;
        
        quantity--;
        
        if (type.equals("heal")) {
            int healVal = Math.min(value, user.getFullHp() - user.getHp());
            user.setHp(user.getHp() + healVal);
            system.log("🧪 " + user.getName() + " menggunakan " + name + " — memulihkan " + healVal + " HP!", "log-heal");
        } else if (type.equals("mana")) {
            int manaVal = Math.min(value, user.getFullMp() - user.getMp());
            user.setMp(user.getMp() + manaVal);
            system.log("💧 " + user.getName() + " menggunakan " + name + " — memulihkan " + manaVal + " MP!", "log-heal");
        } else if (type.equals("bomb")) {
            Random rand = new Random();
            int dmg = value - (int)(enemy.getDefence() * 0.4);
            dmg += rand.nextInt(7) - 3;
            if (dmg < 1) dmg = 1;
            
            // Periksa jika target memiliki status EVADE
            if (enemy.hasStatusEffect(StatusEffect.Type.EVADE)) {
                system.log("💨 " + enemy.getName() + " menghindari lemparan bom dari " + user.getName() + "!", "log-info");
                enemy.removeStatusEffect(StatusEffect.Type.EVADE);
                return;
            }
            
            enemy.setHp(enemy.getHp() - dmg);
            system.recordDamageDealt(user, dmg);
            system.log("💣 " + user.getName() + " melempar " + name + " ke " + enemy.getName() + " — menghasilkan " + dmg + " damage!", "log-attack");
        }
    }
}


public abstract class Skill {
    private String name;
    private String description; /*Deskripsi kegunaan skill */
    private int cost;
    private int damage;
    private String type; 

    public Skill(String name, String desc, int cost, int damage, String type) {
        this.name = name;
        this.description = desc;
        this.cost = cost;
        this.damage = damage;
        this.type = type;
    }

    public Skill() {
        this.name = "";
        this.description = "";
        this.cost = 0;
        this.damage = 0;
        this.type = "";
    }

    // Getter
    public String getName(){
        return name;
    }
    public String getDesc(){
        return description;
    }

    public int getCost(){
        return cost;
    }

    public int getDamage(){
        return damage;
    }

    public String getType(){
        return type;
    }

    // Metode polimorfis untuk menggunakan skill
    public abstract void use(Character caster, Character target, BattleSystem system);
}
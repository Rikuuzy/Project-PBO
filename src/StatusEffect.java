public class StatusEffect {
    public enum Type {
        POISON, BURN, STUN, EVADE, BUFF, BERSERK
    }
    
    private Type type;
    private int duration; // durasi dalam hitungan giliran (turn)
    
    public StatusEffect(Type type, int duration) {
        this.type = type;
        this.duration = duration;
    }
    
    public Type getType() {
        return type;
    }
    
    public int getDuration() {
        return duration;
    }
    
    public void setDuration(int duration) {
        this.duration = duration;
    }
    
    public void decrementDuration() {
        this.duration--;
    }
    
    public String getName() {
        switch(type) {
            case POISON: return "Racun";
            case BURN: return "Terbakar";
            case STUN: return "Stun";
            case EVADE: return "Evade";
            case BUFF: return "Buff ATK";
            case BERSERK: return "Berserk";
            default: return "";
        }
    }

    @Override
    public String toString() {
        return getName() + " (" + duration + " giliran)";
    }
}

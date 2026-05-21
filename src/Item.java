public class Item {

    private final String name;
    private final String type;
    private final int effectValue;
    private final int quantity;

    public Item(String name, String type, int effectValue, int quantity) {
        this.name = name;
        this.type = type;
        this.effectValue = effectValue;
        this.quantity = quantity;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public int getEffectValue() {
        return effectValue;
    }

    public int getQuantity() {
        return quantity;
    }

    public boolean isConsumable() {
        return quantity > 0;
    }

    @Override
    public String toString() {
        return name + " x" + quantity + " (Effect: +" + effectValue + ")";
    }
}

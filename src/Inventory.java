import java.util.ArrayList;
import java.util.List;

public class Inventory {

    private final List<Item> items = new ArrayList<>();

    public Inventory() {
        items.add(new Item("Health Potion", "heal", 25, 3));
        items.add(new Item("Mega Potion", "heal", 50, 1));
        items.add(new Item("Attack Booster", "attack_boost", 15, 2));
        items.add(new Item("Defense Shield", "defense_boost", 10, 2));
    }

    public void addItem(Item item) {
        for (Item existing : items) {
            if (existing.getName().equals(item.getName())) {
                items.remove(existing);
                items.add(new Item(item.getName(), item.getType(), item.getEffectValue(), existing.getQuantity() + item.getQuantity()));
                return;
            }
        }
        items.add(item);
    }

    public Item getItem(int index) {
        if (index >= 0 && index < items.size()) {
            return items.get(index);
        }
        return null;
    }

    public Item removeItem(int index) {
        if (index >= 0 && index < items.size()) {
            return items.remove(index);
        }
        return null;
    }

    public boolean hasItem(String itemName) {
        for (Item item : items) {
            if (item.getName().equals(itemName) && item.getQuantity() > 0) {
                return true;
            }
        }
        return false;
    }

    public void showInventory() {
        System.out.println("\n=== INVENTORY ===");
        if (items.isEmpty()) {
            System.out.println("Inventory is empty.");
            return;
        }
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getQuantity() > 0) {
                System.out.println((i + 1) + ") " + items.get(i).toString());
            }
        }
    }

    public List<Item> getItems() {
        return new ArrayList<>(items);
    }

    public int getItemCount() {
        int total = 0;
        for (Item item : items) {
            total += item.getQuantity();
        }
        return total;
    }

    public Item consumeItem(String name) {
        for (int i = 0; i < items.size(); i++) {
            Item item = items.get(i);
            if (item.getName().equals(name) && item.getQuantity() > 0) {
                Item consumed = new Item(item.getName(), item.getType(), item.getEffectValue(), 1);
                int remainingQty = item.getQuantity() - 1;
                if (remainingQty > 0) {
                    items.set(i, new Item(item.getName(), item.getType(), item.getEffectValue(), remainingQty));
                } else {
                    items.remove(i);
                }
                return consumed;
            }
        }
        return null;
    }

    public Item consumeBestHealItem() {
        Item best = null;
        for (Item item : items) {
            if ("heal".equals(item.getType()) && item.getQuantity() > 0) {
                if (best == null || item.getEffectValue() > best.getEffectValue()) {
                    best = item;
                }
            }
        }
        return best != null ? consumeItem(best.getName()) : null;
    }
}

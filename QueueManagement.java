import javax.swing.*;
import java.awt.*;
import java.security.SecureRandom;
import java.util.*;

public class QueueManagement extends JFrame {
    Map<String, Integer> menu = new LinkedHashMap<>();
    java.util.List<Order> orders = new ArrayList<>();
    JTextArea output = new JTextArea();
    int nextId = 1;

    QueueManagement() {
        menu.put("Sandwich", 80);
        menu.put("Dosa", 90);
        menu.put("Burger", 70);
        menu.put("Fries", 60);
        menu.put("Cold Coffee", 50);
        menu.put("Tea", 20);

        setTitle("NMIMS Canteen Queue");
        setSize(700, 450);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel buttons = new JPanel(new GridLayout(1, 0, 5, 5));
        addButton(buttons, "View Menu", e -> viewMenu());
        addButton(buttons, "Place Order", e -> placeOrder());
        addButton(buttons, "My Orders", e -> showOrders());
        addButton(buttons, "Mark Ready", e -> markReady());
        addButton(buttons, "Verify OTP", e -> collectOrder());

        output.setEditable(false);
        output.setFont(new Font("Monospaced", Font.PLAIN, 14));
        add(buttons, BorderLayout.NORTH);
        add(new JScrollPane(output), BorderLayout.CENTER);
        viewMenu();
    }

    void addButton(JPanel panel, String text, java.awt.event.ActionListener action) {
        JButton b = new JButton(text);
        b.addActionListener(action);
        panel.add(b);
    }

    void viewMenu() {
        StringBuilder text = new StringBuilder("NMIMS CANTEEN MENU\n\n");
        for (String item : menu.keySet())
            text.append(item).append(" - Rs. ").append(menu.get(item)).append("\n");
        output.setText(text.toString());
    }

    // Select multiple items and quantities for ONE order.
    void placeOrder() {
        JPanel panel = new JPanel(new GridLayout(menu.size() + 1, 3, 8, 6));
        panel.add(new JLabel("Select"));
        panel.add(new JLabel("Food item"));
        panel.add(new JLabel("Quantity"));

        Map<String, JCheckBox> checks = new LinkedHashMap<>();
        Map<String, JSpinner> quantities = new LinkedHashMap<>();

        for (String item : menu.keySet()) {
            JCheckBox check = new JCheckBox();
            JSpinner qty = new JSpinner(new SpinnerNumberModel(1, 1, 20, 1));
            checks.put(item, check);
            quantities.put(item, qty);
            panel.add(check);
            panel.add(new JLabel(item + " (Rs. " + menu.get(item) + ")"));
            panel.add(qty);
        }

        int result = JOptionPane.showConfirmDialog(this, panel,
                "Choose items for your order", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (result != JOptionPane.OK_OPTION) return;

        Map<String, Integer> selectedItems = new LinkedHashMap<>();
        int total = 0;
        int maxPrep = 0;

        for (String item : menu.keySet()) {
            if (checks.get(item).isSelected()) {
                int qty = (Integer) quantities.get(item).getValue();
                selectedItems.put(item, qty);
                total += menu.get(item) * qty;
                maxPrep = Math.max(maxPrep, prepTime(item));
            }
        }

        if (selectedItems.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select at least one item.");
            return;
        }

        int wait = 5 + orders.size() * 3 + maxPrep;
        String otp = String.format("%06d",
                new SecureRandom().nextInt(1_000_000));

        Order order = new Order("NM" + nextId++, selectedItems, total, wait, otp);
        orders.add(order);
        showOrderConfirmation(order);
        showOrders();
    }

    int prepTime(String item) {
        switch (item) {
            case "Sandwich": return 8;
            case "Dosa": return 12;
            case "Burger": return 7;
            case "Fries": return 6;
            case "Cold Coffee": return 3;
            case "Tea": return 2;
            default: return 5;
        }
    }

    void showOrderConfirmation(Order order) {
        JDialog dialog = new JDialog(this, "Order Confirmed", true);
        dialog.setSize(400, 430);
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel icon = new JLabel("✓");
        icon.setFont(new Font("Arial", Font.BOLD, 36));
        icon.setForeground(new Color(35, 135, 85));
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = new JLabel("Order placed successfully!");
        title.setFont(new Font("Arial", Font.BOLD, 19));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextArea items = new JTextArea(order.summary());
        items.setEditable(false);
        items.setBackground(new Color(248, 248, 248));
        items.setFont(new Font("Monospaced", Font.PLAIN, 13));
        items.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        JLabel id = new JLabel("Order ID: " + order.id);
        id.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel total = new JLabel("Total: Rs. " + order.total);
        total.setFont(new Font("Arial", Font.BOLD, 16));
        total.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel wait = new JLabel("Estimated wait: " + order.wait + " minutes");
        wait.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel otpTitle = new JLabel("YOUR PICKUP OTP");
        otpTitle.setFont(new Font("Arial", Font.BOLD, 12));
        otpTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel otp = new JLabel(order.otp);
        otp.setFont(new Font("Arial", Font.BOLD, 30));
        otp.setForeground(new Color(135, 25, 55));
        otp.setAlignmentX(Component.CENTER_ALIGNMENT);
        JButton ok = new JButton("Got it");
        ok.setAlignmentX(Component.CENTER_ALIGNMENT);
        ok.addActionListener(e -> dialog.dispose());

        panel.add(icon);
        panel.add(title);
        panel.add(Box.createVerticalStrut(10));
        panel.add(new JScrollPane(items));
        panel.add(Box.createVerticalStrut(8));
        panel.add(id);
        panel.add(total);
        panel.add(wait);
        panel.add(Box.createVerticalStrut(10));
        panel.add(otpTitle);
        panel.add(otp);
        panel.add(ok);

        dialog.setContentPane(panel);
        dialog.setVisible(true);
    }

    void showOrders() {
        StringBuilder text = new StringBuilder("YOUR ORDERS\n\n");
        if (orders.isEmpty()) text.append("No orders yet.");
        for (Order o : orders) {
            text.append(o.id).append(" | ").append(o.summaryOneLine())
                .append(" | Rs. ").append(o.total).append(" | ").append(o.status)
                .append(" | Wait: ").append(o.wait).append(" min\n");
        }
        output.setText(text.toString());
    }

    Order chooseOrder() {
        if (orders.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No orders yet.");
            return null;
        }
        String[] ids = orders.stream().map(o -> o.id).toArray(String[]::new);
        String id = (String) JOptionPane.showInputDialog(this, "Select order:",
                "Choose Order", JOptionPane.PLAIN_MESSAGE, null, ids, ids[0]);
        if (id == null) return null;
        for (Order o : orders) if (o.id.equals(id)) return o;
        return null;
    }

    void markReady() {
        Order o = chooseOrder();
        if (o == null) return;
        if (!o.status.equals("PLACED")) {
            JOptionPane.showMessageDialog(this, "Order is not in PLACED status.");
            return;
        }
        o.status = "READY";
        JOptionPane.showMessageDialog(this, "NOTIFICATION: Order " + o.id
                + " is READY! Student can collect it using their OTP.");
        showOrders();
    }

    void collectOrder() {
        Order o = chooseOrder();
        if (o == null) return;
        if (!o.status.equals("READY")) {
            JOptionPane.showMessageDialog(this, "Order is not ready yet.");
            return;
        }
        String entered = JOptionPane.showInputDialog(this, "Enter pickup OTP:");
        if (entered == null) return;
        if (o.otp.equals(entered.trim())) {
            o.status = "COLLECTED";
            JOptionPane.showMessageDialog(this, "OTP correct. Order handed over!");
        } else {
            JOptionPane.showMessageDialog(this, "Wrong OTP. Order NOT handed over.");
        }
        showOrders();
    }

    static class Order {
        String id, otp, status = "PLACED";
        Map<String, Integer> items;
        int total, wait;

        Order(String id, Map<String, Integer> items, int total, int wait, String otp) {
            this.id = id;
            this.items = new LinkedHashMap<>(items);
            this.total = total;
            this.wait = wait;
            this.otp = otp;
        }

        String summary() {
            StringBuilder s = new StringBuilder();
            for (Map.Entry<String, Integer> e : items.entrySet())
                s.append(e.getKey()).append(" x ").append(e.getValue()).append("\n");
            return s.toString();
        }

        String summaryOneLine() {
            StringBuilder s = new StringBuilder();
            for (Map.Entry<String, Integer> e : items.entrySet()) {
                if (s.length() > 0) s.append(", ");
                s.append(e.getKey()).append(" x ").append(e.getValue());
            }
            return s.toString();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new QueueManagement().setVisible(true));
    }
}

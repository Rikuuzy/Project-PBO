import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        // Jalankan GUI di Event Dispatch Thread (EDT) untuk keamanan Swing thread
        SwingUtilities.invokeLater(() -> {
            GameGUI gui = new GameGUI();
            gui.setVisible(true);
        });
    }
}

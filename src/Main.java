import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            IOMenu menu = new IOMenu("Image Operator (图像处理)_v1.4");
            menu.setVisible(true);
        });
    }
}


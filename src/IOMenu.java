import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Objects;

/**
 * 图像处理软件菜单 <br/>
 * ImageOperator_v1.0 / 2023.9.7 by Maxtrix
 */
public class IOMenu extends JFrame {
    public final String imagePath = "D:/temp-Java/image_operation/";
    public String imageName;
    private final int width, height;

    public BufferedImage IMG = null;

    public IOMenu(String FrameTitle) {
        this.width = 640;
        this.height = 320;

        setTitle(FrameTitle);
        setSize(width, height);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        addPreviewPanel();
        addTitleText();
        addButtons();
    }


    /**
     * 图片预览面板
     */
    private void addPreviewPanel() {
        IMGPreviewPanel IPP = new IMGPreviewPanel();
        add(IPP);
    }

    /**
     * 用于导入和预览图片的面板
     */
    private class IMGPreviewPanel extends JComponent {
        private JLabel textLabel;
        private boolean MouseIn;

        public IMGPreviewPanel() {
            enableEvents(AWTEvent.MOUSE_EVENT_MASK);
            setLocation(10, 14);
            setSize(width *3/5 +1, height *4/5 +1);
            setFont(new Font("", Font.BOLD, 24));
            setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
            setBackground(Color.GRAY);
            setOpaque(true);

            addText();
            MouseIn = false;
        }

        @Override
        protected void processMouseEvent(MouseEvent e) {
            super.processMouseEvent(e);
            if (e.getID() == MouseEvent.MOUSE_ENTERED) MouseIn = true;
            if (e.getID() == MouseEvent.MOUSE_EXITED) MouseIn = false;
            if (e.getID() == MouseEvent.MOUSE_PRESSED) initialImage();
            repaint();
        }

        private void initialImage() {
            File folder = new File(imagePath);
            if (!folder.exists()) System.out.println("Build tempFolder: " + folder.mkdirs());
            JFileChooser fileChooser = new JFileChooser(imagePath);

            fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            int result = fileChooser.showDialog(new JLabel(), "选择");
            File imageFile = fileChooser.getSelectedFile();
            if (imageFile != null && result == JFileChooser.APPROVE_OPTION) {
                if (imageFile.isFile() && imageFile.getName().endsWith(".png")) {
                    String absolutePath = imageFile.getAbsolutePath();
                    System.out.println("Path: " + absolutePath);
                    System.out.println("File: " + fileChooser.getSelectedFile().getName());
                    imageName = imageFile.getName();
                    IMG = IMGKit.inputIMG(absolutePath);
                    calculatePreviewSize();
                    setOpaque(false);
                    remove(textLabel);
                } else {
                    JOptionPane.showMessageDialog(null, "文件格式错误（应为.png）", "导入失败", JOptionPane.ERROR_MESSAGE);
                    System.out.println("File Not Valid.");
                }
            }
        }

        private int preWidth, preHeight;
        private void calculatePreviewSize() {
            double mW = (double) IMG.getWidth() / this.getWidth();
            double mH = (double) IMG.getHeight() / this.getHeight();
            if (mW <= 1 && mH <= 1) {
                preWidth = IMG.getWidth();
                preHeight = IMG.getHeight();
            } else {
                double mC = Math.max(mW, mH);
                preWidth = (int) (IMG.getWidth() / mC);
                preHeight = (int) (IMG.getHeight() / mC);
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (IMG == null) {
                g.setColor(MouseIn ? Color.LIGHT_GRAY : new Color(180, 180, 180));
                g.fillRect(0, 0, this.getWidth(), this.getHeight());
                g.setColor(Color.DARK_GRAY);
                drawDashBox(g);
            }
            else {
                g.drawImage(IMG, 0, 0, preWidth , preHeight, this);
            }

        }
        private void drawDashBox(Graphics g) {
            int segment = 8;
            IMGKit.drawDashLine(g, 24, this.getWidth()-24, 24, segment, true);
            IMGKit.drawDashLine(g, 24, this.getHeight()-24, 24, segment, false);
            IMGKit.drawDashLine(g, 24, this.getWidth()-24, this.getHeight()-24, segment, true);
            IMGKit.drawDashLine(g, 24, this.getHeight()-24, this.getWidth()-24, segment, false);
        }

        private void addText() {
            textLabel = new JLabel("PNG");
            textLabel.setLocation(0, 0);
            textLabel.setSize(this.getWidth(), this.getHeight());
            textLabel.setForeground(Color.DARK_GRAY);
            textLabel.setFont(new Font("Rockwell", Font.PLAIN, 24));
            textLabel.setHorizontalAlignment(SwingConstants.CENTER);
            textLabel.setVerticalAlignment(SwingConstants.CENTER);
            add(textLabel);
        }
    }


    /**
     * 功能选择按钮
     */
    private void addButtons() {
        JButton[] buttons = new JButton[3];
        String[] BNames = new String[]{"CR 色值调节", "DB 背景抹除", "IC 图像卷积"};
        int [] BLocateH = new int[]{13, 20, 27};

        for (int I = 0; I < buttons.length; I++) {
            JButton b = new JButton(BNames[I]);
            b.setLocation(width *2/3, height * BLocateH[I] /40);
            b.setSize(width/4, height/7);
            b.setContentAreaFilled(false);
            b.setFocusPainted(false);
            b.setFont(new Font("黑体", Font.PLAIN, 20));
            buttons[I] = b;
            add(b);
        }

        for (int I = 0; I < buttons.length; I++) {
            int TI = I;
            buttons[I].addActionListener((e) -> {
                if (IMG == null) {
                    JOptionPane.showMessageDialog(this, "请先导入图像", "初始化失败", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                setVisible(false);
                Objects.requireNonNull(NewIPF(TI)).launch();
            });
        }
    }

    private IProcessorFrame NewIPF(int I) {
        switch (I) {
            case 0: return new ColorRegulation("Color Regulation (色值调节) v1.0 - by_Maxtrix", this);
            case 1: return new DeBackdrop("DeBackdrop (背景抹除) v1.0 - by_Maxtrix", this);
            case 2: return new ImageConvolution("Image Convolution (图像卷积) v1.0 - by_Maxtrix", this);
        }
        return null;
    }

    /**
     * 标题文字
     */
    private void addTitleText() {
        JLabel statusLabel = new JLabel("<html>Image Operator<br/>v1.4 by_Maxtrix");
        statusLabel.setLocation(width *2/3, 16);
        statusLabel.setSize(192, 64);
        statusLabel.setForeground(Color.DARK_GRAY);
        statusLabel.setFont(new Font("Rockwell", Font.BOLD, 18));
        add(statusLabel);
    }

    /**
     * 窗口关闭事件的代理
     */
    @Override
    protected void processWindowEvent(WindowEvent e) {
        if (e.getID() == WindowEvent.WINDOW_CLOSING) {
            if (IMG != null && JOptionPane.showConfirmDialog(this, "是否退出？图像不会自动保存", "EXIT", JOptionPane.OK_CANCEL_OPTION) != 0)
                return;
        }
        super.processWindowEvent(e);
    }

}

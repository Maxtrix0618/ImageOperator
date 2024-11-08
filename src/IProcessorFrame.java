import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 图像处理器框架，是各功能单元框架的父类 <br/>
 * ImageProcessor_v1.0 / 2023.9.9 by_Maxtrix
 */
public abstract class IProcessorFrame extends JFrame {
    final IOMenu menu;
    ArrayList<JFrame> SupplySubFrames;
    MagnifyFrame magFrame;
    String imagePath;
    String imageName;
    JLabel imageLabel;
    JLabel windowLabel;

    BufferedImage IMG_DO;

    Point mP;
    int proFigure;  // 像素点工作进程计数

    int widthF;
    int heightF;
    int widthW;
    int heightW;
    int TPN;    // 总像素点数
    int RMC;    // 右边距修正

    public IProcessorFrame(String FrameTitle, IOMenu menu) {
        setTitle(FrameTitle);
        this.menu = menu;
        this.SupplySubFrames = new ArrayList<>();
        this.mP = new Point();
        this.RMC = 0;
    }

    public void launch() {
        loadImage();
        setFrameSize();
        setLocationRelativeTo(null);
        setLayout(null);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

        addImageExhibition();
        addInfoText();
        addMagnify();
        tryAddKeyListeners();
    }

    protected void loadImage() {
        IMG_DO = IMGKit.CloneNewIMG(menu.IMG);
        TPN = IMG_DO.getWidth() * IMG_DO.getHeight();
        imagePath = menu.imagePath;
        imageName = menu.imageName;
    }


    /**
     * 加入放大镜窗及所需keyListener,timer
     */
    protected void addMagnify() {
        magFrame = new MagnifyFrame();
        SupplySubFrames.add(magFrame);

        addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_C) {
                    magFrame.changeEnlargementFactor();
                    requestFocus();
                }
            }
            public void keyReleased(KeyEvent e) {}
            public void keyTyped(KeyEvent e) {}
        });

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                mainTimerRun();
            }
        }, 0, 50);
    }

    /**
     * 主Timer进程循环（暂仅为放大镜窗使用。未来若有其它所用，需将timer从addMagnify方法中分离出去）
     */
    private void mainTimerRun() {
        Point absPoint = MouseInfo.getPointerInfo().getLocation();
        mP.setLocation(IMGKit.LimitedValue(absPoint.x - this.getLocation().x + iX - 25, 0, IMG_DO.getWidth() - 1),
                IMGKit.LimitedValue(absPoint.y - this.getLocation().y + iY - 45, 0, IMG_DO.getHeight() - 1));
        if (magFrame.enlargement != 0)
            magFrame.refresh();
    }


    /**
     * 设置界面与窗口大小
     */
    protected void setFrameSize() {
        widthW = IMGKit.LimitedValue(IMG_DO.getWidth(), 512, 1536 - 240);
        heightW = IMGKit.LimitedValue(IMG_DO.getHeight(), 512, 1024 - 60);
        widthF = widthW + 240;
        heightF = heightW + 60;
        W_roll = (IMG_DO.getWidth() > 1536);
        H_roll = (IMG_DO.getHeight() > 1024);
        System.out.println("Frame: " + widthF + "×" + heightF);
        System.out.println("Window: " + widthW + "×" + heightW);
        System.out.println("IMG: " + IMG_DO.getWidth() + "×" + IMG_DO.getHeight());
        setSize(widthF, heightF);
        setResizable(false);
    }

    protected boolean W_roll;
    protected boolean H_roll;
    protected int iX = 0;
    protected int iY = 0;


    /**
     * 当图片尺寸太大时，加入键盘监听器，使得可以用WASD控制显示窗在图像上的移动
     */
    protected void tryAddKeyListeners() {
        if (W_roll) {
            addKeyListener(new KeyListener() {
                @Override
                public void keyPressed(KeyEvent e) {
                    switch (e.getKeyCode()) {
                        case KeyEvent.VK_A:
                            iX = (iX < 0) ? iX : iX - 20;
                            reloadImage();
                            break;
                        case KeyEvent.VK_D:
                            iX = (iX > imageLabel.getWidth() - windowLabel.getWidth()) ? iX : iX + 20;
                            reloadImage();
                            break;
                    }
                }
                public void keyReleased(KeyEvent e) {}
                public void keyTyped(KeyEvent e) {}
            });
        }
        if (H_roll) {
            addKeyListener(new KeyListener() {
                @Override
                public void keyPressed(KeyEvent e) {
                    switch (e.getKeyCode()) {
                        case KeyEvent.VK_W:
                            iY = (iY < 0) ? iY : iY - 20;
                            reloadImage();
                            break;
                        case KeyEvent.VK_S:
                            iY = (iY > imageLabel.getHeight() - windowLabel.getHeight()) ? iY : iY + 20;
                            reloadImage();
                            break;
                    }
                }

                public void keyReleased(KeyEvent e) {}
                public void keyTyped(KeyEvent e) {}
            });
        }
    }

    protected void paint(int[] p, int x, int y) {
        IMGKit.paint(IMG_DO, p, x, y);
    }

    protected void paintEmpty(int x, int y) {
        IMGKit.paintEmpty(IMG_DO, x, y);
    }

    protected void paintRev(int x, int y) {
        IMGKit.paintRev(IMG_DO, x, y);
    }

    protected int[] getPixel(int x, int y) {
        return IMGKit.getPixel(IMG_DO, x, y);
    }

    protected int[] getPixel(BufferedImage BI, int x, int y) {
        return IMGKit.getPixel(BI, x, y);
    }

    protected boolean outsideArea(int x, int y) {
        return IMGKit.outsideArea(IMG_DO, x, y);
    }

    protected boolean outsideArea(Point point) {
        return IMGKit.outsideArea(IMG_DO, point.x, point.y);
    }


    /**
     * 在IMG_DO上用反色画出(x,y)周围八点组成的方框
     */
    protected void paintPixelBox(int x, int y) {
        for (int i = 0; i < 8; i++) {
            paintRev(x + IMGKit.dxs_8[i], y + IMGKit.dys_8[i]);
        }
    }

    /**
     * 在IMG_DO上用反色画出cP_1到cP_2的矩形区域的外侧方框
     */
    protected void paintAreaBox(Point cP_1, Point cP_2) {
        paintAreaBox(cP_1.x, cP_1.y, cP_2.x, cP_2.y);
    }

    protected void paintAreaBox(int x1, int y1, int x2, int y2) {
        int le = Math.min(x1, x2) - 1;
        int ri = Math.max(x1, x2) + 1;
        int up = Math.min(y1, y2) - 1;
        int dn = Math.max(y1, y2) + 1;
        for (int xa = le; xa <= ri; xa++) {
            paintRev(xa, up);
            paintRev(xa, dn);
        }
        for (int ya = up + 1; ya <= dn - 1; ya++) {
            paintRev(le, ya);
            paintRev(ri, ya);
        }
    }


    /**
     * 将IMG_GO图像更新投放在窗口上
     */
    protected void reloadImage() {
        imageLabel.setIcon(new ImageIcon(IMG_DO));
        imageLabel.setLocation(-iX, -iY);
    }

    /**
     * 图像展示窗（两层，上层视窗windowLabel和下层画布imageLabel）
     */
    protected void addImageExhibition() {
        imageLabel = new JLabel();
        imageLabel.setLocation(0, 0);
        imageLabel.setSize(IMG_DO.getWidth(), IMG_DO.getHeight());

        windowLabel = new JLabel();
        windowLabel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
        windowLabel.setLocation(15, 10);
        windowLabel.setSize(widthW, heightW);
        windowLabel.add(imageLabel);
        add(windowLabel);
        reloadImage();
    }


    protected void IMG_DO_clone(BufferedImage OriBI, BufferedImage NewBI) {
        IMGKit.IMG_clone(OriBI, NewBI);
    }

    protected void extractIMG(BufferedImage BI) {
        IMG_DO_clone(BI, IMG_DO);
    }

    protected void storageIMG(BufferedImage BI) {
        IMG_DO_clone(IMG_DO, BI);
    }

    protected void saveImage() {
        IMGKit.saveImage(IMG_DO, imagePath, this);
    }


    /**
     * 保存图片按钮
     */
    protected void addSaveButton() {
        JButton SaveButton = new JButton("Save");
        SaveButton.setLocation(widthF - 132 + RMC, heightF - 100);
        SaveButton.setSize(96, 32);
        SaveButton.setFocusPainted(false);
        SaveButton.setFont(new Font("", Font.PLAIN, 18));
        SaveButton.addActionListener((e) -> {
            this.requestFocus();
            saveImage();
        });
        add(SaveButton);
    }

    /**
     * 切换背景色展示按钮
     */
    protected void addSetEBgButton() {
        addSetEBgButton("...", widthF - 60, 110, 32, 32);
    }

    protected void addSetEBgButton(String text, int locateX, int locateY, int width, int height) {
        JButton SEBgButton = new JButton(text);
        SEBgButton.setLocation(locateX, locateY);
        SEBgButton.setSize(width, height);
        SEBgButton.setContentAreaFilled(false);
        SEBgButton.setFocusPainted(false);
        SEBgButton.setFont(new Font("", Font.PLAIN, 16));
        SEBgButton.addActionListener((e) -> {
            this.requestFocus();
            Ebg = (Ebg > IMGKit.EbgCs.length - 2) ? 0 : Ebg + 1;
            windowLabel.setBackground(IMGKit.EbgCs[Ebg]);
            windowLabel.setOpaque(Ebg != 0);
            SEBgButton.setBackground(IMGKit.EbgCs[Ebg]);
            SEBgButton.setContentAreaFilled(Ebg != 0);
        });
        add(SEBgButton);
    }

    private int Ebg = 0;


    /**
     * 信息提示文字（图片名+分辨率）
     */
    private void addInfoText() {
        JLabel InfoText = new JLabel("<html>" + imageName + "<br/>" + IMG_DO.getWidth() + " × " + IMG_DO.getHeight());
        InfoText.setLocation(widthF - 135 + RMC, 16);
        InfoText.setSize(128, 64);
        InfoText.setFont(new Font("", Font.PLAIN, 18));
        add(InfoText);
    }

    /**
     * 窗口关闭事件的代理
     */
    @Override
    protected void processWindowEvent(WindowEvent e) {
        if (e.getID() == WindowEvent.WINDOW_CLOSING) {
            switch (JOptionPane.showConfirmDialog(this, "是否将图像回存到主界面？", "EXIT", JOptionPane.OK_CANCEL_OPTION)) {
                case 0:
                    menu.IMG = IMG_DO;
                case 2:
                    dispose();
                    for (JFrame frame : SupplySubFrames)
                        frame.setVisible(false);
                    menu.setVisible(true);
                    break;
                default:
                    return;
            }
        }
        super.processWindowEvent(e);
    }


    /**
     * 放大镜窗
     */
    protected class MagnifyFrame extends JFrame {
        private final JLabel magnifyImageLabel;
        private final int IMG_rb, IMG_db;
        public int enlargement;

        public MagnifyFrame() {
            setUndecorated(true);   // 不显示标题栏
            setSize(128, 128);
            setLayout(null);
            setAlwaysOnTop(true);
            getRootPane().setBorder(BorderFactory.createLineBorder(Color.RED));
            IMG_rb = IMG_DO.getWidth();
            IMG_db = IMG_DO.getHeight();
            enlargement = 0;

            magnifyImageLabel = new JLabel();
            magnifyImageLabel.setLocation(-1, -1);
            magnifyImageLabel.setSize(128, 128);
            add(magnifyImageLabel);
        }

        protected void changeEnlargementFactor() {
            enlargement = (enlargement == 0) ? 2 : ((enlargement >= 16) ? 0 : enlargement * 2);
            setVisible(enlargement != 0);
            refresh();
        }

        public void refresh() {
            if (enlargement == 0) return;
            setLocation(MouseInfo.getPointerInfo().getLocation().x + 40, Math.min(MouseInfo.getPointerInfo().getLocation().y, 920));
            magnifyImageLabel.setIcon(new ImageIcon(magnifyIMG_128()));
        }

        private BufferedImage magnifyIMG_128() {
            int e = enlargement;
            int oriD = 128 / e;
            BufferedImage OriSubIMG = SubIMG(oriD);
            BufferedImage MagSubIMG = new BufferedImage(128, 128, IMG_DO.getType());

            for (int x = 0; x < oriD; x++)
                for (int y = 0; y < oriD; y++) {
                    int P = OriSubIMG.getRGB(x, y);
                    for (int i = 0; i < e; i++)
                        for (int j = 0; j < e; j++) {
                            MagSubIMG.setRGB(e * x + i, e * y + j, P);
                        }
                }
            return MagSubIMG;
        }

        /**
         * 以鼠标位置为中心，返回IMG_DO的以diameter为边长的子图像<br/>
         * > 注意这个子图像会与父图像共享数据数组！
         */
        private BufferedImage SubIMG(int diameter) {
            int x = IMGKit.LimitedValue(mP.x + 1 - diameter / 2, 0, IMG_rb - diameter);
            int y = IMGKit.LimitedValue(mP.y + 1 - diameter / 2, 0, IMG_db - diameter);
            return IMG_DO.getSubimage(x, y, diameter, diameter);
        }
    }

    /**
     * 进度条窗
     */
    protected static class ProgressDialog extends JDialog {
        private final JProgressBar progressBar;

        public ProgressDialog(IProcessorFrame IPF, int total) {
            setTitle("处理中……");
            setSize(256, 128);
            setResizable(false);
            setLocationRelativeTo(null);
            setAlwaysOnTop(true);
            setLayout(null);

            progressBar = new JProgressBar();
            progressBar.setBounds(16, 22, 208, 32);
            progressBar.setStringPainted(true);
            progressBar.setMaximum(total);
            add(progressBar);

            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    progressBar.setValue(IPF.proFigure);
                    if (IPF.proFigure < 0) {
                        timer.cancel();
                        dispose();
                    }
                }
            }, 0, 50);

            setVisible(true);
        }
    }


}

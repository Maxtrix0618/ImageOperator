import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.Timer;

/**
 * 图片抹除背景色 <br/>
 * DeBackdrop_v1.0 / 2023.9.1 by_Maxtrix
 */
public class DeBackdrop extends IProcessorFrame {
    private JSlider toleSlider;
    private JLabel toleText;
    private JLabel ARGBText;
    private JButton ColorButton;
    private JButton DoButton;
    private JButton UdButton;

    private BufferedImage IMG_temp;     // 储存上一步图像

    private final Point bgL;
    private int[] bgARGB;
    private int AddRGB;
    private int ExtentN;
    private int Strategy;
    private int tole;

    public DeBackdrop(String FrameTitle, IOMenu menu) {
        super(FrameTitle, menu);

        this.Strategy = 0;
        this.tole = 0;
        this.bgL = new Point();
        this.bgARGB = new int[4];
        this.AddRGB = -1;
        this.ExtentN = 4;
    }

    public void launch() {
        super.launch();

        addToleSlider();
        addStrategyButton();
        addSetEBgButton();
        addToleText();
        addARGBText();
        addColorButton();
        addDoButton();
        addUndoButton();
        addGLCButton();
        addSaveButton();

        setVisible(true);
        requestFocus();
    }

    protected void loadImage() {
        super.loadImage();
        IMG_temp = IMGKit.CloneNewIMG(IMG_DO);
    }


    /**
     * 容忍度调节滑杆
     */
    private void addToleSlider() {
        toleSlider = new JSlider(0, 256);
        toleSlider.setLocation(widthF -220, 20);
        toleSlider.setSize(80, heightF - 80);
        toleSlider.setOrientation(JSlider.VERTICAL);
        toleSlider.setMajorTickSpacing(16);
        toleSlider.setMinorTickSpacing(4);
        toleSlider.setPaintTicks(true);
        toleSlider.setPaintLabels(true);
        toleSlider.setValue(tole);
        toleSlider.addChangeListener(event -> {
            this.requestFocus();
            tole = ((JSlider) event.getSource()).getValue();
            toleText.setText("Tole : " + tole);
            deBackProgress();
        });
        getContentPane().add(toleSlider);
    }

    /**
     * 容忍度数字
     */
    private void addToleText() {
        toleText = new JLabel("Tole : " + tole);
        toleText.setLocation(widthF -135, 170);
        toleText.setSize(128, 32);
        toleText.setFont(new Font("", Font.PLAIN, 22));
        add(toleText);
    }

    /**
     * ARGB色值信息文字
     */
    private void addARGBText() {
        ARGBText = new JLabel("<html>a:___ | r:___<br/>g:___ | b:___");
        ARGBText.setLocation(widthF -140, 220);
        ARGBText.setSize(128, 64);
        ARGBText.setFont(new Font("", Font.PLAIN, 18));
        add(ARGBText);
    }

    /**
     * 去色开始按钮
     */
    private void addDoButton() {
        DoButton = new JButton("DO");
        DoButton.setLocation(widthF -125, 350);
        DoButton.setSize(72, 32);
        DoButton.setFocusPainted(false);
        DoButton.setFont(new Font("", Font.PLAIN, 20));
        DoButton.addActionListener((e) -> {
            this.requestFocus();
            DoButton.setVisible(false);
            deBackProgress();
        });
        DoButton.setVisible(false);
        add(DoButton);
    }

    /**
     * 去色主方法
     */
    private void deBackProgress() {
        if (AddRGB == -1) return;
        System.out.print("[DB] S-");
        extractIMG(IMG_temp);
        reloadImage();
        doBP_BFS();
        System.out.println("E. ");
        UdButton.setVisible(true);
    }

    private void doBP_BFS() {
        switch (ExtentN) {
            case 0: {
                for (int x = 0; x < IMG_DO.getWidth(); x++)
                    for (int y = 0; y < IMG_DO.getHeight(); y++)
                        if (tolerable(x, y))
                            paintEmpty(x, y);
                break;
            }
            case 4: Dxs = IMGKit.dxs_4; Dys = IMGKit.dys_4; doBP_BFS_Locally(); break;
            case 8: Dxs = IMGKit.dxs_8; Dys = IMGKit.dys_8; doBP_BFS_Locally(); break;
        }
    }
    int[] Dxs, Dys;

    private void doBP_BFS_Locally() {
        boolean[][] called = new boolean[IMG_DO.getWidth()][IMG_DO.getHeight()];    // 是否已加入即访队列
        Queue<Point> await = new LinkedList<>();    // 用于存储即将访问的节点的队列
        await.add(bgL);

        while (!await.isEmpty()) {
            Point curr = await.poll();
            paintEmpty(curr.x, curr.y);
            for (int i = 0; i < Dxs.length; i++) {
                int x = curr.x + Dxs[i];
                int y = curr.y + Dys[i];
                if (outsideArea(x, y) || called[x][y]) continue;
                called[x][y] = true;
                if (tolerable(x, y)) {
                    await.add(new Point(x, y));
                }
            }
        }
    }


    /**
     * 判断像素点是否“可容忍”：如果该位置像素点与参考背景像素bgC的r,g,b色值分别相差都不超过容忍度tole（“加和”下是三者之和的相差），则返回true，否则返回false.
     */
    private boolean tolerable(int x, int y) {
        switch (Strategy) {
            case 0: {   // 独立
                for (int i = 1; i <= 3; i++)
                    if (Math.abs(getPixel(x, y)[i] - bgARGB[i]) > tole)
                        return false;
                return true;
            }
            case 1: {   // 加和
                return !(Math.abs(getPixel(x, y)[1] + getPixel(x, y)[2] + getPixel(x, y)[3] - AddRGB) > tole);
            }
        }
        return false;
    }

    private boolean extractingColor = false;
    private boolean waitForDo = false;
    /**
     * 背景取色按钮
     */
    private void addColorButton() {
        ColorButton = new JButton("");
        ColorButton.setLocation(widthF -115, 290);
        ColorButton.setSize(48, 48);
        ColorButton.setContentAreaFilled(false);
        ColorButton.setFocusPainted(false);
        ColorButton.setBackground(Color.LIGHT_GRAY);
        ColorButton.setFont(new Font("", Font.PLAIN, 24));
        ColorButton.addActionListener((e) -> {
            this.requestFocus();
            if (extractingColor) return;
            if (waitForDo) paintPixelBox(bgL.x, bgL.y);
            extractionColor();
        });
        add(ColorButton);
    }

    /**
     * 取色方法
     */
    private void extractionColor() {
        storageIMG(IMG_temp);
        DoButton.setVisible(false);
        UdButton.setVisible(false);
        ColorButton.setContentAreaFilled(true);
        extractingColor = true;
        waitForDo = false;

        MouseAdapter ML = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e){
                if (e.getButton() == MouseEvent.BUTTON1) {
                    extractingColor = false;
                    waitForDo = true;
                }
            }
        };
        addMouseListener(ML);

        java.util.Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (extractingColor) {
                    timerRun();
                    return;
                }
                timer.cancel();
                removeMouseListener(ML);
                DoButton.setVisible(true);
            }
        }, 0, 50);
    }

    private void timerRun() {
        Point absPoint = MouseInfo.getPointerInfo().getLocation();
        Point relPoint = new Point(absPoint.x-this.getLocation().x+iX-25, absPoint.y-this.getLocation().y+iY-45);
        tryShowPixelMessage(relPoint.x, relPoint.y);
    }

    private void tryShowPixelMessage(int x, int y) {
        extractIMG(IMG_temp);
        if (x < 0 || y < 0 || x >= IMG_DO.getWidth() || y >= IMG_DO.getHeight()) return;
        bgL.setLocation(x, y);
        int bgC = IMG_DO.getRGB(x, y);
        bgARGB = IMGKit.getPixel(bgC);
        AddRGB = bgARGB[1] + bgARGB[2] + bgARGB[3];
        ARGBText.setText("<html>a:" + bgARGB[0] + " | r:" + bgARGB[1] + "<br/>g:" + bgARGB[2] + " | b:" + bgARGB[3]);
        ColorButton.setBackground(new Color(bgC, true));
        paintPixelBox(x, y);
        reloadImage();
    }


    /**
     * 策略转变按钮
     */
    private void addStrategyButton() {
        JButton StButton = new JButton("独立");
        StButton.setLocation(widthF-140, 110);
        StButton.setSize(72, 32);
        StButton.setContentAreaFilled(false);
        StButton.setFocusPainted(false);
        StButton.setFont(new Font("黑体", Font.PLAIN, 18));
        StButton.addActionListener((e) -> {
            this.requestFocus();
            switch (Strategy) {
                case 0: {
                    Strategy = 1;
                    StButton.setText("加和");
                    toleSlider.setMaximum(768);
                    break;
                }
                case 1: {
                    Strategy = 0;
                    StButton.setText("独立");
                    toleSlider.setMaximum(256);
                    break;
                }
            }
            deBackProgress();
        });
        add(StButton);
    }

    /**
     * 撤销按钮
     */
    private void addUndoButton() {
        UdButton = new JButton("Undo");
        UdButton.setLocation(widthF -130, 384);
        UdButton.setSize(84, 32);
        UdButton.setFocusPainted(false);
        UdButton.setFont(new Font("", Font.PLAIN, 18));
        UdButton.addActionListener((e) -> {
            this.requestFocus();
            UdButton.setVisible(false);
            if (AddRGB != -1) DoButton.setVisible(true);
            extractIMG(IMG_temp);
            reloadImage();
        });
        UdButton.setVisible(false);
        add(UdButton);
    }



    /**
     * 局域/全局切换按钮
     */
    private void addGLCButton() {
        JButton GLCButton = new JButton("域4");
        GLCButton.setLocation(widthF -124, 424);
        GLCButton.setSize(72, 32);
        GLCButton.setContentAreaFilled(false);
        GLCButton.setFocusPainted(false);
        GLCButton.setFont(new Font("黑体", Font.PLAIN, 18));
        GLCButton.addActionListener((e) -> {
            this.requestFocus();
            String GLCBText = "";
            switch (ExtentN) {
                case 0: ExtentN = 4; GLCBText = "域4"; break;
                case 4: ExtentN = 8; GLCBText = "域8"; break;
                case 8: ExtentN = 0; GLCBText = "全局"; break;
            }
            GLCButton.setText(GLCBText);
            deBackProgress();
        });
        add(GLCButton);
    }



}

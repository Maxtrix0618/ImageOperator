import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.Timer;

/**
 * 图片色值调节 <br/>
 * ColorRegulation_v1.0 / 2023.9.4 by_Maxtrix
 */
public class ColorRegulation extends IProcessorFrame {
    private JSlider AdsSlider;
    private JLabel AdsValueText;
    private JLabel CPSText;
    private JLabel CPRText;
    private JLabel MPText;
    private JButton SelBoxButton;
    private JButton PSButton;

    private BufferedImage IMG_temp;     // 储存上一步图像
    private BufferedImage IMG_tempBox;  // 储存上一个含框选标线的图像
    private BufferedImage IMG_tempARGB; // 储存调色前的图像

    private ARGBFrame argbFrame;
    private final Point cP_1;
    private final Point cP_2;
    private final int[] AdValues;
    private int AdFN;
    public int[] ARGB;

    public ColorRegulation(String FrameTitle, IOMenu menu) {
        super(FrameTitle, menu);

        this.AdFN = 0;
        this.AdValues = new int[6];
        this.ARGB = new int[4];
        this.cP_1 = new Point();
        this.cP_2 = new Point();
    }

    public void launch() {
        super.launch();

        addAdjustSlider();
        addAdjustFunctionButton();
        addSetEBgButton();
        addAdjustValueText();
        addARGBButton();
        addSelectBoxButton();
        addCPSText();
        addCPRText();
        addMPText();
        addSaveButton();
        addMoreKeyListeners();
        modeGlobal();

        setVisible(true);
        requestFocus();
    }

    protected void loadImage() {
        super.loadImage();
        IMG_temp = IMGKit.CloneNewIMG(IMG_DO);
        IMG_tempBox = IMGKit.CloneNewIMG(IMG_DO);
        IMG_tempARGB = IMGKit.CloneNewIMG(IMG_DO);
    }

    protected void setFrameSize() {
        super.setFrameSize();
        argbFrame = new ARGBFrame(this);
        SupplySubFrames.add(argbFrame);
    }


    /**
     * 加入监听器，用于切换Pass/Save按钮.
     */
    void addMoreKeyListeners() {
        addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_CONTROL) {PSButtonUpdate(true);}
            }
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_CONTROL) {PSButtonUpdate(false);}
            }
            public void keyTyped(KeyEvent e) {}
        });
    }


    /**
     * 调整模式转变按钮
     */
    private void addAdjustFunctionButton() {
        JButton StButton = new JButton(AdNames[AdFN]);
        StButton.setLocation(widthF-145, 90);
        StButton.setSize(88, 32);
        StButton.setContentAreaFilled(false);
        StButton.setFocusPainted(false);
        StButton.setFont(new Font("黑体", Font.PLAIN, 18));
        StButton.addActionListener((e) -> {
            this.requestFocus();
            AdFN = (AdFN >= AdValues.length-1) ? 0 : AdFN+1;
            StButton.setText(AdNames[AdFN]);
            AdsComponentsUpdate();
        });
        add(StButton);
    }

    private final String[] AdNames = new String[]{"亮度", "曝光度", "对比度", "饱和度", "锐化", "降噪"};
    private final int[] AdSMinS = new int[]{-100, -100, -100, -100, 0, 0};
    private final int[] AdSMaxS = new int[]{100, 100, 100, 100, 100, 100};

    private void AdsComponentsUpdate() {
        sliderListenerPause = true;
        AdsSlider.setMinimum(AdSMinS[AdFN]);
        AdsSlider.setMaximum(AdSMaxS[AdFN]);
        AdsSlider.setValue(AdValues[AdFN]);
        sliderListenerPause = false;
    }

    private boolean sliderListenerPause;    // slider的功能与范围在切换时也会触发changeListener，此时须停用之以避免数据流入下位.
    /**
     * 调整调节滑杆
     */
    private void addAdjustSlider() {
        AdsSlider = new JSlider(AdSMinS[AdFN], AdSMaxS[AdFN]);
        AdsSlider.setLocation(widthF -220, 20);
        AdsSlider.setSize(70, heightF - 80);
        AdsSlider.setOrientation(JSlider.VERTICAL);
        AdsSlider.setMajorTickSpacing(20);
        AdsSlider.setMinorTickSpacing(5);
        AdsSlider.setPaintTicks(true);
        AdsSlider.setPaintLabels(true);
        AdsSlider.setValue(AdValues[AdFN]);
        AdsSlider.addChangeListener(event -> {
            this.requestFocus();
            if (sliderListenerPause) return;
            AdValues[AdFN] = ((JSlider) event.getSource()).getValue();
            theValueTextUpdate();
            Do_Adjust();
        });
        sliderListenerPause = false;
        add(AdsSlider);
    }

    /**
     * 调整值文字
     */
    private void addAdjustValueText() {
        AdsValueText = new JLabel("Val  0");
        AdsValueText.setLocation(widthF -135, 135);
        AdsValueText.setSize(128, 32);
        AdsValueText.setFont(new Font("", Font.PLAIN, 20));
        add(AdsValueText);
    }
    private void theValueTextUpdate() {
        int v = AdValues[AdFN];
        String text = (v > 0) ? "+" : "";
        AdsValueText.setText("Val  " + text + v);
    }

    /**
     * 按照当前选择功能进行色彩调整
     */
    private void Do_Adjust() {
        if (boxMode == 1) return;
        int l = Math.min(cP_1.x, cP_2.x);
        int r = Math.max(cP_1.x, cP_2.x);
        int u = Math.min(cP_1.y, cP_2.y);
        int d = Math.max(cP_1.y, cP_2.y);
        int dv = AdValues[AdFN];
        switch (AdFN) {
            case 0 : for (int x = l; x <= r; x++) for (int y = u; y <= d; y++) brightnessAdj(dv, x, y); break;
            case 1 : for (int x = l; x <= r; x++) for (int y = u; y <= d; y++) exposureAdj(dv, x, y); break;
            case 2 : for (int x = l; x <= r; x++) for (int y = u; y <= d; y++) contrastAdj(dv, x, y); break;
            case 3 : for (int x = l; x <= r; x++) for (int y = u; y <= d; y++) saturationAdj(dv, x, y); break;
            case 4 : for (int x = l; x <= r; x++) for (int y = u; y <= d; y++) sharpenAdj(dv, x, y); break;
            case 5 : for (int x = l; x <= r; x++) for (int y = u; y <= d; y++) denoiseAdj(dv, x, y); break;
        }
        reloadImage();
    }


    // 亮度
    private void brightnessAdj(int dv, int x, int y) {
        paintDc(0, mulC(dv), mulC(dv), mulC(dv), x, y);
    }
    // 曝光度
    private void exposureAdj(int dv, int x, int y) {
        // TODO
        System.out.println(dv + "/" + x + "/" + y);
    }
    // 对比度
    private void contrastAdj(int dv, int x, int y) {
        // TODO
        System.out.println(dv + "/" + x + "/" + y);
    }
    // 饱和度
    private void saturationAdj(int dv, int x, int y) {
        int[] argb = IMGKit.getPixel(IMG_tempARGB, x, y);
        int average = (argb[1] + argb[2] + argb[3]) / 3;
        int dr = (Integer.compare(argb[1], average)) * mulC(dv);
        int dg = (Integer.compare(argb[2], average)) * mulC(dv);
        int db = (Integer.compare(argb[3], average)) * mulC(dv);
        paintDc(0, dr, dg, db, x, y);
        // TODO 效果未实现
    }
    // 锐化 TODO 这个应该放到卷积里.
    private void sharpenAdj(int dv, int x, int y) {
        // TODO
        System.out.println(dv + "/" + x + "/" + y);
    }
    // 降噪
    private void denoiseAdj(int dv, int x, int y) {
        // TODO
        System.out.println(dv + "/" + x + "/" + y);
    }


    private int mulC(int v) {return (v * 255 / 100);}

    /**
     * 框选角点信息文字
     */
    private void addCPSText() {
        CPSText = new JLabel();
        CPSText.setLocation(widthF -135, 300);
        CPSText.setSize(256, 64);
        CPSText.setFont(new Font("", Font.BOLD, 18));
        add(CPSText);
    }
    private void addCPRText() {
        CPRText = new JLabel();
        CPRText.setLocation(widthF -135, 330);
        CPRText.setSize(128, 64);
        CPRText.setFont(new Font("", Font.PLAIN, 16));
        add(CPRText);
    }

    /**
     * 鼠标位置信息文字
     */
    private void addMPText() {
        MPText = new JLabel();
        MPText.setLocation(widthF -140, heightF - 200);
        MPText.setSize(128, 96);
        MPText.setFont(new Font("", Font.PLAIN, 14));
        add(MPText);
    }


    /**
     * 执行色差调节方法
     */
    private void Do_ARGB() {
        if (outsideArea(cP_1) || outsideArea(cP_2)) return;
        System.out.print("[CR] S-");
        extractIMG(IMG_tempBox);
        int l = Math.min(cP_1.x, cP_2.x);
        int r = Math.max(cP_1.x, cP_2.x);
        int u = Math.min(cP_1.y, cP_2.y);
        int d = Math.max(cP_1.y, cP_2.y);
        for (int x = l; x <= r; x++)
            for (int y = u; y <= d; y++) {
                paintDc_from_tempARGB(x, y);
            }
        reloadImage();
        System.out.print("E. ");
    }


    /**
     * 色值偏离值调节窗按钮
     */
    private void addARGBButton() {
        JButton colorButton = new JButton("◑");
        colorButton.setLocation(widthF -118, 180);
        colorButton.setSize(54, 54);
        colorButton.setContentAreaFilled(false);
        colorButton.setFocusPainted(false);
        colorButton.setBackground(Color.LIGHT_GRAY);
        colorButton.setFont(new Font("", Font.PLAIN, 22));
        colorButton.addActionListener((e) -> {
            if (boxMode == 1) {
                JOptionPane.showMessageDialog(this, "无法在框选前调节色值", "操作失败", JOptionPane.ERROR_MESSAGE);
                return;
            }
            argbFrame.changeVisible();
            this.requestFocus();
        });
        add(colorButton);
    }

    /**
     * 色值偏离值调节窗
     */
    private class ARGBFrame extends JFrame {
        private final JFrame mainFrame;
        private final JLabel[] valueTexts = new JLabel[4];

        public ARGBFrame(JFrame mainFrame) {
            this.mainFrame = mainFrame;
            setUndecorated(true);   // 不显示标题栏
            setSize(256, heightF);
            setLayout(null);
            getRootPane().setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
            addComponents();
        }

        public void changeVisible() {
            storageIMG(IMG_tempARGB);
            setLocation(mainFrame.getLocation().x + mainFrame.getWidth() -5, mainFrame.getY());
            setVisible(!argbFrame.isVisible());
        }

        private void addComponents() {
            for (int i = 0; i < 4; i++) {
                addSlider(i);
                addValueText(i);
                addNameText(i);
            }
        }

        private void addSlider(int I) {
            JSlider slider = new JSlider(-256, 256);
            slider.setValue(ARGB[I]);
            slider.setLocation(256 *I/4 +2, 80);
            slider.setSize(60, heightF - 100);
            slider.setOrientation(JSlider.VERTICAL);
            slider.setMajorTickSpacing(32);
            slider.setMinorTickSpacing(8);
            slider.setPaintTicks(true);
            slider.setPaintLabels(true);
            slider.addChangeListener(event -> {
                if (boxMode == 1) return;
                ARGB[I] = ((JSlider) event.getSource()).getValue();
                updateValueText(I);
                Do_ARGB();
                mainFrame.requestFocus();
            });
            getContentPane().add(slider);
        }

        private void addValueText(int I) {
            JLabel valueText = new JLabel("0");
            valueText.setLocation(256 *I/4 +18, 40);
            valueText.setSize(48, 48);
            valueText.setFont(new Font("", Font.PLAIN, 18));
            valueTexts[I] = valueText;
            add(valueText);
        }
        private void updateValueText(int I) {
            int v = ARGB[I];
            String t = (v > 0) ? "+" : "";
            valueTexts[I].setText(t + ARGB[I]);
        }

        private void addNameText(int I) {
            String[] N_ARGB = new String[]{"A", "R", "G", "B"};
            JLabel nameText = new JLabel(N_ARGB[I]);
            nameText.setLocation(256 *I/4 +20, 15);
            nameText.setSize(32, 32);
            nameText.setFont(new Font("", Font.BOLD, 18));
            add(nameText);
        }

    }


    private int boxMode = 0;    // 0:Global, 1:Local-selecting, 2:Local-confirm.
    /**
     * 全局/局部（框选）切换按钮
     */
    private void addSelectBoxButton() {
        SelBoxButton = new JButton("□");
        SelBoxButton.setLocation(widthF -118, 245);
        SelBoxButton.setSize(54, 54);
        SelBoxButton.setContentAreaFilled(false);
        SelBoxButton.setFocusPainted(false);
        SelBoxButton.setBackground(Color.LIGHT_GRAY);
        SelBoxButton.setFont(new Font("", Font.PLAIN, 22));
        SelBoxButton.addActionListener((e) -> {
            this.requestFocus();
            if (boxMode == 0 || boxMode == 2) {
                boxMode = 1;
                SelBoxButton.setText("▣");
                selectBox();
                return;
            }
            if (boxMode == 1) {
                boxMode = 0;
                SelBoxButton.setText("□");
                modeGlobal();
            }
        });
        add(SelBoxButton);
    }

    private void modeGlobal() {
        extractIMG(IMG_temp);
        reloadImage();
        storageIMG(IMG_tempBox);
        magFrame.setVisible(false);
        CPSText.setText("GLOBAL");
        CPSText.setFont(new Font("", Font.BOLD, 18));
        CPRText.setText("");
        MPText.setText("");
        cP_1.setLocation(0, 0);
        cP_2.setLocation(IMG_DO.getWidth()-1, IMG_DO.getHeight()-1);
    }


    private boolean mousePress;
    /**
     * 框选方法
     */
    private void selectBox() {
        updateImageFromTemp();
        argbFrame.setVisible(false);
        CPSText.setFont(new Font("", Font.PLAIN, 16));
        cP_1.setLocation(-1, -1);
        cP_2.setLocation(-1, -1);
        mousePress = false;

        MouseListener ML = new MouseListener() {
            @Override
            public void mousePressed(MouseEvent e) {
                if(e.getButton() == MouseEvent.BUTTON1) {
                    updateImageFromTemp();
                    cP_1.setLocation(mP);
                    mousePress = true;
                }
                if (e.getButton() == MouseEvent.BUTTON3) {
                    cP_1.x = cP_1.y = cP_2.x = cP_2.y = -1;
                    CPRText.setText("");
                    updateImageFromTemp();
                }
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    storageIMG(IMG_tempBox);
                    cP_2.setLocation(mP);
                    mousePress = false;
                    boxMode = 2;
                    SelBoxButton.setText("☒");
                }
            }
            @Override
            public void mouseClicked(MouseEvent e) {}
            public void mouseEntered(MouseEvent e) {}
            public void mouseExited(MouseEvent e) {}
        };
        addMouseListener(ML);

        java.util.Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (timerPause) return;
                if (boxMode == 0 || boxMode == 2)
                    removeMouseListener(ML);
                if (boxMode == 0) {
                    timer.cancel();
                    return;
                }
                timerRun();
            }
        }, 0, 50);
    }

    private void updateImageFromTemp() {
        extractIMG(IMG_temp);
        reloadImage();
        storageIMG(IMG_tempBox);
    }

    private void timerRun() {
        MPTextUpdate();
        tryShowPixelLocation(mP.x, mP.y);
    }

    private void MPTextUpdate() {
        int[] argb = IMGKit.getPixel(IMG_DO.getRGB(mP.x, mP.y));
        String text = "<html>⌖ Location<br/>(" + mP.x + "," + mP.y + ")<br/>" +
                "◕ ARGB<br/>" + argb[0] + "|" + argb[1] + "|" + argb[2] + "|" + argb[3] + "<br/>";
        if (magFrame.enlargement != 0)
            text += "⊙ Magnify ×" + magFrame.enlargement;
        MPText.setText(text);
    }


    private void tryShowPixelLocation(int x, int y) {
        if (boxMode == 2) return;
        CPSText.setText("<html>P1: (" + cP_1.x + "," + cP_1.y + ")<br/>P2: (" + cP_2.x + "," + cP_2.y + ")<br/>");

        extractIMG(IMG_tempBox);
        if (mousePress) {
            CPRText.setText(" [" + Math.abs(cP_1.x - mP.x) + "-" + Math.abs(cP_1.y - mP.y) + "] ");
            paintAreaBox(cP_1, mP);
        } else {
            paintPixelBox(x, y);
        }
        reloadImage();
    }


    /**
     * 在IMG的(x,y)处，以tempARGB为原本，按输入增量调节像素<br/>
     * 若低于0或高于255，则维持在0或255.
     */
    private void paintDc(int dA, int dR, int dG, int dB, int x, int y) {
        int[] o_argb = IMGKit.getPixel(IMG_tempARGB, x, y);
        IMGKit.paint(IMG_DO,
                IMGKit.LimitedValue(o_argb[0]+dA, 0, 255),
                IMGKit.LimitedValue(o_argb[1]+dR, 0, 255),
                IMGKit.LimitedValue(o_argb[2]+dG, 0, 255),
                IMGKit.LimitedValue(o_argb[3]+dB, 0, 255),
                x, y);
    }
    private void paintDc_from_tempARGB(int x, int y) {
        paintDc(ARGB[0], ARGB[1], ARGB[2], ARGB[3], x, y);
    }

    /**
     * 切换背景色展示按钮
     */
    @Override
    protected void addSetEBgButton() {addSetEBgButton("...", widthF-50, 90, 32, 32);}

    /**
     * 在图像复制过程中暂停timer的run()方法.
     */
    @Override
    protected void IMG_DO_clone(BufferedImage OriBI, BufferedImage NewBI) {
        timerPause = true;
        IMGKit.IMG_clone(OriBI, NewBI);
        timerPause = false;
    }
    boolean timerPause;


    /**
     * 返回IMG_DO以cP_1和cP_2为矩形对角顶点的子图像<br/>
     * > 注意这个子图像会与父图像共享数据数组！
     */
    private BufferedImage IMG_Sub_cP1cP2() {
        int le = Math.min(cP_1.x, cP_2.x);
        int ri = Math.max(cP_1.x, cP_2.x);
        int up = Math.min(cP_1.y, cP_2.y);
        int dn = Math.max(cP_1.y, cP_2.y);
        return IMG_DO.getSubimage(le, up, ri-le+1, dn-up+1);
    }


    /**
     * 通过/保存（按Ctrl启用）图片按钮
     */
    @Override
    protected void addSaveButton() {
        PSButton = new JButton("Pass");
        PSButton.setLocation(widthF -136, heightF -100);
        PSButton.setSize(96, 32);
        PSButton.setFocusPainted(false);
        PSButton.setFont(new Font("", Font.PLAIN, 18));
        PSButton.addActionListener((e) -> {
            this.requestFocus();
            if (boxMode == 1) {
                JOptionPane.showMessageDialog(this, "无法在框选前通过或保存", "操作失败", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (Ctrl_Pressing)  saveImage();
            else                PassIMG();
        });
        add(PSButton);
    }
    private boolean Ctrl_Pressing;

    /**
     * 输出框选范围的图片到路径（若是Global模式则输出全图）
     */
    @Override
    protected void saveImage() {
        BufferedImage BI = (boxMode == 0) ? IMG_DO : IMG_Sub_cP1cP2();
        IMGKit.saveImage(BI, imagePath, this);
    }


    /**
     * 操作通过，存储到IMG_temp.
     */
    private void PassIMG() {
        switch (boxMode) {
            case 2: paintAreaBox(cP_1, cP_2);
            case 0: storageIMG(IMG_temp); storageIMG(IMG_tempBox); storageIMG(IMG_tempARGB);
            default: reloadImage();
        }
    }

    private void PSButtonUpdate(boolean pressing) {
        Ctrl_Pressing = pressing;
        if (pressing) {
            PSButton.setText("Save");
            PSButton.setFont(new Font("", Font.BOLD, 18));
        } else {
            PSButton.setText("Pass");
            PSButton.setFont(new Font("", Font.PLAIN, 18));
        }
    }


}

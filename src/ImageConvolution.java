import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * 图像卷积 <br/>
 * ImageConvolution_v1.0 / 2023.9.8 by_Maxtrix
 */
public class ImageConvolution extends IProcessorFrame {
    private BufferedImage IMG_temp;     // 储存上一步图像

    private JButton UdButton;

    public ImageConvolution(String frameTitle, IOMenu menu) {
        super(frameTitle, menu);
        this.RMC = -30;
    }
    public void launch() {
        super.launch();

        addSmSpBlurButton();
        addExpCorDButton();
        addUndoButton();
        addSetEBgButton();
        addSaveButton();

        setVisible(true);
        requestFocus();
    }

    protected void loadImage() {
        super.loadImage();
        IMG_temp = IMGKit.CloneNewIMG(IMG_DO);
    }

    @Override
    protected void addSetEBgButton() {addSetEBgButton("BgC", widthF-140 +RMC, 100, 128, 36);}


    /**
     * 平滑模糊与尖峰锐化
     */
    private void addSmSpBlurButton() {
        int KN = 3;
        JButton buttonSm = new JButton("平滑模糊");
        JButton buttonSh = new JButton("尖峰锐化");
        initialButton(buttonSm, 150);
        initialButton(buttonSh, 200);
        buttonSm.addActionListener((e) -> {
            this.requestFocus();
            proFigure = 0;
            new ProgressDialog(this, TPN);
            new Thread(() -> {
                double[][] Kernel = new double[KN][KN];
                double KV = 1 / Math.pow(KN, 2);
                for (int i = 0; i < Kernel.length; i++)
                    for (int j = 0; j < Kernel[0].length; j++)
                        Kernel[i][j] = KV;
                convolution(Kernel);

            }).start();
        });
        buttonSh.addActionListener((e) -> {
            this.requestFocus();
            proFigure = 0;
            new ProgressDialog(this, TPN);
            new Thread(() -> {
                int KC = (KN - 1) / 2;
                double[][] Kernel = new double[KN][KN];
                double KV = -1 / Math.pow(KN, 2);
                for (int i = 0; i < Kernel.length; i++)
                    for (int j = 0; j < Kernel[0].length; j++)
                        Kernel[i][j] = KV;
                Kernel[KC][KC] += 2;
                convolution(Kernel);

            }).start();
        });
        add(buttonSm);
        add(buttonSh);
    }

    /**
     * 膨胀与腐蚀
     */
    private void addExpCorDButton() {
        int Kl = 3;
        JButton buttonE = new JButton("膨胀");
        JButton buttonC = new JButton("腐蚀");
        initialButton(buttonE, 280);
        initialButton(buttonC, 320);
        buttonE.addActionListener((e) -> {
            this.requestFocus();
            proFigure = 0;
            new ProgressDialog(this, TPN);
            new Thread(() -> ExpCor(Kl, true)).start();
        });
        buttonC.addActionListener((e) -> {
            this.requestFocus();
            proFigure = 0;
            new ProgressDialog(this, TPN);
            new Thread(() -> ExpCor(Kl, false)).start();
        });
        add(buttonE);
        add(buttonC);
    }

    /**
     * 撤销
     */
    private void addUndoButton() {
        UdButton = new JButton("Undo");
        initialButton(UdButton, 480);
        UdButton.addActionListener((e) -> {
            extractIMG(IMG_temp);
            storageIMG(IMG_DO);
            reloadImage();
            UdButton.setVisible(false);
        });
        add(UdButton);
        UdButton.setVisible(false);
    }



    /**
     * 按输入的核进行图像卷积
     * @param Kernel 核（二维int数组）
     */
    private void convolution(double[][] Kernel) {
        System.out.print("[IC] S-");
        storageIMG(IMG_temp);
        int KC = (Kernel.length - 1) / 2;
        for (int x = 0; x < IMG_DO.getWidth(); x++)
            for (int y = 0; y < IMG_DO.getHeight(); y++) {
                int[] P = new int[4];
                for (int i = 0; i < Kernel.length; i++)
                    for (int j = 0; j < Kernel[i].length; j++)
                        for (int k = 0; k < P.length; k++)
                            P[k] += Kernel[i][j] * getPixel(IMG_temp, x + (i - KC), y + (j - KC))[k];
                paint(P, x, y);
                proFigure ++;
            }
        reloadImage();
        proFigure = -1;
        UdButton.setVisible(true);
        System.out.println("E. ");
    }


    /**
     * 按输入的核边长进行膨胀或腐蚀
     * @param Kl 核边长
     * @param E - True:Exp, False:Corr.
     */
    private void ExpCor(int Kl, boolean E) {
        System.out.print("[IC] S-");
        storageIMG(IMG_temp);
        int KC = (Kl - 1) / 2;
        for (int x = 0; x < IMG_DO.getWidth(); x++)
            for (int y = 0; y < IMG_DO.getHeight(); y++) {
                int[] P = getPixel(IMG_temp, x, y);
                for (int i = 0; i < Kl; i++)
                    for (int j = 0; j < Kl; j++)
                        for (int k = 0; k < P.length; k++)
                            P[k] = IMGKit.extreme(E, P[k], getPixel(IMG_temp, x + (i - KC), y + (j - KC))[k]);
                paint(P, x, y);
                proFigure ++;
            }
        reloadImage();
        proFigure = -1;
        UdButton.setVisible(true);
        System.out.println("E. ");
    }









    private void initialButton(JButton button, int locationY) {
        button.setLocation(widthF-140 +RMC, locationY);
        button.setSize(128, 36);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setFont(new Font("黑体", Font.PLAIN, 18));
    }




}

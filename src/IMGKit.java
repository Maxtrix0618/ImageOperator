import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * 提供图像处理常用的静态计算方法，勿实例化<br/>
 * IMGKit / 2023.9.7 by_Maxtrix
 */
public class IMGKit {
    private IMGKit() {}

    /**
     * 最大值或最小值
     */
    public static int extreme(boolean max, int a, int b) {
        return (max) ? (Math.max(a,b)) : (Math.min(a,b));
    }

    /**
     * 一个坐标周围的相对坐标
     */
    public static int[] dxs_4 = {-1, 1, 0, 0};
    public static int[] dys_4 = {0, 0, -1, 1};
    public static int[] dxs_8 = {-1, 0, 1, 1, 1, 0, -1, -1};
    public static int[] dys_8 = {-1, -1, -1, 0, 1, 1, 1, 0};

    /**
     * 若in在low到high之间则返回in; 否则返回low(in小于low)或high(in大于high).
     */
    public static int LimitedValue(int in, int low, int high) {
        return Math.min(Math.max(in, low), high);
    }

    /**
     * 判断坐标位置是否超出图像边界
     */
    public static boolean outsideArea(BufferedImage BI, int x, int y) {
        return (x < 0 || y < 0 || x >= BI.getWidth() || y >= BI.getHeight());
    }

    /**
     * 在图像(x,y)处绘制(a,r,g,b)像素点.
     */
    public static void paint(BufferedImage BI, int a, int r, int g, int b, int x, int y) {
        if (outsideArea(BI, x, y)) return;
        int p = (a << 24) | (r << 16) | (g << 8) | b;
        BI.setRGB(x, y, p);
    }
    public static void paint(BufferedImage BI, int[] ps, int x, int y) {
        paint(BI, ps[0], ps[1], ps[2], ps[3], x, y);
    }

    /**
     * 在图像的(x,y)处绘制空白像素（将alpha和rgb色值全改为0）
     */
    public static void paintEmpty(BufferedImage BI, int x, int y) {
        paint(BI, 0, 0, 0, 0, x, y);
    }

    /**
     * 在图像的(x,y)处绘制原像素的反色像素
     */
    public static void paintRev(BufferedImage BI, int x, int y) {
        int[] ARGB = getPixel(BI, x, y);
        paint(BI, ARGB[0], 255-ARGB[1], 255-ARGB[2], 255-ARGB[3], x, y);
    }

    /**
     * 给出图像IMG_DO在(x,y)处的像素色值信息，以int[4]返回（4个int数依此为a,r,g,b的值）
     */
    public static int[] getPixel(BufferedImage BI, int x, int y) {
        if (IMGKit.outsideArea(BI, x, y)) return new int[4];
        return getPixel(BI.getRGB(x, y));
    }
    public static int[] getPixel(int p) {
        int[] ARGB = new int[4];
        ARGB[0] = (p >> 24) & 0xff;
        ARGB[1] = (p >> 16) & 0xff;
        ARGB[2] = (p >> 8) & 0xff;
        ARGB[3] = p & 0xff;
        return ARGB;
    }


    /**
     * 返回一个与OriBI数据数组完全相同的新副本
     */
    public static BufferedImage CloneNewIMG(BufferedImage OriBI) {
        BufferedImage NewBI = new BufferedImage(OriBI.getWidth(), OriBI.getHeight(), OriBI.getType());
        IMG_clone(OriBI, NewBI);
        return NewBI;
    }
    /**
     * 将OriBI的数据数组复制到NewBI上
     */
    public static void IMG_clone(BufferedImage OriBI, BufferedImage NewBI) {
        NewBI.setData(OriBI.getData());
    }


    /**
     * 展示窗口背景色库
     */
    public static final Color[] EbgCs = new Color[]{
            new Color(0, 0, 0, 0),
            new Color(255, 255, 255, 255),
            new Color(0, 0, 0, 255),
            new Color(128, 128, 128, 255),
    };


    /**
     * 画虚线
     * @param w1 起坐标
     * @param w2 尾坐标
     * @param z 定坐标
     * @param segment 间段长
     * @param horizon true:水平; false:竖直.
     */
    public static void drawDashLine(Graphics g, int w1, int w2, int z, int segment, boolean horizon) {
        if (w1 >= w2) return;

        double segN = (w2 - w1 +1.0) / segment;
        int reconSegN = (int) Math.ceil((segN+1) / 2);
        boolean Odd = (reconSegN %2 != 0);

        int start = (w2 + w1) / 2;
        int head = 0;
        int tail = segment/2 -1;
        if (Odd && start+tail <= w2 && start-tail >= w1) drawLineSymmetrically(g, start, head, tail, z, horizon);
        head = tail +1;
        tail = head + segment -1;
        Odd = !Odd;

        while (start+tail <= w2 && start-tail >= w1) {
            if (Odd) drawLineSymmetrically(g, start, head, tail, z, horizon);
            Odd = !Odd;
            head = tail +1;
            tail = head + segment -1;
        }
        if (Odd) drawLineSymmetrically(g, start, head, w2-start, z, horizon);

    }
    private static void drawLineSymmetrically(Graphics g, int start, int head, int tail, int z, boolean horizon) {
        if (horizon) {
            g.drawLine(start + head, z, start + tail, z);
            g.drawLine(start - head, z, start - tail, z);
        }
        else {
            g.drawLine(z, start + head, z, start + tail);
            g.drawLine(z, start - head, z, start - tail);
        }
    }

    /**
     * 从绝对路径返回图片
     */
    public static BufferedImage inputIMG(String path) {
        try {
            return ImageIO.read(new File(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 保存图片到固有路径的指定名字
     */
    public static void saveImage(BufferedImage BI, String path, JFrame parentComponent) {
        String FileName = JOptionPane.showInputDialog(parentComponent,"图片名：", "保存图片", JOptionPane.INFORMATION_MESSAGE);
        String savePath = path + FileName + ".png";
        if (new File(savePath).exists()) {
            JOptionPane.showMessageDialog(parentComponent, "同名文件存在", "保存失败", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (FileName == null) return;
        try {
            ImageIO.write(BI, "png", new File(savePath));
            JOptionPane.showMessageDialog(parentComponent, "图片已成功保存到：\n" + savePath, "保存成功", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }


}

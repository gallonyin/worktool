package org.yameida.worktool.utils.capture;

public class Color {
    private int R = 0;
    private int G = 0;
    private int B = 0;


    public Color(int r, int g, int b) {
        R = r;
        G = g;
        B = b;
    }

    public Color(int b, int g, int r, Boolean bgr) {
        if (bgr) {
            R = r;
            G = g;
            B = b;
        } else {
            R = b;
            G = g;
            B = r;
        }
    }

    public Color(int color) {
        R = android.graphics.Color.red(color);
        G = android.graphics.Color.green(color);
        B = android.graphics.Color.blue(color);
    }

    public Color() {
    }

    public int getR() {
        return R;
    }

    public void setR(int r) {
        R = r;
    }

    public int getG() {
        return G;
    }

    public void setG(int g) {
        G = g;
    }

    public int getB() {
        return B;
    }

    public void setB(int b) {
        B = b;
    }


    public int[] getDexRGB() {
        return new int[]{R, G, B};
    }

    public int[] getDexBGR() {
        return new int[]{B, G, R};
    }


    /**
     * 判断2个颜色是否相同，由于图像渲染叠加，同一个icon可能每次渲染的色值不完全相同，所以判断的时候加入了误差
     *
     * @param color1
     * @param color2
     * @return
     */
    public static boolean isSame(Color color1, Color color2) {
        return isSame(color1, color2, 30);
    }

    /**
     * 判断颜色是否相同，自定义误差
     *
     * @param color1
     * @param color2
     * @param offset
     * @return
     */
    public static boolean isSame(Color color1, Color color2, int offset) {

        // 算法： R G B这3个通道的色值差的绝对值之和小于offset
        return (Math.abs(color1.getR() - color2.getR()) + Math.abs(color1.getG() - color2.getG()) + Math.abs(color1.getB() - color2.getB())) <= offset;
    }

    public boolean equals(Color obj) {
        return obj.getR() == R && obj.getG() == G && obj.getB() == B;
    }

    @Override
    public String toString() {
        return "" + R + "," + G + "," + B;
    }
}

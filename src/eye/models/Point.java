package eye.models;

import java.awt.image.BufferedImage;

/**
 *
 * @author gpdribbler
 * @version $Id: Point.java 24 2010-06-30 07:43:45Z spr1ng $
 */
public class Point {

    private int x;
    private int y;   
    private BufferedImage image;
    
    public Point() {
    }
    
    public Point(int x, int y, BufferedImage image) {
        this.x = x;
        this.y = y;
        this.image = image;        
    }

    //TODO: вычислять расстояние с поправкой на рубежность точки?
    public int compareTo(Point p) {
        int result =  (x - p.getX()) * (x - p.getX());
        result += (y - p.getY()) * (y - p.getY());
        return (int) Math.sqrt(result);
    }
    
    public boolean isBorder(int limit) {
        int diff = 0;
        for(int i = -1; i <= 1; i++) 
            for(int j = -1; j <= 1; j++) 
                diff += getRGBDifference(new Point(x+i, y+j, image));
        diff /= 8;
        if(diff > limit) {
            return true;
        } else {
            return false;
        }
    }//isBorderPixel
    
    public int getRGBDifference(Point point) {
        int rgb1 = image.getRGB(x, y);
        int rgb2 = image.getRGB(point.getX(), point.getY());

        int red1 = (rgb1 & 0x00ff0000) >> 16;
        int green1 = (rgb1 & 0x0000ff00) >> 8;
        int blue1 = (rgb1 & 0x000000ff);

        int red2 = (rgb2 & 0x00ff0000) >> 16;
        int green2 = (rgb2 & 0x0000ff00) >> 8;
        int blue2 = (rgb2 & 0x000000ff);
        
        return (int) Math.sqrt( (red1 - red2)*(red1 - red2) + (green1 - green2) 
                * (green1 - green2) + (blue1 - blue2) * (blue1 - blue2) );
    }//getDifference


    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public BufferedImage getImage() {
        return image;
    }

    public void setImage(BufferedImage image) {
        this.image = image;
    }
    
}

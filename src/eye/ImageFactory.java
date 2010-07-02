package eye;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.imageio.ImageIO;
import eye.models.Point;
import eye.models.Image;
import static eye.net.InetUtils.*;

/**
 *
 * @author gpdribbler, spr1ng
 * @version $Id: ImageFactory.java 28 2010-07-01 05:35:24Z spr1ng $
 */
public class ImageFactory {

    private final static int DEPTH = 25;
    //TODO: сделать дисперсию долевой от размеров картинки
    private final static int DISPERSION = 20;
    private final static int MIN_WIDTH = 128;
    private final static int MIN_HEIGHT = 128;

    private BufferedImage bimg;
    private ArrayList<Point> points;

    public void load(File file) throws IOException {
        bimg = ImageIO.read(file);
    }

    public void load(URL url) throws IOException {
        //Зададим-ка мы проверочку на таймаут
        if (isReachable(url.toString(), 3000))
            bimg = ImageIO.read(url);
        else throw new IIException("Connection timeout");
    }

    /**
     * Создает BufferedImage из потока
     * @param in входящий поток
     * @throws IOException
     */
    public void load(InputStream in) throws IOException {
        bimg = ImageIO.read(in);
    }

    public void saveToFile(String fileName) throws IOException {
        ImageIO.write(bimg, getExtension(fileName), new File(fileName));
    }
    
    /** 
     * Saves to output stream
     * @param imgExtension
     * @param out
     * @throws IOException
     */
    public void saveToOutputStream(String imgExtension, OutputStream out) throws IOException {
        ImageIO.write(bimg, imgExtension, out);
    }

    /**
     * Рисует области вокруг точек белым цветом
     * @param points
     */
    public void draw(List<Point> points) {
        draw(points, Color.WHITE);
    }
    
    /**
     * Рисует области вокруг точек выбранным цветом
     * @param points
     * @param color
     */
    public void draw(List<Point> points, Color color){
        int d = 20;
        Graphics2D graphics = bimg.createGraphics();
        for (Iterator<Point> it = points.iterator(); it.hasNext();) {
            Point point = it.next();
            graphics.setColor(color);
            graphics.drawOval(point.getX()-(d/2), point.getY()-(d/2), d, d);
        }
    }

    public List<Point> seekPoints(InputStream in) throws IOException{
        load(in);
        return seekPoints();
    }

    public List<Point> seekPoints(){
        try {
            Kernel kernel = new Kernel(3, 3, new float[]{1f / 9f, 1f / 9f, 1f / 9f,
                        1f / 9f, 1f / 9f, 1f / 9f, 1f / 9f, 1f / 9f, 1f / 9f});
            BufferedImageOp op = new ConvolveOp(kernel);
            bimg = op.filter(bimg, null);
        } catch (Exception e) {
        }

        points = new ArrayList<Point>();

        //TODO: проверить значение декремента
        int w = bimg.getWidth()-4;
        int h = bimg.getHeight()-4;
        for(int i = 2; i < w; i++)
            for(int j = 2; j < h; j++) {
                Point point = new Point(i, j, bimg);
                if(point.isBorder(DEPTH)) points.add(point);
            }//for

        //TODO: увеличить эффективность за счет просмотра только ближайших точек
        for(int i = 0; i < points.size(); i++)
            for(int j = i+1; j < points.size(); j++)
                if(points.get(i).compareTo(points.get(j)) < DISPERSION) {
                    points.remove(j);
                    j--;
                }

         return points;

    }//seekPoints()

    private boolean hasValidSize() {
        if( (bimg.getWidth() >= MIN_WIDTH) && (bimg.getHeight() >= MIN_HEIGHT) ) {
            return true;
        } else {
            return false;
        }
    }//checkMeasurement

    public void produceMetaImage(Image image) throws IOException {
        load(new URL(image.getUrl()));
        
        if(hasValidSize()) {
            image.setPoints(seekPoints());
            System.out.println(image);
        } else {
            throw new IIException("Invalid image size.");
        }
    }

    /**
     * Gets file extension
     * @param fileName
     * @return
     */
    public static String getExtension(String fileName){
        return fileName.replaceFirst(".*\\.", "");
    }

}

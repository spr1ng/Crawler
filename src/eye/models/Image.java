package eye.models;

import java.util.Iterator;
import java.util.List;

/**
 *
 * @author gpdribbler, spr1ng, stream
 * @version $Id: Image.java 24 2010-06-30 07:43:45Z spr1ng $
 */
public class Image implements RemoteSource{
    
    private String url;
    private List<Point> points;

    public Image() {
    }

    public Image(String url) {
        this.url = url;
    }

    public Image(String url, List<Point> points) {
        this.url = url;
        this.points = points;
    }

    public List<Point> getPoints() {
        return points;
    }

    public void setPoints(List<Point> points) {
        this.points = points;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        String result = "";//url.toString();
        result += ": " + points.size() + " points";
        return result;
    }

    //TODO: реализовать    
    public int compareTo(Image image) {
        int result = 0;
        for (Iterator<Point> it = points.iterator(); it.hasNext();)
            for (Iterator<Point> it1 = image.getPoints().iterator(); it.hasNext();)
                result += it.next().compareTo(it1.next());
        return result;
    }

    public boolean hasMetaData(){
        if (points == null) return false;
        return true;
    }

}

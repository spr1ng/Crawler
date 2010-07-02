package eye.models;

import java.util.Date;

/**
 *
 * @author gpdribbler, spr1ng, stream
 * @version $Id: Place.java 24 2010-06-30 07:43:45Z spr1ng $
 */
public class Place implements RemoteSource{

    private String url;
    private Date date;

    public Place() {        
    }

    public Place(String url, Date date) {
        this.url = url;
        this.date = date;
    }

    public Place(String url) {
        this.url = url;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return url + " " + String.valueOf(date);
    } 
 
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eye;

import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;
import com.db4o.cs.Db4oClientServer;
import db4oserver.AbstractDBManager;
import db4oserver.ConfigLoader;
import eye.models.Image;
import eye.models.Place;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 * @author spr1ng
 * @version $Id: DBManagerEyeImpl.java 28 2010-07-01 05:35:24Z spr1ng $
 */
public class DBManagerEyeImpl extends AbstractDBManager{
    
    private static ConfigLoader conf = ConfigLoader.getInstance();
    public static final Logger LOG = Logger.getLogger(DBManagerEyeImpl.class);

    public ObjectContainer getContainer() {
        return Db4oClientServer.openClient(Db4oClientServer
                .newClientConfiguration(), conf.getHost(), conf.getPort(), conf.getUser(), conf.getPass());
    }

    public void watchDb() {
        ObjectContainer db = getContainer();
        try {
            watchDb(db);
        } finally {
            db.close();
        }
    }

    public void watchDb(ObjectContainer db) {
        ObjectSet objects = db.query().execute();

        int places = 0, images = 0;
        for (Object object : objects) {
            if (object instanceof Place) {
                places++;
                Place p = (Place) object;
                LOG.info("Place url: " + p.getUrl());
                if (p.getDate() != null) {
                    LOG.info("Place data: " + p.getDate());
                }
            }
        }
        LOG.info("-------> Total places: " + places);
        for (Object object : objects) {
            if (object instanceof Image) {
                images++;
                Image i = (Image) object;
                LOG.info("Image: " + i.getUrl());
                if (i.getPoints() != null) {
                    LOG.info("!!!!!!!!!!!!!!!!!!!!!!!Points: " + i.getPoints());
                }
            }
        }
        LOG.info("-------> Total images: " + images);

        LOG.info("Total objects: " + objects.size());
    }

    /**
     * Сохраняет все объекты списка в базу. Возвращает количество новых
     * сохраненных объектов
     * @param objects
     */
    public int store(List objects) {
        if (objects == null) {
            LOG.error("A try to store a null list of objects");
            return 0;
        }
        ObjectContainer db = getContainer();
        int qty = 0;
        try {
            for (Object o : objects) {
                if (o != null) {
                    //Если такого объекта еще нет в базе
                    if (!isItemStored(o)) {
                        db.store(o);
                        qty++;
                    } //else LOG.error("A try to store a duplicate object"); //DELME:
                } else LOG.error("A try to store a null object");
            }
        } finally { db.close(); }

        return qty;
    }

    /** Сохраняет место поиска глаза */
    public void storePlace(String firstPlace) {
        //Инициализируем первое место поиска
        try {
            new URL(firstPlace);
        } catch (MalformedURLException ex) {
            LOG.error("Can't reach target: " + firstPlace, ex);
        }
        List<Place> places = new ArrayList<Place>(1);
        places.add(new Place(firstPlace));
        store(places);
    }

}

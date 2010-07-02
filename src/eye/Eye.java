package eye;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Date;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.net.URL;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.Set;
import org.apache.log4j.Logger;
import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;
import com.db4o.query.Query;

import eye.models.Image;
import eye.models.Place;
import static eye.net.InetUtils.*;

/**
 * The Eye - yet another crawler ;)
 * @author gpdribbler, spr1ng
 * @version $Id: Eye.java 28 2010-07-01 05:35:24Z spr1ng $
 */
public class Eye {
    private static final Logger LOG = Logger.getLogger(Eye.class);
    private static DBManagerEyeImpl dbm = new DBManagerEyeImpl();

    public Eye() {
//        setProxy("cmdeviant", 3128, "alexey", "ahGueb1e");        
    }

    public List<Place> grubPlaces(String pageSource) {
        List<String> regExps = new ArrayList<String>(9);
        regExps.add(".+js$");
        regExps.add(".+xml$");
        regExps.add(".+swf$");
        regExps.add(".+dtd$");
        regExps.add(".+jpg$");
        regExps.add(".+gif$");
        regExps.add(".+bmp$");
        regExps.add(".+png$");
        //TODO: добавить подждержку ico
        regExps.add(".+ico$");

        List<Place> places = new ArrayList<Place>();
        Set<URL> placeUrls = grubURLs(regExps, true);
        for (URL placeUrl : placeUrls) {
            places.add(new Place(placeUrl.toString()));//PENDING
        }

        return places;
    }

    public List<Image> grubImages(String pageSource) {
        List<String> regExps = new ArrayList<String>(4);
        regExps.add(".+jpg$");
        regExps.add(".+gif$");
        regExps.add(".+bmp$");
        regExps.add(".+png$");

        List<Image> images = new ArrayList<Image>();
        Set<URL> imageUrls = grubURLs(regExps);
        for (URL imageUrl : imageUrls) {
            try {
                images.add(new Image(imageUrl.toString()));
            } catch (Exception ex) {
                LOG.error("Can't produce image from url " + imageUrl);
            }
        }

        return images;
    }

    /**
     * 
     * @param pageSource
     * @param regExps
     * @param isExcludeMode если true - url-ки, соответствующие регуляркам
     * не будут включены в список, иначе - будут включены только те, которые
     * соответствуют регуляркам
     * @return
     */
    public Set<URL> grubURLs(List<String> regExps, boolean isExcludeMode) {
        Set<URL> urls = new HashSet<URL>();
        String urlRegExp = "https?://[-_=+/,.?!&%~:|;#\\w\\d]+";//PENDING
        
        String[] words = pageSource.split("\\s");
        for (String word : words) {
            word = word.replaceFirst(".*?http", "http");
            if (!word.startsWith("http")){
                word = word.replaceFirst(".*?href=\"/", "http://" + domain + "/");
                word = word.replaceFirst(".*?href='/", "http://" + domain + "/");
                word = word.replaceFirst(".*?src=\"/", "http://" + domain + "/");
                word = word.replaceFirst(".*?src='/", "http://" + domain + "/");
            }
            String url = findRegexp(word, urlRegExp);
            if (url != null) {
                try {
                    url = url.replaceFirst("/$", "");//убираем последний слэшик, чтобы избежать дублирования ссылок
                    urls.add(new URL(url));
                } catch (MalformedURLException ex) {
                    LOG.error("Incorrect url: " + url, ex);
                }
            }
        }
        //Если вызван без регулярок - возвращаем все url
        if (regExps == null) {
            return urls;
        }
        for (Iterator<URL> url = urls.iterator(); url.hasNext();) {
            String res = findRegexp(url.next().toString(), regExps);
            if (isExcludeMode) {
                if (res != null) {
                    url.remove();
                }
            } else {
                if (res == null) {
                    url.remove();
                }
            }
        }
        return urls;
    }

    /**
     * Возвращает список всех url, найденных в исходнике страницы
     * @param pageSource
     * @return
     */
    public Set<URL> grubURLs() {
        return grubURLs(null, false);
    }

    /**
     * Возвращает список url, найденных в исходнике страницы, соответсвующим регуляркам
     * @param pageSource
     * @return
     */
    public Set<URL> grubURLs(List<String> regExps) {
        return grubURLs(regExps, false);
    }

    private static String pageSource = "";
    private static String domain = "";
    public void run() {
        ObjectContainer db = dbm.getContainer();
        try {

            Runnable imagesGrubbing = new Runnable() {
                public void run() {
                    List<Image> images = grubImages(pageSource);
                    if (images.size() > 0) {
                        long t1 = System.currentTimeMillis();
                        int savedQty = dbm.store(images);
                        long t2 = System.currentTimeMillis() - t1;
                        if (savedQty > 0)
                            LOG.info("---------> Storing new images [" + savedQty + "] done. --> " + t2 + " ms");
                    }
                }
            };

            Runnable placesGrubbing = new Runnable() {
                public void run() {
                    List<Place> places = grubPlaces(pageSource);
                    if (places.size() > 0) {
                        long t1 = System.currentTimeMillis();
                        int savedQty = dbm.store(places);
                        long t2 = System.currentTimeMillis() - t1;
                        if (savedQty > 0)
                            LOG.info("---------> Storing new places [" + savedQty + "] done. --> " + t2 + " ms");
                    }
                }
            };

            Query query = db.query();
            query.constrain(Place.class);
            query.descend("date").orderAscending();//те, что без даты сортируются наверх

            int placeIdx = 0;
            ObjectSet<Place> places = query.execute();
            LOG.info("Places in db: " + places.size());

            for (Place p : places) {
                try {
                    placeIdx++;
                    LOG.info("[" + placeIdx + "/" + places.size() + "] Seeking in " + p.getUrl());
                    //Данные для grubURL();
                    URL u = new URL(p.getUrl());
                    pageSource = getPageSource(u);
                    domain = u.getHost();

                    ThreadGroup tg = new ThreadGroup("sources");
                    new Thread(tg, imagesGrubbing).start();
                    new Thread(tg, placesGrubbing).start();

                    //Ждем завершения потоков..
                    while(tg.activeCount() > 0) Thread.sleep(10);

                    p.setDate(new Date());
                    db.store(p);
                } catch (Exception ex) {
                    db.delete(p);
                    LOG.error(ex);
                } finally {
                    db.commit();
                }
            }
        } finally {
            db.close();
        }

    }//seek



    public static void main(String[] args) throws InterruptedException, MalformedURLException {
        if (!hasInetConnection()) {
            LOG.error("Inet is unreachable! =(");
            System.exit(1);
        }

        Eye eye = new Eye();
        dbm.storePlace("http://ya.ru");
//        eye.watchDb();
        eye.run();
        /*while (true) {
            eye.run();
            Thread.sleep(1000);
        }*/
        /*URL u = new URL("http://ya.ru");
        pageSource = eye.getPageSource(u);
        domain = u.getHost();
        Set<URL> urls = eye.grubURLs();
        for (URL url : urls) {
            System.out.println(url);
        }
        System.out.println("size: " + urls.size());*/
    }

    /**
     * Находит фрагмент строки из source, соответстующий любой регулярке из regs
     * @param source
     * @param regs
     * @return
     */
    private String findRegexp(String source, List<String> regs) {
        for (String reg : regs) {
            Pattern pattern = Pattern.compile(reg);
            Matcher matcher = pattern.matcher(source.toLowerCase());
            if (matcher.find()) {
                return matcher.group();
            }
        }
        return null;
    }

    /**
     * Находит фрагмент строки из source, соответстующий регулярке из reg
     * @param source
     * @param reg
     * @return
     */
    private String findRegexp(String source, String reg) {
        List<String> regs = new ArrayList<String>(1);
        regs.add(reg);
        return findRegexp(source, regs);
    }

    private String getPageSource(URL url) {
        StringBuilder response = new StringBuilder();

        HttpURLConnection con;
        try {
            con = (HttpURLConnection) url.openConnection();
//            con.setRequestProperty("Accept-Charset", "cp1251");
            con.connect();
            Scanner scanner = new Scanner(con.getInputStream());
            while (scanner.hasNext()) {
                response.append(scanner.nextLine());
            }

            con.disconnect();
        } catch (Exception ex) {
            LOG.error("The place is unreachable: " + url);
            return "";
        }

        return response.toString();
    }

}

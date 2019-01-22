package jaxb;

import generated.Node;
import generated.Osm;
import generated.Tag;
import generated.Way;
import stax.StreetData;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

/**
 * Обрабатывает XML-файл с помощью JAXB
 * C помощью xjc были сгенерированы классы по схеме osm.xsd
 */
public class JAXBReader {
    private String filename;
    private ArrayList<String> busStops;
    private TreeMap<String, StreetData> streets;

    public JAXBReader(String filename) {
        this.filename=filename;
        busStops=new ArrayList<String>();
        streets=new TreeMap<String,StreetData>();
    }

    public ArrayList<String> getBusStops() {
        return busStops;
    }

    public TreeMap<String, StreetData> getStreets() {
        return streets;
    }

    /**
     * Загрузка (демаршализация) объекта класса Osm из XML-файла
     * @return  список объектов, которые были дочерними тегами тега osm в XML-файле
     */
    private ArrayList<Object> unmarshal() {
        Osm root=null;
        try {
            JAXBContext jxc = JAXBContext.newInstance(Osm.class);
            Unmarshaller u = jxc.createUnmarshaller();
            root=(Osm)u.unmarshal(new File(filename));

        }
        catch(JAXBException e) {
            e.printStackTrace();
        }

        return root!=null? (ArrayList<Object>)root.getBoundOrUserOrPreferences(): null;
    }

    public void process() {
        ArrayList<Object> list=unmarshal();
        fillListOfBusStops(list);
        fillListOfStreets(list);
        printStreets();
    }

    /**
     * Формирование списка остановок общественного транспорта
     * {@code <node>     <tag k='highway' v='bus_stop' />}
     * @param list  список объектов, которые были дочерними тегами тега osm в XML-файле
     */
    private void fillListOfBusStops(ArrayList<Object> list) {
        for(Object obj:list) {
            if(obj instanceof Node) {
                boolean isBusStop=false;
                String name="";
                for(Tag tag:((Node) obj).getTag()) {
                    if (tag.getK().equals("highway") && tag.getV().equals("bus_stop"))
                        isBusStop = true;
                    else if (tag.getK().equals("name"))
                        name=tag.getV();
                }
                if(isBusStop && !name.equals(""))
                    busStops.add(name);
            }
        }
    }

    /**
     * Формирование списка улиц
     * тег {@code <way>} c вложенным тегом highway=* , у которого есть {@code <tag k='name'}
     * @param list  список объектов, которые были дочерними тегами тега osm в XML-файле
     */
    private void fillListOfStreets(ArrayList<Object> list) {
        for (Object obj : list) {
            if (obj instanceof Way) {
                String name="";
                long id=0;
                String typeRoad="";
                for(Object compWay:((Way) obj).getRest()) {
                    if(compWay instanceof Tag) {
                        Tag tag=(Tag) compWay;
                        String k = tag.getK();
                        if(k.equals("cladr:code"))
                            id=Long.parseLong(tag.getV());
                        if(k.equals("highway"))
                            typeRoad=tag.getV();
                        if(k.equals("name")) {
                            name=tag.getV();
                        }
                    }
                }
                if(streets.containsKey(name))
                    streets.get(name).addPartStreet(typeRoad);
                else if(!name.equals("") && !typeRoad.isEmpty())
                    streets.put(name, new StreetData(id,typeRoad));

            }
        }
    }

    private void printStreets() {
        for (Map.Entry<String, StreetData> entry : streets.entrySet()) {
            System.out.println(entry.getKey()+"\t"+entry.getValue().toString());
        }
    }
}

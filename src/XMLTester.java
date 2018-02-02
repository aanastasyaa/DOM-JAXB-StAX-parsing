import dom.SVGReader;
import jaxb.JAXBReader;
import org.junit.Test;
import stax.StAXReader;
import stax.StreetData;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static junit.framework.TestCase.assertTrue;

public class XMLTester {

    @Test
    public void testDOM() {
        SVGReader reader=new SVGReader("demo.svg","out.svg");
        SVGReader reader2=new SVGReader("clouds.svg","out2.svg");
        reader.process();
        reader2.process();
    }

    @Test
    public void testStAX() {
        StAXReader reader=new StAXReader("UfaCenter.xml","osm.xsd");
        reader.process();
        System.out.println(reader.getBusStops().toString());
    }

    @Test
    public void testJAXB() {
        JAXBReader reader=new JAXBReader("UfaCenter.xml");
        reader.process();
        System.out.println(reader.getBusStops().toString());
    }

    @Test
    public void compareLists() {
        JAXBReader reader=new JAXBReader("UfaCenter.xml");
        System.out.println("JAXB");
        reader.process();
        StAXReader reader2=new StAXReader("UfaCenter.xml","osm.xsd");
        System.out.println("StAX");
        reader2.process();
        List<String> busStops=reader.getBusStops();
        busStops.removeAll(reader2.getBusStops());
        assertTrue(busStops.isEmpty());
        TreeMap<String, StreetData> streetsStAX=reader2.getStreets();
        boolean equal=true;
        for(Map.Entry<String, StreetData> entry : reader.getStreets().entrySet()) {
            if(streetsStAX.containsKey(entry.getKey())){
                if(!entry.getValue().toString().equals(streetsStAX.get(entry.getKey()).toString())) {
                    equal = false;
                    break;
                }
                streetsStAX.remove(entry.getKey());
            }
            else {
                equal = false;
                break;
            }
        }
        assertTrue(equal && streetsStAX.entrySet().isEmpty());
    }
}

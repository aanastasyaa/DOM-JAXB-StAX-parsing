package stax;

import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

public class StAXReader {
    private String filename;
    private String schemaFile;
    private ArrayList<String> busStops;
    private TreeMap<String, StreetData> streets;
    public StAXReader(String filename, String schema) {
        this.filename=filename;
        this.schemaFile =schema;
        busStops=new ArrayList<String>();
        streets=new TreeMap<String,StreetData>();
    }

    public TreeMap<String, StreetData> getStreets() {
        return streets;
    }

    public ArrayList<String> getBusStops() {
        return busStops;
    }

    public void process() {
        readStax();
        printStreets();
    }

    private void readStax() {
        try {
            XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
            XMLStreamReader reader = xmlInputFactory.createXMLStreamReader(new FileReader(filename));

            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = factory.newSchema(new File(schemaFile));
            Validator validator = schema.newValidator();
            validator.validate(new StreamSource(new File(filename)));

            boolean isBusStop=false;
            boolean isWay=false;
            //StreetData
            long id=0;
            String typeRoad="";
            //name
            String name="";

            while (reader.hasNext()) {
                int event=reader.next();
                switch(event) {
                    case XMLEvent.START_ELEMENT:
                        if(reader.getLocalName().equals("tag")) {
                            String k = reader.getAttributeValue("", "k");
                            String v = reader.getAttributeValue("", "v");
                            if (v.equals("bus_stop") && k.equals("highway"))
                                isBusStop = true;
                            else if (k.equals("name")) {
                                name=v;
                            }
                            if(isWay) {
                                if(k.equals("cladr:code"))
                                    id=Long.parseLong(v);
                                if(k.equals("highway"))
                                    typeRoad= v;

                            }
                        }
                        if(reader.getLocalName().equals("node")) {
                            isBusStop=false;
                            name="";
                        }

                        if(reader.getLocalName().equals("way")) {
                            typeRoad = "";
                            id =0;
                            name="";
                            isWay=true;
                        }
                        break;
                    case XMLEvent.END_ELEMENT:
                        if(reader.getLocalName().equals("node")) {
                            if(isBusStop)
                                busStops.add(name);
                            isBusStop=false;
                            name="";
                        }
                        if(reader.getLocalName().equals("way")) {
                            isWay=false;
                            if(streets.containsKey(name))
                                streets.get(name).addPartStreet(typeRoad);
                            else if (!name.equals("") && !typeRoad.equals("")) {
                                streets.put(name,new StreetData(id, typeRoad));
                            }
                            id=0;
                            name="";
                            typeRoad = "";
                        }
                        break;
                }
            }
            reader.close();
        }

        catch(XMLStreamException e) {
            e.printStackTrace();
        }
        catch (SAXException e) {
            e.printStackTrace();
        }
        catch(IOException e) {
            e.printStackTrace();
        }
    }

    private void printStreets() {
        for (Map.Entry<String, StreetData> entry : streets.entrySet()) {
            System.out.println(entry.getKey()+"\t"+entry.getValue().toString());
        }
    }
}

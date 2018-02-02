package dom;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;

public class SVGReader {
    private String filename;
    private String saveFile;
    public SVGReader(String filename, String saveFileName) {
        this.filename=filename;
        saveFile=saveFileName;
    }

    private Document readSVGToDocument() throws SAXException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true); // поддерживать пространства имен

        documentBuilderFactory.setIgnoringElementContentWhitespace(true);
        documentBuilderFactory.setValidating(false);
        try {
            DocumentBuilder documentBuilder=documentBuilderFactory.newDocumentBuilder();
            return documentBuilder.parse(filename);
        }
        catch(ParserConfigurationException e) {
            e.printStackTrace();
            return null;
        }
        catch(IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void process() {
        try {
            Document doc=readSVGToDocument();
            Element root=doc.getDocumentElement();
            printNodeList(root,"rect");
            printNodeList(root,"circle");
            refillElements(root, "rect", "white");
            refillElements(root, "path","black");
            createCircleInCircle(root);
            save(doc);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void printNodeList(Element root, String tagname) {
        NodeList listNodes=root.getElementsByTagName(tagname);
        StringWriter writer = new StringWriter();
        try {
            Transformer t = TransformerFactory
                    .newInstance()
                    .newTransformer();
            t.setOutputProperty(OutputKeys.INDENT, "yes");
            t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            StreamResult outputTarget = new StreamResult(writer);
            for (int i = 0; i < listNodes.getLength(); ++i) {
                //System.out.println(toString(listNodes.item(i)));
                t.transform(new DOMSource(listNodes.item(i)), outputTarget);
            }

        }
        catch (TransformerException e) {
            e.printStackTrace();
        }
        System.out.println(writer);
    }

    private void refillElements(Element root, String tagname, String color) {
        NodeList listNodes=root.getElementsByTagName(tagname);
        for(int i=0; i<listNodes.getLength(); ++i) {
            Element el=(Element) listNodes.item(i);

            if(el.hasAttribute("fill")) {
                el.setAttribute("fill",color);
            }
            else if(el.hasAttribute("style")) {
                el.setAttribute("style", "fill: "+color+";");
            }//иначе создаем атрибут fill
            else
                el.setAttribute("fill", color);

        }
    }

    private void createCircleInCircle(Element root) {
        NodeList listNodes=root.getElementsByTagName("circle");
        int len=listNodes.getLength();
        for(int i=0; i<listNodes.getLength(); i+=2) {
            Node item = listNodes.item(i);
            Node circle= item.cloneNode(false);
            ((Element)circle).setAttribute("r","10");
            ((Element)circle).setAttribute("fill","red");

            item.getParentNode().insertBefore(circle, item.getNextSibling());//после текущего circle
        }
    }

    private void save(Document doc) throws IOException {
        Result sr = new StreamResult(new FileWriter(saveFile));
        DOMSource domSource = new DOMSource(doc);
        Transformer tr;
        try {
            tr = TransformerFactory
                    .newInstance()
                    .newTransformer();
            tr.transform(domSource, sr );
        }
        catch (TransformerException ex) {
            ex.printStackTrace();
        }
    }
}

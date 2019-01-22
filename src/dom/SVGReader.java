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

/**
 * Класс, обрабатывающий SVG-файл с помощью DOM.
 *
 */
public class SVGReader {

    private String filename;
    private String saveFile;//имя, с которым файл будет сохранен

    public SVGReader(String filename, String saveFileName) {
        this.filename=filename;
        saveFile=saveFileName;
    }

    /**
     * Разбор SVG-файла с помощью DocumentBuilder и создание Document
     * @return объект {@code Document}
     * @throws SAXException
     */
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

    /**
     * Метод, выполняющий задание ЛР
     */
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
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Находит в документе все дочерние узлы root с тегом tagname
     * @param root      корень документа
     * @param tagname   тег, по которому ведется поиск узлов
     */
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

    /**
     * У всех дочерних root узлов c тегом tagname устанавливает свойство fill = color,
     * т. е. закрашивает элементы с тегом tagname цветом color
     * @param root      корень документа
     * @param tagname   название тега, у которых будет изменено свойство fill
     * @param color     устанавливаемое значение свойства fill
     */
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

    /**
     * У окружностей (элементы с тегом circle, дочерние узлы root) отмечает центр красной точкой,
     * т. е. добавляет внутрь еще одну окружность с атрибутом fill = red
     * @param root   корень документа
     */
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

    /**
     * Сохраняет модифицированный документ под именем saveFile
     * с помощью Transform API.
     * @param doc   сохраняемый документ
     * @throws IOException
     */
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

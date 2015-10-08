package pro.alex_zaitsev.androidstyler;

import com.intellij.openapi.util.Pair;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Aleksandr on 06.10.2015.
 */
public class StylerEngine {

    /**
     * Gets lines with future style<br>
     * <code>android:layout_width="match_parent"<br>
     * android_layout_height="match_parent"</code>
     *
     * @param source
     * @return
     */
    public static String style(String name, String source) throws IOException, ParserConfigurationException, TransformerException {
        return createOutput(name, parseSource(source));
    }

    private static List<Pair<String, String>> parseSource(String source) throws IOException {
        BufferedReader bufReader = new BufferedReader(new StringReader(source));
        List<Pair<String, String>> lines = new ArrayList<>();
        String line;
        while ((line = bufReader.readLine()) != null) {
            try {
                Pair<String, String> attrValue = clearLine(line);
                if (attrValue != null) {
                    lines.add(attrValue);
                }
            } catch (Exception e) {
                // if line has bad format - ignore it
            }
        }
        return lines;
    }

    private static Pair<String, String> clearLine(String line) {
        String[] lineParts = line.split("=");
        String attribute = lineParts[0].trim();
        if (attribute.contains("xmlns")) {
            // skip namespaces
            return null;
        }
        String value = lineParts[1];
        value = value.substring(value.indexOf("\"") + 1, value.lastIndexOf("\""));
        return Pair.create(attribute, value);
    }

    private static String createOutput(String name, List<Pair<String, String>> xmlParams) throws ParserConfigurationException, TransformerException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        DOMImplementation impl = builder.getDOMImplementation();
        Document doc = impl.createDocument(null, // namespaceURI
                null, // qualifiedName
                null); // doctype
        Element styleElement = doc.createElement("style");
        styleElement.setAttribute("name", name);
        for (Pair<String, String> item : xmlParams) {
            Element itemElement= doc.createElement("item");
            itemElement.setAttribute("name", item.getFirst());
            itemElement.setTextContent(item.getSecond());
            styleElement.appendChild(itemElement);
        }
        doc.appendChild(styleElement);
        return docToStr(doc);
    }

    private static String docToStr(Document doc) throws TransformerException {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));
        return writer.toString().replaceAll("\\r\\n", "\n").replaceAll("<item", "        <item").replaceAll("</style", "    </style");
    }
}

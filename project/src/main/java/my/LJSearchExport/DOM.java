package my.LJSearchExport;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.w3c.dom.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.StreamResult;

import java.io.*;
import java.util.*;

import org.htmlcleaner.*;

public class DOM
{
    public static void out(String s) throws Exception
    {
        Main.out(s);
    }

    static ArrayList<Node> flatten(Node el) throws Exception
    {
        ArrayList<Node> vec = new ArrayList<Node>();
        flatten(vec, el);
        return vec;
    }

    static void flatten(ArrayList<Node> vec, Node el) throws Exception
    {
        if (el == null)
            return;
        vec.add(el);
        flatten(vec, el.getFirstChild());
        flatten(vec, el.getNextSibling());
    }

    public static Node parseHtmlAsXml(String html) throws Exception
    {
        CleanerProperties props = new CleanerProperties();
        props.setAdvancedXmlEscape(true);
        props.setTranslateSpecialEntities(true);
        props.setTransResCharsToNCR(true);
        props.setOmitComments(true);
        TagNode tagNode = new HtmlCleaner(props).clean(html);
        String xml = new PrettyXmlSerializer(props).getAsString(tagNode, "UTF-8");

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setValidating(false);
        dbf.setNamespaceAware(false);
        dbf.setCoalescing(true);
        dbf.setIgnoringComments(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        db.setEntityResolver(new EntityResolver()
        {
            @Override
            public InputSource resolveEntity(String publicId, String systemId)
                    throws SAXException, IOException
            {
                if (systemId.contains("foo.dtd"))
                {
                    return new InputSource(new StringReader(""));
                }
                else
                {
                    return null;
                }
            }
        });

        Document doc = null;

        xml = removeDoctype(xml);

        try
        {
            doc = db.parse(new InputSource(new StringReader(xml)));
        }
        catch (Exception ex)
        {
            out("Error parsing " + Web.getLastURL());
            out("Raw data follows:");
            out("=====================================================================");
            out(xml);
            out("=====================================================================");
            throw ex;
        }

        return doc.getDocumentElement();
    }

    private static String removeDoctype(String xml) throws Exception
    {
        final String pre = "<!doctype ";
        // final int prelen = pre.length();
        final String newdoctype = "<!DOCTYPE html>";

        int k1 = xml.toLowerCase().indexOf(pre);
        if (k1 == -1)
            return xml;

        int k2 = xml.indexOf('>', k1);
        if (k2 == -1)
            throw new Exception("Unterminated DOCTYPE");

        xml = xml.substring(0, k1) + newdoctype + xml.substring(k2 + 1);

        return xml;
    }

    public static ArrayList<String> extractHrefs(String html) throws Exception
    {
        ArrayList<String> vs = new ArrayList<String>();
        ArrayList<Node> vnodes = flatten(parseHtmlAsXml(html));

        for (Node n : vnodes)
        {
            if (!n.getNodeName().equalsIgnoreCase("a"))
                continue;
            if (!(n instanceof Element))
                continue;
            String href = getAttribute((Element) n, "href");
            if (href == null || href.equals("") || href.charAt(0) == '#')
                continue;
            vs.add(href);
        }

        return vs;
    }

    public static void removeElements(Node pageRoot, ArrayList<Node> pageFlat, String tagname, String an1, String av1)
            throws Exception
    {
        if (pageFlat == null)
            pageFlat = flatten(pageRoot);
        ArrayList<Node> vel = findElements(pageFlat, tagname, an1, av1);
        removeElements(pageRoot, vel);
    }

    public static void removeElements(Node pageRoot, ArrayList<Node> pageFlat, String tagname, String an1, String av1, String an2,
            String av2) throws Exception
    {
        if (pageFlat == null)
            pageFlat = flatten(pageRoot);
        ArrayList<Node> vel = findElements(pageFlat, tagname, an1, av1, an2, av2);
        removeElements(pageRoot, vel);
    }

    public static ArrayList<Node> findElements(ArrayList<Node> pageFlat, String tagname) throws Exception
    {
        ArrayList<Node> vel = new ArrayList<Node>();

        for (Node n : pageFlat)
        {
            if (!(n instanceof Element))
                continue;
            if (!n.getNodeName().equalsIgnoreCase(tagname))
                continue;

            vel.add(n);
        }

        return vel;
    }

    public static ArrayList<Node> findElements(ArrayList<Node> pageFlat, String tagname, String an1, String av1) throws Exception
    {
        ArrayList<Node> vel = new ArrayList<Node>();
        Element el;
        String av;

        for (Node n : pageFlat)
        {
            if (!(n instanceof Element))
                continue;
            if (!n.getNodeName().equalsIgnoreCase(tagname))
                continue;

            el = (Element) n;

            av = getAttribute(el, an1);
            if (av == null || !av.equalsIgnoreCase(av1))
                continue;

            vel.add(el);
        }

        return vel;
    }

    public static ArrayList<Node> findElements(ArrayList<Node> pageFlat, String tagname, String an1, String av1, String an2,
            String av2)
            throws Exception
    {
        ArrayList<Node> vel = new ArrayList<Node>();
        Element el;
        String av;

        for (Node n : pageFlat)
        {
            if (!(n instanceof Element))
                continue;
            if (!n.getNodeName().equalsIgnoreCase(tagname))
                continue;

            el = (Element) n;

            av = getAttribute(el, an1);
            if (av == null || !av.equalsIgnoreCase(av1))
                continue;

            av = getAttribute(el, an2);
            if (av == null || !av.equalsIgnoreCase(av2))
                continue;

            vel.add(el);
        }

        return vel;
    }

    public static void removeElements(Node pageRoot, ArrayList<Node> vnodes) throws Exception
    {
        for (Node n : vnodes)
        {
            Node p = n.getParentNode();
            if (p != null && p != n)
                p.removeChild(n);
        }
    }

    public static void enumParents(Set<Node> parents, Node n) throws Exception
    {
        for (;;)
        {
            Node p = n.getParentNode();
            if (p == null || p == n)
                break;
            parents.add(p);
            n = p;
        }
    }

    public static void dumpNode(Node n) throws Exception
    {
        dumpNode(n, "*** ");
    }

    public static void dumpNode(Node n, String prefix) throws Exception
    {
        StringBuilder sb = new StringBuilder(n.getNodeName());
        String av;

        if (n instanceof Element)
        {
            Element el = (Element) n;
            if (null != (av = getAttribute(el, "class")))
                sb.append(" class=[").append(av).append("]");
            if (null != (av = getAttribute(el, "role")))
                sb.append(" role=[").append(av).append("]");
        }

        out(prefix + sb.toString());
    }

    public static void dumpNodeOffset(Node n) throws Exception
    {
        dumpNodeOffset(n, null);
    }

    public static void dumpNodeOffset(Node n, String comment) throws Exception
    {
        int level = 0;
        Node p;

        for (Node nx = n;; nx = p)
        {
            p = nx.getParentNode();
            if (p == null || p == nx)
                break;
            level++;
        }

        StringBuilder sb = new StringBuilder();
        while (level-- > 0)
            sb.append("    ");
        if (comment != null)
            sb.append(comment);
        dumpNode(n, sb.toString());
    }

    public static String getAttribute(Node n, String name) throws Exception
    {
        if (!(n instanceof Element))
            return null;

        Element el = (Element) n;

        String lc = name.toLowerCase();
        String uc = name.toUpperCase();

        if (el.hasAttribute(lc))
            return el.getAttribute(lc);
        else if (el.hasAttribute(uc))
            return el.getAttribute(uc);
        else
            return null;
    }

    public static String emitHtml(Node pageRoot) throws Exception
    {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.setOutputProperty(OutputKeys.METHOD, "html");
        // transformer.setOutputProperty(OutputKeys.MEDIA_TYPE, "text/html")
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        // transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

        StringWriter sw = new StringWriter();
        transformer.transform(new DOMSource(pageRoot), new StreamResult(sw));

        return sw.toString();
    }
}

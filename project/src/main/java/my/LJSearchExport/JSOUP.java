package my.LJSearchExport;

import java.util.*;
import java.io.*;

import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.jsoup.parser.Tag;

public class JSOUP
{
    private static void out(String s) throws Exception
    {
        Main.out(s);
    }

    public static Element makeElement(String tagname, Node nref) throws Exception
    {
        return new Element(Tag.valueOf(tagname), nref.baseUri(), new Attributes());
    }

    public static Element makeElement(String tagname, String baseUri) throws Exception
    {
        return new Element(Tag.valueOf(tagname), baseUri, new Attributes());
    }

    public static Vector<Node> getChildren(Node n) throws Exception
    {
        return new Vector(n.childNodes());
    }

    public static Node getParent(Node n) throws Exception
    {
        Node p = n.parent();
        if (p == n)
            p = null;
        return p;
    }

    public static void addChild(Node parent, Node child) throws Exception
    {
        int nc = parent.childNodeSize();
        if (nc == 0)
            throw new Exception("Cannot add a child to a parent without children");
        parent.childNode(nc - 1).after(child);
    }

    public static String getAttribute(Node n, String name) throws Exception
    {
        if (!(n instanceof Element))
            return null;

        Element el = (Element) n;

        for (Attribute at : el.attributes().asList())
        {
            if (at.getKey().equalsIgnoreCase(name))
                return at.getValue();
        }

        return null;
    }

    public static String getRequiredAttribute(Node n, String name) throws Exception
    {
        String av = getAttribute(n, name);
        if (av == null)
            throw new Exception("Missing required attribute " + name);
        return av;
    }

    public static void updateAttribute(Node n, String attrname, String value) throws Exception
    {
        for (Attribute at : n.attributes().asList())
        {
            if (at.getKey().equalsIgnoreCase(attrname))
                at.setValue(value);
        }
    }

    public static void removeAttribute(Node n, String attrname) throws Exception
    {
        Set<Attribute> xa = new HashSet<Attribute>();

        for (Attribute at : n.attributes().asList())
        {
            if (at.getKey().equalsIgnoreCase(attrname))
                xa.add(at);
        }

        for (Attribute at : xa)
        {
            n.removeAttr(at.getKey());
        }
    }

    public static Vector<Node> flattenChildren(Node el) throws Exception
    {
        Vector<Node> vec = flatten(el);
        if (vec.size() != 0 && vec.get(0) == el)
            vec.remove(0);
        return vec;
    }

    public static Vector<Node> flatten(Node el) throws Exception
    {
        Vector<Node> vec = new Vector<Node>();
        flatten(vec, el, 0);
        return vec;
    }

    private static void flatten(Vector<Node> vec, Node el, int level) throws Exception
    {
        if (el == null)
            return;
        vec.add(el);
        flatten(vec, firstChild(el), level + 1);
        if (level != 0)
            flatten(vec, el.nextSibling(), level + 1);
    }

    public static Node firstChild(Node n) throws Exception
    {
        List<Node> children = n.childNodes();
        if (children.size() == 0)
            return null;
        else
            return children.get(0);
    }

    public static String nodeName(Node n) throws Exception
    {
        return n.nodeName();
    }

    public static Node nextSibling(Node n) throws Exception
    {
        return n.nextSibling();
    }

    public static Vector<Node> findElements(Node root, String tagname) throws Exception
    {
        return findElements(flatten(root), tagname);
    }

    public static Vector<Node> findElements(Vector<Node> pageFlat, String tagname) throws Exception
    {
        Vector<Node> vel = new Vector<Node>();

        for (Node n : pageFlat)
        {
            if (!(n instanceof Element))
                continue;
            if (!n.nodeName().equalsIgnoreCase(tagname))
                continue;

            vel.add(n);
        }

        return vel;
    }

    public static Vector<Node> findElements(Vector<Node> pageFlat, String tagname, String an1, String av1) throws Exception
    {
        Vector<Node> vel = new Vector<Node>();
        Element el;
        String av;

        for (Node n : pageFlat)
        {
            if (!(n instanceof Element))
                continue;
            if (!n.nodeName().equalsIgnoreCase(tagname))
                continue;

            el = (Element) n;

            av = getAttribute(el, an1);
            if (av == null || !av.equalsIgnoreCase(av1))
                continue;

            vel.add(el);
        }

        return vel;
    }

    public static Vector<Node> findElements(Vector<Node> pageFlat, String tagname, String an1, String av1, String an2, String av2)
            throws Exception
    {
        Vector<Node> vel = new Vector<Node>();
        Element el;
        String av;

        for (Node n : pageFlat)
        {
            if (!(n instanceof Element))
                continue;
            if (!n.nodeName().equalsIgnoreCase(tagname))
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

    public static Vector<Node> findElementsWithClass(Node root, String tagname, String cls) throws Exception
    {
        return findElementsWithClass(flatten(root), tagname, cls);
    }

    public static Vector<Node> findElementsWithClass(Vector<Node> pageFlat, String tagname, String cls) throws Exception
    {
        Vector<Node> vel = new Vector<Node>();
        Element el;

        for (Node n : pageFlat)
        {
            if (!(n instanceof Element))
                continue;
            if (tagname != null && !n.nodeName().equalsIgnoreCase(tagname))
                continue;

            el = (Element) n;

            if (classContains(getAttribute(el, "class"), cls))
                vel.add(el);
        }

        return vel;
    }

    public static boolean hasClass(Node n, String cls) throws Exception
    {
        if (n instanceof Element)
        {
            Element el = (Element) n;
            return classContains(getAttribute(el, "class"), cls);
        }
        else
        {
            return false;
        }
    }

    public static Element findSingleElementWithClass(Vector<Node> pageFlat, String tagname, String cls) throws Exception
    {
        Vector<Node> vn = findElementsWithClass(pageFlat, tagname, cls);
        if (vn.size() > 1)
            throw new Exception("Unexpected multiple elements in HTML page");
        if (vn.size() == 0)
            return null;
        else
            return (Element) vn.get(0);
    }

    public static Element findSingleRequiredElementWithClass(Vector<Node> pageFlat, String tagname, String cls) throws Exception
    {
        Element el = findSingleElementWithClass(pageFlat, tagname, cls);
        if (el == null)
            throw new Exception("Missing required element in HTML page");
        return el;
    }

    public static boolean classContains(String clist, String cls) throws Exception
    {
        if (clist == null)
            return false;

        clist = clist.toLowerCase();
        cls = cls.toLowerCase();

        if (clist.indexOf(cls) == -1)
            return false;

        StringTokenizer st = new StringTokenizer(clist, " \t\r\n,");
        while (st.hasMoreTokens())
        {
            if (st.nextToken().equals(cls))
                return true;
        }

        return false;
    }

    public static void removeElementsWithClass(Node pageRoot, String tagname, String cls) throws Exception
    {
        removeElementsWithClass(pageRoot, null, tagname, cls);
    }

    public static void removeElementsWithClass(Node pageRoot, Vector<Node> pageFlat, String tagname, String cls) throws Exception
    {
        if (pageFlat == null)
            pageFlat = flatten(pageRoot);
        Vector<Node> vel = findElementsWithClass(pageFlat, tagname, cls);
        removeElements(pageRoot, vel);
    }

    public static void removeElements(Node pageRoot, String tagname, String an1, String av1) throws Exception
    {
        removeElements(pageRoot, null, tagname, an1, av1);
    }

    public static void removeElements(Node pageRoot, Vector<Node> pageFlat, String tagname, String an1, String av1)
            throws Exception
    {
        if (pageFlat == null)
            pageFlat = flatten(pageRoot);
        Vector<Node> vel = findElements(pageFlat, tagname, an1, av1);
        removeElements(pageRoot, vel);
    }

    public static void removeElements(Node pageRoot, String tagname, String an1, String av1, String an2, String av2)
            throws Exception
    {
        removeElements(pageRoot, null, tagname, an1, av1, an2, av2);
    }

    public static void removeElements(Node pageRoot, Vector<Node> pageFlat, String tagname, String an1, String av1, String an2,
            String av2) throws Exception
    {
        if (pageFlat == null)
            pageFlat = flatten(pageRoot);
        Vector<Node> vel = findElements(pageFlat, tagname, an1, av1, an2, av2);
        removeElements(pageRoot, vel);
    }

    public static void removeElements(Node pageRoot, Vector<Node> vnodes) throws Exception
    {
        for (Node n : vnodes)
        {
            n.remove();
        }
    }

    public static void removeNode(Node n) throws Exception
    {
        n.remove();
    }

    public static void insertAfter(Node nold, Node nnew) throws Exception
    {
        nold.after(nnew);
    }

    public static void enumParents(Set<Node> parents, Node n) throws Exception
    {
        for (;;)
        {
            Node p = n.parent();
            if (p == null || p == n)
                break;
            parents.add(p);
            n = p;
        }
    }

    public static void dumpNodes(Vector<Node> vnodes, String prefix) throws Exception
    {
        for (Node n : vnodes)
            dumpNode(n, prefix);
    }

    public static void dumpNodesOffset(Vector<Node> vnodes) throws Exception
    {
        dumpNodesOffset(vnodes, null);
    }

    public static void dumpNodesOffset(Vector<Node> vnodes, String comment) throws Exception
    {
        for (Node n : vnodes)
            dumpNodeOffset(n, comment);
    }

    public static void dumpNode(Node n) throws Exception
    {
        dumpNode(n, "*** ");
    }

    public static void dumpNode(Node n, String prefix) throws Exception
    {
        StringBuilder sb = new StringBuilder(n.nodeName());
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
            p = nx.parent();
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

    public static Vector<String> extractHrefs(String html) throws Exception
    {
        Node rootNode = parseHtml(html);
        return extractHrefs(rootNode);
    }

    public static Vector<String> extractHrefs(Node root) throws Exception
    {
        Vector<String> vs = new Vector<String>();
        Vector<Node> vnodes = flatten(root);

        for (Node n : vnodes)
        {
            if (!n.nodeName().equalsIgnoreCase("a"))
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

    public static Node parseHtml(String html) throws Exception
    {
        Document doc = Jsoup.parse(html);
        return doc;
    }

    public static String emitHtml(Node pageRoot) throws Exception
    {
        String html = pageRoot.outerHtml();
        return html;
    }
}

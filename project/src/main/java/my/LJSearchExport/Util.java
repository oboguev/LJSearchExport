package my.LJSearchExport;

import java.util.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

public class Util
{
    public static char lastChar(String s) throws Exception
    {
        int len = s.length();
        if (len == 0)
            return '\0';
        else
            return s.charAt(len - 1);
    }

    public static String stripLastChar(String s) throws Exception
    {
        int len = s.length();
        if (len == 0)
            return s;
        else
            return s.substring(0, len - 1);
    }

    public static String stripLastChar(String s, char c) throws Exception
    {
        if (lastChar(s) == c)
            s = stripLastChar(s);
        return s;
    }

    public static List<String> sort(Set<String> set) throws Exception
    {
        List<String> vs = new ArrayList<String>();
        for (String s : set)
            vs.add(s);
        Collections.sort(vs);
        return vs;
    }

    public static boolean is_in_domain(String site, String domain) throws Exception
    {
        site = site.toLowerCase();
        domain = domain.toLowerCase();

        if (site.equals(domain))
            return true;

        int ls = site.length();
        int ld = domain.length();

        if (ld < ls && site.charAt(ls - ld - 1) == '.')
            return true;

        if (site.startsWith("www."))
            return false;

        return is_in_domain("www." + site, domain);
    }

    /*
     * Check if @url begins with @prefix.
     * If yes, strip the prefix, store the result in @sb and return @true.
     * If no, return @false and the value of @sb is unpredictable.
     */
    static boolean beginsWith(String url, String prefix, StringBuilder sb) throws Exception
    {
        if (url.length() >= prefix.length() && url.substring(0, prefix.length()).equalsIgnoreCase(prefix))
        {
            sb.setLength(0);
            sb.append(url.substring(prefix.length()));
            return true;
        }
        else
        {
            return false;
        }
    }

    /*
     * Check if @url begins with prefix = @path + optional "/". 
     * If yes, strip the prefix, store the result in @sb and return @true.
     * If no, return @false and the value of @sb is unpredictable.
     */
    static boolean beginsWithPath(String url, String path, StringBuilder sb) throws Exception
    {
        if (url.equalsIgnoreCase(path))
        {
            sb.setLength(0);
            return true;
        }

        return beginsWith(url, path + "/", sb);
    }

    public static void mkdir(String path) throws Exception
    {
        File file = new File(path);
        if (file.exists() && file.isDirectory())
            return;
        if (file.mkdirs())
            return;
        // re-check again in case directory may have been created concurrently
        if (file.exists() && file.isDirectory())
            return;
        throw new Exception("Unable to create directory " + path);
    }

    public static void writeToFile(String path, String content) throws Exception
    {
        writeToFile(path, content.getBytes(StandardCharsets.UTF_8));
    }

    public static void writeToFile(String path, byte[] bytes) throws Exception
    {
        FileOutputStream fos = new FileOutputStream(path);
        fos.write(bytes);
        fos.flush();
        fos.close();
    }

    public static void writeToFileSafe(String path, String content) throws Exception
    {
        File f = new File(path);
        File ft = new File(path + ".tmp");
        if (f.exists())
            f.delete();
        writeToFile(path + ".tmp", content);
        ft.renameTo(f);
    }

    public static void writeToFileSafe(String path, byte[] content) throws Exception
    {
        File f = new File(path);
        File ft = new File(path + ".tmp");
        if (f.exists())
            f.delete();
        writeToFile(path + ".tmp", content);
        ft.renameTo(f);
    }

    public static String readFileAsString(String path) throws Exception
    {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public static String stripProtocol(String url) throws Exception
    {
        StringBuilder sb = new StringBuilder();

        if (beginsWith(url, "http://", sb))
        {
            // stripped http prefix
            url = sb.toString();
        }
        else if (beginsWith(url, "https://", sb))
        {
            // stripped https prefix
            url = sb.toString();
        }

        return url;
    }

    public static String stripAnchor(String href) throws Exception
    {
        if (href != null)
        {
            int k = href.indexOf('#');
            if (k != -1)
                href = href.substring(0, k);
        }

        return href;
    }

    public static String stripParameters(String href) throws Exception
    {
        if (href != null)
        {
            int k = href.indexOf('?');
            if (k != -1)
                href = href.substring(0, k);
        }

        return href;
    }

    public static String stripParametersAndAnchor(String href) throws Exception
    {
        href = stripParameters(href);
        href = stripAnchor(href);
        return href;
    }

    public static Map<String, String> parseUrlParams(String params) throws Exception
    {
        Map<String, String> res = new HashMap<String, String>();

        StringTokenizer st = new StringTokenizer(params, "&");

        while (st.hasMoreTokens())
        {
            String tok = st.nextToken();
            int k = tok.indexOf('=');
            if (k == -1)
            {
                res.put(tok, null);
            }
            else
            {
                res.put(tok.substring(0, k), tok.substring(k + 1));
            }
        }

        return res;
    }

    final private static Random random_gen = new Random();

    public static int random(int min, int max) throws Exception
    {
        return min + random_gen.nextInt(max - min + 1);
    }

    public static <T> List<T> randomize(List<T> vec) throws Exception
    {
        List<T> res = new ArrayList<T>();

        while (vec.size() > 1)
        {
            int k = random(0, vec.size() - 1);
            res.add(vec.get(k));
            vec.remove(k);
        }

        res.addAll(vec);
        return res;
    }

    public static String despace(String text) throws Exception
    {
        if (text == null)
            return text;
        text = text.replace('\n', ' ').replace('\r', ' ').replace('\t', ' ');
        text = text.replaceAll("\\s+", " ").trim();
        if (text.equals(" "))
            text = "";
        return text;
    }

    /*
     * Read list of strings from file.  
     */
    public static Set<String> read_list(Object obj, String path) throws Exception
    {
        Set<String> ws = new HashSet<String>();
        String line;

        path = relative_path(obj, path);

        Class<?> cls = null;
        if (obj instanceof Class)
            cls = (Class<?>) obj;
        else
            cls = obj.getClass();

        try (InputStream ris = cls.getClassLoader().getResourceAsStream(path);
                InputStreamReader isr = new InputStreamReader(ris, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(isr))
        {
            while (null != (line = bufferedReader.readLine()))
            {
                line = stripComment(line);
                line = despace(line);
                if (line.equals(" ") || line.length() == 0)
                    continue;
                ws.add(line);
            }
        }

        return ws;
    }

    public static String relative_path(Object obj, String path) throws Exception
    {
        Class<?> cls = null;
        if (obj instanceof Class)
            cls = (Class<?>) obj;
        else
            cls = obj.getClass();

        String root = cls.getName().replace('.', '/');
        int k = root.lastIndexOf('/');
        if (k == -1)
            throw new Exception("Unexpected class name: " + cls.getName());
        root = root.substring(0, k);
        return root + "/" + path;
    }

    public static String stripComment(String s) throws Exception
    {
        int k = s.indexOf('#');
        if (k != -1)
            s = s.substring(0, k);
        return s;
    }

    public static List<String> asList(String s)
    {
        return asList(s, ",");
    }

    public static List<String> asList(String s, String sep)
    {
        if (s == null || s.length() == 0)
            return new ArrayList<String>();
        return Arrays.asList(s.split(sep));
    }

    public static void noop()
    {
    }
}
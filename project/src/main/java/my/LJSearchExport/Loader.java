package my.LJSearchExport;

import java.io.File;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static org.apache.commons.lang3.StringEscapeUtils.escapeHtml4;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
// import org.jsoup.nodes.TextNode;

public class Loader
{
    private Node pageRoot;
    private Vector<Node> pageFlat;
    int locatedRecords;
    int seenRecords = 0;

    int yyyy;
    int mm;
    int dd;
    int hh;
    int minmin;
    String poster;
    String journal = null;
    String rid;
    String tid;
    String savedcopy;

    public void load(YYYY_MM_DD cdate, long startTime, long endTime) throws Exception
    {
        StringBuilder sb = new StringBuilder("https://ljsear.ch/search?");
        String pad = "";

        if (Config.User != null)
        {
            sb.append(pad + "author=" + URLEncoder.encode(Config.User, "UTF-8"));
            pad = "&";
        }

        if (Config.InJournal != null)
        {
            sb.append(pad + "community=" + URLEncoder.encode(Config.InJournal, "UTF-8"));
            pad = "&";
        }

        sb.append(pad + "dateFrom=" + startTime);
        pad = "&";

        sb.append(pad + "dateTo=" + endTime);
        pad = "&";

        if (Config.ExportComments)
            sb.append(pad + "in_comments=1");
        else
            sb.append(pad + "in_comments=0");

        String baseurl = sb.toString();

        Thread.sleep(Config.DelayBetweenRequests);
        Web.Response r = Web.get(baseurl);
        processFirstResponse(r);

        while (seenRecords < locatedRecords)
        {
            Thread.sleep(Config.DelayBetweenRequests);
            r = Web.get(baseurl + "&offset=" + seenRecords);
            if (!processNextResponse(r))
                break;
        }

        if (seenRecords < locatedRecords)
        {
            String msg = String.format("For %s: located %d records, but seen only %d",
                                       cdate.toString(), locatedRecords, seenRecords);
            Main.err(msg);
            throw new Exception(msg);
        }
    }

    private void processFirstResponse(Web.Response r) throws Exception
    {
        if (!processResponseFrame(r))
            return;

        Element el = JSOUP.findSingleRequiredElementWithClass(pageFlat, "div", "serp-params");
        el = JSOUP.findSingleRequiredElementWithClass(JSOUP.flatten(el), "div", "serp-params__find-timer");
        String text = Util.despace(el.text()).trim();
        if (text.startsWith("Найдена 1 запись"))
        {
            locatedRecords = 1;
        }
        else
        {
            Matcher m = Pattern.compile("Найдено (\\d+) записей.*").matcher(text);
            if (!m.matches())
            {
                m = Pattern.compile("Найдено (\\d+) записи.*").matcher(text);
                if (!m.matches())
                    m = Pattern.compile("Найдена (\\d+) запись.*").matcher(text);
                if (!m.matches())
                    throw new Exception("Unexpected response page structure: missing the number of retrieved records: [" +
                                        text + "]");
            }
            locatedRecords = Integer.parseInt(m.group(1));
        }

        processSearchEntries();
    }

    private boolean processNextResponse(Web.Response r) throws Exception
    {
        if (!processResponseFrame(r))
            return false;
        processSearchEntries();
        return true;
    }

    private boolean processResponseFrame(Web.Response r) throws Exception
    {
        if (!r.isOK())
            throw new Exception("Unable to read HTTP " + Web.getLastURL() + ", response code: " + r.code + ", reason: " + r.reason);

        pageRoot = JSOUP.parseHtml(r.body);
        pageFlat = JSOUP.flatten(pageRoot);

        boolean inerror = false;
        boolean empty = false;
        for (Node n : JSOUP.findElementsWithClass(pageFlat, "div", "p-error__text"))
        {
            String text = ((Element) n).text();
            text = Util.despace(text).trim();
            if (text.startsWith("А ничего нету тут такого"))
            {
                empty = true;
            }
            else
            {
                Main.err("Server responded with error: " + text);
                inerror = true;
            }
        }

        if (inerror)
            throw new Exception("Server responded with error");

        return !empty;
    }

    private void processSearchEntries() throws Exception
    {
        Element el = JSOUP.findSingleRequiredElementWithClass(pageFlat, "div", "search-results");
        Vector<Node> vn = JSOUP.findElementsWithClass(JSOUP.flatten(el), "div", "search-results__item");
        if (vn.size() == 0)
            throw new Exception("Unexpected page structure: missing list of search results");
        for (Node n : vn)
            processSearchEntry(n);
    }

    private void processSearchEntry(Node n) throws Exception
    {
        seenRecords++;

        Vector<Node> flat = JSOUP.flatten(n);

        /*
         * Location URI, such as "1724043.html" or "1724043.html?thread=11408267"
         * rid => "1724043"
         * tid => "11408267" or blank
         */
        Element el = JSOUP.findSingleRequiredElementWithClass(flat, "a", "search-results__title");
        String href = JSOUP.getRequiredAttribute(el, "href");
        if (Avoid.isAvoid(href))
            return;
        URL url = new URL(href);
        rid = url.getPath();
        if (!rid.startsWith("/") || !rid.endsWith(".html"))
            throw new Exception("Unexpected LJ record URL format");
        rid = rid.substring(0, rid.length() - ".html".length());
        rid = rid.substring(1);
        tid = url.getQuery();
        if (tid != null && tid.length() != 0)
        {
            Matcher m = Pattern.compile("thread=(\\d+)").matcher(tid);
            if (!m.matches())
                throw new Exception("Unexpected LJ record URL format");
            tid = m.group(1);
        }

        journal = null;

        /*
         * savedcopy => "/savedcopy?comm=994545601"
         */
        el = JSOUP.findSingleRequiredElementWithClass(flat, "a", "search-results__cached");
        savedcopy = JSOUP.getRequiredAttribute(el, "href");

        el = JSOUP.findSingleRequiredElementWithClass(flat, "div", "search-results__info");

        boolean comment;

        if (JSOUP.hasClass(el, "search-results__info_type_comment"))
        {
            /*
             * Comment
             */
            String text = Util.despace(el.ownText()).trim();
            Matcher m = Pattern.compile("— комментарий от (\\d+)[.](\\d+)[.](\\d+) в (\\d+):(\\d+) .*").matcher(text);
            if (!m.matches())
                throw new Exception("Unexpected LJ date format");
            dd = Integer.parseInt(m.group(1));
            mm = Integer.parseInt(m.group(2));
            yyyy = 2000 + Integer.parseInt(m.group(3));

            Vector<Node> vn = JSOUP.findElementsWithClass(JSOUP.flatten(el), "a", "search-results__person");
            if (vn.size() != 2)
                throw new Exception("Unexpected poster/jorunal information");
            Element el1 = (Element) vn.get(0);
            Element el2 = (Element) vn.get(1);
            if (JSOUP.hasClass(el1, "search-results__person_post-author") ||
                !JSOUP.hasClass(el2, "search-results__person_post-author"))
                throw new Exception("Unexpected poster/jorunal information");
            poster = Util.despace(el1.ownText()).trim();
            journal = Util.despace(el2.ownText()).trim();
            comment = true;
            if (!Config.ExportComments)
                return;
            if (!Config.ExportCommentsInOwnJournal && poster.equals(journal))
                return;
        }
        else
        {
            /*
             * Root record
             */
            String text = Util.despace(el.ownText()).trim();
            Matcher m = Pattern.compile("— запись от (\\d+)[.](\\d+)[.](\\d+) в (\\d+):(\\d+) .*").matcher(text);
            if (!m.matches())
                m = Pattern.compile("— запись от (\\d+)[.](\\d+)[.](\\d+) в (\\d+):(\\d+)").matcher(text);
            if (!m.matches())
                throw new Exception("Unexpected LJ date header format: [" + text + "]");
            dd = Integer.parseInt(m.group(1));
            mm = Integer.parseInt(m.group(2));
            yyyy = 2000 + Integer.parseInt(m.group(3));
            hh = Integer.parseInt(m.group(4));
            minmin = Integer.parseInt(m.group(5));

            Element el2 = JSOUP.findSingleRequiredElementWithClass(JSOUP.flatten(el), "a", "search-results__person");
            journal = poster = Util.despace(el2.ownText()).trim();
            comment = false;
            if (!Config.ExportEntries)
                return;
        }

        String filepath = makeFilePath(comment);
        File file = new File(filepath);

        if (!Config.ReloadExisting && file.exists())
            return;

        Util.mkdir(file.getParent());

        downloadEntry(filepath);
    }

    private void downloadEntry(String filepath) throws Exception
    {
        try
        {
            Thread.sleep(Config.DelayBetweenRequests);
            Web.Response r = Web.get("https://ljsear.ch" + savedcopy);
            if (!processResponseFrame(r))
                return;

            Element el = JSOUP.findSingleRequiredElementWithClass(pageFlat, "a", "saved-copy-header__original");
            String href = JSOUP.getRequiredAttribute(el, "href");

            String title = null;
            el = JSOUP.findSingleElementWithClass(pageFlat, "div", "article__title");
            if (el != null)
                title = el.text();

            el = JSOUP.findSingleRequiredElementWithClass(pageFlat, "div", "article__main-text");

            StringBuilder sb = new StringBuilder();
            sb.append("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">");
            sb.append("<html>");
            sb.append("<head>");
            sb.append("<meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">");
            sb.append("<title></title>");
            sb.append("</head>");
            sb.append("<body>");
            sb.append("<table width=\"100%\" cellspacing=\"2\" cellpadding=\"2\" border=\"0\">");
            sb.append("<tbody>");
            sb.append("<tr>");
            sb.append("<td valign=\"top\" bgcolor=\"#f9f6e6\">Автор: <b>{author}</b> ");
            sb.append("{time} Оригинал: ");
            sb.append("<b><a href=\"{href-1}\">{href-2}</a></b>");
            sb.append("</td></tr></tbody></table>");
            sb.append("<h3>{article-header}</h3>");
            sb.append("{article-body}");
            sb.append("</body>");
            sb.append("</html>");

            String html = sb.toString();
            html = html.replace("{author}", escapeHtml4(poster));
            html = html.replace("{time}", escapeHtml4(String.format("%04d-%02d-%02d %02d:%02d", yyyy, mm, dd, hh, minmin)));
            html = html.replace("{href-1}", href);
            html = html.replace("{href-2}", escapeHtml4(href));
            html = html.replace("{article-header}", (title != null) ? title : "");
            JSOUP.removeAttribute(el, "class");
            JSOUP.removeAttribute(el, "style");
            html = html.replace("{article-body}", el.outerHtml());

            Util.writeToFileSafe(filepath, html);
        }
        catch (Exception ex)
        {
            throw new Exception("Error while loading https://ljsear.ch" + savedcopy, ex);
        }
    }

    private String makeFilePath(boolean comment) throws Exception
    {
        StringBuilder sb = new StringBuilder(Config.DownloadRoot);

        if (!Config.DownloadRoot.endsWith(File.separator))
            sb.append(File.separator);

        String xposter = poster;
        if (Config.SeparateEntriesComments)
        {
            if (comment)
                xposter += ".comments";
            else
                xposter += ".journal";
        }

        sb.append(String.format("%s%s%s%s%04d%s%02d%s%s",
                                xposter, File.separator,
                                journal, File.separator,
                                yyyy, File.separator,
                                mm, File.separator,
                                rid));
        if (tid != null && tid.length() != 0)
            sb.append("_" + tid);
        sb.append(".html");
        return sb.toString();
    }
}

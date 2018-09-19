package my.LJSearchExport;

import java.util.HashSet;
import java.util.Set;

public class Avoid
{
    private static Set<String> AvoidList;

    public static void init() throws Exception
    {
        /*
         * Known missing or bad records 
         */
        AvoidList = new HashSet<String>();
        // missing
        AvoidList.add("http://rromanov.livejournal.com/262217.html?thread=663369");
        // title-only
        AvoidList.add("http://udod99.livejournal.com/577888.html");
        AvoidList.add("http://udod99.livejournal.com/1155357.html");
    }

    public static boolean isAvoid(String href) throws Exception
    {
        return AvoidList.contains(href);
    }
}

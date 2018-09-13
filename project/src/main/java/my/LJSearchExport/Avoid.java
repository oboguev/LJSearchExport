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
        AvoidList.add("http://rromanov.livejournal.com/262217.html?thread=663369");
    }

    public static boolean isAvoid(String href) throws Exception
    {
        return AvoidList.contains(href);
    }
}

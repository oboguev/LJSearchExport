package my.LJSearchExport;

public class Main
{
    public static void main(String[] args)
    {
        try
        {
            Config.init();
            Web.init();
            new Main().do_main();
            out("** Done.");
        }
        catch (Exception ex)
        {
            err("Exception: ");
            ex.printStackTrace();
        }
        finally
        {
            try
            {
                Web.shutdown();
            }
            catch (Exception ex)
            {

            }
        }
    }

    public static void err(String msg)
    {
        System.err.println(msg);
        System.err.flush();
    }

    public static void out(String msg)
    {
        System.out.println(msg);
        System.out.flush();
    }

    private void do_main() throws Exception
    {
        long unixStart = Config.StartTime.unixTime();
        long unixEnd = Config.EndTime.unixTime() + 24 * 3600 - 1;

        int days = 0;
        for (long unix = unixStart; unix <= unixEnd; unix += 24 * 3600)
        {
            days++;
        }
        Main.out("Will scan " + days + " days.");

        YYYY_MM_DD cdate = Config.StartTime;
        Loader loader = new Loader();

        for (long unix = unixStart; unix <= unixEnd; unix += 24 * 3600, cdate = cdate.next())
        {
            out("Fetching data for " + cdate.toString() + " ...");
            loader.load(cdate, unix, unix + 24 * 3600 - 1);
        }
    }
}

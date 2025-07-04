package my.LJSearchExport;

public class Test
{
    public static void main(String[] args)
    {
        try
        {
            Config.init();
            new Test().do_main();
        }
        catch (Exception ex)
        {
            err("Exception: ");
            ex.printStackTrace();
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
        YYYY_MM_DD x = new YYYY_MM_DD(2017, 8, 3);
        out("" + x.unixTime());
    }
}

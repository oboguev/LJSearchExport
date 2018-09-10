package my.LJSearchExport;

import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class Config
{
    /******************************/
    /** USER-SETTABLE PARAMETERS **/
    /******************************/

    /*
     * Name of the user whose records or comments are to be loaded.
     * Can be null if loading all users. 
     */
    public static String User = "oboguev";

    /*
     * Name of the journal which records or comments are to be loaded.
     * Can be null if loading records in all journals. 
     */
    public static String InJournal = null;
    // public static String InJournal = "udod99";

    /* Export comments? */
    public static boolean ExportComments = true;

    /* Export comments in poster's own journal? */
    public static boolean ExportCommentsInOwnJournal = false;

    /* Export main records */
    public static boolean ExportEntries = false;

    /* Time range (can be null for open range) */
    public static YYYY_MM_DD StartTime = new YYYY_MM_DD(2002, 9, 17);
    // public static YYYY_MM_DD StartTime = new YYYY_MM_DD(2001, 6, 20);
    // public static YYYY_MM_DD StartTime = new YYYY_MM_DD(2000, 1, 1);
    // public static YYYY_MM_DD StartTime = null;
    public static YYYY_MM_DD EndTime = new YYYY_MM_DD(2017, 8, 1);
    // public static YYYY_MM_DD EndTime = new YYYY_MM_DD(2017, 12, 31);
    // public static YYYY_MM_DD EndTime = null;

    /*
     * Location to save download files.
     * Files are saved as DownloadRoot/poster/journal/yyyy/mm/rid.html 
     *                 or DownloadRoot/poster/journal/yyyy/mm/rid_tid.html 
     */
    public static String DownloadRoot = "F:\\WINAPPS\\LJSearchExport\\exported-journals";
    // public static String DownloadRoot = "R:";
    // public static String DownloadRoot = "F:\\@";
    // public static String DownloadRoot = "/home/sergey/LJSearchExport/exported-journals";

    /* Proxy server */
    public static String Proxy = null;
    // public static String Proxy = "http://proxy.xxx.com:8080";

    /**************************/
    /** TECHNICAL PARAMETERS **/
    /**************************/

    public static String UserAgent = "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:40.0) Gecko/20100101 Firefox/40.1";
    public static String UserAgentAccept = "text/html, application/xhtml+xml, */*";
    public static String UserAgentAcceptEncoding = "gzip, deflate";

    public static String TrustStore = null;
    public static String TrustStorePassword = null;

    public static boolean UseFiddler = false;
    public static String FiddlerTrustStore = null;
    public static String FidlerTrustStorePassword = null;

    public static boolean TrustAnySSL = true;

    public static boolean ReloadExisting = false;

    /* ms to sleep between the request to avoid anti-robot */
    public static int DelayBetweenRequests = 5 * 1000;

    /*
     * Adding Fiddler certificate:
     * 
     * set JAVA_HOME=C:\jre1.8.0_121
     * set PATH=%PATH%;C:\jre1.8.0_121\bin
     * keytool -import -alias fidder -file C:\Users\sergey\Desktop\FiddlerRoot.cer -keystore C:\jre1.8.0_121\lib\security\cacerts -storepass changeit
     */

    public static void init() throws Exception
    {
        if (StartTime == null)
            StartTime = new YYYY_MM_DD(2000, 1, 1);

        if (EndTime == null)
            EndTime = new YYYY_MM_DD(2017, 12, 31);

        StartTime.clamp();
        EndTime.clamp();

        if (InJournal == null && User == null)
            throw new Exception("Must specify either User or InJournal or both");

        if (UseFiddler)
        {
            Proxy = "http://localhost:8888";

            if (TrustStore == null && FiddlerTrustStore != null)
            {
                if (FidlerTrustStorePassword == null)
                    throw new Exception("Set password for Fiddler trust store");

                TrustStore = FiddlerTrustStore;
                TrustStorePassword = FidlerTrustStorePassword;
            }
        }

        if (TrustAnySSL)
        {
            SSLContext sslContext = SSLContext.getInstance("SSL");

            // set up a TrustManager that trusts everything
            TrustManager[] trustManagers = new TrustManager[1];
            trustManagers[0] = new LooseTrustManager();
            sslContext.init(null, trustManagers, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
        }
    }

    /* development aids */
    public static boolean True = true;
    public static boolean False = false;

    public static void noop()
    {
    }

    public static class LooseTrustManager implements X509TrustManager
    {
        public X509Certificate[] getAcceptedIssuers()
        {
            // System.err.println("getAcceptedIssuers =============");
            return new X509Certificate[0];
        }

        public void checkClientTrusted(X509Certificate[] certs, String authType)
        {
            // System.err.println("checkClientTrusted =============");
        }

        public void checkServerTrusted(X509Certificate[] certs, String authType)
        {
            // System.err.println("checkServerTrusted =============");
        }
    }
}

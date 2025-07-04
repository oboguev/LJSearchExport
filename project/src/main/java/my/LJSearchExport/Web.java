package my.LJSearchExport;

import java.io.*;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.http.*;
import org.apache.http.client.*;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.*;
import org.apache.http.impl.conn.*;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.config.CookieSpecs;

import java.net.URL;
import java.nio.charset.StandardCharsets;

// GZIP: http://stackoverflow.com/questions/1063004/how-to-decompress-http-response

public class Web
{
    public static CloseableHttpClient httpClient;
    public static CookieStore cookieStore;
    private static ThreadLocal<String> lastURL;

    public static class Response
    {
        public int code;
        public String reason;
        public String body = new String("");
        public byte[] binaryBody;

        public boolean isOK()
        {
            return code >= 200 && code <= 299;
        }
    }

    public static void init() throws Exception
    {
        DefaultProxyRoutePlanner routePlanner = null;

        cookieStore = new BasicCookieStore();
        lastURL = new ThreadLocal<String>();

        if (Config.TrustStore != null)
        {
            System.setProperty("javax.net.ssl.trustStore", Config.TrustStore);
            if (Config.TrustStorePassword != null)
                System.setProperty("javax.net.ssl.trustStorePassword", Config.TrustStorePassword);
        }

        if (Config.Proxy != null)
        {
            URL url = new URL(Config.Proxy);
            String host = url.getHost();
            int port = url.getPort();

            // System.setProperty("proxyHost", host);
            // System.setProperty("proxyPort", "" + port);
            // System.setProperty("proxySet", "true");

            // System.setProperty("http.proxyHost", host);
            // System.setProperty("http.proxyPort", "" + port);
            // System.setProperty("http.proxySet", "true");

            // System.setProperty("https.proxyHost", host);
            // System.setProperty("https.proxyPort", "" + port);
            // System.setProperty("https.proxySet", "true");

            HttpHost proxy = new HttpHost(host, port);
            routePlanner = new DefaultProxyRoutePlanner(proxy);
        }

        // RequestConfig globalConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.NETSCAPE).build();
        RequestConfig globalConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build();

        HttpClientBuilder hcb = HttpClients.custom().setDefaultRequestConfig(globalConfig).setDefaultCookieStore(cookieStore);

        if (routePlanner != null)
            hcb = hcb.setRoutePlanner(routePlanner);

        httpClient = hcb.build();
    }

    public static void shutdown() throws Exception
    {
        cookieStore = null;
        lastURL = null;

        if (httpClient != null)
        {
            httpClient.close();
            httpClient = null;
            // let Apache HttpClient to settle
            Thread.sleep(1500);
        }
    }

    public static CookieStore getCookieStore() throws Exception
    {
        return cookieStore;
    }

    public static Response get(String url) throws Exception
    {
        return get(url, false);
    }

    public static Response get(String url, boolean binary) throws Exception
    {
        lastURL.set(url);
        Response r = new Response();

        HttpGet request = new HttpGet(url);
        setCommon(request);
        CloseableHttpResponse response = null;

        try
        {
            response = httpClient.execute(request);
            r.code = response.getStatusLine().getStatusCode();
            r.reason = response.getStatusLine().getReasonPhrase();
            HttpEntity entity = response.getEntity();

            if (entity != null)
            {
                InputStream entityStream = null;
                BufferedReader brd = null;
                StringBuilder sb = new StringBuilder();
                String line;

                try
                {
                    entityStream = entity.getContent();

                    if (binary)
                    {
                        r.binaryBody = IOUtils.toByteArray(entityStream);
                    }
                    else
                    {
                        brd = new BufferedReader(new InputStreamReader(entityStream, StandardCharsets.UTF_8));
                        while ((line = brd.readLine()) != null)
                            sb.append(line + "\r\n");
                        r.body = sb.toString();
                    }
                }
                finally
                {
                    if (brd != null)
                        brd.close();
                    if (entityStream != null)
                        entityStream.close();
                }
            }
        }
        catch (Exception ex)
        {
            Util.noop();
            throw ex;
        }
        finally
        {
            if (null != null)
                response.close();
        }

        return r;
    }

    public static Response post(String url, String body) throws Exception
    {
        lastURL.set(url);
        Response r = new Response();

        HttpPost request = new HttpPost(url);
        setCommon(request);
        request.setHeader("Content-Type", "application/x-www-form-urlencoded");
        request.setEntity(new StringEntity(body, StandardCharsets.UTF_8));
        CloseableHttpResponse response = httpClient.execute(request);

        try
        {
            r.code = response.getStatusLine().getStatusCode();
            r.reason = response.getStatusLine().getReasonPhrase();
            HttpEntity entity = response.getEntity();

            if (entity != null)
            {
                InputStream entityStream = null;
                BufferedReader brd = null;
                StringBuilder sb = new StringBuilder();
                String line;

                try
                {
                    entityStream = entity.getContent();
                    brd = new BufferedReader(new InputStreamReader(entityStream, StandardCharsets.UTF_8));
                    while ((line = brd.readLine()) != null)
                        sb.append(line + "\r\n");
                    r.body = sb.toString();
                }
                finally
                {
                    if (brd != null)
                        brd.close();
                    if (entityStream != null)
                        entityStream.close();
                }
            }
        }
        finally
        {
            response.close();
        }

        return r;
    }

    private static void setCommon(HttpRequestBase request) throws Exception
    {
        request.setHeader("User-Agent", Config.UserAgent);
        request.setHeader("Accept", Config.UserAgentAccept);
        request.setHeader("Accept-Encoding", Config.UserAgentAcceptEncoding);
        request.setHeader("Cache-Control", "no-cache");
        request.setHeader("Pragma", "no-cache");
    }

    public static String describe(int sc) throws Exception
    {
        return "HTTP status code " + sc;
    }

    public static String escape(String s) throws Exception
    {
        return StringEscapeUtils.escapeHtml4(s);
    }

    public static String unescape(String s) throws Exception
    {
        return StringEscapeUtils.unescapeHtml4(s);
    }

    public static String getLastURL() throws Exception
    {
        return lastURL.get();
    }
}
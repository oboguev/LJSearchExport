package my.LJSearchExport;

import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class YYYY_MM_DD
{
    public int yyyy;
    public int mm;
    public int dd;

    public YYYY_MM_DD(int yyyy, int mm, int dd)
    {
        this.yyyy = yyyy;
        this.mm = mm;
        this.dd = dd;
    }

    public YYYY_MM_DD(YYYY_MM_DD src)
    {
        this.yyyy = src.yyyy;
        this.mm = src.mm;
        this.dd = src.dd;
    }

    public YYYY_MM_DD clone()
    {
        return new YYYY_MM_DD(this);
    }

    @Override
    public String toString()
    {
        if (yyyy >= 0 && yyyy <= 9999 && mm >= 00 && mm <= 99 && dd >= 0 && dd <= 99)
            return String.format("%04d.%02d.%02d", yyyy, mm, dd);
        else
            return "" + yyyy + "." + mm + "." + dd;
    }

    public void clamp() throws Exception
    {
        if (yyyy < 1900 || yyyy > 3000 || mm < 1 || mm > 12 || dd < 1 || dd > 31)
            throw new Exception("Invalid date: " + toString());

        if (dd > YearMonth.of(yyyy, mm).lengthOfMonth())
            dd = YearMonth.of(yyyy, mm).lengthOfMonth();
    }

    public static int compare(YYYY_MM_DD d1, YYYY_MM_DD d2) throws Exception
    {
        if (d1.yyyy < d2.yyyy)
            return -1;
        else if (d1.yyyy > d2.yyyy)
            return 1;
        else if (d1.mm < d2.mm)
            return -1;
        else if (d1.mm > d2.mm)
            return 1;
        else if (d1.dd < d2.dd)
            return -1;
        else if (d1.dd > d2.dd)
            return 1;
        else
            return 0;
    }

    public YYYY_MM_DD next() throws Exception
    {
        YYYY_MM_DD x = new YYYY_MM_DD(yyyy, mm, dd);

        x.dd++;

        if (x.dd > YearMonth.of(yyyy, mm).lengthOfMonth())
        {
            x.dd = 1;
            x.mm++;
            if (x.mm > 12)
            {
                x.mm = 1;
                x.yyyy++;
            }
        }

        return x;
    }

    public long unixTime() throws Exception
    {
        // LJSearch timestamps are for SF (former server location) 
        ZonedDateTime z = ZonedDateTime.of(yyyy, mm, dd, 0, 0, 0, 0, ZoneId.of("America/Los_Angeles"));
        long tz = z.getOffset().getTotalSeconds();
        if (tz > 0)
            throw new Exception("Unexpected time zone");
        tz = -tz;
        if ((tz % 3600) != 0)
            throw new Exception("Unexpected time zone");
        tz = tz / 3600;
        if (tz > 23)
            throw new Exception("Unexpected time zone");

        // "2010-01-02T08:00:00.00Z"
        Instant i = Instant.parse(String.format("%04d-%02d-%02dT%02d:00:00.00Z", yyyy, mm, dd, tz));
        return i.getEpochSecond();
    }
}
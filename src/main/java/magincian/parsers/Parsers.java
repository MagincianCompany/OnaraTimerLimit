package magincian.parsers;

import java.time.LocalDateTime;

public abstract class Parsers {
    public static String LocalDateTimeToString(java.time.LocalDateTime data)
    {
        return data.getYear() + "-" + data.getMonth() + "-" + data.getDayOfMonth() + " " +
                data.getHour() + ":" + data.getMinute() +":"+data.getSecond();
    }
}

package com.sfauto.device.history;

/**
 * <p>Title: CSC2000Java报表模块</p>
 *
 * <p>Description: CSC2000Java报表模块</p>
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: sf</p>
 *
 * @author 徐延明
 * @version 1.0
 */
import java.util.Calendar;
import java.sql.Timestamp;

public class TimeUtils {
    public static int TIME_FORMAT_INT = 0; //用于快照类型表
    public static int TIME_FORMAT_DATE = 1; //用于其他表
    public static int TIME_FORMAT_STRING = 2; //扩展用

    public static Object parseTime(int type, long start) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(start);
        if (type == TIME_FORMAT_INT) {
            int rtn = cal.get(Calendar.HOUR_OF_DAY) * 60 +
                      cal.get(Calendar.MINUTE);
            return new Integer(rtn);
        } else if (type == TIME_FORMAT_DATE) {
            Timestamp stamp = new Timestamp(start);
            return stamp;
        } else if (type == TIME_FORMAT_STRING) {
            return null;
        } else {
            return null;
        }
    }

    public static boolean isLeapYear(int year) {
        if (year % 100 == 0) {
            if (year % 400 == 0) {
                if (year % 4000 == 0) {
                    return false;
                } else {
                    return true;
                }
            } else {
                return false;
            }
        } else if (year % 4 == 0) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isValidDay(int year, int month, int day) {
        if (day <= 0 || day > dayNum(year, month)) {
            return false;
        } else {
            return true;
        }
    }

    public static long prevYear(long now) {
        Calendar rtn = Calendar.getInstance();
        rtn.setTimeInMillis(now);
        int year = rtn.get(Calendar.YEAR);
        rtn.set(Calendar.YEAR, --year);
        return rtn.getTimeInMillis();
    }

    public static long nextYear(long now) {
        Calendar rtn = Calendar.getInstance();
        rtn.setTimeInMillis(now);
        int year = rtn.get(Calendar.YEAR);
        rtn.set(Calendar.YEAR, ++year);
        return rtn.getTimeInMillis();
    }

    public static long prevMonth(long now) {
        Calendar rtn = Calendar.getInstance();
        rtn.setTimeInMillis(now);
        int year = rtn.get(Calendar.YEAR);
        int month = rtn.get(Calendar.MONTH) + 1;

        if (month > 1) {
            month--;
        } else {
            year--;
            month = 12;
        }

        rtn.set(Calendar.YEAR, year);
        rtn.set(Calendar.MONTH, --month);

        return rtn.getTimeInMillis();
    }

    public static long nextMonth(long now) {
        Calendar rtn = Calendar.getInstance();
        rtn.setTimeInMillis(now);
        int year = rtn.get(Calendar.YEAR);
        int month = rtn.get(Calendar.MONTH) + 1;

        if (month == 12) {
            year++;
            month = 1;
        } else {
            month++;
        }

        rtn.set(Calendar.YEAR, year);
        rtn.set(Calendar.MONTH, --month);

        return rtn.getTimeInMillis();
    }

    public static int dayNum(int year, int month) {
        int monthday[] = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
        if (isLeapYear(year)) {
            monthday[1] = 29;
        }
        return monthday[month - 1];
    }

    public static long prevDay(long now) {
        Calendar rtn = Calendar.getInstance();
        rtn.setTimeInMillis(now);
        int year = rtn.get(Calendar.YEAR);
        int month = rtn.get(Calendar.MONTH) + 1;
        int day = rtn.get(Calendar.DAY_OF_MONTH);

        int monthday[] = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
        if (isLeapYear(year)) {
            monthday[1] = 29;
        }

        if (day == 1) {
            if (month > 1) {
                month--;
                day = monthday[month - 1];
            } else {
                year--;
                month = 12;
                day = 31;
            }
        } else {
            day--;
        }

        rtn.set(Calendar.YEAR, year);
        rtn.set(Calendar.MONTH, month - 1);
        rtn.set(Calendar.DAY_OF_MONTH, day);

        return rtn.getTimeInMillis();
    }

    public static long nextDay(long now) {
        Calendar rtn = Calendar.getInstance();
        rtn.setTimeInMillis(now);
        int year = rtn.get(Calendar.YEAR);
        int month = rtn.get(Calendar.MONTH) + 1;
        int day = rtn.get(Calendar.DAY_OF_MONTH);

        int monthday[] = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
        if (isLeapYear(year)) {
            monthday[1] = 29;
        }

        if (day == dayNum(year, month)) {
            if (month == 12) {
                year++;
                month = 1;
                day = 1;
            } else {
                month++;
                day = 1;
            }
        } else {
            day++;
        }

        rtn.set(Calendar.YEAR, year);
        rtn.set(Calendar.MONTH, month - 1);
        rtn.set(Calendar.DAY_OF_MONTH, day);

        return rtn.getTimeInMillis();
    }

    public static long prevHour(long now) {
        Calendar rtn = Calendar.getInstance();
        rtn.setTimeInMillis(now);
        int hour = rtn.get(Calendar.HOUR_OF_DAY);

        if (hour > 1) {
            hour--;
            rtn.set(Calendar.HOUR_OF_DAY, hour);
        } else {
            hour = 23;
            rtn.set(Calendar.HOUR_OF_DAY, hour);
            rtn.setTimeInMillis(prevDay(rtn.getTimeInMillis()));
        }
        return rtn.getTimeInMillis();
    }

    public static long endOfDay(long now) {
        Calendar rtn = Calendar.getInstance();
        rtn.setTimeInMillis(now);
        rtn.set(Calendar.HOUR_OF_DAY, 23);
        rtn.set(Calendar.MINUTE, 59);
        rtn.set(Calendar.SECOND, 59);
        rtn.set(Calendar.MILLISECOND, 0);
        return rtn.getTimeInMillis();
    }

    public static long endOfMonth(long now) {
        Calendar rtn = Calendar.getInstance();
        rtn.setTimeInMillis(now);
        int year, month;
        year = rtn.get(Calendar.YEAR);
        month = rtn.get(Calendar.MONTH) + 1;
        rtn.set(Calendar.DAY_OF_MONTH, dayNum(year, month));
        rtn.set(Calendar.HOUR_OF_DAY, 23);
        rtn.set(Calendar.MINUTE, 59);
        rtn.set(Calendar.SECOND, 59);
        rtn.set(Calendar.MILLISECOND, 0);
        return rtn.getTimeInMillis();
    }

    public static long endOfYear(long now) {
        Calendar rtn = Calendar.getInstance();
        rtn.setTimeInMillis(now);
        rtn.set(Calendar.MONTH, 11);
        rtn.set(Calendar.DAY_OF_MONTH, 31);
        rtn.set(Calendar.HOUR_OF_DAY, 23);
        rtn.set(Calendar.MINUTE, 59);
        rtn.set(Calendar.SECOND, 59);
        rtn.set(Calendar.MILLISECOND, 0);
        return rtn.getTimeInMillis();
    }

    public static long startOfDay(long now) {
        Calendar rtn = Calendar.getInstance();
        rtn.setTimeInMillis(now);
        rtn.set(Calendar.HOUR_OF_DAY, 0);
        rtn.set(Calendar.MINUTE, 0);
        rtn.set(Calendar.SECOND, 0);
        rtn.set(Calendar.MILLISECOND, 0);
        return rtn.getTimeInMillis();
    }

    public static long startOfMonth(long now) {
        Calendar rtn = Calendar.getInstance();
        rtn.setTimeInMillis(now);
        rtn.set(Calendar.DAY_OF_MONTH, 1);
        rtn.set(Calendar.HOUR_OF_DAY, 0);
        rtn.set(Calendar.MINUTE, 0);
        rtn.set(Calendar.SECOND, 0);
        rtn.set(Calendar.MILLISECOND, 0);
        return rtn.getTimeInMillis();
    }

    public static long startOfNextMonth(long now) {
        Calendar rtn = Calendar.getInstance();
        rtn.setTimeInMillis(now);
        int year = rtn.get(Calendar.YEAR);
        int month = rtn.get(Calendar.MONTH) + 1;
        if (month == 12) {
            year++;
            month = 1;
        } else {
            month++;
        }
        rtn.set(Calendar.YEAR, year);
        rtn.set(Calendar.MONTH, month - 1);
        rtn.set(Calendar.DAY_OF_MONTH, 1);
        rtn.set(Calendar.HOUR_OF_DAY, 0);
        rtn.set(Calendar.MINUTE, 0);
        rtn.set(Calendar.SECOND, 0);
        rtn.set(Calendar.MILLISECOND, 0);
        return rtn.getTimeInMillis();
    }

    public static long endOfNextMonth(long now) {
        Calendar rtn = Calendar.getInstance();
        rtn.setTimeInMillis(now);
        int year = rtn.get(Calendar.YEAR);
        int month = rtn.get(Calendar.MONTH) + 1;
        if (month == 12) {
            year++;
            month = 1;
        } else {
            month++;
        }
        rtn.set(Calendar.YEAR, year);
        rtn.set(Calendar.MONTH, month - 1);
        rtn.set(Calendar.DAY_OF_MONTH, dayNum(year, month));
        rtn.set(Calendar.HOUR_OF_DAY, 23);
        rtn.set(Calendar.MINUTE, 59);
        rtn.set(Calendar.SECOND, 59);
        rtn.set(Calendar.MILLISECOND, 0);
        return rtn.getTimeInMillis();
    }

    public static long startOfNextYear(long now) {
        Calendar rtn = Calendar.getInstance();
        rtn.setTimeInMillis(now);
        int year = rtn.get(Calendar.YEAR) + 1;
        rtn.set(Calendar.YEAR, year);
        rtn.set(Calendar.MONTH, 0);
        rtn.set(Calendar.DAY_OF_MONTH, 1);
        rtn.set(Calendar.HOUR_OF_DAY, 0);
        rtn.set(Calendar.MINUTE, 0);
        rtn.set(Calendar.SECOND, 0);
        rtn.set(Calendar.MILLISECOND, 0);
        return rtn.getTimeInMillis();
    }

    public static long startOfYear(long now) {
        Calendar rtn = Calendar.getInstance();
        rtn.setTimeInMillis(now);
        rtn.set(Calendar.MONTH, 0);
        rtn.set(Calendar.DAY_OF_MONTH, 1);
        rtn.set(Calendar.HOUR_OF_DAY, 0);
        rtn.set(Calendar.MINUTE, 0);
        rtn.set(Calendar.SECOND, 0);
        rtn.set(Calendar.MILLISECOND, 0);
        return rtn.getTimeInMillis();
    }
}

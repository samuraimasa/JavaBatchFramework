package framework.util;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * 日付をCalenderを用いて処理してる
 *
 */
public class UtilDate {
    public static final String FORMAT_DATE = "yyyy-MM-dd";
    public static final String FORMAT_DATETIME = "yyyy-MM-dd HH:mm:ss";
    
    private static final String[] WEEKDAY_NAME = {"", "日", "月", "火", "水", "木", "金", "土"};
    
    static {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Tokyo"));
    }
    
    /**
     * "yyyy-mm-dd"形式の文字列から、日付をカレンダー形式で取得
     * @param dateStr
     * @return
     */
    public static Calendar getCalendar(String dateStr) throws Exception{
        String[] ymd = dateStr.split("-");
        int y = Integer.parseInt(ymd[0]);
        int m = Integer.parseInt(ymd[1]);
        int d = Integer.parseInt(ymd[2]);
        
        return getCalender(y, m, d);
    }
    
    /**
     * "yyyy-MM-dd HH:mm:ss"形式の文字列から、日付をカレンダー形式で取得
     * @param dateTimeStr
     * @return
     */
    public static Calendar getCalendarDateTime(String dateTimeStr) {
        String[] ymd = dateTimeStr.substring(0, 10).split("-");
        String[] his = dateTimeStr.substring(11, 19).split(":");
        
        int y = Integer.parseInt(ymd[0]);
        int m = Integer.parseInt(ymd[1]);
        int d = Integer.parseInt(ymd[2]);
        int h = Integer.parseInt(his[0]);
        int i = Integer.parseInt(his[1]);
        int s = Integer.parseInt(his[2]);
        
        return new GregorianCalendar(y, m - 1, d, h, i, s);
    }
    
    public static Calendar getCalender(int y, int m, int d) throws Exception{
        return new GregorianCalendar(y, m - 1, d);
    }
    public static int getYear(Calendar date){
        return date.get(Calendar.YEAR);
    }
    public static int getMonth(Calendar date){
        return date.get(Calendar.MONTH) + 1;
    }
    public static int getDay(Calendar date){
        return date.get(Calendar.DAY_OF_MONTH);
    }
    public static int getWeek(Calendar date){
        return date.get(Calendar.WEEK_OF_MONTH);
    }
    public static int getWeekday(Calendar date){
        return date.get(Calendar.DAY_OF_WEEK);
    }
    public static String getWeekdayName(Calendar date){
        return WEEKDAY_NAME[date.get(Calendar.DAY_OF_WEEK)];
    }
    
    public static int getYear(String date) throws Exception{
        return getCalendar(date).get(Calendar.YEAR);
    }
    public static int getMonth(String date) throws Exception{
        return getCalendar(date).get(Calendar.MONTH) + 1;
    }
    public static int getDay(String date) throws Exception{
        return getCalendar(date).get(Calendar.DAY_OF_MONTH);
    }
    public static int getWeek(String date) throws Exception{
        return getCalendar(date).get(Calendar.WEEK_OF_MONTH);
    }
    public static int getWeekday(String date) throws Exception{
        return getCalendar(date).get(Calendar.DAY_OF_WEEK);
    }
    public static String getWeekdayName(String date) throws Exception{
        return WEEKDAY_NAME[getCalendar(date).get(Calendar.DAY_OF_WEEK)];
    }
    
    /**
     * 日を加算
     * @param date
     * @param addNum
     */
    public static void addDay(Calendar date, int addNum){
        date.add(Calendar.DATE, addNum);
    }
    
    /**
     * 月を加算
     * @param date
     * @param addNum
     */
    public static void addMonth(Calendar date, int addNum){
        date.add(Calendar.MONTH, addNum);
    }
    
    /**
     * 指定日数ずらした日付を文字列で取得
     * @param date
     * @return
     * @throws Exception
     */
    public static String addDay(String date, int addNum) throws Exception {
        Calendar cal = getCalendar(date);
        addDay(cal, addNum);
        return formatString(cal);
    }
    public static String addMonth(String date, int addNum) throws Exception {
        Calendar cal = getCalendar(date);
        addMonth(cal, addNum);
        return formatString(cal);
    }
    
    public static void nextDay(Calendar date){
        addDay(date, 1);
    }
    
    public static String addDateTime(String dateTime, int addNum) throws Exception {
        Calendar cal = getCalendarDateTime(dateTime);
        addDay(cal, addNum);
        return formatString(cal, FORMAT_DATETIME);
    }
    
    /**
     * 秒単位のTV値を返す
     * @return
     * @throws Exception
     */
    public static long getTv() throws Exception {
        return getTv(UtilData.getSystemDate());
    }
    public static long getTv(String datetime) throws Exception {
        return getTv(UtilData.toTimestamp(datetime));
    }
    public static long getTv(Timestamp time) throws Exception {
        if(time == null) return 0;
        return time.getTime() / 1000;
    }
    public static String formatString(long tv) throws Exception {
        return formatString(tv, FORMAT_DATE);
    }
    public static String formatString(long tv, String format) throws Exception {
        Timestamp time = new Timestamp(tv * 1000);
        return UtilData.formatTimestamp(time, format);
    }
    
    /**
     * 今日
     * @return
     */
    public static Calendar getToday(){
        return new GregorianCalendar();
    }
    
    /**
     * 日付を文字列にする
     * @param date
     * @param format
     * @return
     */
    public static String formatString(Calendar date, String format){
        return new SimpleDateFormat(format).format(date.getTime());
    }
    public static String formatString(Calendar date){
        return formatString(date, FORMAT_DATE);
    }
    
    /**
     * 日付文字列かどうか？
     * @param dateStr
     * @return
     */
    public static boolean isDate(String dateStr){
        return UtilData.isLegalFormat(dateStr, UtilData.TYPE_TIMESTAMP);
    }
    
    /**
     * 日付の比較
     * @param dateA
     * @param dateB
     * @return -1: A < B  0: A = B  1: A > B 
     */
    public static int compare(Calendar dateA, Calendar dateB){
        if(dateA.before(dateB)) return -1;
        if(dateA.after(dateB)) return 1;
        return 0;
    }
    public static int compare(String dateA, String dateB) throws Exception{
        Calendar calA = getCalendar(dateA);
        Calendar calB = getCalendar(dateB);
        return compare(calA, calB);
    }
    
    public static int compareDateTime(String dateA, String dateB) throws Exception{
        Calendar calA = getCalendarDateTime(dateA);
        Calendar calB = getCalendarDateTime(dateB);
        return compare(calA, calB);
    }
    
    /**
     * 差分の取得
     */
    public static int diffDateTime(String dateA, String dateB) throws Exception{
        Calendar calA = getCalendarDateTime(dateA);
        Calendar calB = getCalendarDateTime(dateB);
        long oneDay = 1000 * 60 * 60 * 24;
        
        long diff = (calA.getTimeInMillis() - calB.getTimeInMillis()) / oneDay;
        return (int)diff;
    }
    
    /**
     * 今月初日を取得
     * @param nengetsu
     * @param firstdayFlg
     * @return
     */
    public static long getFirstDayTv() throws Exception{
        Calendar cal = Calendar.getInstance();

        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int date = 1;
        cal.set(year, month, date, 0, 0, 0);
        return cal.getTimeInMillis() / 1000;
    }
    
    public static String getFirstMonth() throws Exception{
        return formatString(getFirstDayTv(), FORMAT_DATE);
    }
}
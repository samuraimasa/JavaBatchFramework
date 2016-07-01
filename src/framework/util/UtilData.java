package framework.util;

import java.sql.Timestamp;
import java.sql.Types;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 諸々のデータ操作
 *
 */
public class UtilData {
    // 列の型
    public static final int TYPE_STRING = Types.VARCHAR;
    public static final int TYPE_LONG = Types.INTEGER;
    public static final int TYPE_DOUBLE = Types.DOUBLE;
    public static final int TYPE_TIMESTAMP = Types.TIMESTAMP;
    public static final int TYPE_BOOLEAN = Types.BOOLEAN;
    public static final int TYPE_NULL = Types.NULL;
    
    // 変換の型
    public static final String FORMAT_NUMBER = "###,###";
    public static final String FORMAT_DATE = "yyyy-MM-dd";
    public static final String FORMAT_PERCENT = "###%";
    
    public static final String FORMAT_CHECK_DATE = "[1-2][0-9]{3}-[0-9]{1,2}-[0-9]{1,2}";
    
    private static HashMap<String, Pattern> formatMap = new HashMap<>();
    
    // nullでない文字列にする
    public static String toString(String longValue){
        return (longValue == null ? "" : longValue);
    }
    public static String toString(long longValue){
        return toString(new Long(longValue));
    }
    public static String toString(Long lVal){
        if(lVal == null) return "";
        return lVal.toString();
    }
    public static String toString(double doubleValue){
        return toString(new Double(doubleValue));
    }
    public static String toString(Double dVal){
        if(dVal == null) return "";
        
        // 小数以下がなければ、"10.0"ではなく"10"と出したい
        Long lVal = new Long(dVal.longValue());
        
        if(lVal.doubleValue() == dVal.doubleValue()){
            return lVal.toString();
        }
        
        return dVal.toString();
    }
    
    // 日付や数値をフォーマットに合わせて文字列に変換
    public static String toString(Object value, String format){ return toString(value, getType(value), format); }
    public static String toString(Object value, int type, String format){
        if(value == null) return "";
        
        if(format == null){
            switch(type){
            case TYPE_STRING:        return (String)value;
            case TYPE_TIMESTAMP:    return ((Timestamp)value).toString();
            case TYPE_LONG:            return toString((Long)value);
            case TYPE_DOUBLE:        return toString((Double)value);
            }
        }
        else {
            switch(type){
            case TYPE_STRING:        return (String)value;
            case TYPE_TIMESTAMP:    return formatTimestamp((Timestamp)value, format);
            
            case TYPE_LONG:
            case TYPE_DOUBLE:
                DecimalFormat decimalFormat = new DecimalFormat(format);
                return decimalFormat.format(value);
            }
        }
        
        return "";
    }
    
    public static int getType(Object object){
        if(object == null) return TYPE_NULL;
        
        String className = object.getClass().getName();
        
        if(String.class.getName().equals(className)){
            return TYPE_STRING;
        }else if(Long.class.getName().equals(className)){
            return TYPE_LONG;
        }else if(Double.class.getName().equals(className)){
            return TYPE_DOUBLE;
        }else if(Timestamp.class.getName().equals(className)){
            return TYPE_TIMESTAMP;
        }else if(Boolean.class.getName().equals(className)){
            return TYPE_BOOLEAN;
        }
        
        return TYPE_NULL;
    }
    
    public static String toUpperCase(String value){
        return toString(value).toUpperCase();
    }
    
    public static long longValue(Long value){
        return (value == null ? 0 : value.longValue());
    }
    public static long longValue(Double value){
        return (value == null ? 0 : value.longValue());
    }
    public static long longValue(String value){
        if(UtilMisc.isEmpty(value)) return 0;
        if(!isLegalFormat(value, TYPE_LONG)) return 0;
        return Long.parseLong(value);
    }
    
    public static int intValue(Long value){
        return (value == null ? 0 : value.intValue());
    }
    public static int intValue(String value){
        return (int)longValue(value);
    }
    
    public static double doubleValue(Double value){
        return (value == null ? 0 : value.doubleValue());
    }
    public static double doubleValue(Long value){
        return (value == null ? 0 : value.doubleValue());
    }
    public static double doubleValue(String value){
        if(!isLegalFormat(value, TYPE_DOUBLE)) return 0;
        return Double.parseDouble(value);
    }
    
    public static boolean booleanValue(Boolean value){
        return (value == null ? false : value.booleanValue());
    }
    public static boolean booleanValue(Long value){
        if(value == null) return false;
        return (((Long)value).longValue() == 0 ? false : true);
    }
    public static boolean booleanValue(Double value){
        if(value == null) return false;
        return (((Double)value).doubleValue() == 0 ? false : true);
    }
    
    public static boolean isLegalFormat(String value, int type){
        // 空なら無視
        if(UtilMisc.isEmpty(value)) return true;
        
        // スペース
        String val = value.trim();
        if(UtilMisc.isEmpty(val)) return true;
        
        // 型ごとに判定
        switch(type){
        case TYPE_LONG:
            // 0, 147, 00763
            if(formatCheck(val, "-?[0-9]+")) return true;
            break;
            
        case TYPE_DOUBLE:
            // 0, 147, 00763
            if(formatCheck(val, "-?[0-9]+")) return true;
            
            // 0, 147, 00763, 65.0, 026.954
            if(formatCheck(val, "-?[0-9]+\\.[0-9]+")) return true;
            break;
            
        case TYPE_TIMESTAMP:
            if(toTimestamp(val) != null) return true;
            break;
            
        default:
            return true;
        }
        return false;
    }
    
    // 日付型に変換
    public static Timestamp toTimestamp(String value){
        if(UtilMisc.isEmpty(value)) return null;
        
        // 2015.10.31, 2015/10/31 → 2015-10-31
        String val = value.trim().replaceAll("\\.", "-").replaceAll("/", "-");
        
        if(formatCheck(val, "[1-2][0-9]{3}-[0-9]{1,2}-[0-9]{1,2}")){
            // 2015-10-31
        }else if(formatCheck(val, "[0-9]{2}-[0-9]{1,2}-[0-9]{1,2}")){
            if(val.substring(0, 1).equals("9")){
                // 98-10-31 → 1998-10-31
                val = new StringBuffer().append("19").append(val).toString();
            }else{
                // 15-10-31 → 2015-10-31
                val = new StringBuffer().append(getSystemDate("yyyy").substring(0, 2)).append(val).toString();
            }
        }else if(formatCheck(val, "[0-9]{1,2}-[0-9]{1,2}")){
            // 10-31 → 2015-10-31 
            val = new StringBuffer().append(getSystemDate("yyyy")).append("-").append(val).toString();
        }else if(formatCheck(val, "[1-2][0-9]{3}-[0-9]{1,2}-[0-9]{1,2} [0-9]{2}:[0-9]{2}:[0-9]{2}")){
            // 2015-10-31 15:30:22
            val += ".000";
        }else if(formatCheck(val, "[1-2][0-9]{3}-[0-9]{1,2}-[0-9]{1,2} [0-9]{2}:[0-9]{2}:[0-9]{2}-[0-9]+")){
            // 2015-10-31 15:30:22-120 → 2015-10-31 15:30:22.120
            val = new StringBuffer().append(val.substring(0, 19)).append(".").append(val.substring(20, val.length())).toString();
        }else{
            return null;
        }
        
        // 年月日の文字列取得
        String[] ymd = val.split(" ")[0].split("-");
        
        // 年チェック
        int y = Integer.parseInt(ymd[0]);
        if(y < 1900 || y > Integer.parseInt(getSystemDate("yyyy")) + 50) return null;
        
        // 月チェック
        int m = Integer.parseInt(ymd[1]);
        if(m < 1 || m > 12) return null;
        
        // 日チェック
        int d = Integer.parseInt(ymd[2]);
        if(d < 1 || d > 31) return null;
        
        // 年月日チェック
        Calendar date = new GregorianCalendar(y, m - 1, d);
        if(date.get(Calendar.YEAR) != y || date.get(Calendar.MONTH) != m - 1 || date.get(Calendar.DATE) != d) return null;
        
        // 時刻がなければつける
        if(val.indexOf(":") == -1) val += " 00:00:00.000";
        Timestamp time = Timestamp.valueOf(val);
        
        return time;
    }
    
    // システム日付を日付型で返す
    public static Timestamp getSystemDate(){
        return new Timestamp(Calendar.getInstance().getTimeInMillis());
    }
    
    // システム日付を文字列で返す
    public static String getSystemDate(String format){
        return formatTimestamp(getSystemDate(), format);
    }
    
    // 日付型を文字列に変換する
    public static String formatTimestamp(Timestamp timestamp, String format){
        SimpleDateFormat DateFormat = new SimpleDateFormat(format);
        return DateFormat.format(timestamp);
    }
    
    // 正規表現の形式かどうか
    public static boolean formatCheck(String value, String pattern){
        Pattern pt = (Pattern)formatMap.get(pattern);
        if(pt == null) {
            pt = Pattern.compile(pattern);
            formatMap.put(pattern, pt);
        }
        Matcher matcher = pt.matcher(value);
        if(matcher.find()){
            return true;
        }else{
            return false;
        }
    }
}
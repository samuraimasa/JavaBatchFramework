package framework.util;

import java.util.Calendar;
import java.util.HashMap;

/**
 * プログラム実行時引数の設定
 *
 */
public class UtilArg {
    // 共通のプログラム実行引数
    public static final String    ARG_HELP        = "help";
    public static final String    ARG_DATE        = "date";
    public static final String    ARG_ERROR       = "error";
    
    /**
     * mainの引数で指定された値をMapにして返す
     */
    public static HashMap<String,String> getArgMap(String[] args){
        HashMap<String,String> argMap = new HashMap<>();
        
        // まずは機械的にMapに格納
        for(int i = 0; i < args.length; i++){
            String arg = args[i];
            if(arg.length() == 0) continue;
            
            if(arg.charAt(0) != '-') continue;
            
            // key: -date → "date"
            String key = arg.substring(1, arg.length()).toLowerCase();
            
            // value: -date 2015-11-15 → "2015-11-15"
            String value = null;
            if(i + 1 < args.length) {
                if(args[i + 1].length() > 0 && args[i + 1].charAt(0) != '-') {
                    value = args[i + 1];
                    i++;
                }
            }

            // put("date", "2015-11-15")
            argMap.put(key, value);
            
        }
        
        // 日付
        if(UtilMisc.isEmpty((String)argMap.get(ARG_DATE))){
            // 対象日付が指定なければ、今日とする
            Calendar date = UtilDate.getToday();
            argMap.put(ARG_DATE, UtilDate.formatString(date));
        }

        return argMap;
    }
}

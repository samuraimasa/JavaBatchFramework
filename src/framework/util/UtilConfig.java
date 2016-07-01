package framework.util;

import java.util.HashMap;

/**
 * conf読み込み
 *
 */
public class UtilConfig {
    public static final String APP_CONF         = "app.conf";       // 本番用Config
    public static final String APP_LOCAL_CONF   = "app.local.conf"; // 開発用Config

    public static final String APP_NAME         = "app_name";
    public static final String DB_SERVER        = "db_server";
    public static final String DB_SCHEMA        = "db_schema";
    public static final String DB_USER          = "db_user";
    public static final String DB_PASSWORD      = "db_password";

    public static HashMap<String,String> getConfigMap(){
        HashMap<String,String> confMap = new HashMap<>();
        if(UtilFile.isFile(UtilConst.DIR_CONF + APP_LOCAL_CONF)){
            confMap = UtilFile.readConf(UtilConst.DIR_CONF + APP_LOCAL_CONF, UtilFile.ENCODE_UTF8, UtilFile.TO_LOWER, confMap);
        }else{
            confMap = UtilFile.readConf(UtilConst.DIR_CONF + APP_CONF, UtilFile.ENCODE_UTF8, UtilFile.TO_LOWER, confMap);
        }
        return confMap;
    }

    public static HashMap<String, String> getConfMap(String confName){
        HashMap<String, String> confMap = new HashMap<>();
        confMap = UtilFile.readConf(UtilConst.DIR_CONF + confName, UtilFile.ENCODE_UTF8, UtilFile.TO_LOWER, confMap);
        return confMap;
    }
}

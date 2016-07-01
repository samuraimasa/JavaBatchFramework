package framework.form;

import java.util.HashMap;

import framework.util.UtilMisc;

/**
 * 環境変数などを格納するForm
 * @author masa
 *
 */
public class ArgForm{
    private HashMap<String,String> argMap = new HashMap<>();    // プログラム実行引数
    private HashMap<String,String> confMap = null;              // 設定ファイル
    
    /**
     * Getter
     * @param key
     * @return
     */
    public String get(String key){
        return (String)this.argMap.get(key);
    }
    
    public String get(String key, String defaultValue){
        String val = get(key);
        if(val == null || val.length() == 0) return defaultValue;
        return val;
    }
    
    public boolean containsKey(String key){
        return this.argMap.containsKey(key);
    }
    
    public int getInt(String key){
        return UtilMisc.toInt(get(key));
    }
    
    public int getInt(String key, int defaultValue){
        int val = getInt(key);
        return (val == 0 ? defaultValue : val);
    }

    public void set(String key, String value){
        if(key == null) return;
        this.argMap.put(key, value);
    }
    
    public void setInt(String key, int value){
        if(key == null) return;
        set(key, Integer.toString(value));
    }
    
    public void setInt(String key, String value){
        set(key, value);
    }

    public void reset(){
        this.argMap = new HashMap<String,String>();
    }

    public String getConf(String key){
        return (String)this.confMap.get(key);
    }
    
    public int getConfInt(String key){
        return UtilMisc.toInt(getConf(key));
    }
    
    public int getConfInt(String key, int defaultValue){
        int val = getConfInt(key);
        if(val == 0) return defaultValue;
        return val;
    }
    
    public HashMap<String,String> getConfMap() {
        return confMap;
    }

    public void setConfMap(HashMap<String,String> confMap) {
        this.confMap = confMap;
    }

    public void setArgMap(HashMap<String,String> argMap) {
        this.argMap = argMap;
    }
    
    public HashMap<String,String> getArgMap() {
        return argMap;
    }
}
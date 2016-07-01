package framework.util;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * list と map ヒュージョン
 *
 */
@SuppressWarnings("rawtypes")
public class AssocList extends LinkedHashMap implements Cloneable {
    private static final long serialVersionUID = 5892040595225825124L;
    
    static AssocList defaultList = new AssocList();
    private boolean isMap = false;
    
    public boolean isMap() { return this.isMap; }
    
    //--------------------------------------------------------------------------
    // Path
    
    /**
     * list.append("aa").append("bb").put("key", "value");
     * @param key
     * @return
     */
    public AssocList append(Object key) {
        Object obj = this.get(key);
        if(obj != null && obj instanceof AssocList) return (AssocList)obj;
        AssocList list = new AssocList();
        this.put(key, list);
        return list;
    }
    public AssocList append(long key) { return this.append(longKey(key)); }
    public AssocList append(Object key, Object value) {
        Object obj = this.get(key);
        if(obj != null && obj instanceof AssocList) return (AssocList)obj;
        this.put(key, value);
        return this;
    }
    
    /**
     * list.find("aa").find("bb").get("key");
     * @param key
     * @return
     */
    public AssocList find(Object key) {
        Object obj = this.get(key);
        if(obj != null && obj instanceof AssocList) return (AssocList)obj;
        return defaultList;
    }
    public AssocList find(long key) { return this.find(longKey(key)); }
    
    public static boolean isAssocList(Object obj) { return (obj instanceof AssocList); }
    
    //-----------------------------------------
    // Getter
    
    public String getString(Object key) { return toString(this.get(key)); }
    public int getInt(Object key) { return toInt(this.get(key)); }
    public long getLong(Object key) { return toLong(this.get(key)); }
    public double getDouble(Object key) { return toDouble(this.get(key)); }
    public boolean getBoolean(Object key) { return toBoolean(this.get(key)); }
    
    public boolean containsKey(long key) { return this.containsKey(longKey(key)); }
    public Object get(long key) { return this.get(longKey(key)); }
    public String getString(long key) { return this.getString(longKey(key)); }
    public int getInt(long key) { return this.getInt(longKey(key)); }
    public long getLong(long key) { return this.getLong(longKey(key)); }
    public double getDouble(long key) { return this.getDouble(longKey(key)); }
    public boolean getBoolean(long key) { return this.getBoolean(longKey(key)); }
    
    //-----------------------------------------
    // Setter
    
    public Object put(Object key, Object value) {
        return this.put(key, value, false);
    }
    public Object put(Object key, Object value, boolean isAdd) {
        if(this == defaultList) return null;
        if(!isAdd) this.isMap = true;
        return super.put(key, value);
    }
    
    public void put(Object key, long value) { this.put(key, Long.valueOf(value)); }
    public void put(Object key, double value) { this.put(key, new Double(value)); }
    public void put(Object key, boolean value) { this.put(key, Boolean.valueOf(value)); }
    
    public void put(long key, Object value) { this.put(longKey(key), value); }
    public void put(long key, long value) { this.put(longKey(key), value); }
    public void put(long key, double value) { this.put(longKey(key), value); }
    public void put(long key, boolean value) { this.put(longKey(key), value); }

    private Object nextKey() { return longKey(this.size()); }
    private Object longKey(long key) { return Long.valueOf(key); }
    
    /**
     * list.add("aaa");
     * list.add("bbb");
     * for(int i = 0; i < list.size(); i++) {
     *     System.out.println(list.get(i);
     * }
     * @param value
     */
    public boolean add(Object value) { this.put(nextKey(), value, true); return true; }
    public boolean add(long value) { return this.add(Long.valueOf(value)); }
    public boolean add(double value) { return this.add(new Double(value)); }
    
    public boolean add(int index, Object value) { this.put(longKey(index), value, true); return true; }
    public boolean add(int index, long value) { return this.add(index, Long.valueOf(value)); }
    public boolean add(int index, double value) { return this.add(index, new Double(value)); }
    
    //--------------------------------------------------------------------------
    // Convert
    
    private String toString(Object obj) {
        if(obj instanceof String) return (String)obj;
        if(obj != null) return obj.toString();
        return null;
    }
    private int toInt(Object obj) {
        if(obj instanceof Long) return ((Long)obj).intValue();
        if(obj instanceof Integer) return ((Integer)obj).intValue();
        if(obj instanceof Double) return ((Double)obj).intValue();
        if(obj instanceof String) {
            try {
                return Integer.parseInt((String)obj);
            } catch(Exception e) {
            }
        }
        return 0;
    }
    private long toLong(Object obj) {
        if(obj instanceof Long) return ((Long)obj).longValue();
        if(obj instanceof Integer) return ((Integer)obj).longValue();
        if(obj instanceof Double) return ((Double)obj).longValue();
        if(obj instanceof String) {
            try {
                return Long.parseLong((String)obj);
            } catch(Exception e) {
            }
        }
        return 0;
    }
    private double toDouble(Object obj) {
        if(obj instanceof Double) return ((Double)obj).doubleValue();
        if(obj instanceof Long) return ((Long)obj).doubleValue();
        if(obj instanceof Integer) return ((Integer)obj).doubleValue();
        if(obj instanceof String) {
            try {
                return Double.parseDouble((String)obj);
            } catch(Exception e) {
            }
        }
        return 0;
    }
    private boolean toBoolean(Object obj) {
        if(obj instanceof Boolean) return ((Boolean)obj).booleanValue();
        return false;
    }
    
    //--------------------------------------------------------------------------
    // loop
    
    /**
     * 使用例：
     * list.beforeFirst();
     * while(list.next()) {
     *         list.getKey();
     *         list.getValue();
     * }
     */
    
    private Object[][] entries = null;
    private int p = 0;
    
    public void beforeFirst() {
        makeEntries();
        p = -1;
    }
    public boolean next() {
        return absolute(p + 1);
    }
    public boolean absolute(int i) {
        if(this.entries == null) makeEntries();
        p = i;
        return (0 <= p && p < this.entries.length);
    }
    public Object getKey() {
        if(this.entries == null) return null;
        if((0 <= p && p < this.entries.length)) return entries[p][0];
        return null;
    }
    public Object getValue() {
        if(this.entries == null) return null;
        if((0 <= p && p < this.entries.length)) return entries[p][1];
        return null;
    }
    
    public int getKeyInt() { return toInt(getKey()); }
    public long getKeyLong() { return toLong(getKey()); }
    public double getKeyDouble() { return toDouble(getKey()); }
    public String getKeyString() { return toString(getKey()); }
    
    public int getValueInt() { return toInt(getValue()); }
    public long getValueLong() { return toLong(getValue()); }
    public double getValueDouble() { return toDouble(getValue()); }
    public String getValueString() { return toString(getValue()); }
    
    private void makeEntries() {
        entries = getEntries();
    }
    
    public Object[][] getEntries() {
        Object[][] entries = new Object[this.size()][2];
        int i = 0;
        Iterator it = this.entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry entry = (Map.Entry)it.next();
            entries[i][0] = entry.getKey();
            entries[i][1] = entry.getValue();
            i++;
        }
        return entries;
    }
    
    //--------------------------------------------------------------------------
    
    public int length() { return size(); }
    
    public static String implode(String splitter, AssocList list) {
        if(list == null) return null;
        
        StringBuilder buf = new StringBuilder();
        Object[][] entries = list.getEntries();
        for(int i = 0; i < entries.length; i++) {
            Object value = entries[i][1];
            
            if(i > 0) buf.append(splitter);
            if(value != null) buf.append(value.toString());
        }
        return buf.toString();
    }
    
    public static String implodeKey(String splitter, AssocList list) {
        if(list == null) return null;
        
        StringBuilder buf = new StringBuilder();
        Object[][] entries = list.getEntries();
        for(int i = 0; i < entries.length; i++) {
            Object value = entries[i][0];
            
            if(i > 0) buf.append(splitter);
            if(value != null) buf.append(value.toString());
        }
        return buf.toString();
    }
    
    public static AssocList explode(String splitter, String text) {
        return explode(splitter, text, 0);
    }
    public static AssocList explode(String splitter, String text, int length) {
        if(text == null || text.length() == 0) return new AssocList();
        
        AssocList list = new AssocList();
        
        int start = 0, end = 0;
        while(start < text.length() && (end = text.indexOf(splitter, start)) >= 0) {
            list.add(text.substring(start, end));
            start = end + splitter.length();
            if(length > 0 && list.size() >= length - 1) break;
        }
        list.add(text.substring(start));
        
        return list;
    }
    
    public static AssocList explode(String splitter, AssocList text) {
        return explode(splitter, text, 0);
    }
    public static AssocList explode(String splitter, AssocList text, int length) {
        if(text == null || text.size() == 0) return text;
        
        AssocList list = new AssocList();
        
        int start = 0, end = 0;
        while(start < text.size() && (end = text.indexOf(splitter, start)) >= 0) {
            list.add(text.substring(start, end));
            start = end + 1;
            if(length > 0 && list.size() >= length - 1) break;
        }
        list.add(text.substring(start));
        
        return list;
    }
    
    public int indexOf(String str) {
        return indexOf(str, 0);
    }
    public int indexOf(String str, int fromIndex) {
        //if(this.isMap) return -1;
        if(str == null) return -1;
        for(int i = fromIndex; i < this.size(); i++) {
            String row = this.getString(i);
            if(str.equals(row)) return i;
        }
        return -1;
    }
    public int indexOf(String[] strs, int fromIndex) {
        //if(this.isMap) return -1;
        if(strs == null || strs.length == 0) return -1;
        for(int i = fromIndex; i < this.size(); i++) {
            String row = this.getString(i);
            for(int s = 0; s < strs.length; s++) {
                if(strs[s].equals(row)) return i;
            }
        }
        return -1;
    }
    
    public AssocList substring(int beginIndex) {
        return substring(beginIndex, this.size());
    }
    public AssocList substring(int beginIndex, int endIndex) {
        //if(this.isMap) return null;
        AssocList list = new AssocList();
        for(int i = beginIndex; i < endIndex; i++) {
            list.add(this.get(i));
        }
        return list;
    }
    
    public Object[] toArray() {
        Object[][] entries = this.getEntries();
        Object[] arr = new Object[entries.length];
        for(int i = 0; i < entries.length; i++) {
            arr[i] = entries[i][1];
        }
        return arr;
    }
    public int[] toIntArray() {
        Object[][] entries = this.getEntries();
        int[] arr = new int[entries.length];
        for(int i = 0; i < entries.length; i++) {
            arr[i] = toInt(entries[i][1]);
        }
        return arr;
    }
    public String[] toStringArray() {
        Object[][] entries = this.getEntries();
        String[] arr = new String[entries.length];
        for(int i = 0; i < entries.length; i++) {
            arr[i] = toString(entries[i][1]);
        }
        return arr;
    }
    
    public void merge(AssocList list){
        list.beforeFirst();
        while(list.next()){
            put(list.getKey(), list.getValue());
        }
    }
    
    //--------------------------------------------------------------------------
    // caller
    
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("[");
        Object[][] entries = this.getEntries();
        int _p = p;
        for(int i = 0; i < entries.length && i < 100; i++) {
            Object key = entries[i][0];
            Object value = entries[i][1];
            if(i > 0) buf.append(", ");
            buf.append(key).append(": ").append(value);
        }
        p = _p;
        if(this.size() >= 100) buf.append(", ...");
        buf.append("]");
        return buf.toString();
    }
}

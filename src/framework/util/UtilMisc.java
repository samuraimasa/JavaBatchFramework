package framework.util;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashSet;

import framework.util.AssocList;

/**
 * 雑多なライブラリ
 *
 */
public class UtilMisc {
    /**
     * カンマ区切りデータを分割
     */
    public static String[] splitter(String csv){
        return splitter(csv, ",");
    }
    public static String[] splitter(String text, String separator){
        return splitter(text, separator, false);
    }
    public static String[] splitter(String text, String separator, boolean isNullable){
        if(isEmpty(text)) return (isNullable ? null : new String[0]);
        String[] vals = text.split(separator);
        for(int i = 0; i < vals.length; i++) {
            vals[i] = vals[i].trim();
        }
        return vals;
    }
    public static String[] split(String text, String separator) {
        if(text == null || text.length() == 0) return new String[0];
        
        ArrayList<String> list = new ArrayList<>();
        
        if(separator.startsWith("\\") && separator.length() >= 2) separator = separator.substring(1);
        
        int start = 0, end = 0;
        while(start < text.length() && (end = text.indexOf(separator, start)) >= 0) {
            list.add(text.substring(start, end));
            start = end + separator.length();
        }
        list.add(text.substring(start));
        
        String[] ret = new String[list.size()];
        for(int i = 0; i < ret.length; i++) {
            ret[i] = (String)list.get(i);
        }
        return ret;
    }
    
    public static ArrayList<String> splitToList(String csv){
        return splitToList(csv, ",");
    }
    public static ArrayList<String> splitToList(String text, String separator){
        ArrayList<String> list = new ArrayList<>();
        String[] vals = splitter(text, separator);
        for(int i = 0; i < vals.length; i++) {
            if(vals[i].length() > 0) list.add(vals[i]);
        }
        return list;
    }
    
    public static int[] intSplitter(String text, String separator, boolean isNullable){
        String[] vals = splitter(text, separator);
        if(vals == null || vals.length == 0) return (isNullable ? null : new int[0]);
        int[] intValues = new int[vals.length];
        for(int i = 0; i < vals.length; i++){
            intValues[i] = UtilMisc.toInt(vals[i]);
        }
        return intValues;
    }
    
    public static int[] listToIntArray(ArrayList<Integer> list) {
        int[] ids = new int[list.size()];
        for(int i = 0; i < list.size(); i++) {
            ids[i] = ((Integer)list.get(i)).intValue();
        }
        return ids;
    }
    
    /**
     * 文字列の配列に変換
     */
    public static String[] toStringArray(ArrayList<String> list) {
        int len = (list == null ? 0 : list.size());
        String[] arr = new String[len];
        if(len > 0) {
            for(int i = 0; i < len; i++) {
                Object obj = list.get(i);
                if(obj == null) continue;
                arr[i] = obj.toString();
            }
        }
        return arr;
    }
    
    /**
     * csvをHashSetに変換
     * @param csv
     * @return
     */
    public static HashSet<String> getCsvSet(String csv) {
        if(UtilMisc.isEmpty(csv)) return null;
        
        HashSet<String> set = new HashSet<>();
        String [] strArr = csv.split(",");
        int count = strArr.length;
        for(int i = 0; i < count ;i++){
            set.add(strArr[i]);
        }
        
        return set;
    }
    
    /**
     * 空の文字列かどうか
     */
    public static boolean isEmpty(String value){
        return (value == null || value.length() == 0);
    }
    
    /**
     * リストがnullまたは空
     */
    public static boolean isEmpty(ArrayList<?> list){
        return (list == null || list.size() == 0);
    }
    
    /**
     * 文字列が、特定のキーワードを含んでいるかどうか
     * @param src
     * @param words
     * @return
     */
    public static boolean containsEitherWord(String src, String[] words){
        return containsEitherWord(src, words, true);
    }
    public static boolean containsEitherWord(String src, String[] words, boolean isLike){
        if(isEmpty(src) || words == null || words.length == 0) return false;
        
        for(int i = 0; i < words.length; i++){
            if(words[i].length() == 0) continue;
            if(isLike && src.indexOf(words[i]) != -1) return true;
            if(!isLike && src.equals(words[i])) return true;
        }
        
        return false;
    }
    
    /**
     * リストをCSVに変換
     * @param list
     * @return
     */
    public static String toString(ArrayList<String> list, String separator){
        StringBuilder buf = new StringBuilder();
        if(!isEmpty(list)){
            for(int i = 0; i < list.size(); i++){
                if(i > 0) buf.append(separator);
                buf.append(list.get(i).toString());
            }
        }
        return buf.toString();
    }
    public static String toString(String[] list, String separator){
        StringBuffer buf = new StringBuffer();
        if(list != null){
            for(int i = 0; i < list.length; i++){
                if(i > 0) buf.append(separator);
                buf.append(list[i]);
            }
        }
        return buf.toString();
    }
    public static String toString(AssocList list, String separator){
        StringBuffer buf = new StringBuffer();
        if(!list.isEmpty()){
            list.beforeFirst();
            while(list.next()){
                if(buf.length() > 0) buf.append(separator);
                buf.append(list.getValueString());
            }
        }
        return buf.toString();
    }
    public static String toString(long[] list, String separator){
        StringBuilder buf = new StringBuilder();
        if(list != null){
            for(int i = 0; i < list.length; i++){
                if(i > 0) buf.append(separator);
                buf.append(list[i]);
            }
        }
        return buf.toString();
    }
    public static String toString(int[] list, String separator){
        StringBuilder buf = new StringBuilder();
        if(list != null){
            for(int i = 0; i < list.length; i++){
                if(i > 0) buf.append(separator);
                buf.append(list[i]);
            }
        }
        return buf.toString();
    }
    
    /**
     * リストをCSVに変換
     */
    public static String toCsv(ArrayList<String> list){
        return toString(list, ", ");
    }
    
    public static String toCsv(AssocList list){
        return toString(list, ", ");
    }
    
    /**
     * 高速な数値変換ロジック（正の数のみ、Hexや小数には対応しない）
     */
    public static int toInt(String value) {
        return (int)toLong(value);
    }
    public static long toLong(String value) {
        long n = 0;
        byte sign = 0;
        if(value == null) return 0;
        
        for(int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if(c == '-' && sign == 0) {
                sign = -1;
                continue;
            }
            if(c < '0' || c > '9') {
                if(n > 0) break;
                if(c != ' ' && c != ',') break;
                continue;
            }
            n *= 10;
            n += c - '0';
        }
        
        if(sign == -1) n *= sign;
        return n;
    }
    public static double toDouble(String value) {
        long n = 0;
        byte sign = 0;
        boolean isDecimal = false;
        int div = 1;
        if(value == null) return 0;
        
        for(int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if(c == '-' && sign == 0) {
                sign = -1;
                continue;
            }
            if(c == '.') {
                isDecimal = true;
                continue;
            }
            if(c < '0' || c > '9') {
                if(n > 0) break;
                if(c != ' ' && c != ',') break;
                continue;
            }
            n *= 10;
            n += c - '0';
            if(isDecimal) div *= 10;
        }
        
        if(sign == -1) n *= sign;
        return (div == 1 ? (double)n : ((double)n / (double)div));
    }
    public static boolean isNumber(String value) {
        return isNumber(value, false);
    }
    public static boolean isNumber(String value, boolean whenNull) {
        if(isEmpty(value)) return whenNull;
        
        for(int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if(c == '-' && i == 0) continue;
            if(c < '0' || c > '9') {
                if(c == ' ' || c == ',') continue;
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * 割合を求める
     */
    public static long calcRate(long numerator, long denominator) {
        if(numerator == 0 || denominator == 0) return 0;
        return (numerator * 100) / denominator;
    }
    public static long percent(long numerator, long denominator) {
        return calcRate(numerator * 100, denominator);
    }
    
    /**
     * タグなどの文字列を除去
     */
    public static String validateString(String text) {
        if(isEmpty(text)) return text;
        return text.replaceAll("[\\t\\n\\r\\\\<>'\"]", "").trim();
    }
    
    /**
     * 文字列置換
     */
    public static String replaceAll(String text, String src, String dest){
        if(dest == null) dest = "";
        String buf = text;
        int srcLen = src.length(), destLen = dest.length();
        
        if(srcLen == 0 || destLen == 0) return buf;
        
        int start = 0, find;
        
        while((find = buf.indexOf(src, start)) >= 0){
            buf = buf.substring(0, find) + dest + buf.substring(find + srcLen, buf.length());
            start = find + destLen;
        }
        
        return buf;
    }
    
    /**
     * MD5暗号化
     */
    public static String md5(String text) {
        StringBuilder buf = new StringBuilder();
        String hex = "0123456789abcdef";
        
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] dat = text.getBytes();
            md.update(dat);
            dat = md.digest();
            
            for(int i = 0; i < dat.length; i++) {
                int val = dat[i];
                if(val < 0) val += 256;
                buf.append(hex.charAt(val / 16));
                buf.append(hex.charAt(val % 16));
            }
            
        } catch(Exception e) {
            
        }
        
        return buf.toString();
    }
    
    /**
     * シェルの実行
     * @param text
     * @return
     */
    public static boolean execSh(String command) {
        if(isEmpty(command)) return false;
        try {
            String[] cmdarray = {"/bin/sh", "-c", command};
            Runtime.getRuntime().exec(cmdarray);
        } catch(Exception e) {
            return false;
        }
        return true;
    }
    
    /**
     * 文字列の結合
     */
    public static String join(String[] vals, String splitter) {
        return join(vals, splitter, false);
    }
    public static String join(String[] vals, String splitter, boolean endsWithSplitter) {
        StringBuilder buf = new StringBuilder();
        for(int i = 0; i < vals.length; i++) {
            if(!endsWithSplitter && i > 0) buf.append(splitter);
            buf.append(vals[i]);
            if(endsWithSplitter) buf.append(splitter);
        }
        return buf.toString();
    }
}
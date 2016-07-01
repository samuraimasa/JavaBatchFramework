package framework.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

import framework.util.AssocList;
import framework.util.FileStream;

/**
 * ファイル関連
 * 
 */
public class UtilFile {
    public static final String    CRLF              = "\r\n";
    public static final String    CR                = "\r";
    public static final String    LF                = "\n";

    public static final String    ENCODE_UTF8       = "UTF-8";
    public static final String    ENCODE_MS932      = "MS932";
    public static final String    ENCODE_EUC        = "EUC_JP";
    
    public static final int        TO_UPPER         = 1;
    public static final int        TO_LOWER         = 2;
    
    private static final String[][] ESCAPE = {{"\\n", "\n"}, {"\\\"", "\""}, {"\\\\", "\\"}, {"\\t", "\t"}};

    public static final char        SLASH            = '/';
    public static final char        BACK_SLASH       = '\\';
    public static final String      EQUAL            = "=";

    /**
     * 改行コード検出処理
     * 
     * @param    String fileData
     * @return String
     */
    public static String detectNewLineCode(String detectData) throws Exception {
        
        if (detectData.indexOf(CRLF)    != -1) { return CRLF; }
        if (detectData.indexOf(LF)    != -1) { return LF; }
        if (detectData.indexOf(CR)    != -1) { return CR; }

        // デフォルトはLF
        return LF;
    }
    
    
    /**
     * 指定ディレクトリ内および以下のファイル、ディレクトリを削除する
     * @param ディレクトリパス。
     */
    public static boolean clearDir(File dir) {
        if(!dir.exists()){
            //存在しない場合
            return true;
        }
        String[] files = dir.list();
        if(files == null){
            return false;
        }
        for(int i=0;i<files.length;i++){
            File f = new File(dir.getPath(), files[i]);
            if(f.isDirectory()){
                clearDir(f);
            }
            if(!f.delete()){
                return false;
            }
        }
        return true;
    }
    
    
    /**
     * 指定ディレクトリ内のすべてのファイルサイズの合計を調べる
     * @param ディレクトリパス。
     */
    public static long getDirectorySize(File fileInfo) {
        long size = 0;
        //ファイルリスト取得
        File files[] = fileInfo.listFiles();
        //ファイルカウント
        for(int i=0;i<files.length;i++){
            File file = files[i]; 
            //ファイルならサイズを＋
            if(file.isFile()){
                size=size+file.length();
                
            }
            //回帰処理
            if(file.isDirectory()){
                size=size+getDirectorySize(file);
            }
        }
        return size;
    }
    
    /**
     * ファイルが存在するかどうか？
     * @param path
     * @return
     */
    public static boolean isFile(String path){
        if(UtilMisc.isEmpty(path)) return false;
        File file = new File(path);
        return (file.exists() && file.isFile());
    }
    
    /**
     * ディレクトリが存在するかどうか？
     * @param path
     * @return
     */
    public static boolean isDir(String path){
        if(UtilMisc.isEmpty(path)) return false;
        File file = new File(path);
        return (file.exists() && file.isDirectory());
    }
    
    /**
     * ファイル名を結合
     * @param dir
     * @param file
     * @return
     */
    public static String synthPath(String dir, String file){
        char delimiter = getPathDelimiter(dir);
        StringBuffer buf = new StringBuffer();
        buf.append(dir);
        if(dir.charAt(dir.length() - 1) != delimiter) buf.append(delimiter);
        buf.append(file);
        return buf.toString();
    }
    
    /**
     * ファイルのパスが../..か..\..かを判別する
     * @param path
     * @return
     */
    public static char getPathDelimiter(String path){
        
        if (path.indexOf(SLASH) != -1) { return SLASH; }
        if (path.indexOf(BACK_SLASH) != -1) { return BACK_SLASH; }

        // デフォルトはSLASH
        return SLASH;
    }
    
    /**
     * ディレクトリ作成
     * @param path
     * @return
     */
    public static boolean mkdir(String path){ 
        File file = new File(path);
        return file.mkdir();
    }
    
    /**
     * ディレクトリ権限変更
     * @param path
     * @param auth
     * @return
     */
    public static boolean chmod(String path, int auth){ 
        StringBuilder buf = new StringBuilder();

        buf.append("chmod -R ").append(auth).append(" ").append(path);

        return UtilMisc.execSh(buf.toString());
    }
    
    /**
     * ファイルを読み込む
     * @param path
     * @param encode ENCODE_MS932 等
     * @return
     */
    public static String readTextFile(String path, String encode){
        String text = "";
        if(!isFile(path)) return text;
        try{
            // ファイルオープン
            FileInputStream fis = new FileInputStream(path);
            
            // 読み込み
            text = readTextFile(fis, encode);
            
        }catch(Exception e){
        }
        
        return text;
    }
    public static String readTextFile(FileInputStream fis, String encode){
        StringBuilder buf = new StringBuilder();
        
        InputStreamReader isr = null;
        BufferedReader br = null;
        try{
            isr = new InputStreamReader(fis , encode);
            br = new BufferedReader(isr);
            String line;
            int i = 0;
            
            while((line = br.readLine()) != null){
                if(i > 0){
                    buf.append(CRLF);
                }
                buf.append(line);
                i++;
            }
            
        }catch(Exception e){
            
        }finally{
            try{
                if(br != null) br.close();
                if(isr != null) isr.close();
                if(fis != null) fis.close();
            }catch(Exception e){
            }    
        }
        
        return buf.toString();
    }
    
    /**
     * ファイルに書き込む
     * @param path
     * @param text
     * @param encode
     * @return
     */
    public static boolean writeTextFile(String path, String text, String encode, boolean append, boolean throwsException) throws Exception{
        try{
            FileStream fs = FileStream.getWriteStream(path, encode, append);
            fs.write(text);
            fs.close();
            
        }catch(Exception e){
            if(throwsException) throw(e);
            return false;
        }
        
        return true;
    }
    public static boolean writeTextFile(String path, String text, String encode, boolean append){
        try{
            return writeTextFile(path, text, encode, append, false);
        }catch(Exception e){
            return false;
        }
    }
    public static boolean writeTextFile(String path, String text, String encode){
        return writeTextFile(path, text, encode, false);

    }
    
    
    /**
     * 設定ファイルを読んでマップで返します
     * @param fileName
     * @param encode
     * @return
     */
    public static HashMap<String,String> readConf(String fileName, String encode){
        return readConf(fileName, encode, 0);
    }
    public static HashMap<String,String> readConf(String fileName, String encode, int key_case){
        HashMap<String,String> map = new HashMap<>();
        return readConf(fileName, encode, key_case, map);
    }
    public static HashMap<String,String> readConf(String fileName, String encode, int key_case, HashMap<String,String> map){
        
        // ファイルを読み込む
        String text = readTextFile(fileName, encode);
        
        // 行に分解
        String[] lines = text.split(CRLF);
        
        for(int i = 0; i < lines.length; i++){
            String line = lines[i].trim();
            
            // コメント行は無視
            if(line.length() == 0) continue;
            char c = line.charAt(0);
            if(c < 'A' || c > 'z') continue;
            
            // = で区切る
            int pos = line.indexOf(EQUAL);
            if(pos <= 0 || pos == line.length() - 1) continue;
            
            // キーと値を取得
            String key = line.substring(0, pos).trim();
            String value = line.substring(pos + 1, line.length()).trim();
            if(key.length() == 0 || value.length() == 0) continue;
            
            // 最後が'\'で終わっていたら次の行も加える
            if(hasNextLine(value)){
                // 末尾の'\'を除去
                value = value.substring(0, value.length() - 1);
                while(i + 1 < lines.length){
                    i++;
                    value = new StringBuilder(value).append(CRLF).append(lines[i]).toString();
                    if(hasNextLine(value)){
                        value = value.substring(0, value.length() - 1);
                    }else{
                        break;
                    }
                }
            }
            else
            // <<END ～ END に対応
            if(value.length() > 2 && "<<".equals(value.substring(0, 2))) {
                String tag = value.substring(2, value.length());
                StringBuilder buf = new StringBuilder();
                for(int n = i + 1; n < lines.length; n++){
                    if(tag.equals(lines[n])) {
                        // found
                        value = buf.toString();
                        
                        i = n;
                        break;
                    }
                    buf.append(lines[n]).append(CRLF);
                }
            }
            
            // キーは大文字か小文字にすることもできる
            if(key_case == TO_UPPER) key = key.toUpperCase();
            if(key_case == TO_LOWER) key = key.toLowerCase();
            
            // エスケープされた文字列を戻す
            value = decodeEscapeSequence(value);
            
            // Mapに格納
            map.put(key, value);
        }
        
        return map;
    }
    
    /**
     * 最後が'\'で終わっていたら次の行も加える （ただし\\は除く）
     * @param line
     * @return
     */
    private static boolean hasNextLine(String line){
        if(line.charAt(line.length() - 1) != '\\') return false;
        
        if(line.length() > 2 && line.charAt(line.length() - 2) == '\\') return false;
        
        return true;
    }
    
    /**
     * マップに格納されたデータをファイルに書き出す
     * @param values
     * @param fileName
     * @param encode
     */
    public static void writeConf(HashMap<String,String> values, String fileName, String encode){
        StringBuffer sb = new StringBuffer();
        String lineFeed = (ENCODE_MS932.equals(encode) ? CRLF : LF);
        
        // 各キーと値をテキストに変換
        Iterator<String> itr = values.keySet().iterator();
        while(itr.hasNext()){
            String key = (String)itr.next();
            Object value = values.get(key);
            sb.append(key).append(EQUAL);
            if(value != null){
                // 文字をエスケープする
                if(value instanceof String){
                    value = encodeEscapeSequence((String)value);
                }
                
                sb.append(value);
            }
            sb.append(lineFeed);
        }
        
        // ファイルに書き込む
        writeTextFile(fileName, sb.toString(), encode);
    }
    
    /**
     * エスケープされた文字列を戻す  例："\n" → 改行
     * @param text
     * @return
     */
    public static String decodeEscapeSequence(String text){
        for(int i = 0; i < ESCAPE.length; i++){
            text = UtilMisc.replaceAll(text, ESCAPE[i][0], ESCAPE[i][1]);
        }
        return text;
    }
    
    /**
     * 文字列をエスケープする  例：改行 → "\n"
     * @param text
     * @return
     */
    public static String encodeEscapeSequence(String text){
        for(int i = 0; i < ESCAPE.length; i++){
            text = UtilMisc.replaceAll(text, ESCAPE[i][1], ESCAPE[i][0]);
        }
        return text;
    }
    
    /**
     * ディレクトリ以下のすべてのファイルを抽出する
     * @param fileList
     * @param root_dir
     * @param filePattern
     */
    public static void getFileList(ArrayList<String> fileList, String root_dir, String filePattern) {
        getFileList(fileList, root_dir, filePattern, 100);
    }
    public static void getFileList(ArrayList<String> fileList, String root_dir, String filePattern, int depth) {
        File[] subDirs = new File(root_dir).listFiles();
        if(subDirs == null) return;
        
        for(int i = 0; i < subDirs.length; i++) {
            File child = subDirs[i];
            
            if(child.isFile()) {
                // ファイル
                
                // ファイル名がパターンに一致してなければ無視
                String fileName = child.getName();
                if(filePattern != null && UtilData.formatCheck(fileName, filePattern) == false) continue;
                
                // リストに追加
                fileList.add(child.getPath());
            }
            else if(child.isDirectory()) {
                // ディレクトリ
                
                // 再帰的にその下のファイルを見に行く
                if(depth > 1) getFileList(fileList, child.getPath(), filePattern, depth - 1);
            }
        }
    }
    
    public static AssocList getDirTree(String root_dir, int depth) {
        AssocList list = new AssocList();
        getDirTree(list, root_dir, depth);
        return list;
    }
    public static void getDirTree(AssocList list, String root_dir, int depth) {
        File[] subDirs = new File(root_dir).listFiles();
        if(subDirs == null) return;
        
        for(int i = 0; i < subDirs.length; i++) {
            File child = subDirs[i];
            
            if(child.isFile()) {
                // ファイル
                list.put(child.getName(), child.getPath());
            } else if(child.isDirectory()) {
                // ディレクトリ
                
                // 再帰的にその下のファイルを見に行く
                AssocList childList = new AssocList();
                if(depth > 1) getDirTree(childList, child.getPath(), depth - 1);
                list.put(child.getName(), childList);
            }
        }
    }

    /**
     * ファイルの経過時間を取得
     * @param filePath
     * @return
     */
    public static long getPassedTime(String filePath) {
        long tv = 0;
        if(filePath == null) return tv;
        
        try {
            // 現在時を取得
            long now = Calendar.getInstance().getTimeInMillis();
            
            // ファイルの更新時を取得
            long start = new File(filePath).lastModified();
            
            // 経過時間を秒で取得
            tv = (now - start) / 1000;
            
        } catch(Exception e) {
            
        }
        
        return tv;
    }
}
package framework.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/**
 * ファイルストリーム
 *
 */
public class FileStream {
    private static final int TYPE_READ        = 0;
    private static final int TYPE_WRITE       = 1;
    
    private int type = TYPE_READ;
    private boolean isOpen = false;
    
    FileOutputStream fos = null;
    OutputStreamWriter osw = null;
    BufferedWriter bw = null;
    
    FileInputStream fis = null;
    InputStreamReader isr = null;
    BufferedReader br = null;
    
    /**
     * BufferedWriterの取得
     * 
     * 例：
     *  FileStream fs = FileStream.getWriteStream("/var/text.log", FileStream.ENCODE_EUC, true);
     *  fs.write("this is text.");
     *  fs.close();
     */
    public static FileStream getWriteStream(String path, String encode, boolean append) throws Exception {
        FileStream fs = new FileStream();
        fs.type = TYPE_WRITE;
        fs.isOpen = false;
        
        // open file
        fs.fos = new FileOutputStream(path, append);
        fs.osw = new OutputStreamWriter(fs.fos , encode);
        fs.bw = new BufferedWriter(fs.osw);
        
        fs.isOpen = true;
        
        return fs;
    }
    public static FileStream getWriteStream(String path, String encode) throws Exception {
        return getWriteStream(path, encode, false);
    }
    
    /**
     * write all
     * @param text
     * @throws Exception
     */
    public void write(String text) throws Exception {
        if(this.isOpen == false) return;
        if(this.type != TYPE_WRITE) return;
        if(this.bw == null) return;
        
        try {
            this.bw.write(text);
        } catch(Exception e) {
            this.close();
            throw(e);
        }
    }
    
    /**
     * write line
     * @param line
     * @throws Exception
     */
    public void writeln(String line) throws Exception {
        this.write(line);
        this.write(UtilFile.LF);
    }
    
    /**
     * BufferedReaderの取得
     * 
     * 例：
     *  FileStream fs = FileStream.getReadStream("/var/text.log", FileStream.ENCODE_EUC);
     *  String line;
     *  while( (line = fs.readLine()) != null) {
     *      System.out.println(line);
     *  }
     *  fs.close();
     */
    public static FileStream getReadStream(String path, String encode) throws Exception {
        FileStream fs = new FileStream();
        fs.type = TYPE_READ;
        fs.isOpen = false;
        
        // open file
        fs.fis = new FileInputStream(path);
        fs.isr = new InputStreamReader(fs.fis , encode);
        fs.br = new BufferedReader(fs.isr);
        
        fs.isOpen = true;
        
        return fs;
    }
    
    /**
     * read line
     * @return
     * @throws Exception
     */
    public String readLine() throws Exception {
        if(this.isOpen == false) return null;
        if(this.type != TYPE_READ) return null;
        if(this.br == null) return null;
        
        try {
            return this.br.readLine();
            
        } catch(Exception e) {
            this.close();
            throw(e);
        }
    }
    
    /**
     * read all
     */
    public String read() throws Exception {
        return read(UtilFile.CRLF);
    }
    public String read(String crLf) throws Exception {
        StringBuilder buf = new StringBuilder();
        String line;
       
        while((line = this.readLine()) != null){
            if(1 > 0){
                buf.append(crLf);
            }
            buf.append(line);
        }
        
        return buf.toString();
    }
    
    /**
     * Streamのclose
     */
    public void close() {
        if(this.isOpen == false) return;
        this.isOpen = false;
        
        try {
            switch(this.type) {
            case TYPE_READ:
                if(this.br != null) this.br.close();
                if(this.isr != null) this.isr.close();
                if(this.fis != null) this.fis.close();
                break;
            case TYPE_WRITE:
                if(this.bw != null) this.bw.close();
                if(this.osw != null) this.osw.close();
                if(this.fos != null) this.fos.close();
                break;
            }
        } catch(Exception e) {
        } finally {
            switch(this.type) {
            case TYPE_READ:
                this.br = null;
                this.isr = null;
                this.fis = null;
                break;
            case TYPE_WRITE:
                this.bw = null;
                this.osw = null;
                this.fos = null;
                break;
            }
        }
    }
}

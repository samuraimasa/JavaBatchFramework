package framework.util;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.Calendar;

/**
 * 排他制御用
 *
 */
public class MutexManager{
    public static final int MODE_READ         = 1;
    public static final int MODE_WRITE        = 2;
    public static final int MODE_NO_WAIT      = 3;
    
    RandomAccessFile file = null;
    FileChannel channel = null;
    FileLock lock = null;
    int mode = 0;
    String lockFile = null;
    
    /**
     * 排他の取得
     * @param filePath
     * @param mode
     * @return
     */
    public boolean getMutex(String filePath) {
        return getMutex(filePath, MODE_WRITE);
    }
    public boolean getMutex(String filePath, int mode) {
        this.mode = mode;
        try {
            lockFile = filePath + ".lock";
            if(mode == MODE_NO_WAIT) {
                // ロックされていればNG
                if(new File(lockFile).exists()) {
                    return false;
                }
                // ロックする
                FileStream fs = FileStream.getWriteStream(lockFile, UtilFile.ENCODE_UTF8);
                fs.write("lock");
                fs.close();
            } else {
                file = new RandomAccessFile(lockFile, "rw");
                channel = file.getChannel();
                lock = channel.lock(0, Long.MAX_VALUE, (mode == MODE_READ));
            }
        } catch(Exception e) {
            releaseMutex();
            return false;
        }
        
        return true;
    }
    
    /**
     * 排他の解放
     * @param key
     */
    public void releaseMutex() {
        if(mode == MODE_NO_WAIT) {
            if(lockFile != null) {
                File file = new File(lockFile);
                if(file.exists()) file.delete();
                lockFile = null;
            }
        } else {
            try {
                if(lock != null) lock.release();
                if(channel != null) channel.close();
                if(file != null) file.close();
                
            } catch(Exception e) {
                // なにもできない
            }
            lock = null;
            channel = null;
            file = null;
        }
    }
    
    /**
     * ロック時間を秒で取得　※取得できなければ0を返す
     * @return
     */
    public long getLockedTime() {
        long tv = 0;
        if(lockFile == null) return tv;

        // 現在時を取得
        long now = Calendar.getInstance().getTimeInMillis();
        
        // ファイルの更新時を取得
        long start = new File(lockFile).lastModified();
        
        // 経過時間を秒で取得
        tv = (now - start) / 1000;
        
        return tv;
    }
}

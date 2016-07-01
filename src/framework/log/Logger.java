package framework.log;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import framework.form.ArgForm;
import framework.mail.MailSender;
import framework.util.UtilConst;
import framework.util.UtilData;
import framework.util.UtilDate;
import framework.util.UtilFile;
import framework.util.UtilMisc;

/**
 * ロガー
 *
 */
public class Logger{
    private static final String LOG_PROC         = "_PROC.";
    private static final String LOG_ERR          = "_ERR.";

    private static final String LOG_LEVEL        = "log_level";
    private static final String LEVEL_DEBUG      = "debug";
    private static final String MAIL_SEND        = "mail_send";

    private String procLogFile;
    private String errorLogFile;
    private HashMap<String,String> confMap;
    private ArgForm form = null;
    private String appName;
    private StringBuilder errorBuffer = new StringBuilder();

    private String proc_table = null;
    private String proc_column = null;
    private long proc_rec = 0;
    private long proc_count = 0;
    private String last_sql = null;

    private long start_tv = 0;
    private long proc_id = 0;

    public Logger(String appName, HashMap<String,String> confMap){
        String sysDate = UtilData.getSystemDate(UtilData.FORMAT_DATE);

        String logDir = new StringBuilder(UtilConst.DIR_LOG).append(sysDate).append("/").toString();
        if(UtilFile.isDir(logDir) == false) {
            UtilFile.mkdir(logDir);
            UtilFile.chmod(logDir, 777);
        }

        procLogFile = new StringBuilder(logDir).append(appName).append(LOG_PROC).append(sysDate).toString();
        errorLogFile = new StringBuilder(logDir).append(appName).append(LOG_ERR).append(sysDate).toString();

        this.appName = appName;
        this.confMap = confMap;

        this.start_tv = getTv();
        this.proc_id = this.start_tv % 1000000 * 100 + (int)Math.floor(Math.random() * 100);
    }

    public String getAppName() {
        return appName;
    }

    public void setTable(String table){
        this.proc_table = table;
        this.proc_count = 0;
    }
    public void setRec(String column, long rec){
        this.proc_column = column;
        this.proc_rec = rec;
        this.proc_count++;
    }
    public void setLastSql(String SQL){
        this.last_sql = SQL;
    }
    public String getLastSql(){
        return this.last_sql;
    }
    public void setForm(ArgForm form){
        this.form = form;
    }

    /**
     * エラーログ処理
     * @param text
     */
    public void writeErrorLog(Exception e){
        StringBuilder msg = new StringBuilder();

        if(proc_table != null) msg.append("Table: ").append(proc_table).append("\n");
        if(proc_column != null)
            msg.append("Rec: ").append(proc_column).append(" = ").append(proc_rec)
                .append(" (").append(this.proc_count).append("件目) \n");

        if (e instanceof SQLException) {
            if(last_sql != null) msg.append("Sql: ").append(last_sql).append("\n");

            SQLException se = (SQLException) e;
            msg.append(se.toString());
            msg.append("\n");
            msg.append("ErrorCode: ");
            msg.append(se.getErrorCode());
            msg.append("\n");
            msg.append("SQLState: ");
            msg.append(se.getSQLState());
        }
        else{
            msg.append(e.getClass().getName()).append(" : ").append(e.getMessage());
            msg.append("\n");
            msg.append(e.getClass().getName()).append(" (Cause) : ").append(e.getCause());
        }
        msg.append("\n");

        StackTraceElement[] trace = e.getStackTrace();
        for(int i = 0; i < trace.length; i++) {
            msg.append("    at ").append(trace[i].toString()).append("\n");
        }

        writeErrorLog(msg.toString());
    }
    public void writeMemoryErrorLog(){
        writeErrorLog("メモリエラーが発生しました！\n");

        Runtime rt = Runtime.getRuntime();
        long free = rt.freeMemory();
        long total = rt.totalMemory();
        long max = rt.maxMemory();
        rt.gc();
        StringBuilder msg = new StringBuilder();
        msg.append("  totalMemory = ").append(total).append("\n");
        msg.append("  freeMemory = ").append(free).append("\n");
        msg.append("  maxMemory = ").append(max).append("\n");
        msg.append("\n");

        // いったんここまでで出力しておく
        writeErrorLogAppend(msg.toString());

        msg = new StringBuilder();

        try{
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(baos);
            new Throwable().printStackTrace(ps);
            ps.close();
            baos.close();
            msg.append(baos.toString()).append("\n");

        } catch(Exception e) {

        }

        if(proc_table != null) msg.append("Table: ").append(proc_table).append("\n");
        if(proc_column != null)
            msg.append("Rec: ").append(proc_column).append(" = ").append(proc_rec)
                .append(" (").append(this.proc_count).append("件目) \n");

        appendFormValues(msg);
        writeErrorLogAppend(msg.toString());
    }
    private void appendFormValues(StringBuilder msg){
        if(msg == null || this.form == null) return;
        HashMap<String, String> argMap = this.form.getArgMap();
        if(argMap == null) return;

        msg.append("\n");
        msg.append("Arguments-------------------\n");
        Iterator<String> itr = argMap.keySet().iterator();
        while(itr.hasNext()){
            String key = (String)itr.next();
            Object obj = argMap.get(key);
            String value = (obj == null ? null : obj.toString());

            msg.append(key).append(" = ").append(value).append("\n");
        }
        msg.append("\n");
    }
    public void writeErrorLog(String msg){
        writeErrorLog(msg, true);
    }
    public void writeErrorLog(String msg, boolean appendsForm){
        StringBuilder text = new StringBuilder()
            .append(getSystemTime()).append("-------------------\n")
            .append(msg).append("\n");

        if(appendsForm) appendFormValues(text);
        appendLog(this.errorLogFile, text.toString());

        // エラーメール送信対応
        if("true".equals(this.confMap.get(MAIL_SEND))){
            if(this.errorBuffer.length() == 0){
                // 最初なら、ヘッダ部分を作成
                this.errorBuffer
                    .append("[")
                    .append(this.appName)
                    .append("] にてシステムエラーが発生しました\n")
                    .append("\n")
                    ;

                try {
                    InetAddress myHost = InetAddress.getLocalHost();
                    URL whatismyip = new URL("http://checkip.amazonaws.com");
                    BufferedReader in = new BufferedReader(new InputStreamReader(
                                                           whatismyip.openStream()));
                    String ip = in.readLine();
                    this.errorBuffer.append("Machine Name:" + myHost.getHostName() + ", IP Address:" + ip + "\n\n");
                }catch(Exception e ){}

                this.errorBuffer.append("===== エラー内容 =====\n\n");
            }
            this.errorBuffer.append(text);
        }
    }
    public void writeErrorLogAppend(String text){
        appendLog(this.errorLogFile, text);
        if("true".equals(this.confMap.get(MAIL_SEND))){
            this.errorBuffer.append(text);
        }
    }

    /**
     * 送信すべきエラーがあれば、送信する
     */
    public void sendErrorMail(){
        if(this.errorBuffer.length() == 0) return;

        StringBuilder title = new StringBuilder();
        title.append(" [").append(getHostName()).append("] ");

        // エラーメール送信
        MailSender sender = new MailSender(this.confMap);
        sender.setTitle(title.toString());
        sender.setText(this.errorBuffer.toString());
        sender.send();
    }

    /**
     * サーバーの名前を取得
     */
    private static String getHostName(){
        String name = "unknown";
        try {
            InetAddress host = InetAddress.getLocalHost();
            name = host.getHostName();
            String[] nodes = name.split("\\.");
            name = nodes[0];

        } catch(Exception e) {}
        return name;
    }

    /**
     * 処理ログ
     */
    public void writeProcLog(String msg){
        String text = new StringBuilder()
            .append(getSystemTime())
            .append("[").append(this.proc_id).append("] ")
            .append(msg)
            .toString();
        appendLog(this.procLogFile, text);
    }

    /**
     * デバッグログ
     */
    public void writeDebugLog(String msg){
        ArrayList<String> level = UtilMisc.splitToList((String)this.confMap.get(LOG_LEVEL), ",");
        if(level.contains(LEVEL_DEBUG)){
            appendLog(this.procLogFile, msg);
        }
    }

    /**
     * リソース監視ログ
     */
    public void writeMonitorLog(){
        long tv = getTv();
        if(tv == 0 || this.start_tv == 0) return;

        // 5秒を超えない処理だったら、監視するまでもない
        if(tv - this.start_tv < 5) {
            this.start_tv = tv;
            return;
        }

        // 所要時間
        long sec = tv - this.start_tv;
        long min = sec / 60;
        sec %= 60;
        String proc_time = new StringBuilder().append(min).append(":").append(sec < 10 ? "0" : "").append(sec).toString();
        this.start_tv = tv;

        // メモリ
        Runtime rt = Runtime.getRuntime();
        rt.gc();
        long free = rt.freeMemory();
        long total = rt.totalMemory();
        long max = rt.maxMemory();
        free += max - total;
        String text = new StringBuilder()
            .append(" free memory: ")
            .append(free * 100 / max).append("% = ")
            .append(free).append(" / ").append(max)
            .append(" [proc time = ").append(proc_time).append("]")
            .toString();
        this.writeProcLog(text);

        // 空き容量が不足してきたら、エラーメール対象とする
        if(free * 100 / max < 20) {
            text = new StringBuilder()
                .append("空きメモリが少なくなってきています。\n")
                .append("注意してください。\n")
                .append(text)
                .append("\n")
                .toString();
            this.writeErrorLog(text);
        }
    }

    /**
     * 時刻を求める
     */
    private static long getTv(){
        long tv = 0;
        try{
            tv = UtilDate.getTv();
        } catch(Exception e) {
        }
        return tv;
    }

    /**
     * ログファイルに書き込む処理
     */
    private void appendLog(String fileName, String text){
        UtilFile.writeTextFile(fileName, text + "\n", UtilFile.ENCODE_UTF8, true);
    }

    /**
     * システム日時を取得する
     */
    private String getSystemTime(){
        return UtilData.getSystemDate("yyyy/MM/dd HH:mm:ss ");
    }

    public HashMap<String,String> getConfMap() { return this.confMap; }
}
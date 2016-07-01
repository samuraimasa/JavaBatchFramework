package framework.jdbc.dao;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import framework.log.Logger;

/**
 * Mysql専用のDBマネージャー
 *
 */
public class DbManager{
    private String db_server     = null;
    private String db_schema     = null;
    private String db_user       = null;
    private String db_password   = null;
    private boolean low_priority = false;
    private int updated_lines = 0;

    private Logger logger = null;

    private Connection connection = null;
    private Statement stmt = null;
    private long last_tv = 0;


    public DbManager(Logger logger){
        this.logger = logger;
    }

    public long getLastTv() { return this.last_tv; }

    public void changeSchema(String schema) throws Exception {
        if(schema == null) return;
        if(schema.equals(this.db_schema)) return;
        this.executeUpdate("use " + schema);
        this.db_schema = schema;
    }

    public void finalize() {
        this.closeConnection();
    }

    /**
     * 接続
     * @param db_server
     * @param db_schema
     * @param db_user
     * @param db_password
     * @throws Exception
     */
    public void connect(String db_server, String db_schema, String db_user, String db_password) throws Exception{
        if(this.connection != null){
            // 既に接続されている場合
            if(this.db_server.equals(db_server) && this.db_schema.equals(db_schema)){
                // 同じ接続先であれば、そのまま
                return;
            }
            // 前の接続をクローズ
            this.closeConnection();
        }

        this.db_server = db_server;
        this.db_schema = db_schema;
        this.db_user = db_user;
        this.db_password = db_password;

        // 接続
        this.connection = this.getConnection();
    }

    /**
     * SELECT文の実行
     */
    public ResultSet executeQuery(String SQL) throws Exception {
        this.logger.writeDebugLog("クエリ:\n    " + SQL);
        this.logger.setLastSql(SQL);

        ResultSet rs = stmt.executeQuery(SQL);

        return rs;
    }

    /**
     * Insert文の実行
     */
    public boolean executeInsert(String SQL) throws Exception {
        this.logger.writeDebugLog("更新SQL:\n    " + SQL);
        this.logger.setLastSql(SQL);

        return this.getStatement().execute(SQL);
    }

    /**
     * UPDATE, DELETE文の実行
     */
    public boolean executeUpdate(String SQL) throws Exception {
        this.logger.writeDebugLog("更新SQL:\n    " + SQL);
        this.logger.setLastSql(SQL);

        this.updated_lines = this.getStatement().executeUpdate(SQL);
        return true;
    }

    /**
     * LOAD DATA LOCAL INFILE 処理
     */
    public boolean executeLocalInfile(String SQL, String data) throws Exception {
        this.logger.writeDebugLog("更新SQL:\n    " + SQL);
        this.logger.setLastSql(SQL);

        boolean autoCommitStat = this.connection.getAutoCommit();

        this.connection.setAutoCommit(false);
        PreparedStatement pstmt = this.connection.prepareStatement(SQL);
        InputStream is = new ByteArrayInputStream(data.getBytes());
        ((com.mysql.jdbc.Statement) pstmt).setLocalInfileInputStream(is);
        pstmt.execute();
        this.connection.commit();

        this.connection.setAutoCommit(autoCommitStat);

        return true;
    }

    public void closeConnection(){
        if(this.connection == null) return;

        try{
            this.connection.close();
        }catch(Exception e){
            this.logger.writeErrorLog(e);
        }

        this.connection = null;
        this.stmt = null;
    }

    /**
     * コネクション取得
     */
    private Connection getConnection() throws Exception {
        // 未接続なら接続する
        if(this.connection == null) {
            createNewConnection();
        }

        return this.connection;
    }
    private Statement getStatement() throws Exception {
        // 未接続なら接続する
        if(this.stmt == null) {
            createNewConnection();
        }

        long now = java.util.Calendar.getInstance().getTimeInMillis() / 1000;
        if(now - this.last_tv > 3 * 60) {
            // 3分の間が開いていれば、refreshする
            refresh();
        }
        this.last_tv = now;

        return this.stmt;
    }

    private void createNewConnection() throws Exception {
        StringBuffer url = new StringBuffer();

        url
        .append("jdbc:mysql://")
        .append(this.db_server)
        .append("/")
        .append(this.db_schema)
        .append("?useUnicode=true&characterEncoding=UTF8&zeroDateTimeBehavior=convertToNull")
        ;

        // データベースへ接続
        DriverManager.setLoginTimeout(10);
        this.connection = DriverManager.getConnection(url.toString(), this.db_user, this.db_password);

        this.stmt = this.connection.createStatement();

        this.last_tv = java.util.Calendar.getInstance().getTimeInMillis() / 1000;
    }

    public boolean isLow_priority() {
        return low_priority;
    }

    public void setLow_priority(boolean low_priority) {
        this.low_priority = low_priority;
    }

    public int getUpdated_lines() {
        return this.updated_lines;
    }

    public void refresh() throws Exception {
        if(this.stmt == null) return;

        // 接続チェック
        try {
            this.stmt.execute("select 1");
        } catch(Exception e) {
            // timeoutエラー
            closeConnection();
            createNewConnection();
        }
    }

    public String getDbServer() {
        return this.db_server;
    }

    public String getDbSchema() {
        return this.db_schema;
    }

    public String getDbUser() {
        return this.db_user;
    }

    public String getDbPassword() {
        return this.db_password;
    }

    public String getLastSql() {
        return this.logger.getLastSql();
    }

    public Logger getLogger() {
        return this.logger;
    }
}
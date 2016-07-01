package framework.jdbc.dao;

import java.sql.ResultSet;
import java.util.ArrayList;

import framework.util.AssocList;
import framework.util.UtilMisc;

/**
 * 必要と思われる機能を集約したDao
 *
 */
public class BaseDao{
    public static final String ORDER_ASC          = "ASC";
    public static final String ORDER_DESC         = "DESC";

    public static final String COMP_EQUAL         = " = ";
    public static final String COMP_LESS_THAN     = " < ";
    public static final String COMP_LESS_EQUAL    = " <= ";
    public static final String COMP_GREATER_THAN  = " > ";
    public static final String COMP_GREATER_EQUAL = " >= ";
    public static final String COMP_NOT_EQUAL     = " <> ";

    public static final String STR_NULL            = "";
    public static final String STR_ZERO            = "0";

    private String table = null;
    private AssocList selectMap = new AssocList();
    private AssocList valueMap  = new AssocList();
    private AssocList realValueMap = new AssocList();
    private AssocList whereList = new AssocList();
    private AssocList groupList = new AssocList();
    private AssocList orderList = new AssocList();
    private ArrayList<AssocList> bulkValueList = new ArrayList<AssocList>();
    private String having = null;
    private String index = null;
    private boolean distinct = false;
    private int limit = 0;

    private int limit_from = 0;
    private int limit_to = 0;

    private DbManager db = null;

    public BaseDao(DbManager db, String table){
        this.db = db;
        this.table = table;
    }

    public AssocList getSelectMap() {
        return this.selectMap;
    }
    public AssocList getRealValueMap() {
        return this.realValueMap;
    }
    public AssocList getValueMap() {
        return this.valueMap;
    }

    protected void setRealValueMap(AssocList realValueMap) {
        this.realValueMap = realValueMap;
    }
    protected void setValueMap(AssocList valueMap) {
        this.valueMap = valueMap;
    }

    /**
     * テーブル名
     */
    public void setTable(String table){
        this.table = table;
    }
    public String getTable() { return this.table; }

    public void setTableAs(String table, String alias){
        if(table.indexOf(" ") != -1) table = "( " + table + " )";
        this.table = table + " AS " + alias;
    }

    public void addTable(String table){
        this.table = this.table + ", " + table;
    }

    /**
     * 値をリセット
     */
    public void reset(){
        if(this.selectMap.size() > 0 ) this.selectMap.clear();
        if(this.valueMap.size() > 0 ) this.valueMap.clear();
        if(this.realValueMap.size() > 0 ) this.realValueMap.clear();
        if(this.whereList.size() > 0 ) this.whereList.clear();
        if(this.groupList.size() > 0 ) this.groupList.clear();
        if(this.orderList.size() > 0 ) this.orderList.clear();

        having = null;
        index = null;
        distinct = false;
        limit = 0;
        limit_from = 0;
        limit_to = 0;
    }

    public void resetBulc(){
        if(this.bulkValueList.size() > 0 ) this.bulkValueList.clear();
    }

    /**
     * DISTINCT
     * @param distinct
     */
    public void setDistinct(boolean distinct){
        this.distinct = distinct;
    }

    /**
     * USE INDEX
     * @param index
     */
    public void setIndex(String index){
        this.index = index;
    }
    public String getIndex(){
        return this.index;
    }

    /**
     * SELECT対象の列を指定
     * @param column
     */
    public void addSelect(String column){
        // 既に選択されていればよい
        if(this.selectMap.containsValue(column)) return;

        // 追加
        this.selectMap.put(Integer.valueOf(this.selectMap.size()), column);
    }
    public void addSelect(String table, String column){
        addSelect(table + "." + column);
    }
    public void addSelectAs(String column, String alias){
        addSelect(column + " AS " + alias);
    }
    public void addSelectSum(String column){
        addSelectAs("sum(" + column + ")", column);
    }
    public void addSelectDate(String column){
        addSelectAs("DATE_FORMAT(" + column + ", '%Y-%m-%d')", column);
    }
    public void addSelectDateTime(String column){
        addSelectAs("DATE_FORMAT(" + column + ", '%Y-%m-%d %H:%i:%s')", column);
    }

    public String concat(String[] arr) {
        if(arr == null || arr.length == 0) return STR_NULL;
        StringBuilder buf = new StringBuilder();
        buf.append("(");
        for(int i = 0; i < arr.length; i++) {
            if(i > 0) buf.append(",");
            buf.append(arr[i]);
        }
        buf.append(")");
        return buf.toString();
    }

    public String adddate(String date, int add) {
        if(add == 0) return date;
        return new StringBuilder().append("adddate(").append(date).append(", ").append(add).append(")").toString();
    }

    public String dateIsNull(String col) {
        return "(" + col + " is null OR " + col + " = 0)";
    }

    /**
     * ''で囲む
     * @param value
     * @return
     */
    public static String quoteString(String value) {
        if(value == null) return "''";
        value = UtilMisc.replaceAll(value, "\\", "\\\\");
        value = UtilMisc.replaceAll(value, "'", "\\'");
        return "'" + value + "'";
    }

    /**
     * WHEREを追加
     * @param key
     * @param comp
     * @param value
     */
    public void addWhereStr(String key, String comp, String value){
        addWhere(key, comp, quoteString(value));
    }
    public void addWhere(String key, String comp, String value){
        addWhere(key + comp + value);
    }
    public void addWhere(String key, String comp, long value){
        addWhere(key + comp + value);
    }
    public void addWhereStr(String key, String value){
        addWhereStr(key, COMP_EQUAL, value);
    }
    public void addWhereStrBinary(String key, String value){
        addWhere(key, COMP_EQUAL, "binary " + quoteString(value));
    }
    public void addWhere(String key, String value){
        addWhere(key, COMP_EQUAL, value);
    }
    public void addWhere(String key, long value){
        addWhere(key, COMP_EQUAL, value);
    }
    public void addWhereBetweenStr(String key, String from, String to){
        addWhereBetween(key, "'" + from + "'", "'" + to + "'");
    }
    public void addWhereBetween(String key, String from, String to){
        addWhere(key + " BETWEEN " + from + " AND " + to);
    }
    public void addWhereIn(String key, String valueIn){
        addWhereIn(key, valueIn, null);
    }
    public void addWhereIn(String key, String valueIn, String whenEmpty){
        if(UtilMisc.isEmpty(valueIn)) valueIn = whenEmpty;
        if(UtilMisc.isEmpty(valueIn)) return;
        this.whereList.add(key + " IN (" + valueIn + ")");
    }
    public void addWhere(String where){
        this.whereList.add(where);
    }
    public void addWhereTableJoin(String table1, String col1, String table2, String col2){
        addWhere(table1 + "." + col1 + " = " + table2 + "." + col2);
    }

    public void addWhereNotIn(String key, String valueIn){
        if(UtilMisc.isEmpty(valueIn)) return;
        this.whereList.add(key + " NOT IN (" + valueIn + ")");
    }

    /**
     * GROUP BY
     * @param column
     */
    public void addGroupBy(String column){
        this.groupList.add(column);
    }

    public void addHavingCount(String comp, String count){
        this.having = comp + count;
    }

    /**
     * UPDATE,INSERT用に値を設定
     * @param key
     * @param value
     */
    public void addValue(String key, String value){
        this.valueMap.put(key, (UtilMisc.isEmpty(value) ? "NULL" : value));
        this.realValueMap.put(key, value);
    }
    public void addValue(String key, long value){
        addValue(key, (value == 0 ? STR_ZERO : Long.toString(value)));
    }
    public void addValue(String key, double value){
        addValue(key, (value == 0 ? STR_ZERO : Double.toString(value)));
    }

    public void addValueStr(String key, String value){
        addValue(key, quoteString(value));
        this.realValueMap.put(key, value);
    }

    public void addValueStrValid(String key, String value){
        addValueStr(key, UtilMisc.validateString(value));
    }

    public String getValue(String key){
        return (String)this.realValueMap.get(key);
    }
    public int getValueInt(String key){
        return UtilMisc.toInt((String)this.realValueMap.get(key));
    }

    /*
     * Bulk Insert用のデータを設定
     * @param addValueList
     */
    public void addBulkValue (AssocList addValueList, String key, int value) {
        addValueList.put(key, value);
    }

    public void addBulkValueStr (AssocList addValueList, String key, String value) {
        addValueList.put(key, quoteString(value));
    }

    public void addBulkValueList (AssocList addValueList) {
        bulkValueList.add(addValueList);
    }

    public AssocList getBulkValueList () {
        AssocList bulkValueList = new AssocList();
        for (int count = 0; count < this.bulkValueList.size(); count++) {
            AssocList tmpBulkValueList = this.bulkValueList.get(count);
            bulkValueList.put(count, tmpBulkValueList);
        }
        return bulkValueList;
    }

    public boolean isSetBulValue () {
        return (bulkValueList.size() != 0);
    }

    /**
     * ORDER BY用の列を指定
     * @param column
     */
    public void addOrder(String column){
        this.orderList.add(column);
    }
    public void addOrder(String column, String order){
        addOrder(column + " " + order);
    }

    public void setLimit(int limit){
        this.limit = limit;
    }

    public void setLimit(int limit_from, int limit_to){
        this.limit_from = limit_from;
        this.limit_to = limit_to;
    }

    /**
     * SELECT
     */
    public ResultSet doSelect() throws Exception{
        return doSelect(this.getWhere(), this.getOrderBy());
    }
    public ResultSet doSelect(String where, String orderBy) throws Exception{
        String sql = getSelectSql(where, orderBy);
        return this.db.executeQuery(sql);
    }

    /**
     * INSERT文
     */
    public void doInsert() throws Exception{
        StringBuffer sql = new StringBuffer();

        // INSERT INTO table
        sql.append("INSERT ");
        if(this.db.isLow_priority()) sql.append("LOW_PRIORITY ");
        sql.append("INTO ").append(this.table);

        // SET col1=val1, col2=val2
        sql.append(this.getSetSql());

        this.db.executeUpdate(sql.toString());
    }

    /**
     * INSERTで発番された値を取得
     */
    public long getLastInsertId() throws Exception{
        String sql = "SELECT LAST_INSERT_ID();";
        ResultSet ds = this.db.executeQuery(sql);
        long lId = 0;
        if(ds.first()) lId = ds.getLong(1);
        ds.close();
        return lId;
    }

    /*
     * BULK INSERT文
     */
    public void doBulkInsert () throws Exception {
        StringBuffer sql = new StringBuffer();

        // INSERT INTO table
        sql.append("INSERT ");
        if(this.db.isLow_priority()) sql.append("LOW_PRIORITY ");
        sql.append("INTO ").append(this.table);
        sql.append(" (").append(this.getSetBulkInsertColumns()).append(") ");
        sql.append("VALUES ");

        // (val1, val2, val3...),
        // (val1, val2, val3...),
        sql.append(this.getSetBulkInsertValues());

        this.db.executeInsert(sql.toString());
    }

    /**
     * UPDATE文
     */
    public void doUpdate() throws Exception{
        StringBuffer sql = new StringBuffer();

        // UPDATE table
        sql.append("UPDATE ");
        if(this.db.isLow_priority()) sql.append("LOW_PRIORITY ");
        sql.append(this.table);

        // SET col1=val1, col2=val2
        sql.append(this.getSetSql());

        // WHERE col1=val1, col2=val2
        sql.append(this.getWhere());

        this.db.executeUpdate(sql.toString());
    }

    /**
     * キーがDBにあればUPDATE、なければINSERT
     */
    public void doInsertOrUpdate() throws Exception{
        doInsertOrUpdate(null);
    }
    public void doInsertOrUpdate(String idCol) throws Exception{
        StringBuffer sql = new StringBuffer();

        // SELECTしてみる
        sql.append("SELECT ").append(idCol == null ? "1" : idCol);
        sql.append(" FROM ").append(this.table);
        // USE INDEX
        if(getIndex() != null && getIndex() != ""){
            sql.append(" USE INDEX(").append(getIndex()).append(")");
        }
        sql.append(this.getWhere());
        sql.append(" LIMIT 1");

        ResultSet ds = this.db.executeQuery(sql.toString());

        if(ds.getFetchSize() > 0) {
            // レコードが存在 → Update
            if(idCol != null) {
                ds.first();
                long id = ds.getLong(idCol);
                if(id > 0) this.addWhere(idCol, id);
            }
            doUpdate();

        } else {
            // 存在しない → Insert

            // キー項目をInsertの値に含める
            addValuesFromWhere();

            doInsert();
        }
    }
    public void addValuesFromWhere() {
        for(int i = 0; i < this.whereList.size(); i++) {
            String[] keyVal = ((String)this.whereList.get(i)).split(COMP_EQUAL);
            if(keyVal.length != 2) continue;
            this.addValue(keyVal[0], keyVal[1]);
        }
    }

    /**
     * DELETE文
     */
    public void doDelete() throws Exception{
        StringBuffer sql = new StringBuffer();

        // DELETE FROM table
        sql.append("DELETE ");
        if(this.db.isLow_priority()) sql.append("LOW_PRIORITY ");
        sql.append("FROM ").append(this.table);

        // WHERE col1=val1, col2=val2
        sql.append(this.getWhere());

        this.db.executeUpdate(sql.toString());
    }

    /**
     * SELECT文作成
     * @param where
     * @param orderBy
     * @return
     */
    public String getSelectSql(String where, String orderBy){
        StringBuffer sql = new StringBuffer();

        // SELECT col1,col2,col3
        sql.append("SELECT ");
        if(this.distinct) sql.append(" DISTINCT ");
        if(this.selectMap.size() == 0){
            sql.append("*");
        } else {
            for(int i = 0; i < this.selectMap.size(); i++){
                String column = (String)this.selectMap.get(new Integer(i));
                if(i > 0) sql.append(", ");
                sql.append(column);
            }
        }

        // FROM table
        sql.append(" FROM ").append(this.table);

        // USE INDEX
        if(getIndex() != null && getIndex() != ""){
            sql.append(" USE INDEX(").append(getIndex()).append(")");
        }

        // WHERE col1=val1
        sql.append(where);

        // GROUP BY col1,col2
        if(this.groupList.size() > 0){
            sql.append(" GROUP BY ").append(UtilMisc.toCsv(this.groupList));
        }

        // HAVING
        if(this.having != null){
            sql.append(" HAVING COUNT(*) ").append(this.having);
        }

        // ORDER BY col1 ASC,col2 DESC
        sql.append(orderBy);

        if(this.limit > 0) sql.append(" LIMIT ").append(this.limit);
        else if(this.limit_from >= 0 && this.limit_to > 0){
            sql.append(" LIMIT ").append(this.limit_from).append(", ").append(this.limit_to);
        }

        return sql.toString();
    }

    /**
     * SELECT文作成
     */
    public String getSelectSql(){
        return this.getSelectSql(this.getWhere(), this.getOrderBy());
    }

    /**
     * SET col1 = val1, col2 = val2
     */
    private String getSetSql(){
        StringBuffer sql = new StringBuffer();
        this.valueMap.beforeFirst();
        while(this.valueMap.next()){
            String key   = this.valueMap.getKeyString();
            String value = this.valueMap.getValueString();

            if(sql.length() == 0){
                sql.append(" SET ");
            } else {
                sql.append(", ");
            }
            sql.append(key).append(" = ").append(value);
        }

        return sql.toString();
    }

    /*
     * col1, col2, col3....
     */
    public String getSetBulkInsertColumns() {

        StringBuffer columns = new StringBuffer();
        AssocList bulkValueList = this.bulkValueList.get(0);

        for (Object[] entry : bulkValueList.getEntries()) {
            if (!UtilMisc.isEmpty(columns.toString())) columns.append(", ");
            columns.append(entry[0]);
        }

        return columns.toString();
    }

    /*
     * (val1, val2, val3),
     * (val1, val2, val3),
     */
    public String getSetBulkInsertValues() {

        StringBuffer columns = new StringBuffer();

        for (int count = 0; count < this.bulkValueList.size(); count++) {
            AssocList bulkValueList = this.bulkValueList.get(count);

            if (count != 0) columns.append(", ");
            columns.append("(");

            for (Object[] entry : bulkValueList.getEntries()) {
                if (!columns.toString().endsWith("(")) columns.append(", ");
                columns.append(entry[1]);
            }

            columns.append(") ");
        }

        return columns.toString();
    }

    /**
     * WHERE col1 = val1 AND col2 = val2
     */
    public String getWhere(){
        if(this.whereList.isEmpty()) return STR_NULL;
        return " WHERE " + getWhereList();
    }
    public String getWhereList(){
        if(this.whereList.isEmpty()) return STR_NULL;
        return UtilMisc.toString(this.whereList, " AND ");
    }

    /**
     * ORDER BY col1,col2 DESC
     */
    public String getOrderBy(){
        if(this.orderList.isEmpty()) return STR_NULL;
        return " ORDER BY " + UtilMisc.toCsv(this.orderList);
    }

    /**
     * 更新値がセットされたかどうか
     */
    public boolean hasSetValue() {
        return (this.valueMap.size() > 0);
    }
}

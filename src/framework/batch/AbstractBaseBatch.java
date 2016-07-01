package framework.batch;

import java.util.HashMap;

import framework.jdbc.dao.DbManager;
import framework.form.ArgForm;
import framework.log.Logger;
import framework.util.MutexManager;
import framework.util.UtilArg;
import framework.util.UtilConfig;
import framework.util.UtilConst;
import framework.util.UtilMisc;

/**
 * バッチの共通部分
 * @author masa
 *
 */
public abstract class AbstractBaseBatch {

    protected Logger logger = null;

    @SuppressWarnings("unchecked")
    protected void mainProc(String[] args) {
        HashMap<String,String> argMap = null;
        HashMap<String,String> confMap = null;
        ArgForm form = new ArgForm();

        // 引数を取得
        argMap = UtilArg.getArgMap(args);

        // 引数チェック
        if(checkArg(argMap) == false) {
            showUsage((String)argMap.get(UtilArg.ARG_ERROR));
            return;
        }

        // 設定ファイル読込
        confMap = UtilConfig.getConfigMap();

        form.setArgMap((HashMap<String,String>)argMap.clone());
        form.setConfMap(confMap);

        // ログ
        logger = getLogger(confMap);
        logger.writeProcLog("PROC START " + logger.getAppName() + " " + UtilMisc.toString(args, " ") );
        logger.setForm(form);

        try{
            // DB接続
            DbManager dbMgr = new DbManager(logger);
            dbMgr.connect(
                    form.getConf(UtilConfig.DB_SERVER),
                    form.getConf(UtilConfig.DB_SCHEMA),
                    form.getConf(UtilConfig.DB_USER),
                    form.getConf(UtilConfig.DB_PASSWORD));

            /** メイン処理 */
            try{
                dbMgr.refresh();
                doLogic(dbMgr, form, logger);
            }catch(OutOfMemoryError e){
                logger.writeMemoryErrorLog();
            }catch(Exception e){
                logger.writeErrorLog(e);
            }

            // DBクローズ
            dbMgr.closeConnection();
        }catch(Exception e){
            logger.writeErrorLog(e);
        }

        // 終了
        logger.writeProcLog("PROC END " + logger.getAppName() + " " + UtilMisc.toString(args, " ") + "\n");
        logger.sendErrorMail();
    }

    /**
     * 二重起動防止
     * @param args
     * @param appName
     * @param alertMinute
     */ 
    protected void mainProcMutex(String[] args, String appName, int alertMinute) {
        StringBuilder path = new StringBuilder();
        path.append(UtilConst.DIR_VAR).append(appName);
        MutexManager mutex = new MutexManager();
        if(!mutex.getMutex(path.toString(), MutexManager.MODE_NO_WAIT)) {
            long lockMinute = mutex.getLockedTime() / 60;
            if(lockMinute > alertMinute) {
                // ロック状態が異常に続いていたら、アラート
                Logger _logger = new Logger(appName, UtilConfig.getConfigMap());
                _logger.writeErrorLog(appName + " is skipped : " + appName + ".lock existed for " + lockMinute + " minutes");
                _logger.sendErrorMail();
            }
            return;
        }

        try{
            // 処理実行
            this.mainProc(args);
        } catch(Exception e) {
            mutex.releaseMutex();
            return;
        }
        mutex.releaseMutex();
    }

    /**
     * ログを返す
     */
    protected abstract Logger getLogger(HashMap<String,String> confMap);

    /**
     * メインロジック部分
     */
    protected abstract void doLogic(DbManager dbMgr, ArgForm argForm, Logger logger) throws Exception;

    /**
     * 使用方法を説明して終了
     */
    protected abstract void showUsage(String errorMsg);

    /**
     * 引数チェック
     */
    protected boolean checkArg(HashMap<String,String> argMap){
        // -help
        if(argMap.containsKey(UtilArg.ARG_HELP)) return false;

        // error
        if(argMap.containsKey(UtilArg.ARG_ERROR)) return false;

        return true;
    }
}
package main;

import java.util.HashMap;

import framework.batch.AbstractBaseBatch;
import framework.form.ArgForm;
import framework.jdbc.dao.DbManager;
import framework.log.Logger;
import logic.SampleLogic;

public class Sample extends AbstractBaseBatch{
    private static final String APP_NAME = "GoogleAnalyticsからデータを取得するサンプル";
    
    public static void main(String... args) {
        Sample batch = new Sample();
        batch.mainProcMutex(args, APP_NAME, 180);
    }
    
    /**
     * ログを返す
     */
    @Override
    protected Logger getLogger(HashMap<String,String> confMap){
        return new Logger(APP_NAME, confMap);
    }
    
    /**
     * ロジック部分
     */
    @Override
    protected void doLogic(DbManager dbMgr, ArgForm form, Logger logger) throws Exception {
        SampleLogic doLogic = new SampleLogic(logger, dbMgr, form);
        doLogic.run();
    }
    
    /**
     * 使用方法を説明して終了
     */
    @Override
    protected void showUsage(String errorMsg){
        StringBuilder usage = new StringBuilder();
        
        if(errorMsg != null) usage.append(errorMsg).append("\n\n");
        
        usage
        .append("usage : Sample -test [test] \n");
        
        System.out.println(usage.toString());
    }
}

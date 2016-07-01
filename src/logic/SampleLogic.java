package logic;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;

import dao.GoogleAnalyticsInfoDao;
import dao.entity.GoogleAnalyticsInfo;
import framework.form.ArgForm;
import framework.google.analytics.AnalyticsCore;
import framework.google.analytics.AnalyticsData;
import framework.jdbc.dao.DbManager;
import framework.log.Logger;
import framework.util.AssocList;
import framework.util.UtilConfig;
import framework.util.UtilMisc;

/**
 * 実処理
 *
 */
public class SampleLogic{
    private Logger logger;
    private DbManager dbMgr;
    private ArgForm form;
    
    public SampleLogic(Logger logger, DbManager dbMgr, ArgForm form){
        this.logger = logger;
        this.dbMgr = dbMgr;
        this.form = form;
    }
    
    public void run() throws Exception{
        GoogleAnalyticsInfoDao gaDao = new GoogleAnalyticsInfoDao(dbMgr);
        GoogleAnalyticsInfo ga = null;
        try{
            gaDao.reset();
            gaDao.addWhere(GoogleAnalyticsInfo.COL_ID, form.getInt("infoId"));
            ArrayList<GoogleAnalyticsInfo> gaList = gaDao.select();
            if(UtilMisc.isEmpty(gaList)) return;
            ga = gaList.get(0);
            
            AnalyticsCore analyticsCore = new AnalyticsCore(logger, getCredential(ga));
            AnalyticsData analyticsData = analyticsCore.getAnalyticsData();
            analyticsData.setViewId(ga.viewId);
            analyticsData.setStartDate("2016-07-01");
            analyticsData.setEndDate("2016-07-31");
            analyticsData.setMetrics("ga:sessions");
            
            LinkedList<Map<String,String>> gaDataList = null;
            gaDataList = analyticsData.getData();

            System.out.println(gaDataList);
            
        }catch(Exception e){
            logger.writeErrorLog(e);
        }
        
    }

    private AssocList getCredential(GoogleAnalyticsInfo ga){
        AssocList credential = new AssocList();
        credential.put(AnalyticsCore.APP_NAME, form.getConf(UtilConfig.APP_NAME));
        credential.put(AnalyticsCore.CLIENT_ID, ga.clientId);
        credential.put(AnalyticsCore.CLIENT_SECRET, ga.clientSecret);
        credential.put(AnalyticsCore.ACCESS_TOKEN, ga.accessToken);
        credential.put(AnalyticsCore.REFRESH_TOKEN, ga.refreshToken);
        
        return credential;
    }
}

package dao;

import java.sql.ResultSet;
import java.util.ArrayList;

import framework.jdbc.dao.BaseDao;
import framework.jdbc.dao.DbManager;

import dao.entity.GoogleAnalyticsInfo;

public class GoogleAnalyticsInfoDao extends BaseDao{
    
    public GoogleAnalyticsInfoDao(DbManager dbMgr){
        super(dbMgr, GoogleAnalyticsInfo.TABLE_NAME);
    }
    
    public ArrayList<GoogleAnalyticsInfo> select() throws Exception{
        ArrayList<GoogleAnalyticsInfo> gaList = new ArrayList<>();
        
        ResultSet rs = doSelect();
        
        GoogleAnalyticsInfo ga = null;
        rs.beforeFirst();
        while(rs.next()){
            ga = new GoogleAnalyticsInfo();
            ga.id = rs.getInt(GoogleAnalyticsInfo.COL_ID);
            ga.accountId = rs.getString(GoogleAnalyticsInfo.COL_ACCOUNT_ID);
            ga.webpropertyId = rs.getString(GoogleAnalyticsInfo.COL_WEBPROPERTY_ID);
            ga.viewId = rs.getString(GoogleAnalyticsInfo.COL_VIEW_ID);
            ga.clientId = rs.getString(GoogleAnalyticsInfo.COL_CLIENT_ID);
            ga.clientSecret = rs.getString(GoogleAnalyticsInfo.COL_CLIENT_SECRET);
            ga.accessToken = rs.getString(GoogleAnalyticsInfo.COL_ACCESS_TOKEN);
            ga.refreshToken = rs.getString(GoogleAnalyticsInfo.COL_REFRESH_TOKEN);
            ga.tokenType = rs.getString(GoogleAnalyticsInfo.COL_TOKEN_TYPE);

            gaList.add(ga);
        }
        
        rs.close();
        
        return gaList;
    }
}

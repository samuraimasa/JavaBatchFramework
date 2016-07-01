package dao.entity;

public class GoogleAnalyticsInfo{
    public static final String TABLE_NAME = "google_analytics_info";
    
    public static final String COL_ID = "id";
    public static final String COL_ACCOUNT_ID = "account_id";
    public static final String COL_WEBPROPERTY_ID = "webproperty_id";
    public static final String COL_VIEW_ID = "view_id";
    public static final String COL_CLIENT_ID = "client_id";
    public static final String COL_CLIENT_SECRET = "client_secret";
    public static final String COL_ACCESS_TOKEN = "access_token";
    public static final String COL_REFRESH_TOKEN = "refresh_token";
    public static final String COL_TOKEN_TYPE = "token_type";
    
    public int id;
    public String accountId;
    public String webpropertyId;
    public String viewId;
    public String clientId;
    public String clientSecret;
    public String accessToken;
    public String refreshToken;
    public String tokenType;
    
    @Override
    public String toString(){
        return  "id       : " + id + "\n" +
                "viewId   : " + viewId + "\n"
                ; 
    }
}

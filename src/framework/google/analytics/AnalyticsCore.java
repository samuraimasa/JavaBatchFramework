package framework.google.analytics;

import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;

import com.google.api.services.analytics.Analytics;

import framework.log.Logger;
import framework.util.AssocList;
import framework.util.UtilMisc;

/**
 * client_id, client_secret, refresh_token, access_tokenを用いてGoogleAnalyticsへデータの取得を行うクラス
 * @author masa
 *
 */
public class AnalyticsCore {
    public static final String APP_NAME = "appName";
    public static final String CLIENT_ID = "id";
    public static final String CLIENT_SECRET = "secret";
    public static final String REFRESH_TOKEN = "refresh";
    public static final String ACCESS_TOKEN = "access";
    public static final String EXPIRES_IN = "expires";
    public static final String TOKEN_TYPE = "type";
    public static final String AUTH_URI = "auth_uri";
    public static final String TOKEN_URI = "token_uri";
    
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    
    private Logger logger;
    private Analytics analytics;
    
    private String appName;
    private String clientId;
    private String clientSecret;
    private String refreshToken;
    private String accessToken;
    private long expiresIn = 3600;
    private String tokenType = "Bearer";
    private String authUri = "https://accounts.google.com/o/oauth2/auth";
    private String tokenUri = "https://accounts.google.com/o/oauth2/token";
    
    public AnalyticsCore(Logger logger, AssocList list) throws Exception{
        this.logger = logger;
        this.appName = list.getString(APP_NAME);
        
        this.clientId = list.getString(CLIENT_ID);
        this.clientSecret = list.getString(CLIENT_SECRET);
        this.refreshToken = list.getString(REFRESH_TOKEN);
        this.accessToken = list.getString(ACCESS_TOKEN);
        
        if((long)list.getLong(EXPIRES_IN) > 0){
            this.expiresIn = list.getLong(EXPIRES_IN);
        }
        if(!UtilMisc.isEmpty(list.getString(TOKEN_TYPE))){
            this.tokenType = list.getString(TOKEN_TYPE);
        }
        
        if(!UtilMisc.isEmpty(list.getString(AUTH_URI))){
            this.authUri = list.getString(AUTH_URI);
        }
        if(!UtilMisc.isEmpty(list.getString(TOKEN_URI))){
            this.tokenUri = list.getString(TOKEN_URI);
        }
        
        // Analytics 初期化
        this.analytics = initializeAnalytics();
    }
    
    private Analytics initializeAnalytics() throws Exception {
        GoogleClientSecrets.Details web = new GoogleClientSecrets.Details();
        web.setClientId(clientId);
        web.setClientSecret(clientSecret);
        web.setAuthUri(authUri);
        web.setTokenUri(tokenUri);
        
        GoogleClientSecrets clientSecret = new GoogleClientSecrets();
        clientSecret.setWeb(web);
        
        TokenResponse tokenResponse = new TokenResponse();
        tokenResponse.setFactory(JSON_FACTORY);
        tokenResponse.setRefreshToken(refreshToken);
        tokenResponse.setAccessToken(accessToken);
        tokenResponse.setExpiresInSeconds(expiresIn);
        tokenResponse.setTokenType(tokenType);
        
        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        GoogleCredential credential = new GoogleCredential.Builder()
            .setTransport(httpTransport)
            .setJsonFactory(JSON_FACTORY)
            .setClientSecrets(clientSecret)
            .build()
            .setFromTokenResponse(tokenResponse);
    
        return new Analytics.Builder(httpTransport, JSON_FACTORY, credential)
            .setApplicationName(appName).build();
    }
    
    public AnalyticsData getAnalyticsData(){
        return new AnalyticsData(logger, analytics);
    }
}

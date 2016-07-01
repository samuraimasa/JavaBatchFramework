package framework.google.analytics;

import com.google.api.services.analytics.Analytics;
import com.google.api.services.analytics.Analytics.Data.Ga.Get;
import com.google.api.services.analytics.model.GaData;

import framework.log.Logger;
import framework.util.UtilMisc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * GoogleAnalyticsからreportingデータを取得する
 * @author masa
 *
 */
public class AnalyticsData {
    private Logger logger;
    private Analytics analytics;
    
    private String viewId;
    private int startIndex = 1;
    private int maxResults = 10000;
    private String startDate;
    private String endDate;
    private String metrics;
    private String dimensions;
    private String filters;
    private String segments;
    private String sort;
    
    public AnalyticsData(Logger logger, Analytics analytics){
        this.logger = logger;
        this.analytics = analytics;
    }
    
    /**
     * totalsForAllResults → rows のArrayListを作成
     * 使用例 )
     * for(Map<String,String> row : gaDataList){
     *     for(Entry<String, String> entry : row.entrySet()){
     *         System.out.println(entry.getKey() + " : " + entry.getValue());
     *      }
     *      System.out.println();
     *  }
     * @throws Exception
     */
    public LinkedList<Map<String,String>> getData() throws Exception {
        Get get = analytics.data().ga().get("ga:" + viewId, startDate, endDate, metrics);
        if(!UtilMisc.isEmpty(dimensions)){
            get.setDimensions(dimensions);
        }
        if(!UtilMisc.isEmpty(filters)){
            get.setFilters(filters);
        }
        if(!UtilMisc.isEmpty(segments)){
            get.setSegment(segments);
        }
        if(!UtilMisc.isEmpty(sort)){
            get.setSort(sort);
        }
        get.setMaxResults(maxResults);
        
        LinkedList<Map<String, String>> gaDataList = new LinkedList<>();
        ArrayList<String> columnList = null;
        GaData data = null;
        do{
            get.setStartIndex(startIndex);
            
            data = get.execute();
        
            logger.writeDebugLog(data.getQuery().toString());
        
            // 初回のみ
            if(columnList == null){
                // カラム取得
                columnList = getColumnList(data);
                
                // totals取得
                gaDataList.add((Map<String,String>)data.getTotalsForAllResults());
            }else{
                Thread.sleep(1);
            }
            
            // rows取得
            if (columnList != null && data != null && data.getRows() != null && !data.getRows().isEmpty()) {
                for(int i=0; i<data.getRows().size(); i++){
                    HashMap<String,String> row = new HashMap<>();
                    for(int j=0; j<columnList.size(); j++){
                        row.put(columnList.get(j),  data.getRows().get(i).get(j));
                    }
                    gaDataList.add(row);
                }
            }
            
            startIndex += maxResults;
        }while(data.getNextLink() != null);
        
        return gaDataList;
    }
    
    private ArrayList<String> getColumnList(GaData data){
        ArrayList<String> columnList = new ArrayList<String>();
        if (data != null && !data.getColumnHeaders().isEmpty()) {
            for(int i=0; i<data.getColumnHeaders().size(); i++){
                columnList.add(data.getColumnHeaders().get(i).getName());
            }
        }
        
        return columnList;
    }

    public String getViewId() {
        return viewId;
    }

    public void setViewId(String viewId) {
        this.viewId = viewId;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getMetrics() {
        return metrics;
    }

    public void setMetrics(String metrics) {
        this.metrics = metrics;
    }

    public String getDimensions() {
        return dimensions;
    }

    public void setDimensions(String dimensions) {
        this.dimensions = dimensions;
    }

    public String getFilters() {
        return filters;
    }

    public void setFilters(String filters) {
        this.filters = filters;
    }

    public String getSegments() {
        return segments;
    }

    public void setSegments(String segments) {
        this.segments = segments;
    }

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }
    
    public int getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }

    public int getMaxResults() {
        return maxResults;
    }

    public void setMaxResults(int maxResults) {
        this.maxResults = maxResults;
    }
}

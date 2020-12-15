package cn.zup.iot.timerdecision.dao;

import cn.zup.iot.timerdecision.model.ActivityNode;
import cn.zup.iot.timerdecision.model.DataAdress;
import cn.zup.iot.timerdecision.model.DeviceParm;
import cn.zup.iot.timerdecision.model.NodeRule;
import cn.zup.iot.timerdecision.util.JdbcTemplateUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InitTreeDao{

    private JdbcTemplate jdbcTemplateInit = JdbcTemplateUtils.jdbcTemplatePms;

//    public JdbcTemplate getJdbcTemplateInit() {
//        return jdbcTemplateInit;
//    }
//
//    public void setJdbcTemplateInit(JdbcTemplate jdbcTemplateInit) {
//        this.jdbcTemplateInit = jdbcTemplateInit;
//    }

    public HashMap<Integer, ActivityNode>  getTree(int treeId){
        HashMap<Integer, ActivityNode> listAOV = new HashMap<Integer, ActivityNode>();
        StringBuffer sql= new StringBuffer();
        sql.append(" SELECT A.ACTIVITY_ID,A.TREE_ID,A.ACTIVITY_CODE,A.PARENT_COUNT,A.DATA_ADRESS,A.NEXT_ACTIVITY_CODE,A.WARN_SOURCE_ID,B.SUB_NAME AS NODE_TYPE,A.WARN_FLAG from decision_activity A" +
        " LEFT JOIN decision_config B ON A.ACTIVITY_STATE = B.SUB_ID AND B.ID = 2 ");
        sql.append(" WHERE A.TREE_ID =  "+treeId);
        System.out.println(sql);
        listAOV = jdbcTemplateInit.query(sql.toString(), new ResultSetExtractor<HashMap<Integer, ActivityNode>>() {
            public HashMap<Integer, ActivityNode> extractData(ResultSet rs)  throws SQLException, DataAccessException {
                HashMap<Integer, ActivityNode> listAOV1 = new HashMap<Integer, ActivityNode>();
                while(rs.next()) {
                    ActivityNode row = new ActivityNode();
                    row.setTreeId(rs.getInt("TREE_ID"));
                    row.setnAcitivityID(rs.getInt("ACTIVITY_ID"));
                    row.setnActivityCode(rs.getInt("ACTIVITY_CODE"));
                    row.setnParentCount(rs.getInt("PARENT_COUNT"));
                    row.setInEdges(row.getnParentCount());//入边
                    row.setByActivityNodeType(rs.getString("NODE_TYPE"));
                    row.setStrNextActivityCodes(rs.getString("NEXT_ACTIVITY_CODE"));
                    row.setDataAddress(rs.getString("DATA_ADRESS"));
                    row.setWarnFlag(rs.getInt("WARN_FLAG")!=0);
                    row.setWarnSourceId(rs.getInt("WARN_SOURCE_ID"));
                    StringBuffer sql1= new StringBuffer();
                    sql1.append(" SELECT CONDITIONS,JUDGE_BASIS,GOTO_ACTIVITY FROM decision_activity_rule ");
                    sql1.append(" WHERE  ACTIVITY_ID= "+row.getnAcitivityID());
                    System.out.println(sql1);
                    ArrayList<NodeRule> ruleList = new ArrayList<NodeRule>();
                    ruleList = jdbcTemplateInit.query(sql1.toString(), new ResultSetExtractor<ArrayList<NodeRule>>() {
                        public ArrayList<NodeRule> extractData(ResultSet rs)  throws SQLException, DataAccessException {
                            ArrayList<NodeRule> ruleList1 = new ArrayList<NodeRule>();
                            while (rs.next()){
                                NodeRule rule = new NodeRule();
                                rule.setCONDITIONS(rs.getInt("CONDITIONS"));
                                rule.setJUDGE_VALUE(rs.getString("JUDGE_BASIS"));
                                rule.setGOTO_ACTIVITY(rs.getInt("GOTO_ACTIVITY"));
                                ruleList1.add(rule);
                            }
                            return ruleList1;
                    }});
                    row.setRuleList(ruleList);
                    if (!listAOV1.containsKey(row.getnActivityCode()))
                        listAOV1.put(row.getnActivityCode(), row);
                }
                return listAOV1;
            }});
        return listAOV;
    }
    public List<DeviceParm> getTreeWarnInterval(int warnInterval){
        ArrayList<DeviceParm> warnIntervalList = new ArrayList<DeviceParm>();
        StringBuffer sql= new StringBuffer();
        sql.append(" SELECT TREE_ID,DEVICE_ID,DEVICE_TYPE,BUJIAN_TYPE FROM decision_tree ");
        sql.append(" WHERE WARN_INTERVAL =  "+warnInterval);
        sql.append(" AND TREE_STATE=1 ");
        warnIntervalList = jdbcTemplateInit.query(sql.toString(), new ResultSetExtractor<ArrayList<DeviceParm>>() {
            public ArrayList<DeviceParm> extractData(ResultSet rs) throws SQLException, DataAccessException {
                ArrayList<DeviceParm> warnIntervalList1 = new ArrayList<DeviceParm>();
                while(rs.next()) {
                    DeviceParm deviceParm = new DeviceParm();
                    deviceParm.setTreeId(rs.getInt("TREE_ID"));
                    deviceParm.setDeviceId(rs.getInt("DEVICE_ID"));
                    deviceParm.setDeviceType(rs.getInt("DEVICE_TYPE"));
                    deviceParm.setBujianType(rs.getInt("BUJIAN_TYPE"));
                    warnIntervalList1.add(deviceParm);
                }
                return warnIntervalList1;
        }});
        return warnIntervalList;
    }
    /**
     *
     */
    public Map<String,String> getAddress(){
        StringBuilder strSql = new StringBuilder();
        strSql.append(" select ID,DATA_ADRESS from decision_data_address ");
        Map<String,String> dataAdressMap = jdbcTemplateInit.query(strSql.toString(),
                new ResultSetExtractor<Map<String,String>>(){
                    @Override
                    public  Map<String,String> extractData(ResultSet rs) throws SQLException, DataAccessException {
                        Map<String,String> dataAdressMap = new HashMap<String,String>();
                        while(rs.next()){
                           dataAdressMap.put(String.valueOf(rs.getInt("ID")),rs.getString("DATA_ADRESS"));
                        }
                        return dataAdressMap;
                    }

                });
        return dataAdressMap;
    }
}

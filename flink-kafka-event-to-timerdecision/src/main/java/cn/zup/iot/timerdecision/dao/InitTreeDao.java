package cn.zup.iot.timerdecision.dao;

import cn.zup.iot.timerdecision.model.ActivityNode;
import cn.zup.iot.timerdecision.model.NodeRule;
import cn.zup.iot.timerdecision.util.DataSourceUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

@Component
public class InitTreeDao implements Serializable {
    private JdbcTemplate jdbcTemplateInit;

//    public JdbcTemplate getJdbcTemplateInit() {
//        return jdbcTemplateInit;
//    }
//
    public void setJdbcTemplateInit(JdbcTemplate jdbcTemplateInit) {
        this.jdbcTemplateInit = jdbcTemplateInit;
    }

    public HashMap<Integer, ActivityNode>  getTree(int treeId){
        HashMap<Integer, ActivityNode> listAOV = new HashMap<Integer, ActivityNode>();
        StringBuffer sql= new StringBuffer();
        sql.append(" SELECT A.NODE_ID,A.TREE_ID,A.NODE_CODE,A.PARENT_COUNT,A.DATA_ADRESS,A.NEXT_NODE_CODE,A.WARN_SOURCE_ID,B.SUB_NAME AS NODE_TYPE,A.WARN_FLAG from decision_node A" +
        " LEFT JOIN decision_config B ON A.NODE_TYPE = B.SUB_ID AND B.ID = 2 ");
        sql.append(" WHERE A.TREE_ID =  "+treeId);
        System.out.println(sql);
        listAOV = jdbcTemplateInit.query(sql.toString(), new ResultSetExtractor<HashMap<Integer, ActivityNode>>() {
            public HashMap<Integer, ActivityNode> extractData(ResultSet rs)  throws SQLException, DataAccessException {
                HashMap<Integer, ActivityNode> listAOV1 = new HashMap<Integer, ActivityNode>();
                while(rs.next()) {
                    ActivityNode row = new ActivityNode();
                    row.setTreeId(rs.getInt("TREE_ID"));
                    row.setnAcitivityID(rs.getInt("NODE_ID"));
                    row.setnActivityCode(rs.getInt("NODE_CODE"));
                    row.setnParentCount(rs.getInt("PARENT_COUNT"));
                    row.setInEdges(row.getnParentCount());//入边
                    row.setByActivityNodeType(rs.getString("NODE_TYPE"));
                    row.setStrNextActivityCodes(rs.getString("NEXT_NODE_CODE"));
                    row.setDataAddress(rs.getString("DATA_ADRESS"));
                    row.setWarnFlag(rs.getInt("WARN_FLAG")!=0);
                    row.setWarnSourceId(rs.getInt("WARN_SOURCE_ID"));
                    StringBuffer sql1= new StringBuffer();
                    sql1.append(" SELECT CONDITIONS,JUDGE_VALUE,GOTO_NODE_ID FROM decision_node_rule ");
                    sql1.append(" WHERE TREE_ID =  "+row.getTreeId()+" AND NODE_ID= "+row.getnAcitivityID());
                    System.out.println(sql1);
                    ArrayList<NodeRule> ruleList = new ArrayList<NodeRule>();
                    ruleList = jdbcTemplateInit.query(sql1.toString(), new ResultSetExtractor<ArrayList<NodeRule>>() {
                        public ArrayList<NodeRule> extractData(ResultSet rs)  throws SQLException, DataAccessException {
                            ArrayList<NodeRule> ruleList1 = new ArrayList<NodeRule>();
                            while (rs.next()){
                                NodeRule rule = new NodeRule();
                                rule.setCONDITIONS(rs.getInt("CONDITIONS"));
                                rule.setJUDGE_VALUE(rs.getString("JUDGE_VALUE"));
                                rule.setGOTO_ACTIVITY(rs.getInt("GOTO_NODE_ID"));
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
}

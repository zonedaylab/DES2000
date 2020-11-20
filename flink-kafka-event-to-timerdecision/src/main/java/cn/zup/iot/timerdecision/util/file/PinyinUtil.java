/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.zup.iot.timerdecision.util.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;


/**
 *
 * @author houpeibin
 */
public class PinyinUtil {
    private Map<String, String> pinyinMap;
   
    public PinyinUtil(){
       pinyinMap = getPinyinMap();
    }
    
    public  Map getPinyinMap(){
        Map<String, String> s_pinyinMap = new TreeMap<String, String>();
        Vector vector = new Vector();
        String s = "";
        String pathName = SystemUtil.instance().getIccsWebHome() + File.separator + "config" + File.separator + "pinyin.txt";
        File fp = new File(pathName);
        try {
            long filelen = fp.length();
            if (filelen > 0) {
                BufferedReader reader = new BufferedReader(new FileReader(pathName));

                while ((s = reader.readLine()) != null) {

                    s = s.replace(" ", "");
                    s = s.toLowerCase();
                    String[] ss = s.split(":");
                    for (int i = 0; i < ss[1].length(); ++i) {
                        String py = ss[0];
                        String hz = String.valueOf(ss[1].charAt(i));
                        if (("0123456789").indexOf(py.charAt(py.length() - 1)) >= 0) {
                            py = py.substring(0, py.length() - 1);
                        }
                        s_pinyinMap.put(hz, py);
                    }
                }

            } else {
            }
        } catch (IOException e) {
        }
        return s_pinyinMap;
    }

    public  String queryQp(String hz, String space){
        String qp = "";
        boolean lastChar = false;	
        for (int i = 0; i < hz.length(); ++i) {
            String s = String.valueOf(hz.charAt(i));
            if (pinyinMap.containsKey(s)) {
                lastChar = false;

                if (qp != "") {
                    qp += space;
                }
                qp += pinyinMap.get(s);
            } 
            else {
                if (!lastChar) {
                    if (qp != "") {
                        qp += space;
                    }
                    lastChar = true;
                }
                qp += s;
            }
        }
        return qp;
    }

    public  String queryJp(String hz){
        String jp = "";
        for (int i = 0; i < hz.length(); ++i) {
            String s = String.valueOf(hz.charAt(i));
            if (pinyinMap.containsKey(s)) {
                jp += pinyinMap.get(s).charAt(0);
            }
            else {
                jp += s;
            }
        }
        return jp;
    }

    
    
}

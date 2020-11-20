package cn.zup.iot.timerdecision.util.file;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

public class IniUtil {
    private HashMap<String,HashMap> section;
    private String filePath;
    
    public IniUtil(String iniFilePath){
        section = new HashMap();
        filePath = iniFilePath; 
        readProperties();
    }
    
    private void readProperties(){
        try{
            InputStreamReader read = new InputStreamReader (new FileInputStream(filePath),"gb2312");
            BufferedReader bufReader = new BufferedReader(read);
            String strLine;
            String strSection = null; 
            HashMap<String,String> property = null;
            while((strLine = bufReader.readLine()) != null){
                strLine = strLine.trim();
                if(strLine.startsWith("#")){ 
                    continue;
                }                
                strLine = strLine.split("#")[0];  
                
                if(strLine.matches("\\[.*\\]")){         
                    strSection = strLine.substring(1, strLine.length()-1);     
                    if(strSection != null && !strSection.equals("")){
                        property = new HashMap();
                        section.put(strSection,property);  
                    }
                } else if(strLine.matches(".*=.*")) {
                    String[] strArr = strLine.split("=");                    
                    property.put(strArr[0],strArr[1]);
                }
            }
        } 
        catch(Exception err){
        }
    }
    
    public HashMap<String,HashMap> getSection(){
        return section;
    }
    
    public HashMap<String,String> getPropertyBySection(String strSection){
        return section.get(strSection);
    }
    
    public String getProperty(String strSection,String key){
        HashMap<String,String> hm = section.get(strSection);
        if(hm != null){
            return hm.get(key);
        }
        return null;
    }    
}

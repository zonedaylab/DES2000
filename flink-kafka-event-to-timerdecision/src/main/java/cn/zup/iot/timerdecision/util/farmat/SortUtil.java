package cn.zup.iot.timerdecision.util.farmat;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.Collator;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SortUtil<E> {
	private static Comparator cmp = Collator.getInstance(java.util.Locale.CHINA);
	public static void main(String[] args)   
    {   
		String[] arr = {"1", "2", "3","4"};   
	    arr = sortArray(arr); 
		for (String a : arr)    
        {   
            System.out.println(a);   
        }  
    }   
       
    @SuppressWarnings("unchecked")   
    public  void test(String[] arr)    
    {    
        Arrays.sort(arr, cmp);   
        for (String a : arr)    
        {   
            System.out.println(a);   
        }   
    }   
	//
    public static String[] sortArray(String[] arr){
    	  Arrays.sort(arr, cmp);
    	  return arr;
    }
    
    
    public void sort(List<E> list, final String method, final String sort){  
        Collections.sort(list, new Comparator() {             
            public int compare(Object a, Object b) {  
                int ret = 0;  
                try{  
                    Method m1 = ((E)a).getClass().getMethod(method, null);  
                    Method m2 = ((E)b).getClass().getMethod(method, null);  
                    if(sort != null && "desc".equals(sort))
                        ret = m2.invoke(((E)b), null).toString().compareTo(m1.invoke(((E)a), null).toString());   
                    else
                        ret = m1.invoke(((E)a), null).toString().compareTo(m2.invoke(((E)b), null).toString());  
                }catch(NoSuchMethodException ne){  
                    System.out.println(ne);  
                }catch(IllegalAccessException ie){  
                    System.out.println(ie);  
                }catch(InvocationTargetException it){  
                    System.out.println(it);  
                }  
                return ret;  
            }  
         });  
    }  
    
}

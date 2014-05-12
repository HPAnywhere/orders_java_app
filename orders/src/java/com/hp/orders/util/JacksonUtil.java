package com.hp.orders.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.sf.json.JSONArray;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import com.hp.orders.model.OrderFileVo;
import com.hp.orders.model.RuleVo;

public class JacksonUtil {
	static ObjectMapper objectMapper = new ObjectMapper();
	
	private static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JacksonUtil.class);
	
	public static ObjectMapper getObjectMapper(){
		return objectMapper;
	}
	
	/**
	 * read the orders from the local file and return as a arrayList
	 * @param path
	 * @return the order list
	 */
	public static List<OrderFileVo>readOrders(String path){
		StringBuilder sb = new StringBuilder();
        BufferedReader in;
		try {
			String str;
			in = new BufferedReader(new FileReader(path));
			while ((str=in.readLine()) != null) 
			{
				sb.append(str);
			}
			in.close();
		} catch (IOException e) {
			log.error("Read the orders failed from file" + e);
			return null;
		}
        log.debug(sb.toString());
        JSONArray testJSONArray=JSONArray.fromObject(sb.toString());
        OrderFileVo[] orderFile = (OrderFileVo[])JSONArray.toArray(testJSONArray, OrderFileVo.class);
		return array2List(orderFile);
	}
	
	/**
	 * write the updated order list to the local file
	 * @param path
	 * @param list
	 */
	public static void saveOrder(String path, List<OrderFileVo> list){
		JSONArray str1 = JSONArray.fromObject(list);
    	FileWriter fw;
		try {
			fw = new FileWriter(path);
			fw.write(str1.toString());
			fw.close();
		} catch (IOException e) {
			log.error("Write the orders failed from file" + e);
		}
	}
	
	/**
	 * convert the Json to a List
	 * @param cls
	 * @param strJson
	 * @return a list which contain the cls type object
	 */
	public static <T> List<T> convertToList(Class<T> cls, String strJson){
		if(strJson != null && !strJson.isEmpty()){
			JSONArray testJSONArray=JSONArray.fromObject(strJson);
			T[] t = (T[])JSONArray.toArray(testJSONArray, cls);
			return (List<T>) array2List(t);
		}
		return null;
	}
	
	/**
	 * convert a list to the Json
	 * @param list
	 * @return a json string
	 */
	public static <T> String convertToJSON(List<T> list){
		if(list != null && list.size() != 0){
			JSONArray strJson = JSONArray.fromObject(list);
			return strJson.toString();
		}
		return "";
	}
	
	/**
	 * convert the Json to an object
	 * @param clz
	 * @param strJson
	 * @return a clz type object
	 */
	public static <T> T converToVo(Class<T> clz, String strJson){
		T t = null;
    	ObjectMapper objectMapper = getObjectMapper();
		try {
			t = objectMapper.readValue(strJson, clz);
		} catch (JsonParseException e) {
			log.error("Exception on JsonParseException in converToRuleVo", e);
		} catch (JsonMappingException e) {
			log.error("Exception on JsonMappingException in converToRuleVo", e);
		} catch (IOException e) {
			log.error("Exception on IOException in converToRuleVo", e);
		}
    	return t;
	}
	
	
	/**
	 * convert an object to a json string format
	 * @param value
	 * @return a json string
	 * @throws IOException
	 */
	public static String convertToString(Object value) throws IOException{
    	String result = "";
    	try {
    		result = getObjectMapper().writeValueAsString(value);
		} catch (JsonProcessingException e) {
			log.error("convert to the json failed" + e);
		}
    	return result;
    }
	
	/**
	 * convert an array to a arrayList
	 * @param obj
	 * @return a arrayList
	 */
	public static <T> List<T> array2List(T[] obj){
		List<T> list = new ArrayList<T>();
		for(int i = 0; i < obj.length; i++){
			list.add(obj[i]);
		}
		return list;
	}
	
    /**
     * maintain the rule list with descending sequence by lowerLimitValue
     * @param list
     * @return the sorted rule list
     */
    public static List<RuleVo> sortByDesc(List<RuleVo> list){
    	 Collections.sort(list, new Comparator<RuleVo>(){
				public int compare(RuleVo m, RuleVo n) {
					return Integer.valueOf(n.getLowerLimitValue()) - Integer.valueOf(m.getLowerLimitValue());
				}
    	 });
    	 return list;
    }
}

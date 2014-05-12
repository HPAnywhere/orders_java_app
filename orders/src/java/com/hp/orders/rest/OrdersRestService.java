package com.hp.orders.rest;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Cookie;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.hp.btoaw.integration.data.DSConfiguration;
import com.hp.btoaw.integration.exception.InvalidConfigurationException;
import com.hp.btoaw.integration.service.DataSourceService;
import com.hp.btoaw.integration.service.keyvalue.KeyValueStorageService;
import com.hp.btoaw.integration.service.security.LWSSOService;
import com.hp.orders.MainApp;
import com.hp.orders.consts.Consts;
import com.hp.orders.model.OrderFileVo;
import com.hp.orders.model.OrderVo;
import com.hp.orders.model.PersistencyOrderVo;
import com.hp.orders.model.RuleVo;
import com.hp.orders.util.JacksonUtil;
import com.sun.jersey.api.core.HttpContext;

@Service
@Path("/orders")
@Produces("application/json;charset=utf-8")
public class OrdersRestService {

    @Autowired
    MainApp mainApp;
    @Autowired
    DataSourceService dataSourceService;
    @Autowired
    LWSSOService securityService;
    @Autowired
    RestTemplate restClient;
    
    //used to write the log
    private static Logger logger = LoggerFactory.getLogger(OrdersRestService.class);
    
    //the suffix of the backendLegacy emulator url 
    private static String SERVICE_SUFFIX_PATH = "/services/backendLegacyOrdersEmulator";
    
    //the empty orderVo  while invoking  the backendLegacy emulator encounter an exception
    private static OrderVo emptyResponse = new OrderVo();
    
    /**
     * when getting into the app, first invoke this rest to verify whether the first time login.
     * validate the loginFlag, if the loginFlag is "Y", show the welcome page, then get into the app directly
     * @param userUniqueId
     * @return a json data {"loginFlag":"Y"} or {"loginFlag":"N"}
     */
    @GET
    @Path("/validateFirstLogin/{userUniqueId}")
    public String validateFirstLogin(@PathParam("userUniqueId") String userUniqueId) {
    	String flag = Consts.FIRSTTIMELOGIN;
    	try {
    		flag = getKeyValueStorageService().get(Consts.Order_Admin,userUniqueId);
    		if(Consts.FIRSTTIMELOGIN.equals(flag) || flag == null){
    			//once the user login, set the loginFlag to "N" in db
    			getKeyValueStorageService().put(Consts.Order_Admin,userUniqueId,Consts.NOTFIRSTTIMELOGIN,null);
    		}
		} catch (Exception e) {
			logger.error("There is an error while process the db.",e);
		}
    	if(Consts.FIRSTTIMELOGIN.equals(flag) || flag == null){
    		return "{\"loginFlag\":" + "\"" +Consts.FIRSTTIMELOGIN + "\"}";
    	}
    	else{
    		return "{\"loginFlag\":" + "\"" +Consts.NOTFIRSTTIMELOGIN + "\"}";
    	}
    }
    
    /**
     * at the orders page will display all the orders.
     * fetch all the order from persistency and return as a list
     * @return the order list with the correct color which get from persistency 
     */
    @GET
    public List<OrderVo> getAll(@Context HttpContext context) {
    	 List<OrderVo> orders = Collections.synchronizedList(new ArrayList<OrderVo>());
    	 List<PersistencyOrderVo> povList = getPersistencyOrderVo();
    	 String rawUrl = context.getRequest().getAbsolutePath().getPath();
         String requestUrl = null;
         List<RuleVo> ruleList = new ArrayList<RuleVo>();
         try {
            requestUrl = getBackendDatasourceBaseUrl(rawUrl) + SERVICE_SUFFIX_PATH;
            // get user ID, which is part of the key, using the UserInfoService
     		String userID = mainApp.getUserInfoService().getUserUniqueId();
     		// add the user ID to the app rules prefix
     		String persistencyKey = Consts.KEY_PREFIX_RULES + userID;
     		//here we are actually getting the value of the rules for the user via persistency API
            //first parameter to the get method is the app key
            //second parameter is the persistencyKey created above, which is something like "MyApprovalsRules.<USERID>"
     		String strRules = getKeyValueStorageService().get(Consts.Order_Admin,persistencyKey);
  			ruleList = JacksonUtil.convertToList(RuleVo.class, strRules);
         } catch (InvalidConfigurationException e) {
             logger.error("Failed to retrieve data source configuration details, with exception:" + e);
             //error occurred, returning empty orders list
             return orders;
         } catch (Exception e) {
        	 logger.error("There is an error while process the db.",e);
		}

         HttpEntity requestEntity = createRequestEntityBySessionCookie(MediaType.APPLICATION_JSON);
         
         ResponseEntity<OrderFileVo[]> response = null;

         try {
             response = restClient.exchange(requestUrl, HttpMethod.GET, requestEntity, OrderFileVo[].class);
             
             if (response != null) {
            	 if (response.getStatusCode() == HttpStatus.OK) {
            		 orders = merge(response.getBody(), povList);
            		 if( ruleList != null ){
          				int j = 0;
          				for(int i = 0; i < orders.size(); i++){
          					while(j < ruleList.size()){
          						int lowerLimitValue = Integer.valueOf(ruleList.get(j).getLowerLimitValue());
          						if(Integer.valueOf(orders.get(i).getAmount()) > lowerLimitValue){
          							orders.get(i).setColor(ruleList.get(j).getColor());
          							break;
          						}
          						j++;
          					}
          					j=0;
          				}
          			}
          			else{
          				for(int k = 0; k < orders.size(); k++){
          					orders.get(k).setColor(Consts.GRAY);
          				}
          			}
            	 } else {
            		 logger.error("response status is:" + response.getStatusCode());
            	 }
             }
         } catch (RestClientException e) {
             logger.error("get all orders failed with RestClientException", e);
         }
         return orders;
    }

    /**
     * while edit one order, can use this method to get the selected order.
     * get the order by the given id
     * @param id
     * @return a order object
     */
    @GET
    @Path("{id}")
    public OrderVo getOrder(@Context HttpContext context, @PathParam("id") String id) {
    	OrderVo order = new OrderVo();
    	for(OrderVo ov: getAll(context)){
    		if(id.equals(ov.getId())){
    			order = ov;
    		}
    	}
        String requestUrl = null;
        String rawUrl = context.getRequest().getAbsolutePath().getPath();
        List<PersistencyOrderVo> povList = getPersistencyOrderVo();
        try {
            requestUrl = getBackendDatasourceBaseUrl(rawUrl) + SERVICE_SUFFIX_PATH + "/{id}";
        } catch (InvalidConfigurationException e) {
            logger.error("Failed to retrieve data source configuration details, with exception:" + e);
            return order;
        }

        HttpEntity requestEntity = createRequestEntityBySessionCookie(MediaType.APPLICATION_JSON);

        ResponseEntity<OrderFileVo> response = null;
        Map<String, String> uriParams = new HashMap<String, String>();
        uriParams.put("id", id);

        try {
            response = restClient.exchange(requestUrl, HttpMethod.GET, requestEntity, OrderFileVo.class, uriParams);
        } catch (RestClientException e) {
            logger.warn("get order id:" + id + " failed with RestClientException", e);
        }

        if (response != null) {
            if (response.getStatusCode() == HttpStatus.OK) {
            	OrderFileVo[] ofvArray = {response.getBody()};
            	order = merge(ofvArray,povList).get(0);
                logger.debug("received successful response status ");
            } else {
                logger.error("response status is:" + response.getStatusCode());
            }
            return order;
        } else {
            return emptyResponse;
        }
    }

    /**
     * handle the approve and the reject options, and keep the deviceInfo,updatedDate to the persistency, meanwhile save the updated reason to the local file.
     * @param id
     * @param orderVoJson
     * @return a updated order
     */
    @PUT
    @Path("{id}")
    @Consumes("application/json;charset=utf-8")
    public OrderVo editOrder(@Context HttpContext context, @PathParam("id") String id,String orderVoJson) {
    	OrderVo order = getOrder(context,id);
    	OrderFileVo ofv = new OrderFileVo();
    	List<PersistencyOrderVo> povs = getPersistencyOrderVo();
    	PersistencyOrderVo pov = new PersistencyOrderVo();
    	String requestUrl = null;
    	String rawUrl = context.getRequest().getAbsolutePath().getPath();
    	OrderVo selectedOrderVo = JacksonUtil.converToVo(OrderVo.class, orderVoJson);
    	SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yy");
    	String dateString = selectedOrderVo.getUpdatedDate();
    	long time = Long.parseLong(dateString);
    	String updateDateDisplay = formatter.format(time);
    	//prepare the updated persistencyOrderVo, later it will store to persistency
    	pov.setId(id);
    	pov.setUpdatedDate(dateString);
    	pov.setDeviceInfo(selectedOrderVo.getDeviceInfo());
    	//prepare the updated orderFileVo, later it will store to file
    	ofv.setId(id);
    	ofv.setReason(selectedOrderVo.getReason());
    	ofv.setStatus(selectedOrderVo.getStatus());
    	logger.debug("persistencyOrderVo is: "+ pov);
    	logger.debug("before add pov to the persistencyOrderVo list : " + getPov(povs));
    	povs.add(pov);
    	logger.debug("after add pov to the persistencyOrderVo list : " + getPov(povs));
    	try {
    		
    		// get user ID, which is part of the key, using the UserInfoService
     		String userID = mainApp.getUserInfoService().getUserUniqueId();
     		// add the user ID and the order ID to the app order prefix
     		String persistencyKey = Consts.KEY_PREFIX_ORDER + userID;
     		//here we are actually storing the value of the orders for the user via persistency API
            //first parameter to the get method is the app key
            //second parameter is the persistencyKey created above, which is something like "MyApprovalsOrders.<USERID>"
     		//third parameter is the orders' value
    		getKeyValueStorageService().put(Consts.Order_Admin,persistencyKey,JacksonUtil.convertToJSON(povs),null);
		} catch (Exception e) {
			logger.error("There is an error while process the db.",e);
		}
    	
        try {
            requestUrl = getBackendDatasourceBaseUrl(rawUrl) + SERVICE_SUFFIX_PATH + "/{id}";
        } catch (InvalidConfigurationException e) {
            logger.error("Failed to retrieve data source configuration details, with exception:" + e);
            //error occurred, returning empty orders list
            return order;
        }

        HttpEntity requestEntity = createRequestEntityBySessionCookie(MediaType.APPLICATION_JSON, ofv);

        ResponseEntity<OrderFileVo> response = null;
        Map<String, String> uriParams = new HashMap<String, String>();
        uriParams.put("id", id);
        try {
            response = restClient.exchange(requestUrl, HttpMethod.PUT, requestEntity, OrderFileVo.class, uriParams);
        } catch (RestClientException e) {
            logger.warn("update order failed with RestClientException", e);
        }
        if (response != null) {
            if (response.getStatusCode() == HttpStatus.OK) {
                logger.debug("received successful response status ");
            	order.setStatus(response.getBody().getStatus());
            	order.setReason(response.getBody().getReason());
            	order.setUpdatedDate(updateDateDisplay);
            	order.setDeviceInfo(pov.getDeviceInfo());
            } else {
                logger.error("response status is:" + response.getStatusCode());
            }
            return order;
        } else {
            return emptyResponse;
        }
    }
    
    /**
     * This method is for getting the rules for the user.
     * Rules are kept in the persistency API, for the user level, under rules key
     * @return List<RuleVO> each contain a rule. return empty List/Null if no rules
     */
    @GET
    @Path("/rules")
    public List<RuleVo> getRules(){
    	// we keep the rules with method : getRules(), for the user level, as a string
        // now we need to get the rules for the user level
        
    	List<RuleVo> list = null;
    	try {
    		// get user ID, which is part of the key, using the UserInfoService
    		String userID = mainApp.getUserInfoService().getUserUniqueId();
    		// add the user ID to the app rules prefix
    		String persistencyKey = Consts.KEY_PREFIX_RULES + userID;
    		//here we are actually getting the value of the rules for the user via persistency API
            //first parameter to the get method is the app key
            //second parameter is the persistencyKey created above, which is something like "MyApprovalsRules.<USERID>"
			String strRules = getKeyValueStorageService().get(Consts.Order_Admin,persistencyKey);
            // actually convert the string to a list of RuleVO
			list =  JacksonUtil.convertToList(RuleVo.class, strRules);
		} catch (Exception e) {
			logger.error("There is an error while process the db.",e);
		}
    	if(list == null){
    		return new ArrayList<RuleVo>();
    	}
    	else{
    		return list;
    	}
    }
    
    /**
     * get the rule by given id
     * @param id
     * @return a rule by given id
     */
    @GET
    @Path("/rules/{id}")
    public RuleVo getRule(@PathParam("id") String id){
    	List<RuleVo> ruleList = getRules();
    	if(ruleList != null && ruleList.size() != 0){
    		for(RuleVo rule : getRules()){
    			if(id.equals(rule.getId())){
    				return rule;
    			}
    		}
    	}
		return null;
    }
    
    /**
     * add a new rule and store it to persistency
     * @param ruleJson
     * @return the added rule
     */
    @POST
    @Path("/rules")
    @Consumes("application/json;charset=utf-8")
    public RuleVo addRule(String ruleJson){
    	RuleVo rule = JacksonUtil.converToVo(RuleVo.class, ruleJson);
    	List<RuleVo> ruleList = getRules();
    	ruleList.add(rule);
    	try {
    		// get user ID, which is part of the key, using the UserInfoService
    		String userID = mainApp.getUserInfoService().getUserUniqueId();
    		// add the user ID to the app rules prefix
    		String persistencyKey = Consts.KEY_PREFIX_RULES + userID;
    		//here we are actually storing the value of the rules for the user via persistency API
            //first parameter to the get method is the app key
            //second parameter is the persistencyKey created above, which is something like "MyApprovalsRules.<USERID>"
    		//third parameter is the value of the rules
    		getKeyValueStorageService().put(Consts.Order_Admin,persistencyKey,JacksonUtil.convertToJSON(JacksonUtil.sortByDesc(ruleList)),null);
		} catch (Exception e) {
			logger.error("There is an error while process the db.",e);
		}
    	return rule;
    }
    
    /**
     * update the rule's lowerLimitValue and the color
     * @param id
     * @param ruleJson
     * @return the updated rule
     */
    @PUT
    @Path("/rules/{id}")
    @Consumes("application/json;charset=utf-8")
    public RuleVo editRule(@PathParam("id") String id, String ruleJson){
    	List<RuleVo> ruleList = getRules();
    	RuleVo newRule = JacksonUtil.converToVo(RuleVo.class, ruleJson);
    	int len = ruleList.size();
    	for(int i = 0; i < len; i++){
    		RuleVo oneRule = ruleList.get(i);    		
    		if(oneRule.getId().equals(newRule.getId())) {
    			oneRule.setLowerLimitValue(newRule.getLowerLimitValue());
        		oneRule.setName(newRule.getName());
        		oneRule.setId(newRule.getId());
        		oneRule.setColor(newRule.getColor());       		
        		break;
    		}
    	}
    	
		try {
			// get user ID, which is part of the key, using the UserInfoService
    		String userID = mainApp.getUserInfoService().getUserUniqueId();
    		// add the user ID to the app rules prefix
    		String persistencyKey = Consts.KEY_PREFIX_RULES + userID;
    		//here we are actually storing the value of the rules for the user via persistency API
            //first parameter to the get method is the app key
            //second parameter is the persistencyKey created above, which is something like "MyApprovalsRules.<USERID>"
    		//third parameter is the value of the rules
    		getKeyValueStorageService().put(Consts.Order_Admin,persistencyKey,JacksonUtil.convertToJSON(JacksonUtil.sortByDesc(ruleList)),null);
		} catch (Exception e) {
			logger.error("There is an error while process the db.",e);
		}    	
    	return newRule;
    }
    
    /**
     * delete the rule from persistency by the given id
     * @param id
     */
    @DELETE
    @Path("/rules/{id}")
    public void deleteRule(@PathParam("id") String id){
    	List<RuleVo> ruleList = getRules();
    	for(int i = 0; i < ruleList.size(); i++){
    		if(id.equals(ruleList.get(i).getId())){
    			ruleList.remove(i);
    			break;
    		}
    	}
    	try {
    		// get user ID, which is part of the key, using the UserInfoService
    		String userID = mainApp.getUserInfoService().getUserUniqueId();
    		// add the user ID to the app rules prefix
    		String persistencyKey = Consts.KEY_PREFIX_RULES + userID;
    		//here we are actually storing the value of the rules for the user via persistency API
            //first parameter to the get method is the app key
            //second parameter is the persistencyKey created above, which is something like "MyApprovalsRules.<USERID>"
    		//third parameter is the value of the rules
    		getKeyValueStorageService().put(Consts.Order_Admin,persistencyKey,JacksonUtil.convertToJSON(ruleList),null);
		} catch (Exception e) {
			logger.error("There is an error while process the db.",e);
		}
    }
    
    /**
     * restore all the orders and the rules
     * @return the default orders 
     */
    @PUT
    @Path("/restore")
    public String restore(@Context HttpContext context) {
    	String restoreSuccess = "{\"restoreSuccess\":" + "\"N\"}";
    	boolean flag = false;
    	String requestUrl = null;
    	String rawUrl = context.getRequest().getAbsolutePath().getPath();
    	HttpEntity requestEntity = createRequestEntityBySessionCookie(MediaType.APPLICATION_JSON);
        
        ResponseEntity<OrderFileVo[]> response = null;

        try {
        	requestUrl = getBackendDatasourceBaseUrl(rawUrl) + SERVICE_SUFFIX_PATH;
        	List<PersistencyOrderVo> povList = getPersistencyOrderVo();
        	List<RuleVo> ruleList = getRules();
        	// get user ID, which is part of the key, using the UserInfoService
    		String userID = mainApp.getUserInfoService().getUserUniqueId();
    		// add the user ID to the app rules prefix
    		String persistencyRulesKey = Consts.KEY_PREFIX_RULES + userID;
    		// add the user ID to the app orders prefix
    		String persistencyOrdersKey = Consts.KEY_PREFIX_ORDER + userID;
    		if((povList == null || povList.size() == 0) && (ruleList == null || ruleList.size() == 0)){
    			flag = true;
    		}
    		else{
	    		if(ruleList != null && ruleList.size() != 0){
	    			//here we are actually delete the all the rules from persistency.
	    			//first parameter to the get method is the app key
	    			//second parameter is the persistencyRulesKey created above, which is something like "MyApprovalsRules.<USERID>"
	    			getKeyValueStorageService().remove(Consts.Order_Admin, persistencyRulesKey);
	    			flag = true;
	    		}
	    		if(povList != null && povList.size() != 0){
	    			//here we are actually delete the all the orders from persistency.
	    			//first parameter to the get method is the app key
	    			//second parameter is the persistencyOrdersKey created above, which is something like "MyApprovalsOrders.<USERID>"
	    			getKeyValueStorageService().remove(Consts.Order_Admin, persistencyOrdersKey);
	    			flag = true;
	    		}
    		}
        } catch (Exception e) {
			logger.error("There is an error while process the db.",e);
		}
        	
    	try {
            response = restClient.exchange(requestUrl, HttpMethod.PUT, requestEntity, OrderFileVo[].class);
            if (response != null) {
           	 if (response.getStatusCode() == HttpStatus.OK) {
           		flag = true;
           		logger.debug("received successful response status ");
           	 } else {
           		 logger.error("response status is:" + response.getStatusCode());
           	 }
            }
        } catch (RestClientException e) {
            logger.error("get all orders failed with RestClientException", e);
        }
    	if(flag){
    		 restoreSuccess = "{\"restoreSuccess\":" + "\"Y\"}";
    	}
    	
    	return restoreSuccess;
    }
    
    /**
     * get the persistencyOrderVo list first, once any order get update, then update this list first, then store the updated list to persistency again
     * this method get the persistencyOrderVo list from the persistency
     * @return persistencyOrderVo list
     */
    public List<PersistencyOrderVo> getPersistencyOrderVo(){
    	List<PersistencyOrderVo> list = null;
    	try {
    		// get user ID, which is part of the key, using the UserInfoService
    		String userID = mainApp.getUserInfoService().getUserUniqueId();
    		// add the user ID to the app rules prefix
    		String persistencyKey = Consts.KEY_PREFIX_ORDER + userID;
    		//here we are actually getting the value of the persistencyOrderVo for the user via persistency API
            //first parameter to the get method is the app key
            //second parameter is the persistencyKey created above, which is something like "MyApprovalsOrders.<USERID>"
			String strPersistencyOrderVo = getKeyValueStorageService().get(Consts.Order_Admin,persistencyKey);
            // actually convert the string to a list of RuleVO
			list =  JacksonUtil.convertToList(PersistencyOrderVo.class, strPersistencyOrderVo);
		} catch (Exception e) {
			logger.error("There is an error while process the db.",e);
		}
    	if(list == null){
    		return new ArrayList<PersistencyOrderVo>();
    	}
    	else{
    		return list;
    	}
    }
    
    private KeyValueStorageService getKeyValueStorageService(){
        return mainApp.getKeyValueStorageService();
    }
    
    private String getBackendDatasourceBaseUrl(String rawUrl) throws InvalidConfigurationException {
    	//get events web folder
    	String urlFolder = rawUrl.split("/")[1];
        //retrieve data source configuration from the HPA server
        DSConfiguration dataSource = dataSourceService.getDataSourceConfig(mainApp.getID() + "-DS", null);

        //build the base url to access the backend according to the value of known data source properties
        String protocol = (String) dataSource.getPropertyValue(Consts.PROTOCOL_KEY_NAME);
        String hostname = (String) dataSource.getPropertyValue(Consts.HOSTNAME_KEY_NAME);
        String port = (String) dataSource.getPropertyValue(Consts.PORT_KEY_NAME);

        String requestUrl = protocol + "://" + hostname + ":" + port + "/" + urlFolder;

        return requestUrl;
    }
    
    private HttpEntity createRequestEntityBySessionCookie(MediaType mediaType) {
        return createRequestEntityBySessionCookie(mediaType, null);
    }
    
    private HttpEntity createRequestEntityBySessionCookie(MediaType mediaType, Object requestObject) {

        Cookie sessionCookie = null;

        try {
            sessionCookie = securityService.getSecurityCookie(mainApp.getUserInfoService().getUserName());
            logger.debug("Success to get cookie from session , cookie name : " + sessionCookie.getName() + " , Cookie value: " + sessionCookie.getValue());
        } catch (Exception e) {
            logger.error("failed to retrieve session cookie", e);
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("X-CSRF-HPMEAP", "FROM-MyApprovals");
        if (sessionCookie != null) {
            headers.add("Cookie", sessionCookie.getName() + "=" + sessionCookie.getValue() + ";" + sessionCookie);
        } else {
            logger.error("session cookie is missing, return requestEntity without cookie");
        }
        HttpEntity request;
        if (requestObject == null) {
            request = new HttpEntity(headers);
        } else {
            request = new HttpEntity(requestObject, headers);
        }

        return request;
    }
    
    /**
     * merge the orderFileVo and persistencyOrderVo to orderVo
     * @param fileOrder
     * @param pov
     * @return orderVo
     */
    public static OrderVo convert2OrderVo(OrderFileVo fileOrder, PersistencyOrderVo pov){
    	OrderVo order = new OrderVo();
    	SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yy");
    	order.setId(fileOrder.getId());
    	order.setAmount(fileOrder.getAmount());
    	order.setCreatedDate(fileOrder.getCreatedDate());
    	order.setName(fileOrder.getName());
    	order.setStatus(fileOrder.getStatus());
    	order.setReason(fileOrder.getReason());
    	if(pov != null){
    		String dateString = pov.getUpdatedDate();
    		long timeStamp = Long.parseLong(dateString);
    		String updateDateDisplay = formatter.format(timeStamp);
    		order.setUpdatedDate(updateDateDisplay);
    		order.setDeviceInfo(pov.getDeviceInfo());
    	}
    	else{
    		order.setUpdatedDate("");
    		order.setDeviceInfo("");
    	}
    	return order;
    }
    
    /**
     * merge the persistencyOrderVo and orderFileVo to  OrderVo, then at the front side, can easily use them.
     * merge the orderFileVo array and persistencyOrderVo list to orderVo list
     * @param ofvList
     * @param povList
     * @return orderVo list
     */
    private static List<OrderVo> merge(OrderFileVo[] ofvList, List<PersistencyOrderVo> povList){
    	List<OrderVo> ovList = new ArrayList<OrderVo>();
    	boolean addFlag = true;
    	if(povList != null && povList.size() != 0){
    		for(int i = 0; i < ofvList.length; i++){
    			addFlag = true;
    			for(int j = 0; j < povList.size(); j++){
    				 OrderFileVo ofv =ofvList[i];
    				 PersistencyOrderVo pov = povList.get(j);
    				 if(pov.getId().equals(ofv.getId())){
    					 //convert the orderfileVo to orderVo and set the order's default color to GRAY
    					 ovList.add(convert2OrderVo(ofv,pov));
    					 ovList.get(i).setColor(Consts.GRAY);
    					 addFlag = false;
    					 break;
    				 }
    			}
    			if(addFlag){
    				ovList.add(convert2OrderVo(ofvList[i],null));
    				ovList.get(i).setColor(Consts.GRAY);
    			}
    		}
    	}
    	else{
    		for(int i = 0; i < ofvList.length; i++){
    			ovList.add(convert2OrderVo(ofvList[i],null));
    			ovList.get(i).setColor(Consts.GRAY);
    		}
    	}
    	return ovList;
    }
    //this method just for print the log
    private static String getPov(List<PersistencyOrderVo> list){
    	StringBuilder sb = new StringBuilder();
    	for(int i = 0; i < list.size(); i++){
    		sb.append(" "+list.get(i).getId()+" "+list.get(i).getDeviceInfo()+" "+list.get(i).getUpdatedDate());
    	}
    	return sb.toString();
    }
}

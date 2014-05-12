package com.hp.orders.rest;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.hp.orders.model.OrderFileVo;
import com.hp.orders.model.OrderVo;
import com.hp.orders.util.JacksonUtil;

@Service
@Path("/backendLegacyOrdersEmulator")
@Produces("application/json;charset=utf-8")
public class LegacyOrdersEmulatorService {
	
	private static Logger logger = LoggerFactory.getLogger(LegacyOrdersEmulatorService.class);
	
	private static List<OrderFileVo> ordersFile = new ArrayList<OrderFileVo>();
	
	
	static {
		ordersFile.add(new OrderFileVo("1","12000","01.08.13","","EliteBook for John","Waiting"));
		ordersFile.add(new OrderFileVo("2","7000","02.08.13","","ThinkPad for David","Waiting"));
		ordersFile.add(new OrderFileVo("3","300","03.08.13","","Dinner for Hans","Waiting"));
		ordersFile.add(new OrderFileVo("4","4300","04.08.13","","iPhone5 for Frank","Waiting"));
		ordersFile.add(new OrderFileVo("5","5000","05.08.13","","PHP Guide for Ray","Waiting"));
		Collections.sort(ordersFile, new Comparator<OrderFileVo>() {
			public int compare(OrderFileVo o1, OrderFileVo o2) {
				SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yy");
				Date date1 = null;
				Date date2 = null;
				try {
					date1 = sdf.parse(o1.getCreatedDate());
					date2 = sdf.parse(o2.getCreatedDate());
				} catch (ParseException e) {
					logger.error("There is an error while parsing date." ,e);
				}
				return date1.compareTo(date2);
			}
		});
	}

	/**
     * fetch all the fileOrders from file
     * @return the fileOrders
     */
	@GET
	public List<OrderFileVo> getAll() {
		return ordersFile;
	}
	
	/**
     * get the order by the given id
     * @param id
     * @return a order object
     */
	@GET
    @Path("{id}")
    public OrderFileVo getOrder(@PathParam("id") String id) {
    	for(OrderFileVo ov : ordersFile){
    		if(ov.getId().equals(id)){
    			return ov;
    		}
    	}
    	return null;
    }
	
	/**
     * handle the approve and the reject options, and save the updated reason to the local file.
     * @param id
     * @param orderVoJson
     * @return a updated order
     */
    @PUT
    @Path("{id}")
    @Consumes("application/json;charset=utf-8")
    public OrderFileVo editOrder(@PathParam("id") String id,String orderVoJson) {
    	OrderVo order = JacksonUtil.converToVo(OrderVo.class, orderVoJson);
    	OrderFileVo ofv = getOrder(id);
    	ofv.setReason(order.getReason());
    	ofv.setStatus(order.getStatus());
    	for(int i = 0; i < ordersFile.size(); i++){
    		if(id.equals(ordersFile.get(i).getId())){
    			ordersFile.get(i).setStatus(ofv.getStatus());
    			ordersFile.get(i).setReason(ofv.getReason());
    			break;
    		}
    	}
    	return ofv;
    }
    
    /**
     * restore the OrderFileVo which means reset the reason to "" and reset the status to "Waiting".
     * @return a list which contains orderFileVo
     */
    @PUT
    @Consumes("application/json;charset=utf-8")
    public List<OrderFileVo> editOrder() {
    	for(int i = 0; i < ordersFile.size(); i++){
    			ordersFile.get(i).setStatus("Waiting");
    			ordersFile.get(i).setReason("");
    	}
    	return ordersFile;
    }
}

package com.cmu.imserver;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import javax.annotation.PostConstruct;

import mongoDB.InsertLog;
import mongoDB.Message;
import mongoDB.MessageRepository;
import mongoDB.Response;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import utils.HTTPClientUtils;
import vo.ClientSecretCredential;
import vo.Credential;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import comm.Constants;
import comm.HTTPMethod;
import comm.Roles;

/**
 * Handles requests for the application home page.
 */
@Controller
public class HomeController {
	
	private static final Logger logger = LoggerFactory.getLogger(HomeController.class);
	private static final JsonNodeFactory factory = new JsonNodeFactory(false);
	private static final String APPKEY = Constants.APPKEY;
    // 通过app的client_id和client_secret来获取app管理员token
    private static Credential credential = new ClientSecretCredential(Constants.APP_CLIENT_ID,
            Constants.APP_CLIENT_SECRET, Roles.USER_ROLE_APPADMIN);
	private TaskScheduler scheduler = new ConcurrentTaskScheduler();
	
	private boolean flag=false;
	private String lastResponse;
	@Autowired
	protected MessageRepository messageRepository;

	
//	@PostConstruct
//	private void executeJob(){
//		scheduler.scheduleWithFixedDelay(new Runnable() {
//	        @Override
//	        public void run() {
//	        	try {
//					Thread.sleep(20000);
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//	            lastResponse=home();
//	        }
//	    }, 120000);
//	}
	
	
	/**
	 * Simply selects the home view to render by returning its name.
	 */
	//@Scheduled(fixedDelay=20000)
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public @ResponseBody String home() {
//		if(flag){
//			return lastResponse;
//		}
//		
//		scheduler.scheduleWithFixedDelay(new Runnable() {
//        @Override
//        public void run() {
//            lastResponse=process();
//            System.out.println("tag");
//        }
//    }, 20000);
//		flag=true;
		lastResponse=process();
		  System.out.println("out");
		  return lastResponse;
	}
	
	public String process(){
	       String currentTimestamp = String.valueOf(System.currentTimeMillis());
//	        String sevenDayAgo = String.valueOf(System.currentTimeMillis() - 9 * 24 * 60 * 60 * 1000);
	        
	        String lastTimestamp = InsertLog.readHistory();
	        ObjectNode queryStrNode1 = factory.objectNode();
	        queryStrNode1.put("ql", "select * where timestamp>" + lastTimestamp + " and timestamp<" + currentTimestamp);
	        ObjectNode messages1 = getChatMessages(queryStrNode1);
	        ObjectMapper mapper = new ObjectMapper();
	        Response response=null;
	        try {
				response=mapper.readValue(messages1.toString(), Response.class);
			} catch (JsonParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JsonMappingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        
	        //log the timestamp
	        InsertLog.logHistory(currentTimestamp);
	        //insert into mongoDB
	        List<Message> list = response.getEntities();
	        for(Message m:list){
	        	messageRepository.insert(m);
	        }
	        return messages1.toString();
	}
	
	 /**
		 * 获取聊天消息
		 * 
		 * @param queryStrNode
		 *
		 */
		public static ObjectNode getChatMessages(ObjectNode queryStrNode) {

			ObjectNode objectNode = factory.objectNode();

			// check appKey format
			if (!HTTPClientUtils.match("^(?!-)[0-9a-zA-Z\\-]+#[0-9a-zA-Z]+", APPKEY)) {
				logger.error("Bad format of Appkey: " + APPKEY);

				objectNode.put("message", "Bad format of Appkey");

				return objectNode;
			}

			try {

				String rest = "";
				if (null != queryStrNode && queryStrNode.get("ql") != null && !StringUtils.isEmpty(queryStrNode.get("ql").asText())) {
					rest = "ql="+ java.net.URLEncoder.encode(queryStrNode.get("ql").asText(), "utf-8");
				}
				if (null != queryStrNode && queryStrNode.get("limit") != null && !StringUtils.isEmpty(queryStrNode.get("limit").asText())) {
					rest = rest + "&limit=" + queryStrNode.get("limit").asText();
				}
				if (null != queryStrNode && queryStrNode.get("cursor") != null && !StringUtils.isEmpty(queryStrNode.get("cursor").asText())) {
					rest = rest + "&cursor=" + queryStrNode.get("cursor").asText();
				}
			
				URL chatMessagesUrl = HTTPClientUtils.getURL(Constants.APPKEY.replace("#", "/") + "/chatmessages?" + rest);
				
				objectNode = HTTPClientUtils.sendHTTPRequest(chatMessagesUrl, credential, null, HTTPMethod.METHOD_GET);

			} catch (Exception e) {
				e.printStackTrace();
			}

			return objectNode;
		}
	
}

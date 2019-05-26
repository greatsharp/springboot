package com.ruckuswireless.intune.refact1st.service.impl;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.Blob;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.net.ssl.HostnameVerifier;

import org.apache.commons.lang.StringUtils;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.quartz.CronExpression;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.ruckuswireless.intune.acs.ACSConstants;
import com.ruckuswireless.intune.acs.engine.operation.events.EventType;
import com.ruckuswireless.intune.acs.util.ACSUtils;
import com.ruckuswireless.intune.acs.util.QuartzHelper;
import com.ruckuswireless.intune.cache.CacheUtils;
import com.ruckuswireless.intune.data.Deviceattr;
import com.ruckuswireless.intune.data.EventConfig;
import com.ruckuswireless.intune.event.EventConfigCache;
import com.ruckuswireless.intune.jms.FMJmsSender;
import com.ruckuswireless.intune.refact1st.dao.IDeviceDao;
import com.ruckuswireless.intune.refact1st.dao.ITaskDao;
import com.ruckuswireless.intune.refact1st.service.ISwitchFirmeareService;
import com.ruckuswireless.intune.refact1st.service.ITaskService;
import com.ruckuswireless.intune.refact1st.vo.DeviceInfo;
import com.ruckuswireless.intune.refact1st.vo.DeviceUpgradeTaskDetail;
import com.ruckuswireless.intune.refact1st.vo.EventCfgTaskDetail;
import com.ruckuswireless.intune.refact1st.vo.TaskAttrs;
import com.ruckuswireless.intune.refact1st.vo.TaskDetail;
import com.ruckuswireless.intune.refact1st.vo.TaskDevice;
import com.ruckuswireless.intune.refact1st.vo.TaskInfo;
import com.ruckuswireless.intune.refact1st.vo.ZdBackupTaskDetail;
import com.ruckuswireless.intune.refact1st.vo.ZdRestoreTaskDetail;
import com.ruckuswireless.intune.system.FMSpringContext;
import com.ruckuswireless.intune.util.FileUtils;
import com.ruckuswireless.intune.web.common.AJAXException;
import com.ruckuswireless.intune.web.common.AbstractTask;
import com.ruckuswireless.intune.web.common.SaveTask;
import com.ruckuswireless.intune.zd.ZdWebUtilService;
import com.ruckuswireless.umm.activemq.EventMessage;
import com.ruckuswireless.umm.activemq.EventMessage.EventOwner;
import com.ruckuswireless.umm.activemq.EventMessage.OwnerType;
import com.ruckuswireless.umm.activemq.FirmwareQueueObject;
import com.ruckuswireless.umm.activemq.QueueDestination;

@Service
public class TaskServiceImpl implements ITaskService {

	private static final Logger log = Logger.getLogger(TaskServiceImpl.class);

	private Map<String, String> hm = new HashMap<String, String>();

	{
		hm.put("ZD5005", "ZD5050-00");
		hm.put("ZD5010", "ZD5100-00");
		hm.put("ZD5015", "ZD5150-00");
		hm.put("ZD5020", "ZD5200-00");
		hm.put("ZD5025", "ZD5250-00");
		hm.put("ZD5030", "ZD5300-00");
		hm.put("ZD5035", "ZD5350-00");
		hm.put("ZD5040", "ZD5400-00");
		hm.put("ZD5045", "ZD5450-00");
		hm.put("ZD5050", "ZD5500-00");
		hm.put("ZD5055", "ZD5550-00");
		hm.put("ZD5060", "ZD5600-00");
		hm.put("ZD5065", "ZD5650-00");
		hm.put("ZD5070", "ZD5700-00");
		hm.put("ZD5075", "ZD5750-00");
		hm.put("ZD5080", "ZD5800-00");
		hm.put("ZD5085", "ZD5850-00");
		hm.put("ZD5090", "ZD5900-00");
		hm.put("ZD5095", "ZD5950-00");
		hm.put("ZD5100", "ZD5100-10");
	}

	private static final String IS_LATEST_VERSION = "isLatestVersion";
	private static final String LINE_SEPARATOR = System.getProperty("line.separator");
	public static final String UPGRADE_LOGFILE_PATH = FMSpringContext.getFMWebRootPath() + "/WEB-INF/logs/";
	public static final String UPGRADE_LOGFILE_PREFIX = "FIRMWARE_UPGRADE_TASK_";
	private static final int MAX_DEVICES = 50;
	public static final String TRIGGER_GROUP_NAME = "DEFAULT";
	public static final String JOB_GROUP_NAME = "DEFAULT";
	public static final String CHECK_UPGRADE_STATE_JOB_PREFIX = "Check upgrade job for task=";
	public static final String CHECK_UPGRADE_STATE_TRIGGER_PREFIX = "Check upgrade trigger for task=";
	public static final String FETCH_DEVICE_UPGRADE_PROGRESS_JOB_PREFIX = "Fetch device upgrade progress job for task=";
	public static final String FETCH_DEVICE_UPGRADE_PROGRESS_TRIGGER_PREFIX = "Fetch device upgrade progress trigger for task=";
	public static final String X_CSRF_TOKEN = "61a18965-f473-4f3b-97b1-4651d63b23fa";

	private static final String CMD_FIND_UPDATES = "<ajax-request action=\"docmd\" comp=\"system\" updater=\"rid.0.8801634771208959\" xcmd=\"unleashed_find_updates\"><xcmd cmd=\"unleashed_find_updates\"/></ajax-request>";
	private static final String CMD_GET_UPGRADE_IMAGE_INFO = "<ajax-request action=\"docmd\" comp=\"system\" updater=\"rid.0.3646445746074325\" xcmd=\"unleashed_get_upgrade_image_info\"><xcmd cmd=\"unleashed_get_upgrade_image_info\"/></ajax-request>";
	private static final String CMD_QUERY_UPDATES_INFO = "<ajax-request action=\"docmd\" comp=\"system\" updater=\"rid.0.3095126601894147\" xcmd=\"unleashed_query_updates_info\"><xcmd cmd=\"unleashed_query_updates_info\"/></ajax-request>";
	private static final String CMD_ONLINE_UPGRADE = "<ajax-request action=\"docmd\" comp=\"system\" updater=\"rid.0.4507601164560582\" xcmd=\"unleashed_online_upgrade\"><xcmd cmd=\"unleashed_online_upgrade\" auto-reboot=\"true\" version=";
	private static final String CMD_QUERY_UPGRADE_PROGRESS = "<ajax-request action=\"docmd\" comp=\"system\" updater=\"rid.0.8478171163455639\" xcmd=\"unleashed_query_upgrade_progress\"><xcmd cmd=\"unleashed_query_upgrade_progress\"/></ajax-request>";
	private static final String CMD_QUERY_UPGRADE_STATE = "<ajax-request action=\"docmd\" comp=\"system\" updater=\"rid.0.475175411961968\" xcmd=\"unleashed_query_upgrade_state\"><xcmd cmd=\"unleashed_query_upgrade_state\"/></ajax-request>";
	private static final String CMD_SET_FROMVERSION = "<ajax-request IS_PARTIAL=\"true\" comp=\"system\" action=\"setconf\"><ap-images IS_PARTIAL=\"true\" frombld=\"{build}\" fromver=\"{version}\" /></ajax-request>";
	
	private static class ThreadPoolBuilder {
		static final int CPU_NUMBER = Runtime.getRuntime().availableProcessors();
		private static ExecutorService threadPool = new ThreadPoolExecutor(CPU_NUMBER, CPU_NUMBER*2, 60, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(MAX_DEVICES), new ThreadPoolExecutor.CallerRunsPolicy());
		private static ExecutorService cachedThreadPool = Executors.newCachedThreadPool();
		private static CompletionService<Map<String, List<String>>> cs = new ExecutorCompletionService<Map<String, List<String>>>(cachedThreadPool);
	}

	private static class RestTemplateBuilder {
		private static RestTemplate restTemplate = TaskServiceImpl.getHttpClient();
	}

	@Autowired(required=true)
	private IDeviceDao dDao;

	@Autowired
	ITaskDao taskDao;

	@Autowired
	private CacheUtils cacheUtils;

	@Autowired
	private EventConfigCache eventConfigCache;

	@Autowired
	@Qualifier("jmsTemplate")
	private JmsTemplate jmsTemplate;

	@Autowired
	private FMJmsSender jmsSender;

	@Autowired
	private ISwitchFirmeareService switchFirmeareService;


	public static ExecutorService getThreadPool() {
		return ThreadPoolBuilder.threadPool;
	}

	public static CompletionService<Map<String, List<String>>> getCompletionService() {
		return ThreadPoolBuilder.cs;
	}

	private static RestTemplate getHttpClient() {
		final HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
		final HostnameVerifier hostNameVerifier = new NoopHostnameVerifier();

		try {
			SSLContextBuilder builder = new SSLContextBuilder().loadTrustMaterial(null, new TrustSelfSignedStrategy());
			SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(builder.build(), hostNameVerifier);
			CloseableHttpClient httpclient = HttpClients.custom().setSSLHostnameVerifier(hostNameVerifier)
					.setSSLSocketFactory(sslsf).build();
			requestFactory.setHttpClient(httpclient);
		} catch(Exception e) {
			log.warn("Initiate RestTemplate failed!");
		}

		RestTemplate template = new RestTemplate(requestFactory);
		return template;
	}

	@Override
	public String addTask(Map<String, Object> baseMap, List<Map<String, Object>> data, Locale locale, Integer taskType) throws Exception {
		StringBuffer sb = new StringBuffer();
		Integer successNum = 0;
		for(Map<String, Object> map: data){
			switch (taskType) {
				case 1:
					baseMap.put("firmwaremodel", new String[]{map.get("firmwaremodel").toString()});
					baseMap.put("seldevice", new String[]{map.get("selectDevices").toString()});
					break;
				case 9:
					baseMap.put("fileid", new String[]{map.get("fileId").toString()});
					baseMap.put("comments", new String[]{map.get("comment").toString()});
					baseMap.put("seldevice", new String[]{map.get("selectDevices").toString()});
					break;
			}
			AbstractTask handler = new SaveTask(baseMap);
			try{
				handler.execute();
				successNum++;
			}catch(AJAXException e){
				if(sb.length() > 0){
					sb.append("; ");
				}
				sb.append("Operated error on deviceId equals " + map.get("selectDevices") + ",");
				sb.append(e.getMessage(locale));
			}
		}
		if(successNum == 0){
			throw new Exception(sb.toString());
		}
		return sb.toString();
	}

	private TaskDetail getLastestTaskDetail(Integer taskType, Integer taskId, Integer userId, Integer roleId) {
		TaskDetail detail = null;
		List<TaskDetail> list = null;
		switch (taskType) {
			case TaskInfo.TASK_TYPE.ZD_BACKUP_TASK:
				list = getZdBackupTaskDetail(taskId, userId, roleId);
				break;
			case TaskInfo.TASK_TYPE.UNLEASHED_UPGRADE_TASK:
				list = getUpgradeTaskDetail(taskId, userId, roleId);
				break;
			case TaskInfo.TASK_TYPE.ZD_RESTORE_TASK:
				list = getZdRestoreTaskDetail(taskId, userId, roleId);
				break;
			case TaskInfo.TASK_TYPE.ICX_BACKUP_TASK:
				list = taskDao.getLastIcxBackupTaskDetail(taskId, userId, roleId);
				break;
			case TaskInfo.TASK_TYPE.ICX_RESTORE_TASK:
				list = taskDao.getLastIcxBackupTaskDetail(taskId, userId,roleId);
				break;
			case TaskInfo.TASK_TYPE.SWITCH_UPGRADE_TASK:
				list = getUpgradeTaskDetail(taskId, userId, roleId);
				break;
		}
		detail = list != null && list.size() > 0 ? list.get(0) : null;
		return detail;
	}

	@Override
	public List<TaskInfo> getTaskList(String taskType, Integer userId, Integer roleId, String sortBy, String sortOrd, Locale locale) {
		List<TaskInfo> taskInfos = taskDao.getTaskList(taskType, userId, roleId, sortBy, sortOrd);
		if(taskInfos == null){
			taskInfos = new ArrayList<>();
		}else if(Integer.parseInt(taskType) == 12){
			for(TaskInfo info: taskInfos){
				info.setStatus(this.getTaskStatus(info.getTaskId(), locale));
			}
		}

		List<TaskInfo> returnList = new ArrayList<TaskInfo>(taskInfos.size());
		// update the task status for each task
		for (TaskInfo info : taskInfos) {
			Integer infoTaskType = info.getTaskType();

			//if a task doesn't has any device in it, then omit this task
			int deviceCount = 0;
			switch(infoTaskType) {
				case 99:
					deviceCount = taskDao.deviceCountInZdbackupTable(info.getTaskId());
					break;
				case 9:
				case 13:
				case 16:
					deviceCount = taskDao.deviceCountInTaskTable(info.getTaskId());
					break;
				case 14:
				case 15:
					deviceCount = taskDao.deviceCountInIcxTaskTable(info.getTaskId());
					break;
				default:
					deviceCount = 1;	//for any new task type used in future
					break;
			}
			if(deviceCount == 0) {
				//if a task doesn't has any device in it, then omit this task
				continue;
			}

			TaskDetail detail = getLastestTaskDetail(infoTaskType, info.getTaskId(), userId, roleId);
			Long lastRuntime = null;
			Integer success = null;
			String status = null;
			Short taskStatus = ACSConstants.TASK_NOT_FIRED_YET;

			if(TaskInfo.TASK_TYPE.SWITCH_UPGRADE_TASK == infoTaskType) {
				TaskAttrs ta = taskDao.getTaskAttrs(info.getTaskId(), TaskAttrs.ICX_UPGRADE_TYPE);
				if(ta != null) {
					info.setUpgradeType(ta.getTaskAttrValue());
				}
			}

			if(infoTaskType == 12){
				info.setStatus(this.getTaskStatus(info.getTaskId(), locale));
				info.setCreateTime(info.getCreateTime() + "000");
			}else if (detail == null) {
				info.setStatus(this.getStatusI18N(ACSConstants.TASK_NOT_FIRED_YET, locale));
			} else {
				switch (infoTaskType) {
					case TaskInfo.TASK_TYPE.ZD_BACKUP_TASK:
						ZdBackupTaskDetail backupTaskDetail = (ZdBackupTaskDetail) detail;
						lastRuntime = backupTaskDetail.getRuntime();
						if (lastRuntime == null) {
							status =  this.getStatusI18N(ACSConstants.TASK_NOT_FIRED_YET, locale);
						} else {
							success = backupTaskDetail.getSuccess() == null ? 0 : backupTaskDetail.getSuccess();
							status = (success == 1 ? this.getStatusI18N(ACSConstants.TASK_SUCCESS, locale):this.getStatusI18N(ACSConstants.TASK_FAILED, locale)) + " on " + DateTimeFormatter.ofPattern("MMM dd yyyy HH:mm:ss").format(LocalDateTime.ofInstant(Instant.ofEpochSecond(lastRuntime), ZoneId.systemDefault()));
						}
						break;
					case TaskInfo.TASK_TYPE.ZD_RESTORE_TASK:
						ZdRestoreTaskDetail restoreTaskDetail = (ZdRestoreTaskDetail) detail;
						lastRuntime = restoreTaskDetail.getLastSeen();
						status = this.getTaskStatus(info.getTaskId(), locale);
						if (lastRuntime != null) {
							status = status + " on " + DateTimeFormatter.ofPattern("MMM dd yyyy HH:mm:ss").format(LocalDateTime.ofInstant(Instant.ofEpochSecond(lastRuntime), ZoneId.systemDefault()));
						}
						break;
					case TaskInfo.TASK_TYPE.UNLEASHED_UPGRADE_TASK:
						DeviceUpgradeTaskDetail upgradeTaskDetail = (DeviceUpgradeTaskDetail) detail;
						taskStatus = upgradeTaskDetail.getTaskStatus() == null ? ACSConstants.TASK_NOT_FIRED_YET : Integer.valueOf(upgradeTaskDetail.getTaskStatus()).shortValue();
						if (StringUtils.isEmpty(upgradeTaskDetail.getTimeStampCompleted())) {
							status = this.getStatusI18N(taskStatus, locale); //ACSConstants.TASK_STATUS_ENUM.values()[taskStatus].name();
						} else {
							lastRuntime = Long.valueOf(upgradeTaskDetail.getTimeStampCompleted());
							status = this.getStatusI18N(taskStatus, locale) + " on " + DateTimeFormatter.ofPattern("MMM dd yyyy HH:mm:ss").format(LocalDateTime.ofInstant(Instant.ofEpochSecond(lastRuntime), ZoneId.systemDefault()));
						}
						break;
					case TaskInfo.TASK_TYPE.ICX_BACKUP_TASK:
						ZdBackupTaskDetail icxBackupTaskDetail = (ZdBackupTaskDetail) detail;
						lastRuntime = icxBackupTaskDetail.getRuntime();
						Integer backupStatusCode = icxBackupTaskDetail.getSuccess();
						if (lastRuntime == null) {
							status = this.getStatusI18N(Short.valueOf(String.valueOf(backupStatusCode)), locale);
						} else {
							status = this.getStatusI18N(Short.valueOf(String.valueOf(backupStatusCode)), locale) + " on " + DateTimeFormatter.ofPattern("MMM dd yyyy HH:mm:ss").format(LocalDateTime.ofInstant(Instant.ofEpochSecond(lastRuntime), ZoneId.systemDefault()));
						}
						break;
					case TaskInfo.TASK_TYPE.ICX_RESTORE_TASK:
						ZdBackupTaskDetail icxRestoreTaskDetail = (ZdBackupTaskDetail) detail;
						lastRuntime = icxRestoreTaskDetail.getRuntime();
						Integer restoreStatusCode = icxRestoreTaskDetail.getSuccess();
						if (lastRuntime == null) {
							status = this.getStatusI18N(Short.valueOf(String.valueOf(restoreStatusCode)), locale);
						} else {
							status = this.getStatusI18N(Short.valueOf(String.valueOf(restoreStatusCode)), locale) + " on " + DateTimeFormatter.ofPattern("MMM dd yyyy HH:mm:ss").format(LocalDateTime.ofInstant(Instant.ofEpochSecond(lastRuntime), ZoneId.systemDefault()));
						}
						break;
					case TaskInfo.TASK_TYPE.SWITCH_UPGRADE_TASK:
						DeviceUpgradeTaskDetail icxUpgradeTaskDetail = (DeviceUpgradeTaskDetail) detail;
						taskStatus = icxUpgradeTaskDetail.getTaskStatus() == null ? ACSConstants.TASK_NOT_FIRED_YET : Integer.valueOf(icxUpgradeTaskDetail.getTaskStatus()).shortValue();
						if (StringUtils.isEmpty(icxUpgradeTaskDetail.getTimeStampCompleted())) {
							status = this.getStatusI18N(taskStatus, locale);
						} else {
							lastRuntime = Long.valueOf(icxUpgradeTaskDetail.getTimeStampCompleted());
							status = this.getStatusI18N(taskStatus, locale) + " on " + DateTimeFormatter.ofPattern("MMM dd yyyy HH:mm:ss").format(LocalDateTime.ofInstant(Instant.ofEpochSecond(lastRuntime), ZoneId.systemDefault()));
						}
						break;
				}
				info.setStatus(status);
			}

			returnList.add(info);
		}
		return returnList;
	}

	private String getTaskStatus(Integer taskId, Locale locale) {

		List<Integer> list = taskDao.getTaskStatus(taskId);
		boolean isSuccess = true;
		boolean isFailed = false;
		boolean isDownloaded = false;
		boolean isCancelled = false;
		boolean isApplied = false;
		boolean isPending = false;
		boolean isExpired = false;
		boolean isNotFiredYet = false;
		for (Integer status : list) {
			if (status != ACSConstants.TASK_SUCCESS) {
				isSuccess = false;
			}
			if (status == ACSConstants.TASK_PENDING) {
				isPending = true;
			} else if (status == ACSConstants.TASK_FAILED) {
				isFailed = true;
			} else if (status == ACSConstants.TASK_CANCELLED) {
				isCancelled = true;
			} else if (status == ACSConstants.TASK_NOT_FIRED_YET) {
				isNotFiredYet = true;
			} else if (status == ACSConstants.TASK_APPLIED) {
				isApplied = true;
			} else if (status == ACSConstants.TASK_DOWNLOADED) {
				isDownloaded = true;
			} else if (status == ACSConstants.TASK_EXPIRED) {
				isExpired = true;
			}
		}

		Short taskStatus = null;
		if (isSuccess) {
			taskStatus = ACSConstants.TASK_SUCCESS;
		} else if (isFailed) {
			taskStatus = ACSConstants.TASK_FAILED;
		} else if (isCancelled) {
			taskStatus = ACSConstants.TASK_CANCELLED;
		} else if (isPending) {
			taskStatus = ACSConstants.TASK_PENDING;
		} else if (isApplied) {
			taskStatus = ACSConstants.TASK_APPLIED;
		} else if (isDownloaded) {
			taskStatus = ACSConstants.TASK_DOWNLOADED;
		} else if (isExpired) {
			taskStatus = ACSConstants.TASK_EXPIRED;
		} else if (isNotFiredYet) {
			taskStatus = ACSConstants.TASK_NOT_FIRED_YET;
		} else {
			taskStatus = ACSConstants.TASK_FAILED;
		}
		return getStatusI18N(taskStatus, locale);

	}

	@Override
	public List<TaskDetail> getZdBackupTaskDetail(Integer taskId, Integer userId, Integer roleId) {
		List<TaskDetail> data = taskDao.getZdBackupTaskDetail(taskId, userId, roleId);
		if(data == null){
			data = new ArrayList<>();
		}
		return data;
	}

	@Override
	public List<TaskDetail> getIcxBackupTaskDetail(Integer taskId, Integer userId, Integer roleId) {

		List<TaskDetail> data = taskDao.getIcxBackupTaskDetail(taskId, userId, roleId);
		if(data == null){
			data = new ArrayList<>();
		}
		return data;
	}

	@Override
	public List<TaskDetail> getZdRestoreTaskDetail(Integer taskId, Integer userId, Integer roleId) {
		List<TaskDetail> data = taskDao.getZdRestoreTaskDetail(taskId, userId, roleId);
		if(data == null){
			data = new ArrayList<>();
		}else{
			for (Iterator<TaskDetail> it = data.iterator(); it.hasNext();) {
				ZdRestoreTaskDetail taskDetail = (ZdRestoreTaskDetail) it.next();
				taskDetail.setId(taskDetail.getDeviceId() + "," + taskDetail.getTaskId() + "," + taskDetail.getStatus());
				taskDetail.setSerial(taskDetail.getSerialNumber() + "," + taskDetail.getModelName() + "," + taskDetail.getDeviceId());
				String comments = taskDetail.getComments();
				if(comments != null){
					String[] versions = comments.split(",");
					taskDetail.setVersion(versions[0]);
					taskDetail.setNewVersion(versions[1]);
					taskDetail.setComments(null);
				}
				taskDetail.setFaultSummary(getFaultSummary(taskDetail.getSummaryBlob()));
				taskDetail.setSummaryBlob(null);
			}
		}
		return data;
	}

	@Override
	public List<TaskDetail> getIcxRestoreTaskDetail(Integer taskId, Integer userId, Integer roleId) {

		List<TaskDetail> data = taskDao.getIcxBackupTaskDetail(taskId, userId, roleId);
		if(data == null){
			data = new ArrayList<>();
		}
		return data;
	}

	public List<TaskDevice> getZdBackupTaskDevices(Integer taskId, Integer userId, Integer roleId) {
		List<TaskDevice> data = taskDao.getZdBackupTaskDevices(taskId, userId, roleId);
		if(data == null) {
			data = new ArrayList<>();
		}
		return data;
	}

	@Override
	public List<TaskDevice> getIcxBackupTaskDevices(Integer taskId, Integer userId, Integer roleId) {
		List<TaskDevice> data = taskDao.getIcxBackupTaskDevices(taskId, userId, roleId);
		if(data == null) {
			data = new ArrayList<>();
		}
		return data;
	}

	public List<TaskDevice> getZdRestoreTaskDevices(Integer taskId, Integer userId, Integer roleId) {
		List<TaskDevice> data = taskDao.getZdRestoreTaskDevices(taskId, userId, roleId);
		if(data == null) {
			data = new ArrayList<>();
		}
		return data;
	}

	@Override
	public List<TaskDevice> getIcxRestoreTaskDevices(Integer taskId, Integer userId, Integer roleId) {
		List<TaskDevice> data = taskDao.getIcxRestoreTaskDevices(taskId, userId, roleId);
		if(data == null) {
			data = new ArrayList<>();
		}
		return data;
	}

	public List<TaskDevice> getUnleashedUpgradeTaskDevices(Integer taskId, Integer userId, Integer roleId, boolean excludeFinished) {
		List<TaskDevice> data = taskDao.getUnleashedUpgradeTaskDevices(taskId, userId, roleId, excludeFinished);
		if(data == null) {
			data = new ArrayList<>();
		}
		return data;
	}

	@Override
	public List<TaskDetail> getUpgradeTaskDetail(Integer taskId, Integer userId, Integer roleId) {
		List<TaskDetail> list = taskDao.getUpgradeTaskDetail(taskId, userId, roleId);
		if(list==null || list.size()==0) {
			return new ArrayList<>();
		}

		return list;
	}

	private String getFaultSummary(Object obj){
		Blob blob= (Blob)obj;
		String ret = "" ;
		if(blob != null){
			try {
				byte[] bdata = blob.getBytes(1, (int) blob.length());
				ret = new String(bdata);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				ret = "" ;
			}
		}
		return ret ;
	}

	@Override
	public List<TaskDetail> getSoloApConfigTaskDetail(Integer taskId, Integer userId, Integer roleId) {
		List<TaskDetail> data = taskDao.getSoloApConfigTaskDetail(taskId, userId, roleId);
		if(data == null){
			data = new ArrayList<>();
		}
		return data;
	}

	@Override
	@Transactional
	public Integer delZdBackupTask(Integer taskId, Integer userId, Integer roleId) throws Exception {
		taskDao.delZdBackupDeviceHistory(taskId, userId, roleId);
		taskDao.delZdBackupDevice(taskId, userId, roleId);
		taskDao.delZdBackupHistory(taskId, userId, roleId);
		Integer result = taskDao.delZdBackupTask(taskId, userId, roleId);
		if(result <= 0){
			throw new Exception("Delete zdbackup_task failed in taskId equals " + taskId + "!");
		}
		return result;
	}

	@Override
	public Integer delIcxTask(Integer taskId, Integer userId, Integer roleId,Integer taskType) throws Exception {
		log.warn(taskId);
		log.warn(userId);
		List<String> fileList =new ArrayList<>();
		if(taskType==TaskInfo.TASK_TYPE.ICX_BACKUP_TASK){
			fileList = taskDao.getBackFileNamesInIcxBackupTaskByTaskId(taskId, userId, roleId);
		}
		taskDao.delIcxTaskHistory(taskId, userId, roleId);
		taskDao.delIcxTaskDevice(taskId, userId, roleId);
		Integer result = taskDao.delIcxTask(taskId, userId, roleId);
		//delete back storage files
		String fileDir = FMSpringContext.getFlexMasterHome() + "/apps/umm-agent/icx-config/";
		if(taskType==TaskInfo.TASK_TYPE.ICX_BACKUP_TASK){
			for(String fileName : fileList) {
				try {
					FileUtils.delete(fileDir+fileName);
				}catch (Exception e){
					//ignore
				}
			}
		}
		return result;
	}

	@Override
	@Transactional
	public Integer delTask(Integer taskId, Integer userId, Integer roleId) throws Exception {
		Integer result = 0;
		result = taskDao.delTask(taskId, userId, roleId);
		return result;
	}

	@Override
	@Transactional
	public Integer delSoloApConfigTask(Integer taskId, Integer userId, Integer roleId) throws Exception {
		Integer result = 0;
		return result;
	}

	@Override
	@Transactional
	public Integer delTaskList(List<Map<String, Integer>> taskArr, Integer userId, Integer roleId) throws Exception {
		Integer result = 0;
		for(Map<String, Integer> taskMap: taskArr){
			if(taskMap.size() == 0){
				throw new Exception("Delete task failed, task info is empty!");
			}
			if(taskMap.size() == 1){
				throw new Exception("Delete task failed, task type is empty when taskId = " + taskMap.get("taskId") + "!");
			}
			switch (taskMap.get("taskType")) {
				case TaskInfo.TASK_TYPE.ZD_BACKUP_TASK:
					taskDao.delZdBackupDeviceHistory(taskMap.get("taskId"), userId, roleId);
					taskDao.delZdBackupDevice(taskMap.get("taskId"), userId, roleId);
					taskDao.delZdBackupHistory(taskMap.get("taskId"), userId, roleId);
					result = taskDao.delZdBackupTask(taskMap.get("taskId"), userId, roleId);
					if(result <= 0){
						throw new Exception("Delete zdbackup_task failed in taskId equals " + taskMap.get("taskId") + "!");
					}
					break;
				case TaskInfo.TASK_TYPE.UNLEASHED_UPGRADE_TASK:
				case TaskInfo.TASK_TYPE.SWITCH_UPGRADE_TASK:
					result = taskDao.delTask(taskMap.get("taskId"), userId, roleId);
					if(result <= 0){
						throw new Exception("Delete task failed in taskId equals " + taskMap.get("taskId") + "!");
					}
					//delete the upgrade log
					this.deleteUpgradeLog(taskMap.get("taskId"));
					break;
				case TaskInfo.TASK_TYPE.ZD_RESTORE_TASK:
					result = taskDao.delTask(taskMap.get("taskId"), userId, roleId);
					if(result <= 0){
						throw new Exception("Delete task failed in taskId equals " + taskMap.get("taskId") + "!");
					}
					break;
				case TaskInfo.TASK_TYPE.SOLO_AP_CONFIG_TASK:
				case TaskInfo.TASK_TYPE.ICX_BACKUP_TASK:
					this.delIcxTask(taskMap.get("taskId"), userId, roleId,TaskInfo.TASK_TYPE.ICX_BACKUP_TASK);
					break;
				case TaskInfo.TASK_TYPE.ICX_RESTORE_TASK:
					this.delIcxTask(taskMap.get("taskId"), userId, roleId,TaskInfo.TASK_TYPE.ICX_RESTORE_TASK);
					break;
				default:
					throw new Exception("Delete task failed, task type is not define!");
			}
		}
		return result;
	}

	private void deleteUpgradeLog(Integer taskId) {
		FileSystem fs = FileSystems.getDefault();
		PathMatcher pm = fs.getPathMatcher("glob:" + UPGRADE_LOGFILE_PATH + UPGRADE_LOGFILE_PREFIX + taskId + "_*.txt");
		try {
			Files.walkFileTree(Paths.get(UPGRADE_LOGFILE_PATH), new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
					if (pm.matches(path)) {
						Files.delete(path);
					}
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
					return FileVisitResult.CONTINUE;
				}
			});
		} catch (IOException e) {
			log.error("Delete task=" + taskId + " upgrade log file failed!", e);
		}
	}

	@Override
	public List<TaskDetail> getEventTaskDetail(Integer taskId, Integer userId, Integer roleId, Locale locale) {
		String deviceIds = cacheUtils.getAssignedDeviceIds(userId, roleId, 0);
		if(deviceIds == null || deviceIds.isEmpty())
			return null;
		List<TaskDetail> data = taskDao.getEventTaskDetail(taskId, deviceIds);

		for(TaskDetail detail: data){
			EventCfgTaskDetail tmp = (EventCfgTaskDetail) detail;
			Integer deviceId = tmp.getDeviceId().intValue();
			EventConfig eventConfig = this.eventConfigCache.getEventConfigByDeviceId(deviceId);
			tmp.setTaskName(eventConfig.getName());
			String modelUpper = tmp.getModel().toUpperCase();
			if(modelUpper.startsWith("ZD5") && !modelUpper.toUpperCase().equals("ZD5000")){
				tmp.setModel((String)hm.get(modelUpper));
			}
			String taskStatus = this.getStatusI18N(Short.parseShort(tmp.getStatus()), locale);
			tmp.setStatus(taskStatus);
		}

		return data;
	}

	private String getStatusI18N(Short taskStatus, Locale locale) {
		ResourceBundle rb = ResourceBundle.getBundle("uimsg", locale);

		String statusStr = null;
		switch (taskStatus) {
			case ACSConstants.TASK_SUCCESS:
				statusStr = rb.getString("admin.provision.status.success");
				break;
			case ACSConstants.TASK_FAILED:
				statusStr = rb.getString("admin.provision.status.failed");
				break;
			case ACSConstants.TASK_CANCELLED:
				statusStr = rb.getString("admin.provision.status.canceled");
				break;
			case ACSConstants.TASK_PENDING:
				statusStr = rb.getString("admin.provision.status.pending");
				break;
			case ACSConstants.TASK_APPLIED:
				statusStr = rb.getString("admin.provision.status.applied");
				break;
			case ACSConstants.TASK_DOWNLOADED:
				statusStr = rb.getString("admin.provision.status.downloaded");
				break;
			case ACSConstants.TASK_EXPIRED:
				statusStr = rb.getString("admin.provision.status.expired");
				break;
			case ACSConstants.TASK_INCOMPLETE:
				statusStr = rb.getString("admin.provision.status.incomplete");
				break;
			case ACSConstants.TASK_NOT_FIRED_YET:
				statusStr = rb.getString("admin.provision.status.notFiredYet");
		}

		return statusStr;

	}

	@Override
	public Integer retryEventCfgTask(Integer taskId) {
		Integer result = taskDao.retryEventCfgTask(taskId);
		return result;
	}

	@Override
	public TaskInfo getTaskByIdandType(Integer taskId, Integer taskType, Integer userId, Integer roleId) {
		if(taskId==null || taskType==null) {
			return null;
		}

		TaskInfo task = null;

		switch(taskType) {
			case TaskInfo.TASK_TYPE.ZD_BACKUP_TASK:
				task = taskDao.getZdBackupTask(taskId, userId, roleId);
				break;
			case TaskInfo.TASK_TYPE.ZD_RESTORE_TASK:
				task = taskDao.getZdRestoreTask(taskId, userId, roleId);
				break;
			case TaskInfo.TASK_TYPE.UNLEASHED_UPGRADE_TASK:
				task = taskDao.getUpgradeTask(taskId, userId, roleId);
				break;
			case TaskInfo.TASK_TYPE.ICX_BACKUP_TASK:
				task = taskDao.getIcxBackupTask(taskId, userId, roleId);
				break;
			case TaskInfo.TASK_TYPE.ICX_RESTORE_TASK:
				task = taskDao.getIcxRestoreTask(taskId, userId, roleId);
				break;
			case TaskInfo.TASK_TYPE.SWITCH_UPGRADE_TASK:
				task = taskDao.getUpgradeTask(taskId, userId, roleId);
				break;
			default:
				break;
		}

		return task;
	}

	@Override
	@Transactional(rollbackFor=Exception.class)
	public String updateUnleashedUpgradeTask(TaskInfo task, List<TaskDevice> taskDevices, Locale locale,
											 Integer taskType, Integer userId, Integer roleId) throws Exception {

		Integer successNum = 0;
		int runNowCount = 0;
		boolean isScheduledTask = false;

		if(taskDevices.size() > MAX_DEVICES) {
			return "Server cannot support more than " + MAX_DEVICES + " devices to do upgrade concurrently!";
		}

		if(task.getScheduled()!=null && task.getScheduled().length()>0) {
			isScheduledTask = true;
			if(System.currentTimeMillis() > Long.valueOf(task.getScheduled())) {
				return "Scheduled time must be later than current time!";
			}
		}

		taskDao.updateTask(task);
		for(TaskDevice device : taskDevices) {
			device.setIsCompleted(0);
			device.setServerRetryCount(0);
			device.setTaskStatus(isScheduledTask?Integer.valueOf(ACSConstants.TASK_NOT_FIRED_YET):Integer.valueOf(ACSConstants.TASK_APPLIED));
			taskDao.updateTaskDevice(device);

			TaskAttrs ta = new TaskAttrs();
			ta.setTaskId(task.getTaskId());
			ta.setDeviceId(device.getDeviceId());
			ta.setTaskAttrName(TaskAttrs.TARGET_VERSION);
			ta.setTaskAttrValue(device.getOriginalVersion() + "->" + device.getTargetVersion());
			taskDao.updateTaskAttrs(ta);

			if(!isScheduledTask) {
				//add device to thread pool,emit online upgrade concurrently
				this.doUpgradeInThreadPool(device, userId, roleId);
				runNowCount++;
			}

			successNum++;
		}

		if(successNum>0 && isScheduledTask){
			//schedule task to DB
			this.scheduleUpgradeTask(task.getTaskId(), task.getScheduled(), userId, roleId);
		}

		if(runNowCount > 0) {
			//schedule a quartz task, run once after 1 hour later, to check the upgrade status.
			this.scheduleUpgradeStatusCheckTask(task.getTaskId(), userId, roleId);
		}

		return null;
	}


	@Override
	@Transactional(rollbackFor=Exception.class)
	public String addUnleashedUpgradeTask(TaskInfo task, List<TaskDevice> taskDevices, Locale locale, Integer taskType, Integer userId, Integer roleId)
			throws Exception {
		Integer successNum = 0;
		int runNowCount = 0;
		boolean isScheduledTask = false;

		if(taskDevices.size() > MAX_DEVICES) {
			return "Server cannot support more than " + MAX_DEVICES + " devices to do upgrade concurrently!";
		}

		if(task.getScheduled()!=null && task.getScheduled().length()>0) {
			isScheduledTask = true;
			if(System.currentTimeMillis() > Long.valueOf(task.getScheduled())) {
				return "Scheduled time must be later than current time!";
			}
		}

		Integer count = taskDao.isTaskNameExist(task.getTaskName());
		if(count > 0) {
			return "Task name is exist!";
		}

		for(TaskDevice device : taskDevices) {
			List<TaskDevice> list = taskDao.isDeviceInScheduledTask(device.getDeviceId());
			if(list.size() > 0) {
				return "Device (" + list.get(0).getDeviceName() + ") is in another scheduled upgrade task (" + list.get(0).getTaskName() + ") !";
			}
		}

		taskDao.saveTask(task);
		for(TaskDevice device : taskDevices) {
			device.setTaskId(task.getTaskId());
			device.setIsCompleted(0);
			device.setServerRetryCount(0);
			device.setTaskStatus(isScheduledTask?Integer.valueOf(ACSConstants.TASK_NOT_FIRED_YET):Integer.valueOf(ACSConstants.TASK_APPLIED));
			taskDao.saveTaskDevice(device);

			TaskAttrs ta = new TaskAttrs();
			ta.setTaskId(task.getTaskId());
			ta.setDeviceId(device.getDeviceId());
			ta.setTaskAttrName(TaskAttrs.TARGET_VERSION);
			ta.setTaskAttrValue(device.getOriginalVersion() + "->" + device.getTargetVersion());
			taskDao.saveTaskAttrs(ta);

			if(!isScheduledTask) {
				//add device to thread pool,emit online upgrade concurrently
				this.doUpgradeInThreadPool(device, userId, roleId);
				runNowCount++;
			}

			successNum++;
		}

		if(successNum>0 && isScheduledTask){
			//schedule task to DB
			this.scheduleUpgradeTask(task.getTaskId(), task.getScheduled(), userId, roleId);
		}

		if(runNowCount > 0) {
			//schedule a quartz task, run once after 1 hour later, to check the upgrade status.
			this.scheduleUpgradeStatusCheckTask(task.getTaskId(), userId, roleId);
		}

		return null;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public String addSwitchUpgradeTask(TaskInfo task, List<TaskDevice> taskDevices, Locale locale, Integer userId,
									   Integer roleId) throws Exception {

		if (task.getScheduled() != null && task.getScheduled().length() > 0) {
			if (System.currentTimeMillis() > Long.valueOf(task.getScheduled())) {
				return "Scheduled time must be later than current time!";
			}
		}

		if (taskDao.isTaskNameExist(task.getTaskName()) > 0) {
			return "Task name is exist!";
		}

		if (taskDevices.size() == 0) {
			return "Device list is empty!";
		}

		for (TaskDevice device : taskDevices) {
			List<TaskDevice> list = taskDao.isDeviceInScheduledTask(device.getDeviceId());
			if (list.size() > 0) {
				return "Device (" + list.get(0).getDeviceName() + ") is in another scheduled upgrade task ("
						+ list.get(0).getTaskName() + ") !";
			}
		}

		log.warn("addSwitchUpgradeTask:" + task.toString());
		taskDao.saveTask(task);
		TaskAttrs taskAttrs = new TaskAttrs();
		taskAttrs.setTaskId(task.getTaskId());
		taskAttrs.setTaskAttrName(TaskAttrs.ICX_UPGRADE_TYPE);
		taskAttrs.setTaskAttrValue(task.getUpgradeType());
		taskDao.saveTaskAttrs(taskAttrs);

		for (TaskDevice device : taskDevices) {
			device.setTaskId(task.getTaskId());
			device.setIsCompleted(0);
			device.setServerRetryCount(0);
			device.setTaskStatus(Integer.valueOf(ACSConstants.TASK_NOT_FIRED_YET));
			taskDao.saveTaskDevice(device);

			TaskAttrs ta = new TaskAttrs();
			ta.setTaskId(task.getTaskId());
			ta.setDeviceId(device.getDeviceId());
			ta.setTaskAttrName(TaskAttrs.TARGET_VERSION);
			ta.setTaskAttrValue(device.getOriginalVersion() + "->" + device.getTargetVersion());
			taskDao.saveTaskAttrs(ta);
		}

		this.runOrScheduleSwitchUpgradeTask(task, taskDevices, userId, roleId);

		return null;
	}

	@Override
	@Transactional(rollbackFor=Exception.class)
	public String updateSwitchUpgradeTask(TaskInfo task, List<TaskDevice> taskDevices, Locale locale, Integer taskType,
										  Integer userId, Integer roleId) throws Exception {

		if(task.getScheduled()!=null && task.getScheduled().length()>0) {
			if(System.currentTimeMillis() > Long.valueOf(task.getScheduled())) {
				return "Scheduled time must be later than current time!";
			}
		}
		log.warn("updateSwitchUpgradeTask:" + task.toString());
		taskDao.updateTask(task);
		TaskAttrs taskAttrs = new TaskAttrs();
		taskAttrs.setTaskId(task.getTaskId());
		taskAttrs.setTaskAttrName(TaskAttrs.ICX_UPGRADE_TYPE);
		taskAttrs.setTaskAttrValue(task.getUpgradeType());
		taskDao.updateTaskAttrs(taskAttrs);

		for(TaskDevice device : taskDevices) {
			device.setIsCompleted(0);
			device.setServerRetryCount(0);
			device.setTaskStatus(Integer.valueOf(ACSConstants.TASK_NOT_FIRED_YET));
			taskDao.updateTaskDevice(device);

			TaskAttrs ta = new TaskAttrs();
			ta.setTaskId(task.getTaskId());
			ta.setDeviceId(device.getDeviceId());
			ta.setTaskAttrName(TaskAttrs.TARGET_VERSION);
			ta.setTaskAttrValue(device.getOriginalVersion() + "->" + device.getTargetVersion());
			taskDao.updateTaskAttrs(ta);
		}

		this.runOrScheduleSwitchUpgradeTask(task, taskDevices, userId, roleId);

		return null;
	}


	private void runOrScheduleSwitchUpgradeTask(TaskInfo task, List<TaskDevice> taskDevices, Integer userId,
												Integer roleId) throws SchedulerException, ParseException {
		if (task.getScheduled() == null || task.getScheduled().trim().isEmpty()) {
			doSwitchUpgradeTask(task, taskDevices, userId, roleId);
		} else {
			Scheduler schedulerBean = (Scheduler) FMSpringContext.getBean("schedulerFactoryDB");

			// delete exist job at first
			schedulerBean.pauseTrigger(Integer.toString(task.getTaskId()), TRIGGER_GROUP_NAME);
			schedulerBean.unscheduleJob(Integer.toString(task.getTaskId()), JOB_GROUP_NAME);
			schedulerBean.deleteJob(Integer.toString(task.getTaskId()), JOB_GROUP_NAME);

			Timestamp time = new Timestamp(Long.valueOf(task.getScheduled()));
			SimpleTrigger trigger = new SimpleTrigger();
			trigger.setName(Integer.toString(task.getTaskId()));
			trigger.setJobName(Integer.toString(task.getTaskId()));
			trigger.setStartTime(time);
			trigger.setRepeatCount(0);
			trigger.setRepeatInterval(1);
			log.info("Trying to Schedule TASK id=" + task.getTaskId() + " with time=" + time);

			JobDetail jobDetail = ((JobDetail) FMSpringContext.getBean("upgradeUnleashedJob"));
			jobDetail.setName(Integer.toString(task.getTaskId()));
			jobDetail.getJobDataMap().put("taskId", task.getTaskId());
			jobDetail.getJobDataMap().put("userId", userId);
			jobDetail.getJobDataMap().put("roleId", roleId);
			jobDetail.getJobDataMap().put("scheduledTime", task.getScheduled());
			jobDetail.getJobDataMap().put("upgradeType", task.getUpgradeType());
			jobDetail.getJobDataMap().put("type", "doUpgradeSwitch");

			schedulerBean.scheduleJob(jobDetail, trigger);
		}

	}

	public void doSwitchUpgradeTask(TaskInfo task, List<TaskDevice> taskDevices, Integer userId, Integer roleId) {
		log.warn("doSwitchUpgradeTask:" + task.toString());
		task.setIsActive(Integer.valueOf(ACSConstants.TASK_DORMANT_MASK));
		task.setIsFired(Integer.valueOf(ACSConstants.TASK_FIRED_MASK));
		updateTaskFired(task);
		boolean isSwitch = task.getUpgradeType().equals("switch") ? true : false;
		List<FirmwareQueueObject> queueObjects = taskDevices.stream().map(td -> {
			String model = dDao.getModelNameFromModelMasterByDeviceId(td.getDeviceId().longValue());
			model = model.split("-")[0];
			String imageName = switchFirmeareService.getFirmwareImageName(td.getTargetVersion(), model, isSwitch);

			FirmwareQueueObject _queueObject = new FirmwareQueueObject();
			_queueObject.setDeviceId(td.getDeviceId());
			_queueObject.setFirmwareVersion(td.getTargetVersion());
			_queueObject.setImageName(imageName);
			_queueObject.setTaskId(td.getTaskId());
			_queueObject.setModel(model);
			return _queueObject;
		}).filter(obj -> {
			return obj.getImageName() != null;
		}).collect(Collectors.toList());

		jmsSender.sendSwitchFirmwareUpgradeMessage(queueObjects);
	}

	public void doUpgradeInThreadPool(TaskDevice device, Integer userId, Integer roleId) {
		TaskServiceImpl.getThreadPool().execute(new Upgrader(device, userId, roleId));
	}

	public void checkUpgradeStatusInThreadPool(TaskDevice device, Integer userId, Integer roleId) {
		TaskServiceImpl.getThreadPool().execute(new CheckThread(device, userId, roleId));
	}

	public void fetchDeviceUpgradeProgressInThreadPool(TaskDevice device, Integer userId, Integer roleId) {
		TaskServiceImpl.getThreadPool().execute(new FetchThread(device, userId, roleId));
	}

	public void scheduleUpgradeTask(Integer taskId, String scheduledTime, Integer userId, Integer roleId) throws SchedulerException, ParseException {
		Scheduler schedulerBean = (Scheduler) FMSpringContext.getBean("schedulerFactoryDB");

		//delete exist job at first
		schedulerBean.pauseTrigger(Integer.toString(taskId), TRIGGER_GROUP_NAME);
		schedulerBean.unscheduleJob(Integer.toString(taskId), JOB_GROUP_NAME);
		schedulerBean.deleteJob(Integer.toString(taskId), JOB_GROUP_NAME);

		Timestamp time = new Timestamp(Long.valueOf(scheduledTime));
		SimpleTrigger trigger = new SimpleTrigger();
		trigger.setName(Integer.toString(taskId));
		trigger.setJobName(Integer.toString(taskId));
		trigger.setStartTime(time);
		trigger.setRepeatCount(0);
		trigger.setRepeatInterval(1);
		log.info("Trying to Schedule TASK id=" + taskId + " with time=" + time);

		JobDetail jobDetail = ((JobDetail) FMSpringContext.getBean("upgradeUnleashedJob"));
		jobDetail.setName(Integer.toString(taskId));
		jobDetail.getJobDataMap().put("taskId", taskId);
		jobDetail.getJobDataMap().put("userId", userId);
		jobDetail.getJobDataMap().put("roleId", roleId);
		jobDetail.getJobDataMap().put("scheduledTime", scheduledTime);
		jobDetail.getJobDataMap().put("type", "doUpgrade");

		schedulerBean.scheduleJob(jobDetail, trigger);
	}

	public void scheduleUpgradeStatusCheckTask(Integer taskId, Integer userId, Integer roleId) throws SchedulerException, ParseException {
		TaskServiceImpl.deleteExistCheckStateJob(taskId);

		Scheduler schedulerBean = (Scheduler) FMSpringContext.getBean("schedulerFactoryDB");
		JobDetail jobDetail = ((JobDetail) FMSpringContext.getBean("upgradeUnleashedJob"));
		jobDetail.setName(CHECK_UPGRADE_STATE_JOB_PREFIX + taskId);
		jobDetail.getJobDataMap().put("taskId", taskId);
		jobDetail.getJobDataMap().put("userId", userId);
		jobDetail.getJobDataMap().put("roleId", roleId);
		jobDetail.getJobDataMap().put("type", "checkStatus");

		SimpleTrigger trigger = new SimpleTrigger();
		trigger.setName(CHECK_UPGRADE_STATE_TRIGGER_PREFIX + taskId);
		trigger.setJobName(CHECK_UPGRADE_STATE_JOB_PREFIX + taskId);
		Long currentTime = System.currentTimeMillis();
		Date startTime = new Date(currentTime + 3600000);	//emit after 1 hour later
		trigger.setRepeatCount(0);
		trigger.setRepeatInterval(1);
		trigger.setStartTime(startTime);

		schedulerBean.scheduleJob(jobDetail, trigger);
		//schedule a task to fetch each device's real time progress
		this.scheduleDeviceUpgradeProgressFetchTask(taskId, userId, roleId);
	}

	public void scheduleDeviceUpgradeProgressFetchTask(Integer taskId, Integer userId, Integer roleId) throws SchedulerException, ParseException {
		TaskServiceImpl.deleteExistFetchProgressJob(taskId);

		Scheduler schedulerBean = (Scheduler) FMSpringContext.getBean("schedulerFactoryDB");
		JobDetail jobDetail = ((JobDetail) FMSpringContext.getBean("upgradeUnleashedJob"));
		jobDetail.setName(FETCH_DEVICE_UPGRADE_PROGRESS_JOB_PREFIX + taskId);
		jobDetail.getJobDataMap().put("taskId", taskId);
		jobDetail.getJobDataMap().put("userId", userId);
		jobDetail.getJobDataMap().put("roleId", roleId);
		jobDetail.getJobDataMap().put("type", "checkDeviceProgress");

		SimpleTrigger trigger = new SimpleTrigger();
		trigger.setName(FETCH_DEVICE_UPGRADE_PROGRESS_TRIGGER_PREFIX + taskId);
		trigger.setJobName(FETCH_DEVICE_UPGRADE_PROGRESS_JOB_PREFIX + taskId);
		Long currentTime = System.currentTimeMillis();
		Date startTime = new Date(currentTime + 5000);	//emit after 5s later
		Date endTime = new Date(currentTime + 600000);	//end after 10 minutes later
		trigger.setStartTime(startTime);
		trigger.setEndTime(endTime);
		trigger.setRepeatInterval(20000);	//20s
		trigger.setRepeatCount(28);

		schedulerBean.scheduleJob(jobDetail, trigger);
	}

	@Override
	public Map<String, List<String>> getDeviceUpgradeList(String deviceIds, Integer userId, Integer roleId) throws Exception {
		Map<String, List<String>> data = new HashMap<String, List<String>>();

		String [] idArray = deviceIds.split(",");
		try {
			for(String id : idArray) {
				Integer.parseInt(id);
			}
		} catch(RuntimeException e) {
			throw new Exception("deviceIds should be numbers which delimited by comma, eg: 1,2,3,4,5");
		}

		List<Future<Map<String, List<String>>>> futureList = new ArrayList<Future<Map<String, List<String>>>>(idArray.length);
		for(String id: idArray) {
			futureList.add(TaskServiceImpl.getCompletionService().submit(new DeviceCallable(id, userId, roleId)));
		}

		for(Future<Map<String, List<String>>> f : futureList) {
			if(f != null) {
				Map<String, List<String>> map = f.get();
				Set<String> keySet = map.keySet();
				String deviceId = (String)keySet.toArray()[0];
				data.put(deviceId, map.get(deviceId));
			}
		}

		return data;
	}

	@Transactional(rollbackFor=Exception.class)
	public Integer updateTask(TaskInfo task) {
		return taskDao.updateTask(task);
	}

	@Transactional(rollbackFor=Exception.class)
	public Integer updateZdRestoreTask(TaskInfo task) throws Exception{
		if (task.getScheduled() != null && !task.getScheduled().trim().isEmpty()) {
			task.setIsFired(0);
			Long stime=new Long(task.getScheduled());
			Timestamp schTime= new Timestamp(stime);
			Timestamp tCur = ACSUtils.getCurrentUTCTimestamp();
			if(ACSUtils.minutesBetweenTimestamps(schTime, tCur) < 1){
				throw new Exception("Difference between current time and scheduled time must not be less than a minute");
			}
			QuartzHelper.unScheduleRepeatJob( String.valueOf(task.getTaskId().intValue()), ACSConstants.QRTZ_DEFAULT_GROUP);

			QuartzHelper.scheduleTask(task.getTaskId().intValue(),new CronExpression(ACSUtils.convertUTCTimestampToLocalTimeCron(schTime)));
		}else {
			task.setIsFired(1);
		}
		return taskDao.updateTask(task);
	}

	@Transactional(rollbackFor=Exception.class)
	public Integer updateTaskFired(TaskInfo task) {
		return taskDao.updateTaskFired(task);
	}

	public void updateTaskDevice(TaskDevice device) {
		taskDao.updateTaskDevice(device);
	}

	public void appendUpgradeLog(Integer taskId, Integer deviceId, String progress) {
		String fileName = TaskServiceImpl.UPGRADE_LOGFILE_PATH + UPGRADE_LOGFILE_PREFIX + taskId +"_" + deviceId + ".txt";
		Path path = Paths.get(fileName);
		if(Files.notExists(path, LinkOption.NOFOLLOW_LINKS)) {
			try {
				Files.createFile(path);
			} catch (IOException e) {
				log.error("FetchThread -> create upgrade log file for task=" + taskId + " failed!", e);
			}
		}

		try {
			Date date = new Date(System.currentTimeMillis());
			DateFormat df= DateFormat.getDateTimeInstance();
			StringBuilder sb = new StringBuilder(TaskServiceImpl.LINE_SEPARATOR + "--------------------" + df.format(date) + "--------------------" + TaskServiceImpl.LINE_SEPARATOR);
			Files.write(path, sb.toString().getBytes(), StandardOpenOption.APPEND);
			Files.write(path, (progress + TaskServiceImpl.LINE_SEPARATOR).getBytes(), StandardOpenOption.APPEND);
		} catch (IOException e) {
			log.error("FetchThread -> append upgrade status:" + progress + " failed!", e);
		}
	}

	public static HttpHeaders getHttpHeaders(String ejsCookie) {
		HttpHeaders headers = new HttpHeaders();
		headers.add("Cookie", "-ejs-session-=" + ejsCookie);
		headers.add("X_CSRF_TOKEN", TaskServiceImpl.X_CSRF_TOKEN);
		MediaType type = MediaType.parseMediaType("application/x-www-form-urlencoded; charset=UTF-8");
		headers.setContentType(type);

		return headers;
	}

	public void sendUpgradeEvent(TaskDevice taskDevice, boolean isSuccessed) {
		EventMessage message = new EventMessage();
		EventOwner ownerDevice = new EventOwner();
		ownerDevice.setId(taskDevice.getDeviceId());
		ownerDevice.setMac(taskDevice.getMac());
		ownerDevice.setName(taskDevice.getDeviceName());
		ownerDevice.setSerialNumber(taskDevice.getSerialNumber());
		message.setDevice(ownerDevice);
		message.setOwnerType(OwnerType.ZD);

		String eventContent = null;
		if(isSuccessed) {
			eventContent = EventType.AP_IMAGE_UPGRADE_SUCCESS.getDesc() + " " + taskDevice.getMac() + "," + taskDevice.getOriginalVersion() + "," + taskDevice.getTargetVersion();
		} else {
			eventContent = EventType.AP_IMAGE_UPGRADE_FAILED.getDesc() + " " + taskDevice.getMac() + "," + taskDevice.getOriginalVersion() + "," + taskDevice.getTargetVersion();
		}
		message.setContent(eventContent);
		message.setTime(System.currentTimeMillis());

		jmsTemplate.convertAndSend(QueueDestination.DEVICE_EVENT.name(), message);
	}

	class Upgrader implements Runnable {
		private TaskDevice device;
		private Long deviceId;
		private String targetVersion;
		private Integer userId;
		private Integer roleId;

		public Upgrader(TaskDevice device, Integer userId, Integer roleId) {
			this.device = device;
			this.userId = userId;
			this.roleId = roleId;
		}

		@Override
		public void run() {
			if(device==null || device.getDeviceId()==null || userId==null || roleId==null){
				return;
			}
			if(device.getOriginalVersion()==null || device.getTargetVersion()==null) {
				return;
			}
			deviceId = device.getDeviceId().longValue();
			targetVersion = device.getTargetVersion();

			Deviceattr deviceattr = dDao.getDeviceAttr("InternetGatewayDevice.X_001392.ReadWrite_password", deviceId);
			DeviceInfo dev = dDao.getDevice(deviceId, this.userId, this.roleId);
			String password = deviceattr.getAttrValue();
			Integer port = dev.getSshTunnelPort();

			if(dev.getSoftware().equals(targetVersion)) {
				return;
			}

			try {
				String ejsCookie = null;
				for(int i=0; i<6; i++) {
					ejsCookie = ZdWebUtilService.login("127.0.0.1", Integer.valueOf(port), dev.getSerialNumber(), password);
					log.warn("Upgrader run login:" + ejsCookie);
					if(ejsCookie == null) {
						Thread.sleep(1000);
					} else {
						break;
					}
				}
				log.warn("Upgrader run cookie:" + ejsCookie);
				if (ejsCookie == null) {
					throw new Exception("Get ejs-session failed, cannot open ssh tunnel with port=" + port);
				} else {
					HttpHeaders headers = TaskServiceImpl.getHttpHeaders(ejsCookie);
					
					int x = device.getOriginalVersion().lastIndexOf(".");
					String fromBuild = device.getOriginalVersion().substring(x+1, device.getOriginalVersion().length());
					String fromVer = device.getOriginalVersion().substring(0, x);
					String setFromVerRequest = CMD_SET_FROMVERSION.replace("{build}", fromBuild).replace("{version}", fromVer);
					HttpEntity<byte[]> formEntity = new HttpEntity<byte[]>(setFromVerRequest.getBytes(), headers);
					log.debug("https://127.0.0.1:" + port + "/admin/_conf.jsp, cmd: " + setFromVerRequest);
					appendUpgradeLog(device.getTaskId(), deviceId.intValue(), "Set from version to unleashed:" + device.getOriginalVersion());
					RestTemplateBuilder.restTemplate.exchange(
							"https://127.0.0.1:" + port + "/admin/_conf.jsp",
							org.springframework.http.HttpMethod.POST, formEntity, String.class);
					
					String xmlString = TaskServiceImpl.CMD_ONLINE_UPGRADE + targetVersion + "/></ajax-request>";
					formEntity = new HttpEntity<byte[]>(xmlString.getBytes(), headers);
					log.debug("https://127.0.0.1:" + port + "/admin/_cmdstat.jsp, cmd: " + xmlString);
					appendUpgradeLog(device.getTaskId(), deviceId.intValue(), "Starting upgrade to version:" + targetVersion);

					RestTemplateBuilder.restTemplate.exchange(
							"https://127.0.0.1:" + port + "/admin/_cmdstat.jsp",
							org.springframework.http.HttpMethod.POST, formEntity, String.class);
				}
			} catch (Exception e) {
				String error = "UpgradeThread -> taskId=" + device.getTaskId() + ", deviceId=" + deviceId + ", cannot open ssh tunnel with port=" + port + ", error:" + e.getMessage();
				log.error(error, e);
				appendUpgradeLog(device.getTaskId(), deviceId.intValue(), error);

				device.setIsCompleted(Integer.valueOf(ACSConstants.TASK_COMPLETED_MASK));
				device.setTaskStatus(Integer.valueOf(ACSConstants.TASK_FAILED));
				device.setFaultSummary("Error:" + e.getMessage());
				device.setTimeStampCompleted(String.valueOf(System.currentTimeMillis()));
				taskDao.updateTaskDevice(device);

				//Send failed event
				TaskServiceImpl taskService = (TaskServiceImpl) FMSpringContext.getWebApplicationContext()
						.getBean(TaskServiceImpl.class);
				taskService.sendUpgradeEvent(device, false);
			}
		}
	}



	class CheckThread implements Runnable {
		private TaskDevice device;
		private Long deviceId;
		private String targetVersion;
		private Integer userId;
		private Integer roleId;
		private Integer port;
		private String password;
		private String serialNumber;

		public CheckThread(TaskDevice device, Integer userId, Integer roleId) {
			this.device = device;
			this.userId = userId;
			this.roleId = roleId;
		}

		private void checkUpgradeProgress() {
			try {
				String ejsCookie = null;
				for(int i=0; i<6; i++) {
					ejsCookie = ZdWebUtilService.login("127.0.0.1", Integer.valueOf(port), serialNumber, password);
					if(ejsCookie == null) {
						Thread.sleep(1000);
					} else {
						break;
					}
				}
				log.debug("CheckThread checkUpgradeStates cookie:" + ejsCookie);
				if (ejsCookie == null) {
					throw new Exception("Get ejs-session failed, cannot open ssh tunnel with port=" + port);
				} else {
					HttpHeaders headers = TaskServiceImpl.getHttpHeaders(ejsCookie);

					String xmlString = TaskServiceImpl.CMD_QUERY_UPGRADE_PROGRESS;
					HttpEntity<byte[]> formEntity = new HttpEntity<byte[]>(xmlString.getBytes(), headers);
					HttpEntity<String> result = RestTemplateBuilder.restTemplate.exchange(
							"https://127.0.0.1:" + port + "/admin/_cmdstat.jsp",
							org.springframework.http.HttpMethod.POST, formEntity, String.class);
					String response = result.getBody();
					JSONObject xmlJSONObj = XML.toJSONObject(response);
					log.debug("CheckThread -> 'unleashed_query_upgrade_progress' cmd response = " + xmlJSONObj);
					if (xmlJSONObj.has("ajax-response")) {
						JSONObject ajaxRes = xmlJSONObj.getJSONObject("ajax-response");
						if (ajaxRes.has("response")) {
							JSONObject res = ajaxRes.getJSONObject("response");
							if (res.has("xmsg")) {
								JSONObject xmsg = res.getJSONObject("xmsg");
								if(xmsg.has("msg")) {
									String msg = xmsg.getString("msg");
									if(msg.indexOf("IDLE") != -1) {
										//error happened, then check all members detail state
										this.checkUpgradeStates();
									} else {
										//upgrading, msg=ONLINE_UPGRADING_AUTO_REBOOT ?
										log.warn("checkUpgradeProgress, status=" + msg);
									}
								}
							}
						}
					} else {
						//if no response, it means device is in rebooting process
						log.warn("deviceId=" + deviceId + ", rebooting...");
					}
				}
			} catch (Exception e) {
				String error = "CheckThread -> taskId=" + device.getTaskId() + ", deviceId=" + deviceId + ", cannot open ssh tunnel with port=" + port + ", error=" + e.getMessage();
				log.error(error, e);
				appendUpgradeLog(device.getTaskId(), deviceId.intValue(), error);

				device.setIsCompleted(Integer.valueOf(ACSConstants.TASK_COMPLETED_MASK));
				device.setTaskStatus(Integer.valueOf(ACSConstants.TASK_FAILED));
				device.setFaultSummary("Error:" + e.getMessage());
				device.setTimeStampCompleted(String.valueOf(System.currentTimeMillis()));
				taskDao.updateTaskDevice(device);

				//Send failed event
				TaskServiceImpl taskService = (TaskServiceImpl) FMSpringContext.getWebApplicationContext()
						.getBean(TaskServiceImpl.class);
				taskService.sendUpgradeEvent(device, false);
			}
		}

		private void checkUpgradeStates() {
			StringBuilder sb = new StringBuilder();
			try {
				String ejsCookie = null;
				for(int i=0; i<6; i++) {
					ejsCookie = ZdWebUtilService.login("127.0.0.1", Integer.valueOf(port), serialNumber, password);
					if(ejsCookie == null) {
						Thread.sleep(1000);
					} else {
						break;
					}
				}
				log.debug("CheckThread checkUpgradeStates ejsCookie:" + ejsCookie);
				if (ejsCookie == null) {
					throw new Exception("Get ejs-session failed, cannot open ssh tunnel with port=" + port);
				} else {
					HttpHeaders headers = TaskServiceImpl.getHttpHeaders(ejsCookie);

					String xmlString = TaskServiceImpl.CMD_QUERY_UPGRADE_STATE;
					HttpEntity<byte[]> formEntity = new HttpEntity<byte[]>(xmlString.getBytes(), headers);
					HttpEntity<String> result = RestTemplateBuilder.restTemplate.exchange(
							"https://127.0.0.1:" + port + "/admin/_cmdstat.jsp",
							org.springframework.http.HttpMethod.POST, formEntity, String.class);
					String response = result.getBody();
					JSONObject xmlJSONObj = XML.toJSONObject(response);
					if (xmlJSONObj.has("ajax-response")) {
						JSONObject ajaxRes = xmlJSONObj.getJSONObject("ajax-response");
						if (ajaxRes.has("response")) {
							JSONObject res = ajaxRes.getJSONObject("response");
							if (res.has("upgrade-ap-list")) {
								JSONObject modelList = res.getJSONObject("upgrade-ap-list");
								@SuppressWarnings("unchecked")
								Iterator<String> apModels = modelList.keys();
								while(apModels.hasNext()) {
									JSONObject apModel = modelList.getJSONObject(apModels.next());
									if(apModel.has("ap") && apModel.has("ap-num")) {
										if(apModel.optInt("ap-num") > 1) {
											JSONArray apList = apModel.getJSONArray("ap");
											for(int i=0; i<apList.length(); i++) {
												JSONObject ap = apList.getJSONObject(i);
												if(ap.has("upgrade-state") && "UPGRADE_FAIL".equals(ap.getString("upgrade-state"))) {
													sb.append(ap.getString("fail-reason") + ";");
												} else {
													sb.append("time out!");
												}
											}
										} else {
											JSONObject ap = apModel.getJSONObject("ap");
											if(ap.has("upgrade-state") && "UPGRADE_FAIL".equals(ap.getString("upgrade-state"))) {
												sb.append(ap.getString("fail-reason"));
											} else {
												sb.append("time out!");
											}
										}

									}
								}
							} else {
								throw new Exception("No upgrade-ap-list xml node in returned data:" + res.toString());
							}
						}
					}

					if(sb.length() > 0) {
						throw new Exception("Upgrade failed, the reason is:" + sb);
					}
				}
			} catch (Exception e) {
				String error = "CheckThread -> taskId=" + device.getTaskId() + ", deviceId=" + deviceId + ", port=" + port + ", error:" + e.getMessage();
				log.error(error, e);
				appendUpgradeLog(device.getTaskId(), deviceId.intValue(), error);

				device.setIsCompleted(Integer.valueOf(ACSConstants.TASK_COMPLETED_MASK));
				device.setTaskStatus(Integer.valueOf(ACSConstants.TASK_FAILED));
				device.setFaultSummary("Error:" + e.getMessage());
				taskDao.updateTaskDevice(device);

				//Send failed event
				TaskServiceImpl taskService = (TaskServiceImpl) FMSpringContext.getWebApplicationContext()
						.getBean(TaskServiceImpl.class);
				taskService.sendUpgradeEvent(device, false);
			}
		}

		@Override
		public void run() {
			if(device==null || device.getDeviceId()==null || userId==null || roleId==null){
				return;
			}

			deviceId = device.getDeviceId().longValue();
			targetVersion = device.getTargetVersion();

			if(targetVersion.equals(device.getFwVersion()) && device.getIsCompleted()==ACSConstants.TASK_COMPLETED_MASK && device.getTaskStatus()==ACSConstants.TASK_SUCCESS) {
				//device was upgraded successfully
				return;
			}

			DeviceInfo dev = dDao.getDevice(deviceId, this.userId, this.roleId);
			Deviceattr deviceattr = dDao.getDeviceAttr("InternetGatewayDevice.X_001392.ReadWrite_password", deviceId);
			this.password = deviceattr.getAttrValue();
			this.port = dev.getSshTunnelPort();
			this.serialNumber = dev.getSerialNumber();

			if(dev.getSoftware().equals(targetVersion)) {
				//upgrade success
				device.setIsCompleted(Integer.valueOf(ACSConstants.TASK_COMPLETED_MASK));
				device.setTaskStatus(Integer.valueOf(ACSConstants.TASK_SUCCESS));
				device.setFaultSummary(null);
				device.setTimeStampCompleted(String.valueOf(System.currentTimeMillis()));
				taskDao.updateTaskDevice(device);

				//Send event
				TaskServiceImpl taskService = (TaskServiceImpl) FMSpringContext.getWebApplicationContext()
						.getBean(TaskServiceImpl.class);
				taskService.sendUpgradeEvent(device, true);
			} else {
				//query upgrade progress
				this.checkUpgradeProgress();
			}
		}
	}

	class FetchThread implements Runnable {
		private TaskDevice device;
		private Long deviceId;
		private String targetVersion;
		private Integer userId;
		private Integer roleId;
		private Integer port;
		private String password;
		private String serialNumber;

		public FetchThread(TaskDevice device, Integer userId, Integer roleId) {
			this.device = device;
			this.userId = userId;
			this.roleId = roleId;
		}

		private void appendUpgradeProgress(JSONObject ap, StringBuilder sb) throws JSONException {
			String apName = ap.getString("dev-name");
			String mac = ap.getString("mac");
			String progress = ap.has("progress") ? ap.getString("progress") : "";
			String state = ap.has("upgrade-state") ? ap.getString("upgrade-state") : "";
			String failreason = ap.has("fail-reason") ? ap.getString("fail-reason") : "";

			switch(state) {
				case "ACTIVE_UPGRADED":
				case "BACKUP_UPGRADED":
				case "REBOOTING":
					sb.append(apName + "(" + mac + "): Rebooting$$");
					break;
				case "UPGRADE_FAIL":
					sb.append(apName + "(" + mac + "): Failed, reason: " + failreason + "$$");
					break;
				case "DOWNLOADING":
					sb.append(apName + "(" + mac + "): Downloading, " + progress + "%$$");
					break;
				case "FLASHING":
					sb.append(apName + "(" + mac + "): Flashing, " + progress + "%$$");
					break;
				case "UPGRADE_DISPATCHING":
					sb.append(apName + "(" + mac + "): Dispatching$$");
					break;
				default:
					sb.append(apName + "(" + mac + "): Waiting$$");
					break;
			}
		}

		private void checkUpgradeStates() {
			StringBuilder sb = new StringBuilder();
			try {
				String ejsCookie = null;
				for(int i=0; i<6; i++) {
					ejsCookie = ZdWebUtilService.login("127.0.0.1", Integer.valueOf(port), serialNumber, password);
					log.debug("FetchThread checkUpgradeStates login:" + ejsCookie);
					if(ejsCookie == null) {
						Thread.sleep(1000);
					} else {
						break;
					}
				}
				log.debug("FetchThread checkUpgradeStates ejsCookie:" + ejsCookie);
				if (ejsCookie == null) {
					throw new Exception("Get ejs-session failed, cannot open ssh tunnel with port=" + port);
				} else {
					HttpHeaders headers = TaskServiceImpl.getHttpHeaders(ejsCookie);

					String xmlString = TaskServiceImpl.CMD_QUERY_UPGRADE_STATE;
					HttpEntity<byte[]> formEntity = new HttpEntity<byte[]>(xmlString.getBytes(), headers);
					HttpEntity<String> result = RestTemplateBuilder.restTemplate.exchange(
							"https://127.0.0.1:" + port + "/admin/_cmdstat.jsp",
							org.springframework.http.HttpMethod.POST, formEntity, String.class);
					String response = result.getBody();
					JSONObject xmlJSONObj = XML.toJSONObject(response);
					if (xmlJSONObj.has("ajax-response")) {
						JSONObject ajaxRes = xmlJSONObj.getJSONObject("ajax-response");
						if (ajaxRes.has("response")) {
							JSONObject res = ajaxRes.getJSONObject("response");
							if (res.has("upgrade-ap-list")) {
								JSONObject modelList = res.getJSONObject("upgrade-ap-list");
								//append to upgrade log file
								ITaskService taskService = (TaskServiceImpl) FMSpringContext.getWebApplicationContext()
										.getBean(TaskServiceImpl.class);
								taskService.appendUpgradeLog(device.getTaskId(), device.getDeviceId(), modelList.toString());

								@SuppressWarnings("unchecked")
								Iterator<String> apModels = modelList.keys();
								while(apModels.hasNext()) {
									JSONObject apModel = modelList.getJSONObject(apModels.next());

									if(apModel.has("ap") && apModel.has("ap-num")) {
										if(apModel.optInt("ap-num") > 1) {
											JSONArray apList = apModel.getJSONArray("ap");
											for(int i=0; i<apList.length(); i++) {
												JSONObject ap = apList.getJSONObject(i);
												this.appendUpgradeProgress(ap, sb);
											}
										} else {
											JSONObject ap = apModel.getJSONObject("ap");
											this.appendUpgradeProgress(ap, sb);
										}
									}
								}//end of while
							} else {
								throw new Exception("No upgrade-ap-list xml node in returned data:" + res.toString());
							}
						}
					}

					TaskAttrs ta = new TaskAttrs();
					ta.setTaskId(device.getTaskId());
					ta.setDeviceId(device.getDeviceId());
					ta.setTaskValueDisplay(sb.toString());
					taskDao.updateTaskAttrsProgress(ta);
				}
			} catch (Exception e) {
				String error = "FetchThread -> taskId=" + device.getTaskId() + ", deviceId=" + deviceId + ", port=" + port + ", error:" + e.getMessage();
				log.error(error, e);
			}
		}

		@Override
		public void run() {
			if(device==null || device.getDeviceId()==null || userId==null || roleId==null){
				return;
			}

			deviceId = device.getDeviceId().longValue();
			targetVersion = device.getTargetVersion();

			if(targetVersion.equals(device.getFwVersion()) && device.getIsCompleted()==ACSConstants.TASK_COMPLETED_MASK && device.getTaskStatus()==ACSConstants.TASK_SUCCESS) {
				//device was upgraded successfully
				return;
			}

			DeviceInfo dev = dDao.getDevice(deviceId, this.userId, this.roleId);
			Deviceattr deviceattr = dDao.getDeviceAttr("InternetGatewayDevice.X_001392.ReadWrite_password", deviceId);
			this.password = deviceattr.getAttrValue();
			this.port = dev.getSshTunnelPort();
			this.serialNumber = dev.getSerialNumber();

			if(dev.getSoftware().equals(targetVersion)) {
				//upgrade success, delete schedule
				device.setIsCompleted(Integer.valueOf(ACSConstants.TASK_COMPLETED_MASK));
				device.setTaskStatus(Integer.valueOf(ACSConstants.TASK_SUCCESS));
				device.setFaultSummary(null);
				device.setTimeStampCompleted(String.valueOf(System.currentTimeMillis()));
				taskDao.updateTaskDevice(device);
				//Send event
				TaskServiceImpl taskService = (TaskServiceImpl) FMSpringContext.getWebApplicationContext()
						.getBean(TaskServiceImpl.class);
				taskService.sendUpgradeEvent(device, true);
			} else {
				//query upgrade progress
				this.checkUpgradeStates();
			}
		}
	}




	class DeviceCallable implements Callable<Map<String, List<String>>> {
		private String deviceId;
		private Integer userId;
		private Integer roleId;
		private boolean success = true;
		private Map<String, List<String>> data;

		public DeviceCallable(String deviceId, Integer userId, Integer roleId) {
			this.deviceId = deviceId;
			this.userId = userId;
			this.roleId = roleId;
			data = new HashMap<String, List<String>>();
		}

		private boolean getVersionList(List<String> versionList, Integer port, HttpHeaders headers) throws JSONException {
			HttpEntity<byte[]> queryUpdatesInfoCmd = new HttpEntity<byte[]>(TaskServiceImpl.CMD_QUERY_UPDATES_INFO.getBytes(), headers);
			HttpEntity<String> result = RestTemplateBuilder.restTemplate.exchange(
					"https://127.0.0.1:" + port + "/admin/_cmdstat.jsp",
					org.springframework.http.HttpMethod.POST, queryUpdatesInfoCmd, String.class);
			String response = result.getBody();
			JSONObject xmlJSONObj = XML.toJSONObject(response);
			log.warn("getVersionList response:" + xmlJSONObj.toString());
			if (xmlJSONObj.has("ajax-response")) {
				JSONObject ajaxRes = xmlJSONObj.getJSONObject("ajax-response");
				if (ajaxRes.has("response")) {
					JSONObject res = ajaxRes.getJSONObject("response");
					if(res.has("updates")) {
						JSONObject updates = res.getJSONObject("updates");
						if(updates.optJSONArray("version") != null) {
							JSONArray imgs = updates.getJSONArray("version");
							if(imgs.length()>0) {
								for(int i=0; i<imgs.length(); i++) {
									JSONObject o = imgs.getJSONObject(i);
									versionList.add(o.getString("number"));
								}
							}
						} else if(updates.has("version")) {
							JSONObject version = updates.getJSONObject("version");
							versionList.add(version.getString("number"));
						} else {
							versionList.add(IS_LATEST_VERSION);
						}
					} else {
						return false;
					}
				} else {
					return false;
				}
			} else {
				return false;
			}
			return true;
		}

		@Override
		public Map<String, List<String>> call() throws Exception {
			List<String> versionList = new ArrayList<>();

			Deviceattr deviceattr = dDao.getDeviceAttr("InternetGatewayDevice.X_001392.ReadWrite_password", Long.valueOf(deviceId));
			DeviceInfo dev = dDao.getDevice(Long.valueOf(deviceId), userId, roleId);
			if(deviceattr==null || dev==null) {
				throw new Exception("Device cannot be accessed, deviceId=" + deviceId);
			}
			String password = deviceattr.getAttrValue();
			Integer port = dev.getSshTunnelPort();
			String serialNumber = dev.getSerialNumber();

			try {
				String ejsCookie = null;
				for(int i=0; i<6; i++) {
					ejsCookie = ZdWebUtilService.login("127.0.0.1", Integer.valueOf(port), dev.getSerialNumber(), password);
					log.warn("DeviceCallable call login "+ deviceId  + "-" + port + " -" + serialNumber + " :" + ejsCookie);
					if(ejsCookie == null) {
						Thread.sleep(3000);
					} else {
						break;
					}
				}
				log.warn("DeviceCallable call ejsCookie "+ deviceId  + "-" + port + " -" + serialNumber + " :" + ejsCookie);
				if (ejsCookie == null) {
					throw new Exception("Get ejs-session failed, cannot open ssh tunnel with port=" + port);
				} else {
					try {
						log.warn("Get versionList 2s...");
						Thread.sleep(2000);
					} catch (Exception e) {
						log.error("Get versionList 2s:", e);
					}
					HttpHeaders headers = TaskServiceImpl.getHttpHeaders(ejsCookie);

					HttpEntity<byte[]> findUpdatesCmd = new HttpEntity<byte[]>(TaskServiceImpl.CMD_FIND_UPDATES.getBytes(), headers);
					RestTemplateBuilder.restTemplate.exchange(
							"https://127.0.0.1:" + port + "/admin/_cmdstat.jsp",
							org.springframework.http.HttpMethod.POST, findUpdatesCmd, String.class);
					HttpEntity<byte[]> getUpgradeImgCmd = new HttpEntity<byte[]>(TaskServiceImpl.CMD_GET_UPGRADE_IMAGE_INFO.getBytes(), headers);
					RestTemplateBuilder.restTemplate.exchange(
							"https://127.0.0.1:" + port + "/admin/_cmdstat.jsp",
							org.springframework.http.HttpMethod.POST, getUpgradeImgCmd, String.class);
					
					//try 20 times
					for(int i=0; i<20; i++) {
						Thread.sleep(1000);
						ejsCookie = ZdWebUtilService.login("127.0.0.1", Integer.valueOf(port), dev.getSerialNumber(), password);
						log.warn("DeviceCallable call-" + (i+1) + " login "+ deviceId  + "-" + port + " -" + serialNumber + " :" + ejsCookie);
						headers = TaskServiceImpl.getHttpHeaders(ejsCookie);
						success = this.getVersionList(versionList, port, headers);
						if(success) {
							break;
						}
					}

					if(!success) {
						throw new Exception(TaskServiceImpl.CMD_QUERY_UPDATES_INFO + "cmd failed!");
					}
				}
			}catch(Exception e) {
				versionList.add("Exception");
				versionList.add("Get upgrade version list failed on device=" + deviceId + ", reason:" + e.getMessage());
			}

			data.put(deviceId, versionList);
			return data;
		}
	}

	public static void deleteExistCheckStateJob(Integer taskId) {
		Scheduler schedulerBean = (Scheduler) FMSpringContext.getBean("schedulerFactoryDB");

		try {
			schedulerBean.pauseTrigger(TaskServiceImpl.CHECK_UPGRADE_STATE_TRIGGER_PREFIX+taskId, TaskServiceImpl.TRIGGER_GROUP_NAME);
			schedulerBean.unscheduleJob(TaskServiceImpl.CHECK_UPGRADE_STATE_JOB_PREFIX+taskId, TaskServiceImpl.JOB_GROUP_NAME);
			schedulerBean.deleteJob(TaskServiceImpl.CHECK_UPGRADE_STATE_JOB_PREFIX+taskId, TaskServiceImpl.JOB_GROUP_NAME);
		} catch (SchedulerException e) {
			log.error("Remove " + TaskServiceImpl.CHECK_UPGRADE_STATE_JOB_PREFIX+taskId + " failed!", e);
		}
	}

	public static void deleteExistFetchProgressJob(Integer taskId) {
		Scheduler schedulerBean = (Scheduler) FMSpringContext.getBean("schedulerFactoryDB");

		try {
			schedulerBean.pauseTrigger(TaskServiceImpl.FETCH_DEVICE_UPGRADE_PROGRESS_TRIGGER_PREFIX+taskId, TaskServiceImpl.TRIGGER_GROUP_NAME);
			schedulerBean.unscheduleJob(TaskServiceImpl.FETCH_DEVICE_UPGRADE_PROGRESS_JOB_PREFIX+taskId, TaskServiceImpl.JOB_GROUP_NAME);
			schedulerBean.deleteJob(TaskServiceImpl.FETCH_DEVICE_UPGRADE_PROGRESS_JOB_PREFIX+taskId, TaskServiceImpl.JOB_GROUP_NAME);
		} catch (SchedulerException e) {
			log.error("Remove " + TaskServiceImpl.FETCH_DEVICE_UPGRADE_PROGRESS_JOB_PREFIX+taskId + " failed!", e);
		}
	}

}

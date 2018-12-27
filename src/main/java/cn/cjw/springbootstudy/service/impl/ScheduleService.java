package cn.cjw.springbootstudy.service.impl;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class ScheduleService {
	
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

	@Scheduled(fixedRate=5000)
	public void reportCurrentTime() {
		String time = dateFormat.format(new Date());
//		System.out.println("现在时间：" + time);
	}
}

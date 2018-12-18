package cn.cjw.springbootstudy.listener;

import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ApplicationStartedEventListener implements ApplicationListener<ApplicationStartedEvent>{@Override
	public void onApplicationEvent(ApplicationStartedEvent event) {
		log.info("......ApplicationStartedEvent......");
	}

}

package cn.cjw.springbootstudy;
import org.springframework.beans.BeansException;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import cn.cjw.springbootstudy.model.BlogProperties;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootApplication
@EnableScheduling
@EnableAsync
public class SpringbootStudyApplication implements ApplicationContextAware{
	
//	@Autowired
//	private BlogProperties blogProps;
	
	private static ApplicationContext context;

	public static void main(String[] args) {
		SpringApplication.run(SpringbootStudyApplication.class, args);
	}
	
	@Bean
	public DataLoader dataLoader() {
		return new DataLoader();
	}
	
	class DataLoader implements CommandLineRunner {

		@Override
		public void run(String... args) throws Exception {
			log.info("Loading data.....");
			BlogProperties blogProps = SpringbootStudyApplication.getBean(BlogProperties.class);
			log.info(blogProps.getDesc());
		}
		
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		setContext(applicationContext);
	}

	public static ApplicationContext getContext() {
		return context;
	}

	public static void setContext(ApplicationContext context) {
		SpringbootStudyApplication.context = context;
	}
	
	public static <T> T getBean(Class<T> className) {
		return context.getBean(className);
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T getBean(String className) {
		return (T)context.getBean(className);
	}
}

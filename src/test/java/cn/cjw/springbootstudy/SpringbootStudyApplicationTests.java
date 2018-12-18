package cn.cjw.springbootstudy;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class SpringbootStudyApplicationTests {

	@Test
	public void contextLoads() throws UnknownHostException {
		InetAddress addr = InetAddress.getLocalHost();
		log.info(addr.getHostAddress());
	}

}

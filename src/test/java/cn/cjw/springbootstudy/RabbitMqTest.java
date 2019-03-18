package cn.cjw.springbootstudy;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import cn.cjw.springbootstudy.service.Sender;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RabbitMqTest {
	
	@Autowired
    private Sender sender;

    @Test
    public void hello() throws Exception {
    	for(int i=0; i<Integer.MAX_VALUE; i++) {
    		sender.send(i);
    		Thread.sleep(3000);
    	}
        
    }

}

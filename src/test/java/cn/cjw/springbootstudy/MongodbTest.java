package cn.cjw.springbootstudy;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import cn.cjw.springbootstudy.model.User;
import cn.cjw.springbootstudy.service.UserMongoRepository;


@RunWith(SpringRunner.class)
@SpringBootTest
public class MongodbTest {
	
	@Autowired
	private UserMongoRepository userRepository;
	
	@Before
	public void setUp() {
		userRepository.deleteAll();
	}
	
	@Test
	public void testUser() {
		User user = new User(1L, "zhangshan", 34);
		userRepository.save(user);
		
		user = new User(2L, "lishi", 14);
		userRepository.save(user);
		
		user = new User(3L, "wangwu", 24);
		userRepository.save(user);
		
		Assert.assertEquals(3, userRepository.findAll().size());
		
		user = userRepository.findById(1L).get();
		Assert.assertEquals("zhangshan", user.getName());
		
	}
}

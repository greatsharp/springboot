package cn.cjw.springbootstudy;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;

import cn.cjw.springbootstudy.model.User;
import cn.cjw.springbootstudy.service.UserMapper;


@RunWith(SpringRunner.class)
@SpringBootTest
public class MybatisTest {
	
	@Autowired
	private UserMapper userMapper;
	
	@Before
	public void setup() {
		userMapper.deleteAll();
	}
	
	@Test
	@Rollback
	public void testUser() {
		User user = new User();
		user.setName("zhangshan");
		user.setAge(10);
		userMapper.insert(user);
		
		user = userMapper.findByName("zhangshan");
		Assert.assertEquals(10, user.getAge().longValue());
	}
}

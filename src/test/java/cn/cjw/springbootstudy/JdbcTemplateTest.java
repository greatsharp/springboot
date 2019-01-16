package cn.cjw.springbootstudy;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import cn.cjw.springbootstudy.model.User;
import cn.cjw.springbootstudy.service.UserService;

@RunWith(SpringRunner.class)
@SpringBootTest
public class JdbcTemplateTest {
	
	@Autowired
	private CacheManager cacheManager;

	@Autowired
	private UserService userService;
	
	@Test
	public void test() {
		Cache userCache = cacheManager.getCache("users");
		User user = new User();
		user.setName("zhangshan");
		user.setAge(10);
		userService.createUser(user);
		
		List<User> list = userService.getAllUser();
		list.stream().forEach(u -> System.out.println(u.getId() + "=" + u.getName() + "," + u.getAge()));
		userCache.put("userList", list);
		
		list = userService.getAllUser();
		list.stream().forEach(u -> System.out.println(u.getId() + "=" + u.getName() + "," + u.getAge()));
		
		list = (List<User>)(userCache.get("userList"));
		list.stream().forEach(u -> System.out.println(u.getId() + "=" + u.getName() + "," + u.getAge()));
	}
	
	@Transactional
	@Test
	public void testRollback() {
		User user = new User();
		user.setName("no name");
		//age is null, create will be rollback
		userService.createUser(user);
	}
}

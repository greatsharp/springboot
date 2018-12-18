package cn.cjw.springbootstudy;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import cn.cjw.springbootstudy.model.User;


@RunWith(SpringRunner.class)
@SpringBootTest
public class RedisTest {
	
	@Autowired
	private StringRedisTemplate stringRedisTemplate;
	
	@Autowired
	private RedisTemplate<String, Object> redisTemplate;
	
	@Test
	public void testString() {
		stringRedisTemplate.opsForValue().set("name", "rapheal");
		Assert.assertEquals("rapheal", stringRedisTemplate.opsForValue().get("name"));
	}
	
	@Test
	public void testUser() {
		User user = new User(1L, "zhangshan", 34);
		redisTemplate.opsForValue().set(user.getName(), user);
		
		user = new User(2L, "lishi", 14);
		redisTemplate.opsForValue().set(user.getName(), user);
		
		user = new User(3L, "wangwu", 24);
		redisTemplate.opsForValue().set(user.getName(), user);
		
		user = (User)redisTemplate.opsForValue().get("zhangshan");
		Assert.assertEquals(34, user.getAge().longValue());
		user = (User)redisTemplate.opsForValue().get("lishi");
		Assert.assertEquals(14, user.getAge().longValue());
		user = (User)redisTemplate.opsForValue().get("wangwu");
		Assert.assertEquals(24, user.getAge().longValue());
	}
}

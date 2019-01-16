package cn.cjw.springbootstudy.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import cn.cjw.springbootstudy.model.User;
import cn.cjw.springbootstudy.service.UserService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@CacheConfig(cacheNames = "users")
@Service
public class UserServiceImpl implements UserService {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Override
	public void createUser(User user) {
		jdbcTemplate.update("insert into user(name,age) values(?,?)", user.getName(), user.getAge());
	}

	@Override
	public void deleteUser(Long id) {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateUser(User user) {
		// TODO Auto-generated method stub

	}

	@Override
	public User getUserbyId(Long id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Cacheable
	@Override
	public List<User> getAllUser() {
		List<User> result = new ArrayList<User>();
		String sql = "select id, name, age from user order by id";
		log.warn(sql);
		result = jdbcTemplate.query(sql, new BeanPropertyRowMapper<User>(User.class));
//		result = jdbcTemplate.query(sql, new RowMapper<User>() {
//
//			@Override
//			public User mapRow(ResultSet rs, int rowNum) throws SQLException {
//				User user = new User();
//				user.setId(rs.getLong("id"));
//				user.setName(rs.getString("name"));
//				user.setAge(rs.getInt("age"));
//				
//				return user;
//			}
//			
//		});
		return result;
	}

}

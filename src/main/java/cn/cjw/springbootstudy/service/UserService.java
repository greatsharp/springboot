package cn.cjw.springbootstudy.service;

import java.util.List;

import cn.cjw.springbootstudy.model.User;

public interface UserService {
	
	void createUser(User user);
	
	void deleteUser(Long id);
	
	void updateUser(User user);
	
	User getUserbyId(Long id);

	List<User> getAllUser();
}

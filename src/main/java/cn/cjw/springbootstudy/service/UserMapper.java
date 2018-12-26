package cn.cjw.springbootstudy.service;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import cn.cjw.springbootstudy.model.User;

@Mapper
public interface UserMapper {

	@Select("select * from user where name=#{name}")
	User findByName(@Param("name") String name);
	
	@Insert("insert into user(name, age) values(#{name}, #{age})")
	int insert(User user);
	
	@Delete("delete from user")
	int deleteAll();
}

package cn.cjw.springbootstudy.service;

import org.springframework.data.mongodb.repository.MongoRepository;

import cn.cjw.springbootstudy.model.User;

public interface UserMongoRepository extends MongoRepository<User, Long>{

	User findByName(String name);
}

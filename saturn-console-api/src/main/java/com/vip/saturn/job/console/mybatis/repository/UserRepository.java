package com.vip.saturn.job.console.mybatis.repository;

import com.vip.saturn.job.console.mybatis.entity.User;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author hebelala
 */
@Repository
public interface UserRepository {

	int insert(User user);

	int update(User user);

	List<User> selectAll();

	User select(String name);

	User selectWithNotFilterDeleted(String name);

}

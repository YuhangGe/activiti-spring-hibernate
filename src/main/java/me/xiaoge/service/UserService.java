package me.xiaoge.service;

import me.xiaoge.model.User;
import me.xiaoge.web.UserCommand;
import me.xiaoge.web.UserGrid;

public interface UserService {
	
	User get(Long id);
	
	void save(UserCommand userCommand);
	
	void delete(User user);
	
	UserGrid findAll();
	
	void saveAll(UserGrid userGrid);

	void updateWithAll(UserGrid userGrid);
	
}

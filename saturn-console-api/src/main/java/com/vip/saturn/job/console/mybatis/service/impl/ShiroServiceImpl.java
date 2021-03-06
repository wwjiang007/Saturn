package com.vip.saturn.job.console.mybatis.service.impl;

import com.vip.saturn.job.console.exception.SaturnJobConsoleException;
import com.vip.saturn.job.console.mybatis.entity.*;
import com.vip.saturn.job.console.mybatis.repository.*;
import com.vip.saturn.job.console.mybatis.service.ShiroService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * @author hebelala
 */
@Service
public class ShiroServiceImpl implements ShiroService {

	@Autowired
	private PermissionRepository permissionRepository;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private RolePermissionRepository rolePermissionRepository;

	@Autowired
	private UserRoleRepository userRoleRepository;

	private String superRoleKey = "super";

	@Transactional(rollbackFor = Exception.class)
	@Override
	public void addUserRole(UserRole userRole) throws SaturnJobConsoleException {
		// add user first if not exists
		User user = userRepository.selectWithNotFilterDeleted(userRole.getUserName());
		if (user == null) {
			userRepository.insert(userRole.getUser());
		} else {
			userRepository.update(user);
		}
		// check role is existing
		String roleKey = userRole.getRoleKey();
		Role role = roleRepository.selectByKey(roleKey);
		if (role == null) {
			throw new SaturnJobConsoleException(String.format("该角色key(%s)不存在", roleKey));
		}
		// insert or update userRole
		UserRole pre = userRoleRepository.selectWithNotFilterDeleted(userRole);
		if (pre == null) {
			userRoleRepository.insert(userRole);
		} else {
			userRoleRepository.update(pre, userRole);
		}
	}

	@Transactional(rollbackFor = Exception.class)
	@Override
	public void deleteUserRole(UserRole userRole) throws SaturnJobConsoleException {
		userRoleRepository.delete(userRole);
	}

	@Transactional(rollbackFor = Exception.class)
	@Override
	public void updateUserRole(UserRole pre, UserRole cur) throws SaturnJobConsoleException {
		userRoleRepository.update(pre, cur);
	}

	@Transactional(readOnly = true)
	@Override
	public List<User> getAllUsers() throws SaturnJobConsoleException {
		List<User> allUsers = new ArrayList<>();
		List<User> users = userRepository.selectAll();
		if (users != null) {
			for (User user : users) {
				allUsers.add(getUser(user.getName()));
			}
		}
		return allUsers;
	}

	@Transactional(readOnly = true)
	@Override
	public User getUser(String userName) throws SaturnJobConsoleException {
		User user = userRepository.select(userName);
		//TODO user maybe is null
		List<UserRole> userRoles = userRoleRepository.selectByUserName(userName);
		user.setUserRoles(userRoles);
		if (userRoles == null) {
			return user;
		}
		for (UserRole userRole : userRoles) {
			String roleKey = userRole.getRoleKey();
			Role role = roleRepository.selectByKey(roleKey);
			userRole.setRole(role);
			if (role == null) {
				continue;
			}
			List<RolePermission> rolePermissions = rolePermissionRepository.selectByRoleKey(roleKey);
			role.setRolePermissions(rolePermissions);
			if (rolePermissions == null) {
				continue;
			}
			for (RolePermission rolePermission : rolePermissions) {
				Permission permission = permissionRepository.selectByKey(rolePermission.getPermissionKey());
				rolePermission.setPermission(permission);
			}
		}
		return user;
	}

	@Transactional(readOnly = true)
	@Override
	public List<User> getSupers() throws SaturnJobConsoleException {
		List<User> superUsers = new ArrayList<>();
		List<UserRole> userRoles = userRoleRepository.selectByRoleKey(superRoleKey);
		if (userRoles == null) {
			return superUsers;
		}
		for (UserRole userRole : userRoles) {
			User user = userRepository.select(userRole.getUserName());
			superUsers.add(user);
		}
		return superUsers;
	}

	@Override
	public Role getSuperRole() throws SaturnJobConsoleException {
		Role role = roleRepository.selectByKey(superRoleKey);
		if (role == null) {
			return null;
		}
		List<RolePermission> rolePermissions = rolePermissionRepository.selectByRoleKey(superRoleKey);
		if (rolePermissions != null) {
			for (RolePermission rolePermission : rolePermissions) {
				Permission permission = permissionRepository.selectByKey(rolePermission.getPermissionKey());
				rolePermission.setPermission(permission);
			}
		}
		role.setRolePermissions(rolePermissions);
		return role;
	}
}

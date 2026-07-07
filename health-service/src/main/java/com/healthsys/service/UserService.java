package com.healthsys.service;

import com.healthsys.dao.UserDAO;
import com.healthsys.dao.DoctorDAO;
import com.healthsys.common.entity.Users;
import com.healthsys.common.entity.Doctor;

import java.util.List;

public class UserService {
    private UserDAO userDAO;
    private DoctorDAO doctorDAO;

    public UserService() {
        this.userDAO = new UserDAO();
        this.doctorDAO = new DoctorDAO();
    }

    public boolean updateUserProfile(Users user) {
        return userDAO.updateUserProfile(user);
    }

    // 旧方法：获取所有医护人员 → 改为查doctors表
    public List<Doctor> getAllDoctors() {
        return doctorDAO.getAll();
    }

    public boolean resetUserPassword(Long userId) {
        return userDAO.updateUserPassword(userId, UserDAO.INITIAL_PASSWORD);
    }

    // 兼容旧代码（返回类型变了，但旧调用者需要调整）
    public List<Users> getAllMedicalUsers() {
        // 新架构下医护人员在doctors表，返回空列表保持编译兼容
        return List.of();
    }
}

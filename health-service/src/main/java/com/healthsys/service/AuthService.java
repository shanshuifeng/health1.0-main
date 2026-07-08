package com.healthsys.service;

import com.healthsys.dao.UserDAO;
import com.healthsys.dao.AdminDAO;
import com.healthsys.dao.DoctorDAO;
import com.healthsys.common.entity.Users;
import com.healthsys.common.entity.Admin;
import com.healthsys.common.entity.Doctor;

public class AuthService {
    public interface LoginListener {
        void onLoginSuccess(Users user);
        void onFirstLogin(Users user);
        void onLoginFailed(String errorMessage);
        void onPasswordChangeSuccess(Users user);
        void onPasswordChangeFailed(String errorMessage);
    }

    private LoginListener loginListener;

    public void setLoginListener(LoginListener listener) {
        this.loginListener = listener;
    }

    // ==================== 普通用户登录 ====================

    public void handleLogin(String phone, String password) {
        if (phone.isEmpty() || password.isEmpty()) {
            fail("手机号和密码不能为空");
            return;
        }

        UserDAO userDAO = new UserDAO();
        Users user = userDAO.getUserByPhone(phone);
        if (user == null) { fail("手机号未注册"); return; }
        if (!password.equals(user.getPasswordHash())) { fail("密码错误"); return; }

        if (user.isFirstLogin()) {
            if (loginListener != null) loginListener.onFirstLogin(user);
        } else {
            if (loginListener != null) loginListener.onLoginSuccess(user);
        }
    }

    // ==================== 管理员登录 ====================

    public void handleAdminLogin(String username, String password) {
        if (username.isEmpty() || password.isEmpty()) { fail("用户名和密码不能为空"); return; }

        AdminDAO dao = new AdminDAO();
        Admin admin = dao.getByUsername(username);
        if (admin == null) { fail("管理员账号不存在"); return; }
        if (!password.equals(admin.getPasswordHash())) { fail("管理员密码错误"); return; }

        Users u = toVirtualUser(admin.getAdminId(), admin.getRealName(), admin.getPhone());
        if (loginListener != null) loginListener.onLoginSuccess(u);
    }

    // ==================== 医生登录 ====================

    public void handleDoctorLogin(String username, String password) {
        if (username.isEmpty() || password.isEmpty()) { fail("用户名和密码不能为空"); return; }

        DoctorDAO dao = new DoctorDAO();
        Doctor doctor = dao.getByUsername(username);
        if (doctor == null) { fail("医生账号不存在"); return; }
        if (!password.equals(doctor.getPasswordHash())) { fail("医生密码错误"); return; }

        Users u = toVirtualUser(doctor.getDoctorId(), doctor.getName(), null);
        if (loginListener != null) loginListener.onLoginSuccess(u);
    }

    // ==================== 改密 ====================

    public void handleChangePassword(Users user, String newPassword, String confirmPassword) {
        if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
            if (loginListener != null) loginListener.onPasswordChangeFailed("密码不能为空");
            return;
        }
        if (!newPassword.equals(confirmPassword)) {
            if (loginListener != null) loginListener.onPasswordChangeFailed("两次输入的密码不一致");
            return;
        }
        if (newPassword.length() < 6) {
            if (loginListener != null) loginListener.onPasswordChangeFailed("密码长度不能少于6位");
            return;
        }
        UserDAO userDAO = new UserDAO();
        if (userDAO.updateUserPassword(user.getId(), newPassword)) {
            if (loginListener != null) loginListener.onPasswordChangeSuccess(user);
        } else {
            if (loginListener != null) loginListener.onPasswordChangeFailed("密码修改失败");
        }
    }

    // ==================== Helpers ====================

    private void fail(String msg) {
        if (loginListener != null) loginListener.onLoginFailed(msg);
    }

    private Users toVirtualUser(Long id, String name, String phone) {
        Users u = new Users();
        u.setUserId(id);
        u.setRealName(name);
        u.setPhone(phone != null ? phone : "");
        u.setFirstLogin(false);
        u.setRole("MEDICAL");  // 标记为医护角色，用于 LoginView 路由
        return u;
    }
}

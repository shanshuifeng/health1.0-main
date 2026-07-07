package com.healthsys.service;

import com.healthsys.dao.UserDAO;
import com.healthsys.common.entity.Users;
import com.healthsys.common.util.EncryptUtil;

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

    public void handleLogin(String phone, String password) {
        if (phone.isEmpty() || password.isEmpty()) {
            if (loginListener != null) {
                loginListener.onLoginFailed("手机号和密码不能为空");
            }
            return;
        }

        UserDAO userDAO = new UserDAO();
        Users user = userDAO.getUserByPhone(phone);

        if (user == null || !EncryptUtil.decrypt(user.getPassword()).equals(password)) {
            if (loginListener != null) {
                loginListener.onLoginFailed("手机号或密码错误");
            }
            return;
        }

        if (user.isFirstLogin()) {
            if (loginListener != null) {
                loginListener.onFirstLogin(user);
            }
        } else {
            if (loginListener != null) {
                loginListener.onLoginSuccess(user);
            }
        }
    }

    public void handleChangePassword(Users user, String newPassword, String confirmPassword) {
        if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
            if (loginListener != null) {
                loginListener.onPasswordChangeFailed("密码不能为空");
            }
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            if (loginListener != null) {
                loginListener.onPasswordChangeFailed("两次输入的密码不一致");
            }
            return;
        }

        if (newPassword.length() < 6) {
            if (loginListener != null) {
                loginListener.onPasswordChangeFailed("密码长度不能少于6位");
            }
            return;
        }

        UserDAO userDAO = new UserDAO();
        if (userDAO.updateUserPassword(user.getId(), newPassword)) {
            if (loginListener != null) {
                loginListener.onPasswordChangeSuccess(user);
            }
        } else {
            if (loginListener != null) {
                loginListener.onPasswordChangeFailed("密码修改失败");
            }
        }
    }
}

package com.healthsys.service;

import com.healthsys.dao.AppointmentDAO;
import com.healthsys.dao.CheckItemDAO;
import com.healthsys.dao.CheckItemGroupDAO;
import com.healthsys.common.entity.Appointment;
import com.healthsys.common.entity.CheckItem;
import com.healthsys.common.entity.CheckItemGroup;
import com.healthsys.common.entity.Users;
import java.util.List;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class AppointmentService {
    private AppointmentDAO appointmentDAO;
    private CheckItemGroupDAO checkItemGroupDAO;
    private CheckItemDAO checkItemDAO;

    public AppointmentService() {
        this.appointmentDAO = new AppointmentDAO();
        this.checkItemGroupDAO = new CheckItemGroupDAO();
        this.checkItemDAO = new CheckItemDAO();
    }

    public Double getAppointmentPrice(Long appointmentId) {
        Appointment appointment = appointmentDAO.getAppointmentById(appointmentId);
        if (appointment == null) return null;
        Long groupId = appointment.getGroupId();
        if (groupId == null) return 0.0;
        CheckItemGroup group = checkItemGroupDAO.getById(groupId);
        return group != null ? group.getPrice() : null;
    }

    public Appointment getAppointmentById(Long appointmentId) {
        return appointmentDAO.getAppointmentById(appointmentId);
    }

    // 新版：支持examDate和examTimeSlot
    public boolean createAppointment(Users user, Long groupId, LocalDate examDate, String examTimeSlot) {
        Appointment appointment = new Appointment(user.getId(), groupId, LocalDateTime.now());
        appointment.setExamDate(examDate);
        appointment.setExamTimeSlot(examTimeSlot);
        return appointmentDAO.createAppointment(appointment);
    }

    // 兼容旧版：用java.util.Date（转为LocalDateTime作为预约时间）
    public boolean createAppointment(Users user, Long groupId, java.util.Date appointmentTime) {
        LocalDateTime ldt = appointmentTime.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime();
        Appointment appointment = new Appointment(user.getId(), groupId, ldt);
        appointment.setExamDate(ldt.toLocalDate());
        return appointmentDAO.createAppointment(appointment);
    }

    // 完全兼容旧版签名
    public boolean createAppointment(Users user, Long groupId, LocalDateTime appointmentTime) {
        Appointment appointment = new Appointment(user.getId(), groupId, appointmentTime);
        appointment.setExamDate(appointmentTime.toLocalDate());
        return appointmentDAO.createAppointment(appointment);
    }

    public List<Appointment> getUserAppointments(Users user) {
        return appointmentDAO.getUserAppointments(user.getId());
    }

    public boolean cancelAppointment(long appointmentId) {
        return appointmentDAO.cancelAppointment(appointmentId);
    }

    public boolean completeAppointment(long appointmentId) {
        return appointmentDAO.completeAppointment(appointmentId);
    }

    public List<CheckItemGroup> getAllGroups() {
        return checkItemGroupDAO.getAll();
    }

    public boolean createCustomGroup(CheckItemGroup group, List<Long> itemIds) {
        return checkItemGroupDAO.createGroup(group, itemIds);
    }

    public List<Appointment> getUserAppointmentsByStatus(long userId, String status) {
        return appointmentDAO.getUserAppointments(userId).stream()
                .filter(a -> status.equals(a.getStatus()))
                .toList();
    }

    public boolean updatePaymentStatus(Long appointmentId, boolean paid) {
        return appointmentDAO.updatePaymentStatus(appointmentId, paid);
    }

    public CheckItemGroup getCheckItemGroupById(Long groupId) {
        return checkItemGroupDAO.getById(groupId);
    }

    public CheckItem getCheckItemById(Long itemId) {
        return checkItemDAO.getById(itemId);
    }

    public List<CheckItem> getAllTests() {
        return checkItemDAO.getAll();
    }

    // 兼容旧方法名
    public List<CheckItemGroup> getAllPackages() { return getAllGroups(); }
    public boolean createCustomPackage(CheckItemGroup group, List<Long> itemIds) { return createCustomGroup(group, itemIds); }
    public CheckItemGroup getTestPackageById(Long id) { return getCheckItemGroupById(id); }
    public CheckItem getMedicalTestById(Long id) { return getCheckItemById(id); }

}

package com.healthsys.service;

import com.healthsys.dao.ExamRecordDAO;
import com.healthsys.common.entity.ExamRecord;

import java.util.List;

public class ExamService {
    private final ExamRecordDAO examRecordDAO = new ExamRecordDAO();

    public boolean addExamRecord(ExamRecord record) {
        return examRecordDAO.addExamRecord(record);
    }

    public List<ExamRecord> getExamRecordsByAppointment(Long appointmentId) {
        return examRecordDAO.getExamRecordsByAppointment(appointmentId);
    }

    public boolean updateExamResult(Long appointmentId, Long testId, String resultValue) {
        return examRecordDAO.updateExamResult(appointmentId, testId, resultValue);
    }

    public boolean existsRecord(Long appointmentId, Long testId) {
        return examRecordDAO.existsRecord(appointmentId, testId);
    }
}

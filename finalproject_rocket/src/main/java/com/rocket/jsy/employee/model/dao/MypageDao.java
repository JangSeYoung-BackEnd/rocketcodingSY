package com.rocket.jsy.employee.model.dao;

import java.sql.Timestamp;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;

public interface MypageDao {
	void startWork(SqlSession session, int empNo);
	void endWork(SqlSession session, int empNo);
	Map<String, Object> selectEmployeeByNo(SqlSession session, int empNo);

}
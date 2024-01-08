package com.rocket.seoj.logistics.model.service;

import com.rocket.seoj.logistics.model.dao.PublisherDao;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.ibatis.session.SqlSession;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Log4j2
public class PublisherService {

    private final PublisherDao dao;
    private final SqlSession session;

    @Transactional
    public int updateColumn(long id,
                            String columnName,
                            String tableName,
                            String value,
                            String parentTableName,
                            String parentColumnId,
                            String parentColumnName,
                            String columnId) throws DataAccessException {

        try {
            return dao.updateColumn(session, id, columnName, tableName, value, parentTableName, parentColumnId,
                                    parentColumnName, columnId);
        } catch (DataAccessException e) {
            throw e;
        }

    }

    @Transactional
    public List<Map<String, Object>> selectAllPublisher() {
        return dao.selectAllPublisher(session);
    }


}
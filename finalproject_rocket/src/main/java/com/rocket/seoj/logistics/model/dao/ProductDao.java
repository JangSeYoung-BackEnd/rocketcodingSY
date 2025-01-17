package com.rocket.seoj.logistics.model.dao;


import com.rocket.seoj.logistics.model.dto.PrdAttach;
import com.rocket.seoj.logistics.model.dto.Product;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSession;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Brief description of functions
 *
 * @author J
 * @version 2023-12-25
 */
@Repository
@Slf4j
public class ProductDao {


/*    public int updateColumn(SqlSession session,
                            long id,
                            String columnName,
                            String tableName,
                            String value,
                            String parentTableName,
                            String parentColumnId,
                            String parentColumnName,
                            String columnId) throws DataAccessException {
        Map<String, Object> params = new HashMap<>();
        params.put("id", id);
        params.put("columnName", columnName);
        params.put("tableName", tableName);
        params.put("value", value);
        params.put("parentTableName", parentTableName);
        params.put("parentColumnId", parentColumnId);
        params.put("parentColumnName", parentColumnName);
        params.put("columnId", columnId);

        log.debug("ㅇㅇㅇㅇㅇㅇㅇㅇㅇㅇㅇㅇㅇㅇㅇㅇㅇㅇㅇㅇㅇㅇㅇ" + String.valueOf(
                id) + ", " + columnName + ", " + tableName + ", " + value + ", " + parentTableName + ", " + parentColumnId + ", " + parentColumnName + ", " + columnId);

        try {

            if (parentTableName.equals(tableName)) {
                return session.update("publisher.updateColumn_sameTable", params);
            } else {
                return session.update("publisher.updateColumn", params);
            }
        } catch (DataAccessException e) {
            throw e;
        }

    }*/

    public List<Map<String, Object>> selectAllProductAndPublisherAndFiles(SqlSession session) {
        return session.selectList("product.selectAllProductAndPublisherAndFiles");
    }

    public List<Map<String, Object>> selectAllPublisher(SqlSession session) {
        return session.selectList("publisher.selectAllPublisher");
    }

    public long insertProduct(SqlSession session, Product params) {
        return session.insert("product.insertProduct", params);
    }


    public List<Integer> insertProductAttach(SqlSession session, List<PrdAttach> fileList) {
        List<Integer> result = new ArrayList<>();

        for (PrdAttach attach : fileList) {
            int resultOne = session.insert("productattach.insertProductAttach", attach);

            result.add(resultOne);
        }

        return result;
    }

    public boolean isdelUpdateProduct(SqlSession session, Long prdId) {
        return session.update("product.isdelUpdateProduct", prdId) > 0;
    }

    public int updateColumn(SqlSession session,
                            long id,
                            String columnName,
                            String tableName,
                            String value,
                            String parentTableName,
                            String parentColumnId,
                            String parentColumnName,
                            String columnId) {
        Map<String, Object> params = new HashMap<>();
        params.put("id", id);
        params.put("columnName", columnName);
        params.put("tableName", tableName);
        params.put("value", value);
        params.put("parentTableName", parentTableName);
        params.put("parentColumnId", parentColumnId);
        params.put("parentColumnName", parentColumnName);
        params.put("columnId", columnId);

/*        log.debug("ㅇㅇㅇㅇㅇㅇㅇㅇㅇㅇㅇㅇㅇㅇㅇㅇㅇㅇㅇㅇㅇㅇㅇ" + String.valueOf(
                id) + ", " + columnName + ", " + tableName + ", " + value + ", " + parentTableName + ", " + parentColumnId + ", " + parentColumnName + ", " + columnId);*/

        try {

            if (parentTableName.equals(tableName)) {
                return session.update("product.updateColumn_sameTable", params);
            } else {
                return session.update("product.updateColumn", params);
            }
        } catch (DataAccessException e) {
            throw e;
        }
    }

/*    public boolean isdelUpdatePublisher(SqlSession session, Long pubId) {
        return session.update("publisher.isdelUpdatePublisher", pubId) > 0;
    }

    public long insertPublisher(SqlSession session, HashMap<String, Object> params) {
        return session.insert("publisher.insertPublisher", params);
    }*/
}

package tikitiki.repository;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.AbstractListHandler;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VisitLogRepository implements AutoCloseable {

    private static final String SQL_UP_TO_50_TEMPLATE =
            "SELECT v.id, u.name as user_name, h.name as hotel_name, date " +
                    "FROM visit_logs v " +
                    "INNER JOIN hotels h ON v.hotel_id = h.id " +
                    "INNER JOIN users u ON v.user_id = u.id " +
                    "WHERE v.del_flg = 0 AND u.del_flg = 0 AND h.del_flg = 0 ";

    private static final String SQL_UP_TO_75_TEMPLATE = SQL_UP_TO_50_TEMPLATE + " AND u.id = ? ";

    private static final String SQL_UP_TO_100_TEMPLATE = SQL_UP_TO_50_TEMPLATE + " AND h.id = ? ";

    private static final AbstractListHandler<Map<String, Object>> ROW_HANDLER =
    new AbstractListHandler<Map<String, Object>>(){
        @Override
        protected Map<String, Object> handleRow(ResultSet rs) throws SQLException {
            Map<String, Object> map = new HashMap<>();
            map.put("id", rs.getString(1));
            map.put("user_name", rs.getString(2));
            map.put("hotel_name", rs.getString(3));
            map.put("date", rs.getString(4));
            return map;
        }
    };

    private final Connection connection;

    public VisitLogRepository(Connection connection) {
        this.connection = connection;
    }

    public List<Map<String, Object>> queryByCondition(int condition) {
        if (0 < condition && condition <= 50) {
             return executeStatementUpTo50(condition);
        } else  if (50 < condition && condition <= 75) {
            return executeStatementUpTo75(condition);
        } else if(75 < condition && condition <= 100) {
            return executeStatementUpTo100(condition);
        } else {
            throw new IllegalArgumentException("argument is illegal!: condition=" + condition);
        }
    }

    @Override
    public void close() throws Exception {
        connection.close();
    }

    private List<Map<String, Object>> executeStatementUpTo50(int condition) {
        String statement;
        if ( condition == 50 ) {
             statement = SQL_UP_TO_50_TEMPLATE + "LIMIT " + (1000 * 49) + ", 1000";
        } else {
            statement = SQL_UP_TO_50_TEMPLATE + "LIMIT " + (1000 * ((condition * 10) - 1)) + ", 1000";
        }

        QueryRunner queryRunner = new QueryRunner();
        try {
            return queryRunner.query(connection, statement, ROW_HANDLER);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private List<Map<String, Object>> executeStatementUpTo75(int condition) {
        String statement = SQL_UP_TO_75_TEMPLATE + " LIMIT 0, 300";
        QueryRunner queryRunner = new QueryRunner();
        try {
            int userId = condition - 50;
            return queryRunner.query(connection, statement, ROW_HANDLER, userId);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private List<Map<String, Object>> executeStatementUpTo100(int condition) {
        String statement = SQL_UP_TO_100_TEMPLATE + " LIMIT 0, 300";
        QueryRunner queryRunner = new QueryRunner();
        try {
            int hotelId = condition - 75;
            return queryRunner.query(connection, statement, ROW_HANDLER, hotelId);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}

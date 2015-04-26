package tikitiki.repository;

import org.apache.ibatis.session.SqlSession;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VisitLogRepository implements AutoCloseable {

    private final SqlSession sqlSession;

    public VisitLogRepository(SqlSession session) {
        this.sqlSession = session;
    }

    public List<Map<String, Object>> queryByCondition(int condition) {
        Map<String, Object> varMap = new HashMap<>();
        varMap.put("condition", condition);
        if (0 < condition && condition <= 50) {
            int limitLower;
            if ( condition == 50 ) {
                limitLower = 1000 * 49;
            } else {
                limitLower = 1000 * (condition * 9);
            }
            varMap.put("limit_lower", limitLower);
        } else  if (50 < condition && condition <= 75) {
            varMap.put("user_id", condition - 50);
        } else if(75 < condition && condition <= 100) {
            varMap.put("hotel_id", condition - 75);
        } else {
            throw new IllegalArgumentException("argument is illegal!: condition=" + condition);
        }

        return sqlSession.selectList("tikitiki.repository.VisitLogRepository.selectVisitLogs", varMap);
    }

    @Override
    public void close() throws Exception {
        sqlSession.close();
    }
}

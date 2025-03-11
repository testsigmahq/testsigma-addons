package dbMethods;

import java.sql.*;
import com.testsigma.sdk.Logger;
import oracle.jdbc.pool.OracleDataSource;
import oracle.jdbc.OracleConnection;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.json.JSONArray;
import org.json.JSONObject;


public class DBConnection {

    Logger logger = new Logger(new StringBuilder(""));
    public JSONArray executeQueries(String databaseName, String userName, String password, String query, String wallet_path) throws Exception {

        System.setProperty("oracle.net.tns_admin", wallet_path);
        System.setProperty("oracle.net.wallet_location", wallet_path);
        System.setProperty("oracle.net.ssl_server_dn_match", "true");
        Class.forName("oracle.jdbc.driver.OracleDriver");
        OracleDataSource ods = new OracleDataSource();
        ods.setURL("jdbc:oracle:thin:@" + databaseName + "?TNS_ADMIN=" + wallet_path);
        ods.setUser(userName);
        ods.setPassword(password);
        JSONArray query_result = null;
        try (OracleConnection connection = (OracleConnection) ods.getConnection()) {
            DatabaseMetaData dbmd = connection.getMetaData();
            query_result = printSales(connection, query);
        } catch (Exception e) {
            logger.info(ExceptionUtils.getStackTrace(e));
        }
        return query_result;
    }

    public JSONArray printSales(Connection connection, String query) {
        JSONArray jsonArray = new JSONArray();
        try (Statement statement = connection.createStatement()) {
            try (ResultSet resultSet = statement.executeQuery(query)) {
                while (resultSet.next()) {
                    int columns = resultSet.getMetaData().getColumnCount();
                    JSONObject obj = new JSONObject();
                    for (int i = 0; i < columns; i++)
                        obj.put(resultSet.getMetaData().getColumnLabel(i + 1).toLowerCase(), resultSet.getObject(i + 1));
                    jsonArray.put(obj);
                }
            } catch (Exception e) {
                logger.info(ExceptionUtils.getStackTrace(e));
            }
        } catch (Exception e){
            logger.info(ExceptionUtils.getStackTrace(e));
        }
        return jsonArray;
    }

}

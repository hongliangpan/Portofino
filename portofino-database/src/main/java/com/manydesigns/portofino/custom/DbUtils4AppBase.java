package com.manydesigns.portofino.custom;
import com.manydesigns.elements.stripes.ElementsActionBeanContext;
import com.manydesigns.portofino.modules.DatabaseModule;
import com.manydesigns.portofino.persistence.Persistence;
import com.manydesigns.portofino.persistence.QueryUtils;
import com.manydesigns.portofino.util.DbUtils;
import org.hibernate.Session;
import org.hibernate.jdbc.Work;

import javax.servlet.http.HttpServletRequest;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class DbUtils4AppBase {

	public static Session getSession(String dbName) {
		return DbUtils.getPersistence().getSession(dbName);
	}

	public static Persistence getPersistence() {
		return DbUtils.getPersistence();
	}

	public static void callProcedure(String dbName,Persistence persistence, final String sql) {
		getSession(dbName).doWork(new Work() {
			public void execute(Connection connection) throws SQLException {
				CallableStatement procStatement = null;
				try {
					System.err.println(sql);
					procStatement = connection.prepareCall(sql);

					procStatement.execute();
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					if (null != procStatement) {
						procStatement.close();
					}
				}
			}
		});

	}

	public static void runSql(String dbName,Persistence persistence, final String sql) {
		QueryUtils.runSqlDml(persistence.getSession(dbName), sql);
	}

	public static void runSql(String dbName,final String sql) {
		QueryUtils.runSqlDml(getSession(dbName), sql);
	}

	public static List<Map<String, Object>> runSqlReturnMap(String dbName,String sql, Persistence persistence) {
		List<Map<String, Object>> mapList = QueryUtils.runSqlReturnMap(persistence.getSession(dbName), sql);
		return mapList;
	}

	public static List<Map<String, Object>> runSqlReturnMap(String dbName,String sql) {
		List<Map<String, Object>> mapList = QueryUtils.runSqlReturnMap(getSession(dbName), sql);
		return mapList;
	}

	public static List<Map<String, Object>> runSqlReturnMap(String dbName, String sql, HttpServletRequest request) {
		ElementsActionBeanContext context = new ElementsActionBeanContext();
		context.setRequest(request);
		System.err.println(sql);
		Persistence persistence = (Persistence) request.getServletContext().getAttribute(DatabaseModule.PERSISTENCE);
		List<Map<String, Object>> mapList = QueryUtils.runSqlReturnMap(persistence.getSession(dbName), sql);
		return mapList;
	}

	public static List<String> getColumnName(List<Map<String, Object>> data, String sql) {
		List<String> result = new ArrayList<String>();
		for (Map<String, Object> map : data) {
			for (String columnName : map.keySet()) {
				String remark = CustomProperties.getInstance().getColumnRemarks(columnName);
				result.add(remark);
			}
			break;
		}
		return result;
	}

	public static Map<String, String> getColumnRemark(String dbName,String tableName) {
		String schema = "itsboard200";
		String columnSql = "SELECT column_name,column_comment FROM INFORMATION_SCHEMA.columns WHERE TABLE_SCHEMA='"
				+ schema + "' AND TABLE_NAME='" + tableName + "'";
		Map<String, String> result = new HashMap<String, String>();
		List<Map<String, Object>> comments = runSqlReturnMap(dbName,columnSql, getPersistence());
		for (Map<String, Object> map : comments) {
			for (Entry<String, Object> column : map.entrySet()) {
				if (null != column.getValue()) {
					result.put(column.getKey(), column.getValue().toString());
				}
			}
		}
		return result;
	}

	public static List<List<String>> getRowValues(List<Map<String, Object>> data, String sql) {
		List<List<String>> result = new ArrayList<List<String>>();
		for (Map<String, Object> map : data) {
			List<String> row = new ArrayList<String>();
			for (Object value : map.values()) {
				if (value != null) {
					row.add(value.toString());
				} else {
					row.add("");
				}
			}
			result.add(row);
		}

		return result;
	}

	public static String replaceSql4MessageFormat(final String sql) {
		return sql.replaceAll("'", "''");
	}

}

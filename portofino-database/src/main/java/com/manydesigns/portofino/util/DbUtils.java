package com.manydesigns.portofino.util;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.manydesigns.elements.stripes.ElementsActionBeanContext;
import com.manydesigns.elements.text.OgnlSqlFormat;
import com.manydesigns.portofino.modules.DatabaseModule;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.jdbc.Work;

import com.manydesigns.portofino.persistence.Persistence;
import com.manydesigns.portofino.persistence.QueryUtils;

import javax.servlet.http.HttpServletRequest;

public class DbUtils {
	private static Persistence persistence;

	public static void callProcedure(Persistence persistence, final String sql) {
		Session session = persistence.getSession(getDbName());
		callProcedure(session,sql);
	}
	public static void callProcedure(Session session, final String sql) {
		session.doWork(new Work() {
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
	public static void runSqlDml(Persistence persistence, final String sql) {
		Session session = persistence.getSession(getDbName());
		runSqlDml(session, sql);
	}
	public static int runSqlDml(Session session, final String sql) {
		final ArrayList result = new ArrayList();
		try {
			session.doWork(new Work() {
				public void execute(Connection connection) throws SQLException {
					Statement stmt = connection.createStatement();
					try {
						result.add(Integer.valueOf(stmt.executeUpdate(sql)));
					} finally {
						stmt.close();
					}
				}
			});
			session.getTransaction().commit();
			session.beginTransaction();
		} catch (HibernateException var6) {
			result.add(Integer.valueOf(-1));
			session.getTransaction().rollback();
			session.beginTransaction();
			throw var6;
		}
		return result.size() > 0 ? ((Integer) result.get(0)).intValue() : -1;
	}
	public static List<Map<String, Object>> runSqlReturnMap(String sql, HttpServletRequest request) {
		return runSqlReturnMap(getDbName(), sql, request);
	}
	public static List<Map<String, Object>> runSqlReturnMap(String dbName, String sql, HttpServletRequest request) {
		ElementsActionBeanContext context = new ElementsActionBeanContext();
		context.setRequest(request);
		System.err.println(sql);
		Persistence persistence = (Persistence) request.getServletContext().getAttribute(DatabaseModule.PERSISTENCE);
		List<Map<String, Object>> mapList = QueryUtils.runSqlReturnMap(persistence.getSession(dbName), sql);
		return mapList;
	}
	public static List<Map<String, Object>> runSqlReturnMap(String sql, Persistence persistence) {
		List<Map<String, Object>> mapList = QueryUtils.runSqlReturnMap(persistence.getSession(getDbName()), sql);
		return mapList;
	}

	public static String getDbName() {
		return persistence.getModel().getDatabases().get(0).getDatabaseName();
		// return defaultDbName;
	}

	public static Persistence getPersistence() {
		return persistence;
	}

	public static void setPersistence(Persistence persistence) {
		DbUtils.persistence = persistence;
	}
}

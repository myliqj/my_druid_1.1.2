package com.myliqj.util;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import com.myliqj.db.dao.SqlXML;

public class MetaDataSql { 
	//public static Map<String,Map<String,SqlXML>> SQLMAP = new HashMap(<String,new HashMap<String, SqlXML>()>();

	public static Map<String,Map<String,SqlXML>> SQLMAP = new HashMap();

	public static final String DB2_for_read_only_with_ur = " FOR READ ONLY WITH UR";
	 
	
	static {
		
		//JdkXml.getSQLMAP("D:\\wkMyEclipse2015\\druid_1.1.2\\src\\com\\myliqj\\util\\sqlXml.xml",SQLMAP,for_read_only_with_ur);
		
	}
	
	public static Map<String,SqlXML> getSqlMap(String dbType){
		Map<String, SqlXML> m = SQLMAP.get(dbType);
		if (m==null){
			synchronized (SQLMAP) {
				m = SQLMAP.get(dbType);
				if (m==null){
					String file = "sqlMapXml_"+dbType+".xml";
					URL url = JdkXml.class.getResource("/resource/"+file);
					if (url!=null) file = url.getFile();
					m = new HashMap<String,SqlXML>();
					String sqlTail = "db2".equals(dbType)?DB2_for_read_only_with_ur:"";
					JdkXml.getSQLMAP(file, m, sqlTail);
					SQLMAP.put(dbType, m);
				}
			}
		}
		return m;
	}
	
	public static SqlXML getSql(String dbType,String sqlMapId){
		Map<String,SqlXML> m = getSqlMap(dbType); 
		return m.get(sqlMapId);
	}

	/**
	 
	 // java.sql.Types 
        public final static int ARRAY               = 2003;
        public final static int BIGINT          =  -5;
        public final static int BINARY          =  -2;
        public final static int BIT             =  -7;
        public final static int BLOB                = 2004;
        public final static int CHAR            =   1;
        public final static int CLOB                = 2005;
        public final static int DATE            =  91;
        public final static int DECIMAL         =   3;
        public final static int DISTINCT            = 2001;
        public final static int DOUBLE          =   8;
        public final static int FLOAT           =   6;
        public final static int INTEGER         =   4;
        public final static int JAVA_OBJECT         = 2000;
        public final static int LONGVARBINARY   =  -4;
        public final static int LONGVARCHAR     =  -1;
        public final static int NULL            =   0;
        public final static int NUMERIC         =   2;
        public final static int OTHER           = 1111;
        public final static int REAL            =   7;
        public final static int REF                 = 2006;
        public final static int SMALLINT        =   5;
        public final static int STRUCT              = 2002;
        public final static int TIME            =  92;
        public final static int TIMESTAMP       =  93;
        public final static int TINYINT         =  -6;
        public final static int VARBINARY       =  -3;
        public final static int VARCHAR         =  12; 
	 */
//	public boolean IsNumberType(int c){
//		return ( c==-5 || c==3 || c==8 || c==6 || c==4 || c==2 || c==7 || c==5 || c==-6);
//	}
	public static boolean isNumberType(int javaType){
		return (javaType==java.sql.Types.BIGINT
			 || javaType==java.sql.Types.DECIMAL
			 || javaType==java.sql.Types.DOUBLE
			 || javaType==java.sql.Types.FLOAT
			 || javaType==java.sql.Types.INTEGER
			 || javaType==java.sql.Types.NUMERIC
			 || javaType==java.sql.Types.REAL
			 || javaType==java.sql.Types.SMALLINT
			 || javaType==java.sql.Types.TINYINT);
	}
	public static boolean isStringType(int javaType){
		return (javaType==java.sql.Types.CHAR
			 || javaType==java.sql.Types.VARCHAR
			 || javaType==java.sql.Types.LONGVARCHAR
			 || javaType==java.sql.Types.CLOB);
	}

	public static boolean isDataTimeType(int javaType){
		return (javaType==java.sql.Types.DATE
			 || javaType==java.sql.Types.TIMESTAMP
			 || javaType==java.sql.Types.TIME);
	}
	public static int db2TypeToJavaType(String db2Type){
		if ("VARCHAR".equals(db2Type) || "CHARACTER".equals(db2Type)) {
			return java.sql.Types.VARCHAR;
		} else if ("BIGINT".equals(db2Type)) {
			return java.sql.Types.BIGINT;
		} else if ("BLOB".equals(db2Type)) {
			return java.sql.Types.BLOB;
		} else if ("CLOB".equals(db2Type)) {
			return java.sql.Types.CLOB;
		} else if ("DATE".equals(db2Type)) {
			return java.sql.Types.DATE;
		} else if ("DECIMAL".equals(db2Type)) {
			return java.sql.Types.DECIMAL;
		} else if ("DOUBLE".equals(db2Type)) {
			return java.sql.Types.DOUBLE;
		} else if ("INTEGER".equals(db2Type)) {
			return java.sql.Types.INTEGER;
		} else if ("LONG VARCHAR".equals(db2Type)) {
			return java.sql.Types.LONGVARCHAR;
		} else if ("SMALLINT".equals(db2Type)) {
			return java.sql.Types.SMALLINT;
		} else if ("TIMESTAMP".equals(db2Type)) {
			return java.sql.Types.TIMESTAMP;
		} else if ("TIME".equals(db2Type)) {
			return java.sql.Types.TIME;
		}
		return java.sql.Types.OTHER;
	}
	
	/**
	 * 返回指定SQL语句的指定部份，跳过空格,TAB
	 * @param sql
	 * @param start
	 * @param count
	 * @return
	 */
	public static String getExecuteSqlCharCount(String sql,int start,int count){
		// 获取指定数量的字符，跳过 空格,TAB
		String result = "";
		if (count==0) return result;
		
		int len = sql.length();
		int cur = start; int curCount = 0;
		while (cur<len){
			char c = sql.charAt(cur);
			if (c==' '||c=='\t') {
				cur++;
				continue;
			}
			result += c;
			curCount++;
			if (curCount>=count) break;
			
			cur++;
		}
		return result;
	}
	
	/**
	 * 返回执行SQL语句最开始有效部分索引，跟过前面注释与空白(空格,TAB,回车,换行)
	 * @param sql
	 * @return
	 */
	public static int getExecuteSqlStartCharIndex(String sql){
		// 跟过前面注释与空白(空格,TAB,回车,换行)
		int cur = 0;
		int len = sql.length();
		while (cur<len){
			char c = sql.charAt(cur);
			if (c==' ' || c=='\t' || c=='\r' || c=='\n') {
				cur++;
				continue;
			}else if (c=='-'){
				cur ++;
				if (cur<len && sql.charAt(cur)=='-'){
					// 单行注释:  -- ... \r\n
					cur++;
					while (cur<len){ 
						// find: \n or \r\n
						c = sql.charAt(cur);
						if (c=='\r'){
							cur++; 
							if (cur<len) c = sql.charAt(cur);
							if (c=='\n') cur++;
							break;
						}else if (c=='\n') {
							cur++;
							break;
						}						
						cur++;
					}
					continue;
				}
			} else if (c=='/'){
				cur ++;
				if (cur<len && sql.charAt(cur)=='*'){
					// 多行注释:  /* ... */
					cur++;
					while (cur<len){ 
						// find: */
						c = sql.charAt(cur);
						if (c=='*'){
							cur++;
							if (cur<len) c = sql.charAt(cur);
							if ( c=='/') {
								cur++;
								break;
							}
						}
						cur++;
					}
					continue;
				}
			}				
			break;
		}
		return cur;
	}
	
	
//	static {
//		SQLMAP.put(SqlObjDefine.USERNAME, "SELECT TRIM(CURRENT SCHEMA) AS USERNAME FROM SYSIBM.SYSDUMMY1 "+for_read_only_with_ur);
//		SQLMAP.put(SqlObjDefine.LOGINUSER, "SELECT TRIM(USER) LOGINUSER FROM SYSIBM.SYSDUMMY1 "+for_read_only_with_ur);
//		
//		
//		SQLMAP.put(SqlObjDefine.TABLE_ALL, "");
//		SQLMAP.put(SqlObjDefine.INDEX
//				, "SELECT TRIM(INDSCHEMA) INDEXSCHEMA, TRIM(INDNAME) INDEXNAME, SUBSTR(REPLACE(COLNAMES,'+',','),2) FIELDNAMES "
//				+ ", UNIQUERULE INDEXTYPE, CREATE_TIME,STATS_TIME, REMARKS "
//				+ "FROM SYSCAT.INDEXES A WHERE TABNAME = ? AND TABSCHEMA = ? "
//				+ "ORDER BY CASE WHEN UNIQUERULE='P' THEN 1 WHEN UNIQUERULE='U' THEN 2 ELSE 3 END,CREATE_TIME "+ for_read_only_with_ur);
//		  
//		// procedure
//		SQLMAP.put(SqlObjDefine.PROCEDURE,
//		"SELECT TRIM(R.ROUTINESCHEMA) PROCSCHEMA,TRIM(R.ROUTINENAME) PROCNAME, R.REMARKS DESCRIPTION,R.CREATE_TIME CREATETIME "
//		+ ", CASE WHEN (R.ORIGIN = ''Q'' AND R.VALID!='Y') OR P.VALID!='Y' THEN 'N' ELSE 'Y' END ISVALID "
//		+ ", R.SPECIFICNAME, CASE WHEN R.ROUTINETYPE='P' THEN 'PROCEDURE' WHEN R.ROUTINETYPE='F' THEN 'FUNCTION' ELSE 'N/A' END PROCTYPE "
//		+ ", TRIM(CHAR(R.ROUTINEID))||VALUE(' ' ||P.PKGNAME,'') OBJECTID \n"
//		+ " FROM SYSCAT.ROUTINES R LEFT JOIN SYSCAT.PACKAGES P ON P.PKGSCHEMA = R.ROUTINESCHEMA AND P.PKGNAME = ''P''|| SUBSTR(CHAR(R.LIB_ID+10000000),2) "
//		+ " #WHERE_01# \nUNION ALL\n "
//		+ " SELECT TRIM(T.TRIGSCHEMA) PROCSCHEMA,TRIM(T.TRIGNAME) PROCNAME,T.REMARKS DESCRIPTION,T.CREATE_TIME CREATETIME,T.VALID ISVALID "
//		+ ", TRIM(T.TABSCHEMA)||''.''||T.TABNAME SPECIFICNAME, 'TRIGGER' PROCTYPE "
//        + ",'' PKGNAME OBJECTID \n"
//        + " FROM SYSCAT.TRIGGERS T #WHERE_02# \nORDER BY CREATE_TIME DESC "+for_read_only_with_ur
//		);
//		
//		
//		SQLMAP.put(SqlObjDefine.PROCEDURE_PARAMS_LENGTH, "SELECT A.LENGTH FROM SYSCAT.PROCPARMS A WHERE A.PROCNAME=? AND A.PROCSCHEMA=? AND A.PARMNAME=? "+for_read_only_with_ur);
//		
//	}
}

package com.myliqj.util;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SQLStringUtils {

	private static final Logger LOG = Logger.getLogger(SQLStringUtils.class.getName());
 
	public static final Pattern UNION_PATTERN = Pattern.compile( //"(.*)(union all|union)(.*)"
			"(.*)\\s*(UNION\\s*(ALL)*|EXCEPT\\s*(ALL)*|MINUS\\s*(ALL)*|INTERSECT\\s*(ALL)*)\\s*(.*)"
			, Pattern.DOTALL + Pattern.CASE_INSENSITIVE);
	
	public static final Pattern SELECT_PATTERN = Pattern.compile(
			"(^\\s*SELECT\\b.*\\bFROM\\b\\s)(.*)\\s*(\\bWHERE\\b)", Pattern.DOTALL + Pattern.CASE_INSENSITIVE);

	public static final Pattern SELECT_PATTERN_2 = Pattern.compile(
			"(^\\s*SELECT\\b.*\\bFROM\\b\\s)(.*)\\s+$", Pattern.DOTALL + Pattern.CASE_INSENSITIVE);

	public static final Pattern INSERT_PATTERN = Pattern.compile(
			"(^\\s*\\b*INSERT\\s*\\bINTO\\b\\s)(\\w+)(.*)", Pattern.DOTALL + Pattern.CASE_INSENSITIVE);

	public static final Pattern UPDATE_PATTERN = Pattern.compile(
			"(^\\s*\\b*UPDATE\\s*\\b)(\\b.+)(\\bSET\\b.*)", Pattern.DOTALL + Pattern.CASE_INSENSITIVE);
//			"(^\\bUPDATE\\s\\b)(\\b.+)(\\bSET\\b.*$)", Pattern.DOTALL + Pattern.CASE_INSENSITIVE);

	public static final Pattern DELETE_PATTERN = Pattern.compile(
			"(^\\s*\\b*DELETE\\b.*\\bFROM\\b\\s)(\\w+)(.*)", Pattern.DOTALL + Pattern.CASE_INSENSITIVE);
//			"(^\\bDELETE\\b.*\\bFROM\\b\\s)(\\w+)(.*$)", Pattern.DOTALL + Pattern.CASE_INSENSITIVE);

	public static final Pattern JOIN_PATTERN = Pattern.compile("^\\s*(.*)\\b\\s+(LEFT|RIGHT|INNER)+\\s*(OUTER)*\\s+JOIN\\b\\s+(.+)\\s+\\bON\\b\\s+(\\b.*)",Pattern.DOTALL + Pattern.CASE_INSENSITIVE);
	public static final Pattern JOIN_PATTERN_S = Pattern.compile("^\\s*(.*)\\b(.*)*\\s+(LEFT|RIGHT|INNER|ON)+\\s*(OUTER)*\\s*(.*)",Pattern.DOTALL + Pattern.CASE_INSENSITIVE);
	
	public static String[] getTableNames(String sql) {
		//System.out.println(sql);
		String[] names = null;
		List<String> tables = new ArrayList<String>();
		Matcher match;


		//if (tables.size() == 0) {
			match = INSERT_PATTERN.matcher(sql);
			if (match.find()) {
				tables.add(match.group(2));
				if (match.group(3) != null) {
					String[] nested = getTableNames(match.group(3));
					if ((nested != null) && (nested.length > 0)) {
						for (int i = 0; i < nested.length; i++) {
							tables.add(nested[i]);
						}
					}
				}
				return tables.toArray(new String[tables.size()]);
			}
		//}

		//if (tables.size() == 0) {
			match = UPDATE_PATTERN.matcher(sql);
			if (match.find()) {
				tables.add(match.group(2));
				return tables.toArray(new String[tables.size()]);
			}
		//}

		//if (tables.size() == 0) {
			match = DELETE_PATTERN.matcher(sql);
			if (match.find()) {
				tables.add(match.group(2));
				if (match.group(3) != null) {
					String[] nested = getTableNames(match.group(3));
					if ((nested != null) && (nested.length > 0)) {
						for (int i = 0; i < nested.length; i++) {
							tables.add(nested[i]);
						}
					}
				}
				return tables.toArray(new String[tables.size()]);
			}
		//}

		
		
		//if (tables.size() == 0){
			match = UNION_PATTERN.matcher(sql);
			while (match.find()) {

	        	//for(int j = 1; j <= match.groupCount(); j++)
	            //    System.out.print("[" + match.group(j) + "]");
	        	
				if (match.groupCount() > 1) {
					if (match.group(1) != null) {
						String[] nested = getTableNames(match.group(1));
						if ((nested != null) && (nested.length > 0)) {
							for (int i = 0; i < nested.length; i++) {
								tables.add(nested[i]);
							}
						}
					}
					if (match.group(7) != null) {
						String[] nested = getTableNames(match.group(7));
						if ((nested != null) && (nested.length > 0)) {
							for (int i = 0; i < nested.length; i++) {
								tables.add(nested[i]);								
							}
						}
					}
					return tables.toArray(new String[tables.size()]);
				}
			}
		//}
		
		
		match = SELECT_PATTERN.matcher(sql);
		while (match.find()) {
			if (match.groupCount() > 1) {
				//tables.add(match.group(2));
				String tabs[] = match.group(2).split("(?i)JOIN");
				// join
				if(tabs.length==1){
					tables.add(tabs[0]);
				}else{
//					for (int j=0;j<tabs.length;j++){
//						Matcher m = JOIN_PATTERN_S.matcher(tabs[j]);
//						if (m.find()){
//							tables.add(m.group(1));
//						}
//					}
				}
				
				if (match.group(3) != null) {
					String[] nested = getTableNames(match.group(3));
					if ((nested != null) && (nested.length > 0)) {
						for (int i = 0; i < nested.length; i++) {
							tables.add(nested[i]); 
						}
					}
				}
				return tables.toArray(new String[tables.size()]);
			}
		}

		//if (tables.size() == 0) {
			match = SELECT_PATTERN_2.matcher(sql);
			if (match.find()) {
				tables.add(match.group(2));
				return tables.toArray(new String[tables.size()]);
			}
		//}
		
		
		names = tables.toArray(new String[tables.size()]);
		return names;
	}	
	
	

	public static boolean isQuote(String tok) {
		return "\"".equals( tok ) ||
				"`".equals( tok ) ||
				"]".equals( tok ) ||
				"[".equals( tok ) ||
				"'".equals( tok );
	}
	
	public static String formatCreateTable(String sql) {
		final StringBuilder result = new StringBuilder( 60 ).append( "\n    " );
		final StringTokenizer tokens = new StringTokenizer( sql, "(,)'[]\"", true );

		int depth = 0;
		boolean quoted = false;
		while ( tokens.hasMoreTokens() ) {
			final String token = tokens.nextToken();
			if ( isQuote( token ) ) {
				quoted = !quoted;
				result.append( token );
			}
			else if ( quoted ) {
				result.append( token );
			}
			else {
				if ( ")".equals( token ) ) {
					depth--;
					if ( depth == 0 ) {
						result.append( "\n    " );
					}
				}
				result.append( token );
				if ( ",".equals( token ) && depth == 1 ) {
					result.append( "\n       " );
				}
				if ( "(".equals( token ) ) {
					depth++;
					if ( depth == 1 ) {
						result.append( "\n        " );
					}
				}
			}
		}

		return result.toString();
	}
	
	
}

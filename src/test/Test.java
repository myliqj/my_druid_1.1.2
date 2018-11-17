package test;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.alibaba.druid.sql.SQLUtils;
import com.myliqj.util.SQLStringUtils;

public class Test {

	public static void main(String[] args) {
		
		String sql = "insert into aa select * from liqj.dirtmp_c union all select * from liqj.dirtmp_d union all\n" + 
				"  select * from liqj.dirtmp_e union all select * from liqj.dirtmp_f\n";
		//sql = "select * from liqj.dirtmp_c union  all select * from liqj.dirtmp_d";
//		sql = "insert into a1 ";
		sql ="select r.CREATEDTS, a.bname,s.REMARKS,case when b.routinename!=a.bname and a.btype='F' then\n" + 
				"   case when a.btype='F' then a.bname else b.specificname  end end dep\n" + 
				"  ,(select r.routinename from syscat.routines r where r.routineschema=b.routineschema and r.specificname=b.specificname) p1\n" + 
				"  ,(select r.remarks from syscat.routines r where r.routineschema=b.routineschema and r.specificname=b.specificname) rem1\n" + 
				"  ,a.*,HEX(UNIQUE_ID) unique_id,'X''' || HEX(UNIQUE_ID) || '''' AS UNIQUE_ID,b.*, r.*\n" + 
				"from syscat.routinedep b left join syscat.packagedep a on a.pkgname=b.bname  -- and\n" + 
				"  left join syscat.tables s on s.tabschema=a.bschema and s.tabname=a.bname\n" + 
				"  left join sysibm.sysroutines r on b.routinename=r.routinename and b.ROUTINESCHEMA=r.ROUTINESCHEMA and b.SPECIFICNAME=r.SPECIFICNAME\n" + 
				"where ( (1=1 and b.routinename like upper('%yz_sy_tj_dyzfyffb%')) or\n" + 
				"    ( 1=1 and a.bname like  (upper('%yz_sy_tj_dyzfyffb%') ))\n" + 
				"     --or a.pkgname =  x'5035343932373139' --'P9474418'\n" + 
				"    )   --and a.btype='T'\n" + 
				"order by case when b.routinename!=a.bname and a.btype='F' then 0 else 1 end\n" + 
				"  ,b.routinename,a.btype,a.bname  for read only with ur\n";
		String tabs[] = SQLStringUtils.getTableNames(sql);
		
		System.out.println(Arrays.asList(tabs));
		
		
//		Pattern p = Pattern.compile("(\\d{3,5})([a-z]{2})");
//        String s = "123aa-34345bb-234cc-00";
//        Matcher m = p.matcher(s);
//        System.out.println(m.groupCount());//2组
//        while(m.find()){
//        	//System.out.println(m.group());//数字字母都有
//        	//System.out.println(m.group(1));//只有数字
//        	System.out.println(m.group(2));//只有字母
//        }
        
		//(.*)(union)([ \\s]*all)*(.*) 
		//Pattern p = Pattern.compile("(.*)\\s+(union[\\s]*(all)*)\\s+(.*)",Pattern.DOTALL + Pattern.CASE_INSENSITIVE );
//        String s = "select * from liqj.dirtmp_c union   all select * from liqj.dirtmp_d";  (^\\s\\bINSERT\\sINTO\\b\\s)(\\w+)(.*)
//		Pattern p = Pattern.compile("(^\\s*INSERT\\s+INTO)\\s*(\\w+)(.*)",Pattern.DOTALL + Pattern.CASE_INSENSITIVE );
//		String s = "insert into aa ddd";
		
//		Pattern p = Pattern.compile("(^.*)\\b\\s+(LEFT|RIGHT|INNER)+\\s*(OUTER)*\\s+JOIN\\b\\s+(.+)\\s+\\bON\\b\\s+(\\b.*)",Pattern.DOTALL + Pattern.CASE_INSENSITIVE);
		Pattern p = Pattern.compile("^\\s*(.+)\\s+(.*)\\s+(LEFT|RIGHT|INNER||ON)\\s*(OUTER)*(.*)",Pattern.DOTALL + Pattern.CASE_INSENSITIVE);
		String s = "a1 left outer join b1 on 1=2"; // [a1][left][outer][b1][1=2]
		s = "sys.tab left join t1 on 1=2 and dd=fdd"; // [sys.tab][left][null][t1][1=2 and dd=fdd]
		s = "sys.t1 inner join t5 a on 1=2"; // [sys.t1][inner][null][t5 a][1=2]
		s = "t5 a inner outer join t8 b on 1=5"; //[t5 a][inner][outer][t8 b][1=5]
		s = "u right outer join aaaa a on 1=7"; //[u right outer ][ aaaa a on 1=7]
		s = "u right outer ";
		s = " aaaa a on 1=7"; s = "a b on";
		Matcher m = p.matcher(s); 
        System.out.println(m.groupCount());
        while(m.find()){
//        	System.out.println(m.group());
//        	System.out.println(m.group(1));
//        	System.out.println(m.group(2));
//        	System.out.println(m.group(3));
        	for(int j = 1; j <= m.groupCount(); j++)
                System.out.print("[" + m.group(j) + "]");
              System.out.println();
        }
        
//        String t[] = s.split("(?i)JOIN");
//        System.out.println(t.length);
//    	for(int j = 0; j < t.length; j++)
//            System.out.print("[" + t[j] + "]");
//    	System.out.println();
    	
    	Pattern p2 = Pattern.compile("(.+\\b)\\s+(.*)",Pattern.DOTALL + Pattern.CASE_INSENSITIVE);
    	Matcher m2 = p2.matcher("a b");  
        while(m2.find()){
        	for(int j = 1; j <= m2.groupCount(); j++)
                System.out.print("[" + m2.group(j) + "]");
              System.out.println();
        }
        
    	System.out.println("db2:"+SQLUtils.format_custom_1_1_2(sql, "db2/1/1/1/0/3/2/  ")+"\n");
    	System.out.println("oracle:"+SQLUtils.format_custom_1_1_2(sql, "oracle/1/1/1/0/3/2/  ")+"\n");
    	System.out.println("sqlserver:"+SQLUtils.format_custom_1_1_2(sql, "sqlserver/1/1/1/0/3/2/  ")+"\n");
    	System.out.println("mysql:"+SQLUtils.format_custom_1_1_2(sql, "mysql/1/1/1/0/3/2/  ")+"\n");
    	System.out.println("postgresql:"+SQLUtils.format_custom_1_1_2(sql, "postgresql/1/1/1/0/3/2/  ")+"\n");
    	System.out.println("hsql:"+SQLUtils.format_custom_1_1_2(sql, "hsql/1/1/1/0/3/2/  ")+"\n");
        
        
	}

}

package com.myliqj.ssq;

import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.ibm.db2.jcc.b.SqlException;

public class SSQ {
	
	//static ThreadLocal<Connection> db = new ThreadLocal<Connection>();
	
	// 红色球号码区由1-33共三十三个号码组成，蓝色球号码区由1-16共十六个号码组成。投注时选择6个红色球号码和1个蓝色球号码组成一注进行单式投注
	/*

2018-08-30 23:04:20.99>db2 "create table ssq_20180830(num varchar(20)) compress yes"
DB20000I  SQL 命令成功完成。	 
2018-08-30 23:07:50.62>db2 load from ssq_random_20180830_1.txt of del messages lo_ssq.msg insert into ssq_20180830  NONRECOVERABLE
读取行数         = 208839909
跳过行数         = 0
装入行数         = 208839909
拒绝行数         = 0
删除行数         = 0
落实行数         = 208839909
2018-08-30 23:10:06.99>
2018-08-30 23:16:46.08>db2 "reorg table admin.SSQ_20180830 resetdictionary"
DB20000I  REORG 命令成功完成。
2018-08-31  1:20:08.26>

db2pd -alldbs -reorgs

SELECT tabname,sum(DATA_OBJECT_p_SIZE) /1024 MB, sum(DATA_OBJECT_P_SIZE + INDEX_OBJECT_P_SIZE + LONG_OBJECT_P_SIZE +
              LOB_OBJECT_P_SIZE + XML_OBJECT_P_SIZE) /1024 AS TOTAL_P_SIZE --,t.*
FROM TABLE (SYSPROC.ADMIN_GET_TAB_INFO('ADMIN','SSQ_20180830')) AS T  group by t.tabname;
-- 7G->4283MB


	 行压缩 compress yes , 值压缩 COMPRESS YES VALUE COMPRESSION
--db2 "alter table admin.SSQ_20180830 compress yes"

--https://www.ibm.com/developerworks/cn/data/library/techarticles/dm-0707schurr/ DB2 9：行压缩与大型 RID
db2 "reorg table admin.SSQ_20180830 resetdictionary"
--db2 "reorgchk update statistics on table admin.SSQ_20180830"
	 	 
    */

	public static void main1(String[] args) throws Exception{
//		ArrayList<Integer> ssq_red_start = new ArrayList<Integer>();
//		randomBall(ssq_red_start,6,33);
//		System.out.println(ssq_red_start);
//
//		ArrayList<Integer> ssq_blue_start = new ArrayList<Integer>();
//		randomBall(ssq_blue_start,1,16);
//		System.out.println(ssq_blue_start);
		char fChar = '\\';
		System.out.println("\'\"`");
		System.out.println("'\"`");
		if ("'\"`".indexOf(fChar)>=0){
			System.out.println("aaa");
		}
		
		if(1==1)return;
		
		StringBuilder sb = new StringBuilder();		
		FileOutputStream fos = new FileOutputStream("e:\\ssq_random_20180830_1.txt");
		Random mRand = new Random();
		for (int i = 0; i < 7693; i++) {
			int getGroup = mRand.nextInt(54109);
			System.out.println("i="+i + "  group="+getGroup);
			randomSSQ(getGroup,fos,sb);
		}
		fos.close();
//		System.out.println(sb.toString());
	}
	
	// public int nextInt(int n) 该方法的作用是生成一个随机的int值，该值介于[0,n)的区间，也就是0到n之间的随机int值，包含0而不包含n。
	public static void randomSSQ(int getGroup,FileOutputStream fos,StringBuilder sb) throws Exception{
		int group = getGroup;
		
		byte[] arr = new byte[7*group];
		Random rand = new Random();
		int num = -1;
		for (int i = 0; i < group; i++) { 
			
			boolean[] bool = new boolean[33+1];
			// 6个红球
			for (int j = 0; j < 6; j++) {
				do{
					num = rand.nextInt(33+1);
					//System.out.println("red=" + num);
				}while(num==0|| bool[num]);//(isTrue && bool[num])||(!isTrue&&!bool[num]));
				bool[num] = true;
				arr[i*7+j] = (byte)num;
			}
			Arrays.sort(arr,i*7,i*7+6);
			
			// 1个蓝球
			do{
				num = rand.nextInt(16+1);
				//System.out.println("blue=" + num);
			}while(num==0);
			arr[i*7+6] = (byte)num; 
		}
		
		sb.delete(0, sb.length());
		for (int i = 0; i < arr.length; i++) {
			byte b=arr[i];
			boolean issplit = (i+1)%7==0;
			sb.append(b<10?"0"+b:b).append(!issplit?" ":"\n");
			
//			System.out.print((b<10?"0"+b:b) + (!issplit?" ":""));
//			if (issplit){
//				System.out.println();
//			}
		}
		fos.write(sb.toString().getBytes());
		
		
	}
	
	/**
     * 机选号码
     *
     * @param list
     * @param num     机选几个
     * @param ballNum 在多少个数里选
     */
    public static void randomBall(ArrayList<Integer> list, int num, int ballNum) {
        list.clear();
        Random random = new Random();
        boolean[] bool = new boolean[ballNum+1];
        int randInt = 0;
        for (int j = 0; j < num; j++) {
            do {
                randInt = random.nextInt(ballNum+1);
            } while (randInt==0 || bool[randInt]);
            bool[randInt] = true;
            list.add(randInt);
        }
        Collections.sort(list);
    }
    
    
    
    
    public static void randomSSQ1(int getGroup,FileOutputStream fos,StringBuilder sb) throws Exception{
    	
    	
    	
		int group = getGroup;
		
		byte[] arr = new byte[7*group];
		Random rand = new Random();
		int num = -1;
		for (int i = 0; i < group; i++) { 
			
			boolean[] bool = new boolean[33+1];
			// 6个红球
			for (int j = 0; j < 6; j++) {
				do{
					num = rand.nextInt(33+1);
					//System.out.println("red=" + num);
				}while(num==0|| bool[num]);//(isTrue && bool[num])||(!isTrue&&!bool[num]));
				bool[num] = true;
				arr[i*7+j] = (byte)num;
			}
			Arrays.sort(arr,i*7,i*7+6);
			
			// 1个蓝球
			do{
				num = rand.nextInt(16+1);
				//System.out.println("blue=" + num);
			}while(num==0);
			arr[i*7+6] = (byte)num; 
		}
		
		sb.delete(0, sb.length());
		for (int i = 0; i < arr.length; i++) {
			byte b=arr[i];
			boolean issplit = (i+1)%7==0;
			sb.append(b<10?"0"+b:b).append(!issplit?" ":"\n");
			
//			System.out.print((b<10?"0"+b:b) + (!issplit?" ":""));
//			if (issplit){
//				System.out.println();
//			}
		}
		fos.write(sb.toString().getBytes());
		
		
	}
    
    public static void arrayToStrng (byte[] arr,List<String> ssq){
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < arr.length; i++) {
			byte b=arr[i];
			boolean issplit = (i+1)%7==0;
			sb.append(b<10?"0"+b:b);
			if (issplit){
				// 完成一组
				ssq.add(sb.toString());
				sb.delete(0, sb.length());
			}else{
				sb.append(",");
			}
		}   
    }
    
    public static void sleep_my(long v){
    	try {
			Thread.sleep(v);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    }
    
    
    public static class GenSSQ extends Thread {
    	byte[] arr; 
    	int group;
    	public GenSSQ(byte[] arr,int group){
    		this.arr = arr;
    		this.group = group;
    	}
    	@Override
    	public void run() {
    		
    		//byte[] arr = new byte[7*group];
    		Random rand = new Random();
    		int num = -1;
    		for (int i = 0; i < group; i++) { 
    			
    			if (i%18 == 0){
    				sleep_my(500);
    				rand = new Random();
    				int r = rand.nextInt(270);
    				sleep_my(r);
    				r = rand.nextInt(145);
    				sleep_my(r);
    			}
    			
    			boolean[] bool = new boolean[33+1];
    			// 6个红球
    			for (int j = 0; j < 6; j++) {
    				do{
    					num = rand.nextInt(33+1);
    					//System.out.println("red=" + num);
    				}while(num==0|| bool[num]);//(isTrue && bool[num])||(!isTrue&&!bool[num]));
    				bool[num] = true;
    				arr[i*7+j] = (byte)num;
    			}
    			Arrays.sort(arr,i*7,i*7+6);

    			sleep_my(num);
    			// 1个蓝球
    			do{
    				num = rand.nextInt(16+1);
    				//System.out.println("blue=" + num);
    			}while(num==0);
    			arr[i*7+6] = (byte)num; 
    		}
    	}
    }
    
    public static String CREATE_TEMP = "declare global temporary table session.temp_id as (select a.*,cast('' as varchar(20)) pk_s from fssb.t_ssq_rand a) definition only with replace on commit preserve rows not logged";
//    public static String INSERT_SQL = "insert into session.temp_id(r1,r2,r3,r4,r5,r6,b1,pk,sl0928) VALUES (?,?,?,?,?,?,?,?,?)";
    public static String INSERT_SQL = "insert into session.temp_id(r1,r2,r3,r4,r5,r6,b1,pk,pk_s) VALUES (?,?,?,?,?,?,?,?,?)";
    public static String CALL_PROC = "{call fssb.ups_t_ssq_rand_pk()}";

    public static String URL = "jdbc:db2://127.0.0.1:50000/test";
    public static String USER = "db2admin";
    public static String PASSWORD = "db2admin";
    
    public static class SaveSSQ extends Thread {
    	byte[] arr; 
    	int group;
    	int level;
    	public SaveSSQ(int level,int group){
    		this.level=level;
    		this.group = group;
    	}
    	@Override
    	public void run() {
    		
    		for (int n = 1; n <= level; n++) {
    		
	    		System.out.println("thread-id:" + getId() + ",level=" + n + ",group=" + group + " start");
	    		
	    		byte[] arr = new byte[7*group];
	    		GenSSQ ssq = new GenSSQ(arr,group);
	        	ssq.start();
	        	try {
					ssq.join();
				} catch (InterruptedException e2) {
					e2.printStackTrace();
					return;
				}
	        	System.out.println("thread-id:" + getId() + ",level=" + n + ",group=" + group + " get ssq end");
	    		
	    		Connection conn = null;
	    		try{	    		
	    			
	    			conn = DriverManager.getConnection(URL, USER, PASSWORD);
	    			
		    		Statement stmt = conn.createStatement();
		    		stmt.execute(CREATE_TEMP);
		    		stmt.close();
		    		
		    		PreparedStatement pstmt = conn.prepareStatement(INSERT_SQL);
		    		conn.setAutoCommit(false);
		    		
		    		StringBuilder sb = new StringBuilder();
		    		int count = 1;
		    		for (int i = 0; i < arr.length; i++) {
		    			byte b=arr[i];
		    			boolean issplit = (i+1)%7==0;
		    			sb.append(b<10?"0"+b:b);
		    			//System.out.println(count);
		    			if (issplit){
		    				// 完成一组
		    				//System.out.println(i);
		    				pstmt.setObject(count, b);
		    				count++;
		    				pstmt.setObject(count, 0L);
		    				count++;
		    				pstmt.setObject(count, sb.toString());
	//	    				pstmt.setLong(count, 1L);
		    				count = 1;
		    				if (i < arr.length) pstmt.addBatch();
	//	    				
	//	    				System.out.println("insert into session.temp_id(r1,r2,r3,r4,r5,r6,b1,pk_s) values("+sb.toString()+",0,'"
	//	    						+sb.toString().replace(",", "")+"')");
	//	    				stmt.execute("insert into session.temp_id(r1,r2,r3,r4,r5,r6,b1,pk,pk_s) values("+sb.toString()+",0,'"
	//	    						+sb.toString().replace(",", "")+"')");
		    				
		    				sb.delete(0, sb.length());
		    			}else{
		    				//sb.append(",");
		    				pstmt.setObject(count, b);
		    				count++;
		    			}
		    		}
		    		
		    		pstmt.executeBatch();
		    		conn.commit();
		    		
		    		CallableStatement call = conn.prepareCall(CALL_PROC);
		    		call.execute();
		    		conn.commit();
		    		
		    		conn.setAutoCommit(true);
	    		}catch (Exception e) {
	    			try {if(conn!=null){ conn.rollback();} } catch (SQLException e1) {}
	    			//SqlException e1 = (SqlException) e;
	    			//System.out.println(e1.getErrorCode());
	    			//e1.getNextException().printStackTrace();
	    			System.out.println("thread-id:" + getId() + " " + e.getMessage());
	    			e.printStackTrace();
	    		}finally{
	    			if(conn!=null){
	    				try {conn.close();} catch (SQLException e) {}
	    			}
	
	            	System.out.println("thread-id:" + getId() + ",level=" + n + ",group=" + group + " save ssq end");
	    		}

			}
    	}
    }
    
    
    public static void main(String[] args) throws Exception{
    	
    	Class.forName("com.ibm.db2.jcc.DB2Driver");
    	
    	int group = 29;
//    	byte[] arr = new byte[7*group];
//    	GenSSQ ssq = new GenSSQ(arr,group);
//    	ssq.start();
//    	ssq.join();
//    	
//    	List<String> out = new ArrayList<String>();
//    	
//    	arrayToStrng(arr, out);
//    	
//    	for (String string : out) {
//    		System.out.println(string);
//			
//		}
    	//for (int j = 0; j < 100; j++) {
	    	SaveSSQ[] runs = new SaveSSQ[30];
	    	for (int i = 0; i < runs.length; i++) {
	    		Random rand = new Random();
	    		rand.nextInt();rand.nextInt();rand.nextInt();
	    		int r = rand.nextInt(5790);
	    		sleep_my(r);rand.nextInt();
				runs[i] = new SaveSSQ(rand.nextInt(478493),rand.nextInt(4391));
				runs[i].start();			
			}
	    	
	    	
    	//}
    	//SaveSSQ ssq1 = new SaveSSQ(1200);
    	//ssq1.start();    	
    	//ssq1.join();
    	
    }
}

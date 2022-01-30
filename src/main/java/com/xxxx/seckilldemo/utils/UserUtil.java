package com.xxxx.seckilldemo.utils;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.xxxx.seckilldemo.pojo.User;
import com.xxxx.seckilldemo.vo.RespBean;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

//生成用户工具类
public class UserUtil {
    private static void createUser(int count) throws Exception {

        //生成用户
        List<User> users=new ArrayList<>(count);
        for(int i=0;i<count;i++){
            User user=new User();
            user.setId(1300000000L+i);
            user.setLoginCount(1);
            user.setNickname("user"+i);
            user.setRegisterDate(new Date());
            user.setSalt("1a2b3c4d");
            user.setPassword(MD5Util.inputPassToDBPass("123456",user.getSalt()));
            users.add(user);
        }
        System.out.println("create user");

        //将生成的用户插入数据库中
        Connection conn=getConn();
        String sql="insert into t_user(login_count,nickname,register_date,salt,password,id)values(?,?,?,?,?,?)";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        for(int i=0;i<users.size();i++) {
            User user = users.get(i);
            pstmt.setInt(1,user.getLoginCount());
            pstmt.setString(2,user.getNickname());
            pstmt.setTimestamp(3,new Timestamp(user.getRegisterDate().getTime()));
            pstmt.setString(4,user.getSalt());
            pstmt.setString(5,user.getPassword());
            pstmt.setLong(6,user.getId());
            pstmt.addBatch();
        }
        pstmt.executeBatch();
        pstmt.close();
        conn.close();
        System.out.println("insert to db");

        //登录，生成userTicket
        String urlString ="http://localhost:8089/login/doLogin";
        File file=new File("C:\\Users\\11349\\Desktop\\config.txt");
        if(file.exists()){
            file.delete();
        }
        RandomAccessFile raf=new RandomAccessFile(file,"rw");
        file.createNewFile();
        raf.seek(0);
        for(int i=0;i<users.size();i++){
            User user = users.get(i);
            URL url = new URL(urlString);
            HttpURLConnection co=(HttpURLConnection)url.openConnection();
            co.setRequestMethod("POST");//登录用POST请求
            co.setDoOutput(true);
            OutputStream out=co.getOutputStream();
            String params="mobile="+user.getId()+"&password="+MD5Util.inputPassToFromPass("123456");
            out.write(params.getBytes());
            out.flush();
            InputStream inputStream = co.getInputStream();
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            byte[] buff=new byte[1024];
            int len=0;
            while((len=inputStream.read(buff))>=0){
                bout.write(buff,0,len);
            }
            inputStream.close();
            bout.close();
            String response=new String(bout.toByteArray());
            ObjectMapper mapper = new ObjectMapper();
            RespBean respBean = mapper.readValue(response, RespBean.class);
            String userTicket=((String)respBean.getObj());
            System.out.println("create userTicket"+user.getId());

            String row=user.getId()+","+userTicket;
            raf.seek(raf.length());
            raf.write(row.getBytes());
            raf.write("\r\n".getBytes());
            System.out.println("write to file:"+user.getId());
        }
        raf.close();
        System.out.println("over");
    }
    //获取数据库连接的方法
    private static Connection getConn() throws Exception {
        String url="jdbc:mysql://localhost:3309/seckill?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia"
                +"/Shanghai";
        String username="root";
        String password="123456";
        String driver="com.mysql.jdbc.Driver";
        Class.forName(driver);
        return DriverManager.getConnection(url,username,password);
    }

    public static void main(String[] args) throws Exception {
        createUser(5000);
    }
}

package com.trei.cerberus.db;

import java.sql.*;

public class SQLiteJDBCcreateTable{

    public static void main( String args[] )
      {
        Connection c = null;
        Statement stmt = null;
        try {
          Class.forName("org.sqlite.JDBC");
          c = DriverManager.getConnection("jdbc:sqlite:test.db");
          System.out.println("Opened database successfully");
    
          stmt = c.createStatement();
          String sql = "CREATE TABLE COMPANY " +
                       "(ID INT PRIMARY KEY     NOT NULL," +
                       " NAME           TEXT    NOT NULL, " + 
                       " AGE            INT     NOT NULL, " + 
                       " ADDRESS        CHAR(50), " + 
                       " SALARY         REAL)";
/*
          String sql = "CREATE TABLE Staff "+
                       "( firstName  TEXT NOT NULL, " +
                       "  patronym   TEXT, " +
                       "  lastName   TEXT NOT NULL, " +
                       "  birthday   TEXT, " + // format YYYYMMDD
                       "  position   TEXT, " +
                       "  department TEXT, " +
                       "  tableId    TEXT, " +
                       "  uid        TEXT, " +
                       "  PRIMARY KEY (firstName, lastName, birthday))";

          String sql = "CREATE TABLE RfidTag "+
                       "( uid             TEXT NOT NULL, " +
                       "  type            TEXT, " +
                       "  protocol        TEXT, " +
                       "  issueDate       TEXT, " + // format YYYYMMDD
                       "  expirationDate  TEXT, " + // format YYYYMMDD
                       "  PRIMARY KEY (uid) )";
*/

          stmt.executeUpdate(sql);    
          stmt.close();    
          c.close();
        } catch ( Exception e ) {
          System.err.println( e.getClass().getName() + ": " + e.getMessage() );
          System.exit(0);
        }
        System.out.println("Table created successfully");
      }
}


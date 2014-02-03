package com.trei.cerberus.db;

import java.sql.*;

public class SelectStaffDB
{
  public static void main( String args[] )
  {
    Connection c = null;
    Statement stmt = null;
    try {
      Class.forName("org.sqlite.JDBC");

      c = DriverManager.getConnection("jdbc:sqlite:STAFF_DB.3db");
      //c = DriverManager.getConnection("jdbc:sqlite:STAFF_DB.3db");
      //c = DriverManager.getConnection("jdbc:sqlite:test.db");
      c.setAutoCommit(false);
      System.out.println("Opened database successfully");

      stmt = c.createStatement();
      //ResultSet rs = stmt.executeQuery( "SELECT * FROM COMPANY;" );
      ResultSet rs = stmt.executeQuery( "SELECT * FROM STAFF;" );
      //ResultSet rs = stmt.executeQuery( "SELECT * FROM Attendance;" );
      while ( rs.next() ) {
         String  uid        = rs.getString("UID");
         String  lastName   = rs.getString("Last_Name");
         String  firstName  = rs.getString("First_Name");
         String  patronymic = rs.getString("Patronymic");
         String  birthDate  = rs.getString("Birth_Date");
         String  tableID    = rs.getString("Table_ID");

         System.out.println( "UID = " + uid  );
         System.out.println( "FIRST_NAME = " + firstName );
         System.out.println( "Patronymic = " + patronymic );
         System.out.println( "LAST_NAME = "  + lastName );
         System.out.println( "BIRTH_DATE = " + birthDate );
         System.out.println( "TABLE_ID = " + tableID );
         System.out.println();
      }
      rs.close();
      stmt.close();
      c.close();
    } catch ( Exception e ) {
      System.err.println( e.getClass().getName() + ": " + e.getMessage() );
      System.exit(0);
    }
    System.out.println("Operation done successfully");
  }
}

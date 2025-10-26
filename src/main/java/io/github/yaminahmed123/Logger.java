package io.github.yaminahmed123;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

// The purpose of this class is to make debug logging and server/client logging more easy
public abstract class Logger {
    // ANSI escape codes
    private static final String red = "\u001B[31m";
    private static final String green = "\u001B[32m";
    private static final String yellow = "\u001B[33m";
    private static final String reset = "\u001B[0m"; // reset color

    private static final String space = "         ";

    private static String bracketString(String str){
        int local_char_limit = 20;
        int l = str.length();
        String lts = getTime();
        int lt = lts.length();
        int addition = local_char_limit-(lt+l);
        String space = "";
        for(int i = 0; i < addition; ++i){
            space += " ";
        }
        String result = str+space+lts;
        return "["+result+"]";
    }

    private static String getTime(){
        LocalTime time = LocalTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        return " "+time.format(formatter);
    }


    public static boolean DEBUG_LOG = true; // set this to false so the logger will not log debug states.

    // use this function to log things from a function
    public static void FUNCTION_LOG(String f_name, String msg){
        System.out.println(green+bracketString(f_name)+space+msg+reset);
    }

    // use this function to log things from a function in debug mode
    public static void FUNCTION_LOG_D(String f_name, String msg){
        if(DEBUG_LOG){
            System.out.println(yellow+bracketString(f_name)+space+msg+reset);
        }
    }

    public static void APPLICATION_LOG(String application, String msg){
        System.out.println(green+bracketString(application)+space+msg+reset);
    }

    public static void ERROR_LOG(String f_name, String msg){
        System.out.println(red+bracketString(f_name)+space+msg+reset);
    }
}

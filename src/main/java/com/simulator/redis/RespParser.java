package com.simulator.redis;

import java.util.ArrayList;
import java.util.List;

public class RespParser {
    public static List<String> parse(String input) {
        List<String> result = new ArrayList<>();
        String[] lines = input.split("\r\n");
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].startsWith("$") && i + 1 < lines.length) {
                result.add(lines[++i]);    
            }
        }
        return result;
    }  
}

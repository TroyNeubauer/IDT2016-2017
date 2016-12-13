package com.ohsidt.test;

import java.util.*;

public class ArrayUtil<T> {

	public static <T> List<T> toArrayList(T[] args) {
		List<T> list = new ArrayList<T>(args.length);
		for(T s : args){
			list.add(s);
		}
		return list;
	}
	
	public static String concat(String seperator, String... array){
		String result = new String();
		for(int i = 0; i < array.length; i++){
			result += array[i] + seperator;
		}
		return result;
	}

}

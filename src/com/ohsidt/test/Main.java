package com.ohsidt.test;

import com.troy.troyberry.utils.BooleanArray;
import com.troy.troyberry.utils.StringFormatter;

public class Main {

	public static void main(String[] args) {

		Executor e = new Executor ("Mr. Lau class test.jar", new Action() {
			
			@Override
			public void onAction() {
				System.out.println("process ended!");
			}
		});
	}

}

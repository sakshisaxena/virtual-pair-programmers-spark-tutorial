package com.virtualpairprogrammers;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;

import scala.Tuple2;
import scala.Tuple22;


public class Main {
	
	public static void main(String[] args) {
		List<Integer> inputData = new ArrayList<>();
		inputData.add(35);
		inputData.add(12);
		inputData.add(90);
		inputData.add(20);
		Logger.getLogger("org.apache").setLevel(Level.WARN);
		SparkConf conf = new SparkConf().setAppName("startingSpark").setMaster("local[*]");   //if we mention only "local" the spark program will only run in a single thread so will not get full performance of the machine
																							  // local[*] says to use all the available cores in this machine to run the program			
		JavaSparkContext sc = new JavaSparkContext(conf);
		JavaRDD<Integer> originalRdd = sc.parallelize(inputData);
		Integer result = originalRdd.reduce((value1,value2) -> value1+value2);  //Return type of reduce function is same as input type
		JavaRDD<Double> sqrtRdd = originalRdd.map( value -> Math.sqrt(value));
		
		sqrtRdd.foreach(System.out::println);
		
		//Find number of elements in sqrtRdd
		System.out.println("Num of elements:" + sqrtRdd.count());
		
		//count using map and reduce
		Long count = sqrtRdd.map(value -> 1L).reduce((a,b) -> a + b);
		System.out.println("count is:" + count);
		
		System.out.println("Result is :" + result);
		
		
		JavaRDD<Tuple2<Integer, Double>> sqrtRdd2 = originalRdd.map(value -> new Tuple2<Integer,Double> (value, Math.sqrt(value)));
		
		Tuple2<Integer, Double> myValue = new Tuple2<>(9, 3.0);
		
		//new Tuple22<>(_1, _2, _3, _4, _5, _6, _7, _8, _9, _10, _11, _12, _13, _14, _15, _16, _17, _18, _19, _20, _21, _22)  exists
		//A tuple in Scala is a lightweight syntax and good syntax to store data in a table format
		
		sc.close();
	}
}

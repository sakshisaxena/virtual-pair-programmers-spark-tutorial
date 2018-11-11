package com.virtualpairprogrammers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;


public class FileInSpark {
	public static void main(String[] args) {
		
		Logger.getLogger("org.apache").setLevel(Level.WARN);
		
		SparkConf conf = new SparkConf().setAppName("startingSpark").setMaster("local[*]");
		JavaSparkContext sc = new JavaSparkContext(conf);
		
		/*JavaRDD<String> sentences = sc.parallelize(inputData);
		JavaRDD<String> words = sentences.flatMap(value -> Arrays.asList(value.split(" ")).iterator());
		words = words.filter(value -> value.length()>1);
		words.foreach(System.out::println);*/
		
		JavaRDD<String> initialRdd =sc.textFile("src/main/resources/subtitles/input.txt");
		
		initialRdd
		  .flatMap(value -> Arrays.asList(value.split(" ")).iterator())
		  //.filter(value -> value.length()>1)
		  .foreach(System.out::println);
		
		sc.close();
	}
}

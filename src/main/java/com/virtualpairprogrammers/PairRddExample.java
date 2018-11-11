package com.virtualpairprogrammers;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;

import scala.Tuple2;



public class PairRddExample {
		@SuppressWarnings("resource")
		public static void main( String[] args) {
			List<String> inputData = new ArrayList<>();
			inputData.add("WARN: Tuesday 4 September 0405");
			inputData.add("ERROR: Tuesday 4 September 0408");
			inputData.add("FATAL: Wednesday 5 September 1632");
			inputData.add("ERROR: Friday 7 September 1854");
			inputData.add("WARN: Saturday 8 September 1942");
			
			Logger.getLogger("org.apache").setLevel(Level.WARN);
			
			SparkConf conf = new SparkConf().setAppName("startingSpark").setMaster("local[*]");
			JavaSparkContext sc = new JavaSparkContext(conf);
			
			JavaRDD<String> origMessages = sc.parallelize(inputData);
			
			//AIM: To find the number of msgs in each log level
			JavaPairRDD<String, String> logs = origMessages.mapToPair( rawValue -> {
				String arr[] = rawValue.split(":");
				String level = arr[0];
				String msg = arr[1];
				
				return new Tuple2<>(level, msg);
			});	
			
			JavaPairRDD<String, Long> logsCount = origMessages.mapToPair( rawValue -> {
				String arr[] = rawValue.split(":");
				String level = arr[0];
				
				return new Tuple2<>(level, 1L);
			});	
			
			JavaPairRDD<String, Long> sumCount = logsCount.reduceByKey((value1, value2) -> value1+value2);
			
			sumCount.foreach(tuple -> System.out.println(tuple._1 + " " + tuple._2));
			
			//in single line
			sc.parallelize(inputData).mapToPair(rawValue -> new Tuple2<>(rawValue.split(":")[0], 1L))
			.reduceByKey((value1, value2) -> value1+value2)
			.foreach(tuple -> System.out.println(tuple._1 +" has " + tuple._2 +" instances"));;
			
			sc.close();
		}	
}

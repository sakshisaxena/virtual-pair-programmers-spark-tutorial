package com.virtualpairprogrammers;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;

import scala.Tuple2;


public class KeywordRanking {
public static void main(String[] args) {
		
		Logger.getLogger("org.apache").setLevel(Level.WARN);
		
		SparkConf conf = new SparkConf().setAppName("startingSpark").setMaster("local[*]");
		JavaSparkContext sc = new JavaSparkContext(conf);
		
		/*JavaRDD<String> sentences = sc.parallelize(inputData);
		JavaRDD<String> words = sentences.flatMap(value -> Arrays.asList(value.split(" ")).iterator());
		words = words.filter(value -> value.length()>1);
		words.foreach(System.out::println);*/
		
		JavaRDD<String> initialRdd =sc.textFile("src/main/resources/subtitles/input.txt");
		JavaRDD<String> onlyLettersRdd = initialRdd.map(sentence -> sentence.replaceAll("[^a-zA-Z\\s]", "").toLowerCase());  // \s is space in regex
		
		Util utilObj = new Util();
		
		JavaRDD<String> notBoringRdd = onlyLettersRdd
		  .flatMap(value -> Arrays.asList(value.split(" ")).iterator())
		  .filter(value -> value.length()>0)
		  .filter(value -> utilObj.isNotBoring(value));
		
		KeywordRanking mainObj = new KeywordRanking();
		
		JavaPairRDD<String, Long> totals = notBoringRdd.mapToPair(
				value -> new Tuple2<String, Long> (value, 1L))
				.reduceByKey((value1, value2) -> value1+value2);
				//.takeOrdered(10, mainObj.new TupleComparator())
				//.foreach(tuple2 -> System.out.println(tuple2._1 +" has " + tuple2._2 + " occurences"));
		
		JavaPairRDD<Long, String> switched = totals.mapToPair(tupleVal -> new Tuple2<Long, String> (tupleVal._2, tupleVal._1));
		
		JavaPairRDD<Long, String> sorted = switched.sortByKey(false);
		System.out.println("Number of partitions :" + sorted.getNumPartitions());   // Minimum size of data should be 64 MB to get a partition 
		//sorted.foreach(System.out::println); // forEach sends a thread to each partition and threads don't execute sequentially
		sorted.take(10).forEach(System.out::println);
		sc.close();
	
}
}
	/*class TupleComparator implements Comparator<Tuple2<String, Long>>, Serializable {

		 (non-Javadoc)
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 
		@Override
		public int compare(Tuple2<String, Long> o1, Tuple2<String, Long> o2) {
			// TODO Auto-generated method stub
			return o1._2.compareTo(o2._2);
		}
	}*/




/*
 * [Stage 0:>                                                          (0 + 2) / 2]
 * [Stage 0:>                                                          (a + b) / c]
 * a = number of partitions/tasks completed
 * b = number of partitions actively under process (number of tasks)  - tells the number of cores under use in the hardware on which Spark is running, this keep reducing with less data to process
 * c = number of total partitions, each of size 64MB
 * */
 
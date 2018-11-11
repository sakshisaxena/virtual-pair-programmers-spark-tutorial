package com.virtualpairprogrammers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaSparkContext;

import scala.Tuple2;

/**
 * This class is used in the chapter late in the course where we analyse viewing figures.
 * You can ignore until then.
 */
public class ViewingFigures 
{
	@SuppressWarnings("resource")
	public static void main(String[] args)
	{
		System.setProperty("hadoop.home.dir", "c:/hadoop");
		Logger.getLogger("org.apache").setLevel(Level.WARN);

		SparkConf conf = new SparkConf().setAppName("startingSpark").setMaster("local[*]");
		JavaSparkContext sc = new JavaSparkContext(conf);
		
		// Use true to use hardcoded data identical to that in the PDF guide.
		boolean testMode = false;
		
		JavaPairRDD<Integer, Integer> viewData = setUpViewDataRdd(sc, testMode);
		JavaPairRDD<Integer, Integer> chapterData = setUpChapterDataRdd(sc, testMode);
		JavaPairRDD<Integer, String> titlesData = setUpTitlesDataRdd(sc, testMode);

		// TODO - over to you!
		
		JavaPairRDD<Integer, Integer> countChapter = chapterData.mapToPair(value1 -> new Tuple2<Integer,Integer> (value1._2, 1))
													.reduceByKey((value1, value2) -> value1 + value2);
		//countChapter.foreach(System.out::println);
		
		//Step 1: Remove duplicates
		viewData = viewData.distinct();
		JavaPairRDD<Integer, Integer> reverseViewData = viewData.mapToPair(row ->
				new Tuple2(row._2, row._1));
				
		//Step 2: Form a map of user, course id
	
		JavaPairRDD<Integer, Tuple2<Integer, Integer>> courseViewDataJoin = reverseViewData.join(chapterData);
		//courseViewDataJoin.foreach(System.out::println);
		/*
		 * [userid, coursetitle]
		 */
		JavaPairRDD<Integer, Integer> courseViewData = courseViewDataJoin.mapToPair(
				row -> row._2);
		
		
		//Step 3: Reverse the Pair RDD [coursetitle, userid], count views per
		JavaPairRDD<Tuple2<Integer, Integer>, Integer> courseToUser = courseViewData.mapToPair(row ->
		new Tuple2 (new Tuple2<Integer, Integer> (row._2, row._1), 1));
		//Step 4: Reduce by key to get number of chapters visited in a course by each user
		courseToUser = courseToUser.reduceByKey((value1, value2) -> value1 + value2);
		
		JavaPairRDD<Integer, Integer> coursePerUserCount = courseToUser.mapToPair(
				row -> new Tuple2<Integer, Integer> (row._1._1, row._2));
		//coursePerUserCount.foreach(System.out::println);
		HashMap<Integer, Double> courseCompletePerUser = new HashMap<Integer, Double>();
		
		
		for(Tuple2 row : coursePerUserCount.collect()) {
				int courseid = row._1$mcI$sp();
				JavaPairRDD<Integer, Integer> getChapCount = countChapter.filter(valueRow -> valueRow._1 == courseid);
				double value = (double) row._2$mcI$sp()/ (double) getChapCount.first()._2$mcI$sp() * 100;
				int score = 0;
				if(value>90)
					score = 10;
				else if (value > 50 && value < 90)
					score = 4;
				else if (value > 25 && value < 50)
					score = 2;
				double finalscore = courseCompletePerUser.getOrDefault(courseid, 0.0) + score;
				courseCompletePerUser.put(courseid, finalscore);
				//System.out.println("Course :" + row._1 + " " + "Percentage Complete: " + finalscore);
		};
		List<Tuple2<Integer, Integer>> courseScoring = new ArrayList<>();
		for(Map.Entry<Integer, Double> entry : courseCompletePerUser.entrySet()) {
			courseScoring.add(new Tuple2(entry.getKey(), entry.getValue()));
		}
		JavaPairRDD<Integer, Integer> courseScoringRdd = sc.parallelizePairs(courseScoring);
		JavaPairRDD<Integer, String> titleScoring = courseScoringRdd.join(titlesData).mapToPair(row -> (Tuple2<Integer, String>) row._2);
		titleScoring.sortByKey().collect().forEach(System.out::println);
		
		Scanner scanner = new Scanner(System.in);
		scanner.nextLine();
		sc.close();
	}

	private static JavaPairRDD<Integer, String> setUpTitlesDataRdd(JavaSparkContext sc, boolean testMode) {
		
		if (testMode)
		{
			// (chapterId, title)
			List<Tuple2<Integer, String>> rawTitles = new ArrayList<>();
			rawTitles.add(new Tuple2<>(1, "How to find a better job"));
			rawTitles.add(new Tuple2<>(2, "Work faster harder smarter until you drop"));
			rawTitles.add(new Tuple2<>(3, "Content Creation is a Mug's Game"));
			return sc.parallelizePairs(rawTitles);
		}
		return sc.textFile("src/main/resources/viewing figures/titles.csv")
				                                    .mapToPair(commaSeparatedLine -> {
														String[] cols = commaSeparatedLine.split(",");
														return new Tuple2<Integer, String>(new Integer(cols[0]),cols[1]);
				                                    });
	}

	private static JavaPairRDD<Integer, Integer> setUpChapterDataRdd(JavaSparkContext sc, boolean testMode) {
		
		if (testMode)
		{
			// (chapterId, (courseId, courseTitle))
			List<Tuple2<Integer, Integer>> rawChapterData = new ArrayList<>();
			rawChapterData.add(new Tuple2<>(96,  1));
			rawChapterData.add(new Tuple2<>(97,  1));
			rawChapterData.add(new Tuple2<>(98,  1));
			rawChapterData.add(new Tuple2<>(99,  2));
			rawChapterData.add(new Tuple2<>(100, 3));
			rawChapterData.add(new Tuple2<>(101, 3));
			rawChapterData.add(new Tuple2<>(102, 3));
			rawChapterData.add(new Tuple2<>(103, 3));
			rawChapterData.add(new Tuple2<>(104, 3));
			rawChapterData.add(new Tuple2<>(105, 3));
			rawChapterData.add(new Tuple2<>(106, 3));
			rawChapterData.add(new Tuple2<>(107, 3));
			rawChapterData.add(new Tuple2<>(108, 3));
			rawChapterData.add(new Tuple2<>(109, 3));
			return sc.parallelizePairs(rawChapterData);
		}

		return sc.textFile("src/main/resources/viewing figures/chapters.csv")
													  .mapToPair(commaSeparatedLine -> {
															String[] cols = commaSeparatedLine.split(",");
															return new Tuple2<Integer, Integer>(new Integer(cols[0]), new Integer(cols[1]));
													  	});
	}

	private static JavaPairRDD<Integer, Integer> setUpViewDataRdd(JavaSparkContext sc, boolean testMode) {
		
		if (testMode)
		{
			// Chapter views - (userId, chapterId)
			List<Tuple2<Integer, Integer>> rawViewData = new ArrayList<>();
			rawViewData.add(new Tuple2<>(14, 96));
			rawViewData.add(new Tuple2<>(14, 97));
			rawViewData.add(new Tuple2<>(13, 96));
			rawViewData.add(new Tuple2<>(13, 96));
			rawViewData.add(new Tuple2<>(13, 96));
			rawViewData.add(new Tuple2<>(14, 99));
			rawViewData.add(new Tuple2<>(13, 100));
			return  sc.parallelizePairs(rawViewData);
		}
		
		return sc.textFile("src/main/resources/viewing figures/views-*.csv")
				     .mapToPair(commaSeparatedLine -> {
				    	 String[] columns = commaSeparatedLine.split(",");
				    	 return new Tuple2<Integer, Integer>(new Integer(columns[0]), new Integer(columns[1]));
				     });
	}
}

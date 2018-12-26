package com.euge.hadoop;

import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

public class WordCount {

	//Mapper
	//Takes as input a {Long,Text} and produces and intermediate {Text, Int}
	public static class Map extends Mapper<LongWritable, Text, Text, IntWritable> {
		private final static IntWritable one = new IntWritable(1);
		private final Text word = new Text();

		//For each record in the imput file, this method is called
		//The method does not return anything, but we will add in the context whatever we need to pass forward to the reducer
		@Override
		public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
			//Each imput is a key value pair. We take the value part and cast it as string
			final String line = value.toString();
			//We tokenize the value
			final StringTokenizer tokenizer = new StringTokenizer(line);
			while (tokenizer.hasMoreTokens()) {
				final String next = tokenizer.nextToken();
				//We will output any of the tokenized values that contains the word dfs
				if (next.contains("dfs")){
					word.set(next);
					context.write(word, one);
				}
			}
		}
	}

	//Reducer
	//Takes as input an {Text, Int} and produces a {Text, Int}
	//The input of the reducer is the output of the mapper
	//Hadoop will merge all the outpues, and sort them so we can be assured that a give key will only be present once in the input
	//and that the value will be the collection of all the values found for that key in the output of the different mappers
	//We can also ensure that the inputs will be sorted by key
	public static class Reduce extends Reducer<Text, IntWritable, Text, IntWritable> {

		//Called for each of the inputs of the reducer
		//The outputs of the reducer will be passed along via the context, as we did with the mapper
		@Override
		public void reduce(Text key, Iterable<IntWritable> values, Context context)
				throws IOException, InterruptedException {
			int sum = 0;
			//We iterate through all the values collected for the input key
			for (final IntWritable val : values) {
				sum += val.get();
			}
			//Writes the output
			context.write(key, new IntWritable(sum));
		}
	}

	//Driver
	public static void main(String[] args) throws Exception {
		final Configuration conf = new Configuration();
		//Defines a job
		final Job job = new Job(conf, "myWordcount");
		//Specifies in which class the mapper and reducer will be defined
		job.setJarByClass(WordCount.class);
		//Declares what is the type of the output. Here we are saying it is a {Text, Int}
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(IntWritable.class);
		//We specify which are the map and reduce classes
		job.setMapperClass(Map.class);
		job.setReducerClass(Reduce.class);

		//We specify what is type of the files. In this case they will be plain text files (not compressed, or something like that)
		job.setInputFormatClass(TextInputFormat.class);
		//We specify what is the definition of the output
		job.setOutputFormatClass(TextOutputFormat.class);

		//We specify where the files will be read and written
		FileInputFormat.addInputPath(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));

		//Will wait until the job si completed
		job.waitForCompletion(true);
	}

}
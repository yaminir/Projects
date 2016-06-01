import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.StringTokenizer;
import java.util.TreeMap;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Reducer.Context;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class TopFiveWorkdsWiki {
// Mapper Class is used to provide key value pairs based on the keyword pattern provided as an argument.
// Data set and keyword are passed as arguments to the Map function

	public static class TokenizerMapper extends
			Mapper<Object, Text, Text, IntWritable> {
// Hadoop Supported Data types

		private final static IntWritable one = new IntWritable(1);
		private Text word = new Text();

		public void map(Object key, Text value, Context context)
				throws IOException, InterruptedException {
// Gets the keyword pattern from the argument			
			Configuration conf = context.getConfiguration();
			String pattern = conf.get("pattern");
// Map function splits the Records by tab space
			String[] wiki = value.toString().split("\t");
			if (wiki.length > 2) {
// If the second column of the dataset i.e Title contains the keyword then that record is taken 

			if (wiki[1].contains(pattern)) {
					StringTokenizer itr = new StringTokenizer(wiki[3].trim());
// iterating through all the words available in the selected line and obtaining the key value pair		
					while (itr.hasMoreTokens()) {
						word.set(itr.nextToken());
//sending the key value pairs to output collector which passes the same to reducer
						context.write(word, one);
					}
				}
			}

		}
	}
// Combiner Function will gather output in memory lists.Each list is assigned for a key value.
	public static class IntSumCombiner extends
			Reducer<Text, IntWritable, Text, IntWritable> {
		private IntWritable result = new IntWritable();

		public void reduce(Text key, Iterable<IntWritable> values,
				Context context) throws IOException, InterruptedException {
			int sum = 0;
			for (IntWritable val : values) {
				sum += val.get();
			}
			result.set(sum);
			context.write(key, result);
		}
	}
// Reducer function adds the values.

	public static class IntSumReducer extends
			Reducer<Text, IntWritable, Text, IntWritable> {
		private TreeMap<Integer, String> topRecordMap = new TreeMap<Integer, String>();

		private IntWritable result = new IntWritable();

// Values i.e count are added and combined key value pairs are obtained.
		public void reduce(Text key, Iterable<IntWritable> values,
				Context context) throws IOException, InterruptedException {
			int sum = 0;
			for (IntWritable val : values) {
				sum += val.get();
			}
			result.set(sum);
// TopRecordMap returns the records obtained from Map function
			if (topRecordMap.size() == 0) {

				topRecordMap.put(sum, key.toString().trim());

			} else if ((topRecordMap.size() > 0) && (topRecordMap.size() < 5)) {

				if (topRecordMap.containsKey(sum)) {

					topRecordMap.put(sum, topRecordMap.get(sum) + ","
							+ key.toString().trim());

				} else {

					topRecordMap.put(sum, key.toString().trim());

				}
			} else if (topRecordMap.size() == 5) {

				if (topRecordMap.containsKey(sum)) {

					topRecordMap.put(sum, topRecordMap.get(sum) + ","
							+ key.toString().trim());

				} else if (topRecordMap.firstKey() < sum) {

					topRecordMap.remove(topRecordMap.firstKey());
					topRecordMap.put(sum, key.toString().trim());
				}

			}

		}

		@Override
		protected void cleanup(Context context) throws IOException,
				InterruptedException {

			for (Integer key : topRecordMap.keySet()) {

				String words[] = topRecordMap.get(key).split(",");
				for (String word : words)
					context.write(new Text(word), new IntWritable(key));

			}
			super.cleanup(context);
		}

	}

	public static void main(String[] args) throws Exception {
		Configuration conf = new Configuration();
		conf.set("pattern", args[1].trim());
// Creating a Job object and assigning job name 
		Job job = new Job(conf, TopFiveWorkdsWiki.class.getSimpleName());
		job.setJarByClass(TopFiveWorkdsWiki.class);
//Providing mapper and reducer class names
		job.setMapperClass(TokenizerMapper.class);
		job.setCombinerClass(IntSumCombiner.class);
		job.setReducerClass(IntSumReducer.class);
		job.setNumReduceTasks(1);
// Setting Configuration object with data type of key and calue 
//Keys are Words
		job.setOutputKeyClass(Text.class);
// Values are count	
		job.setOutputValueClass(IntWritable.class);
//Providing input and output directories
		FileInputFormat.addInputPath(job, new Path(args[0]));
		System.out
				.println("Find top five words In all article whose Title  contains -- '"
						+ args[1].trim() + "' -- in the path " + args[0].trim());
// Creating the output directory
				String outputDir = System.getenv("MY_HADOOP_HOME") + "/"
				+ TopFiveWorkdsWiki.class.getSimpleName() + "_"
				+ System.currentTimeMillis();
		FileOutputFormat.setOutputPath(job, new Path(outputDir));
		if ((job.waitForCompletion(true) ? 0 : 1) == 0) {
			System.out.println("Please Check the Path " + outputDir
					+ " for OutPut");

			System.out.println("A Sample Output is given below ");
			final FileSystem fs = FileSystem.get(new JobConf(conf,
					TopFiveWorkdsWiki.class));
			FileStatus fileStatuses[] = fs.listStatus(new Path(outputDir));
			for (FileStatus fileStatus : fileStatuses) {
				// If the file name starts with "part", read it.
				if (fileStatus.getPath().getName().startsWith("part")) {
					Path outFile = fileStatus.getPath();
// Output is displayed after the execution
					try{
		                BufferedReader br=new BufferedReader(new InputStreamReader(fs.open(outFile)));
		                String line;
		                line=br.readLine();
		                while (line != null){
		                        System.out.println(line);
		                        line=br.readLine();
		                }
		        }catch(Exception e){
		        }
				}
			}
		}
	}
}


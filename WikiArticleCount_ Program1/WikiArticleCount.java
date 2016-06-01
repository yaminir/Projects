import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class WikiArticleCount {
// Mapper Class is used to provide key value pairs based on the keyword pattern provided as an argument.
// Data set and keyword are passed as arguments to the MAp function

	public static class TokenizerMapper extends
			Mapper<Object, Text, Text, IntWritable> {
// Hadoop Supported Data types
		private final static IntWritable one = new IntWritable(1);

		public void map(Object key, Text value, Context context)
				throws IOException, InterruptedException {

			Configuration conf = context.getConfiguration();
// Gets the keyword pattern from the argument			
			String pattern = conf.get("pattern");
// Map function splits the Records by tab space
			String[] wiki = value.toString().split("\t");
			if (wiki.length > 3) {
// If the column of the dataset that is title or column 4 of the dataset i.e content contains the keyword then a key pair is taken.
				if (wiki[1].contains(pattern) || wiki[3].contains(pattern)) {
// This command passes the key value pairs to the Reducer function
				context.write(new Text(pattern), one);
				}
			}

		}
	}
// Reducer function adds the values.

	public static class IntSumReducer extends
			Reducer<Text, IntWritable, Text, IntWritable> {
		private IntWritable result = new IntWritable();

		public void reduce(Text key, Iterable<IntWritable> values,
				Context context) throws IOException, InterruptedException {
			int sum = 0;
// Values i.e count are added and combined key value pairs are obtained.
			for (IntWritable val : values) {
				sum += val.get();
			}
			result.set(sum);
			context.write(key, result);
		}
	}

	public static void main(String[] args) throws Exception {
		Configuration conf = new Configuration();
		conf.set("pattern", args[1].trim());
// Creating a Job object and assigning job name 
		Job job = new Job(conf, WikiArticleCount.class.getSimpleName());
		job.setJarByClass(WikiArticleCount.class);
//Providing mapper and reducer class names
		job.setMapperClass(TokenizerMapper.class);
		job.setCombinerClass(IntSumReducer.class);
		job.setReducerClass(IntSumReducer.class);
// Setting Configuration object with data type of key and calue 
//Keys are Words
		job.setOutputKeyClass(Text.class);
// Values are count	
		job.setOutputValueClass(IntWritable.class);
//Providing input and output directories
		FileInputFormat.addInputPath(job, new Path(args[0]));
		System.out.println("Find " + args[1].trim() + " in the path "
				+ args[0].trim());
		String outputDir = System.getenv("MY_HADOOP_HOME") + "/"
				+ WikiArticleCount.class.getSimpleName() + "_"
				+ System.currentTimeMillis();
		FileOutputFormat.setOutputPath(job, new Path(outputDir));
		if ((job.waitForCompletion(true) ? 0 : 1) == 0) {
			System.out.println("Please Check the Path " + outputDir
					+ " for OutPut");

			System.out.println("A Sample Output is given below ");

			final FileSystem fs = FileSystem.get(new JobConf(conf,
					WikiArticleCount.class));
			FileStatus fileStatuses[] = fs.listStatus(new Path(outputDir));
			for (FileStatus fileStatus : fileStatuses) {
				// If the file name starts with "part", read it.
				if (fileStatus.getPath().getName().startsWith("part")) {
					Path outFile = fileStatus.getPath();
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


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class WikiArticleSearch {

// Mapper Class is used to provide key value pairs based on the keyword pattern provided as an argument.
// Data set keyword and option through which way you want to search the input file are passed as arguments to the Map function.
	public static class TokenizerMapper extends
			Mapper<Object, Text, Text, Text> {
// Hadoop Supported Data types
		private final static IntWritable one = new IntWritable(1);
		private Text searchKey = new Text();
		private Text articleName = new Text();

		public void map(Object key, Text value, Context context)
				throws IOException, InterruptedException {

			Configuration conf = context.getConfiguration();
// Gets the option through which way the input file should be searched			
			String option = conf.get("option");
			String searchData = conf.get("searchData");

			String[] wiki = value.toString().split("\t");
// If option="0" the 2nd argument should be a string and passed then the map function searched for the records containing the string.
			if ("0".equals(option)) {
				if (wiki[1].toLowerCase().contains(
						searchData.trim().toLowerCase())) {

					searchKey.set(searchData);
					articleName.set(wiki[1]);
					context.write(searchKey, articleName);
				}

			} else {
				if (wiki[2].length() == 19) {
					String[] DTTM = wiki[2].split(" ");
					String[] dttm = DTTM[0].split("-");
					String outputKey = null;

					if ("1".equals(option)) {
						outputKey = dttm[0];
					} else if ("2".equals(option)) {
						outputKey = dttm[0] + "-" + dttm[1];

					} else if ("3".equals(option)) {
						outputKey = DTTM[0];
					}

					if (outputKey.equalsIgnoreCase(searchData)) {
						searchKey.set(outputKey);
						articleName.set(wiki[1]);
						context.write(searchKey, articleName);
					}
				}
			}

		}
	}

	public static void main(String[] args) throws Exception {
		Configuration conf = new Configuration();
		conf.set("option", args[1].trim());
		conf.set("searchData", args[2].trim());

		Job job = new Job(conf, WikiArticleSearch.class.getSimpleName());
		job.setJarByClass(WikiArticleSearch.class);

// Creating a Job object and assigning job name
		job.setMapperClass(TokenizerMapper.class);
		job.setNumReduceTasks(0);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
		FileInputFormat.addInputPath(job, new Path(args[0]));
		System.out.println("Find " + args[1].trim() + " in the path "
				+ args[0].trim());
		String outputDir = System.getenv("MY_HADOOP_HOME") + "/"
				+ WikiArticleSearch.class.getSimpleName() + "_"
				+ System.currentTimeMillis();

		FileOutputFormat.setOutputPath(job, new Path(outputDir));
		if ((job.waitForCompletion(true) ? 0 : 1) == 0) {
			System.out.println("Please Check the Path " + outputDir
					+ " for OutPut");

			System.out.println("A Sample Output is given below ");
			final FileSystem fs = FileSystem.get(new JobConf(conf,
					WikiArticleSearch.class));
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

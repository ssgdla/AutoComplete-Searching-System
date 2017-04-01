import java.io.IOException;
import org.apache.hadoop.filecache.DistributedCache;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.lib.db.DBConfiguration;
import org.apache.hadoop.mapreduce.lib.db.DBOutputFormat;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.*;
/**
 * Created by zheye on 17-3-31.
 */
@SuppressWarnings("deprecation")
public class Driver {

    public static void main(String[] args) throws ClassNotFoundException, IOException, InterruptedException {
        String inputDir = args[0];
        String outputDir = args[1];
    //    String noGram = args[2];
    //    String threshold = args[3];
    //    String topK = args[4];

        String noGram = "4";
        String threshold = "3";
        String topK = "20";

        //job1
        Configuration conf1 = new Configuration();
        conf1.set("textinputformat.record.delimiter", ".");
        conf1.set("noGram", noGram);

        Job job1 = Job.getInstance();
        job1.setJobName("NGram");
        job1.setJarByClass(Driver.class);

        job1.setMapperClass(NGramLibraryBuilder.NGramMapper.class);
        job1.setReducerClass(NGramLibraryBuilder.NGramReducer.class);

        job1.setOutputKeyClass(Text.class);
        job1.setOutputValueClass(IntWritable.class);

        job1.setInputFormatClass(TextInputFormat.class);
        job1.setOutputFormatClass(TextOutputFormat.class);

        TextInputFormat.setInputPaths(job1, new Path(inputDir));
        TextOutputFormat.setOutputPath(job1, new Path(outputDir));
        job1.waitForCompletion(true);

        Configuration conf2 = new Configuration();
        conf2.set("threshold", threshold);
        conf2.set("topK", topK);
        DistributedCache.addFileToClassPath(new Path("/home/zheye/mysql/mysql-connector-java-5.1.41-bin.jar"), conf2);

        DBConfiguration.configureDB(conf2,
                "com.mysql.jdbc.Driver",
                "jdbc:mysql://localhost:3306/testBase",
                "root",
                "SSss1212");


        Job job2 = Job.getInstance();
        job2.setJobName("LanguageModel");
        job2.setJarByClass(Driver.class);
        job2.setMapperClass(LanguageModel.Map.class);
        job2.setReducerClass(LanguageModel.Reduce.class);

        job2.setMapOutputKeyClass(Text.class);
        job2.setMapOutputValueClass(Text.class);

        job2.setOutputKeyClass(DBOutputWritable.class);
        job2.setOutputValueClass(NullWritable.class);

        job2.setInputFormatClass(TextInputFormat.class);
        job2.setOutputFormatClass(DBOutputFormat.class);


        DBOutputFormat.setOutput(job2, "output", new String[]{"starting_phrase", "following_word", "count"});
        TextInputFormat.setInputPaths(job2, outputDir);
        job2.waitForCompletion(true);


    }
}

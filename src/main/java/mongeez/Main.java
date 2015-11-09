package mongeez;

import java.util.Scanner;

import org.apache.commons.cli.AlreadySelectedException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.StringUtils;
import org.mongeez.MongeezRunner;
import org.springframework.core.io.UrlResource;
import org.springframework.util.ResourceUtils;

import com.mongodb.Mongo;
import com.mongodb.MongoClient;

public class Main {

	public static void main(String[] args) throws AlreadySelectedException {

		// create the command line parser
		CommandLineParser parser = new DefaultParser();

		Options global = new Options();

		// create the Options
		OptionGroup options = new OptionGroup();
		Option help = new Option("help", "Print this message");
		options.addOption(help);
		options.addOption(new Option("migrate", "Migrate"));
		options.addOption(new Option("clean", "Clean"));
		options.addOption(new Option("v", "version", false, "Print the version"));
		options.setRequired(true);
		options.setSelected(help);

		global.addOptionGroup(options);

		global.addOption(new Option("h", "host", true, "The host. Default is 'localhost'"));
		global.addOption(Option.builder("p").longOpt("port").hasArg(true).desc("The port. Default is '27017'").type(Integer.class).build());
		global.addOption("b", "database", true, "The database. Default is 'test'");

		global.addOption("c", "changesets", true, "[For migrate only] The file containing the changesets. Default is 'classpath:facts/root.xml'");

		global.addOption("f", "force", false, "Force.");

		try {
			// parse the command line arguments
			CommandLine line = parser.parse(global, args);

			if (line.hasOption("help")) {
				printHelp(global);
				return;
			}

			String host = line.getOptionValue("host", "localhost");
			int port = Integer.valueOf(line.getOptionValue("port", "27017"));
			String database = line.getOptionValue("database", "test");

			Mongo mongo = new MongoClient(host, port);

			if (line.hasOption("migrate")) {
				boolean doIt = true;
				if (!line.hasOption("f")) {
					System.err.println("You are going to migrate your database " + host + ":" + port + "/" + database + ", proceed? [y/N]");
					Scanner scan = new Scanner(System.in);
					String input = scan.nextLine();
					scan.close();
					doIt = StringUtils.isNotEmpty(input) && input.equals("y");
				}

				if (doIt) {
					MongeezRunner runner = new MongeezRunner();
					runner.setMongo(mongo);
					runner.setDbName(database);
					runner.setFile(new UrlResource(ResourceUtils.getURL("classpath:facts/root.xml")));
					runner.execute();
				}
			} else if (line.hasOption("clean")) {
				boolean doIt = true;
				if (!line.hasOption("f")) {
					System.err.println("You are going to delete all your data on " + host + ":" + port + "/" + database + ", proceed? [y/N]");
					Scanner scan = new Scanner(System.in);
					String input = scan.nextLine();
					scan.close();
					doIt = StringUtils.isNotEmpty(input) && input.equals("y");
				}

				if (doIt) {
					mongo.dropDatabase(database);
				}
			}

		} catch (ParseException exp) {
			printHelp(global);
		} catch (Exception e) {
			System.err.println("Well, fuck..." + e.getMessage());
		}
	}

	public static void printHelp(Options options) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("mongeez", options);
	}
}

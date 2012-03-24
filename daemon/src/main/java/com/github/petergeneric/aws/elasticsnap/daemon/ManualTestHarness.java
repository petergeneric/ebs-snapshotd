package com.github.petergeneric.aws.elasticsnap.daemon;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeSnapshotsRequest;
import com.amazonaws.util.AwsHostNameUtils;
import com.github.petergeneric.aws.elasticsnap.daemon.service.impl.AWSDiscardSnapshotServiceImpl;
import com.github.petergeneric.aws.elasticsnap.daemon.service.impl.AWSDurableVolumeDiscoveryServiceImpl;
import com.github.petergeneric.aws.elasticsnap.daemon.service.impl.LoggingNotificationService;
import com.github.petergeneric.aws.elasticsnap.daemon.service.impl.NaiveAWSCreateSnapshotService;
import com.github.petergeneric.aws.elasticsnap.daemon.service.impl.SingleProfileBackupProfileServiceImpl;
import com.github.petergeneric.aws.elasticsnap.daemon.service.impl.VolumeActionServiceImpl;
import com.github.petergeneric.aws.elasticsnap.daemon.type.BackupProfile;
import com.github.petergeneric.aws.elasticsnap.daemon.type.DurableVolume;

/**
 * Manual test harness
 */
public class ManualTestHarness
{
	public static void main(String[] args) throws Exception
	{
		final File awsconf = new File("conf/aws.properties");

		if (!awsconf.exists())
		{
			throw new IllegalArgumentException("Expected conf/aws.properties with at least accessKey, secretAccessKey.");
		}

		// configure log4j
		PropertyConfigurator.configure("conf/log4j.properties");

		Properties properties = new Properties();
		properties.load(new FileInputStream(awsconf));

		final String region = properties.getProperty("region", "us-east-1");

		final BasicAWSCredentials auth;
		{
			final String accessKey = properties.getProperty("accessKey");
			final String secretAccessKey = properties.getProperty("secretAccessKey");

			auth = new BasicAWSCredentials(accessKey, secretAccessKey);
		}

		AmazonEC2Client ec2 = new AmazonEC2Client(auth);
		ec2.setEndpoint("https://ec2." + region + ".amazonaws.com");

		final String profileName = properties.getProperty("profile.name", "default");
		final long interval = Long.valueOf(properties.getProperty("profile.interval", "60000")); // create snapshots every minute
		final long expire = Long.valueOf(properties.getProperty("profile.expire", "120000")); // 2 minute expire
		final int min = Integer.valueOf(properties.getProperty("profile.expire", "3")); // 3 backups

		BackupProfile profile = new BackupProfile(profileName, interval, expire, min);

		main(ec2, profile);
	}

	private static void main(AmazonEC2Client ec2, BackupProfile profile)
	{
		AWSDurableVolumeDiscoveryServiceImpl disc = new AWSDurableVolumeDiscoveryServiceImpl(ec2);

		List<DurableVolume> vols = disc.discover();

		LoggingNotificationService notifier = new LoggingNotificationService(); // log to log4j
		AWSDiscardSnapshotServiceImpl discarder = new AWSDiscardSnapshotServiceImpl(ec2); // really discard (call setEnabled(false) to experiment
		NaiveAWSCreateSnapshotService creater = new NaiveAWSCreateSnapshotService(ec2); // really snapshot
		SingleProfileBackupProfileServiceImpl profiler = new SingleProfileBackupProfileServiceImpl();
		profiler.setProfile(profile);

		VolumeActionServiceImpl actioner = new VolumeActionServiceImpl();
		actioner.setCreateSnapshotService(creater);
		actioner.setDiscardSnapshotService(discarder);
		actioner.setNotificationService(notifier);

		for (DurableVolume vol : vols)
		{
			profiler.assignProfile(vol);
			actioner.handle(vol);
		}
	}
}

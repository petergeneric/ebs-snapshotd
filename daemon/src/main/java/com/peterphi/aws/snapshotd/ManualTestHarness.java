package com.peterphi.aws.snapshotd;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeSnapshotsRequest;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.util.AwsHostNameUtils;
import com.peterphi.aws.snapshotd.service.iface.BackupService;
import com.peterphi.aws.snapshotd.service.iface.NotificationService;
import com.peterphi.aws.snapshotd.service.impl.AWSDiscardSnapshotServiceImpl;
import com.peterphi.aws.snapshotd.service.impl.AWSDurableVolumeDiscoveryServiceImpl;
import com.peterphi.aws.snapshotd.service.impl.BackupServiceImpl;
import com.peterphi.aws.snapshotd.service.impl.LoggingNotificationService;
import com.peterphi.aws.snapshotd.service.impl.NaiveAWSCreateSnapshotService;
import com.peterphi.aws.snapshotd.service.impl.SNSNotificationService;
import com.peterphi.aws.snapshotd.service.impl.SingleProfileBackupProfileServiceImpl;
import com.peterphi.aws.snapshotd.service.impl.TagBasedBackupProfileServiceImpl;
import com.peterphi.aws.snapshotd.service.impl.VolumeActionServiceImpl;
import com.peterphi.aws.snapshotd.service.impl.SNSNotificationService.NotifyLevel;
import com.peterphi.aws.snapshotd.type.BackupProfile;
import com.peterphi.aws.snapshotd.type.DurableVolume;

/**
 * Manual test harness
 */
public class ManualTestHarness
{
	public static void main(String[] args) throws Exception
	{
		final File awsconf = new File("conf/aws.properties");
		final File backupconf = new File("conf/backup.properties");

		if (!awsconf.exists())
		{
			throw new IllegalArgumentException("Expected conf/aws.properties with at least accessKey, secretAccessKey.");
		}

		if (!backupconf.exists())
		{
			throw new IllegalArgumentException("Expected " + backupconf);
		}

		// configure log4j
		PropertyConfigurator.configure("conf/log4j.properties");

		Properties properties = new Properties();
		properties.load(new FileInputStream(awsconf));
		properties.load(new FileInputStream(backupconf));

		final String region = properties.getProperty("region", "us-east-1");

		final BasicAWSCredentials auth;
		{
			final String accessKey = properties.getProperty("accessKey");
			final String secretAccessKey = properties.getProperty("secretAccessKey");

			auth = new BasicAWSCredentials(accessKey, secretAccessKey);
		}

		AmazonEC2Client ec2 = new AmazonEC2Client(auth);
		AmazonSNSClient sns = new AmazonSNSClient(auth);

		// N.B. HTTP endpoints important if using one-jar (TODO figure out why - I assume AWS ships CA certs in their jars and one-jar screws them up?)
		ec2.setEndpoint("http://ec2." + region + ".amazonaws.com");
		sns.setEndpoint("http://sns." + region + ".amazonaws.com");

		final String profileName = properties.getProperty("profile.name", "default");
		final long interval = Long.valueOf(properties.getProperty("profile.interval", "60000")); // create snapshots every minute
		final long expire = Long.valueOf(properties.getProperty("profile.expire", "120000")); // 2 minute expire
		final int min = Integer.valueOf(properties.getProperty("profile.min", "3")); // 3 backups

		BackupProfile profile = new BackupProfile(profileName, interval, expire, min);

		main(ec2, sns, profile, properties);
	}

	private static void main(AmazonEC2Client ec2, AmazonSNSClient sns, BackupProfile profile, Properties properties)
	{
		// If there is an sns.topic property, switch on SNS notifications
		// Otherwise, log to log4j
		final NotificationService notifier;
		if (properties.getProperty("sns.topic") != null)
		{
			SNSNotificationService snsNotifier = new SNSNotificationService(sns);
			snsNotifier.setTopic(properties.getProperty("sns.topic"));

			snsNotifier.setMinimumLevel(NotifyLevel.ENACTED); // by default, notify about snapshot create/discard and any detected errors

			notifier = snsNotifier; // log to Amazon SNS
		}
		else
		{
			notifier = new LoggingNotificationService(); // log everything with log4j
		}

		AWSDiscardSnapshotServiceImpl discarder = new AWSDiscardSnapshotServiceImpl(ec2); // really discard (call setEnabled(false) to experiment
		NaiveAWSCreateSnapshotService creater = new NaiveAWSCreateSnapshotService(ec2); // really snapshot
		TagBasedBackupProfileServiceImpl profiler = new TagBasedBackupProfileServiceImpl();
		profiler.addProfile(profile);

		VolumeActionServiceImpl actioner = new VolumeActionServiceImpl();
		actioner.setCreateSnapshotService(creater);
		actioner.setDiscardSnapshotService(discarder);
		actioner.setNotificationService(notifier);

		AWSDurableVolumeDiscoveryServiceImpl discoverer = new AWSDurableVolumeDiscoveryServiceImpl(ec2);

		BackupService backupService = new BackupServiceImpl();
		backupService.setNotificationService(notifier);
		backupService.setDurableVolumeDiscoveryService(discoverer);
		backupService.setProfileService(profiler);
		backupService.setVolumeActionService(actioner);

		// Run an iteration of the backup service
		backupService.backup();
	}
}

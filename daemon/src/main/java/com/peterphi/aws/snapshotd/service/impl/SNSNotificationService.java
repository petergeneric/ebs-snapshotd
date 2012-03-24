package com.peterphi.aws.snapshotd.service.impl;

import org.apache.log4j.Logger;

import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.Snapshot;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.peterphi.aws.snapshotd.service.iface.NotificationService;
import com.peterphi.aws.snapshotd.service.impl.SNSNotificationService.NotifyLevel;
import com.peterphi.aws.snapshotd.type.DurableVolume;

/**
 * A simple SNS-based notifier
 */
public class SNSNotificationService implements NotificationService
{
	private static final transient Logger log = Logger.getLogger(SNSNotificationService.class);

	public static enum NotifyLevel
	{
		INFO, WARN, ENACTED, ERROR;
	}

	protected AmazonSNSClient sns;
	protected String topic;
	protected NotifyLevel minimumLevel = NotifyLevel.WARN;

	public SNSNotificationService()
	{
	}

	public SNSNotificationService(AmazonSNSClient sns)
	{
		setSNS(sns);
	}

	public void setSNS(AmazonSNSClient sns)
	{
		this.sns = sns;
	}

	public void setTopic(String topic)
	{
		this.topic = topic;
	}

	public void setMinimumLevel(NotifyLevel level)
	{
		this.minimumLevel = level;
	}

	public void send(NotifyLevel level, String subject, String message)
	{
		// ignore messages below our delivery threshold
		if (level.ordinal() < minimumLevel.ordinal())
			return;

		PublishRequest pub = new PublishRequest();
		pub.setTopicArn(topic);
		pub.setMessage(message);

		if (subject != null)
			pub.setSubject(subject);

		PublishResult result = sns.publish(pub);

		log.info("Published " + level + " msg to SNS: " + message + " (msg id " + result.getMessageId() + ")");
	}

	@Override
	public void discarded(DurableVolume volume, Snapshot snapshot)
	{
		final String message = "Discarded snapshot " + snapshot + "\nFor: " + volume;
		send(NotifyLevel.ENACTED, "Deleted " + snapshot.getSnapshotId() + " (based on " + snapshot.getVolumeId() + ")", message);
	}

	@Override
	public void created(DurableVolume volume, Snapshot snapshot)
	{
		final String message = "Created snapshot " + snapshot + "\nFor: " + volume;
		send(NotifyLevel.ENACTED, "Backed up " + snapshot.getVolumeId() + " to " + snapshot.getSnapshotId(), message);
	}

	@Override
	public void warn(DurableVolume volume, String message)
	{
		warn(message + "\nFor: " + volume);
	}

	@Override
	public void error(DurableVolume volume, String message)
	{
		error(message + "\nFor: " + volume);
	}

	@Override
	public void info(DurableVolume volume, String message)
	{
		info(message + "\nFor: " + volume);
	}

	@Override
	public void warn(String message)
	{
		send(NotifyLevel.WARN, null, message);
	}

	@Override
	public void error(String message)
	{
		send(NotifyLevel.ERROR, null, message);
	}

	@Override
	public void info(String message)
	{
		send(NotifyLevel.INFO, null, message);
	}
}

package com.peterphi.aws.snapshotd.service.impl;

import org.apache.log4j.Logger;

import com.amazonaws.services.ec2.model.Snapshot;
import com.peterphi.aws.snapshotd.service.iface.NotificationService;
import com.peterphi.aws.snapshotd.type.DurableVolume;

public class LoggingNotificationService implements NotificationService
{
	private static final transient Logger log = Logger.getLogger(LoggingNotificationService.class);

	@Override
	public void warn(String message)
	{
		log.warn(message);
	}

	@Override
	public void error(String message)
	{
		log.error(message);
	}

	@Override
	public void info(String message)
	{
		log.info(message);
	}

	@Override
	public void warn(DurableVolume volume, String message)
	{
		warn(volume + ": " + message);
	}

	@Override
	public void error(DurableVolume volume, String message)
	{
		error(volume + ": " + message);
	}

	@Override
	public void info(DurableVolume volume, String message)
	{
		info(volume + ": " + message);
	}

	@Override
	public void discarded(DurableVolume volume, Snapshot snapshot)
	{
		info(volume, "Discarded snapshot " + snapshot.getSnapshotId());
	}

	@Override
	public void created(DurableVolume volume, Snapshot snapshot)
	{
		info(volume, "Creating new snapshot " + snapshot.getSnapshotId());
	}
}

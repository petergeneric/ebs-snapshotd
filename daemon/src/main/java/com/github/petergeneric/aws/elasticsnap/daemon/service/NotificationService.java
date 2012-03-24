package com.github.petergeneric.aws.elasticsnap.daemon.service;

import com.amazonaws.services.ec2.model.Snapshot;
import com.github.petergeneric.aws.elasticsnap.daemon.type.DurableVolume;

public interface NotificationService
{
	// structured notificationd
	
	public void discarded(DurableVolume volume, Snapshot snapshot);

	public void created(DurableVolume volume, Snapshot snapshot);

	// volume-specific messages

	public void warn(DurableVolume volume, String message);

	public void error(DurableVolume volume, String message);

	public void info(DurableVolume volume, String message);

	// non-volume-specific messages

	public void warn(String message);

	public void error(String message);

	public void info(String message);

}

package com.peterphi.aws.snapshotd.service.iface;

import com.peterphi.aws.snapshotd.type.DurableVolume;

/**
 * A service which takes any necessary action for a DurableVolume
 */
public interface VolumeActionService
{
	public void handle(DurableVolume volume);

	public void setNotificationService(NotificationService notify);

	public void setCreateSnapshotService(CreateSnapshotService snapshotter);

	public void setDiscardSnapshotService(DiscardSnapshotService discarder);
}

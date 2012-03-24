package com.peterphi.aws.snapshotd.service;

import com.peterphi.aws.snapshotd.type.DurableVolume;

/**
 * A service which takes any necessary action for a DurableVolume
 */
public interface VolumeActionService
{
	public void handle(DurableVolume volume);
}

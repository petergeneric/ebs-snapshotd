package com.github.petergeneric.aws.elasticsnap.daemon.service;

import com.github.petergeneric.aws.elasticsnap.daemon.type.DurableVolume;

/**
 * A service which takes any necessary action for a DurableVolume
 */
public interface VolumeActionService
{
	public void handle(DurableVolume volume);
}

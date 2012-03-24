package com.github.petergeneric.aws.elasticsnap.daemon.service;

import java.util.List;

import com.github.petergeneric.aws.elasticsnap.daemon.type.DurableVolume;

/**
 * Discovers DurableVolumes
 */
public interface DurableVolumeDiscoveryService
{
	public List<DurableVolume> discover();
}

package com.peterphi.aws.snapshotd.service.iface;

import java.util.List;

import com.peterphi.aws.snapshotd.type.DurableVolume;

/**
 * Discovers DurableVolumes
 */
public interface DurableVolumeDiscoveryService
{
	public List<DurableVolume> discover();
}

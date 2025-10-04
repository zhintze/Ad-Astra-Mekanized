package com.hecookin.adastramekanized.common.entities.multipart;

import net.minecraft.world.entity.Entity;

/**
 * Interface for multipart entity parts.
 * Adapted from Ad Astra.
 */
public interface MultipartPartEntity<T extends Entity & MultipartEntity> {

    T getParent();
}

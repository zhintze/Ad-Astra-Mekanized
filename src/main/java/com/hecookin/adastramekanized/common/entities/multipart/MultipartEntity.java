package com.hecookin.adastramekanized.common.entities.multipart;

import java.util.List;

/**
 * Interface for entities that consist of multiple parts.
 * Adapted from Ad Astra for multipart collision handling.
 */
public interface MultipartEntity {

    List<MultipartPartEntity<?>> getMultipartParts();
}

package com.rgms.shared.util;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;

import java.io.Serializable;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.UUID;

/**
 * UUID v7 Generator — timestamp-ordered UUIDs for B-Tree index efficiency.
 * The first 48 bits embed the current Unix timestamp in milliseconds,
 * ensuring sequential inserts and avoiding index fragmentation in PostgreSQL.
 */
public class Uuid7Generator implements IdentifierGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    public Serializable generate(SharedSessionContractImplementor session, Object object) {
        long timestampMs = Instant.now().toEpochMilli();

        // High 64 bits: 48-bit timestamp | 4-bit version (7) | 12-bit random
        long high = (timestampMs << 16) | 0x7000L | (RANDOM.nextLong() & 0x0FFFL);
        // Low 64 bits: 2-bit variant (10) | 62-bit random
        long low = 0x8000000000000000L | (RANDOM.nextLong() & 0x3FFFFFFFFFFFFFFFL);

        return new UUID(high, low);
    }
}

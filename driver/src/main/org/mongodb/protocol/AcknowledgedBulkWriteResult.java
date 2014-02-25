/*
 * Copyright (c) 2008-2014 MongoDB, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mongodb.protocol;

import org.mongodb.BulkWriteResult;
import org.mongodb.BulkWriteUpsert;
import org.mongodb.operation.WriteRequest;

import java.util.Collections;
import java.util.List;

import static org.mongodb.assertions.Assertions.notNull;

/**
 * This class is not part of the public API.
 */
public class AcknowledgedBulkWriteResult extends BulkWriteResult {
    private int insertedCount;
    private int updatedCount;
    private int removedCount;
    private int modifiedCount;
    private final List<BulkWriteUpsert> upserts;

    AcknowledgedBulkWriteResult(final int insertedCount, final int updatedCount, final int removedCount,
                                final int modifiedCount, final List<BulkWriteUpsert> upserts) {
        this.insertedCount = insertedCount;
        this.updatedCount = updatedCount;
        this.removedCount = removedCount;
        this.modifiedCount = modifiedCount;
        this.upserts = Collections.unmodifiableList(notNull("upserts", upserts));
    }

    public AcknowledgedBulkWriteResult(final WriteRequest.Type type, final int count, final List<BulkWriteUpsert> upserts) {
        this(type, count, 0, upserts);
    }

    public AcknowledgedBulkWriteResult(final WriteRequest.Type type, final int count, final int modifiedCount,
                                       final List<BulkWriteUpsert> upserts) {
        this(type == WriteRequest.Type.INSERT ? count : 0,
             (type == WriteRequest.Type.UPDATE || type == WriteRequest.Type.REPLACE)  ? count : 0,
             type == WriteRequest.Type.REMOVE ? count : 0,
             modifiedCount, upserts);
    }

    @Override
    public boolean isAcknowledged() {
        return true;
    }

    @Override
    public int getInsertedCount() {
        return insertedCount;
    }

    @Override
    public int getUpdatedCount() {
        return updatedCount;
    }

    @Override
    public int getRemovedCount() {
        return removedCount;
    }

    @Override
    public int getModifiedCount() {
        return modifiedCount;
    }

    @Override
    public List<BulkWriteUpsert> getUpserts() {
        return upserts;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AcknowledgedBulkWriteResult that = (AcknowledgedBulkWriteResult) o;

        if (insertedCount != that.insertedCount) {
            return false;
        }
        if (modifiedCount != that.modifiedCount) {
            return false;
        }
        if (removedCount != that.removedCount) {
            return false;
        }
        if (updatedCount != that.updatedCount) {
            return false;
        }
        if (!upserts.equals(that.upserts)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = upserts.hashCode();
        result = 31 * result + insertedCount;
        result = 31 * result + updatedCount;
        result = 31 * result + removedCount;
        result = 31 * result + modifiedCount;
        return result;
    }

    @Override
    public String toString() {
        return "AcknowledgedBulkWriteResult{"
               + "insertedCount=" + insertedCount
               + ", updatedCount=" + updatedCount
               + ", removedCount=" + removedCount
               + ", modifiedCount=" + modifiedCount
               + ", upserts=" + upserts
               + '}';
    }
}

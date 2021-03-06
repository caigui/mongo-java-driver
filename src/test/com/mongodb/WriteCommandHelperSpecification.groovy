/*
 * Copyright (c) 2008 - 2013 MongoDB Inc. <http://10gen.com>
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

package com.mongodb

import spock.lang.Specification

import static com.mongodb.WriteCommandResultHelper.getBulkWriteException
import static com.mongodb.WriteCommandResultHelper.getBulkWriteResult
import static com.mongodb.WriteCommandResultHelper.hasError
import static com.mongodb.WriteRequest.Type.INSERT
import static com.mongodb.WriteRequest.Type.REMOVE
import static com.mongodb.WriteRequest.Type.REPLACE
import static com.mongodb.WriteRequest.Type.UPDATE

class WriteCommandHelperSpecification extends Specification {

    def 'should get bulk write result from with a count matching the n field'() {
        expect:
        getBulkWriteResult(INSERT, getCommandResult(new BasicDBObject('n', 1))) == new AcknowledgedBulkWriteResult(INSERT, 1, [])
    }


    def 'should get bulk write result with upserts matching the upserted field'() {
        expect:
        [new BulkWriteUpsert(0, 'id1'), new BulkWriteUpsert(2, 'id2')] ==
        getBulkWriteResult(UPDATE, getCommandResult(new BasicDBObject('n', 1)
                                                            .append('upserted', [new BasicDBObject('index', 0).append('_id', 'id1'),
                                                                                 new BasicDBObject('index', 2).append('_id', 'id2')])))
                .getUpserts()
    }

    def 'should not have modified count for update with no nModified field in the result'() {
        expect:
        !getBulkWriteResult(UPDATE, getCommandResult(new BasicDBObject('n', 1))).isModifiedCountAvailable()
    }

    def 'should not have modified count for replace with no nModified field in the result'() {
        expect:
        !getBulkWriteResult(REPLACE, getCommandResult(new BasicDBObject('n', 1))).isModifiedCountAvailable()
    }

    def 'should have modified count of 0 for insert with no nModified field in the result'() {
        expect:
        0 == getBulkWriteResult(INSERT, getCommandResult(new BasicDBObject('n', 1))).getModifiedCount()
    }

    def 'should have modified count of 0 for remove with no nModified field in the result'() {
        expect:
        0 == getBulkWriteResult(REMOVE, getCommandResult(new BasicDBObject('n', 1))).getModifiedCount()
    }

    def 'should not have error if writeErrors is empty and writeConcernError is missing'() {
        expect:
        !hasError(getCommandResult(new BasicDBObject()));
    }

    def 'should have error if writeErrors is not empty'() {
        expect:
        hasError(getCommandResult(new BasicDBObject('writeErrors',
                                                    [new BasicDBObject('index', 3)
                                                             .append('code', 100)
                                                             .append('errmsg', 'some error')])));
    }

    def 'should have error if writeConcernError is present'() {
        expect:
        hasError(getCommandResult(new BasicDBObject('writeConcernError',
                                                    new BasicDBObject('code', 75)
                                                            .append('errmsg', 'wtimeout')
                                                            .append('errInfo', new BasicDBObject('wtimeout', '0')))))
    }

    def 'getting bulk write exception should throw if there are no errors'() {
        when:
        getBulkWriteException(INSERT, getCommandResult(new BasicDBObject()))

        then:
        thrown(MongoInternalException)
    }

    def 'should get write errors from the writeErrors field'() {
        expect:
        [new BulkWriteError(100, 'some error', new BasicDBObject(), 3),
         new BulkWriteError(11000, 'duplicate key', new BasicDBObject('_id', 'id1'), 5)] ==
        getBulkWriteException(INSERT, getCommandResult(new BasicDBObject('ok', 0)
                                                               .append('n', 1)
                                                               .append('code', 65)
                                                               .append('errmsg', 'bulk op errors')
                                                               .append('writeErrors',
                                                                       [new BasicDBObject('index', 3)
                                                                                .append('code', 100)
                                                                                .append('errmsg', 'some error'),
                                                                        new BasicDBObject('index', 5)
                                                                                .append('code', 11000)
                                                                                .append('errmsg', 'duplicate key')
                                                                                .append('errInfo',
                                                                                        new BasicDBObject('_id', 'id1'))]))).writeErrors

    }

    def 'should get write concern error from writeConcernError field'() {
        expect:
        new WriteConcernError(75, 'wtimeout', new BasicDBObject('wtimeout', '0')) ==
        getBulkWriteException(INSERT, getCommandResult(new BasicDBObject('n', 1)
                                                               .append('writeConcernError',
                                                                       new BasicDBObject('code', 75)
                                                                               .append('errmsg', 'wtimeout')
                                                                               .append('errInfo', new BasicDBObject('wtimeout', '0')))))
                .writeConcernError
    }


    def getCommandResult(BasicDBObject document) {
        def commandResult = new CommandResult(new ServerAddress())
        commandResult.putAll(document)
        commandResult
    }
}
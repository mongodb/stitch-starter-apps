package com.mongodb.stitch.starter_app.model.objects;

/*
 * Copyright 2018-present MongoDB, Inc.
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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Date;

import org.bson.BSON;
import org.bson.BsonReader;
import org.bson.BsonString;
import org.bson.BsonValue;
import org.bson.BsonWriter;
import org.bson.Document;
import org.bson.codecs.CollectibleCodec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.DocumentCodec;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.pojo.annotations.BsonCreator;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonIgnore;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;

/**
 * Friend represents a friend that would exist in a FriendList. MongoDB POJO annotations are included
 * so Friend objects can be directly inserted and retrieved from a MongoDB collection instance.
 * See http://mongodb.github.io/mongo-java-driver/3.6/driver/getting-started/quick-start-pojo/
 */
public class Friend {
    public static final String OWNER_KEY = "owner_id";
    public static final String FRIEND_NAME = "name";
    public static final String DATE_ADDED = "date_added";

    @NonNull
    private ObjectId id;

    @Nullable
    private ObjectId ownerId;
    private String friendName;
    private String dateAdded;

    /**
     * The @BsonCreator annotation tells the codec generator that this is the constructor that
     * both the remote MongoDB and local MongoDB will use to construct a Friend object
     * from BSON that it gets from a database instance
     */
    @BsonCreator
    public Friend(
            @NonNull @BsonId final ObjectId id,
            @BsonProperty(OWNER_KEY) final ObjectId ownerId,
            @BsonProperty(FRIEND_NAME) final String friendName,
            @BsonProperty(DATE_ADDED) final String dateAdded) {

        this.id = id;
        this.ownerId = ownerId;
        this.dateAdded = dateAdded;
        this.friendName = friendName;
    }

    /**
     *
     * The following getters are what the codec generator will use to generate a BSON document out
     * of a Friend object when writing to a database instance.
     */
    // Getters
    @NonNull
    @BsonId
    public ObjectId getId() {
        return id;
    }

    @Nullable
    @BsonProperty(OWNER_KEY)
    public ObjectId getOwnerId() {
        return ownerId;
    }

    @Nullable
    @BsonProperty(FRIEND_NAME)
    public String getFriendName() {
        return friendName;
    }

    @Nullable
    @BsonProperty(DATE_ADDED)
    public String getDateAdded() {
        return dateAdded;
    }
}

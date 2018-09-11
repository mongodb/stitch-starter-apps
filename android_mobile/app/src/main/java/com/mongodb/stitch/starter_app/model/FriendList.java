package com.mongodb.stitch.starter_app.model;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.stitch.android.core.Stitch;
import com.mongodb.stitch.android.core.StitchAppClient;
import com.mongodb.stitch.android.core.auth.StitchUser;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoClient;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoCollection;
import com.mongodb.stitch.core.StitchAppClientConfiguration;
import com.mongodb.stitch.core.auth.providers.anonymous.AnonymousCredential;
import com.mongodb.stitch.core.auth.providers.userpassword.UserPasswordCredential;
import com.mongodb.stitch.android.services.mongodb.local.LocalMongoDbService;
import com.mongodb.stitch.core.internal.common.BsonUtils;
import com.mongodb.stitch.starter_app.R;

import com.mongodb.stitch.starter_app.model.objects.Friend;
import com.mongodb.stitch.starter_app.model.objects.User;

import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.pojo.PojoCodecProvider;


import java.util.ArrayList;
import java.util.List;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

/**
 * This is the model for the application. It contains no Android UI-specific logic, but it contains
 * all the logic of an authenticated task list that can be accessed via Stitch. It should only
 * expose logic that the Activity (controller) needs to see. For that reason, it does not publicly
 * expose any Stitch-specific classes.
 */
public class FriendList {
    public static final String FRIEND_LIST_DATABASE = "StarterApp";
    public static final String FRIEND_LIST_COLLECTION = "friends";

    // Stitch specific fields
    private String _stitchClientAppId;
    private StitchAppClient _stitchClient;
    private RemoteMongoCollection<Friend> _remoteFriendCollection;
    private MongoCollection<Friend> _localFriendCollection;
    private MongoClient _mobileClient;
    private User _user;

    // General fields
    private final List<Friend> _cachedRemoteDataList;
    private final List<Friend> _cachedLocalDataList;

    public FriendList(final Context context) {
        this._stitchClientAppId = context.getString(R.string.friend_list_stitch_client_app_id);
        this._initializeStitch(context);
        this._cachedRemoteDataList = new ArrayList<>();
        this._cachedLocalDataList = new ArrayList<>();
    }

    /**
     * Initializes the _stitchClient field, calling Stitch's static client initialization functions
     * if the client has never been initialized before.
     */
    private synchronized void _initializeStitch(Context context) {
        Stitch.initialize(context);

        // Set up codecs that will allow us to create a MongoDB collection of Friend objects.
        CodecProvider friendListCodecProvider = PojoCodecProvider
                .builder()
                .register(Friend.class)
                .build();

        if (!Stitch.hasAppClient(_stitchClientAppId)) {
            // Initialize the Stitch app client for the first time.
            Stitch.initializeAppClient(
                    _stitchClientAppId,
                    new StitchAppClientConfiguration.Builder()
                            .withCodecRegistry(fromProviders(friendListCodecProvider))
                            .build()
            );

            _stitchClient = Stitch.getAppClient(_stitchClientAppId);

            //Set up Atlas collection
            _remoteFriendCollection = _stitchClient
                    .getServiceClient(RemoteMongoClient.factory, "mongodb-atlas")
                    .getDatabase(FRIEND_LIST_DATABASE)
                    .getCollection(FRIEND_LIST_COLLECTION, Friend.class);

            //Set up local DB collection
            _mobileClient = _stitchClient.getServiceClient(LocalMongoDbService.clientFactory);
            _localFriendCollection = _mobileClient
                    .getDatabase(FRIEND_LIST_DATABASE)
                    .getCollection(FRIEND_LIST_COLLECTION, Friend.class)
                    .withCodecRegistry(fromRegistries(
                            BsonUtils.DEFAULT_CODEC_REGISTRY,
                            fromProviders(friendListCodecProvider))
                    );
        }
    }

    public User getUser(){
        return _user;
    }
    /**
     * Returns whether or not a user is currently logged into the FriendList.
     *
     * @return whether or not a user is currently logged into the FriendList.
     */
    public Boolean isLoggedIn() {
        if (_stitchClient.getAuth().getUser()==null) {
        }

        else if ( _stitchClient.getAuth().isLoggedIn()){
           StitchUser foobar =  _stitchClient.getAuth().getUser();
         _user = new User(_stitchClient.getAuth().getUser());
        }
        return _stitchClient.getAuth().isLoggedIn();
    }

    /**
     * Gets the data from the Atlas collection. In this example, it returns all records. If you want
     * to only return the records that match the authenticated user, change the .find() call below to
     *    .find("owner_id", new ObjectId(authedUser.getId())
     * @return Task<Void>
     */
    public Task<Void> getRemoteData() {

        if (!_stitchClient.getAuth().isLoggedIn()) {
            return Tasks.forException(new IllegalStateException("Must be logged in to refresh list."));
        }

        final List<Friend> findResult = new ArrayList<>();

        return _remoteFriendCollection.find(new Document())
                .into(findResult).continueWithTask(new Continuation<List<Friend>, Task<Void>>() {
                    @Override
                    public Task<Void> then(@NonNull Task<List<Friend>> task) throws Exception {
                        if (!task.isSuccessful()) {
                            if (task.getException() != null) {
                                throw task.getException();
                            }
                            throw new IllegalStateException("Refreshing the list failed for unknown reason.");
                        }

                        FriendList.this._cachedRemoteDataList.clear();
                        FriendList.this._cachedRemoteDataList.addAll(findResult);

                        return Tasks.forResult(null);
                    }
                });
    }

    /**
     * Copies the RemoteData to the local DB. Because calls to the local DB are synchronous,
     * we need to catch exceptions and return them as a Task.
     */
    public Task<ArrayList<Friend>> copyToLocal() {
        if (_cachedRemoteDataList.size() <= 0) {

            IllegalStateException ise =  new IllegalStateException("You need to get the remote data before making a local copy.");
            return Tasks.forException(ise);
        }

        try {
            deleteLocal();

            _localFriendCollection.insertMany(_cachedRemoteDataList);

            ArrayList<Friend> results = new ArrayList<>();
            _localFriendCollection.find(Friend.class).into(results);

            this._cachedLocalDataList.addAll(results);
            return Tasks.forResult(results);

        } catch (Exception ex){
            return Tasks.forException(ex);
        }
    }

    /**
     * Copies the RemoteData to the local DB. Because calls to the local DB are synchronous,
     * we need to catch exceptions and return them as a Task.
     */
    public Task<Long> deleteLocal() {
        try {
            DeleteResult deleteResult = _localFriendCollection.deleteMany(new BsonDocument());
            FriendList.this._cachedLocalDataList.clear();
            return Tasks.forResult(deleteResult.getDeletedCount());
        } catch (Exception ex) {
            return Tasks.forException(ex);
        }
    }

    /**
     * Logs into this FriendList anonymously, via Stitch under the hood.
     *
     * @return
     */
    public Task<User> loginAnonymously() {

        if (_stitchClient.getAuth().isLoggedIn()) {
            return Tasks.forException(new IllegalStateException("You must be logged out first."));
        }
        return _stitchClient.getAuth().loginWithCredential(new AnonymousCredential())
                .continueWithTask(new Continuation<StitchUser, Task<User>>() {
                    @Override
                    public Task<User> then(@NonNull Task<StitchUser> task) throws Exception {
                        if (task.isSuccessful()) {
                            _user = new User(task.getResult());
                            return Tasks.forResult(_user);
                        } else return Tasks.forException(task.getException());
                    }
                });
    }

    /**
     * Logs into this FriendList using email/password, via Stitch under the hood.
     *
     * @return
     */
    public Task<User> loginEmail(String email, String password) {
        if (_stitchClient.getAuth().isLoggedIn()) {
            return Tasks.forException(new IllegalStateException("Must be logged out first."));
        }
        return _stitchClient.getAuth().loginWithCredential(new UserPasswordCredential(email, password))
                .continueWithTask(new Continuation<StitchUser, Task<User>>() {
                    @Override
                    public Task<User> then(@NonNull Task<StitchUser> task) throws Exception {
                        if (task.isSuccessful()) {
                            _user = new User(task.getResult());
                            return Tasks.forResult(_user);
                        } else return Tasks.forException(task.getException());
                    }
                });
    }

    /**
     * Clears the cached list of friends and triggers a logout in Stitch.
     */
    public void logout() {
        this._cachedRemoteDataList.clear();
        this._stitchClient.getAuth().logout().continueWith(new Continuation<Void, Void>() {
            @Override
            public Void then(@NonNull Task<Void> task) {
                _cachedLocalDataList.clear();
                _cachedRemoteDataList.clear();
                return null;
            }
        });
    }

    /**
     * Retrieves the cached list of Friend objects that this FriendList holds.
     *
     * @return a list of Friend objects
     */
    public List<Friend> getCachedRemoteItems() {
        return _cachedRemoteDataList;
    }

    public List<Friend> getCachedLocalItems() {
        return _cachedLocalDataList;
    }

    public Task callFunctionSayHello() {
        return this._stitchClient.callFunction("SayHello", null, String.class);
    }
}

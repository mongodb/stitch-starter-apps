![alt text][logo]
# MongoDb Android Starter App
An Android starter project for working with MongoDB Mobile &amp;
[Stitch](https://docs.mongodb.com/stitch/).

At MongoDB World 2018, Mongo announced the beta of 
[MongoDB Mobile](https://www.mongodb.com/use-cases/mobile), which 
enables you to use Mongo on your Android and iOS devices. This starter 
app provides a way to get up and running quickly (with zero config), and 
then either customize it or steal the code for your own projects.

# Requirements
For a list of platforms supported by MongoDB Mobile, see the
[MongoDB Mobile docs](https://docs.mongodb.com/stitch/mongodb/mobile/mobile-overview/).

# Getting Started

1. Clone or download this repo.
2. Copy the MongoDB Mobile tarball from one of the following locations:

   | Platform    | Link |
   |-------------|------|
   | x86_64      | https://s3.amazonaws.com/mciuploads/mongodb-mongo-v4.0/embedded-sdk/embedded-sdk-android-x86_64-latest.tgz |
   | arm64-v8a   | https://s3.amazonaws.com/mciuploads/mongodb-mongo-v4.0/embedded-sdk/embedded-sdk-android-arm64-latest.tgz  |
   | armeabi-v7a | https://s3.amazonaws.com/mciuploads/mongodb-mongo-v4.0/embedded-sdk/embedded-sdk-android-arm32-latest.tgz  |

3. Extract the tarball, and then copy the contents of the */lib/* folder to /app/src/main/jniLibs/_platform_, where
_platform_ matches one of the 3 listed above. When finished, your
directory structure should look similar to this:
   ```
   MongoDbMobile_Starter
   |-> app
      |-> src
         |-> main
            |-> jniLibs
                  |-> x86_64 (or arm64-v8a or armeabi-v7a)
                        |-> cmake
                        |-> pkgconfig
                        |-> libaccumulator.so
                        |-> etc.
   ```

4. Open the project in Android Studio and explore!

## Customize it!

The purpose of this starter app is to give you a foundation on which to 
build your own Stitch & Mobile-enabled Android apps. 

## Read More

MongoDB Mobile also works on iOS! See https://docs.mongodb.com/stitch/mongodb/mobile/mobile-overview/
to get started.

https://docs.mongodb.com/stitch/mongodb/mobile/build-mobile/

[logo]: https://pbs.twimg.com/media/DC3eZkzXkAAGPhI.jpg
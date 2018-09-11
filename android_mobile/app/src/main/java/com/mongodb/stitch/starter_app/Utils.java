package com.mongodb.stitch.starter_app;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.mongodb.stitch.starter_app.model.FriendList;

public class Utils {
    public static <T> Task<T> displayToastIfTaskFails(
            final Context context,
            final Task<T> task,
            final String errorMessage
    ) {
        return task.continueWithTask(new Continuation<T, Task<T>>() {
            @Override
            public Task<T> then(@NonNull Task<T> task) {
                if (!task.isSuccessful()) {
                    if (task.getException() != null) {
                        Log.d(Utils.class.getName(), errorMessage + ": " + task.getException().getMessage());
                        Toast.makeText(context, errorMessage + ": " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                    throw new IllegalStateException();
                } else {
                    return Tasks.forResult(task.getResult());
                }
            }
        });
    }
}

package edu.dartmouth.cs.myrun.dblayer;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.AsyncTaskLoader;

import java.util.List;

public class ExerciseEntryListLoader extends AsyncTaskLoader<List<ExerciseEntry>> {
    //小尖括号里面的是  要load的东西 的类型
    // 这次要读的是一个Entry的list

    private ExerciseEntryDataSource dataSource;
    private String userEmail;

    public ExerciseEntryListLoader(@NonNull Context context, @Nullable String userEmail) {
        super(context);
        // create the database
        this.dataSource = new ExerciseEntryDataSource(context);
        this.userEmail = userEmail;
    }

    @Nullable
    @Override
    public List<ExerciseEntry> loadInBackground() {
        if (userEmail != null) {
            return dataSource.getAllExerciseEntriesOfUser(userEmail);
        }
        else {
            return dataSource.getAllExerciseEntries();
        }
    }
}

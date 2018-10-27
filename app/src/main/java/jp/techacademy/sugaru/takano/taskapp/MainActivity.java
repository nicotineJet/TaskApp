package jp.techacademy.sugaru.takano.taskapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.Sort;

public class MainActivity extends AppCompatActivity {
    public final static String EXTRA_TASK = "jp.techacademy.sugaru.takano.taskapp.TASK";

    //Realmクラスを保持するmRealmを定義
    private Realm mRealm;
    //mRealmListenerはRealmデータベースに変化があった場合に呼ばれるリスナー
    private RealmChangeListener mRealmListener = new RealmChangeListener() {
        @Override
        public void onChange(Object element) {
            reloadListView();
        }
    };

    private ListView mListView;
    private TaskAdapter mTaskAdapter;

    private EditText categorySearchText;
    private Button categorySearchButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,InputActivity.class);
                startActivity(intent);
            }
        });

        //Realmの設定

        //オブジェクトを取得
        mRealm = Realm.getDefaultInstance();
        //mRealmListenerを設定
        mRealm.addChangeListener(mRealmListener);

        //ListViewの設定
        //↓このthisは何?
        mTaskAdapter = new TaskAdapter(MainActivity.this);
        mListView = (ListView) findViewById(R.id.listView1);

        categorySearchText = (EditText) findViewById(R.id.category_search_text);
        categorySearchButton = (Button)findViewById(R.id.category_search_button);

        categorySearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchListView();
            }
        });

        //ListViewをタップしたときの処理
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //入力・編集画面に遷移させる
                Task task = (Task) parent.getAdapter().getItem(position);

                Intent intent = new Intent(MainActivity.this,InputActivity.class);
                intent.putExtra(EXTRA_TASK,task.getId());

                startActivity(intent);
            }
        });
        //ListViewを長押しした時の処理
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                //タスクを削除する

                final Task task = (Task)parent.getAdapter().getItem(position);

                //ダイアログを表示する
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                builder.setTitle("削除");
                builder.setMessage(task.getTitle() + "を削除しますか");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        RealmResults<Task> results =mRealm.where(Task.class).equalTo("id",task.getId()).findAll();

                        mRealm.beginTransaction();
                        results.deleteAllFromRealm();
                        mRealm.commitTransaction();

                        Intent resultIntent = new Intent(getApplicationContext(),TaskAlarmReceiver.class);
                        PendingIntent resultPendingIntent = PendingIntent.getBroadcast(
                                MainActivity.this,
                                task.getId(),
                                resultIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT
                        );

                        AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
                        alarmManager.cancel(resultPendingIntent);

                        reloadListView();
                    }
                });
                builder.setNegativeButton("CANCEL",null);

                AlertDialog dialog = builder.create();
                dialog.show();

                return true;
            }
        });

        reloadListView();
    }

    private void reloadListView(){
        //Realmデータベースから、「全てのデータを取得して新しい日時順に並べた結果」を取得   Sort.DESCENDINGは降順
        RealmResults<Task> taskRealmResults = mRealm.where(Task.class).findAll().sort("date", Sort.DESCENDING);
        //上記の結果を、TaskListとしてセットする
        mTaskAdapter.setTaskList(mRealm.copyFromRealm(taskRealmResults));
        //TaskのListView用のアダプタに渡す
        mListView.setAdapter(mTaskAdapter);
        //表示を更新するために、アダプターにデータが変更されたことを知らせる
        mTaskAdapter.notifyDataSetChanged();
    }

    private void searchListView(){
        String a = categorySearchText.getText().toString();
        if(a.length() != 0) {
            RealmResults<Task> taskRealmResults = mRealm.where(Task.class).equalTo("category", a).findAll().sort("date", Sort.DESCENDING);
            //上記の結果を、TaskListとしてセットする
            mTaskAdapter.setTaskList(mRealm.copyFromRealm(taskRealmResults));
            //TaskのListView用のアダプタに渡す
            mListView.setAdapter(mTaskAdapter);
            //表示を更新するために、アダプターにデータが変更されたことを知らせる
            mTaskAdapter.notifyDataSetChanged();
        }else{
            reloadListView();
        }
    }

    //onDestroyはActivityが破棄されるときに呼び出される
    @Override
    protected void onDestroy(){
        super.onDestroy();

        mRealm.close();
    }
}

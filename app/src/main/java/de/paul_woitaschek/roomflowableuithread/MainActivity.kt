package de.paul_woitaschek.roomflowableuithread

import android.annotation.SuppressLint
import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Database
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.arch.persistence.room.Query
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.os.Bundle
import android.os.StrictMode
import android.support.v7.app.AppCompatActivity
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {


  @SuppressLint("CheckResult")
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    val threadPolicy =
      StrictMode.ThreadPolicy.Builder()
        .detectDiskReads()
        .detectDiskWrites()
        .penaltyLog()
        .build()
    StrictMode.setThreadPolicy(threadPolicy)

    val dao = Room.databaseBuilder(this, AppDb::class.java, "myRandomDatabase")
      .build()
      .dao()

    val disposable = dao.stream()
      .subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe()

    Single.timer(1, TimeUnit.SECONDS, AndroidSchedulers.mainThread())
      .subscribe { it ->
        disposable.dispose()
      }
  }

  @Database(
    entities = [MyEntity::class],
    version = 3
  )
  abstract class AppDb : RoomDatabase() {

    abstract fun dao(): MyDao
  }

  @Dao
  interface MyDao {

    @Query("SELECT * FROM myEntity")
    fun stream(): Flowable<List<MyEntity>>
  }

  @Entity(tableName = "myEntity")
  data class MyEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long
  )
}

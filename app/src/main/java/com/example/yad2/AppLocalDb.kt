package com.example.yad2

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.yad2.models.Convertors
import com.example.yad2.models.Product
import com.example.yad2.models.User
import com.example.yad2.models.dao.ProductDao
import com.example.yad2.models.dao.UserDao

@Database(entities = [Product::class, User::class], version = 13)
@TypeConverters(*[Convertors::class])
abstract class AppLocalDbRepository : RoomDatabase() {
    abstract fun productDao(): ProductDao?
    abstract fun userDao(): UserDao?
}

object AppLocalDb {
    var db: AppLocalDbRepository = Room.databaseBuilder(
        MyApplication.getContext(),
        AppLocalDbRepository::class.java,
        "dbFile.db"
    )
        .fallbackToDestructiveMigration()
        .build()
}

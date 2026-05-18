package com.example.adas_application

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Annotation::class], version = 1, exportSchema = false)
abstract class AnnotationDatabase : RoomDatabase() {

    abstract fun annotationDao(): AnnotationDao

    companion object {
        @Volatile
        private var INSTANCE: AnnotationDatabase? = null

        fun getDatabase(context: Context): AnnotationDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AnnotationDatabase::class.java,
                    "annotation_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

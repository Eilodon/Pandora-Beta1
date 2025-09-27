package com.pandora.core.cac.di

import android.content.Context
import androidx.room.Room
import com.pandora.core.cac.db.CACDao
import com.pandora.core.cac.db.CACDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideCACDatabase(@ApplicationContext context: Context): CACDatabase {
        return Room.databaseBuilder(
            context,
            CACDatabase::class.java,
            "pandora_cac.db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideCACDao(database: CACDatabase): CACDao {
        return database.dao()
    }
}

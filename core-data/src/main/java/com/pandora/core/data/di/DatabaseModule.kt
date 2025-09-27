package com.pandora.core.data.di

import android.content.Context
import androidx.room.Room
import com.pandora.core.cac.db.MemoryDao
import com.pandora.core.data.PandoraDatabase
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
    fun providePandoraDatabase(@ApplicationContext context: Context): PandoraDatabase {
        return Room.databaseBuilder(
            context,
            PandoraDatabase::class.java,
            "pandora_os.db"
        ).build()
    }

    @Provides
    fun provideMemoryDao(database: PandoraDatabase): MemoryDao {
        return database.memoryDao()
    }
}

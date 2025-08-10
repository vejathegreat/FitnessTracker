package com.velaphi.core.data.repository;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava"
})
public final class WorkoutRepositoryImpl_Factory implements Factory<WorkoutRepositoryImpl> {
  @Override
  public WorkoutRepositoryImpl get() {
    return newInstance();
  }

  public static WorkoutRepositoryImpl_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static WorkoutRepositoryImpl newInstance() {
    return new WorkoutRepositoryImpl();
  }

  private static final class InstanceHolder {
    private static final WorkoutRepositoryImpl_Factory INSTANCE = new WorkoutRepositoryImpl_Factory();
  }
}

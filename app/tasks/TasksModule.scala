package tasks

import play.api.inject.{SimpleModule, bind}

class TasksModule extends SimpleModule(bind[BaseActorTask].toSelf.eagerly())
package com.angelo.dashboard.programs

import com.angelo.dashboard.layers.ZAppLayers.AppDependencies
import zio.RIO

object ZPrograms {

  val programs: List[RIO[AppDependencies, Unit]] =
    ZTableMakerProgram.initTable ::
      ZServerProgram.serveHttpRequests ::
      ZSchedulerProgram.scheduleSlackNotifications :: Nil
}

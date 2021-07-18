package com.angelo.dashboard.programs

import com.angelo.dashboard.layers.ZAppLayers.TableMakerEnvironment
import com.angelo.dashboard.services.ZIssueTableMaker
import com.angelo.dashboard.services.ZIssueTableMaker._
import zio.URIO
import zio.duration.durationInt
import zio.logging.Logging.info

object ZTableMakerProgram {

  val initTable: URIO[TableMakerEnvironment, Unit] =
    ZIssueTableMaker.service
      .flatMap(_.makeTable)
      .refineToOrDie[TableAlreadyExists]
      .tapError(err => info(s"Table ${err.tableName} already exists. Nothing to do"))
      .delay(5.seconds) //give the db container time to start
      .ignore
}

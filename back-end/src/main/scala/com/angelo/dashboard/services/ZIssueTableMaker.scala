package com.angelo.dashboard.services

import com.angelo.dashboard.client.ZDbClient
import com.angelo.dashboard.client.ZDbClient.ZDbClient
import zio.{Has, Task, URIO, URLayer, ZIO, ZLayer}

object ZIssueTableMaker {

  type ZIssueTableMaker = Has[Service]

  trait Service {
    def makeTable: Task[Unit]
  }

  /** a very thin layer (pun intended) */
  val live: URLayer[ZDbClient, ZIssueTableMaker] =
    ZLayer.fromService[ZDbClient.Service, Service] { client =>
      new Service {
        override def makeTable: Task[Unit] = client.createTable
      }
    }

  // accessor
  val service: URIO[ZIssueTableMaker, Service] = ZIO.service[Service]
}
